package jmutation.execution.output;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.Callable;

public class OutputHandler implements Callable<String> {
    private final StringBuilder stringBuilder = new StringBuilder();
    private BufferedReader bufferedReader;

    protected OutputHandler() {
    }

    protected void setBufferedReader(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    @Override
    public String call() {
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                output(line);
                stringBuilder.append("\n").append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public void output(String outputString) {
        System.out.println(outputString);
    }

    public static class OutputHandlerBuilder {
        protected OutputHandler handler = new OutputHandler();

        public void setBufferedReader(BufferedReader reader) {
            handler.setBufferedReader(reader);
        }

        public OutputHandler build() {
            return handler;
        }
    }
}
