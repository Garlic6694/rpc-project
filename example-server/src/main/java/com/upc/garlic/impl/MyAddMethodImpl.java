package com.upc.garlic.impl;

import com.upc.garlic.annotation.RpcService;
import com.upc.garlic.hello.MyAddMethod;

@RpcService(version = "2.0", group = "add")
public class MyAddMethodImpl implements MyAddMethod {
    @Override
    public Integer add(Integer a, Integer b) {
        return a + b;
    }
}
