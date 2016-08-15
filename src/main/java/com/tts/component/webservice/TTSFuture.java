package com.tts.component.webservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by zhaoqi on 2016/5/27.
 */
public class TTSFuture<T> {

    private static final Logger logger = LoggerFactory.getLogger(TTSFuture.class);

    private Future<T> future;
    private int waitingTime;
    private String serviceName;

    public TTSFuture(Future<T> future, String serviceName, int waitingTime) {
        this.future = future;
        this.waitingTime = waitingTime;
        this.serviceName = serviceName;
    }

    public T get() throws InterruptedException, ExecutionException, TimeoutException {
        return this.get(waitingTime);
    }

    public T get(int timeOut) throws InterruptedException, ExecutionException, TimeoutException {
        T t =future.get(timeOut, TimeUnit.SECONDS);
        logger.info("call service [{}] success return [{}]",serviceName,t);
        return t;
    }
}
