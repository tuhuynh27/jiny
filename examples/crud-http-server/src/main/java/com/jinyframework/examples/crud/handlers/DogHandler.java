package com.jinyframework.examples.crud.handlers;

import com.google.gson.Gson;
import com.jinyframework.core.AbstractRequestBinder.Context;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import com.jinyframework.examples.crud.entities.Dog;
import com.jinyframework.examples.crud.factories.AppFactory;
import com.jinyframework.examples.crud.utils.ResponseHelper;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

@RequiredArgsConstructor
public class DogHandler {
    private final Gson gson = AppFactory.getGson();
    private final Connection conn = AppFactory.getSQLConnection();

    public HttpResponse getDogs(Context ctx) throws SQLException {
        @Cleanup val stmt = conn.prepareStatement("SELECT * FROM dog");
        @Cleanup val rs = stmt.executeQuery();
        val dogs = new ArrayList<Dog>();
        while (rs.next()) {
            val id = rs.getInt("id");
            val name = (String) rs.getObject("name");
            val owner = (String) rs.getObject("owner");
            val dog = new Dog(id, name, owner);
            dogs.add(dog);
        }
        return HttpResponse.of(dogs);
    }

    public HttpResponse getDog(Context ctx) throws SQLException {
        val idParam = Integer.parseInt(ctx.pathParam("id"));
        @Cleanup val stmt = conn.prepareStatement("SELECT * FROM dog WHERE id = ?");
        stmt.setInt(1, idParam);
        @Cleanup val rs = stmt.executeQuery();
        if (rs.next()) {
            val id = rs.getInt("id");
            val name = (String) rs.getObject("name");
            val owner = (String) rs.getObject("owner");
            val dog = new Dog(id, name, owner);
            return HttpResponse.of(dog);
        }
        return ResponseHelper.error("Invalid Dog ID");
    }

    public HttpResponse addDog(Context ctx) throws SQLException {
        val body = ctx.getBody();
        val newDog = gson.fromJson(body, Dog.class);
        @Cleanup val stmt = conn.prepareStatement("INSERT INTO dog (NAME, OWNER) VALUES (?,?)");
        stmt.setString(1, newDog.getName());
        stmt.setString(2, newDog.getOwner());
        val result = stmt.executeUpdate();
        return result >= 1 ? ResponseHelper.success("Done") : ResponseHelper.error("Invalid add statement");
    }

    public HttpResponse updateDog(Context ctx) throws SQLException {
        val idParam = Integer.parseInt(ctx.pathParam("id"));
        val body = ctx.getBody();
        val updatingDog = gson.fromJson(body, Dog.class);
        @Cleanup val stmt = conn.prepareStatement("UPDATE dog SET name = ?, owner = ? WHERE id = ?");
        stmt.setString(1, updatingDog.getName());
        stmt.setString(2, updatingDog.getOwner());
        stmt.setInt(3, idParam);
        val result = stmt.executeUpdate();
        return result >= 1 ? ResponseHelper.success("Done") : ResponseHelper.error("Invalid update statement");
    }

    public HttpResponse deleteDog(Context ctx) throws SQLException {
        val idParam = Integer.parseInt(ctx.pathParam("id"));
        @Cleanup val stmt = conn.prepareStatement("DELETE FROM dog WHERE id = ?");
        stmt.setInt(1, idParam);
        val result = stmt.executeUpdate();
        return result >= 1 ? ResponseHelper.success("Done") : ResponseHelper.error("Invalid delete statement");
    }
}
