package org.sumdu.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.sumdu.controllers.MainController;
import org.sumdu.models.DatabaseInstance;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DockerService {
    private DockerClient dockerClient;

    public DockerService() {
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

    public boolean isImageDownloaded(String imageName) {
        try {
            List<Image> images = dockerClient.listImagesCmd()
                    .exec();

            for (Image image : images) {
                if (image.getRepoTags() != null) {
                    for (String repoTag : image.getRepoTags()) {
                        if (repoTag.equals(imageName)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (DockerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isContainerExists(String containerName) throws DockerException, InterruptedException {
        return dockerClient.listContainersCmd()
                .withNameFilter(Collections.singletonList(containerName))
                .exec()
                .stream()
                .anyMatch(container -> container.getNames()[0].equals("/" + containerName));
    }

    public void runDockerContainer(DatabaseInstance database, Map<String, String> containers) {
        try {
            if (isContainerExists(database.getImage_name())) {
                startContainer(database.getName());
            } else {
                createAndStartContainer(database, containers);
            }
        } catch (DockerException | InterruptedException e) {
            e.printStackTrace();
            // Обработка ошибок, если не удалось создать или запустить контейнер
        }
    }

    public void startContainer(String containerName) throws DockerException, InterruptedException {
        dockerClient.startContainerCmd(containerName).exec();
    }

    public void stopContainer(String containerName) {
        dockerClient.stopContainerCmd(containerName).exec();
    }

    public void createAndStartContainer(DatabaseInstance database, Map<String, String> containers) throws DockerException, InterruptedException {
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

    public void pullImage(DatabaseInstance database, VBox vbox, MainController mainController) {
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(database.getImage_name());
        pullImageCmd.exec(new ResultCallback<PullResponseItem>() {
            @Override
            public void onStart(Closeable closeable) {
            }

            @Override
            public void onNext(PullResponseItem object) {
            }

            @Override
            public void onError(Throwable throwable) {
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Error downloading " + database.getImage_name());
                    vbox.getChildren().set(0, errorLabel);
                });
            }

            @Override
            public void onComplete() {
                Platform.runLater(() -> {
                    mainController.DatabasesView.getItems().remove(vbox);
                    mainController.addButtonAndLabel(database);
                });
            }

            @Override
            public void close() throws IOException {
            }
        });
    }
}
