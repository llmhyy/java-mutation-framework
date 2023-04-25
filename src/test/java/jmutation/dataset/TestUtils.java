package jmutation.dataset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtils {
    public static void dirsAreEqual(String expectedDirPath, String actualDirPath) throws IOException {
        Iterator<File> expectedFiles = FileUtils.iterateFiles(new File(expectedDirPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        Iterator<File> actualFiles = FileUtils.iterateFiles(new File(actualDirPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        while (expectedFiles.hasNext() && actualFiles.hasNext()) {
            File expectedFile = expectedFiles.next();
            File actualFile = actualFiles.next();
            assertEquals(expectedFile.getName(), actualFile.getName());
            assertTrue(FileUtils.contentEqualsIgnoreEOL(expectedFile, actualFile, null));
        }
        assertFalse(expectedFiles.hasNext() && actualFiles.hasNext());
    }

    public static void deleteIfExists(File fileToDelete) throws IOException {
        if (fileToDelete.exists()) {
            if (fileToDelete.isDirectory()) {
                FileUtils.deleteDirectory(fileToDelete);
                return;
            }
            fileToDelete.delete();
        }
    }

    public static boolean isRunningInGitHubActions() {
        return System.getenv("GITHUB_ACTIONS") != null;
    }
}
