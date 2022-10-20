package jmutation.mutation.semantic.semseed.io.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileHandler implements IOHandler {
    private File file;

    public boolean write(String string) throws IOException {
        return false;
    }

    public String readAll() throws IOException {
        return Files.readString(file.toPath());
    }

    public void setSource(String filePath) {
        this.file = new File(filePath);
    }
}
