package com.tts.component.webservice;

import com.tts.component.annotation.ZeuService;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tts on 2016/5/17.
 */
public class ZeusInitial implements ApplicationContextAware , InitializingBean{

    private static final Logger logger = LoggerFactory.getLogger(ZeusInitial.class);
    private static Map<String, Object> zeusServices = new HashMap<>();

    private String ip;
    private String port;
    private IRegister register;
    private String group;
    private String hostName;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        zeusServices.putAll(applicationContext.getBeansWithAnnotation(Controller.class));
        zeusServices.putAll(applicationContext.getBeansWithAnnotation(RestController.class));
        logger.info("zeus service initialing , find all zeus service {}", zeusServices);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String localIp = this.getLocalHost()[0];
        String localHostName = this.getLocalHost()[1];
        String localPort = port;
        for (Object service : zeusServices.values()) {

            RequestMapping requestMapping =AnnotationUtils.findAnnotation(service.getClass(), RequestMapping.class);
            String classPath = getPath(requestMapping);

            Method[] methods = ReflectionUtils.getAllDeclaredMethods(service.getClass());

            for (Method method: methods) {
                RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                if (null != methodRequestMapping) {
                    ZeuService zeuService = AnnotationUtils.findAnnotation(method,ZeuService.class);

                    String serviceName = getServiceName(zeuService,method.getName());
                    String serviceGroup = getServiceGroup(zeuService,group);
                    String methodPath = getPath(methodRequestMapping);
                    register.registerService(localHostName,localIp,localPort,classPath,methodPath,serviceName,serviceGroup);
                }
            }
        }
    }


    private String[] getLocalHost() {
        if (StringUtils.isNotEmpty(this.ip) && StringUtils.isNotEmpty(this.hostName)) {
            return new String[]{ip,hostName};
        }
        String localIP = "127.0.0.1";
        String localHostName = "local";
        DatagramSocket sock = null;

        try {
            InetSocketAddress e = new InetSocketAddress(InetAddress.getByName("1.2.3.4"), 1);
            sock = new DatagramSocket();
            sock.connect(e);
            localIP = sock.getLocalAddress().getHostAddress();
            localHostName = sock.getLocalAddress().getHostName();
            this.ip = localIP;
            this.hostName = localHostName;
        } catch (Exception e) {
            logger.error("get local ip error",e);
        } finally {
            sock.disconnect();
            sock.close();
            sock = null;
        }

        return new String[]{localIP,localHostName};
    }

    private String getServiceName(ZeuService zeuService, String defaultName) {
        if (null == zeuService) {
            return defaultName;
        }
        if (StringUtils.isBlank(zeuService.value())) {
            return defaultName;
        }
        return zeuService.value();
    }

    private String getServiceGroup(ZeuService zeuService, String defaultGroup) {
        if (null == zeuService) {
            return defaultGroup;
        }
        if (StringUtils.isBlank(zeuService.value())) {
            return defaultGroup;
        }
        return zeuService.group();
    }

    private static final String getPath(RequestMapping requestMapping) {
        if(requestMapping == null) {
            return null;
        } else {
            String[] path = requestMapping.path();
            if(ArrayUtils.isNotEmpty(path)) {
                return path[0];
            } else {
                String[] value = requestMapping.value();
                return ArrayUtils.isNotEmpty(value)?value[0]:null;
            }
        }
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setRegister(IRegister register) {
        this.register = register;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
