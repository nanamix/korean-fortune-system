package com.fortune.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 생년월일 유효성 검사 어노테이션
 * @author 김동현
 * @version 1.0
 * @since 2025-06-21
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BirthDateValidator.class)
@Documented
public @interface ValidBirthDate {
    /**
     * 유효성 검사 실패 시 반환할 메시지
     * @return 메시지
     */
    String message() default "올바르지 않은 생년월일입니다";
    /**
     * 유효성 검사 그룹
     * @return 그룹
     */
    Class<?>[] groups() default {};
    /**
     * 유효성 검사 페이로드
     * @return 페이로드
     */
    Class<? extends Payload>[] payload() default {};
}
