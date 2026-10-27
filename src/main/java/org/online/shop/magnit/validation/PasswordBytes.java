package org.online.shop.magnit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;
@Documented @Constraint(validatedBy = PasswordBytesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordBytes {
    String message() default "Пароль должен занимать не более 72 байт в UTF-8";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
