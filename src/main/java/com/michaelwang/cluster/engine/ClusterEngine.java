package com.michaelwang.cluster.engine;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import avro.shaded.com.google.common.collect.Maps;
import com.michaelwang.cluster.ClusterStrategy;
import com.michaelwang.cluster.impl.ClusterStrategyEnum;
import com.michaelwang.cluster.impl.HashClusterStrategyImpl;
import com.michaelwang.cluster.impl.PollingClusterStrategyImpl;
import com.michaelwang.cluster.impl.RandomClusterStrategyImpl;
import com.michaelwang.cluster.impl.WeightPollingClusterStrategyImpl;
import com.michaelwang.cluster.impl.WeightRandomClusterStrategyImpl;

/**
 * @author jiuwang.wjw
 */
public class ClusterEngine {

    private static final Map<ClusterStrategyEnum, ClusterStrategy> clusterStrategyMap = new ConcurrentHashMap<>();
    private static final Map<String, ClusterStrategy> customizeStrategyMap = new ConcurrentHashMap<>();


    static {
        clusterStrategyMap.put(ClusterStrategyEnum.Random, new RandomClusterStrategyImpl());
        clusterStrategyMap.put(ClusterStrategyEnum.WeightRandom, new WeightRandomClusterStrategyImpl());
        clusterStrategyMap.put(ClusterStrategyEnum.Polling, new PollingClusterStrategyImpl());
        clusterStrategyMap.put(ClusterStrategyEnum.WeightPolling, new WeightPollingClusterStrategyImpl());
        clusterStrategyMap.put(ClusterStrategyEnum.Hash, new HashClusterStrategyImpl());

        ServiceLoader<ClusterStrategy> serviceLoader = ServiceLoader.load(ClusterStrategy.class);
        Iterator<ClusterStrategy> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            ClusterStrategy clusterStrategy = iterator.next();
            System.out.println("clusterStrategy name: " + clusterStrategy.getClass().getSimpleName());
            customizeStrategyMap.put(clusterStrategy.getClass().getSimpleName(), clusterStrategy);
        }
    }

    public static ClusterStrategy queryClusterStrategy(String clusterStrategy) {
        ClusterStrategy clusterStrategyTemp;
        if ((clusterStrategyTemp = customizeStrategyMap.get(clusterStrategy)) != null) {
            return clusterStrategyTemp;
        }
        ClusterStrategyEnum clusterStrategyEnum = ClusterStrategyEnum.queryByCode(clusterStrategy);
        if (clusterStrategyEnum == null) {
            //默认选择随机算法
            return new RandomClusterStrategyImpl();
        }
        return clusterStrategyMap.get(clusterStrategyEnum);
    }
}
