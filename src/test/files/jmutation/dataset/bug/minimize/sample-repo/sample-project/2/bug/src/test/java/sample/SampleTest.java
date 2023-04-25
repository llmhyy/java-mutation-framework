package sample;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SampleTest {
	@Test
	public void testMethod() {
		Sample sample = new Sample();
		assertEquals(1, sample.method());
	}
}
