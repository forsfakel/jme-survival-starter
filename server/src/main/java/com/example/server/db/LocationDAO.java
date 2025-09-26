package com.example.server.db;

import com.example.server.model.LocationEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class LocationDAO {
    private final SessionFactory sessionFactory;

    public LocationDAO() {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    public void save(LocationEntity location) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(location);
            session.getTransaction().commit();
        }
    }

    public LocationEntity findByCoords(int x, int y) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "FROM LocationEntity WHERE x = :x AND y = :y", LocationEntity.class)
                           .setParameter("x", x)
                           .setParameter("y", y)
                           .uniqueResult();
        }
    }
}
