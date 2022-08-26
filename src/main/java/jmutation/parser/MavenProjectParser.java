package jmutation.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import jmutation.constants.MavenConstants;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class MavenProjectParser extends ProjectParser {
    Model model;
    String rootPath;

    public MavenProjectParser(File root) {
        super(root);
        rootPath = root.getAbsolutePath();
        File pomFile = getPomFile();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            model = reader.read(new FileReader(pomFile));
        } catch (XmlPullParserException | IOException e) {
            System.out.println("pom not found");
        }
    }

    public String getProjectName() {
        return model.getName();
    }

    public String getSrcFolderPath() {
        String srcFolderPath = model.getBuild().getSourceDirectory();
        if (srcFolderPath == null) {
            srcFolderPath = rootPath + File.separator + MavenConstants.SRC_FOLDER;
        }
        return rootPath + srcFolderPath;
    }

    public String getTestFolderPath() {
        String testFolderPath = model.getBuild().getTestSourceDirectory();
        if (testFolderPath == null) {
            testFolderPath = File.separator + MavenConstants.TEST_FOLDER;
        }
        return rootPath + testFolderPath;
    }

    public String getCompiledSrcFolderPath() {
        String outputPath = model.getBuild().getOutputDirectory();
        if (outputPath == null) {
            outputPath = rootPath + File.separator + MavenConstants.COMPILATION_FOLDER + File.separator +
                    MavenConstants.COMPILED_CLASS_FOLDER;
        }
        return rootPath + outputPath;
    }

    public String getCompiledTestFolderPath() {
        String testOutputPath = model.getBuild().getTestOutputDirectory();
        if (testOutputPath == null) {
            testOutputPath =  rootPath + File.separator + MavenConstants.COMPILATION_FOLDER + File.separator +
                    MavenConstants.TEST_CLASS_FOLDER;
        }
        return rootPath + testOutputPath;
    }

    private File getPomFile() {
        Predicate<File> isPomPredicate = file -> file.getName().equals("pom.xml");
        List<File> pomFiles = walk(root, isPomPredicate);
        return pomFiles.get(0);
    }
}
