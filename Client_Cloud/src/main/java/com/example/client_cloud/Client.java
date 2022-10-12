package com.example.client_cloud;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import org.example.DaemonThreadFactory;
import org.example.model.*;

import java.io.File;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Client {
    private CloudMainController controller;
    private final AuthController authController;
    private Network<ObjectDecoderInputStream, ObjectEncoderOutputStream> network;
    private Socket socket;
    private boolean needReadMessages = true;
    private final DaemonThreadFactory factory;
    private String currentDirectory;


    public Client(AuthController authController) {
        this.authController = authController;
        factory = new DaemonThreadFactory();
        initNetwork();
    }

    private void auth() {
        try {
            while (true) {
                CloudMessage cloudMessage = (CloudMessage) network.getInputStream().readObject();
                if (cloudMessage instanceof AuthOk) {
                    Platform.runLater(() -> authController.switchScene("mainCloudView.fxml"));
                    break;
                } else if (cloudMessage instanceof ErrorMessage em) {
                    Platform.runLater(() -> authController.showError(em.getErrorMessage()));
                } else if (cloudMessage instanceof RegistrationSuccessMessage rs) {
                    Platform.runLater(() -> authController.showOk(rs.getMessage()));
                    Platform.runLater(() -> authController.switchScene("hello-view.fxml"));
                }
            }
        } catch (Exception ignored) {}
    }

    private void readMessages() {
        try {
            while (needReadMessages) {
                CloudMessage cloudMessage = (CloudMessage) network.getInputStream().readObject();
                if (cloudMessage instanceof FileMessage fileMessage) {
                    Files.write(Path.of(currentDirectory).resolve(fileMessage.getFileName()), fileMessage.getBytes());
                    Platform.runLater(() -> controller.fillView(controller.clientView, getFiles(currentDirectory)));
                } else if (cloudMessage instanceof ListMessage listMessage) {
                    Platform.runLater(() -> controller.fillView(controller.serverView, listMessage.getFiles()));
                }
            }
        } catch (Exception e) {
            System.err.println("Server off");
            e.printStackTrace();
        }
    }

    private void initNetwork() {
        try {
            socket = new Socket("localhost", 8189);
            network = new Network<>(
                    new ObjectDecoderInputStream(socket.getInputStream()),
                    new ObjectEncoderOutputStream(socket.getOutputStream()));
            factory.getThread(() -> {
                        Client.this.auth();
                        Client.this.readMessages();
                    }, "cloud-client-read-thread")
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Network<ObjectDecoderInputStream, ObjectEncoderOutputStream> getNetwork() {
        return network;
    }

    public List<String> getFiles(String directory) {
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

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public void setController(CloudMainController controller) {
        this.controller = controller;
    }
}
