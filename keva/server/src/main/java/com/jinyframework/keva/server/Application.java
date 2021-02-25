package com.jinyframework.keva.server;

import com.jinyframework.keva.server.config.ConfigHolder;
import com.jinyframework.keva.server.config.ConfigManager;
import com.jinyframework.keva.server.core.Server;
import com.jinyframework.keva.server.util.ArgsParser;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

@Slf4j
public final class Application {
    private Application() {
    }

    public static Map<String, String> getConfig(String[] args) {
        val options = new HashSet<String>();
        options.add("h");
        options.add("p");
        options.add("ht");
        options.add("rc");
        options.add("bk");
        options.add("sn");

        options.add("f");
        return ArgsParser.parse(args, options);
    }

    public static void main(String[] args) {
        try {
            val config = getConfig(args);

//            val hostname = config.getOrDefault("h", "localhost");
//            val port = Integer.parseInt(config.getOrDefault("p", "6767"));
//            val heartbeatTimeout = Integer.parseInt(config.getOrDefault("ht", "120000"));
//            val recoveryPath = config.getOrDefault("rc", "./dump.keva");
//            val backupPath = config.getOrDefault("bk", "./dump.keva");
//            val snapInterval = config.getOrDefault("sn", "PT2M");
            val configFilePath = config.get("f");
            if (configFilePath != null) {
                ConfigManager.loadConfigFromFile(configFilePath, null);
            } else {
                // init using default values
                ConfigManager.setConfig(ConfigHolder.fromProperties(new Properties()));
            }

            val server = new Server(ConfigManager.getConfig());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.shutdown();
                } catch (Exception e) {
                    log.error("Problem occurred when stopping server: ", e);
                } finally {
                    log.info("Bye");
                }
            }));
            server.run();
        } catch (Exception e) {
            log.error("There was a problem running server: ", e);
        }
    }
}
