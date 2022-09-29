package jmutation.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceExtractor {
    public static void extractFile(String name, String outputPath) throws IOException {
        File target = new File(outputPath, name);
        if (target.exists())
            return;

        target.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(target);
        ClassLoader cl = ResourceExtractor.class.getClassLoader();
        InputStream in = cl.getResourceAsStream(name);

        byte[] buf = new byte[8 * 1024];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        out.close();
        in.close();
    }
}
