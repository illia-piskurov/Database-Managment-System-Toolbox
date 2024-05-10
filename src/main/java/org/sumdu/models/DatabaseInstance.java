package org.sumdu.models;

public class DatabaseInstance {
    private String database;
    private String image_name;
    private String name;
    private String port;
    private String pass;

    public DatabaseInstance(String database, String image_name, String name, String port, String pass) {
        this.database = database;
        this.image_name = image_name;
        this.name = name;
        this.port = port;
        this.pass = pass;
    }

    public String getDatabase() {
        return database;
    }

    public String getImage_name() {
        return image_name;
    }

    public String getName() {
        return name;
    }

    public String getPort() {
        return port;
    }

    public String getPass() {
        return pass;
    }
}
