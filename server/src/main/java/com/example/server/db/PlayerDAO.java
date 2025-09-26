package com.example.server.db;

import com.example.server.model.PlayerEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class PlayerDAO {
    private final SessionFactory sessionFactory;

    public PlayerDAO() {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    public void save(PlayerEntity player) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(player);
            session.getTransaction().commit();
        }
    }

    public PlayerEntity findByNickname(String nickname) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "FROM PlayerEntity WHERE nickname = :nickname", PlayerEntity.class)
                           .setParameter("nickname", nickname)
                           .uniqueResult();
        }
    }

    public void update(PlayerEntity player) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.merge(player);
            session.getTransaction().commit();
        }
    }

    /** Створити або оновити hp/hpMax без завантаження всієї сутності. */
    public void upsertHp(String nickname, int hp, int hpMax) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Спробуємо оновити існуючого:
            int updated = session.createQuery(
                            "UPDATE PlayerEntity p SET p.hp = :hp, p.hpMax = :hpMax WHERE p.nickname = :nick")
                                  .setParameter("hp", hp)
                                  .setParameter("hpMax", hpMax)
                                  .setParameter("nick", nickname)
                                  .executeUpdate();

            if (updated == 0) {
                // Нема запису — створюємо
                PlayerEntity p = new PlayerEntity();
                p.setNickname(nickname);
                p.setHp(hp);
                p.setHpMax(hpMax);
                session.persist(p);
            }

            session.getTransaction().commit();
        }
    }
}
