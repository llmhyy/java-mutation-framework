package jmutation.mutation.semantic.semseed.io.handler;

import java.io.IOException;

public interface IOHandler {
    boolean write(String string) throws IOException;

    String readAll() throws IOException;

    void setSource(String string);
}
