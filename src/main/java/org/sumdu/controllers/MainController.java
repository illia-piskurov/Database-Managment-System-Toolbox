package org.sumdu.controllers;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    public Button NewButton;
    public ListView DatabasesView;

    private String currName = "";
    private String currPass = "";
    private String currPort = "";

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

    public void setDataAboutNewDatabase(String name, String pass, String port) {
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
}
