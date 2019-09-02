package com.michaelwang.invoker;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import com.michaelwang.commonHandler.NettyDecoderHandler;
import com.michaelwang.commonHandler.NettyEncoderHandler;
import com.michaelwang.model.ProviderService;
import com.michaelwang.model.WmResponse;
import com.michaelwang.serialization.common.SerializeType;
import com.michaelwang.util.PropertyConfigeHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消费者获取相应服务提供者的Channel句柄的Factory
 *
 * 每个消费者与每个提供者之间目前建立的是唯一的连接，后期可扩展，比如可由用户自定义是否针对某个接口与服务提供者之间建立单独的连接等
 *
 * @author jiuwang.wjw
 */
public class NettyChannelFactory {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannelFactory.class);

    private static final NettyChannelFactory channelFactory = new NettyChannelFactory();

    /**
     * Key为服务提供者地址,value为Netty Channel阻塞队列
     */
    private static volatile Map<InetSocketAddress, Channel> channelMap = new ConcurrentHashMap<>();
    /**
     * 初始化序列化协议类型,该值为可配置信息
     */
    private static final SerializeType serializeType = PropertyConfigeHelper.getSerializeType();
    /**
     * 服务提供者列表
     */
    private static List<ProviderService> serviceMetaDataList = new ArrayList<>();

    private EventLoopGroup group = new NioEventLoopGroup();
    private Bootstrap bootstrap = new Bootstrap();

    private NettyChannelFactory() {
        final NettyClientReceiveMessageHandler nettyClientInvokeHandler = new NettyClientReceiveMessageHandler();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    //注册Netty编码器
                    ch.pipeline().addLast(new NettyEncoderHandler(serializeType));
                    //注册Netty解码器
                    ch.pipeline().addLast(new NettyDecoderHandler(WmResponse.class, serializeType));
                    //注册客户端业务逻辑处理handler
                    ch.pipeline().addLast(nettyClientInvokeHandler);
                }
            });
    }

    /**
     * 初始化Netty channel 连接队列Map
     *
     * @param providerMap
     */
    public void initChannelPoolFactory(Map<String, List<ProviderService>> providerMap) {
        Map<InetSocketAddress, Channel> channelPoolMapTemp = new ConcurrentHashMap<>();
        //将服务提供者信息存入serviceMetaDataList列表
        Collection<List<ProviderService>> collectionServiceMetaDataList = providerMap.values();
        for (List<ProviderService> serviceMetaDataModels : collectionServiceMetaDataList) {
            if (CollectionUtils.isEmpty(serviceMetaDataModels)) {
                continue;
            }
            serviceMetaDataList.addAll(serviceMetaDataModels);
        }

        //获取服务提供者地址列表
        Set<InetSocketAddress> socketAddressSet = new HashSet<>();
        for (ProviderService serviceMetaData : serviceMetaDataList) {
            String serviceIp = serviceMetaData.getServerIp();
            int servicePort = serviceMetaData.getServerPort();

            InetSocketAddress socketAddress = new InetSocketAddress(serviceIp, servicePort);
            socketAddressSet.add(socketAddress);
        }

        //根据服务提供者地址列表初始化Channel阻塞队列,并以地址为Key,地址对应的Channel阻塞队列为value,存入channelPoolMapTemp
        for (InetSocketAddress socketAddress : socketAddressSet) {
            Channel channel = registerChannel(socketAddress);
            channelPoolMapTemp.put(socketAddress, channel);
        }
        channelMap = channelPoolMapTemp;
    }

    /**
     * 根据服务提供者地址获取对应的Netty Channel阻塞队列
     *
     * @param socketAddress
     * @return
     */
    public Channel acquire(InetSocketAddress socketAddress) {
        return channelMap.get(socketAddress);
    }

    /**
     * 为服务提供者地址socketAddress注册新的Channel
     *
     * @param socketAddress
     * @return
     */
    public Channel registerChannel(InetSocketAddress socketAddress) {
        try {
            ChannelFuture channelFuture = bootstrap.connect(socketAddress).sync();
            final Channel newChannel = channelFuture.channel();
            final CountDownLatch connectedLatch = new CountDownLatch(1);

            final List<Boolean> isSuccessHolder = new ArrayList<>(1);
            //监听Channel是否建立成功
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    //若Channel建立成功,保存建立成功的标记
                    if (future.isSuccess()) {
                        isSuccessHolder.add(Boolean.TRUE);
                    } else {
                        //若Channel建立失败,保存建立失败的标记
                        future.cause().printStackTrace();
                        isSuccessHolder.add(Boolean.FALSE);
                    }
                    connectedLatch.countDown();
                }
            });

            connectedLatch.await();
            //如果Channel建立成功,返回新建的Channel
            if (isSuccessHolder.get(0)) {
                channelMap.put(socketAddress, newChannel);
                return newChannel;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public static NettyChannelFactory channelFactoryInstance() {
        return channelFactory;
    }
}
