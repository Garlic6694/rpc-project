package com.upc.garlic.remoting.transport.netty.codec;

import com.upc.garlic.compress.Compress;
import com.upc.garlic.enums.CompressTypeEnum;
import com.upc.garlic.enums.SerializationTypeEnum;
import com.upc.garlic.extension.ExtensionLoader;
import com.upc.garlic.remoting.constants.RpcConstants;
import com.upc.garlic.remoting.dto.RpcMessage;
import com.upc.garlic.remoting.dto.RpcRequest;
import com.upc.garlic.remoting.dto.RpcResponse;
import com.upc.garlic.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
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
 * </pre>
 * <p>
 * {@link LengthFieldBasedFrameDecoder} is a length-based decoder , used to solve TCP unpacking and sticking problems.
 * </p>
 *
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    public RpcMessageDecoder() {
        this(RpcConstants.MAX_FRAME_LENGTH,
                5,
                4,
                -9,
                0);
    }

    /**
     * @param maxFrameLength      Maximum frame length. It decide the maximum length of data that can be received.
     *                            If it exceeds, the data will be discarded.
     * @param lengthFieldOffset   偏移fl前面的几位
     * @param lengthFieldLength   full length的开头
     * @param lengthAdjustment    记为adj 再要读full length - adj ,也就是剩下的信息
     * @param initialBytesToStrip 所有的字节都需要，因此为0，不逃过任何字
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset,
                             int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength,
                lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("decode byteBuf error!", e);
                    throw e;
                }
            }
        }
        return decoded;
    }

    private Object decodeFrame(ByteBuf in) {
        //ByteBuf 必须按照顺序读
        checkMagicNumber(in);
        checkVersion(in);

        int fullLength = in.readInt();

        //构建RpcMessage
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType)
                .compress(compressType)
                .build();
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] body = new byte[bodyLength];
            in.readBytes(body);
            //解压缩
            String decompressMethod = CompressTypeEnum
                    .getMethod(rpcMessage.getCompress());
            Compress decompress = ExtensionLoader
                    .getExtensionLoader(Compress.class)
                    .getExtension(decompressMethod);
            body = decompress.decompress(body);
            log.info("decompress message by [{}]", decompressMethod);
            //反序列化
            String deserializerMethod = SerializationTypeEnum
                    .getMethod(rpcMessage.getCodec());
            Serializer serializer = ExtensionLoader
                    .getExtensionLoader(Serializer.class)
                    .getExtension(deserializerMethod);
            if (rpcMessage.getMessageType() == RpcConstants.REQUEST_TYPE) {
                RpcRequest tmpData = serializer.deserialize(body, RpcRequest.class);
                rpcMessage.setData(tmpData);
            } else if (rpcMessage.getMessageType() == RpcConstants.RESPONSE_TYPE) {
                RpcResponse<?> tmpData = serializer.deserialize(body, RpcResponse.class);
                rpcMessage.setData(tmpData);
            }
        }
        return rpcMessage;
    }

    private void checkMagicNumber(ByteBuf in) {
        int length = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmpBytes = new byte[length];
        in.readBytes(tmpBytes);
        for (int i = 0; i < length; i++) {
            if (tmpBytes[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmpBytes));
            }
        }
    }

    private void checkVersion(ByteBuf in) {
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version is not compatible " + version);
        }
    }


}
