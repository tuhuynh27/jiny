package com.jinyframework.keva;

import com.jinyframework.keva.core.CommandService;
import com.jinyframework.keva.core.Server;
import com.jinyframework.keva.storage.HashMapStorage;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public final class Application {
    private Application() {
    }

    public static void main(String[] args) {
        try {
            val stringStringHashMap = new HashMapStorage<String,String>();
            val server = Server.builder()
                    .host("localhost")
                    .port(6767)
                    .commandService(new CommandService(stringStringHashMap))
                    .build();
            server.run();
        } catch (Exception e) {
            log.error("There was a problem running server: ",e);
        }
    }
}
