package jmutation.dataset;

public class TestCase {
    private final String testClassName;
    private final String testMethodName;
    private final String testCaseStr;

    public TestCase(String testClassName, String testMethodName, String testCaseStr) {
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.testCaseStr = testCaseStr;
    }

    public String testClassName() {
        return testClassName;
    }

    public String testMethodName() {
        return testMethodName;
    }

    public String testCaseStr() {
        return testCaseStr;
    }

    @Override
    public String toString() {
        return testCaseStr;
    }
}
