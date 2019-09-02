package com.michaelwang.commonHandler;

import java.util.List;

import com.michaelwang.serialization.common.SerializeType;
import com.michaelwang.serialization.engine.SerializerEngine;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author jiuwang.wjw
 */
public class NettyDecoderHandler extends ByteToMessageDecoder {

    /**
     * 解码对象class
     */
    private Class<?> genericClass;
    /**
     * 解码对象编码所使用序列化类型
     */
    private SerializeType serializeType;

    public NettyDecoderHandler(Class<?> genericClass, SerializeType serializeType) {
        this.genericClass = genericClass;
        this.serializeType = serializeType;
    }

    /**
     * 将读入的ByteBuf中的数据解析成多个我们传入的类型的对象保存在out中，然后将其透传
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //获取消息头所标识的消息体字节数组长度
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        //若当前可以获取到的字节数小于实际长度,则直接返回,直到当前可以获取到的字节数等于实际长度
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        //读取完整的消息体字节数组
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        //将字节数组反序列化为java对象
        Object obj = SerializerEngine.deserialize(data, genericClass, serializeType);
        out.add(obj);
    }

}
