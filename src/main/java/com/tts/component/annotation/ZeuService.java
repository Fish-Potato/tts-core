package com.tts.component.annotation;

import java.lang.annotation.*;

/**
 * Created by tts on 2016/5/11.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@Documented
public @interface ZeuService {
    String value() default "";
    String group() default "";
}
