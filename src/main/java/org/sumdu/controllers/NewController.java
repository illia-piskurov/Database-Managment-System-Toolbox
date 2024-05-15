package org.sumdu.controllers;

import com.github.dockerjava.api.exception.DockerException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.sumdu.helpers.AlertHelper;
import org.sumdu.helpers.JSONHelper;
import org.sumdu.models.DatabaseInstance;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class NewController {
    public Button CancelButton;
    public Button Create;
    public Label NameLabel;
    public TextField NameText;
    public TextField PortText;
    public Label PortLabel;
    public TextField PasswordText;
    public Label PasswordLabel;
    public ComboBox<String> DatabaseComboBox;
    public Label DatabaseLabel;
    public Label TitleLabel;

    private MainController mainController;
    private List<DatabaseInstance> databases;

    public void initialize() throws IOException {
        databases = JSONHelper.readDatabaseInstancesFromFile("./config.json");

        ObservableList<String> databaseList = FXCollections.observableArrayList(
                databases.stream()
                        .map(DatabaseInstance::getDatabase)
                        .collect(Collectors.toList())
        );
        var postgresInstance = databases.stream()
                .filter(instance -> "PostgreSQL".equals(instance.getDatabase()))
                .findFirst()
                .orElse(null);

        DatabaseComboBox.setItems(databaseList);
        DatabaseComboBox.getSelectionModel().select(postgresInstance.getDatabase());
        PasswordText.setText(postgresInstance.getPass());
        PortText.setText(postgresInstance.getPort());
    }

    public void cancelButtonClicked(MouseEvent mouseEvent) {
        Stage stage = (Stage) CancelButton.getScene().getWindow();
        stage.close();
    }

    public void createButtonClicked(MouseEvent mouseEvent) {
        if (NameText.getText().isEmpty()) {
            AlertHelper.showAlert("Name cannot be empty!");
        } else if (PasswordText.getText().isEmpty()) {
            AlertHelper.showAlert("Password cannot be empty!");
        } else if (!PortText.getText().matches("\\d+")) {
            AlertHelper.showAlert("Port must be a integer!");
        } else {
            var database_type = DatabaseComboBox.getSelectionModel().getSelectedItem();
            var dbInstance = databases.stream()
                    .filter(instance -> database_type.equals(instance.getDatabase()))
                    .findFirst()
                    .orElse(null);
            var database =  new DatabaseInstance(
                    dbInstance.getDatabase(),
                    dbInstance.getImage_name(),
                    NameText.getText(),
                    PortText.getText(),
                    PasswordText.getText(),
                    dbInstance.getEnvs()
            );

            try {
                mainController.addNewDatabaseInstance(database);
            } catch (DockerException e) {
                AlertHelper.showAlert(e.getMessage());
            }

            Stage stage = (Stage) CancelButton.getScene().getWindow();
            stage.close();
        }
    }

    public void onDatabaseComboBoxAction(ActionEvent actionEvent) {
        String selectedDatabase = DatabaseComboBox.getSelectionModel().getSelectedItem();
        var dbInstance = databases.stream()
                .filter(instance -> selectedDatabase.equals(instance.getDatabase()))
                .findFirst()
                .orElse(null);
        PasswordText.setText(dbInstance.getPass());
        PortText.setText(dbInstance.getPort());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
