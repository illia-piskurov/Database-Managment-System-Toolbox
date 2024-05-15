package org.sumdu.helpers;

import org.sumdu.models.DatabaseInstance;

public class EnvHelper {
    public static String[] getEnvVars(DatabaseInstance database) {
        return switch (database.getDatabase().toLowerCase()) {
            case "postgresql" -> new String[] {
                    "POSTGRES_PASSWORD=" + database.getPass(),
                    "PGPORT=" + database.getPort()
            };
            case "mysql" -> new String[] {
                    "MYSQL_ROOT_PASSWORD=" + database.getPass(),
                    "MYSQL_SSL_MODE=DISABLED",
                    "MYSQL_ALLOW_CLEAR_PASSWORD=yes"
            };
            case "mariadb" -> new String[] {
                    "MARIADB_ROOT_PASSWORD=" + database.getPass(),
                    "MARIADB_SSL_MODE=DISABLED",
                    "MARIADB_ALLOW_CLEAR_PASSWORD=yes"
            };
            case "db2" -> new String[] {
                    "DB2INST1_PASSWORD=" + database.getPass(),
                    "LICENSE=accept",
                    "DBNAME=toolbox"
            };
            default -> throw new IllegalArgumentException("Unsupported database type: " + database.getDatabase());
        };
    }
}
