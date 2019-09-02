package com.michaelwang.cluster.impl;

import java.util.ArrayList;
import java.util.List;

import com.michaelwang.cluster.ClusterStrategy;
import com.michaelwang.model.ProviderService;
import org.apache.commons.lang3.RandomUtils;

/**
 * @author jiuwang.wjw
 *
 * 加权随机负载均衡算法
 */
public class WeightRandomClusterStrategyImpl implements ClusterStrategy {

    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        //存放加权后的服务提供者列表
        List<ProviderService> providerList = new ArrayList<>();
        for (ProviderService provider : providerServices) {
            int weight = provider.getWeight();
            for (int i = 0; i < weight; i++) {
                providerList.add(provider.copy());
            }
        }

        int MAX_LEN = providerList.size();
        int index = RandomUtils.nextInt(0, MAX_LEN - 1);
        return providerList.get(index);
    }
}
