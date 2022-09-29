package jmutation.constants;

public enum ExternalLibrary {
    JUNIT("junit"),
    HAMCREST("org.hamcrest.core"),
    TEST_RUNNER("testrunner"),
    BCEL("bcel-6.0"),
    JAVA_ASSIST("javassist"),
    INSTRUMENTATOR("instrumentator"),
    JUNIT_PLATFORM("junit-platform-console-standalone-1.9.0"),
    JUNIT5_RUNNER("org.junit.platform.runner_1.8.1.v20211018-1956"),
    TESTNG("org.testng_7.4.0.r202105021533");
    private final String name;

    ExternalLibrary(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
