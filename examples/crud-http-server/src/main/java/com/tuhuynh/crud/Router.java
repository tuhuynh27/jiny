package com.tuhuynh.crud;

import com.tuhuynh.crud.handlers.CatHandler;
import com.tuhuynh.crud.handlers.StudentHandler;
import com.tuhuynh.crud.storage.MongoDB;
import com.tuhuynh.jerrymouse.HttpServer;

import lombok.val;

import java.util.ArrayList;

public final class Router {
    public static void initRouter(HttpServer server) {
        val studentHandler = new StudentHandler(new ArrayList<>());
        server.get("/students", studentHandler::getStudents);
        server.get("/students/:email", studentHandler::getStudent);
        server.post("/students", studentHandler::addStudent);
        server.put("/students/:email", studentHandler::updateStudent);
        server.delete("/students/:email", studentHandler::deleteStudent);

        val mongoClient = MongoDB.init();
        val catHandler = new CatHandler(mongoClient);
        server.get("/cats", catHandler::getCats);
        server.post("/cats", catHandler::addCat);
    }
}
