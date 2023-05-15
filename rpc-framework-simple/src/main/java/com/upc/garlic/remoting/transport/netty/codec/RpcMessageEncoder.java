package com.upc.garlic.remoting.transport.netty.codec;

import com.upc.garlic.compress.Compress;
import com.upc.garlic.enums.CompressTypeEnum;
import com.upc.garlic.enums.SerializationTypeEnum;
import com.upc.garlic.extension.ExtensionLoader;
import com.upc.garlic.remoting.constants.RpcConstants;
import com.upc.garlic.remoting.dto.RpcMessage;
import com.upc.garlic.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * custom protocol decoder
 * <p>
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
 * </pre>
 *
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) throws Exception {
        try {
            /*
             *  magic code,
             *  version,
             *  full length,
             *  messageType,
             *  codec,
             *  compress,
             *  RequestId
             */
//            log.info("need to encode rpcMessage : [{}]",rpcMessage);
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(RpcConstants.VERSION);
            out.writerIndex(out.writerIndex() + 4); //space for full length
            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);
            out.writeByte(rpcMessage.getCodec());
            out.writeByte(rpcMessage.getCompress());
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());

            //计算full length
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;
            //如果不是心跳信息，full length = head length + body length;
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE &&
                    messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                //先序列化再压缩
                //序列化方法
                String codecMethod = SerializationTypeEnum.getMethod(rpcMessage.getCodec());
                //通过SPI获取 serializer 实现类
                Serializer serializer = ExtensionLoader
                        .getExtensionLoader(Serializer.class)
                        .getExtension(codecMethod);
                bodyBytes = serializer.serialize(rpcMessage.getData());
                log.info("RPC Data has been serialized");

                //压缩方法
                String compressMethod = CompressTypeEnum.getMethod(rpcMessage.getCompress());
                Compress compress = ExtensionLoader
                        .getExtensionLoader(Compress.class)
                        .getExtension(compressMethod);
                bodyBytes = compress.compress(bodyBytes);
                log.info("RPC Data has been compressed");
                fullLength += bodyBytes.length;
            }
            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }
            int writerIndex = out.writerIndex();
            //定位full length 写入位置
            out.writerIndex(writerIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            log.info("full length is : [{}]", fullLength);
            out.writeInt(fullLength);
            //index body后
            out.writerIndex(writerIndex);
        } catch (Exception e) {
            log.error("try to encode request fail!", e);
        }
    }
}
