package org.sumdu.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

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
                        "pass", "admin"
                ),
                "MySQL", Map.of(
                        "port", "3306",
                        "pass", "admin"
                ),
                "MariaDB", Map.of(
                        "port", "3307",
                        "pass", "admin"
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Name cannot be empty!");
            alert.showAndWait();
        } else if (PasswordText.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Password cannot be empty!");
            alert.showAndWait();
        } else if (!PortText.getText().matches("\\d+")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Port must be a integer!");
            alert.showAndWait();
        } else {
            mainController.setDataAboutNewDatabase(
                    DatabaseComboBox.getSelectionModel().getSelectedItem(),
                    PasswordText.getText(),
                    PortText.getText()
            );

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
