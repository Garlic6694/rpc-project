package com.upc.garlic.remoting.transport;

import com.upc.garlic.extension.SPI;
import com.upc.garlic.remoting.dto.RpcRequest;

/**
 * send RPC request and get RPC response
 */
@SPI
public interface RpcRequestTransport {
    /**
     * send RPC request and get RPC response
     *
     * @param rpcRequest data body;
     * @return response from server;
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
