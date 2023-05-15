package com.upc.garlic;

import com.upc.garlic.config.RpcServiceConfig;
import com.upc.garlic.hello.MyAddMethod;
import com.upc.garlic.impl.MyAddMethodImpl;
import com.upc.garlic.remoting.transport.netty.server.NettyRpcServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static com.upc.garlic.utils.PrintUtil.print;


class MyProviderTest {
    public static void main(String[] args) throws UnknownHostException {
        MyAddMethod myAddMethod = new MyAddMethodImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .version("version1.0")
                .group("calculation")
                .service(myAddMethod)
                .build();

        System.out.println(rpcServiceConfig.getRpcServiceName());
        InetSocketAddress inetSocketAddress =
                new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
        print(inetSocketAddress);
    }

}