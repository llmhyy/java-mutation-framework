package jmutation.dataset.bug;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatasetHelper {
    public static ExecutorService createExecutorService() {
        int numOfCores = Runtime.getRuntime().availableProcessors() - 1;
        return Executors.newFixedThreadPool(numOfCores);
    }
}
