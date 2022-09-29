package org.example.model;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ListMessage implements CloudMessage{

    public ListMessage(Path path) throws IOException {
        this.files = Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    private final List<String> files;

    @Override
    public MessageType getType() {
        return MessageType.LIST;
    }
}
