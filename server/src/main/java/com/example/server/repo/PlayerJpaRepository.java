package com.example.server.repo;

import com.example.server.model.PlayerEntity;
import com.example.server.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class PlayerJpaRepository {

    public PlayerEntity findByName(String name) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from PlayerEntity p where p.nickname=:n", PlayerEntity.class)
                           .setParameter("n", name)
                           .uniqueResult();
        }
    }

    public void saveOrUpdate(PlayerEntity e) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            s.merge(e); // upsert
            tx.commit();
        }
    }
}
