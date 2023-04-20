package com.stellariver.milky.demo;


import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Scanner;

public class SimpleNettyDemo {

    static class Server {

        public static void main(String[] args) {
            new ServerBootstrap()
                    .group(new NioEventLoopGroup())
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    ByteBuf byteBuf = (ByteBuf) msg;
                                    super.channelRead(ctx, byteBuf.toString(Charset.defaultCharset()));
                                    ch.writeAndFlush(byteBuf);
                                }
                            });
                        }
                    }).bind(8080);
        }




    }

    static class Client {
        public static void main(String[] args) throws InterruptedException {
            NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
            Channel channel = new Bootstrap()
                    .group(eventExecutors)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    System.out.println(msg);
                                    super.channelRead(ctx, msg);
                                }
                            });
                        }
                    })
                    .connect(new InetSocketAddress(8080))
                    .sync()
                    .channel();

            while (true) {
                Scanner scanner = new Scanner(System.in);
                String s = scanner.nextLine();
                if (Objects.equals(s, "q")) {
                    channel.close();
                    eventExecutors.shutdownGracefully();
                    break;
                } else {
                    channel.writeAndFlush(s);
                }
            }
        }



    }


}