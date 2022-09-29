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
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, CloudMessage cloudMessage) throws Exception {
        log.debug("Received: {}", cloudMessage.getType());
        if (cloudMessage instanceof FileMessage fm) {
            Files.write(currentServerDir.resolve(fm.getFileName()), fm.getBytes());
            channelHandlerContext.writeAndFlush(new ListMessage(currentServerDir));
        } else if (cloudMessage instanceof FileRequest fr) {
            channelHandlerContext.writeAndFlush(new FileMessage(currentServerDir.resolve(fr.getFilename())));
        } else if (cloudMessage instanceof DirectoryRequest dr) {
            Path path = Path.of(currentServerDir + "/" + dr.getDirectoryName()).normalize();
            if (!Files.isDirectory(path)) {
                Files.createDirectory(path);
                channelHandlerContext.writeAndFlush(new ListMessage(currentServerDir));
            } else if (Files.isDirectory(path)) {
                if (path.equals(serverDir)) {
                    channelHandlerContext.writeAndFlush(new ListMessage(path));
                }else {
                    channelHandlerContext.writeAndFlush(new ListMessage(path, ""));
                }
                currentServerDir = path;
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverDir = Path.of("server_files");
        currentServerDir = serverDir;
        ctx.writeAndFlush(new ListMessage(serverDir));
    }
}