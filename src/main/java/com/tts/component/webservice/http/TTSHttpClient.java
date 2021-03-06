package com.tts.component.webservice.http;

import com.tts.util.JsonUtil;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by zhaoqi on 2016/4/28.
 */
public class TTSHttpClient {
    public static <T> T send(Object params, String url, RequestMethod method, Class<T> clazz,int connectionTimeout,int readTimeOut) {
        String args = JsonUtil.toString(params);
        String response = TTSHttpClientBuilder.sendRequest(Base64Utils.encodeToString(args.getBytes()), url, method, connectionTimeout, readTimeOut);
        if (null == response) {
            return null;
        }
        // base64解码
        return JsonUtil.toObject(new String(Base64Utils.decodeFromString(response)),clazz);
    }
}
