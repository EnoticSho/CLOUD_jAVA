package org.example;

import java.io.*;

public class FileUtils {

    private final static int BATCH_SIZE = 256;


    public static void readFileFromStream(DataInputStream dis, String dstDirectory) throws IOException {
        byte[] batch = new byte[BATCH_SIZE];
        String fileName = dis.readUTF();
        long size = dis.readLong();
        try (
                FileOutputStream outputStream = new FileOutputStream(dstDirectory + "/" + fileName)) {
            for (int i = 0; i < (size / BATCH_SIZE) + 1; i++) {
                int read = dis.read(batch);
                outputStream.write(batch, 0, read);
            }
        } catch (Exception ignored) {
        }
    }

    public static void writeFileToStream(DataOutputStream dos, String fileName, String filePath) throws IOException {
        byte[] batch = new byte[BATCH_SIZE];
        dos.writeUTF(fileName);
        dos.writeLong(fileName.length());
        try (FileInputStream fileInputStream = new FileInputStream(filePath)){
            while (fileInputStream.available() > 0) {
                int read = fileInputStream.read(batch);
                dos.write(batch, 0 , read);
            }
            System.out.println("Файл отправлен на сервер " + fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
