package com.jinyframework.keva.server.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.jinyframework.keva.server.config.ConfigHolder.makeDefaultConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class ConfigManagerTest {
    @Test
    void getSetConfig() {
        val def = makeDefaultConfig();
        ConfigManager.setConfig(def);
        val actual = ConfigManager.getConfig();
        assertEquals(def, actual);
    }

    @Test
    void loadConfig() throws Exception {
        String[] args = {
                ""
        };
        ConfigManager.loadConfig(args);
        val def = makeDefaultConfig();
        assertEquals(def, ConfigManager.getConfig());
        args = new String[]{
                "-p", "123123"
        };
        log.info(Arrays.toString(args));
        ConfigManager.loadConfig(args);
        assertEquals(123123, ConfigManager.getConfig().getPort());
    }
}
