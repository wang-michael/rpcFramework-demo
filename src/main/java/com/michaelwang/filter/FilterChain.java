package com.michaelwang.filter;

import com.michaelwang.model.WmRequest;
import com.michaelwang.model.WmResponse;

/**
 * @author jiuwang.wjw
 */
public interface FilterChain {
    void doFilter(WmRequest wmRequest, WmResponse wmResponse);
}
