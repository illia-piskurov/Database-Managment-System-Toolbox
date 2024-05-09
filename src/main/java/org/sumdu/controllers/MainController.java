package org.sumdu.controllers;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;


public class MainController {
    public Button NewButton;
    public ListView DatabasesView;

    private DockerClient dockerClient;

    public void initialize() {
        try {
            dockerClient = DefaultDockerClient.fromEnv().uri("npipe:////./pipe/docker_engine").build();
        } catch (DockerCertificateException e) {
            throw new RuntimeException(e);
        }
    }

    public void newButtonClicked(MouseEvent mouseEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("new.fxml"));
            Parent root = fxmlLoader.load();
            NewController newController = fxmlLoader.getController();
            newController.setMainController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(String.valueOf(getClass().getResource("../icon.png"))));
            stage.setTitle("Create new database service");
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDataAboutNewDatabase(String name, String pass, String port, String image_name) {
        if (isImageDownloaded(image_name)) {
            addButtonAndLabel(image_name);
        } else {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

            Label label = new Label("Downloading " + image_name + "...");
            HBox.setMargin(label, new Insets(0, 10, 0, 0));
            HBox hbox = new HBox(label, progressBar);

            VBox vbox = new VBox(hbox);

            DatabasesView.getItems().add(vbox);

            pullImage(image_name, vbox, name);
        }
    }

    private void pullImage(String imageName, VBox vbox, String name) {
//        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(imageName);
//        pullImageCmd.exec(new ResultCallback<PullResponseItem>() {
//            @Override
//            public void onStart(Closeable closeable) {
//            }
//
//            @Override
//            public void onNext(PullResponseItem object) {
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                Platform.runLater(() -> {
//                    Label errorLabel = new Label("Error downloading " + imageName);
//                    vbox.getChildren().set(0, errorLabel);
//                });
//            }
//
//            @Override
//            public void onComplete() {
//                Platform.runLater(() -> {
//                    DatabasesView.getItems().remove(vbox);
//                    addButtonAndLabel(name);
//                });
//            }
//
//            @Override
//            public void close() throws IOException {
//            }
//        });
    }

    private void addButtonAndLabel(String name) {
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> {
            VBox vboxToRemove = (VBox)((Button) event.getSource()).getParent().getParent();
            DatabasesView.getItems().remove(vboxToRemove);
        });

        Label label = new Label(name);
        HBox.setMargin(label, new Insets(0, 10, 0, 0));
        HBox hbox = new HBox(label, deleteButton);

        VBox vbox = new VBox(hbox);

        DatabasesView.getItems().add(vbox);
    }

    private boolean isImageDownloaded(String imageName) {
        try {
            List<com.spotify.docker.client.messages.Image> images = dockerClient.listImages(DockerClient.ListImagesParam.byName(imageName));
            return !images.isEmpty();
        } catch (DockerException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}