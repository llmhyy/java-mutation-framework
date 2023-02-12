package jmutation.parser;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MavenProjectParserTest {
    private MavenProjectParser mavenProjectParser;

    @TempDir
    private File root;
    private Model mavenModel;

    @BeforeEach
    void setup() throws IOException, XmlPullParserException {
        File tempPomFile = new File(root, "pom.xml");
        String pomContentStr = String.join("\n",
                "<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://maven.apache.org/POM/4.0.0\"",
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">",
                "    <modelVersion>4.0.0</modelVersion>",
                "    <groupId>java-mutation-framework</groupId>",
                "    <artifactId>java-mutation-framework</artifactId>",
                "    <version>0.0.1-SNAPSHOT</version>",
                "    <properties>",
                "        <maven.compiler.source>11</maven.compiler.source>",
                "        <maven.compiler.target>11</maven.compiler.target>",
                "        <maven.model.version>3.8.2</maven.model.version>",
                "    </properties>",
                "    <build>",
                "        <plugins>",
                "            <plugin>",
                "                <groupId>org.apache.maven.plugins</groupId>",
                "                <artifactId>maven-compiler-plugin</artifactId>",
                "                <version>3.10.1</version>",
                "                <configuration>",
                "                    <source>11</source>",
                "                    <target>11</target>",
                "                </configuration>",
                "            </plugin>",
                "            <plugin>",
                "                <groupId>org.apache.maven.plugins</groupId>",
                "                <artifactId>maven-surefire-plugin</artifactId>",
                "                <version>3.0.0-M6</version>",
                "                <configuration>",
                "                    <systemPropertyVariables>",
                "                        <maven.home>${maven.home}</maven.home>",
                "                    </systemPropertyVariables>",
                "                </configuration>",
                "            </plugin>",
                "        </plugins>",
                "    </build>",
                "",
                "    <dependencies>",
                "        <!-- https://mvnrepository.com/artifact/org.eclipse.jdt/org.eclipse.jdt.core -->",
                "        <dependency>",
                "            <groupId>org.eclipse.jdt</groupId>",
                "            <artifactId>org.eclipse.jdt.core</artifactId>",
                "            <version>3.30.0</version>",
                "            <exclusions>",
                "                <exclusion>",
                "                    <groupId>org.osgi.service</groupId>",
                "                    <artifactId>org.osgi.service.prefs</artifactId>",
                "                </exclusion>",
                "            </exclusions>",
                "        </dependency>",
                "        <!-- https://mvnrepository.com/artifact/org.eclipse.platform/org.eclipse.core.filebuffers -->",
                "        <dependency>",
                "            <groupId>org.eclipse.platform</groupId>",
                "            <artifactId>org.eclipse.core.filebuffers</artifactId>",
                "            <version>3.7.200</version>",
                "        </dependency>",
                "        <!-- https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter -->",
                "        <dependency>",
                "            <groupId>org.junit.jupiter</groupId>",
                "            <artifactId>junit-jupiter</artifactId>",
                "            <version>5.9.0</version>",
                "            <scope>test</scope>",
                "        </dependency>",
                "        <!-- https://mvnrepository.com/artifact/org.apache.maven/maven-model -->",
                "        <dependency>",
                "            <groupId>org.apache.maven</groupId>",
                "            <artifactId>maven-model</artifactId>",
                "            <version>${maven.model.version}</version>",
                "        </dependency>",
                "    </dependencies>",
                "</project>");
        try (FileWriter pomFileWriter = new FileWriter(tempPomFile)) {
            pomFileWriter.write(pomContentStr);
        }
        MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        mavenModel = mavenXpp3Reader.read(new ByteArrayInputStream(pomContentStr.getBytes()));
        mavenProjectParser = new MavenProjectParser(root);
    }

    @Test
    void getExternalLibs_PomFileWithDependenciesProvided_GetsCorrectExtLibLocations() {
        List<File> actualResult = mavenProjectParser.getExternalLibs();
        List<Dependency> dependencies = mavenModel.getDependencies();
        List<File> expectedResult = new ArrayList<>();
        File mavenRepoRoot = new File(String.join(File.separator, System.getProperty("user.home"), ".m2",
                "repository"));
        for (Dependency dependency : dependencies) {
            String version = dependency.getVersion();
            if (version.startsWith("${")) {
                String propertyName = version.substring(2, version.length() - 1);
                Properties properties = mavenModel.getProperties();
                version = properties.getProperty(propertyName);
            }
            expectedResult.add(new File(mavenRepoRoot, String.join(File.separator,
                    dependency.getGroupId().replace(".", File.separator),
                    dependency.getArtifactId(), version)));
        }
        assertEquals(expectedResult, actualResult);
    }

}