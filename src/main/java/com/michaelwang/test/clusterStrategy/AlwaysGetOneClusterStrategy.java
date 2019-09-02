package com.michaelwang.test.clusterStrategy;

import java.util.List;

import com.michaelwang.cluster.ClusterStrategy;
import com.michaelwang.model.ProviderService;

/**
 * @author jiuwang.wjw
 */
public class AlwaysGetOneClusterStrategy implements ClusterStrategy {

    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        System.out.println("AlwaysGetOneClusterStrategy");
        return providerServices.get(0);
    }
}
