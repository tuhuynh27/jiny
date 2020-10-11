package com.jinyframework.examples.crud.storage;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class MySQL {
    public static Connection init() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost/test?user=root&password=example");
        } catch (SQLException t) {
            log.error(t.getMessage(), t);
        }

        return null;
    }
}
