package com.example;

import java.io.*;
import java.net.Socket;

public class FileHandler implements Runnable{

    private static final String SERVER_DIR = "server_files";

    private static final String SEND_FILE_COMMAND = "file";

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
                String command = dis.readUTF();
                if (command.equals(SEND_FILE_COMMAND)) {
                    String fileName = dis.readUTF();
                    long size = dis.readLong();
                    try (FileOutputStream outputStream = new FileOutputStream(SERVER_DIR + "/" + fileName)) {
                        for (int i = 0; i < (size + BATCH_SIZE - 1); i++) {
                            int read = dis.read(batch);
                            outputStream.write(batch, 0, read);
                        }
                    } catch (Exception ignored) {}
                }else {
                    System.out.println("Unknown command received: " + command);
                }
            }
        } catch (Exception ignored) {}
    }
}
