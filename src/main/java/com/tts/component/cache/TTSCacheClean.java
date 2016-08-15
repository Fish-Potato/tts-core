package com.tts.component.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zhaoqi on 2016/8/12 0012.
 * 清理缓存
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TTSCacheClean {

    /**
     * 缓存key表达式
     * 格式: "anyWords#{productId}anyWords#{ProductName}anyWords"
     * #{arg} 会被arg的字段值替换
     * @return
     */
    String keyExpression() default "";

    /**
     * 缓存key用到的参数
     * @return
     */
    String[] includeArgs() default {};

    /**
     * 缓存key不用到的参数
     * @return
     */
    String[] excludeArgs() default {};

    /**
     * 被清理的方法名称
     * @return
     */
    String methodName() default "";

    /**
     * 是否在方法执行前清理缓存
     * 默认方法执行后才清理缓存
     * @return
     */
    boolean beforeTheMethod() default false;

}
