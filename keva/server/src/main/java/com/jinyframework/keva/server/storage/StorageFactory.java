package com.jinyframework.keva.server.storage;

import com.jinyframework.keva.server.core.KevaSocket;
import com.jinyframework.keva.server.noheap.NoHeapDB;
import com.jinyframework.keva.server.noheap.NoHeapDBStore;
import lombok.Setter;
import lombok.val;

import java.util.concurrent.ConcurrentHashMap;

@Setter
public final class StorageFactory {
    private static NoHeapDBStore noHeapDBStore;
    private static ConcurrentHashMap<String, KevaSocket> socketHashMap;

    public synchronized static NoHeapDBStore getNoHeapDBStore() {
        if (noHeapDBStore == null) {
            try {
                val db = new NoHeapDB();
                db.createStore("Keva", NoHeapDBStore.Storage.IN_MEMORY, 128);
                noHeapDBStore = db.getStore("Keva");
            } catch (Exception ex) {
                System.out.println("Cannot get noHeapDbStore");
                System.exit(1);
            }
        }

        return noHeapDBStore;
    }

    public synchronized static ConcurrentHashMap<String, KevaSocket> getSocketHashMap() {
        if (socketHashMap == null) {
            socketHashMap = new ConcurrentHashMap<>();
        }

        return socketHashMap;
    }
}
