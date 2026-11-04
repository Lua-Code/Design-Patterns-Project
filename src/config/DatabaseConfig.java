package config;

public class DatabaseConfig {

    private static final String CONNECTION_STRING =  "mongodb+srv://Kevin_David:russianbias@cluster0.uq9abcj.mongodb.net/";

    private static final String DATABASE_NAME = "Design_Patterns";

    private DatabaseConfig() {
    }

    public static String getConnectionString() {
        return CONNECTION_STRING;
    }

    public static String getDatabaseName() {
        return DATABASE_NAME;
    }
}
