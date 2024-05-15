package org.sumdu.controllers;

import com.github.dockerjava.api.exception.DockerException;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import org.sumdu.helpers.AlertHelper;
import org.sumdu.models.DatabaseInstance;
import org.sumdu.services.DockerService;

import java.io.IOException;
import java.util.List;

public class MainController {
    public Button NewButton;
    public ListView DatabasesView;

    private DockerService dockerService = new DockerService();
    private List<DatabaseInstance> databaseInstances;

    public void initialize() {
        databaseInstances = dockerService.readListsOfDatabases();

        for (var databaseInstance : databaseInstances) {
            addButtonAndLabel(databaseInstance);
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

    public void addNewDatabaseInstance(DatabaseInstance database) throws DockerException {
        databaseInstances.add(database);

        if (dockerService.isImageDownloaded(database.getImage_name())) {
            addButtonAndLabel(database);
        } else {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

            Label label = new Label("Downloading " + database.getImage_name() + "...");
            HBox.setMargin(label, new Insets(0, 10, 0, 0));
            HBox hbox = new HBox(label, progressBar);

            VBox vbox = new VBox(hbox);

            DatabasesView.getItems().add(vbox);

            dockerService.pullImage(database, vbox, this);
        }
    }

    public void addButtonAndLabel(DatabaseInstance database) {
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> {
            try {
                dockerService.removeContainer(database.getContainerId());
                HBox vboxToRemove = (HBox) ((Button) event.getSource()).getParent().getParent();
                DatabasesView.getItems().remove(vboxToRemove);
            } catch (InterruptedException e) {
                AlertHelper.showAlert(e.getMessage());
            }
        });

        Button runAndStopButton = new Button("Run");
        runAndStopButton.setOnAction(event -> {
            var button = (Button) event.getSource();
            if (button.getText().equals("Run")) {
                try {
                    dockerService.runDockerContainer(database);
                    button.setText("Stop");
                } catch (DockerException | InterruptedException e) {
                    AlertHelper.showAlert(e.getMessage());
                }
            } else {
                dockerService.stopContainer(database.getContainerId());
                button.setText("Run");
            }
        });

        Label nameLabel = new Label(String.format("%s (%s)", database.getName(), database.getDatabase()));
        String jdbcStr = database.getJDBCStr();
        Label jdbcLabel = new Label(jdbcStr);

        Button copyButton = new Button("Copy JDBC to clipboard");
        copyButton.setOnAction(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(jdbcStr);
            clipboard.setContent(content);
        });

        VBox labelBox = new VBox(nameLabel, jdbcLabel);
        HBox hbox = new HBox(runAndStopButton, deleteButton, copyButton);
        labelBox.setAlignment(Pos.TOP_LEFT);

        HBox item = new HBox(hbox, labelBox);
        item.setSpacing(10);
        hbox.setSpacing(10);

        DatabasesView.getItems().add(item);
    }
}