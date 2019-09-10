package com.michaelwang.invoker;

import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

import com.michaelwang.model.InvokerService;
import com.michaelwang.model.ProviderService;
import com.michaelwang.register.IRegisterCenter4Invoker;
import com.michaelwang.register.RegisterCenter;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author jiuwang.wjw
 */
public class InvokerFactoryBean implements FactoryBean, InitializingBean {

    /**
     * 服务接口
     */
    private Class<?> targetInterface;
    /**
     * 超时时间
     */
    private int timeout;
    /**
     * 服务bean
     */
    private Object serviceObject;
    /**
     * 负载均衡策略
     */
    private String clusterStrategy;
    /**
     * 服务提供者唯一标识
     */
    private String remoteAppKey;
    /**
     * 服务分组组名
     */
    private String groupName = "default";

    @Override
    public Object getObject() throws Exception {
        return serviceObject;
    }

    @Override
    public Class<?> getObjectType() {
        return targetInterface;
    }


    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("InvokerFactoryBean: " + this);
        //获取服务注册中心
        IRegisterCenter4Invoker registerCenter4Consumer = RegisterCenter.singleton();
        if (!registerCenter4Consumer.isInit()) {
            synchronized (registerCenter4Consumer) {
                if (!registerCenter4Consumer.isInit()) {
                    //初始化服务提供者列表到本地缓存
                    // TODO : 根据remoteAppKey + groupName区分指定组的服务来进行调用的功能尚未实现，目前是根据interface来调用某个服务的
                    registerCenter4Consumer.initProviderMap(remoteAppKey, groupName);
                    //初始化Netty Channel
                    Map<String, List<ProviderService>> providerMap = registerCenter4Consumer
                        .getServiceMetaDataMap4Consume();
                    if (MapUtils.isEmpty(providerMap)) {
                        throw new RuntimeException("service provider list is empty.");
                    }
                    NettyChannelFactory.channelFactoryInstance().initChannelPoolFactory(providerMap);
                }
                ((RegisterCenter)registerCenter4Consumer).setInit(true);
            }
        }

        // 获取服务提供者代理对象
        InvokerProxyBeanFactory proxyFactory = InvokerProxyBeanFactory
                .newInvokerProxyBeanFactory(targetInterface, timeout, clusterStrategy);
        this.serviceObject = proxyFactory.getProxy();

        //将消费者信息注册到注册中心
        InvokerService invoker = new InvokerService();
        invoker.setServiceItf(targetInterface);
        invoker.setRemoteAppKey(remoteAppKey);
        invoker.setGroupName(groupName);
        registerCenter4Consumer.registerInvoker(invoker);
    }


    public Class<?> getTargetInterface() {
        return targetInterface;
    }

    public void setTargetInterface(Class<?> targetInterface) {
        this.targetInterface = targetInterface;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public void setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
    }

    public String getClusterStrategy() {
        return clusterStrategy;
    }

    public void setClusterStrategy(String clusterStrategy) {
        this.clusterStrategy = clusterStrategy;
    }

    public String getRemoteAppKey() {
        return remoteAppKey;
    }

    public void setRemoteAppKey(String remoteAppKey) {
        this.remoteAppKey = remoteAppKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
