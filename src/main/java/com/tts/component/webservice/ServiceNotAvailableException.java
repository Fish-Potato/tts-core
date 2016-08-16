package com.tts.component.webservice;

/**
 * Created by zhaoqi on 2016/8/16 0016.
 */
public class ServiceNotAvailableException extends RuntimeException {

    private String serviceName;

    private Throwable originException;

    public ServiceNotAvailableException(String serviceName, Throwable throwable) {
        this.serviceName = serviceName;
        this.originException = throwable;
    }

    public ServiceNotAvailableException(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMessage() {
        return "service {} is not available " + serviceName;
    }
}
