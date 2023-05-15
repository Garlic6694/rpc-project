package com.upc.garlic.provider;

import com.upc.garlic.config.RpcServiceConfig;

/**
 * 储存和提供服务对象
 */
public interface ServiceProvider {
    void addService(RpcServiceConfig rpcServiceConfig);

    Object getService(String rpcServiceName);

    void publishService(RpcServiceConfig rpcServiceConfig);
}
