package jmutation.constants;

public enum ExternalLibrary {
    JUNIT("junit"),
    HAMCREST("org.hamcrest.core"),
    TEST_RUNNER("testrunner"),
    BCEL("bcel-6.0"),
    JAVA_ASSIST("javassist"),
    INSTRUMENTATOR("instrumentator"),
    JUNIT_PLATFORM("junit-platform-console-standalone-1.0.0"),
    JUNIT5_RUNNER("junit-platform-runner-1.0.0"),
    TESTNG("org.testng_7.4.0.r202105021533");
    private final String name;

    ExternalLibrary(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
