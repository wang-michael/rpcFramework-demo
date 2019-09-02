package com.michaelwang.register;

import java.util.List;

import com.michaelwang.model.InvokerService;
import com.michaelwang.model.ProviderService;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 服务治理接口
 *
 * @author jiuwang.wjw
 */
public interface IRegisterCenter4Governance {

    /**
     * 获取服务提供者列表与服务消费者列表
     *
     * @param serviceName
     * @param appKey
     * @return
     */
     Pair<List<ProviderService>, List<InvokerService>> queryProvidersAndInvokers(String serviceName, String appKey);
}
