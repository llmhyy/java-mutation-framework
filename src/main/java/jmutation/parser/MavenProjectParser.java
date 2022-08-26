package jmutation.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import jmutation.constants.MavenConstants;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class MavenProjectParser extends ProjectParser {
    Model model;
    Build build;
    String rootPath;

    public MavenProjectParser(File root) {
        super(root);
        rootPath = root.getAbsolutePath();
        File pomFile = getPomFile();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            model = reader.read(new FileReader(pomFile));
            build = model.getBuild();
        } catch (XmlPullParserException | IOException e) {
            System.out.println("pom not found");
        }
    }

    public String getProjectName() {
        return model.getName();
    }

    public String getSrcFolderPath() {
        String srcFolderPath = rootPath + File.separator + MavenConstants.SRC_FOLDER;
        if (build == null) {
            return srcFolderPath;
        }
        String pomPath = build.getSourceDirectory();
        if (pomPath == null) {
            return srcFolderPath;
        }
        return rootPath + pomPath;
    }

    public String getTestFolderPath() {
        String testFolderPath = rootPath + File.separator + MavenConstants.TEST_FOLDER;
        if (build == null) {
            return testFolderPath;
        }
        String pomPath = build.getTestSourceDirectory();
        if (pomPath == null) {
            return testFolderPath;
        }
        return rootPath + pomPath;
    }

    public String getCompiledSrcFolderPath() {
        String outputPath = MavenConstants.COMPILATION_FOLDER + File.separator +
                MavenConstants.COMPILED_CLASS_FOLDER;
        if (build == null) {
            return outputPath;
        }
        String pomPath = build.getOutputDirectory();
        if (pomPath == null) {
            return outputPath;
        }
        return rootPath + pomPath;
    }

    public String getCompiledTestFolderPath() {
        String testOutputPath = MavenConstants.COMPILATION_FOLDER + File.separator +
                MavenConstants.TEST_CLASS_FOLDER;
        if (build == null) {
            return testOutputPath;
        }
        String pomPath = build.getTestOutputDirectory();
        if (pomPath == null) {
            return testOutputPath;
        }
        return rootPath + pomPath;
    }

    private File getPomFile() {
        Predicate<File> isPomPredicate = file -> file.getName().equals("pom.xml");
        List<File> pomFiles = walk(root, isPomPredicate);
        return pomFiles.get(0);
    }
}
