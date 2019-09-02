package com.michaelwang.provider.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.*;

import com.alibaba.fastjson.JSON;

import avro.shaded.com.google.common.base.Predicate;
import avro.shaded.com.google.common.collect.Collections2;
import com.michaelwang.filter.Filter;
import com.michaelwang.filter.filterChainImpl.DefaultFilterChain;
import com.michaelwang.filter.filterImpl.DefaultInvokeFilter;
import com.michaelwang.filter.filterImpl.DefaultServerFilter;
import com.michaelwang.model.ProviderService;
import com.michaelwang.model.WmRequest;
import com.michaelwang.model.WmResponse;
import com.michaelwang.provider.BusinessThreadRunnable;
import com.michaelwang.register.IRegisterCenter4Provider;
import com.michaelwang.register.RegisterCenter;
import com.michaelwang.util.NamedThreadFactory;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jiuwang.wjw
 * 服务端消息接收处理器
 */
@Sharable
public class NettyServerReceiveMessageHandler extends SimpleChannelInboundHandler<WmRequest> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerReceiveMessageHandler.class);
    protected static final ExecutorService SHARED_EXECUTOR = new ThreadPoolExecutor(200, 200, 0, TimeUnit.SECONDS, new SynchronousQueue<>(), new NamedThreadFactory("WM-RpcDemo-Business-POOL"));

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 客户端链路关闭在这里可以察觉的到
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //System.out.println("exceptionCaught! ");
        //cause.printStackTrace();
        //发生异常,关闭链路
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WmRequest request) throws Exception {
        System.out.println("received message ! method: " + request.getInvokedMethodName() + " port: " + request.getProviderService().getServerPort());
        if (ctx.channel().isWritable()) {
            SHARED_EXECUTOR.execute(new BusinessThreadRunnable(ctx, request));
        } else {
            logger.error("------------channel closed!---------------");
        }
    }
}
