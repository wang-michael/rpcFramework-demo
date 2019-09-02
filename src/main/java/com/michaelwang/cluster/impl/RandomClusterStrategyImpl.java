package com.michaelwang.cluster.impl;

import java.util.List;

import com.michaelwang.cluster.ClusterStrategy;
import com.michaelwang.model.ProviderService;
import org.apache.commons.lang3.RandomUtils;

/**
 * @author jiuwang.wjw
 *
 * 随机算法
 */
public class RandomClusterStrategyImpl implements ClusterStrategy {
    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        int MAX_LEN = providerServices.size();
        int index = RandomUtils.nextInt(0, MAX_LEN - 1);
        return providerServices.get(index);
    }
}
