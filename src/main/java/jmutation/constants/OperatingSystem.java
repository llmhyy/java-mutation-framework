package jmutation.constants;

public enum OperatingSystem {
    WINDOWS, MACOS, LINUX, OTHERS;

    public static OperatingSystem getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return OperatingSystem.WINDOWS;
        if (os.contains("nix") || os.contains("nux")) return OperatingSystem.LINUX;
        if (os.contains("mac")) return OperatingSystem.MACOS;
        return OperatingSystem.OTHERS;
    }
}
