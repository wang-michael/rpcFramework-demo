package com.michaelwang.invoker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.michaelwang.model.WmResponse;
import com.michaelwang.model.WmResponseWrapper;

/**
 * @author jiuwang.wjw
 */
public class InvokerResponseHolder {

    /**
     * 服务返回结果Map
     */
    private static final Map<String, WmResponseWrapper> responseMap = new ConcurrentHashMap<>();
    /**
     * 清除过期的返回结果
     */
    private static final ExecutorService removeExpireKeyExecutor = Executors.newSingleThreadExecutor();

    static {
        //删除超时未获取到结果的key,防止内存泄露
        removeExpireKeyExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        for (Map.Entry<String, WmResponseWrapper> entry : responseMap.entrySet()) {
                            boolean isExpire = entry.getValue().isExpire();
                            if (isExpire) {
                                responseMap.remove(entry.getKey());
                            }
                            Thread.sleep(10);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    /**
     * 初始化返回结果容器,requestUniqueKey唯一标识本次调用
     *
     * @param requestUniqueKey
     */
    public static void initResponseData(String requestUniqueKey) {
        responseMap.put(requestUniqueKey, WmResponseWrapper.of());
    }


    /**
     * 将Netty调用异步返回结果放入阻塞队列
     *
     * @param response
     */
    public static void putResultValue(WmResponse response) {
        long currentTime = System.currentTimeMillis();
        WmResponseWrapper responseWrapper = responseMap.get(response.getUniqueKey());
        responseWrapper.setResponseTime(currentTime);
        responseWrapper.getResponseQueue().add(response);
        responseMap.put(response.getUniqueKey(), responseWrapper);
    }


    /**
     * 从阻塞队列中获取Netty异步返回的结果值
     *
     * @param requestUniqueKey
     * @param timeout
     * @return
     */
    public static WmResponse getValue(String requestUniqueKey, long timeout) {
        WmResponseWrapper responseWrapper = responseMap.get(requestUniqueKey);
        try {
            return responseWrapper.getResponseQueue().poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            responseMap.remove(requestUniqueKey);
        }
    }
}
