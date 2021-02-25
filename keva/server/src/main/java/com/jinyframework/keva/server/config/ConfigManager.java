package com.jinyframework.keva.server.config;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Properties;

@Slf4j
public final class ConfigManager {
    public static final String DEFAULT_FILE_PATH = Paths.get(".", "keva.properties").toString();
    private static ConfigHolder configHolder;

    private ConfigManager() {
    }

    public static ConfigHolder getConfig() {
        return configHolder;
    }

    public static void setConfig(ConfigHolder config) {
        configHolder = config;
    }

    public static void loadConfigFromFile(String filePath, Properties overrideProps) throws Exception {
        if (filePath.isEmpty()) {
            filePath = DEFAULT_FILE_PATH;
        }
        val props = new Properties();
        @Cleanup
        val file = new FileInputStream(filePath);
        props.load(file);

        if (overrideProps != null && !overrideProps.isEmpty()) {
            for (val override : overrideProps.stringPropertyNames()) {
                props.setProperty(override, overrideProps.getProperty(override));
            }
        }

        configHolder = ConfigHolder.fromProperties(props);
    }
}
