package jmutation.parser;

import jmutation.constants.MavenConstants;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;

import static jmutation.parser.ProjectParser.walk;

class MavenProjectParser extends ProjectInfoParser {
    private String projectName = super.getProjectName();
    private String srcFolderPath = MavenConstants.SRC_FOLDER;
    private String testFolderPath = MavenConstants.TEST_FOLDER;
    private String compiledSrcFolderPath = MavenConstants.COMPILATION_FOLDER + File.separator +
            MavenConstants.COMPILED_CLASS_FOLDER;
    private String compiledTestFolderPath = MavenConstants.COMPILATION_FOLDER + File.separator +
            MavenConstants.TEST_CLASS_FOLDER;

    private List<File> externalLibs;

    public MavenProjectParser(File root) {
        super(root);
        File pomFile = getPomFile();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try (FileReader fileReader = new FileReader(pomFile)) {
            Model model = reader.read(fileReader);
            projectName = model.getName() == null ? projectName : model.getName();
            Build build = model.getBuild();
            if (build == null) {
                return;
            }
            srcFolderPath = (build.getSourceDirectory() == null) ? srcFolderPath : build.getSourceDirectory();
            testFolderPath = (build.getTestSourceDirectory() == null) ? testFolderPath :
                    build.getTestSourceDirectory();
            compiledSrcFolderPath = (build.getOutputDirectory() == null) ? compiledSrcFolderPath :
                    build.getOutputDirectory();
            compiledTestFolderPath = (build.getTestOutputDirectory() == null) ? compiledTestFolderPath :
                    build.getTestOutputDirectory();
            externalLibs = setupExternalLibs(model);
        } catch (XmlPullParserException | IOException e) {
            System.out.println("pom not found");
        }
    }

    @Override
    String getProjectName() {
        return projectName;
    }

    String getSrcFolderPath() {
        return srcFolderPath;
    }

    String getTestFolderPath() {
        return testFolderPath;
    }

    String getCompiledSrcFolderPath() {
        return compiledSrcFolderPath;
    }

    String getCompiledTestFolderPath() {
        return compiledTestFolderPath;
    }

    private File getPomFile() {
        Predicate<File> isPomPredicate = file -> file.getName().equals("pom.xml");
        List<File> pomFiles = walk(getRoot(), isPomPredicate, false);
        return pomFiles.get(0);
    }

    public List<File> getExternalLibs() {
        return externalLibs;
    }

    private List<File> setupExternalLibs(Model mavenModel) {
        List<File> externalLibs = new ArrayList<>();
        List<Dependency> dependencies = mavenModel.getDependencies();
        File mavenRepoRoot = new File(String.join(File.separator, System.getProperty("user.home"), ".m2",
                "repository"));
        for (Dependency dependency : dependencies) {
            String version = dependency.getVersion();
            if (version.startsWith("${")) {
                String propertyName = version.substring(2, version.length() - 1);
                Properties properties = mavenModel.getProperties();
                version = properties.getProperty(propertyName);
            }
            externalLibs.add(new File(mavenRepoRoot, String.join(File.separator,
                    dependency.getGroupId().replace(".", File.separator),
                    dependency.getArtifactId(), version)));
        }
        return externalLibs;
    }
}
