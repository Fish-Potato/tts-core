package com.tts.component.cache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhaoqi on 2016/8/12 0012.
 */
@Aspect
public class CacheHandlerAop {

    private CacheClient<String,Object> cacheClient;

    private static final Logger logger = LoggerFactory.getLogger(CacheHandlerAop.class);

    private int defaultTimeout = 300;

    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void controllerPointcut() {
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerPointcut() {
    }

    @Pointcut("@annotation(com.tts.component.cache.TTSCache)")
    public void cacheAnnotationPointCut() {
    }

    @Pointcut("@annotation(com.tts.component.cache.TTSCacheClean)")
    public void cacheCleanAnnotationPointCut() {
    }

    @Pointcut("@annotation(com.tts.component.cache.TTSCacheUpdate)")
    public void cacheUpdateAnnotationPointCut() {
    }

    @Around("(controllerPointcut()|| restControllerPointcut()) &&  cacheAnnotationPointCut() ")
    public Object addCacheAop(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //方法名称
        final String methodName = signature.getMethod().getName();
        //返回类型
        final Class<?> returnType = signature.getMethod().getReturnType();

        TTSCache ttsCache = signature.getMethod().getAnnotation(TTSCache.class);

        final String cacheKey = getCacheKey(joinPoint,ttsCache);

        // 从缓存中获取
        try {
            Object result = cacheClient.get(cacheKey,returnType);
            if (null != result) {
                logger.debug("method {} hit the cache key is {}",methodName,cacheKey);
                return result;
            }
        } catch (Exception e) {

        }
        Object result;
        // 执行请求
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            logger.error("method {} throws the exception",methodName);
            return throwable;
        }
        try {
            // 写缓存
            if (null != result) {
                int expireTime = ttsCache.expire();

                if (expireTime <= 0) {
                    expireTime =defaultTimeout;
                }
                cacheClient.set(cacheKey, result, expireTime, TimeUnit.SECONDS);
                logger.debug("method {} miss the cache key {} ,add the cache success after the method",methodName ,cacheKey);
            }
        } catch (Exception e) {
            logger.error("method {} add key {} exception",methodName,cacheKey);
        }
        return result;
    }

    @Around("(controllerPointcut()|| restControllerPointcut()) &&  cacheCleanAnnotationPointCut() ")
    public Object cleanCacheAop(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //方法名称
        final String methodName = signature.getMethod().getName();

        TTSCacheClean ttsCacheClean = signature.getMethod().getAnnotation(TTSCacheClean.class);

        final String cacheKey = getCacheKey(joinPoint,ttsCacheClean);

        boolean beforeTheMethod = ttsCacheClean.beforeTheMethod();

        // 方法执行前删除缓存
        try {
            if (beforeTheMethod) {
                cacheClient.delete(cacheKey);
            }
        } catch (Exception e) {
            logger.error("method {} delete key {} exception",methodName,cacheKey);
        }

        Object result;
        try {
            // 执行请求
            result = joinPoint.proceed();

            if (!beforeTheMethod) {
                cacheClient.delete(cacheKey);
            }
        } catch (Throwable throwable) {
            logger.error("method {} throws the exception",methodName);
            throw throwable;
        }

        // 删除缓存
        try {
            if (!beforeTheMethod) {
                cacheClient.delete(cacheKey);
            }
        } catch (Exception e) {
            logger.error("method {} delete key {} exception",methodName,cacheKey);
        }
        logger.debug("method {} delete the cache {] success after the method",methodName ,cacheKey);
        return result;
    }

    @Around("(controllerPointcut()|| restControllerPointcut()) &&  cacheCleanAnnotationPointCut() ")
    public Object cleanUpdateCacheAop(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //方法名称
        final String methodName = signature.getMethod().getName();

        TTSCacheUpdate ttsCacheUpdate = signature.getMethod().getAnnotation(TTSCacheUpdate.class);

        final String cacheKey = getCacheKey(joinPoint,ttsCacheUpdate);
        Object result;
        // 执行请求
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            logger.error("method {} throws the exception",methodName);
            throw throwable;
        }
        // 删除缓存
        try {
            cacheClient.delete(cacheKey);
        } catch (Exception e) {
            logger.error("method {} delete key {} exception",methodName,cacheKey);
        }

        // 写入缓存
        try {
            int expireTime = ttsCacheUpdate.expire();
            if (expireTime <= 0) {
                expireTime =defaultTimeout;
            }
            cacheClient.set(cacheKey,result,expireTime,TimeUnit.SECONDS);
            logger.debug("method {} update the cache {] success after the method",methodName ,cacheKey);
        } catch (Exception e) {
            logger.error("method {} add key {} exception",methodName,cacheKey);
        }
        return result;
    }

    private String getCacheKey(ProceedingJoinPoint joinPoint, TTSCache ttsCache) {
        return getCacheKey(joinPoint,ttsCache.keyExpression(),ttsCache.includeArgs(),ttsCache.excludeArgs());
    }

    private String getCacheKey(ProceedingJoinPoint joinPoint, TTSCacheClean ttsCacheClean) {
        return getCacheKey(joinPoint, ttsCacheClean.keyExpression(), ttsCacheClean.includeArgs(), ttsCacheClean.excludeArgs());
    }

    private String getCacheKey(ProceedingJoinPoint joinPoint, TTSCacheUpdate ttsCacheClean) {
        return getCacheKey(joinPoint, ttsCacheClean.keyExpression(), ttsCacheClean.includeArgs(), ttsCacheClean.excludeArgs());
    }

    private String getCacheKey(ProceedingJoinPoint joinPoint, String s, String[] strings, String[] strings1) {
        return "";
    }
}
