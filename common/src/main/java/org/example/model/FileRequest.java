package org.example.model;

import lombok.Getter;

@Getter
public class FileRequest implements CloudMessage{

    private final String filename;

    public FileRequest(String filename) {
        this.filename = filename;
    }

    @Override
    public MessageType getType() {
        return MessageType.FILE_REQUEST;
    }
}
