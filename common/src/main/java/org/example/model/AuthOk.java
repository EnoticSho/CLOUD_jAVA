package org.example.model;

public class AuthOk implements CloudMessage{
    @Override
    public MessageType getType() {
        return MessageType.AUTH_OK;
    }
}
