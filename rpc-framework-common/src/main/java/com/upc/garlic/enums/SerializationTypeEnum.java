package com.upc.garlic.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum SerializationTypeEnum {
    KRYO((byte) 0x01, "kryo"),
    PROTOSTUFF((byte) 0x02, "protostuff"),
    HESSIAN((byte) 0x01, "hessian");

    private final byte code;
    private final String method;

    public static String getMethod(byte code) {
        for (SerializationTypeEnum c : SerializationTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.method;
            }
        }
        return null;
    }
}
