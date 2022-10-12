package com.example.client_cloud;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import org.example.model.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

public class CloudMainController implements Initializable{
    public Label label;
    public Pane DirectoryField;
    private Client client;
    public ListView<String> clientView;
    public ListView<String> serverView;
    public TextField CreateDir;

    private ContextMenu cm;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> setCurrentDirectory(System.getProperty("user.home")));
        contextMenuInitialize();
        clientViewSetActions();
        serverViewSetActions();
    }

    public void downloadFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        client.getNetwork().getOutputStream().writeObject(new FileRequest(fileName));
    }

    public void sendToServer(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        client.getNetwork().getOutputStream().writeObject(new FileMessage(Path.of(client.getCurrentDirectory()).resolve(fileName)));
    }

    public void setCurrentDirectory(String directory) {
        client.setCurrentDirectory(directory);
        fillView(clientView, client.getFiles(client.getCurrentDirectory()));
    }

    public void fillView(ListView<String> view, List<String> data) {
        view.getItems().clear();
        view.getItems().addAll(data);
    }

    private void contextMenuInitialize() {
        cm = new ContextMenu();
        MenuItem menuItemRen = new MenuItem("Rename a file");
        MenuItem menuItemDel = new MenuItem("Delete a file");
        cm.getItems().add(menuItemRen);
        cm.getItems().add(menuItemDel);

        menuItemRen.setOnAction(actionEvent -> {
            renameAFile();
        });

        menuItemDel.setOnAction(actionEvent -> {
            try {
                deleteAFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void clientViewSetActions() {
        clientView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                serverView.getSelectionModel().clearSelection();
            }
            if (event.getClickCount() == 2) {
                String selected = clientView.getSelectionModel().getSelectedItem();
                File selectedFile = new File(client.getCurrentDirectory() + "/" + selected);
                if (selectedFile.isDirectory()) {
                    setCurrentDirectory(client.getCurrentDirectory() + "/" + selected);
                }
            }
            if (event.getButton() == MouseButton.SECONDARY) {
                String selected = clientView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    cm.show(clientView, event.getScreenX(), event.getScreenY());
                }
            }
        });
    }

    private void serverViewSetActions() {
        serverView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                clientView.getSelectionModel().clearSelection();
            }
            if (event.getClickCount() == 2) {
                String selected = serverView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    try {
                        client.getNetwork().getOutputStream().writeObject(new DirectoryRequest(selected));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (event.getButton() == MouseButton.SECONDARY) {
                String selected = serverView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    cm.show(serverView, event.getScreenX(), event.getScreenY());
                }
            }
        });
    }

    public void openTextField(ActionEvent actionEvent) {
        DirectoryField.setVisible(true);
        label.setText("ENTER DIRECTORY NAME");
        CreateDir.setPromptText("ENTER DIRECTORY NAME");
            CreateDir.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    String dirName = CreateDir.getText();
                    try {
                        client.getNetwork().getOutputStream().writeObject(new DirectoryRequest(dirName));
                        DirectoryField.setVisible(false);
                        CreateDir.setText("");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
    }

    public void deleteFile(ActionEvent actionEvent) throws IOException {
        deleteAFile();
    }

    public void renameFile(ActionEvent actionEvent) {
        renameAFile();
    }

    private void deleteAFile() throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        if (fileName != null) {
            client.getNetwork().getOutputStream().writeObject(new FileDelete(fileName));
        } else {
            fileName = clientView.getSelectionModel().getSelectedItem();
            if (fileName != null) {
                Path path = Path.of(client.getCurrentDirectory() + "/" + fileName);
                if (Files.exists(path) && !Files.isDirectory(path)) {
                    Files.delete(path);
                    fillView(clientView, client.getFiles(client.getCurrentDirectory()));
                }
            }
        }
    }

    private void renameAFile() {
        String fileOldName = serverView.getSelectionModel().getSelectedItem();
        if (fileOldName != null) {
            DirectoryField.setVisible(true);
            label.setText("ENTER NEW FILE NAME");
            CreateDir.setPromptText("ENTER NEW FILE NAME");
            final String a = fileOldName;
            CreateDir.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    String fileNewName = CreateDir.getText();
                    try {
                        client.getNetwork().getOutputStream().writeObject(new FileRename(fileNewName, a));
                        DirectoryField.setVisible(false);
                        CreateDir.setText("");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            fileOldName = clientView.getSelectionModel().getSelectedItem();
            if (fileOldName != null) {
                File oldFile = new File(client.getCurrentDirectory() + "/" + fileOldName);
                if (!Files.isDirectory(oldFile.toPath())) {
                    DirectoryField.setVisible(true);
                    label.setText("ENTER NEW FILE NAME");
                    CreateDir.setPromptText("ENTER NEW FILE NAME");
                    CreateDir.setOnKeyPressed(keyEvent -> {
                        if (keyEvent.getCode() == KeyCode.ENTER) {
                            String fileNewName = CreateDir.getText();
                            File newFile = new File(client.getCurrentDirectory() + "/" + fileNewName);
                            oldFile.renameTo(newFile);
                            DirectoryField.setVisible(false);
                            CreateDir.setText("");
                        }
                    });
                    fillView(clientView, client.getFiles(client.getCurrentDirectory()));
                }
            }
        }
    }

    public void setClient(Client client) {
        this.client = client;
    }
}