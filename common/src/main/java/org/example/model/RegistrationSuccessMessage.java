package org.example.model;

public class RegistrationSuccessMessage implements CloudMessage{
    @Override
    public MessageType getType() {
        return MessageType.REG_OK;
    }
}
