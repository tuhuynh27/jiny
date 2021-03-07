package com.jinyframework.keva.performance;

import com.jinyframework.keva.store.NoHeapStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.jinyframework.keva.store.NoHeapStoreManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class PerformanceTest {
    public final static int THREAD_POOL_SIZE = 1;

    public static void main(String[] args) throws InterruptedException {
        val cHashMap = new CustomizedConcurrentHashMap();
        performTest(cHashMap);

        val manager = new NoHeapStoreManager();
        manager.createStore("Performance-Test", NoHeapStore.Storage.IN_MEMORY, 1843);
        val noHeapStore = manager.getStore("Performance-Test");
        performTest(noHeapStore);
    }

    public static void performTest(NoHeapStore noHeapStore) throws InterruptedException {
        log.info("Test started for: " + noHeapStore.getClass());

        long averageTime = 0;
        for (int i = 0; i < 10; i++) {
            log.info("Generating random array...");
            ArrayList<String> list = new ArrayList<>();
            for (int i1 = 1; i1 < 10_000_000; i1++) {
                list.add(String.valueOf(i1));
            }
            Collections.shuffle(list);
            log.info("Done!");

            long startTime = System.nanoTime();
            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            for (int j = 0; j < THREAD_POOL_SIZE; j++) {
                executorService.execute(() -> {
                    for (val x : list) {
                        // Put value
                        noHeapStore.putString(x, String.valueOf(x));
                    }

                    String val;
                    for (val x : list) {
                        // Get value
                        val = noHeapStore.getString(x);
                    }
                });
            }

            executorService.shutdown();
            val term = executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            if (!term) {
                log.error("Cannot terminate thread pool!");
            }

            long endTime = System.nanoTime();
            long totalTime = (endTime - startTime) / 1000000L;
            averageTime += totalTime;
            log.info("10M entries added/retrieved in " + totalTime + " ms");
            System.gc();
        }

        log.info("For " + noHeapStore.getClass() + " the average time is " + averageTime / 10 + " ms\n");
    }

    static class CustomizedConcurrentHashMap implements NoHeapStore {
        private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

        @Override
        public long getRecordCount() {
            return 0;
        }

        @Override
        public long getEmptyCount() {
            return 0;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getFolder() {
            return null;
        }

        @Override
        public long getFileSize() {
            return 0;
        }

        @Override
        public boolean putInteger(String key, Integer val) {
            return false;
        }

        @Override
        public Integer getInteger(String key) {
            return null;
        }

        @Override
        public boolean putShort(String key, Short val) {
            return false;
        }

        @Override
        public Short getShort(String key) {
            return null;
        }

        @Override
        public boolean putLong(String key, Long val) {
            return false;
        }

        @Override
        public Long getLong(String key) {
            return null;
        }

        @Override
        public boolean putFloat(String key, Float val) {
            return false;
        }

        @Override
        public Float getFloat(String key) {
            return null;
        }

        @Override
        public boolean putDouble(String key, Double val) {
            return false;
        }

        @Override
        public Double getDouble(String key) {
            return null;
        }

        @Override
        public boolean putString(String key, String val) {
            map.put(key, val);
            return true;
        }

        @Override
        public String getString(String key) {
            return map.get(key);
        }

        @Override
        public boolean putObject(String key, Object msg) {
            return false;
        }

        @Override
        public Object getObject(String key) {
            return null;
        }

        @Override
        public boolean putChar(String key, char val) {
            return false;
        }

        @Override
        public char getChar(String key) {
            return 0;
        }

        @Override
        public boolean remove(String key) {
            return false;
        }

        @Override
        public Object iterateStart() {
            return null;
        }

        @Override
        public Object iterateNext() {
            return null;
        }

        @Override
        public void delete() {
        }
    }
}