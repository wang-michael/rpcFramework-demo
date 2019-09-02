package com.michaelwang.model;

import java.io.Serializable;

/**
 * @author jiuwang.wjw
 */
public class WmResponse implements Serializable {

    private static final long serialVersionUID = -9021514950801838458L;
    /**
     * UUID,唯一标识一次返回值
     */
    private String uniqueKey;
    /**
     * 客户端指定的服务超时时间
     */
    private long invokeTimeout;
    /**
     * 接口调用返回的结果对象
     */
    private Object result;

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public long getInvokeTimeout() {
        return invokeTimeout;
    }

    public void setInvokeTimeout(long invokeTimeout) {
        this.invokeTimeout = invokeTimeout;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public static void copy(WmResponse src, WmResponse dest) {
        if (src == null) {
            return;
        }
        dest.setInvokeTimeout(src.getInvokeTimeout());
        dest.setUniqueKey(src.getUniqueKey());
        dest.setResult(src.getResult());
    }
}
