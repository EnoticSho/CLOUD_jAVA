package com.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileHandler implements Runnable {

    private static final String SERVER_DIR = "server_files";
    private static final String SEND_FILE_COMMAND = "file-to-server";
    private static final String SEND_TO_CLIENT_FILE_COMMAND = "file-to-client";
    private static final String DOWNLOAD_FILE = "download_file";

    private static final Integer BATCH_SIZE = 256;
    private byte[] batch;

    private List<String> listOfFiles;

    private Socket socket;

    private final DataOutputStream dos;
    private final DataInputStream dis;

    public FileHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.dos = new DataOutputStream(socket.getOutputStream());
        this.dis = new DataInputStream(socket.getInputStream());
        batch = new byte[BATCH_SIZE];
        File file = new File(SERVER_DIR);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    @Override
    public void run() {
        System.out.println("Start Listening...");
        try {
            directoryFiles();
            while (true) {
                String command = dis.readUTF();
                System.out.println(command);
                if (command.equals(SEND_FILE_COMMAND)) {
                    System.out.println("Загрузка файла на сервер");
                    String fileName = dis.readUTF();
                    long size = dis.readLong();
                    try (FileOutputStream outputStream = new FileOutputStream(SERVER_DIR + "/" + fileName)) {
                        for (int i = 0; i < (size / BATCH_SIZE) + 1; i++) {
                            int read = dis.read(batch);
                            outputStream.write(batch, 0, read);
                        }
                    } catch (Exception ignored) {}
                    directoryFiles();
                    System.out.println("файл закачен на сервер " + fileName);
                } else if (command.equals(SEND_TO_CLIENT_FILE_COMMAND)) {
                    System.out.println("Отправка файла клиенту");
                    String fileName = dis.readUTF();
                    File serverFile = new File(SERVER_DIR + "/" + fileName);
                    if (serverFile.isFile()) {
                        try {
                            dos.writeUTF(DOWNLOAD_FILE);
                            dos.writeLong(serverFile.length());
                            try (FileInputStream fileInputStream = new FileInputStream(serverFile)) {
                                byte[] bytes = fileInputStream.readAllBytes();
                                dos.write(bytes);
                                System.out.println("файл отправлен клиенту " + fileName);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } catch (Exception e) {
                            System.err.println("e = " + e.getMessage());
                        }
                    }
                } else {
                    System.out.println("Unknown command received: " + command);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void directoryFiles() throws IOException {
        File dir = new File(SERVER_DIR);
        if (dir.isDirectory()) {
            listOfFiles = List.of(dir.list());
            dos.writeLong(listOfFiles.size());
            if (listOfFiles != null) {
                for (int i = 0; i < listOfFiles.size(); i++) {
                    dos.writeUTF(listOfFiles.get(i));
                }
            }
        }
    }
}