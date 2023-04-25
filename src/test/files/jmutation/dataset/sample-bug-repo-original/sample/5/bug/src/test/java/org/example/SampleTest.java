package org.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SampleTest {
    @Test
    public void test() {
        Sample sample = new Sample();
        int actual = sample.sampleMethod(1);
        assertEquals(10, actual);
    }
}
