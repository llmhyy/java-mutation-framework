package jmutation.dataset.utils;

import jmutation.dataset.bug.Log;

import java.util.logging.Level;
import java.util.logging.Logger;


public class Counter {
    private static final Logger logger = Log.createLogger(Counter.class);
    private static final Object lock = new Object();
    private int bugId;

    public Counter(int bugId) {
        this.bugId = bugId;
    }

    public int increaseAndGetBugId() {
        synchronized (lock) {
            logger.log(Level.INFO, Thread.currentThread().getName() + " : " + bugId);
            bugId++;
            return bugId;
        }
    }
}
