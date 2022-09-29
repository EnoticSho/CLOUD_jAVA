package org.example.model;

import lombok.Getter;

@Getter
public class DirectoryRequest implements CloudMessage{

    private final String directoryName;

    public DirectoryRequest(String directoryName) {
        this.directoryName = directoryName;
    }

    @Override
    public MessageType getType() {
        return MessageType.DIRECTORY_REQUEST;
    }
}
