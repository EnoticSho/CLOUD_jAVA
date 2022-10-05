package com.example.client_cloud;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.example.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CloudMainController {
    private final Client client;

    private Stage stage;
    private Scene scene;
    private Parent root;

    private ActionEvent lastEvent;


    public ListView<String> clientView;
    public ListView<String> serverView;
    @FXML
    public TextField CreateDir;
    public Pane Start_Window;
    public Pane logIn;
    public Pane SignUp;
    public TextField login_in;
    public TextField password_in;
    public AnchorPane main_pane;
    @FXML
    public TextField signUP_field_login;
    @FXML
    public TextField signUP_field_password;
    @FXML
    private AnchorPane DirectoryField;
    private ContextMenu cm;


    public CloudMainController() {
        client = new Client(this);
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

    public void mainCloudViewInitialize() {
        contextMenuInitialize();
        setCurrentDirectory(System.getProperty("user.home"));
        clientViewSetActions();
        serverViewSetActions();
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

    public void showError(String error) {
        final Alert alert = new Alert(Alert.AlertType.ERROR, error, new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        alert.setTitle("Ошибка!");
        alert.showAndWait();
    }

    public void sign_in(ActionEvent event) throws IOException {
        lastEvent = event;
        switchScene("authView.fxml");
    }

    public void sign_up(ActionEvent event) throws IOException {
        lastEvent = event;
        switchScene("regView.fxml");
    }

    public void log_in(ActionEvent event) throws IOException {
        client.getNetwork().getOutputStream().writeObject(new AuthMessage(login_in.getText(), password_in.getText()));
        lastEvent = event;
    }

    public void signUP(ActionEvent actionEvent) throws IOException {
        client.getNetwork().getOutputStream().writeObject(new RegistrationMessage(signUP_field_login.getText(), signUP_field_password.getText()));
        lastEvent = actionEvent;
    }

    public void switchScene(String viewName) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource(viewName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage = (Stage)((Node)lastEvent.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}