package com.jinyframework.examples.crud.factories;

import com.jinyframework.examples.crud.entities.Tiger;
import lombok.Setter;
import lombok.val;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

@Setter
public class HibernateFactory {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            val configuration = new Configuration();
            configuration.setProperty("hibernate.current_session_context_class", "thread");
            configuration.setProperty("connection.driver_class", "com.mysql.jdbc.Driver");
            configuration.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/hibernate");
            configuration.setProperty("hibernate.connection.username", "root");
            configuration.setProperty("hibernate.connection.password", "example");
            configuration.setProperty("dialect", "org.hibernate.dialect.MySQLDialect");
            configuration.setProperty("hibernate.hbm2ddl.auto", "update");
            configuration.setProperty("show_sql", "true");
            configuration.setProperty("hibernate.connection.pool_size", "10");
            configuration.addAnnotatedClass(Tiger.class);
            val builder = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties());
            sessionFactory = configuration.buildSessionFactory(builder.build());
        }

        return sessionFactory;
    }
}
