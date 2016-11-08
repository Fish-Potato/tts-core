package com.tts.component.webservice;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询
 * Created by onefish on 2016/11/8 0008.
 */
public class RoundRobinStrategy implements Strategy {
    private final AtomicInteger index = new AtomicInteger(0);
    @Override
    public <T> T getServiceInstance(List<T> services) {
        if (CollectionUtils.isEmpty(services)) {
            return null;
        }
        int thisIndex = Math.abs(index.getAndIncrement());
        return services.get(thisIndex % services.size());
    }
}
