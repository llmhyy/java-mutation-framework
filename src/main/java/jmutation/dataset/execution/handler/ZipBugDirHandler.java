package jmutation.dataset.execution.handler;

import jmutation.dataset.execution.Request;
import jmutation.dataset.utils.Zipper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ZipBugDirHandler extends Handler {
    private final String dirToZip;

    public ZipBugDirHandler(String dirToZip) {
        super(new BaseHandler());
        this.dirToZip = dirToZip;
    }

    public ZipBugDirHandler(Handler nextHandler, String dirToZip) {
        super(nextHandler);
        this.dirToZip = dirToZip;
    }

    @Override
    protected boolean individualHandler(Request request) {
        zip();
        return true;
    }

    private void zip() {
        Zipper.zip(dirToZip);
        try {
            FileUtils.deleteDirectory(new File(dirToZip));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
