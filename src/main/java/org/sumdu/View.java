package org.sumdu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class View extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (isDockerInstalled()) {
            Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
            primaryStage.getIcons().add(new Image(String.valueOf(getClass().getResource("icon.png"))));
            primaryStage.setTitle("Database Management System Toolbox");
            primaryStage.setScene(new Scene(root, 600, 600));
            primaryStage.setResizable(false);
            primaryStage.show();
        } else {
            Parent root = FXMLLoader.load(getClass().getResource("nodocker.fxml"));
            primaryStage.getIcons().add(new Image(String.valueOf(getClass().getResource("icon.png"))));
            primaryStage.setTitle("Database Management System Toolbox");
            primaryStage.setScene(new Scene(root, 400, 200));
            primaryStage.setResizable(false);
            primaryStage.show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static boolean isDockerInstalled() {
        try {
            Process process = new ProcessBuilder("docker", "--version").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Docker version")) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
