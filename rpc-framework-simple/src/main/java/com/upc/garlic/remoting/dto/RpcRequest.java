package com.upc.garlic.remoting.dto;

import lombok.*;

/**
 * Rpc 请求类
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
public class RpcRequest {
    private static final long serialVersionUID = 1905122041950251207L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }

}
