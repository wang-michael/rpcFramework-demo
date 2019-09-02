package com.michaelwang.test.customFilter;

import com.michaelwang.filter.Filter;
import com.michaelwang.filter.FilterChain;
import com.michaelwang.model.WmRequest;
import com.michaelwang.model.WmResponse;

/**
 * @author jiuwang.wjw
 * @date 2019/07/29
 */
public class HahaFilter implements Filter {

    @Override
    public void doFilter(WmRequest wmRequest, WmResponse wmResponse, FilterChain filterChain) {
        System.out.println("********haha before********");
        filterChain.doFilter(wmRequest, wmResponse);
        System.out.println("********haha finish********");
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
