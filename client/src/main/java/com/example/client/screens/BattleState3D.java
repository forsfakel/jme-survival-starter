package com.example.client.screens;

import com.example.client.GameApp;
import com.example.client.PlayerContext;
import com.example.shared.messages.BattleEndReport;
import com.example.shared.messages.PlayerHpSync;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;

public class BattleState3D extends BaseAppState {

    public enum Arena {SURFACE, MINE_TUNNEL}

    private PlayerContext pc;
    private final String battleId;
    private final Arena arena;
    private final Runnable onExit;
    private float maxStepUp = 0.6f; // макс. одноразовий підйом по висоті (≈ 1 м)

    private SimpleApplication app;
    private Node localRoot = new Node("BattleRoot");

    private TerrainQuad terrain;
    private Geometry playerGeom;


    private BitmapText hint;
    private ActionListener exitListener, moveListener, attackListener;

    // рух гравця
    private boolean mvF, mvB, mvL, mvR;
    private float moveSpeed = 6f;

    private BitmapText hpText;

    // камера зверху під кутом
    private final Vector3f camOffset = new Vector3f(0, 32f, 38f);
    private float camLerp = 8f;

    // межі руху (переобчислюються від терейну)
    private float minX = -5f, maxX = 5f, minZ = -10f, maxZ = 10f;

    // debug-маркери
    private boolean debugAdded = false;

    // --- combat ---
    private int playerHP;
    private int playerMaxHP;
    private float attackCooldown = 0.5f; // сек
    private float attackTimer = 0f;
    private float attackRange = 1.8f;
    private float attackArcCos = FastMath.cos(FastMath.DEG_TO_RAD * 70f); // кут вліво/вправо ≈70°
    private boolean attackPressed = false;

    private java.util.ArrayList<Enemy> enemies = new java.util.ArrayList<>();
    private float enemySpeed = 3f;
    private float enemyAttackCooldown = 1.2f;
    private float enemyAttackRange = 1.6f;
    private int enemyDamage = 8;

    // --- slope limit (боремось з лазінням на стіни/круті схили) ---
    private float maxSlopeDeg = 60f;                         // максимально дозволений нахил
    private float minNormalY = FastMath.cos(maxSlopeDeg * FastMath.DEG_TO_RAD); // поріг по normal.y

    // === Damage popups (цифри шкоди) ===
    private static final class DmgPopup {
        BitmapText text;
        float life = 0.9f;     // сек
        Vector3f world;        // стартова світова точка
        float vy = 1.2f;       // швидкість підйому (у world Y)
    }

    private final java.util.ArrayList<DmgPopup> dmgPopups = new java.util.ArrayList<>();

    // === Knockback ===
    private Vector3f playerKbVel = new Vector3f(0, 0, 0);
    private float kbDamp = 6f;      // коеф. згасання/тертя (чим більше — тим швидше гасне)
    private float kbScaleOnHit = 6f; // сила відкиду

    // === Telegraph / AI стан ворога ===
    private enum EState {CHASE, WINDUP, RECOVER}

    private static final class Enemy {
        Node node;            // корінь ворога
        Geometry body;        // сфера-тіло
        BitmapText hpLabel;
        int hp = 40, hpMax = 40;
        float atkCd = 0f;
        boolean alive = true;

        EState st = EState.CHASE;
        float timer = 0f;
        float windupDur = 0.6f;
        float recoverDur = 0.3f;
        Geometry telegraphCone; // телеграф (Geometry)
    }

    public BattleState3D(String battleId, Arena arena, Runnable onExit) {
        this.battleId = battleId;
        this.arena = arena;
        this.onExit = onExit;
    }

    @Override
    protected void initialize(Application application) {
        // 0) app
        this.app = (SimpleApplication) application;

        // Контекст гравця
        this.pc = com.example.client.PlayerContext.get();
        // (опціонально) лінт-захист
        if (this.pc == null) {
            throw new IllegalStateException("PlayerContext.get() returned null");
        }
        // ініціалізація HP бою з контексту
        this.playerMaxHP = pc.getHpMax();
        this.playerHP = pc.getHp();

        // 3) Позначити, що в бою
        pc.setInBattle(true);

        System.out.println(
                "[CLIENT] BattleState3D.init HP=" + this.playerHP + "/" + this.playerMaxHP);

        // 1) камера (перспектива) + фон
        var cam = app.getCamera();
        cam.setParallelProjection(false);
        float aspect = (float) app.getContext().getSettings().getWidth() / (float) app.getContext()
                                                                                           .getSettings()
                                                                                           .getHeight();
        cam.setFrustumPerspective(55f, aspect, 0.1f, 2000f);
        app.getViewPort().setBackgroundColor(ColorRGBA.DarkGray);

        // 2) керування курсором / flyCam
        app.getFlyByCamera().setEnabled(false);
        app.getInputManager().setCursorVisible(false);

        // 3) корінь підсцени
        app.getRootNode().attachChild(localRoot);

        // 4) світло
        localRoot.addLight(new AmbientLight(ColorRGBA.White.mult(0.35f)));
        var sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1, -1, -0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        localRoot.addLight(sun);

        // 5) арена з heightmap
        switch (arena) {
            case MINE_TUNNEL -> buildArenaFromHeightmap("Textures/terrains/mine_height.png",
                    "Textures/terrains/mine_floor.png");
            case SURFACE -> buildArenaFromHeightmap("Textures/terrains/surface_height.png",
                    "Textures/terrains/grass.png");
        }

        // межі за фактичною позицією/масштабом терейну
        applyBoundsFromTerrain();
        float border = 2.0f; // відступ від краю, щоб не спавнити на бортах
        minX += border;
        maxX -= border;
        minZ += border;
        maxZ -= border;

        // --- Спавн у низинах: випадковий X, мінімум Y уздовж Z ---
        float stepZ = 0.25f;   // крок сканування вздовж Z
        float padX = 1.5f;    // відступ від країв X (по X)
        float clearance = 0.8f; // вимога пласкості/кліренсу
        java.util.ArrayList<Vector3f> placed = new java.util.ArrayList<>();
        Vector3f heroStart = pickValleyWithSeparation(placed, 0f, stepZ, padX, clearance);
        placed.add(heroStart);

        // --- тепер створюємо гравця на знайденій точці ---
        playerGeom = sphere(0.6f, new ColorRGBA(0.2f, 0.7f, 0.9f, 1f));
        localRoot.attachChild(playerGeom);
        playerGeom.setLocalTranslation(heroStart);

        // 8) камера після гравця
        placeCameraInstant();

        // 8) (опційно) вороги-заглушки
        // 8) вороги: спавнимо теж у низинах з мінімальною дистанцією
        SpawnConfig sc = new SpawnConfig();
        sc.count = 3;         // змінюй як потрібно
        sc.minDist = 3.0f;    // відстань між усіма спавнами
        sc.stepZ = 0.25f;
        sc.padX = 1.5f;
        sc.clearance = 0.8f;
        sc.attempts = 40;

        spawnEnemiesValley(heroStart, sc);

        // 9) debug-маркери один раз
        addDebugMarkersOnce();

        // 10) HUD-підказка
        hint = new BitmapText(app.getAssetManager().loadFont("Interface/Fonts/Default.fnt"), false);
        hint.setSize(18);
        hint.setText("БІЙ (демо). WASD — рух; ENTER — перемога; ESC — відступ.");
        int sh = app.getContext().getSettings().getHeight();
        hint.setLocalTranslation(20, sh - 20, 0);
        app.getGuiNode().attachChild(hint);

        hpText = new BitmapText(app.getAssetManager().loadFont("Interface/Fonts/Default.fnt"),
                false);
        hpText.setSize(18);
        hpText.setColor(ColorRGBA.White);
        hpText.setLocalTranslation(20, 40, 0);
        hpText.setText("HP: " + playerHP + " / " + playerMaxHP);
        app.getGuiNode().attachChild(hpText);

        // 11) інпут

        // Стартові HP беремо з контексту ТУТ, після створення HUD (щоб одразу показати правильне)
        this.playerMaxHP = pc.getHpMax();
        this.playerHP = Math.min(pc.getHp(), this.playerMaxHP);

        var im = app.getInputManager();
        if (!im.hasMapping("BATTLE_WIN")) {
            im.addMapping("BATTLE_WIN", new KeyTrigger(KeyInput.KEY_RETURN));
        }
        if (!im.hasMapping("BATTLE_RUN")) {
            im.addMapping("BATTLE_RUN", new KeyTrigger(KeyInput.KEY_ESCAPE));
        }
        if (!im.hasMapping("P_FWD")) {
            im.addMapping("P_FWD", new KeyTrigger(KeyInput.KEY_W));
        }
        if (!im.hasMapping("P_BACK")) {
            im.addMapping("P_BACK", new KeyTrigger(KeyInput.KEY_S));
        }
        if (!im.hasMapping("P_LEFT")) {
            im.addMapping("P_LEFT", new KeyTrigger(KeyInput.KEY_A));
        }
        if (!im.hasMapping("P_RIGHT")) {
            im.addMapping("P_RIGHT", new KeyTrigger(KeyInput.KEY_D));
        }
        // атака (SPACE або LMB)

        if (!im.hasMapping("ATTACK")) {
            im.addMapping("ATTACK", new KeyTrigger(KeyInput.KEY_SPACE));
            // якщо хочеш — додатково: мапа на ЛКМ
            // im.addMapping("ATTACK", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        }
        attackListener = (name, pressed, tpf) -> {
            if (!"ATTACK".equals(name)) {
                return;
            }
            if (pressed) {
                attackPressed = true;
            }
        };
        im.addListener(attackListener, "ATTACK");

        exitListener = (name, pressed, tpf) -> {
            if (!pressed) {
                return;
            }
            if ("BATTLE_WIN".equals(name)) {
                ((com.example.client.GameApp) app).sendToServer(
                        new BattleEndReport(battleId, true));

                if (onExit != null) {
                    onExit.run();
                }

                // (опційно) синхронізація на сервер:
                ((com.example.client.GameApp) app).sendToServer(
                        new com.example.shared.messages.PlayerHpSync(pc.getHp(), pc.getHpMax()));
                getStateManager().detach(this);
            } else if ("BATTLE_RUN".equals(name)) {
                ((com.example.client.GameApp) app).sendToServer(
                        new BattleEndReport(battleId, false));

                if (onExit != null) {
                    onExit.run();
                }

                // (опційно) синхронізація на сервер:
                ((com.example.client.GameApp) app).sendToServer(
                        new com.example.shared.messages.PlayerHpSync(pc.getHp(), pc.getHpMax()));
                getStateManager().detach(this);
            }

            if ("BATTLE_WIN".equals(name)) {
                finishBattle(true);
            } else if ("BATTLE_RUN".equals(name)) {
                finishBattle(false);
            }
        };
        im.addListener(exitListener, "BATTLE_WIN", "BATTLE_RUN");

        moveListener = (name, pressed, tpf) -> {
            switch (name) {
                case "P_FWD" -> mvF = pressed;
                case "P_BACK" -> mvB = pressed;
                case "P_LEFT" -> mvL = pressed;
                case "P_RIGHT" -> mvR = pressed;
            }
        };
        im.addListener(moveListener, "P_FWD", "P_BACK", "P_LEFT", "P_RIGHT");
        minNormalY = FastMath.cos(maxSlopeDeg * FastMath.DEG_TO_RAD);


    }

    // --- побудова арени з heightmap ---
    private void buildArenaFromHeightmap(String heightmapPath, String texPath) {
        var heightTex = app.getAssetManager().loadTexture(heightmapPath);

        var hmap = new ImageBasedHeightMap(heightTex.getImage(), 1f);
        hmap.load();

        int size = hmap.getSize();   // 513 (2^N + 1)
        int patchSize = 65;

        terrain = new TerrainQuad("arena", patchSize, size, hmap.getHeightMap());

        // матеріал ДО attachChild
        Material mat = new Material(app.getAssetManager(),
                "Common/MatDefs/Terrain/TerrainLighting.j3md");
        Texture diff = app.getAssetManager().loadTexture(texPath);
        diff.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("DiffuseMap", diff);
        mat.setFloat("DiffuseMap_0_scale", 16f);
        terrain.setMaterial(mat);

        // трансформа (сплющуємо висоту, центруємо на (0,0))
        terrain.setLocalScale(1f, 0.30f, 1f);
        float half = (size - 1) * 0.5f;
        terrain.setLocalTranslation(-half * terrain.getLocalScale().x, 0f,
                -half * terrain.getLocalScale().z);

        // fallback, про всяк
        if (terrain.getMaterial() == null) {
            Material dbg = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            dbg.setColor("Color", ColorRGBA.Gray);
            terrain.setMaterial(dbg);
        }

        localRoot.attachChild(terrain);
    }

    private void applyBoundsFromTerrain() {
        if (terrain == null) {
            return;
        }
        int size = terrain.getTerrainSize(); // 513
        Vector3f ws = terrain.getWorldScale();
        Vector3f wt = terrain.getWorldTranslation();
        float halfX = (size - 1) * 0.5f * ws.x;
        float halfZ = (size - 1) * 0.5f * ws.z;

        minX = wt.x - halfX + 1f;
        maxX = wt.x + halfX - 1f;
        minZ = wt.z - halfZ + 1f;
        maxZ = wt.z + halfZ - 1f;
    }

    // --- утиліти геометрії ---
    private Geometry box(float x, float y, float z, ColorRGBA col) {
        Geometry g = new Geometry("box", new Box(x, y, z));
        Material m = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", col);
        g.setMaterial(m);
        return g;
    }

    private Geometry sphere(float r, ColorRGBA col) {
        Geometry g = new Geometry("sphere", new Sphere(16, 16, r));
        Material m = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", col);
        g.setMaterial(m);
        return g;
    }

    private Enemy spawnEnemy(Vector3f at) {
        var e = new Enemy();
        // Якщо прийшов тільки X/Z — підігнати Y під рельєф
        float gy = Float.isNaN(at.y) ? Float.NaN : at.y;
        if (Float.isNaN(gy) || Math.abs(gy) < 1e-4f) {
            float ground = safeHeightAt(at.x, at.z);
            if (!Float.isNaN(ground)) {
                at = new Vector3f(at.x, ground + 1f, at.z);
            } else {
                at = new Vector3f(at.x, 1f, at.z);
            }
        }

        Node enemyNode = new Node("enemy");
        enemyNode.setLocalTranslation(at);

        Geometry body = sphere(0.7f, ColorRGBA.Red);
        enemyNode.attachChild(body);

        Geometry tele = makeTelegraph();        // ← без параметрів
        tele.setCullHint(Spatial.CullHint.Always);
        enemyNode.attachChild(tele);

        localRoot.attachChild(enemyNode);

        e.node = enemyNode;
        e.body = body;
        e.telegraphCone = tele;

        e.hpLabel = new BitmapText(app.getAssetManager().loadFont("Interface/Fonts/Default.fnt"),
                false);
        e.hpLabel.setSize(14);
        e.hpLabel.setColor(ColorRGBA.White);
        e.hpLabel.setText(e.hp + " / " + e.hpMax);
        app.getGuiNode().attachChild(e.hpLabel);

        enemies.add(e);
        return e;
    }

    private void updateEnemyHpLabel(Enemy e) {
        if (e.hpLabel == null) {
            return;
        }
        // позиція над головою у 2D GUI
        Vector3f world = e.node.getWorldTranslation().add(0, 1.2f, 0);
        Vector3f screen = app.getCamera().getScreenCoordinates(world);
        e.hpLabel.setLocalTranslation(screen.x - e.hpLabel.getLineWidth() / 2f, screen.y + 10f, 0);
        e.hpLabel.setText(e.hp + " / " + e.hpMax);
    }

    private void addDebugMarkersOnce() {
        if (debugAdded) {
            return;
        }
        Geometry xLine = box(20f, 0.05f, 0.05f, ColorRGBA.Yellow);
        xLine.setLocalTranslation(0, 0.2f, 0);
        localRoot.attachChild(xLine);
        Geometry zLine = box(0.05f, 0.05f, 20f, ColorRGBA.Yellow);
        zLine.setLocalTranslation(0, 0.2f, 0);
        localRoot.attachChild(zLine);
        debugAdded = true;
    }

    private void placeCameraInstant() {
        Vector3f playerPos = playerGeom.getWorldTranslation();
        Vector3f camPos = playerPos.add(camOffset);
        app.getCamera().setLocation(camPos);
        app.getCamera().lookAt(playerPos, Vector3f.UNIT_Y);
    }

    private float safeHeightAt(float x, float z) {
        if (terrain == null) {
            return Float.NaN;
        }

        float h = terrain.getHeight(new Vector2f(x, z));
        if (!Float.isNaN(h)) {
            return h;
        }

        // raycast зверху вниз
        CollisionResults res = new CollisionResults();
        Ray ray = new Ray(new Vector3f(x, 1000f, z), Vector3f.UNIT_Y.negate());
        terrain.collideWith(ray, res);
        if (res.size() > 0) {
            return res.getClosestCollision().getContactPoint().y;
        }
        return Float.NaN;
    }

    @Override
    public void update(float tpf) {
        if (playerGeom == null) {
            return;
        }

        // таймери
        attackTimer = Math.max(0f, attackTimer - tpf);
        for (var e : enemies)
            e.atkCd = Math.max(0f, e.atkCd - tpf);

        // 1) Рух гравця
        Vector3f pos = playerGeom.getLocalTranslation();
        Vector3f dir = new Vector3f((mvR ? 1f : 0f) - (mvL ? 1f : 0f), 0f,
                (mvB ? 1f : 0f) - (mvF ? 1f : 0f));
        if (dir.lengthSquared() > 0f) {
            dir.normalizeLocal().multLocal(moveSpeed * tpf);
            Vector3f next = tryMoveOnTerrain(pos, dir);
            if (next.subtract(pos).lengthSquared() > 1e-6f) {
                float yaw = (float) Math.atan2(next.x - pos.x, next.z - pos.z);
                playerGeom.setLocalRotation(new Quaternion().fromAngles(0, -yaw, 0));
                pos = next;
                playerGeom.setLocalTranslation(pos);
            }

        }
        // Knockback інтеграція
        if (playerKbVel.lengthSquared() > 1e-4f) {
            Vector3f nextKb = applyKnockback(playerGeom.getLocalTranslation(), playerKbVel, tpf);
            playerGeom.setLocalTranslation(nextKb);
            // демпінг
            float damp = FastMath.exp(-kbDamp * tpf);
            playerKbVel.multLocal(damp);
        }

        // 2) Атака гравця
        if (attackPressed && attackTimer <= 0f) {
            attackPressed = false;
            attackTimer = attackCooldown;

            Enemy best = null;
            float bestDist = Float.MAX_VALUE;
            // вибір цілі
            Vector3f fw = forwardDir();
            for (var e : enemies) {
                if (!e.alive) {
                    continue;
                }
                float d = distXZ(pos, e.node.getWorldTranslation());
                if (d <= attackRange) {
                    Vector3f toE = e.node.getWorldTranslation().subtract(pos).normalizeLocal();
                    if (fw.dot(toE) >= attackArcCos) {
                        if (d < bestDist) {
                            bestDist = d;
                            best = e;
                        }
                    }
                }
            }

            // нанесення урону + відкидання
            if (best != null) {
                best.hp = Math.max(0, best.hp - 20);

                Vector3f push = best.node.getWorldTranslation().subtract(
                        playerGeom.getWorldTranslation());
                push.y = 0;
                if (push.lengthSquared() > 0) {
                    push.normalizeLocal().multLocal(2.2f);
                    Vector3f next = tryMoveOnTerrain(best.node.getLocalTranslation(),
                            push.mult(tpf * 8f));
                    best.node.setLocalTranslation(next);
                }

                showDamage(best.node.getWorldTranslation().add(0, 1.2f, 0), 20, ColorRGBA.Orange);
                flash(best.body, ColorRGBA.Orange, 120);   // ← flash на Geometry, тому body

                if (best.hp == 0) {
                    best.alive = false;
                    localRoot.detachChild(best.node);
                    if (best.hpLabel != null) {
                        app.getGuiNode().detachChild(best.hpLabel);
                    }

                    boolean anyAlive = enemies.stream().anyMatch(en -> en.alive);
                    if (!anyAlive) {
                        ((com.example.client.GameApp) app).sendToServer(
                                new BattleEndReport(battleId, true));
                        if (onExit != null) {
                            onExit.run();
                        }
                        getStateManager().detach(this);
                        return;
                    }
                }
            }
        } else {
            attackPressed = false; // не накопичуємо натиск
        }

        // 3) AI ворогів: наближення та атака
        for (var e : enemies) {
            if (!e.alive) {
                continue;
            }

            Vector3f ep = e.node.getLocalTranslation();
            float d = distXZ(ep, pos);

            switch (e.st) {
                case CHASE -> {
                    if (d > enemyAttackRange) {
                        Vector3f step = new Vector3f(pos.x - ep.x, 0f, pos.z - ep.z);
                        if (step.lengthSquared() > 0f) {
                            step.normalizeLocal().multLocal(enemySpeed * tpf);
                            Vector3f next = tryMoveOnTerrain(ep, step);
                            if (!next.equals(ep)) {
                                float yaw = (float) Math.atan2(next.x - ep.x, next.z - ep.z);
                                e.node.setLocalRotation(new Quaternion().fromAngles(0, -yaw, 0));
                                e.node.setLocalTranslation(next);
                            }
                        }
                    } else if (e.atkCd <= 0f) {
                        e.st = EState.WINDUP;
                        e.timer = e.windupDur;
                        e.telegraphCone.setCullHint(Spatial.CullHint.Never);
                        flash(e.body, ColorRGBA.Yellow, 100);
                    }
                }
                case WINDUP -> {
                    float yaw = (float) Math.atan2(pos.x - ep.x, pos.z - ep.z);
                    e.node.setLocalRotation(new Quaternion().fromAngles(0, -yaw, 0));

                    e.timer -= tpf;
                    if (e.timer <= 0f) {
                        e.telegraphCone.setCullHint(Spatial.CullHint.Always);

                        float hitDist = distXZ(e.node.getWorldTranslation(),
                                playerGeom.getWorldTranslation());
                        if (hitDist <= enemyAttackRange * 1.1f) {
                            playerHP = Math.max(0, playerHP - enemyDamage);
                            flash(playerGeom, ColorRGBA.Red, 120);

                            Vector3f pushP = playerGeom.getWorldTranslation().subtract(
                                    e.node.getWorldTranslation());
                            pushP.y = 0;
                            if (pushP.lengthSquared() > 0) {
                                pushP.normalizeLocal().multLocal(kbScaleOnHit);
                                playerKbVel.addLocal(pushP);
                            }
                            showDamage(playerGeom.getWorldTranslation().add(0, 1.2f, 0),
                                    enemyDamage, ColorRGBA.Red);

                            if (playerHP == 0) {
                                ((com.example.client.GameApp) app).sendToServer(
                                        new BattleEndReport(battleId, false));
                                if (onExit != null) {
                                    onExit.run();
                                }
                                getStateManager().detach(this);
                                return;
                            }
                        }

                        e.st = EState.RECOVER;
                        e.timer = e.recoverDur;
                    }
                }
                case RECOVER -> {
                    e.timer -= tpf;
                    if (e.timer <= 0f) {
                        e.atkCd = enemyAttackCooldown;
                        e.st = EState.CHASE;
                    }
                }
            }

            updateEnemyHpLabel(e);
        }

        // після оновлення pos (перед камерою)
        float groundY = safeHeightAt(pos.x, pos.z);
        if (!Float.isNaN(groundY)) {
            float targetY = groundY + 1f;
            // м’яке “прилипання” без ривків
            pos.y = FastMath.interpolateLinear(1f - FastMath.exp(-10f * tpf), pos.y, targetY);
            playerGeom.setLocalTranslation(pos);
        }

        // 4) Камера слідує
        Vector3f look = playerGeom.getWorldTranslation();
        if (Float.isFinite(look.x) && Float.isFinite(look.y) && Float.isFinite(look.z)) {
            Vector3f targetCam = look.add(camOffset);
            Vector3f curCam = app.getCamera().getLocation();
            float a = 1f - FastMath.exp(-camLerp * tpf);
            Vector3f newCam = curCam.interpolateLocal(targetCam, a);
            app.getCamera().setLocation(newCam);
            app.getCamera().lookAt(look, Vector3f.UNIT_Y);
        }

        if (hpText != null) {
            hpText.setText("HP: " + playerHP + " / " + playerMaxHP);
        }
        updateDamagePopups(tpf);
    }

    /**
     * Єдине місце завершення бою: спершу зберігаємо HP, потім усе інше.
     */
    private void finishBattle(boolean win) {
        // синхронізуємо hp з реального бою в контекст
        pc.setHp(this.playerHP);
        pc.setInBattle(false);

        ((com.example.client.GameApp) app).sendToServer(
                new com.example.shared.messages.PlayerHpSync(this.playerHP, this.playerMaxHP));
        System.out.println(
                "[CLIENT] finishBattle -> PlayerHpSync " + this.playerHP + "/" + this.playerMaxHP);

        ((com.example.client.GameApp) app).sendToServer(
                new com.example.shared.messages.BattleEndReport(battleId, win));

        if (onExit != null) {
            onExit.run();
        }
        getStateManager().detach(this);
    }

    @Override
    protected void cleanup(Application application) {

        System.out.println(
                "[BattleState3D] cleanup() called. Saving HP and marking out-of-battle.");

        // синхронізуємо HP з реального бою у локальний контекст
        if (pc != null) {
            pc.setHp(this.playerHP);
            pc.setInBattle(false);
        }

        // ОБОВʼЯЗКОВО: синк на сервер, щоб БД оновилась
        try {
            ((com.example.client.GameApp) app).sendToServer(
                    new com.example.shared.messages.PlayerHpSync(this.playerHP, this.playerMaxHP));
            System.out.println(
                    "[CLIENT] cleanup -> PlayerHpSync " + this.playerHP + "/" + this.playerMaxHP);
        } catch (Exception e) {
            e.printStackTrace();
        }


        var im = app.getInputManager();
        if (exitListener != null) {
            im.removeListener(exitListener);
        }
        if (moveListener != null) {
            im.removeListener(moveListener);
        }
        if (attackListener != null) {
            im.removeListener(attackListener);
            attackListener = null;
        }


        enemies.clear();

        if (hint != null) {
            app.getGuiNode().detachChild(hint);
        }
        if (hpText != null) {
            app.getGuiNode().detachChild(hpText);
            hpText = null;
        }

        pc.setInBattle(false); // страховка
        app.getRootNode().detachChild(localRoot);
        app.getInputManager().setCursorVisible(true);
        // якщо бій був у будівлі — попросимо відкрити її знову
        if (pc != null && pc.isInBuilding() && pc.getBuildingName() != null) {
            ((com.example.client.GameApp) app).sendToServer(
                    new com.example.shared.messages.OpenBuildingRequest(pc.getBuildingName(),
                            pc.getLocation().getX(), pc.getLocation().getY()));
        } else if (onExit != null) {
            onExit.run();
        }

    }


    /**
     * Рух без фізики з обмеженням по схилу та "step up".
     * Повертає нову позицію, або стару, або варіант зі "слайдом" по схилу.
     */
    private Vector3f tryMoveOnTerrain(Vector3f cur, Vector3f delta) {
        // 0) кандидат по XZ з межами
        Vector3f cand = cur.add(delta);
        cand.x = FastMath.clamp(cand.x, minX, maxX);
        cand.z = FastMath.clamp(cand.z, minZ, maxZ);

        // КЛАМП поточної точки перед семплом (інакше можливий NaN на краю)
        float cx = FastMath.clamp(cur.x, minX, maxX);
        float cz = FastMath.clamp(cur.z, minZ, maxZ);

        float yCur = safeHeightAt(cx, cz);
        float yCand = safeHeightAt(cand.x, cand.z);
        if (Float.isNaN(yCur) || Float.isNaN(yCand)) {
            return cur.clone(); // не знаємо висоту — стоїмо
        }

        // 1) «спуск вниз» дозволяємо практично завжди (щоб не “застрягати” на краю)
        float dh = yCand - yCur;
        if (dh <= 0f) {
            cand.y = yCand + 1f;
            return cand;
        }

        // 2) «крок вгору» — має бути в межах
        if (dh > maxStepUp) {
            // спроба слайду уздовж схилу
            Vector3f slid = slideAlongSlope(cur, delta);
            return (slid != null) ? slid : cur.clone();
        }

        // 3) перевірка схилу в точці приземлення (за нормаллю терейну)
        Vector3f n = (terrain != null) ? terrain.getNormal(new Vector2f(cand.x, cand.z)) : null;
        if (n != null && n.y < minNormalY) {
            Vector3f slid = slideAlongSlope(cur, delta);
            return (slid != null) ? slid : cur.clone();
        }

        // 4) усе ок
        cand.y = yCand + 1f;
        return cand;
    }

    private Vector3f forwardDir() {
        return playerGeom.getWorldRotation().mult(Vector3f.UNIT_Z).negateLocal().normalizeLocal();
    }

    private float distXZ(Vector3f a, Vector3f b) {
        float dx = a.x - b.x, dz = a.z - b.z;
        return FastMath.sqrt(dx * dx + dz * dz);
    }

    private void flash(Geometry g, ColorRGBA color, int ms) {
        var m = g.getMaterial();
        var old = (ColorRGBA) m.getParam("Color").getValue();
        m.setColor("Color", color);
        new Thread(() -> {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException ignored) {
            }
            app.enqueue(() -> m.setColor("Color", old));
        }).start();
    }

    /**
     * Спроба "слайду" вздовж схилу: перевіряємо півкроки по осях.
     * Повертає нову позицію або null, якщо нічого не вийшло.
     */
    private Vector3f slideAlongSlope(Vector3f cur, Vector3f delta) {
        // спробуємо рухатися тільки по тій осі, де крок більший,
        // якщо не вийшло — по іншій
        boolean xFirst = Math.abs(delta.x) >= Math.abs(delta.z);

        Vector3f pos1 = tryAxisOne(cur,
                new Vector3f(xFirst ? delta.x : 0f, 0f, xFirst ? 0f : delta.z));
        if (pos1 != null) {
            return pos1;
        }

        Vector3f pos2 = tryAxisOne(cur,
                new Vector3f(xFirst ? 0f : delta.x, 0f, xFirst ? delta.z : 0f));
        return pos2; // може бути null
    }

    /**
     * Перевірка руху тільки по одній осі з тими ж обмеженнями схилу/степу.
     */
    private Vector3f tryAxisOne(Vector3f cur, Vector3f step) {
        if (step.x == 0f && step.z == 0f) {
            return null;
        }

        // Базу клампимо перед семплом висоти
        float cx = FastMath.clamp(cur.x, minX, maxX);
        float cz = FastMath.clamp(cur.z, minZ, maxZ);
        Vector3f base = new Vector3f(cx, cur.y, cz);

        Vector3f cand = base.add(step);
        cand.x = FastMath.clamp(cand.x, minX, maxX);
        cand.z = FastMath.clamp(cand.z, minZ, maxZ);

        float yCur = safeHeightAt(base.x, base.z);
        float yCand = safeHeightAt(cand.x, cand.z);
        if (Float.isNaN(yCur) || Float.isNaN(yCand)) {
            return null;
        }

        float dh = yCand - yCur;
        if (dh > maxStepUp) {
            return null;                 // занадто високий «поріг»
        }

        Vector3f n = (terrain != null) ? terrain.getNormal(new Vector2f(cand.x, cand.z)) : null;
        if (n != null && n.y < minNormalY) {
            return null; // занадто круто
        }

        cand.y = yCand + 1f;
        return cand;
    }

    // ------ Damage numbers ------
    private void showDamage(Vector3f worldAt, int amount, ColorRGBA color) {
        DmgPopup p = new DmgPopup();
        p.world = worldAt.clone();
        p.text = new BitmapText(app.getAssetManager().loadFont("Interface/Fonts/Default.fnt"),
                false);
        p.text.setSize(18);
        p.text.setColor(color);
        p.text.setText(Integer.toString(amount));
        app.getGuiNode().attachChild(p.text);
        dmgPopups.add(p);
    }

    private void updateDamagePopups(float tpf) {
        for (int i = dmgPopups.size() - 1; i >= 0; --i) {
            DmgPopup p = dmgPopups.get(i);
            p.life -= tpf;
            p.world.y += p.vy * tpf;
            // fade
            float a = Math.max(0f, p.life / 0.9f);
            var c = (ColorRGBA) p.text.getColor().clone();
            c.a = a;
            p.text.setColor(c);
            // позиція на екрані
            Vector3f scr = app.getCamera().getScreenCoordinates(p.world);
            p.text.setLocalTranslation(scr.x - p.text.getLineWidth() / 2f, scr.y, 0);
            if (p.life <= 0f) {
                app.getGuiNode().detachChild(p.text);
                dmgPopups.remove(i);
            }
        }
    }

    // ------ Telegraph graphics (простий “сектор” перед ворогом) ------
    private Geometry makeTelegraph() {
        var g = new Geometry("tel", new Box(0.25f, 0.01f, 1.3f));
        var m = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", new ColorRGBA(1f, 0.4f, 0.1f, 0.85f));
        g.setMaterial(m);
        g.setQueueBucket(RenderQueue.Bucket.Transparent);
        g.setLocalTranslation(0, 0.05f, -1.0f);
        return g;
    }

    // ------ Knockback step з перевіркою схилу ------
    private Vector3f applyKnockback(Vector3f cur, Vector3f kbVel, float tpf) {
        if (kbVel.lengthSquared() < 1e-4f) {
            return cur;
        }
        Vector3f delta = kbVel.mult(tpf);
        Vector3f next = tryMoveOnTerrain(cur, delta);
        return next;
    }

    private float distXZf(Vector3f a, Vector3f b) {
        float dx = a.x - b.x, dz = a.z - b.z;
        return FastMath.sqrt(dx * dx + dz * dz);
    }

    /**
     * Простий підбір позиції з відстанню від уже обраних.
     */
    private Vector3f pickValleyWithSeparation(java.util.List<Vector3f> used, float minDist,
            float stepZ, float padX, float clearance) {
        for (int tries = 0; tries < 20; tries++) {
            Vector3f p = findValleySpawnRandomX(stepZ, padX, clearance);
            if (p == null) {
                continue;
            }
            boolean ok = true;
            for (Vector3f u : used) {
                if (distXZf(p, u) < minDist) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                return p;
            }
        }
        // фолбек: що знайшли — те й беремо
        Vector3f p = findValleySpawnRandomX(stepZ, padX, clearance);
        return (p != null) ? p : new Vector3f((minX + maxX) * 0.5f, 1f, (minZ + maxZ) * 0.5f);
    }

    // Випадковий X у межах арени з маленьким відступом
    private float randomX(float pad) {
        float lo = minX + pad, hi = maxX - pad;
        return FastMath.nextRandomFloat() * (hi - lo) + lo;
    }

    // Безпечний семпл висоти (NaN => Float.NaN)
    private float sampleHeight(float x, float z) {
        x = FastMath.clamp(x, minX, maxX);
        z = FastMath.clamp(z, minZ, maxZ);
        return safeHeightAt(x, z);
    }

    /**
     * Перевірка пласкості та зіскосу поруч із (x,z) у квадраті +/- clearance.
     */
    private boolean isFlatAndNotTooSteep(float x, float z, float clearance) {
        float centerH = sampleHeight(x, z);
        if (Float.isNaN(centerH)) {
            return false;
        }

        float step = Math.max(0.25f, clearance / 2f);
        for (float dx = -clearance; dx <= clearance; dx += step) {
            for (float dz = -clearance; dz <= clearance; dz += step) {
                float xx = FastMath.clamp(x + dx, minX, maxX);
                float zz = FastMath.clamp(z + dz, minZ, maxZ);
                float h = sampleHeight(xx, zz);
                if (Float.isNaN(h)) {
                    return false;
                }
                if (Math.abs(h - centerH) > maxStepUp) {
                    return false;
                }
                Vector3f n = (terrain != null) ? terrain.getNormal(new Vector2f(xx, zz)) : null;
                if (n != null && n.y < minNormalY) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Знаходить точку «низини» для випадкового X: сканує Z, шукає мінімум Y.
     * Потім трохи уточнює локально і перевіряє пласкість/схил.
     *
     * @param stepZ     крок сканування вздовж Z (0.25–0.5 м)
     * @param padX      відступ від країв X (м)
     * @param clearance перевірка пласкості в радіусі (м)
     */
    private Vector3f findValleySpawnRandomX(float stepZ, float padX, float clearance) {
        // НЕРЕКУРСИВНА версія з обмеженням спроб
        final int ATTEMPTS = 10;     // скільки різних X спробувати
        final float refineWindow = 4f; // локальне вікно в метрах уздовж Z

        Vector3f fallbackBest = null;     // найнижча точка, навіть якщо не пласко
        float fallbackBestH = Float.POSITIVE_INFINITY;

        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            // 1) випадковий X
            float x = randomX(padX);

            // 2) скан по всьому Z на мінімум
            float bestZ = Float.NaN, bestH = Float.POSITIVE_INFINITY;
            for (float z = minZ; z <= maxZ; z += stepZ) {
                float h = sampleHeight(x, z);
                if (Float.isNaN(h)) {
                    continue;
                }
                if (h < bestH) {
                    bestH = h;
                    bestZ = z;
                }
            }
            if (Float.isNaN(bestZ)) {
                continue;
            }

            // 3) локальне уточнення в маленькому вікні навколо bestZ
            float window = Math.max(1.0f, Math.min(refineWindow, (maxZ - minZ) * 0.25f));
            float refineZ = bestZ, refineH = bestH;
            for (float z = Math.max(minZ, bestZ - window); z <= Math.min(maxZ, bestZ + window);
                    z += stepZ * 0.5f) {
                float h = sampleHeight(x, z);
                if (!Float.isNaN(h) && h < refineH) {
                    refineH = h;
                    refineZ = z;
                }
            }

            // 4) невеликий 2D-рефайн
            float[] offsets = {0f, -0.5f, 0.5f, -1.0f, 1.0f};
            float fx = x, fz = refineZ, fh = refineH;
            for (float dx : offsets)
                for (float dz : offsets) {
                    float xx = FastMath.clamp(x + dx, minX, maxX);
                    float zz = FastMath.clamp(refineZ + dz, minZ, maxZ);
                    float h = sampleHeight(xx, zz);
                    if (!Float.isNaN(h) && h < fh) {
                        fh = h;
                        fx = xx;
                        fz = zz;
                    }
                }

            // Запам'ятовуємо найнижчу як фолбек
            if (fh < fallbackBestH) {
                fallbackBestH = fh;
                fallbackBest = new Vector3f(fx, fh + 1f, fz);
            }

            // 5) перевірити пласкість і схил — якщо ок, повертаємо
            if (isFlatAndNotTooSteep(fx, fz, clearance)) {
                return new Vector3f(fx, fh + 1f, fz);
            }
            // інакше — наступна спроба з іншим X
        }

        // Якщо жодна спроба не дала пласку точку — повертаємо найнижчу знайдену
        return (fallbackBest != null) ? fallbackBest : new Vector3f((minX + maxX) * 0.5f, 1f,
                (minZ + maxZ) * 0.5f);
    }

    private static final class SpawnConfig {
        int count = 3;          // скільки ворогів
        float minDist = 3.0f;   // мінімальна відстань між усіма спавнами (та до героя)
        float stepZ = 0.25f;    // крок сканування уздовж Z
        float padX = 1.5f;      // відступ від країв по X
        float clearance = 0.8f; // радіус перевірки «пласкості»
        int attempts = 40;      // глобальний ліміт спроб
    }

    private java.util.List<Vector3f> spawnEnemiesValley(Vector3f heroPos, SpawnConfig cfg) {
        java.util.ArrayList<Vector3f> placed = new java.util.ArrayList<>();
        placed.add(heroPos); // щоб не спавнити ворогів «у героя»

        java.util.ArrayList<Vector3f> result = new java.util.ArrayList<>();
        int tries = 0;

        while (result.size() < cfg.count && tries < cfg.attempts) {
            tries++;
            Vector3f p = findValleySpawnRandomX(cfg.stepZ, cfg.padX, cfg.clearance);
            if (p == null) {
                continue;
            }

            boolean ok = true;
            for (Vector3f u : placed) {
                if (distXZf(p, u) < cfg.minDist) {
                    ok = false;
                    break;
                }
            }
            if (!ok) {
                continue;
            }

            // Закріплюємо
            placed.add(p);
            result.add(p);
            spawnEnemy(p); // використовує твою вже виправлену версію
        }

        if (result.size() < cfg.count) {
            System.out.println(
                    "[BattleState3D] Warning: spawned only " + result.size() + " of " + cfg.count);
        }
        return result;
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }
}