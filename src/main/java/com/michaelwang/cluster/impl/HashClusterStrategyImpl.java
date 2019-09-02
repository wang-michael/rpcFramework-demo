package com.michaelwang.cluster.impl;

import java.util.List;

import com.michaelwang.cluster.ClusterStrategy;
import com.michaelwang.model.ProviderService;
import com.michaelwang.util.IPHelper;

/**
 * @author jiuwang.wjw
 */
public class HashClusterStrategyImpl implements ClusterStrategy {

    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        //获取调用方ip
        String localIP = IPHelper.localIp();
        //获取源地址对应的hashcode
        int hashCode = localIP.hashCode();
        //获取服务列表大小
        int size = providerServices.size();
        return providerServices.get(hashCode % size);
    }
}
