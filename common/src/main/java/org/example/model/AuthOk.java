package org.example.model;

import lombok.Getter;

@Getter
public class AuthOk implements CloudMessage{

    private final String authOk = "Успешная авторизация";

    @Override
    public MessageType getType() {
        return MessageType.AUTH_OK;
    }
}
