package com.upc.garlic.registry.zk;

import com.upc.garlic.enums.LoadBalanceEnum;
import com.upc.garlic.enums.RpcErrorMessageEnum;
import com.upc.garlic.exception.RpcException;
import com.upc.garlic.extension.ExtensionLoader;
import com.upc.garlic.loadbalance.LoadBalance;
import com.upc.garlic.registry.ServiceDiscovery;
import com.upc.garlic.registry.zk.util.CuratorUtils;
import com.upc.garlic.remoting.dto.RpcRequest;
import com.upc.garlic.utils.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl() {
        this.loadBalance = ExtensionLoader
                .getExtensionLoader(LoadBalance.class)
                .getExtension(LoadBalanceEnum.LOADBALANCE.getName());
    }

    /**
     * lookup service by rpcServiceName
     *
     * @param rpcRequest generate rpcServiceName
     * @return service address
     */
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildNodes(zkClient, rpcServiceName);
        if (CollectionUtil.isEmpty(serviceUrlList)) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("Successfully found the service address:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
