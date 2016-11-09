package com.tts.component.webservice.excption;

/**
 * Created by zhaoqi on 2016/5/19.
 */
public class ServiceNotFoundException extends RuntimeException {

    private Throwable originException;
    private String message;

    private String serviceName;

    public ServiceNotFoundException(String serviceName) {
        super();
        this.serviceName = serviceName;
    }

    public ServiceNotFoundException(String serviceName,Throwable throwable) {
        super();
        this.serviceName = serviceName;
        this.originException = throwable;
    }

    public String getMessage() {
        return "Could find service : " + serviceName;
    }

}
