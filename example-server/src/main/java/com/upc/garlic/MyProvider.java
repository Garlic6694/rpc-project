package com.upc.garlic;

import com.upc.garlic.annotation.RpcScan;
import com.upc.garlic.config.RpcServiceConfig;
import com.upc.garlic.hello.AddService;
import com.upc.garlic.impl.AddServiceImpl;
import com.upc.garlic.remoting.transport.netty.server.NettyRpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RpcScan(basePackage = {"com.upc.garlic"})
public class MyProvider {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyProvider.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");

        AddService myAddMethod = new AddServiceImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .version("2.0")
                .group("add")
                .service(myAddMethod)
                .build();
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}
