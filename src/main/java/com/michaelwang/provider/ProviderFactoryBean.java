package com.michaelwang.provider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.michaelwang.model.ProviderService;
import com.michaelwang.provider.NettyServer;
import com.michaelwang.register.IRegisterCenter4Provider;
import com.michaelwang.register.RegisterCenter;
import com.michaelwang.util.IPHelper;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 服务Bean发布入口
 *
 * @author jiuwang.wjw
 */
public class ProviderFactoryBean implements FactoryBean, InitializingBean {

    /**
     * 服务接口
     */
    private Class<?> serviceItf;
    /**
     * 服务实现类
     */
    private Object serviceObject;
    /**
     * 服务端口
     */
    private String serverPort;
    /**
     * 服务超时时间，暂时没有用到，以客户端设置的为准
     */
    private long timeout;
    /**
     * 服务代理对象,暂时没有用到
     */
    private Object serviceProxyObject;
    /**
     * 服务提供者唯一标识，设置这个参数为了防止有多个不同的应用服务接口名字相同
     */
    private String appKey;
    /**
     * 服务分组组名，服务分组功能暂时没有用到
     */
    private String groupName = "default";
    /**
     * 服务提供者权重,默认为1 ,范围为[1-100]
     */
    private int weight = 1;
    /**
     * 服务端线程数,默认10个线程，服务端使用此参数对每个接口的并发调用数进行控制，限流
     */
    private int workerThreads = 10;

    @Override
    public Object getObject() {
        return serviceProxyObject;
    }

    @Override
    public Class<?> getObjectType() {
        return serviceItf;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        //启动Netty服务端
        NettyServer.singleton().start(Integer.parseInt(serverPort));
        System.out.println("ProviderFactoryBean: " + this);

        //注册到zk,元数据注册中心
        List<ProviderService> providerServiceList = buildProviderServiceInfos();
        IRegisterCenter4Provider registerCenter4Provider = RegisterCenter.singleton();
        registerCenter4Provider.registerProvider(providerServiceList);
    }


    private List<ProviderService> buildProviderServiceInfos() {
        List<ProviderService> providerList = new ArrayList<>();
        Method[] methods = serviceObject.getClass().getDeclaredMethods();
        // serviceObject的每个method对应于一个ProviderService，比如HelloService.sayHello就对应一个ProviderService
        // 这样设计的目的是缓存每个method对应的ProviderService，以便可以直接进行调用
        for (Method method : methods) {
            ProviderService providerService = new ProviderService();
            providerService.setServiceItf(serviceItf);
            providerService.setServiceObject(serviceObject);
            providerService.setServerIp(IPHelper.localIp());
            providerService.setServerPort(Integer.parseInt(serverPort));
            providerService.setTimeout(timeout);
            providerService.setServiceMethod(method);
            providerService.setWeight(weight);
            providerService.setWorkerThreads(workerThreads);
            providerService.setAppKey(appKey);
            providerService.setGroupName(groupName);
            providerList.add(providerService);
        }
        return providerList;
    }


    public Class<?> getServiceItf() {
        return serviceItf;
    }

    public void setServiceItf(Class<?> serviceItf) {
        this.serviceItf = serviceItf;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public void setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public Object getServiceProxyObject() {
        return serviceProxyObject;
    }

    public void setServiceProxyObject(Object serviceProxyObject) {
        this.serviceProxyObject = serviceProxyObject;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
