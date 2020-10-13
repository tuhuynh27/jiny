package com.jinyframework.examples.crud.repositories;

import com.jinyframework.examples.crud.entities.Tiger;
import org.hibernate.SessionFactory;

public class TigerRepository extends CrudRepository<Tiger> {
    public TigerRepository(SessionFactory sessionFactory, Class<Tiger> entityClass) {
        super(sessionFactory, entityClass);
    }
}
