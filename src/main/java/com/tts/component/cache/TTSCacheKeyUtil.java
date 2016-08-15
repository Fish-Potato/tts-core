package com.tts.component.cache;

import com.tts.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaoqi on 2016/8/15 0015.
 */
public class TTSCacheKeyUtil {

    private final static String TTS_CACHE_PREFIX = "TTS_cache_";

    public static String getCacheKey(ProceedingJoinPoint joinPoint, TTSCache ttsCache, String methodName) {
        return getCacheKey(joinPoint, methodName,ttsCache.keyExpression(),ttsCache.includeArgs(),ttsCache.excludeArgs());
    }

    public static String getCacheKey(ProceedingJoinPoint joinPoint, TTSCacheClean ttsCacheClean, String methodName) {
        return getCacheKey(joinPoint, methodName, ttsCacheClean.keyExpression(), ttsCacheClean.includeArgs(), ttsCacheClean.excludeArgs());
    }

    public static String getCacheKey(ProceedingJoinPoint joinPoint, TTSCacheUpdate ttsCacheUpdate, String methodName) {
        return getCacheKey(joinPoint, methodName, ttsCacheUpdate.keyExpression(), ttsCacheUpdate.includeArgs(), ttsCacheUpdate.excludeArgs());
    }

    public static String getCacheKey(ProceedingJoinPoint joinPoint, String methodName, String expression, String[] includeArgs, String[] excludeArgs) {
        Object[] args = joinPoint.getArgs();
        return getCacheKey(args, methodName, expression, includeArgs, excludeArgs);
    }

    /**
     * 1.如果有expression，则按照expression作为key
     * 2.没有expression，取参数中的includeArgs字段
     * 3.没有expression且没有includeArgs，则取参数中excludeArgs之外的字段
     * @param args 请求的参数
     * @param methodName 请求的方法名
     * @param expression cache key表达式
     * @param includeArgs 包含的参数
     * @param excludeArgs 不包含的参数
     * @return cache key
     */
    public static String getCacheKey(Object[] args, String methodName, String expression, String[] includeArgs, String[] excludeArgs) {
        String cacheKey =TTS_CACHE_PREFIX + methodName+":";
        try {
            // key expression不为空的时候，key为手动设置，按规则替换后直接返回
            if (StringUtils.isNotBlank(expression)) {
                cacheKey += expression;
                String[] replacingArgs = findReplacingArgs(expression);
                for (Object arg:args) {
                    for (String relacingArgName :replacingArgs) {
                        Object fieldValue = FieldUtils.readField(arg, relacingArgName,true);
                        cacheKey = cacheKey.replace("#{"+ relacingArgName +"}", JsonUtil.toString(fieldValue));
                    }
                }
                return cacheKey;
            } else {
                for (Object arg : args) {
                    Field[] fields = arg.getClass().getDeclaredFields();
                    for (Field field: fields) {
                        field.setAccessible(true );
                        if (null != includeArgs && includeArgs.length>0) {
                            for (String includeArg : includeArgs) {
                                if (field.getName().equals(includeArg)) {
                                    cacheKey +=includeArg+"="+JsonUtil.toString(field.get(arg))+":";
                                    break;
                                }
                            }
                        } else if (null != excludeArgs && excludeArgs.length>0) {
                            for (String excludeArg : excludeArgs) {
                                if (field.getName().equals(excludeArg)) {
                                    continue;
                                }
                                cacheKey += JsonUtil.toString(field.get(arg))+":";
                            }
                        } else {
                            cacheKey += JsonUtil.toString(field.get(arg))+":";
                        }
                    }
                }
            }
        } catch (Exception e) {

        }

        return cacheKey.substring(0,cacheKey.length()-1);
    }

    private static String[] findReplacingArgs(String expression) {

        List<String> arguments = new ArrayList<>();
        String regex = "\\{.*\\}";
        String[] splitStr = expression.split("#");
        Pattern pattern = Pattern.compile(regex);
        for (String str : splitStr) {
            Matcher mat = pattern.matcher(str);
            if (mat.find()) {
                arguments.add(mat.group(0).substring(1,mat.group(0).length()-1));
            }

        }
        return arguments.toArray(new String[arguments.size()]);
    }
}
