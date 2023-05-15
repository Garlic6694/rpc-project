package com.upc.garlic;

import com.upc.garlic.annotation.RpcReference;
import com.upc.garlic.annotation.RpcScan;
import com.upc.garlic.config.RpcServiceConfig;
import com.upc.garlic.hello.AddService;
import com.upc.garlic.hello.MyAddMethod;
import com.upc.garlic.proxy.RpcClientProxy;
import com.upc.garlic.remoting.transport.RpcRequestTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
@RpcScan(basePackage = {"com.upc.garlic"})
public class MyClient {
//    @RpcReference(version = "2.0", group = "add")
//    private static AddService addService;

    @RpcReference(version = "2.0", group = "add")
    private static MyAddMethod myAddMethod;

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyClient.class);
        RpcRequestTransport rpcRequestTransport = (RpcRequestTransport) applicationContext.getBean("nettyRpcClient");
        RpcServiceConfig config = RpcServiceConfig.builder()
                .group("add")
                .version("2.0")
                .build();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport, config);
        AddService addService = rpcClientProxy.getProxy(AddService.class);

        Integer sum2 = myAddMethod.add(1, 2);
        System.out.println(sum2);

        Integer sum3 = addService.add3(1, 2, 3);
        System.out.println(sum3);

    }
}
