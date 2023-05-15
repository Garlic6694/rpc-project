package com.upc.garlic.provider.impl;

import com.upc.garlic.config.RpcServiceConfig;
import com.upc.garlic.enums.RpcErrorMessageEnum;
import com.upc.garlic.enums.ServiceRegistryEnum;
import com.upc.garlic.exception.RpcException;
import com.upc.garlic.extension.ExtensionLoader;
import com.upc.garlic.provider.ServiceProvider;
import com.upc.garlic.registry.ServiceRegistry;
import com.upc.garlic.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * zookeeper 服务器提供
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    /**
     * <p>key: rpc service name 接口名+version+group </p>
     * <p>value: service object </p>
     */
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader
                .getExtensionLoader(ServiceRegistry.class)
                .getExtension(ServiceRegistryEnum.ZK.getName());
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            InetSocketAddress inetSocketAddress =
                    new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), inetSocketAddress);
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}
