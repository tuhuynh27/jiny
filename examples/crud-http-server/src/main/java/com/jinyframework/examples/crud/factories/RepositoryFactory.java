package com.jinyframework.examples.crud.factories;

import com.jinyframework.examples.crud.entities.Tiger;
import com.jinyframework.examples.crud.repositories.TigerRepository;
import lombok.Setter;

@Setter
public class RepositoryFactory {
    private static TigerRepository tigerRepository;

    public static TigerRepository getTigerRepository() {
        if (tigerRepository == null) {
            tigerRepository = new TigerRepository(HibernateFactory.getSessionFactory(), Tiger.class);
        }

        return tigerRepository;
    }
}
