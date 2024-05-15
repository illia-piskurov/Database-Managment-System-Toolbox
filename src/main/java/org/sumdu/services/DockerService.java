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
import org.sumdu.helpers.EnvHelper;
import org.sumdu.models.DatabaseInstance;

import java.io.Closeable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

public class DockerService {
    private final DockerClient dockerClient;

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

    public boolean isImageDownloaded(String imageName) throws DockerException {
        return dockerClient.listImagesCmd()
                .exec()
                .stream()
                .flatMap(image -> Arrays.stream(image.getRepoTags()))
                .anyMatch(repoTag -> repoTag.equals(imageName));
    }

    public boolean isContainerExists(String containerId) throws DockerException {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .withIdFilter(Collections.singletonList(containerId))
                .exec()
                .stream()
                .anyMatch(container -> container.getId().equals(containerId));
    }

    public void runDockerContainer(DatabaseInstance database) throws DockerException, InterruptedException {
        if (isContainerExists(database.getContainerId())) {
            startContainer(database.getContainerId());
        } else {
            createAndStartContainer(database);
        }
    }

    public void startContainer(String containerName) throws DockerException {
        dockerClient.startContainerCmd(containerName).exec();
    }

    public void stopContainer(String containerName) {
        dockerClient.stopContainerCmd(containerName).exec();
    }

    public void createAndStartContainer(DatabaseInstance database) throws DockerException {
        int hostPort = Integer.parseInt(database.getPort());

        ExposedPort exposedPort = ExposedPort.tcp(hostPort);
        Ports portBindings = new Ports();
        portBindings.bind(exposedPort, Ports.Binding.bindPort(hostPort));

        String hostDirectory = String.format("C:\\%s\\data", database.getDatabase().toLowerCase());
        createDirectoryIfNotExists(hostDirectory);

        Volume volume = new Volume("/database");
        Bind bind = new Bind(hostDirectory, volume);

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withPortBindings(portBindings)
                .withBinds(bind);

        var envVars = EnvHelper.getEnvVars(database);
        CreateContainerResponse container = dockerClient.createContainerCmd(database.getImage_name())
                .withHostConfig(hostConfig)
                .withExposedPorts(exposedPort)
                .withEnv(envVars)
                .withLabels(Map.of(
                        "dms-toolbox", "",
                        "port", database.getPort(),
                        "pass", database.getPass(),
                        "name", database.getName(),
                        "image_name", database.getImage_name(),
                        "database", database.getDatabase()
                ))
                .exec();

        database.setContainerId(container.getId());

        dockerClient.startContainerCmd(container.getId()).exec();
    }

    private void createDirectoryIfNotExists(String directoryPath) {
        Path path = Paths.get(directoryPath);
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create directory: " + directoryPath, e);
        }
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
            public void close() {
            }
        });
    }

    public List<DatabaseInstance> readListsOfDatabases() throws DockerException {
        List<DatabaseInstance> databaseInstances = new ArrayList<>();

        dockerClient.listContainersCmd()
                .withShowAll(true)
                .withLabelFilter(Collections.singletonList("dms-toolbox"))
                .exec()
                .forEach(container -> {
                    Map<String, String> labels = container.getLabels();
                    DatabaseInstance databaseInstance = new DatabaseInstance();
                    databaseInstance.setName(labels.get("name"));
                    databaseInstance.setPort(labels.get("port"));
                    databaseInstance.setPass(labels.get("pass"));
                    databaseInstance.setImage_name(labels.get("image_name"));
                    databaseInstance.setDatabase(labels.get("database"));
                    databaseInstance.setContainerId(container.getId());
                    databaseInstances.add(databaseInstance);
                });

        return databaseInstances;
    }

    public void removeContainer(String containerId) throws DockerException, InterruptedException {
        dockerClient.removeContainerCmd(containerId).exec();
    }

    public boolean isContainerRunning(String containerId) throws DockerException {
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .withIdFilter(Collections.singletonList(containerId))
                .exec();
        return containers
                .stream()
                .anyMatch(container -> container.getState().equals("running"));
    }
}
