package org.sumdu.models;

public class DatabaseInstance {
    private String database;
    private String image_name;
    private String name;
    private String port;
    private String pass;
    private String containerId;

    public DatabaseInstance(String database, String image_name, String name, String port, String pass) {
        this.database = database;
        this.image_name = image_name;
        this.name = name;
        this.port = port;
        this.pass = pass;
    }

    public DatabaseInstance() {
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

    public String getContainerId() {
        return containerId;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
}
