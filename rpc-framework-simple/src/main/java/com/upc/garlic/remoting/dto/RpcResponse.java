package com.upc.garlic.remoting.dto;

import com.upc.garlic.enums.RpcResponseCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rpc 响应类
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcResponse<T> {
    private static final long serialVersionUID = 715745410605631233L;
    private String requestId;

    /**
     * 响应码
     */
    private Integer code;

    private String message;

    /**
     * 响应 body
     */
    private T data;

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> rpcResponse = RpcResponse.<T>builder()
                .code(RpcResponseCodeEnum.SUCCESS.getCode())
                .message(RpcResponseCodeEnum.SUCCESS.getMessage())
                .requestId(requestId)
                .build();
        if (data != null) {
            rpcResponse.setData(data);
        }
        return rpcResponse;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum) {
        return RpcResponse.<T>builder()
                .code(rpcResponseCodeEnum.getCode())
                .message(rpcResponseCodeEnum.getMessage())
                .build();
    }

}
