package com.michaelwang.invoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;

import com.michaelwang.cluster.ClusterStrategy;
import com.michaelwang.cluster.engine.ClusterEngine;
import com.michaelwang.filter.Filter;
import com.michaelwang.filter.FilterChain;
import com.michaelwang.filter.filterChainImpl.DefaultFilterChain;
import com.michaelwang.filter.filterImpl.DefaultInvokeFilter;
import com.michaelwang.model.ProviderService;
import com.michaelwang.model.WmRequest;
import com.michaelwang.model.WmResponse;
import com.michaelwang.register.IRegisterCenter4Invoker;
import com.michaelwang.register.RegisterCenter;

/**
 * @author jiuwang.wjw
 */
public class InvokerProxyBeanFactory implements InvocationHandler {

    /**
     * 需要调用的服务的接口
     */
    private Class<?> targetInterface;
    /**
     * 超时时间
     */
    private int consumeTimeout;
    /**
     * 负载均衡策略
     */
    private String clusterStrategy;

    /**
     * 所有的 filter
     * @param targetInterface
     * @param consumeTimeout
     * @param clusterStrategy
     */
    private static List<Filter> filterList = new ArrayList<>();

    static {
        ServiceLoader<Filter> serviceLoader = ServiceLoader.load(Filter.class);
        Iterator<Filter> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            filterList.add(iterator.next());
        }
        filterList.add(new DefaultInvokeFilter());
        Collections.sort(filterList, new Comparator<Filter>() {
            @Override
            public int compare(Filter o1, Filter o2) {
                if (o1.getPriority() == o2.getPriority()) {
                    return 0;
                }
                return o1.getPriority() < o2.getPriority() ? -1 : 1;
            }
        });
    }

    public InvokerProxyBeanFactory(Class<?> targetInterface, int consumeTimeout, String clusterStrategy) {
        this.targetInterface = targetInterface;
        this.consumeTimeout = consumeTimeout;
        this.clusterStrategy = clusterStrategy;
    }

    /**
     * 注意方法可能被并发调用
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // filter 调用链
        DefaultFilterChain filterChain = new DefaultFilterChain(filterList);
        // 服务接口名称
        String serviceKey = targetInterface.getName();
        //获取某个接口的服务提供者列表
        IRegisterCenter4Invoker registerCenter4Consumer = RegisterCenter.singleton();
        List<ProviderService> providerServices = registerCenter4Consumer.getServiceMetaDataMap4Consume().get(serviceKey);
        //根据软负载策略,从服务提供者列表选取本次调用的服务提供者
        ClusterStrategy clusterStrategyService = ClusterEngine.queryClusterStrategy(clusterStrategy);
        ProviderService providerService = clusterStrategyService.select(providerServices);
        //复制一份服务提供者信息
        ProviderService newProvider = providerService.copy();
        //设置本次调用服务的方法以及接口
        newProvider.setServiceMethod(method);
        newProvider.setServiceItf(targetInterface);

        //声明调用Request对象,WmRequest表示发起一次调用所包含的信息
        final WmRequest request = new WmRequest();
        //设置本次调用的唯一标识
        request.setUniqueKey(UUID.randomUUID().toString() + "-" + Thread.currentThread().getId());
        //设置本次调用的服务提供者信息
        request.setProviderService(newProvider);
        //设置本次调用的超时时间
        request.setInvokeTimeout(consumeTimeout);
        //设置本次调用的方法名称
        request.setInvokedMethodName(method.getName());
        //设置本次调用的方法参数信息
        request.setArgs(args);

        WmResponse response = new WmResponse();
        filterChain.doFilter(request, response);
        if (filterChain.getCurrentFilterIndex() == filterList.size()) {
            return response.getResult();
        }
        return null;
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{targetInterface}, this);
    }

    /**
     * 每个interface对应自己的InvokerProxyBeanFactory
     * @param targetInterface
     * @param consumeTimeout
     * @param clusterStrategy
     * @return
     * @throws Exception
     */
    public static InvokerProxyBeanFactory newInvokerProxyBeanFactory(Class<?> targetInterface, int consumeTimeout, String clusterStrategy) throws Exception {
        return new InvokerProxyBeanFactory(targetInterface, consumeTimeout, clusterStrategy);
    }
}
