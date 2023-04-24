package sample;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SampleTest {
	@Test
	public void testMethod() {
		Sample sample = new Sample();
		int actual = sample.method(0);
		assertEquals(3, actual);
	}
}