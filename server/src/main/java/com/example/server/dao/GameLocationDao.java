package com.example.server.dao;

import com.example.shared.model.GameLocation;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class GameLocationDao {

    private final SessionFactory sessionFactory;

    public GameLocationDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(GameLocation location) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(location);
            tx.commit();
        }
    }

    public void update(GameLocation location) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(location);
            tx.commit();
        }
    }

    public GameLocation findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(GameLocation.class, id);
        }
    }

    public List<GameLocation> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from GameLocation", GameLocation.class).list();
        }
    }

    public void delete(GameLocation location) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.remove(location);
            tx.commit();
        }
    }
}
