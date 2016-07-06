package com.tts.component.webservice;

/**
 * Created by tts on 2016/5/13.
 */
public interface ServiceFinder {
//    <T> T getServiceFinder();

    ServiceInstanceDetail getService(String serviceName) throws ServiceNotFoundException;
}
