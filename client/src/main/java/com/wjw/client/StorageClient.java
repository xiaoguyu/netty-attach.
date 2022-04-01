package com.wjw.client;

import com.wjw.client.handler.RequestSendHandler;
import com.wjw.client.handler.ResponseReceiveHandler;
import com.wjw.storage.StorageUploadFileRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author wjw
 * @description: 客户端测试类
 * @title: StorageClient
 * @date 2022/4/1 14:15
 */
public class StorageClient {

    private Channel channel;

    private String host;
    private int port;

    public StorageClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup worderGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(worderGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new RequestSendHandler(), new ResponseReceiveHandler());
                }
            });

            ChannelFuture f = b.connect(host, port).sync();
            channel = f.channel();
            write("C:\\Users\\wjw\\Desktop\\hotelQrCode.jpg");
            f.channel().closeFuture().sync();
        } finally {
            worderGroup.shutdownGracefully();
        }
    }

    public void write(String path) {
        File file = new File(path);
        try {
            FileInputStream fis = new FileInputStream(file);
            StorageUploadFileRequest request = new StorageUploadFileRequest(fis, "jpg", file.length(), false);
            channel.write(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        channel.flush();
    }

    public static void main(String[] args) throws Exception {
        StorageClient client = new StorageClient("127.0.0.1", 8088);
        client.run();
    }
}
