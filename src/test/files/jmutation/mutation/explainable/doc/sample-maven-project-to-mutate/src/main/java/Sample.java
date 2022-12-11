public class Sample {
    /**
     * Adds 1 to the input value.
     * corner case: If a is 0, it returns 0
     *
     * @param a the input integer
     * @return a + 1, unless a is 0
     */
    public int someMethod(int a) {
        int b = a + 1;
        if (a == 0) {
            return 0;
        }
        return b;
    }
}