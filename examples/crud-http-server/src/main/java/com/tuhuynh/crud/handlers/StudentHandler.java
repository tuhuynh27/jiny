package com.tuhuynh.crud.handlers;

import com.google.gson.Gson;
import com.tuhuynh.crud.entities.Student;
import com.tuhuynh.crud.utils.ResponseHelper;
import com.tuhuynh.jerrymouse.core.RequestBinder.RequestContext;
import com.tuhuynh.jerrymouse.core.RequestBinder.HttpResponse;

import java.util.ArrayList;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public final class StudentHandler {
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

        val matched = this.students.stream()
                .filter(e -> e.getEmail().toLowerCase()
                        .equals(newStudent.getEmail().toLowerCase()));
        if (matched.toArray().length > 0) {
            return ResponseHelper.error("Email is existed");
        }

        this.students.add(newStudent);
        return ResponseHelper.success("Done");
    }

    public HttpResponse updateStudent(final RequestContext ctx) {
        throw new RuntimeException("WIP");
    }

    public HttpResponse deleteStudent(final RequestContext ctx) {
        val email = ctx.getParam().get("email");

        this.students.removeIf(e -> e.getEmail().toLowerCase().equals(email.toLowerCase()));
        return ResponseHelper.success("Done");
    }
}