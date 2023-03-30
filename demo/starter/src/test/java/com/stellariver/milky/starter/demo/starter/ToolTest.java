package com.stellariver.milky.starter.demo.starter;


import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.NettyRuntime;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;

public class ToolTest {

    public static void main(String[] args) {
    }

    static class Server {
        public static void main(String[] args) {
            EventLoopGroup group = new DefaultEventLoopGroup();
            new ServerBootstrap()
                    .group(new NioEventLoopGroup())
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(group, new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    ByteBuf byteBuf = (ByteBuf) msg;
                                    System.out.println(Thread.currentThread() + " " + byteBuf.toString(Charset.defaultCharset()));
                                    ctx.fireChannelRead(msg);
                                }
                            }).addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    ByteBuf byteBuf = (ByteBuf) msg;
                                    System.out.println(Thread.currentThread() + " " + byteBuf.toString(Charset.defaultCharset()));

                                }
                            });
                        }
                    }).bind(8080);
        }
    }

    static class Client {
        public static void main(String[] args) throws InterruptedException {
            Channel channel = new Bootstrap()
                    .group(new NioEventLoopGroup())
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringEncoder());
                        }
                    })
                    .connect(new InetSocketAddress(8080))
                    .sync()
                    .channel();
            channel.writeAndFlush("Hello!");
        }
    }


}