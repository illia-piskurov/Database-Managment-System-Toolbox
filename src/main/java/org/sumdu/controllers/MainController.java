package org.sumdu.controllers;

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
import org.sumdu.services.DockerService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {
    public Button NewButton;
    public ListView DatabasesView;

    private DockerService dockerService = new DockerService();
    private Map<String, String> containers = new HashMap<>();

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
            VBox vboxToRemove = (VBox) ((Button) event.getSource()).getParent().getParent();
            DatabasesView.getItems().remove(vboxToRemove);
        });

        Button runAndStopButton = new Button("Run");
        runAndStopButton.setOnAction(event -> {
            var button = (Button) event.getSource();
            if (button.getText().equals("Run")) {
                dockerService.runDockerContainer(database, containers);
                button.setText("Stop");
            } else {
                dockerService.stopContainer(containers.get(database.getName()));
                button.setText("Run");
            }
        });

        Label label = new Label(database.getName() + " (" + database.getDatabase() + ")");
        HBox hbox = new HBox(runAndStopButton, deleteButton, label);
        hbox.setSpacing(10);

        VBox vbox = new VBox(hbox);

        DatabasesView.getItems().add(vbox);
    }
}