package com.michaelwang.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 服务发布自定义标签
 *
 * @author jiuwang.wjw
 */
public class WmRemoteServiceNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("service", new ProviderFactoryBeanDefinitionParser());
    }
}
