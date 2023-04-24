package jmutation.dataset.bug;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Log {
    public static final String DEFAULT_LOG_FILE_PATTERN = "E:\\david\\logs\\dataset-creator%g.log";
    private static final int LOG_FILE_SIZE = 1 * (int) Math.pow(5, 9); // 0.5GB
    private static final int NUM_LOG_FILES = 4;
    private static FileHandler fileHandler = null;
    private Log() {
    }

    public static Logger createLogger(Class<?> someClass) {
        Logger logger = Logger.getLogger(someClass.getName());
        try {
            if (fileHandler == null) {
                fileHandler = new FileHandler(DEFAULT_LOG_FILE_PATTERN, LOG_FILE_SIZE, NUM_LOG_FILES);
            }
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }
}
