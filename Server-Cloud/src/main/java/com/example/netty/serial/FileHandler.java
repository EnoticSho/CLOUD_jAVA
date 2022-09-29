package com.example.netty.serial;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.model.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path serverDir;
    private Path currentServerDir;

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
        currentServerDir = serverDir;
        ctx.writeAndFlush(new ListMessage(serverDir));
    }
}
