package com.yrris.coddy.ratelimit.annotation;

import com.yrris.coddy.ratelimit.enums.RateLimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    String key() default "";

    int rate() default 10;

    int rateInterval() default 1;

    RateLimitType limitType() default RateLimitType.USER;

    String message() default "Too many requests, please try again later";
}
