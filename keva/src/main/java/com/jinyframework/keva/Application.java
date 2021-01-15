package com.jinyframework.keva;

import com.jinyframework.keva.core.Server;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public final class Application {
    private Application() {
    }

    public static void main(String[] args) {
        try {
            val server = Server.builder()
                    .host("localhost")
                    .port(6767)
                    .build();
            server.run();
        } catch (Exception e) {
            log.error("There was a problem running server: ",e);
        }
    }
}
