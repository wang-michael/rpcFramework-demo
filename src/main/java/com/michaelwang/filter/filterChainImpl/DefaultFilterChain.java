package com.michaelwang.filter.filterChainImpl;

import java.util.List;

import com.michaelwang.filter.Filter;
import com.michaelwang.filter.FilterChain;
import com.michaelwang.model.WmRequest;
import com.michaelwang.model.WmResponse;

/**
 * @author jiuwang.wjw
 */
public class DefaultFilterChain implements FilterChain {

    private List<Filter> filterList;
    private int currentFilterIndex;

    public DefaultFilterChain(List<Filter> filterList) {
        this.filterList = filterList;
        currentFilterIndex = 0;
    }

    @Override
    public void doFilter(WmRequest wmRequest, WmResponse wmResponse) {
        filterList.get(currentFilterIndex++).doFilter(wmRequest, wmResponse, this);
    }

    public List<Filter> getFilterList() {
        return filterList;
    }

    public void setFilterList(List<Filter> filterList) {
        this.filterList = filterList;
    }

    public int getCurrentFilterIndex() {
        return currentFilterIndex;
    }

    public void setCurrentFilterIndex(int currentFilterIndex) {
        this.currentFilterIndex = currentFilterIndex;
    }
}
