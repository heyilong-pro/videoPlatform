package com.lyy.anno;

import com.lyy.validation.StateValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
        validatedBy = {StateValidation.class}//指定提供校验规则的类
)
public @interface State {
    //提供校验失败后的提示信息
    String message() default "state参数的值只能是已发布或草稿";
    //指定分组
    Class<?>[] groups() default {};
    //负载 获取State注解的附加信息
    Class<? extends Payload>[] payload() default {};
}
