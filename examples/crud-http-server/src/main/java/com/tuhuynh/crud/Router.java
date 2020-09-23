package com.tuhuynh.crud;

import com.tuhuynh.crud.handlers.CrudHandler;
import com.tuhuynh.jerrymouse.HttpServer;

import lombok.val;

import java.util.ArrayList;

public final class Router {
    public static void initRouter(HttpServer server) {
        val crudHandler = new CrudHandler(new ArrayList<>());

        server.get("/students", crudHandler::getStudents);
        server.get("/students/:email", crudHandler::getStudent);
        server.post("/students", crudHandler::addStudent);
        server.put("/students/:email", crudHandler::updateStudent);
        server.delete("/students/:email", crudHandler::deleteStudent);
    }
}
