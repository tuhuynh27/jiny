package com.jinyframework.keva.command;

import com.jinyframework.keva.storage.StorageService;

import java.util.List;

public class Set implements CommandHandler{
    @Override
    public Object handle(List<String> args) {
        return StorageService.getStringStringStore().put(args.get(0),args.get(1));
    }
}
