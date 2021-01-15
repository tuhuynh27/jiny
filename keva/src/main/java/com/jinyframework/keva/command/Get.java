package com.jinyframework.keva.command;

import com.jinyframework.keva.storage.StorageService;

import java.util.List;

public class Get implements CommandHandler{
    @Override
    public Object handle(List<String> args) {
        return StorageService.getStringStringStore().get(args.get(0));
    }
}
