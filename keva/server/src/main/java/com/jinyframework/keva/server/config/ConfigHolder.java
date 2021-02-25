package com.jinyframework.keva.server.config;

import lombok.*;

import java.util.Properties;

@Builder
@Getter
@Setter
public class ConfigHolder {

    @ConfigProp(name = "heartbeat_enabled", defaultVal = "false")
    private Boolean heartbeatEnabled;
    @ConfigProp(name = "snapshot_enabled", defaultVal = "false")
    private Boolean snapshotEnabled;
    @ConfigProp(name = "hostname", defaultVal = "localhost")
    private String hostname;
    @ConfigProp(name = "port", defaultVal = "6767")
    private Integer port;
    @ConfigProp(name = "heartbeat_timeout", defaultVal = "120000")
    private Long heartbeatTimeout;
    @ConfigProp(name = "snapshot_interval", defaultVal = "PT2M")
    private String snapshotInterval;
    @ConfigProp(name = "backup_path", defaultVal = "./dump.keva")
    private String backupPath;
    @ConfigProp(name = "recovery_path", defaultVal = "./dump.keva")
    private String recoveryPath;

    public static ConfigHolder fromProperties(@NonNull Properties props) throws Exception {
        val configHolder = builder().build();

        val fields = ConfigHolder.class.getDeclaredFields();
        for (val field : fields) {
            if (field.isAnnotationPresent(ConfigProp.class)) {
                val annotation = field.getAnnotation(ConfigProp.class);
                val value = parse(props.getProperty(annotation.name(), annotation.defaultVal()), field.getType());
                field.set(configHolder, value);
            }
        }

        return configHolder;
    }

    private static <T> T parse(String s, Class<T> clazz) throws Exception {
        return clazz.getConstructor(new Class[]{String.class}).newInstance(s);
    }
}
