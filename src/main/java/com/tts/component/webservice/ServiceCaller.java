package com.tts.component.webservice;

/**
 * Created by tts on 2016/5/12.
 */
public interface ServiceCaller {
    <T> T execute(String serviceName, String param, Class<T> clazz) throws Exception;
}
