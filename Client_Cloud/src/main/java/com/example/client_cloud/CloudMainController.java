package com.example.client_cloud;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.example.DaemonThreadFactory;
import org.example.model.*;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class CloudMainController implements Initializable {


    private Network<ObjectDecoderInputStream, ObjectEncoderOutputStream> network;
    private Socket socket;
    private boolean needReadMessages = true;
    private boolean needAuthMessage = true;
    private DaemonThreadFactory factory;


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
    private String currentDirectory;

    public void downloadFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        network.getOutputStream().writeObject(new FileRequest(fileName));
    }

    public void sendToServer(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        network.getOutputStream().writeObject(new FileMessage(Path.of(currentDirectory).resolve(fileName)));
    }

    private void auth() {
        Start_Window.setVisible(true);
        try {
            while (needAuthMessage) {
                CloudMessage cloudMessage = (CloudMessage) network.getInputStream().readObject();
                if (cloudMessage instanceof AuthOk ao) {
                    needAuthMessage = false;
                    logIn.setVisible(false);
                } else if (cloudMessage instanceof ErrorMessage em) {
                    Platform.runLater(() -> showError(em.getErrorMessage()));
                } else if (cloudMessage instanceof RegistrationSuccessMessage rsm) {
                    SignUp.setVisible(false);
                    Start_Window.setVisible(true);
                }
            }
        } catch (Exception ignored) {}
        Start_Window.setVisible(false);
    }

    private void readMessages() {
        main_pane.setVisible(true);
        try {
            while (needReadMessages) {
                CloudMessage cloudMessage = (CloudMessage) network.getInputStream().readObject();
                if (cloudMessage instanceof FileMessage fileMessage) {
                    Files.write(Path.of(currentDirectory).resolve(fileMessage.getFileName()), fileMessage.getBytes());
                    Platform.runLater(() -> fillView(clientView, getFiles(currentDirectory)));
                } else if (cloudMessage instanceof ListMessage listMessage) {
                    Platform.runLater(() -> fillView(serverView, listMessage.getFiles()));
                }
            }
        } catch (Exception e) {
            System.err.println("Server off");
            e.printStackTrace();
        }
        main_pane.setVisible(false);
    }

    private void initNetwork() {
        try {
            socket = new Socket("localhost", 8189);
            network = new Network<>(
                    new ObjectDecoderInputStream(socket.getInputStream()),
                    new ObjectEncoderOutputStream(socket.getOutputStream()));
            factory.getThread(() -> {
                        CloudMainController.this.auth();
                        CloudMainController.this.readMessages();
                    }, "cloud-client-read-thread")
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        factory = new DaemonThreadFactory();
        initNetwork();
        contextMenuInitialize();
        setCurrentDirectory(System.getProperty("user.home"));
        fillView(clientView, getFiles(currentDirectory));
        clientViewSetActions();
        serverViewSetActions();
    }

    private void setCurrentDirectory(String directory) {
        currentDirectory = directory;
        fillView(clientView, getFiles(currentDirectory));
    }

    private void fillView(ListView<String> view, List<String> data) {
        view.getItems().clear();
        view.getItems().addAll(data);
    }

    private List<String> getFiles(String directory) {
        File dir = new File(directory);
        if (dir.isDirectory()) {
            String[] list = dir.list();
            if (list != null) {
                List<String> files = new ArrayList<>(Arrays.asList(list));
                files.add(0, "..");
                return files;
            }
        }
        return List.of();
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
                File selectedFile = new File(currentDirectory + "/" + selected);
                if (selectedFile.isDirectory()) {
                    setCurrentDirectory(currentDirectory + "/" + selected);
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
                        network.getOutputStream().writeObject(new DirectoryRequest(selected));
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
                    network.getOutputStream().writeObject(new DirectoryRequest(dirName));
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
            network.getOutputStream().writeObject(new FileDelete(fileName));
        } else {
            fileName = clientView.getSelectionModel().getSelectedItem();
            if (fileName != null) {
                Path path = Path.of(currentDirectory + "/" + fileName);
                if (Files.exists(path) && !Files.isDirectory(path)) {
                    Files.delete(path);
                    fillView(clientView, getFiles(currentDirectory));
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
                        network.getOutputStream().writeObject(new FileRename(fileNewName, a));
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
                File oldFile = new File(currentDirectory + "/" + fileOldName);
                if (!Files.isDirectory(oldFile.toPath())) {
                    DirectoryField.setVisible(true);
                    CreateDir.setPromptText("ENTER NEW FILE NAME");
                    CreateDir.setOnKeyPressed(keyEvent -> {
                        if (keyEvent.getCode() == KeyCode.ENTER) {
                            String fileNewName = CreateDir.getText();
                            File newFile = new File(currentDirectory + "/" + fileNewName);
                            oldFile.renameTo(newFile);
                            DirectoryField.setVisible(false);
                            CreateDir.setText("");
                        }
                    });
                    fillView(clientView, getFiles(currentDirectory));
                }
            }
        }
    }

    public void showError(String error) {
        final Alert alert = new Alert(Alert.AlertType.ERROR, error, new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        alert.setTitle("Ошибка!");
        alert.showAndWait();
    }

    public void sign_in(ActionEvent actionEvent) {
        Start_Window.setVisible(false);
        logIn.setVisible(true);
    }

    public void sign_up(ActionEvent actionEvent) {
        Start_Window.setVisible(false);
        SignUp.setVisible(true);
    }

    public void log_in(ActionEvent actionEvent) throws IOException {
        network.getOutputStream().writeObject(new AuthMessage(login_in.getText(), password_in.getText()));
    }

    public void signUP(ActionEvent actionEvent) throws IOException {
        network.getOutputStream().writeObject(new RegistrationMessage(signUP_field_login.getText(), signUP_field_password.getText()));
    }
}