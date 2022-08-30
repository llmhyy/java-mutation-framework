package jmutation.utils;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomSingleton {
    private static final Random random = new Random();
    private static class LazyLoader {
        // By wrapping, the singleton will only be generated when genSingleton() is called,
        // not when class is accessed e.g. RandomSingleton.getClass()
        static final RandomSingleton INSTANCE = new RandomSingleton();
    }

    private RandomSingleton() {
    }

    public static RandomSingleton getSingleton() {
        return LazyLoader.INSTANCE;
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    public double random() {
       return random.nextDouble();
    }

    public void shuffle(List list) {
        Collections.shuffle(list, random);
    }
}
