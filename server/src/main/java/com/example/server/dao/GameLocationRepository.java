package com.example.server.dao;

import com.example.server.HibernateUtil;
import com.example.shared.model.GameLocation;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class GameLocationRepository {

    public void save(GameLocation location) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(location);
            tx.commit();
        }
    }

    public GameLocation findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(GameLocation.class, id);
        }
    }

    public List<GameLocation> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<GameLocation> query = session.createQuery("from GameLocation", GameLocation.class);
            return query.list();
        }
    }

    public GameLocation findByCoords(int x, int y) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<GameLocation> query = session.createQuery(
                    "from GameLocation g where g.coords.x = :x and g.coords.y = :y",
                    GameLocation.class
            );
            query.setParameter("x", x);
            query.setParameter("y", y);
            return query.uniqueResult();
        }
    }
}
