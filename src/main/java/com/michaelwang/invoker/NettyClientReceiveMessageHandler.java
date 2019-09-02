package com.michaelwang.invoker;

import com.michaelwang.invoker.InvokerResponseHolder;
import com.michaelwang.model.WmResponse;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author jiuwang.wjw
 */
@Sharable
public class NettyClientReceiveMessageHandler extends SimpleChannelInboundHandler<WmResponse> {

    public NettyClientReceiveMessageHandler() {
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WmResponse response) throws Exception {
        //将Netty异步返回的结果存入阻塞队列,以便调用端同步获取
        InvokerResponseHolder.putResultValue(response);
    }
}
