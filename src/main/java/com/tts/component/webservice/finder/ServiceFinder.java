package com.tts.component.webservice.finder;

import com.tts.component.webservice.domain.ServiceInstanceDetail;
import com.tts.component.webservice.excption.ServiceNotFoundException;

/**
 * Created by zhaoqi on 2016/5/13.
 */
public interface ServiceFinder {
//    <T> T getServiceFinder();

    ServiceInstanceDetail getService(String serviceName) throws ServiceNotFoundException;
}
