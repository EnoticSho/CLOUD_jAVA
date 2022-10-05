package com.example.netty.serial;

import com.example.netty.Database.DBAuthService;
import com.example.netty.Database.DBRegistrationService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.model.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

@Slf4j
public class FileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path serverDir;
    private Path currentServerDir;

    private final DBAuthService authService;

    public FileHandler(DBAuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        log.debug("Received: {}", cloudMessage.getType());
        if (cloudMessage instanceof FileMessage fm) {
            Files.write(currentServerDir.resolve(fm.getFileName()), fm.getBytes());
        } else if (cloudMessage instanceof FileRequest fr) {
            ctx.writeAndFlush(new FileMessage(currentServerDir.resolve(fr.getFilename())));
        } else if (cloudMessage instanceof DirectoryRequest dr) {
            Path path = Path.of(currentServerDir + "/" + dr.getDirectoryName()).normalize();
            if (!Files.isDirectory(path)) {
                Files.createDirectory(path);
            } else if (Files.isDirectory(path)) {
                currentServerDir = path;
            }
        } else if (cloudMessage instanceof FileDelete fd) {
            Path path = currentServerDir.resolve(fd.getFileName());
            if (Files.exists(path) && !Files.isDirectory(path)) {
                Files.delete(path);
            }
        } else if (cloudMessage instanceof FileRename fr) {
            Path path = currentServerDir.resolve(fr.getOldFileName());
            File oldFile = new File(path.toString());
            path = currentServerDir.resolve(fr.getNewFileName());
            File newFIle = new File(path.toString());
            if (Files.exists(oldFile.toPath()) && !Files.isDirectory(oldFile.toPath())) {
                oldFile.renameTo(newFIle);
            }
        } else if (cloudMessage instanceof AuthMessage am) {
            Path pathByLoginAndPassword = authService.getPathByLoginAndPassword(am.getLogin(), am.getPassword());
            if (pathByLoginAndPassword != null){
                serverDir = serverDir.resolve(pathByLoginAndPassword);
                currentServerDir = serverDir;
                ctx.writeAndFlush(new AuthOk());
            } else {
                ctx.writeAndFlush(new ErrorMessage("Неверный логин и пароль"));
            }
        } else if (cloudMessage instanceof RegistrationMessage rm) {
            try (DBRegistrationService dbRegistrationService = new DBRegistrationService()){
                dbRegistrationService.regUser(rm.getLogin(), rm.getPassword());
                ctx.writeAndFlush(new RegistrationSuccessMessage());
                if (!Files.exists(serverDir.resolve(rm.getLogin()))) {
                    Files.createDirectory(serverDir.resolve(rm.getLogin()));
                }
            } catch (SQLException e) {
                ctx.writeAndFlush(new ErrorMessage("Данный пользователь уже существует"));
            }
        }

        if (currentServerDir.equals(serverDir)) {
            ctx.writeAndFlush(new ListMessage(currentServerDir));
        }else {
            ctx.writeAndFlush(new ListMessage(currentServerDir, ""));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverDir = Path.of("server_files");
    }
}
