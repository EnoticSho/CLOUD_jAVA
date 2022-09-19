package com.example.client_cloud;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class CloudMainController implements Initializable {

    @FXML
    private ListView<String> clientView;
    @FXML
    private ListView<String> serverView;

    private String currentDirectory;
    private String serverDirectory;

    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket socket;
    private final byte[] batch = new byte[256];

    private static final String SEND_FILE_COMMAND = "file-to-server";
    private static final String SEND_TO_CLIENT_FILE_COMMAND = "file-to-client";
    private static final String DOWNLOAD_FILE= "download_file";

    public void sendToServer(ActionEvent actionEvent) {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        String filePath = currentDirectory + "/" + fileName;
        File file = new File(filePath);
        if (file.isFile()) {
            try {
                dos.writeUTF(SEND_FILE_COMMAND);
                dos.writeUTF(fileName);
                dos.writeLong(file.length());
                try (FileInputStream fileInputStream = new FileInputStream(file)){
                    while (fileInputStream.available() > 0) {
                        int read = fileInputStream.read(batch);
                        dos.write(batch, 0 , read);
                    }
//                    byte[] bytes = fileInputStream.readAllBytes();
//                    dos.write(bytes);
                    System.out.println("Файл отправлен на сервер " + fileName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                fillView(serverView, getFiles(dis.readUTF()));
            }catch (Exception e) {
                System.err.println("e = " + e.getMessage());
            }
        }
    }

    public void sendToClient(ActionEvent actionEvent) {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        try {
            System.out.println("Получение файла с сервера " + fileName);
            dos.writeUTF(SEND_TO_CLIENT_FILE_COMMAND);
            dos.writeUTF(fileName);
            String command = dis.readUTF();
            if (command.equals(DOWNLOAD_FILE)) {
                long size = dis.readLong();
                try (FileOutputStream outputStream = new FileOutputStream(currentDirectory + "/" + fileName)) {
                    for (int i = 0; i < (size / batch.length) + 1; i++) {
                        int read = dis.read(batch);
                        outputStream.write(batch, 0, read);
                    }
                    System.out.println("Файл получен " + fileName);
                    fillView(clientView, getFiles(currentDirectory));
                } catch (Exception ignored) {
                }
            }
        }catch (Exception e) {
            System.err.println("e = " + e.getMessage());
        }
    }

    private void initNetwork() {
        try {
            socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (Exception ignored) {}
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initNetwork();
        try {
            serverDirectory = dis.readUTF();
            fillView(serverView, getFiles(serverDirectory));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setCurrentDirectory(System.getProperty("user.home"));
        clientView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = clientView.getSelectionModel().getSelectedItem();
                File selectedFile = new File(currentDirectory + "/" + selected);
                if (selectedFile.isDirectory()) {
                    setCurrentDirectory(currentDirectory + "/" + selected);
                }
            }
        });
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
}