package org.example.model;

import lombok.Getter;

@Getter
public class FileRename implements CloudMessage{

    private final String newFileName;
    private final String oldFileName;

    public FileRename(String newFileName, String oldFileName) {
        this.newFileName = newFileName;
        this.oldFileName = oldFileName;
    }

    @Override
    public MessageType getType() {
        return MessageType.FILE_RENAME;
    }
}
