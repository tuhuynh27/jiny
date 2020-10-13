package com.jinyframework.examples.crud.repositories;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;

import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

import lombok.val;

@RequiredArgsConstructor
public abstract class CrudRepository<T> {
    private final SessionFactory sessionFactory;
    private final Class<T> entityClass;

    public T save(T entity) {
        val session = sessionFactory.getCurrentSession();
        val tx = session.beginTransaction();
        session.save(entity);
        tx.commit();
        return entity;
    }

    public T find(long id) {
        val session = sessionFactory.getCurrentSession();
        val tx = session.beginTransaction();
        try {
            return session.find(entityClass, id);
        } finally {
            tx.commit();
        }
    }

    public void update(T entity) {
        val session = sessionFactory.getCurrentSession();
        val tx = session.beginTransaction();
        sessionFactory.getCurrentSession().update(entity);
        tx.commit();
    }

    public void delete(T entity) {
        val session = sessionFactory.getCurrentSession();
        val tx = session.beginTransaction();
        session.delete(entity);
        tx.commit();
    }

    public List<T> list() {
        val session = sessionFactory.getCurrentSession();
        val tx = session.beginTransaction();
        try {
            CriteriaQuery<T> query = session.getCriteriaBuilder().createQuery(entityClass);
            query.select(query.from(entityClass));
            return session.createQuery(query).getResultList();
        } finally {
            tx.commit();
        }
    }
}
