package com.jinyframework.examples.crud.entities;

import lombok.Data;

@Data
public class Dog {
    final int id;
    final String name;
    final String owner;
}
