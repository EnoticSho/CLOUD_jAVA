package com.example.netty.serial;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.model.CloudMessage;
import org.example.model.FileMessage;
import org.example.model.FileRequest;
import org.example.model.ListMessage;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path serverDir;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, CloudMessage cloudMessage) throws Exception {
        log.debug("Received: {}", cloudMessage.getType());
        if (cloudMessage instanceof FileMessage fm) {
            Files.write(serverDir.resolve(fm.getFileName()), fm.getBytes());
            channelHandlerContext.writeAndFlush(new ListMessage(serverDir));
        } else if (cloudMessage instanceof FileRequest fr) {
            channelHandlerContext.writeAndFlush(new FileMessage(serverDir.resolve(fr.getFilename())));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverDir = Path.of("server_files");
        ctx.writeAndFlush(new ListMessage(serverDir));
    }
}
