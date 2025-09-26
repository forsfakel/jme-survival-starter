package com.example.server.repo;

import com.example.server.model.LocationEntity;
import com.example.server.model.id.LocationId;
import com.example.server.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class LocationJpaRepository {
    public LocationEntity getOrCreate(int x, int y) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            LocationEntity e = s.get(LocationEntity.class, new LocationId(x, y));
            if (e != null) return e;
        }
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            LocationEntity e = new LocationEntity(new LocationId(x, y));
            s.persist(e);
            tx.commit();
            return e;
        }
    }
}
