package com.upc.garlic.config;

import com.upc.garlic.registry.zk.util.CuratorUtils;
import com.upc.garlic.remoting.transport.netty.client.NettyRpcClient;
import com.upc.garlic.remoting.transport.netty.server.NettyRpcServer;
import com.upc.garlic.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * 服务器关闭的时候，释放所有资源，比如unregister所有服务器
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("shut down and clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                InetSocketAddress inetSocketAddress =
                        new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            ThreadPoolFactoryUtil.shutDownAllThreadPool();
        }));
    }

}
