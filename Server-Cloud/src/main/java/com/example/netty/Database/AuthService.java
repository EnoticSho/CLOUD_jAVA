package com.example.netty.Database;

import java.io.Closeable;
import java.nio.file.Path;

public interface AuthService extends Closeable {

    Path getPathByLoginAndPassword(String login, String password);

    void run();

    void close();
}
