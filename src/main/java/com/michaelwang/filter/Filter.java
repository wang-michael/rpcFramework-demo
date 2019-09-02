package com.michaelwang.filter;

import com.michaelwang.model.WmRequest;
import com.michaelwang.model.WmResponse;

/**
 * @author jiuwang.wjw
 */
public interface Filter {

    /**
     * 进行拦截调用的方法
     * @param wmRequest
     * @param wmResponse
     * @param filterChain
     */
    void doFilter(WmRequest wmRequest, WmResponse wmResponse, FilterChain filterChain);

    /**
     * 返回当前 Filter 的优先级，值越小越优先，按照优先级进行调用
     * 自定义实现的 Filter 的 Priority 最小可设置为 Integer.MIN_VALUE + 10000, 最大可设置为 Integer.MAX_VALUE - 10000。
     * @return
     */
    int getPriority();
}
