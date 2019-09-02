package com.michaelwang.register;

import java.util.List;
import java.util.Map;

import com.michaelwang.model.ProviderService;

/**
 * @author jiuwang.wjw
 *
 * 服务端注册中心接口
 */
public interface IRegisterCenter4Provider {

    /**
     * 服务端将服务提供者信息注册到zk对应的节点下
     *
     * @param serviceMetaData
     */
     void registerProvider(final List<ProviderService> serviceMetaData);


    /**
     * 服务端获取服务提供者信息
     *
     * 返回Map : Key:服务提供者接口  value:服务提供者服务方法列表
     *
     * @return
     */
     Map<String, List<ProviderService>> getProviderServiceMap();
}
