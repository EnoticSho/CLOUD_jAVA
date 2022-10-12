package org.example.model;

import lombok.Getter;

@Getter
public class RegistrationSuccessMessage implements CloudMessage{

    private final String message = "Успешная регистрация";
    @Override
    public MessageType getType() {
        return MessageType.REG_OK;
    }
}
