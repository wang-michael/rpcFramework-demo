package com.michaelwang.filter.filterImpl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;

import avro.shaded.com.google.common.base.Predicate;
import avro.shaded.com.google.common.collect.Collections2;
import com.michaelwang.filter.Filter;
import com.michaelwang.filter.FilterChain;
import com.michaelwang.model.ProviderService;
import com.michaelwang.model.WmRequest;
import com.michaelwang.model.WmResponse;
import com.michaelwang.register.IRegisterCenter4Provider;
import com.michaelwang.register.RegisterCenter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jiuwang.wjw
 */
public class DefaultServerFilter implements Filter {

    /**
     * 服务端限流
     */
    private static final Map<String, Semaphore> serviceKeySemaphoreMap = new ConcurrentHashMap<>();

    @Override
    public void doFilter(WmRequest request, WmResponse response, FilterChain filterChain) {
        //从服务调用对象里获取服务提供者信息
        ProviderService metaDataModel = request.getProviderService();
        long consumeTimeOut = request.getInvokeTimeout();
        final String methodName = request.getInvokedMethodName();

        //根据方法名称定位到具体某一个服务提供者
        String serviceKey = metaDataModel.getServiceItf().getName();
        //获取限流工具类
        int workerThread = metaDataModel.getWorkerThreads();
        Semaphore semaphore = serviceKeySemaphoreMap.get(serviceKey);
        if (semaphore == null) {
            synchronized (serviceKeySemaphoreMap) {
                semaphore = serviceKeySemaphoreMap.get(serviceKey);
                if (semaphore == null) {
                    semaphore = new Semaphore(workerThread);
                    serviceKeySemaphoreMap.put(serviceKey, semaphore);
                }
            }
        }

        //获取注册中心服务
        IRegisterCenter4Provider registerCenter4Provider = RegisterCenter.singleton();
        List<ProviderService> localProviderCaches = registerCenter4Provider.getProviderServiceMap().get(serviceKey);

        Object result = null;
        boolean acquire = false;

        try {
            // 从localProviderCaches中筛选出与当前客户端要调用的方法对应的ProviderService
            ProviderService localProviderCache = Collections2
                .filter(localProviderCaches, new Predicate<ProviderService>() {
                    @Override
                    public boolean apply(ProviderService input) {
                        return StringUtils.equals(input.getServiceMethod().getName(), methodName) && input.getServerPort() == request.getProviderService().getServerPort();
                    }
                }).iterator().next();
            Object serviceObject = localProviderCache.getServiceObject();

            // 利用反射发起服务调用，由于之前已经对method对象进行了缓存，所以这里不需要重新查找
            Method method = localProviderCache.getServiceMethod();
            //利用semaphore实现限流
            acquire = semaphore.tryAcquire(consumeTimeOut, TimeUnit.MILLISECONDS);
            if (acquire) {
                result = method.invoke(serviceObject, request.getArgs());
                //System.out.println("---------------"+result);
            }
        } catch (Exception e) {
            System.out.println(JSON.toJSONString(localProviderCaches) + "  " + methodName+" "+e.getMessage());
            result = e;
        } finally {
            if (acquire) {
                semaphore.release();
            }
        }

        // 根据服务调用结果组装调用返回对象
        response.setInvokeTimeout(consumeTimeOut);
        response.setUniqueKey(request.getUniqueKey());
        response.setResult(result);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }
}
