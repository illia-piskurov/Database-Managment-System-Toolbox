package org.sumdu.controllers;

import com.github.dockerjava.api.exception.DockerException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.sumdu.helpers.AlertHelper;
import org.sumdu.models.DatabaseInstance;

import java.util.Map;

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

    private Map<String, Map<String, String>> databases;
    private MainController mainController;

    public void initialize() {
        databases = Map.of(
                "PostgreSQL", Map.of(
                        "port", "5432",
                        "pass", "admin",
                        "image_name", "postgres:latest"
                ),
                "MySQL", Map.of(
                        "port", "3306",
                        "pass", "admin",
                        "image_name", "mysql:latest"
                ),
                "MariaDB", Map.of(
                        "port", "3307",
                        "pass", "admin",
                        "image_name", "mariadb:latest"
                )
        );

        ObservableList<String> databaseList = FXCollections.observableArrayList(databases.keySet());

        DatabaseComboBox.setItems(databaseList);
        DatabaseComboBox.getSelectionModel().select("PostgreSQL");
        PasswordText.setText(databases.get("PostgreSQL").get("pass"));
        PortText.setText(databases.get("PostgreSQL").get("port"));
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
            var database =  new DatabaseInstance(
                    database_type,
                    databases.get(database_type).get("image_name"),
                    NameText.getText(),
                    PortText.getText(),
                    PasswordText.getText()
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
        PasswordText.setText(databases.get(selectedDatabase).get("pass"));
        PortText.setText(databases.get(selectedDatabase).get("port"));
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
