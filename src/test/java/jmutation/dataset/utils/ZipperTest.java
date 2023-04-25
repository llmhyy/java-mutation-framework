package jmutation.dataset.utils;

import jmutation.dataset.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ZipperTest {
    private static final String FILE_DIRECTORY = String.join(File.separator, "src", "test", "files", "jmutation", "dataset",
            "utils");
    // expected name of the zipped file, check its existence after zipping.
    private static final String NEWLY_ZIPPED_TRACE_FILE_PATH = FILE_DIRECTORY + File.separator + "trace-to-zip.zip";
    private static final String TRACE_FILE_TO_ZIP_FILE_PATH = FILE_DIRECTORY + File.separator + "trace-to-zip.exec";
    private static final String EXPECTED_UNZIPPED_TRACE_FILE_PATH = TRACE_FILE_TO_ZIP_FILE_PATH;
    // expected name of the zipped file, check its contents with the expected
    // contents.
    private static final String NEWLY_UNZIPPED_TRACE_FILE_PATH = FILE_DIRECTORY + File.separator
            + "trace-to-unzip.exec";
    private static final String TRACE_FILE_TO_UNZIP_PATH = FILE_DIRECTORY + File.separator + "trace-to-unzip.zip";


    private static final String DIR_TO_ZIP_PATH = FILE_DIRECTORY + File.separator + "dir-to-zip";
    private static final String NEWLY_ZIPPED_DIR_PATH = DIR_TO_ZIP_PATH + ".zip";


    private static final String NEWLY_UNZIPPED_DIR_PATH = FILE_DIRECTORY + File.separator + "dir-to-unzip";
    private static final String DIR_TO_UNZIP_PATH = NEWLY_UNZIPPED_DIR_PATH + ".zip";

    @Test
    void zip_largeTraceFile_zipsCorrectly() {
        Zipper.zip(TRACE_FILE_TO_ZIP_FILE_PATH);
        // Each zip creates different content (last modified time), so we just check for
        // existence here.
        assertTrue(new File(NEWLY_ZIPPED_TRACE_FILE_PATH).exists());
    }

    @Test
    void unzip_largeTraceFile_unzipsCorrectly() throws IOException {
        Zipper.unzip(TRACE_FILE_TO_UNZIP_PATH, FILE_DIRECTORY);
        contentsAreEqual(EXPECTED_UNZIPPED_TRACE_FILE_PATH, NEWLY_UNZIPPED_TRACE_FILE_PATH);
    }

    @Test
    void zip_directory_zipsCorrectly() {
        Zipper.zip(DIR_TO_ZIP_PATH);
        assertTrue(new File(NEWLY_ZIPPED_DIR_PATH).exists());
    }

    @Test
    void unzip_directory_unzipsCorrectly() throws IOException {
        Zipper.unzip(DIR_TO_UNZIP_PATH, NEWLY_UNZIPPED_DIR_PATH);
        TestUtils.dirsAreEqual(DIR_TO_ZIP_PATH, NEWLY_UNZIPPED_DIR_PATH);
    }

    @Test
    void zip_then_unzip_directory_zipsAndUnzipsCorrectly() throws IOException {
        Zipper.zip(DIR_TO_ZIP_PATH);
        Zipper.unzip(NEWLY_ZIPPED_DIR_PATH, NEWLY_UNZIPPED_DIR_PATH);
        TestUtils.dirsAreEqual(DIR_TO_ZIP_PATH, NEWLY_UNZIPPED_DIR_PATH);
    }

    @AfterEach
    void afterEach() throws IOException {
        TestUtils.deleteIfExists(new File(NEWLY_ZIPPED_TRACE_FILE_PATH));
        TestUtils.deleteIfExists(new File(NEWLY_UNZIPPED_TRACE_FILE_PATH));
        TestUtils.deleteIfExists(new File(NEWLY_UNZIPPED_DIR_PATH));
        TestUtils.deleteIfExists(new File(NEWLY_ZIPPED_DIR_PATH));
    }

    private void contentsAreEqual(String expectedFilePath, String actualFilePath) {
        try (FileInputStream fis1 = new FileInputStream(expectedFilePath);
             FileInputStream fis2 = new FileInputStream(actualFilePath);) {
            byte[] file1Data = new byte[fis1.available()];
            fis1.read(file1Data);

            byte[] file2Data = new byte[fis2.available()];
            fis2.read(file2Data);
            assertTrue(Arrays.equals(file1Data, file2Data));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}
