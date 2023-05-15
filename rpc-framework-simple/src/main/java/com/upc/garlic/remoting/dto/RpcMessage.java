package com.upc.garlic.remoting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 传输的数据和相关的识别信息
 * custom protocol decoder
 * <pre>
 *   0     1     2     3     4        5    6    7    8      9          10      11        12   13   14   15   16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+--------+----+----+----+----+
 *   |   magic   code        |version |     full length     |messageType| codec |compress|    RequestId      |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 *
 * @author Garlic
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcMessage {
    /*
     *  magic code,     bytes
     *  version,        byte
     *  full length,    int
     *  messageType,    byte
     *  codec,          byte
     *  compress,       byte
     *  RequestId,      int
     */

    /**
     * message 类型 请求？响应？心跳？
     */
    private byte messageType;

    /**
     * 序列化类型 serialization type
     */
    private byte codec;

    /**
     * 压缩类型
     */
    private byte compress;

    private int requestId;

    /**
     * rpc中的传输的数据
     */
    private Object data;
}
