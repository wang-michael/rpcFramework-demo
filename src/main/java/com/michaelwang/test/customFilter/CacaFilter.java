package com.michaelwang.test.customFilter;

import com.michaelwang.filter.Filter;
import com.michaelwang.filter.FilterChain;
import com.michaelwang.model.WmRequest;
import com.michaelwang.model.WmResponse;

/**
 * @author jiuwang.wjw
 */
public class CacaFilter implements Filter {
    @Override
    public void doFilter(WmRequest wmRequest, WmResponse wmResponse, FilterChain filterChain) {
        System.out.println("---------caca before--------");
        filterChain.doFilter(wmRequest, wmResponse);
        System.out.println("---------caca finish-------- ");
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
