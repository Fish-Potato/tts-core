package com.tts.component.webservice.finder;

import com.tts.component.webservice.domain.TTSFuture;
import com.tts.component.webservice.excption.ServiceNotAvailableException;
import com.tts.component.webservice.excption.ServiceNotFoundException;
import com.tts.component.webservice.hystrix.HystrixCommonCommand;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.concurrent.Future;

/**
 * Created by zhaoqi on 2016/5/12.
 */
@SuppressWarnings("unchecked")
public class Invoker implements ServiceCaller {

    private ServiceFinder serviceFinder;

    // Timeout value in milliseconds for a command
    private int defaultTimeOut ;

    // waiting timeout
    private int waitingTimeOut;

    private RestTemplate restTemplate;

    private <T> TTSFuture<T> execute(String serviceName, String url, Object param, Class<T> clazz, T fallBack, RequestMethod method, Integer timeOut ) throws ServiceNotFoundException,ServiceNotAvailableException {
        HystrixCommonCommand commonCommand = new HystrixCommonCommand(serviceName, url, serviceFinder, method, param, clazz,timeOut,restTemplate);
        commonCommand.setFallBack(fallBack);
        Future future = commonCommand.queue();

        return new TTSFuture(future,serviceName,waitingTimeOut);
    }

    @Override
    public <T> TTSFuture<T> futureGet(String serviceName, Object param, Class<T> clazz, T fallBack, int timeOut) throws ServiceNotFoundException,ServiceNotAvailableException {
        return this.execute(serviceName,null,param,clazz,fallBack,RequestMethod.GET,timeOut);
    }

    @Override
    public <T> TTSFuture<T> futureGet(String serviceName, Object param, Class<T> clazz, int timeOut) throws ServiceNotFoundException,ServiceNotAvailableException {
        return this.futureGet(serviceName,param,clazz,null,timeOut);
    }

    @Override
    public <T> TTSFuture<T> futureGet(String serviceName, Object param, Class<T> clazz, T fallBack) throws ServiceNotFoundException,ServiceNotAvailableException {
        return this.futureGet(serviceName,param,clazz,fallBack,defaultTimeOut);
    }

    @Override
    public <T> TTSFuture<T> futureGet(String serviceName, Object param, Class<T> clazz) throws ServiceNotFoundException,ServiceNotAvailableException {
        return this.futureGet(serviceName, param, clazz,null);
    }

    @Override
    public <T> TTSFuture<T> futurePost(String serviceName, Object param, Class<T> clazz, T fallBack, int timeOut) throws ServiceNotFoundException,ServiceNotAvailableException {
        return this.execute(serviceName,null,param,clazz,fallBack,RequestMethod.POST,timeOut);
    }

    @Override
    public <T> TTSFuture<T> futurePost(String serviceName, Object param, Class<T> clazz, T fallBack) throws ServiceNotFoundException,ServiceNotAvailableException {
        return this.futurePost(serviceName,param,clazz,fallBack,defaultTimeOut);
    }

    @Override
    public <T> TTSFuture<T> futurePost(String serviceName, Object param, Class<T> clazz, int timeOut) throws ServiceNotFoundException,ServiceNotAvailableException {
        return this.futurePost(serviceName,param,clazz,null,timeOut);
    }

    @Override
    public <T> TTSFuture<T> futurePost(String serviceName, Object param, Class<T> clazz) throws ServiceNotFoundException,ServiceNotAvailableException {
        return this.futurePost(serviceName,param,clazz,null);
    }

    @Override
    public <T> TTSFuture<T> futurePost(String serviceName, String url, Object param, Class<T> clazz, T fallBack, int timeOut) throws ServiceNotFoundException, ServiceNotAvailableException {
        return this.execute(serviceName,url,param,clazz,fallBack,RequestMethod.POST,timeOut);
    }

    @Override
    public <T> TTSFuture<T> futureGet(String serviceName, String url, Object param, Class<T> clazz, T fallBack, int timeOut) throws ServiceNotFoundException, ServiceNotAvailableException {
        return this.execute(serviceName,url,param,clazz,fallBack,RequestMethod.GET,timeOut);
    }

    @Override
    public <T> TTSFuture<T> futurePost(String serviceName, String url, Object param, Class<T> clazz) throws ServiceNotFoundException, ServiceNotAvailableException {
        return futurePost(serviceName,url,param,clazz,null,defaultTimeOut);
    }

    @Override
    public <T> TTSFuture<T> futurePost(String serviceName, String url, Object param, Class<T> clazz, T fallBack) throws ServiceNotFoundException, ServiceNotAvailableException {
        return futurePost(serviceName,url,param,clazz,fallBack,defaultTimeOut);
    }

    @Override
    public <T> TTSFuture<T> futurePost(String serviceName, String url, Object param, Class<T> clazz, int timeOut) throws ServiceNotFoundException, ServiceNotAvailableException {
        return futurePost(serviceName,url,param,clazz,null,timeOut);
    }

    @Override
    public <T> TTSFuture<T> futureGet(String serviceName, String url, Object param, Class<T> clazz) throws ServiceNotFoundException, ServiceNotAvailableException {
        return futureGet(serviceName,url,param,clazz,null,defaultTimeOut);
    }

    @Override
    public <T> TTSFuture<T> futureGet(String serviceName, String url, Object param, Class<T> clazz, T fallBack) throws ServiceNotFoundException, ServiceNotAvailableException {
        return futureGet(serviceName,url,param,clazz,fallBack,defaultTimeOut);
    }

    @Override
    public <T> TTSFuture<T> futureGet(String serviceName, String url, Object param, Class<T> clazz, int timeOut) throws ServiceNotFoundException, ServiceNotAvailableException {
        return futureGet(serviceName,url,param,clazz,null,timeOut);
    }

    public void setServiceFinder(ServiceFinder serviceFinder) {
        this.serviceFinder = serviceFinder;
    }

    public void setDefaultTimeOut(int defaultTimeOut) {
        this.defaultTimeOut = defaultTimeOut;
    }

    public void setWaitingTimeOut(int waitingTimeOut) {
        this.waitingTimeOut = waitingTimeOut;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
