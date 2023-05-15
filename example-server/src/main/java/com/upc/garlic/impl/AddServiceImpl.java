package com.upc.garlic.impl;

import com.upc.garlic.annotation.RpcService;
import com.upc.garlic.hello.AddService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@RpcService(version = "2.0", group = "add")
public class AddServiceImpl implements AddService {
    @Override
    public Integer add(Integer a, Integer b) {
        return a + b;
    }

    @Override
    public Integer add3(Integer a, Integer b, Integer c) {
        return a + b + c;
    }
}
