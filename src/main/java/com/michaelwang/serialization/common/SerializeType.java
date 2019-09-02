package com.michaelwang.serialization.common;

import org.apache.commons.lang.StringUtils;

/**
 * @author jiuwang.wjw
 */
public enum SerializeType {

    DefaultJavaSerializer("DefaultJavaSerializer"),
    HessianSerializer("HessianSerializer"),
    JSONSerializer("JSONSerializer"),
    XmlSerializer("XmlSerializer");

    private String serializeType;

    SerializeType(String serializeType) {
        this.serializeType = serializeType;
    }


    public static SerializeType queryByType(String serializeType) {
        if (StringUtils.isBlank(serializeType)) {
            return null;
        }

        for (SerializeType serialize : SerializeType.values()) {
            if (StringUtils.equals(serializeType, serialize.getSerializeType())) {
                return serialize;
            }
        }
        return null;
    }

    public String getSerializeType() {
        return serializeType;
    }
}
