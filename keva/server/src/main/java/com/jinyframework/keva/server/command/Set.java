package com.jinyframework.keva.server.command;

import java.util.List;

import static com.jinyframework.keva.server.storage.StorageFactory.hashStore;

public class Set implements CommandHandler {
    @Override
    public Object handle(List<String> args) {
        try {
            hashStore().put(args.get(0), args.get(1));
            return 1;
        } catch (Exception ignore) {
            return 0;
        }
    }
}
