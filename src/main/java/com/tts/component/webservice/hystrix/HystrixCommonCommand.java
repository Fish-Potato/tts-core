package com.tts.component.webservice.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.tts.component.webservice.excption.ServiceNotAvailableException;
import com.tts.component.webservice.http.TTSHttpClient;
import com.tts.component.webservice.finder.ServiceFinder;
import com.tts.component.webservice.domain.ServiceInstanceDetail;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

/**
 * Created by zhaoqi on 2016/5/20.
 */
public class HystrixCommonCommand<T> extends HystrixCommand<T> {

    private static final Logger logger = LoggerFactory.getLogger(HystrixCommonCommand.class);

    private String serviceName;
    private String urlPath;
    private ServiceFinder serviceFinder;
    private RequestMethod methodType;
    private Object param;
    private Class<T> responseType;
    private T fallBack;
    private RestTemplate restTemplate;

    public HystrixCommonCommand(String serviceName, String urlPath, ServiceFinder serviceFinder, RequestMethod methodType, Object param, Class<T> responseType, int timeOut,RestTemplate restTemplate) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(serviceName.split("\\.")[0]))
                .andCommandKey(HystrixCommandKey.Factory.asKey(serviceName))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(timeOut)));
        this.serviceName=serviceName;
        this.urlPath = urlPath;
        this.serviceFinder=serviceFinder;
        this.methodType=methodType;
        this.param=param;
        this.responseType=responseType;
        this.restTemplate=restTemplate;
    }

    /**
     * 重写run方法，实现熔断器保护下的接口调用
     * @return
     * @throws Exception
     */
    @Override
    protected T run() throws Exception {
        String url;
        if (StringUtils.isNotBlank(urlPath)) {
            url = urlPath;
        } else {
            // http call
            ServiceInstanceDetail detail = serviceFinder.getService(serviceName);
            url = "http://"+detail.getLocalIp()+":"+detail.getLocalPort()+detail.getClassPath()+detail.getMethodPath();
        }
        return this.doHttpCall(url);
    }

    // 自定义的http client（自带base64加密）
    private T doHttpCall(String url) throws ServiceNotAvailableException {
        return TTSHttpClient.send(param,url, methodType,responseType,0,0);
    }

    // 使用RestTemplate进行http调用
    private T httpCall(String url) throws ServiceNotAvailableException {
        if (methodType.equals(RequestMethod.GET)) {
            return restTemplate.getForObject(url,responseType,param);
        } else {
            return restTemplate.postForObject(url,param,responseType);
        }

    }

    public void setFallBack(T fallBack) {
        this.fallBack = fallBack;
    }

    /**
     * 降级，接口调用失败会执行fallback
     * @return
     */
    protected T getFallback() {
        logger.info("execute service [{}] failed ,do fallback",serviceName);
        if (null != fallBack) {
            // 执行fallback
            fallBack = doFallBack();
            return fallBack;
        }
        else {
            throw new UnsupportedOperationException("No fallback available."+serviceName);
        }
    }

    private T doFallBack() {
        // do something
        return fallBack;
    }
}
