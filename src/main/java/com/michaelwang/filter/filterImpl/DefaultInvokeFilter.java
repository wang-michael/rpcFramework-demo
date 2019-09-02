package com.michaelwang.filter.filterImpl;

import java.net.InetSocketAddress;

import com.michaelwang.filter.Filter;
import com.michaelwang.filter.FilterChain;
import com.michaelwang.invoker.InvokerResponseHolder;
import com.michaelwang.invoker.NettyChannelFactory;
import com.michaelwang.model.WmRequest;
import com.michaelwang.model.WmResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * @author jiuwang.wjw
 */
public class DefaultInvokeFilter implements Filter {

    @Override
    public void doFilter(WmRequest request, WmResponse response, FilterChain filterChain) {
        //System.out.println("#############invoke before#############");
        String serverIp = request.getProviderService().getServerIp();
        int serverPort = request.getProviderService().getServerPort();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(serverIp, serverPort);

        InvokerResponseHolder.initResponseData(request.getUniqueKey());
        //根据本地调用服务提供者地址获取对应的Netty通道channel队列
        Channel channel = NettyChannelFactory.channelFactoryInstance().acquire(inetSocketAddress);
        //若获取的channel通道已经不可用,则重新获取一个
        while (!channel.isOpen() || !channel.isActive() || !channel.isWritable()) {
            //若队列中没有可用的Channel,则重新注册一个Channel
            channel = NettyChannelFactory.channelFactoryInstance().registerChannel(inetSocketAddress);
        }
        //将本次调用的信息写入Netty通道,发起异步调用
        ChannelFuture channelFuture = channel.writeAndFlush(request);
        channelFuture.syncUninterruptibly();
        //从返回结果容器中获取返回结果,同时设置等待超时时间为invokeTimeout
        long invokeTimeout = request.getInvokeTimeout();
        WmResponse responseTemp = InvokerResponseHolder.getValue(request.getUniqueKey(), invokeTimeout);
        // 这里将RPC调用返回的数据拷贝到函数入参 response 当中，目的是为了在com.michaelwang.invoker.InvokerProxyBeanFactory.invoke
        // 中可以获取到返回值
        WmResponse.copy(responseTemp, response);
    }

    /**
     * 这个 filter 用来进行实际的请求调用，一定是 filterChain 中最后被调用的
     * 所以设置其优先级为最低
     * @return
     */
    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

}
