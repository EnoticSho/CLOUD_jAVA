package com.example;

import java.io.*;
import java.net.Socket;

public class FileHandler implements Runnable{

    private static final String SERVER_DIR = "server_files";

    private static final String SEND_FILE_COMMAND = "file-to-server";

    private static final String SEND_TO_CLIENT_FILE_COMMAND = "file-to-client";
    private static final String DOWNLOAD_FILE= "download_file";

    private static final Integer BATCH_SIZE = 256;

    private Socket socket;

    private final DataOutputStream dos;
    private final DataInputStream dis;

    private byte[] batch;

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
            while (true) {
                dos.writeUTF(SERVER_DIR);
                String command = dis.readUTF();
                System.out.println(command);
                if (command.equals(SEND_FILE_COMMAND)) {
                    String fileName = dis.readUTF();
                    long size = dis.readLong();
                    try (FileOutputStream outputStream = new FileOutputStream(SERVER_DIR + "/" + fileName)) {
                        for (int i = 0; i < (size + BATCH_SIZE - 1); i++) {
                            int read = dis.read(batch);
                            outputStream.write(batch, 0, read);
                        }
                        System.out.println("файл закачен на сервер + " + fileName);
                    } catch (Exception ignored) {}
                }else if (command.equals(SEND_TO_CLIENT_FILE_COMMAND)){
                    String fileName = dis.readUTF();
                    File file = new File(fileName);
                    System.out.println(file.getName());
                    if (file.isFile()) {
                        try {
                            dos.writeUTF(DOWNLOAD_FILE);
                            dos.writeLong(file.length());
                            try (FileInputStream fileInputStream = new FileInputStream(file)){
                                byte[] bytes = fileInputStream.readAllBytes();
                                dos.write(bytes);
                                System.out.println("файл отпрвлен клиенту " + fileName);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }catch (Exception e) {
                            System.err.println("e = " + e.getMessage());
                        }
                    }
                }else {
                    System.out.println("Unknown command received: " + command);
                }
            }
        } catch (Exception ignored) {}
    }
}
