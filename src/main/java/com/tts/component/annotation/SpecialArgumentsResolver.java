package com.tts.component.annotation;

import com.tts.component.converter.JsonHttpMessageConverter;
import com.tts.util.JsonUtil;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by tts on 2016/5/6.
 */
public class SpecialArgumentsResolver implements HandlerMethodArgumentResolver {
    @Resource
    JsonHttpMessageConverter jsonHttpMessageConverter;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Json.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return this.readArguments(webRequest, parameter, parameter.getParameterType());
    }

    private Object readArguments(NativeWebRequest webRequest, MethodParameter parameter, Class<?> classType) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(servletRequest);
        Object arg =null;
        try {
            // get方式取queryString
            if (servletRequest.getMethod().equals(RequestMappingHandlerAdapter.METHOD_GET)) {
                // base64解码
                String decodedQueryString = new String(Base64Utils.decodeFromString(servletRequest.getQueryString()));
                return JsonUtil.toObject(decodedQueryString, classType);
            }
            // Json注解使用dotaJsonHttpMessageConverter读取参数
            arg = jsonHttpMessageConverter.readInternal(classType, inputMessage);
            if (null == arg) {
                throw new HttpMessageNotReadableException("Required request body is missing: " +
                        parameter.getMethod().toGenericString());
            }
        } catch (IOException e) {
            //
        }
        return arg;
    }
}
