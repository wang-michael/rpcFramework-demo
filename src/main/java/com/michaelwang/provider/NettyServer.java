package com.michaelwang.provider;

import java.util.HashSet;
import java.util.Set;

import com.michaelwang.commonHandler.NettyDecoderHandler;
import com.michaelwang.commonHandler.NettyEncoderHandler;
import com.michaelwang.model.WmRequest;
import com.michaelwang.provider.handler.NettyServerReceiveMessageHandler;
import com.michaelwang.serialization.common.SerializeType;
import com.michaelwang.util.PropertyConfigeHelper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author jiuwang.wjw
 */
public class NettyServer {

    private static NettyServer nettyServer = new NettyServer();

    private Channel channel;
    /**
     * 服务端boss线程组
     */
    private EventLoopGroup bossGroup;
    /**
     * 服务端worker线程组
     */
    private EventLoopGroup workerGroup;
    private ServerBootstrap serverBootstrap;
    /**
     * 序列化类型配置信息
     */
    private SerializeType serializeType = PropertyConfigeHelper.getSerializeType();

    private Set<Integer> startedPort = new HashSet<>();

    /**
     * 启动Netty服务
     *
     * @param port
     */
    public void start(final int port) {
        if (startedPort.contains(port)) {
            return;
        }
        synchronized (NettyServer.class) {
            if (startedPort.contains(port)) {
                return;
            }
            if (bossGroup == null && workerGroup == null) {
                bossGroup = new NioEventLoopGroup();
                workerGroup = new NioEventLoopGroup();
                serverBootstrap = new ServerBootstrap();

                final NettyServerReceiveMessageHandler serverInvokeHandler = new NettyServerReceiveMessageHandler();

                serverBootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //注册解码器NettyDecoderHandler
                            ch.pipeline().addLast(new NettyDecoderHandler(WmRequest.class, serializeType));
                            //注册编码器NettyEncoderHandler
                            ch.pipeline().addLast(new NettyEncoderHandler(serializeType));
                            //注册服务端业务逻辑处理器NettyServerInvokeHandler
                            ch.pipeline().addLast(serverInvokeHandler);
                        }
                    });
            }

            try {
                channel = serverBootstrap.bind(port).sync().channel();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            startedPort.add(port);
        }
    }


    /**
     * 停止Netty服务
     */
    public void stop() {
        if (null == channel) {
            throw new RuntimeException("Netty Server Stoped");
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
    }


    private NettyServer() {
    }


    public static NettyServer singleton() {
        return nettyServer;
    }
}
