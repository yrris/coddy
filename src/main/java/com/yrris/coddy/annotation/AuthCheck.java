package com.yrris.coddy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for endpoint-level auth checks.
 * Applied on controller methods to enforce login and optional role requirements.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthCheck {

    /**
     * Required role name (e.g. "ADMIN"). Empty string means login-only check.
     */
    String mustRole() default "";
}
