package com.mall.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Description：JSR303自定义注解 必须有前三个方法
 * @author L
 */
@Documented
// 指定校验器   这里可以指定多个不同的校验器
@Constraint(validatedBy = {ListValueConstraintValidator.class })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface ListValue {
	// 1 使用该属性去Validation.properties中取
	String message() default "{com.mall.common.valid.listvalue.message}";

	// 2
	Class<?>[] groups() default { };

	// 3
	Class<? extends Payload>[] payload() default { };

	int[] vals() default { };
}
