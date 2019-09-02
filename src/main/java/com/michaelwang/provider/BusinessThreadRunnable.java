package com.michaelwang.provider;

import com.michaelwang.filter.Filter;
import com.michaelwang.filter.filterChainImpl.DefaultFilterChain;
import com.michaelwang.filter.filterImpl.DefaultServerFilter;
import com.michaelwang.model.WmRequest;
import com.michaelwang.model.WmResponse;
import io.netty.channel.ChannelHandlerContext;

import java.util.*;

public class BusinessThreadRunnable implements Runnable {

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
        filterList.add(new DefaultServerFilter());
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

    private ChannelHandlerContext ctx;
    private WmRequest request;

    public BusinessThreadRunnable(ChannelHandlerContext ctx, WmRequest request) {
        this.ctx = ctx;
        this.request = request;
    }

    @Override
    public void run() {
        // filter 调用链
        DefaultFilterChain filterChain = new DefaultFilterChain(filterList);
        WmResponse response = new WmResponse();
        filterChain.doFilter(request, response);
        if (filterChain.getCurrentFilterIndex() == filterList.size()) {
            ctx.writeAndFlush(response);
        }
    }
}
