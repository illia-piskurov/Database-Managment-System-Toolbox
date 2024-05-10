package org.sumdu.controllers;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
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

import org.sumdu.models.DatabaseInstance;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Map;

public class MainController {
    public Button NewButton;
    public ListView DatabasesView;

    private DockerClient dockerClient;

    private Map<String, String> containers = new HashMap<String, String>();

    public void initialize() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("npipe:////./pipe/docker_engine")
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        dockerClient = DockerClientImpl.getInstance(config, httpClient);
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

    public void addNewDatabaseInstance(DatabaseInstance database) {
        if (isImageDownloaded(database.getImage_name())) {
            addButtonAndLabel(database);
        } else {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

            Label label = new Label("Downloading " + database.getImage_name() + "...");
            HBox.setMargin(label, new Insets(0, 10, 0, 0));
            HBox hbox = new HBox(label, progressBar);

            VBox vbox = new VBox(hbox);

            DatabasesView.getItems().add(vbox);

            //pullImage(image_name, vbox, name);
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

    private void addButtonAndLabel(DatabaseInstance database) {
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> {
            VBox vboxToRemove = (VBox) ((Button) event.getSource()).getParent().getParent();
            DatabasesView.getItems().remove(vboxToRemove);
        });

        Button runAndStopButton = new Button("Run");
        runAndStopButton.setOnAction(event -> {
            var button = (Button) event.getSource();
            if (button.getText().equals("Run")) {
                runDockerContainer(database);
                button.setText("Stop");
            } else {
                stopContainer(database.getName());
                button.setText("Run");
            }
        });

        Label label = new Label(database.getName() + " (" + database.getDatabase() + ")");
        HBox hbox = new HBox(runAndStopButton, deleteButton, label);
        hbox.setSpacing(10);

        VBox vbox = new VBox(hbox);

        DatabasesView.getItems().add(vbox);
    }

    private boolean isImageDownloaded(String imageName) {
        try {
            List<com.github.dockerjava.api.model.Image> images = dockerClient.listImagesCmd()
                    .withImageNameFilter(imageName)
                    .exec();
            return !images.isEmpty();
        } catch (DockerException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isContainerExists(String containerName) throws DockerException, InterruptedException {
        return dockerClient.listContainersCmd()
                .withNameFilter(Collections.singletonList(containerName))
                .exec()
                .stream()
                .anyMatch(container -> container.getNames()[0].equals("/" + containerName));
    }

    private void runDockerContainer(DatabaseInstance database) {
        try {
            if (isContainerExists(database.getImage_name())) {
                startContainer(database.getName());
            } else {
                createAndStartContainer(database);
            }
        } catch (DockerException | InterruptedException e) {
            e.printStackTrace();
            // Обработка ошибок, если не удалось создать или запустить контейнер
        }
    }

    private void startContainer(String containerName) throws DockerException, InterruptedException {
        dockerClient.startContainerCmd(containerName).exec();
    }

    private void stopContainer(String containerName) {
        dockerClient.stopContainerCmd(containers.get(containerName)).exec();
    }

    private void createAndStartContainer(DatabaseInstance database) throws DockerException, InterruptedException {
        int hostPort = Integer.parseInt(database.getPort());

        ExposedPort tcp5432 = ExposedPort.tcp(hostPort);
        Ports portBindings = new Ports();
        portBindings.bind(tcp5432, Ports.Binding.bindPort(hostPort));

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withPortBindings(portBindings);

        CreateContainerResponse container = dockerClient.createContainerCmd(database.getImage_name())
                .withHostConfig(hostConfig)
                .withExposedPorts(tcp5432)
                .withEnv(
                        "POSTGRES_PASSWORD=" + database.getPass(),
                        "PGPORT=" + database.getPort())
                .exec();

        containers.put(database.getName(), container.getId());

        dockerClient.startContainerCmd(container.getId()).exec();
    }
}