package jmutation.dataset.utils;

import jmutation.dataset.bug.Log;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zipper {
    private static final Logger logger = Log.createLogger(Zipper.class);

    private Zipper() {
    }

    public static boolean zip(String path) {
        File file = new File(path);
        if (!file.exists()) {
            logger.warning(String.format("File at %s not found", path));
            return false;
        }

        try (FileOutputStream fos = new FileOutputStream(file.getParent() + File.separator + FilenameUtils.removeExtension(file.getName()) + ".zip");
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            return zip(file, ".", zipOut);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean zip(File file, String parentPath, ZipOutputStream zos) {
        logger.info(String.format("Zipping %s", file.getAbsoluteFile()));
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (File child : children) {
                zip(child, parentPath + File.separator + file.getName(), zos);
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                ZipEntry zipEntry = new ZipEntry(parentPath + File.separator + file.getName());
                zos.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zos.write(bytes, 0, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.severe(String.format("Zipping %s failed", file.getAbsoluteFile()));
                return false;
            }
        }
        logger.info(String.format("Done zipping %s", file.getAbsoluteFile()));
        return true;
    }

    public static boolean unzip(String path, String newPath) {
        File destDir = new File(newPath);

        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(path));) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        // write file content
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
