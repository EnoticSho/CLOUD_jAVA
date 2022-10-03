package org.example.model;

import lombok.Getter;

@Getter
public class AuthMessage implements CloudMessage{

    private final String login;
    private final String password;

    public AuthMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public MessageType getType() {
        return MessageType.AUTH;
    }
}
