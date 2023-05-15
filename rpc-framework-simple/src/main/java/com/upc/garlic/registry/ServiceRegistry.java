package com.upc.garlic.registry;

import com.upc.garlic.extension.SPI;

import java.net.InetSocketAddress;

@SPI
public interface ServiceRegistry {
    /**
     * 注册服务
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
