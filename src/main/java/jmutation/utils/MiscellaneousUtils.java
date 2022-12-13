package jmutation.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class MiscellaneousUtils {

    private static Dotenv dotenv = null;

    public static Dotenv loadEnv() {
        if (dotenv != null) return dotenv;
        dotenv = Dotenv.configure().load();
        return dotenv;
    }
}
