package com.tuhuynh.crud.handlers;

import com.google.gson.Gson;
import com.tuhuynh.crud.entities.ResponseObject;
import com.tuhuynh.crud.entities.Student;
import com.tuhuynh.jerrymouse.core.RequestBinder.RequestContext;
import com.tuhuynh.jerrymouse.core.RequestBinder.HttpResponse;

import java.util.ArrayList;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public final class CrudHandler {
    private final Gson gson = new Gson();
    private final ArrayList<Student> students;

    public HttpResponse getStudents(final RequestContext ctx) {
        val students = this.students.toArray();
        return HttpResponse.of(students).transform(gson::toJson);
    }

    public HttpResponse getStudent(final RequestContext ctx) {
        throw new RuntimeException("WIP");
    }

    public HttpResponse addStudent(final RequestContext ctx) {
        val body = ctx.getBody();
        val newStudent = gson.fromJson(body, Student.class);
        this.students.add(newStudent);
        return HttpResponse
                .of(ResponseObject.builder().message("Done").build())
                .transform(gson::toJson);
    }

    public HttpResponse updateStudent(final RequestContext ctx) {
        throw new RuntimeException("WIP");
    }

    public HttpResponse deleteStudent(final RequestContext ctx) {
        throw new RuntimeException("WIP");
    }
}