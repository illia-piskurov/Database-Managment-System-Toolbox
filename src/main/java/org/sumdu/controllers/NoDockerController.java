package org.sumdu.controllers;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;

public class NoDockerController {
    public TextField DockerWebsite;
    public Button CopyButton;

    public void onCopyClick(MouseEvent mouseEvent) {
        String text = DockerWebsite.getText();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }
}
