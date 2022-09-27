package org.example.model;

import java.io.Serializable;

public interface CloudMessage extends Serializable {
    MessageType getType();
}
