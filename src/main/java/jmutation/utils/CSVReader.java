package jmutation.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class CSVReader {
    private final BufferedReader bufferedReader;
    private final String delimiter;

    public CSVReader(BufferedReader bufferedReader, String delimiter) {
        this.bufferedReader = bufferedReader;
        this.delimiter = delimiter;
    }

    public Iterator<List<String>> read() {
        return new Iterator<>() {
            private String line = null;

            @Override
            public boolean hasNext() {
                if (line == null) {
                    try {
                        line = bufferedReader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            bufferedReader.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                return line != null;
            }

            @Override
            public List<String> next() {
                List<String> result = new ArrayList<>();
                Scanner scanner = new Scanner(line);
                scanner.useDelimiter(delimiter);
                while (scanner.hasNext()) {
                    String data = scanner.next();
                    result.add(data);
                }
                line = null;
                return result;
            }
        };
    }
}
