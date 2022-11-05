package jmutation.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomSingleton {
    private static final Random random = new Random();

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

    public List shuffle(List list) {
        List shuffledList = new ArrayList<>();
        shuffledList.addAll(list);
        Collections.shuffle(shuffledList, random);
        return shuffledList;
    }

    private static class LazyLoader {
        // By wrapping, the singleton will only be generated when getSingleton() is called,
        // not when class is accessed e.g. RandomSingleton.getClass()
        static final RandomSingleton INSTANCE = new RandomSingleton();
    }
}
