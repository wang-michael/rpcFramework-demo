package com.michaelwang.cluster;

import java.util.List;

import com.michaelwang.model.ProviderService;

/**
 * @author jiuwang.wjw
 */
public interface ClusterStrategy {

    /**
     * 负载策略算法
     *
     * @param providerServices
     * @return
     */
    ProviderService select(List<ProviderService> providerServices);
}
