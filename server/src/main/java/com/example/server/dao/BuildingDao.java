package com.example.server.dao;

import com.example.shared.model.Building;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class BuildingDao {

    private final SessionFactory sessionFactory;

    public BuildingDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(Building building) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(building);
            tx.commit();
        }
    }

    public void update(Building building) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(building);
            tx.commit();
        }
    }

    public Building findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Building.class, id);
        }
    }

    public List<Building> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Building", Building.class).list();
        }
    }

    public void delete(Building building) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.remove(building);
            tx.commit();
        }
    }
}
