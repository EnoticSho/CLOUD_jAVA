package org.example.model;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public class FileMessage implements CloudMessage{

    private final String fileName;
    private final long size;
    private final byte[] bytes;

    public FileMessage(Path path) throws IOException {
        fileName = path.getFileName().toString();
        bytes = Files.readAllBytes(path);
        size = bytes.length;
    }

    @Override
    public MessageType getType() {
        return MessageType.FILE;
    }
}
