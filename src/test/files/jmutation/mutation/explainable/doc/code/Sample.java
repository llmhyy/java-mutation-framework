public class Sample {
    /**
     * This is a description.
     * corner case: 0 is not allowed
     */
    public int method(int a) {
        int b = a + 1;
        if (a == 0) {
            throw new RuntimeException("0 is not allowed");
        }
        return b;
    }
}