package com.tts.component.webservice;

/**
 * Created by tts on 2016/5/17.
 */
public interface IRegister {
    void registerService(String hostName,String ip,String port,String classPath, String methodPath,String serviceName,String serviceGroup);
}
