package com.jinyframework.keva.server.storage;

import com.jinyframework.keva.server.config.ConfigManager;
import com.jinyframework.keva.server.core.KevaSocket;
import com.jinyframework.keva.server.noheap.NoHeapStore;
import com.jinyframework.keva.server.noheap.NoHeapStoreManager;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.concurrent.ConcurrentHashMap;

@Setter
@Slf4j
public final class StorageFactory {
    private static NoHeapStore noHeapStore;
    private static ConcurrentHashMap<String, KevaSocket> socketHashMap;

    public synchronized static NoHeapStore getNoHeapDBStore() {
        if (noHeapStore == null) {
            try {
                val db = new NoHeapStoreManager();
                db.createStore("Keva", NoHeapStore.Storage.PERSISTED, ConfigManager.getConfig().getHeapSize());
                noHeapStore = db.getStore("Keva");
            } catch (Exception ex) {
                log.error("Cannot get noHeapDbStore");
                System.exit(1);
            }
        }

        return noHeapStore;
    }

    public synchronized static ConcurrentHashMap<String, KevaSocket> getSocketHashMap() {
        if (socketHashMap == null) {
            socketHashMap = new ConcurrentHashMap<>();
        }

        return socketHashMap;
    }
}
