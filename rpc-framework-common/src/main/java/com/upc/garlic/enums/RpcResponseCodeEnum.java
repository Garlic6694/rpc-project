package com.upc.garlic.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {
    SUCCESS(200, "RPC success"),
    FAIL(500, "RPC fail");

    private final int code;
    private final String message;
}
