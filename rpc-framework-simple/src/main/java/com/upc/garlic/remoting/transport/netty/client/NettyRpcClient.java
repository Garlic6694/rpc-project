package com.upc.garlic.remoting.transport.netty.client;

import com.upc.garlic.enums.CompressTypeEnum;
import com.upc.garlic.enums.SerializationTypeEnum;
import com.upc.garlic.factory.SingletonFactory;
import com.upc.garlic.provider.impl.ChannelProviderImpl;
import com.upc.garlic.registry.ServiceDiscovery;
import com.upc.garlic.registry.zk.ZkServiceDiscoveryImpl;
import com.upc.garlic.remoting.constants.RpcConstants;
import com.upc.garlic.remoting.dto.RpcMessage;
import com.upc.garlic.remoting.dto.RpcRequest;
import com.upc.garlic.remoting.dto.RpcResponse;
import com.upc.garlic.remoting.transport.RpcRequestTransport;
import com.upc.garlic.remoting.transport.netty.codec.RpcMessageDecoder;
import com.upc.garlic.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * initialize and close Bootstrap object
 */
@Slf4j
@Component
public class NettyRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProviderImpl channelProvider;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient() {
        // init resources such as EventLoopGroup, Bootstrap
        this.eventLoopGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                // 连接等待时间，超时失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        ChannelPipeline cp = sc.pipeline();
                        cp.addLast(new IdleStateHandler(0, 10, 0, TimeUnit.SECONDS));// 心跳
                        cp.addLast(new RpcMessageEncoder());
                        cp.addLast(new RpcMessageDecoder());
                        cp.addLast(new NettyRpcClientHandler());
                    }
                });
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.serviceDiscovery = SingletonFactory.getInstance(ZkServiceDiscoveryImpl.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProviderImpl.class);
    }


    /**
     * send RPC request and get RPC response
     *
     * @param rpcRequest data body;
     * @return response from server;
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        //返回值future
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        //服务器地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        //返回服务器相关channel
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            //request放入unprocessedMap
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder()
                    .data(rpcRequest)
                    .codec(SerializationTypeEnum.KRYO.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE)
                    .build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message : [{}]", rpcMessage);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("send failed : ", future.cause());
                }
            });
        } else {
            throw new IllegalStateException("channel is not active");
        }
        return resultFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    /**
     * @param inetSocketAddress hostname-ip-port
     * @return channel
     */
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        try {
            CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
            bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("The client has connected [{} successful]", inetSocketAddress.toString());
                    completableFuture.complete(future.channel());
                } else {
                    throw new IllegalStateException("future is not success");
                }
            });
            return completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("connect to service error");
            throw new RuntimeException(e);
        }
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
        bootstrap.clone();
    }
}
