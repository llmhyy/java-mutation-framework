package jmutation.utils;

public class MiscellaneousTestUtils {
    public static boolean isRunningInGitHubActions() {
        return System.getenv("GITHUB_ACTIONS") != null;
    }
}
