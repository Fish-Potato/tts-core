package com.tts.component.webservice;

import com.tts.component.webservice.hystrix.HystrixCommonCommand;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * Created by zhaoqi on 2016/5/12.
 */
@SuppressWarnings("unchecked")
public class Invoker implements ServiceCaller{

    private ServiceFinder serviceFinder;

    // Timeout value in milliseconds for a command
    private int defaultTimeOut ;

    // waiting timeout
    private int waitingTimeOut;

    private <T> TTSFuture<T> execute(String serviceName, Object param, Class<T> clazz, T fallBack, RequestMethod method, Integer timeOut ) throws ServiceNotFoundException, InterruptedException, ExecutionException, TimeoutException {
        HystrixCommonCommand commonCommand = new HystrixCommonCommand(serviceName, serviceFinder, method, param, clazz,timeOut);
        commonCommand.setFallBack(fallBack);
        Future future = commonCommand.queue();

        return new TTSFuture(future,serviceName,waitingTimeOut);
    }

    @Override
    public <T> TTSFuture<T> futureGet(String serviceName, Object param, Class<T> clazz, T fallBack, int timeOut) throws Exception {
        return this.execute(serviceName,param,clazz,fallBack,RequestMethod.GET,timeOut);
    }

    @Override
    public <T> TTSFuture<T> futureGet(String serviceName, Object param, Class<T> clazz, int timeOut) throws Exception {
        return this.futureGet(serviceName,param,clazz,null,timeOut);
    }

    @Override
    public <T> TTSFuture<T> futureGet(String serviceName, Object param, Class<T> clazz, T fallBack) throws Exception {
        return this.futureGet(serviceName,param,clazz,fallBack,defaultTimeOut);
    }

    @Override
    public <T> TTSFuture<T> futureGet(String serviceName, Object param, Class<T> clazz) throws Exception {
        return this.futureGet(serviceName, param, clazz,null);
    }

    @Override
    public <T> TTSFuture<T> futurePost(String serviceName, Object param, Class<T> clazz, T fallBack, int timeOut) throws Exception {
        return this.execute(serviceName,param,clazz,fallBack,RequestMethod.POST,timeOut);
    }

    @Override
    public <T> TTSFuture<T> futurePost(String serviceName, Object param, Class<T> clazz, T fallBack) throws Exception {
        return this.futurePost(serviceName,param,clazz,fallBack,defaultTimeOut);
    }

    @Override
    public <T> TTSFuture<T> futurePost(String serviceName, Object param, Class<T> clazz, int timeOut) throws Exception {
        return this.futurePost(serviceName,param,clazz,null,timeOut);
    }

    @Override
    public <T> TTSFuture<T> futurePost(String serviceName, Object param, Class<T> clazz) throws Exception {
        return this.futurePost(serviceName,param,clazz,null);
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
}
