package com.upc.garlic.registry;

import com.upc.garlic.extension.SPI;
import com.upc.garlic.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * 服务发现
 */
@SPI
public interface ServiceDiscovery {
    /**
     * lookup service by rpcServiceName
     *
     * @param rpcRequest generate rpcServiceName
     * @return service address
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);

}
