package com.jinyframework.keva.performance;

import com.jinyframework.keva.store.NoHeapStore;
import com.jinyframework.keva.store.NoHeapStoreManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PerformanceTest {
    public final static int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) throws InterruptedException {
        val manager = new NoHeapStoreManager();
        manager.createStore("Performance-Test", NoHeapStore.Storage.IN_MEMORY, 1843);
        val noHeapStore = manager.getStore("Performance-Test");
        performTest(noHeapStore);
    }

    // Currently single thread test
    public static void performTest(NoHeapStore noHeapStore) throws InterruptedException {
        log.info("Thread usage: " + THREAD_POOL_SIZE);

        ArrayList<String> list = new ArrayList<>();
        for (int i1 = 1; i1 < 1_000_000; i1++) {
            list.add(String.valueOf(i1));
        }
        Collections.shuffle(list);

        log.info("Test started for: " + noHeapStore.getClass());

        long averageTime = 0;
        for (int i = 0; i < 1_000_000; i++) {
            long startTime = System.nanoTime();
            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            for (int j = 0; j < THREAD_POOL_SIZE; j++) {
                executorService.execute(() -> {
                    for (val x : list) {
                        // Put value
                        noHeapStore.putString(x, String.valueOf(x));

                        // Get value
                        noHeapStore.getString(x);
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
            log.info("1M entries added/retrieved in " + totalTime + " ms");
        }

        log.info("For " + noHeapStore.getClass() + " the average time is " + averageTime / 10 + " ms\n");
    }
}
