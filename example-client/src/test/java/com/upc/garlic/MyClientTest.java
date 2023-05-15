package com.upc.garlic;

import com.upc.garlic.enums.CompressTypeEnum;
import com.upc.garlic.enums.SerializationTypeEnum;
import com.upc.garlic.remoting.constants.RpcConstants;
import com.upc.garlic.remoting.dto.RpcMessage;
import com.upc.garlic.remoting.dto.RpcRequest;

import java.util.UUID;

import static com.upc.garlic.utils.PrintUtil.print;
import static org.junit.jupiter.api.Assertions.*;

class MyClientTest {
    public static void main(String[] args) {
        RpcRequest rpcRequest = RpcRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .interfaceName("MyAddMethod")
                .methodName("add")
                .parameters(new Object[]{1, 2})
                .paramTypes(new Class[]{Integer.class, Integer.class})
                .version("version1.0")
                .group("calculation")
                .build();
        //com.upc.garlic.hello.MyAddMethodcalculationversion1.0
        print(rpcRequest.getRpcServiceName());
        RpcMessage rpcMessage = RpcMessage.builder()
                .data(rpcRequest)
                .codec(SerializationTypeEnum.KRYO.getCode())
                .compress(CompressTypeEnum.GZIP.getCode())
                .messageType(RpcConstants.REQUEST_TYPE)
                .build();
        String compressMethod = CompressTypeEnum.getMethod(rpcMessage.getCompress());
        print(compressMethod);

    }

}