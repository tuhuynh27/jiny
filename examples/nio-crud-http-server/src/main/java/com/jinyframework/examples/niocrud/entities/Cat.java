package com.jinyframework.examples.niocrud.entities;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class Cat {
    ObjectId id;
    String name;
    String owner;
}
