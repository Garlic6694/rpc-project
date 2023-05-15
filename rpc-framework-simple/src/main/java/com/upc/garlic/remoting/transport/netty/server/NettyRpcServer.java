package com.upc.garlic.remoting.transport.netty.server;

import com.upc.garlic.config.CustomShutdownHook;
import com.upc.garlic.config.RpcServiceConfig;
import com.upc.garlic.factory.SingletonFactory;
import com.upc.garlic.provider.ServiceProvider;
import com.upc.garlic.provider.impl.ZkServiceProviderImpl;
import com.upc.garlic.remoting.transport.netty.client.NettyRpcClientHandler;
import com.upc.garlic.remoting.transport.netty.codec.RpcMessageDecoder;
import com.upc.garlic.remoting.transport.netty.codec.RpcMessageEncoder;
import com.upc.garlic.utils.RuntimeUtil;
import com.upc.garlic.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * 服务器接收客户端的信息，调用相应的方法，并且返回结果
 */
@Slf4j
@Component("nettyRpcServer")
public class NettyRpcServer {
    public static final int PORT = 9998;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);

    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    @SneakyThrows
    public void start() {
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus() * 2,
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group", false)
        );
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)//Nagle
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128)//握手完毕的请求队列大小
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline cp = socketChannel.pipeline();
                            cp.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            cp.addLast(new RpcMessageDecoder());
                            cp.addLast(new RpcMessageEncoder());
                            cp.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });
            ChannelFuture cf = bootstrap.bind(host, PORT).sync();
            cf.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("start server failed!");
        } finally {
            log.error("shutdown bossGroup & workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }
}
