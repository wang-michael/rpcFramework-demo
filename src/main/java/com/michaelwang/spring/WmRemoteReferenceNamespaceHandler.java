package com.michaelwang.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author jiuwang.wjw
 */
public class WmRemoteReferenceNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("reference", new InvokerFactoryBeanDefinitionParser());
    }
}
