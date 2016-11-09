package com.tts.component.webservice.finder;

import com.tts.component.webservice.domain.TTSFuture;
import com.tts.component.webservice.excption.ServiceNotAvailableException;
import com.tts.component.webservice.excption.ServiceNotFoundException;

/**
 * Created by zhaoqi on 2016/5/12.
 */
public interface ServiceCaller {
    <T> TTSFuture<T> futureGet(String serviceName, Object param, Class<T> clazz, T fallBack) throws ServiceNotFoundException,ServiceNotAvailableException;

    <T> TTSFuture<T> futureGet(String serviceName, Object param, Class<T> clazz) throws  ServiceNotFoundException,ServiceNotAvailableException;

    <T> TTSFuture<T> futureGet(String serviceName, Object param, Class<T> clazz, int timeOut) throws  ServiceNotFoundException,ServiceNotAvailableException;

    <T> TTSFuture<T> futureGet(String serviceName, Object param, Class<T> clazz, T fallBack, int timeOut) throws  ServiceNotFoundException,ServiceNotAvailableException;

    <T> TTSFuture<T> futurePost(String serviceName, Object param, Class<T> clazz) throws ServiceNotFoundException,ServiceNotAvailableException;

    <T> TTSFuture<T> futurePost(String serviceName, Object param, Class<T> clazz, T fallBack) throws ServiceNotFoundException,ServiceNotAvailableException;

    <T> TTSFuture<T> futurePost(String serviceName, Object param, Class<T> clazz, int timeOut) throws  ServiceNotFoundException,ServiceNotAvailableException;

    <T> TTSFuture<T> futurePost(String serviceName, Object param, Class<T> clazz, T fallBack, int timeOut) throws  ServiceNotFoundException,ServiceNotAvailableException;

    <T> TTSFuture<T> futurePost(String serviceName, String url, Object param, Class<T> clazz, T fallBack, int timeOut) throws  ServiceNotFoundException,ServiceNotAvailableException;
    <T> TTSFuture<T> futurePost(String serviceName, String url, Object param, Class<T> clazz) throws  ServiceNotFoundException,ServiceNotAvailableException;
    <T> TTSFuture<T> futurePost(String serviceName, String url, Object param, Class<T> clazz, T fallBack) throws  ServiceNotFoundException,ServiceNotAvailableException;
    <T> TTSFuture<T> futurePost(String serviceName, String url, Object param, Class<T> clazz,  int timeOut) throws  ServiceNotFoundException,ServiceNotAvailableException;

    <T> TTSFuture<T> futureGet(String serviceName, String url, Object param, Class<T> clazz, T fallBack, int timeOut) throws  ServiceNotFoundException,ServiceNotAvailableException;
    <T> TTSFuture<T> futureGet(String serviceName, String url, Object param, Class<T> clazz) throws  ServiceNotFoundException,ServiceNotAvailableException;
    <T> TTSFuture<T> futureGet(String serviceName, String url, Object param, Class<T> clazz, T fallBack) throws  ServiceNotFoundException,ServiceNotAvailableException;
    <T> TTSFuture<T> futureGet(String serviceName, String url, Object param, Class<T> clazz, int timeOut) throws  ServiceNotFoundException,ServiceNotAvailableException;
}
