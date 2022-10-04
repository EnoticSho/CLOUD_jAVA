package org.example.model;

import lombok.Getter;

@Getter
public class FileDelete implements CloudMessage {

    private final String fileName;

    public FileDelete(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public MessageType getType() {
        return MessageType.FILE_DELETE;
    }
}
