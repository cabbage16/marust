package com.bamdoliro.maru.shared.auth.annotation;

import com.bamdoliro.maru.shared.auth.Authority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RoleCheck {
    Authority[] value() default{};
}
