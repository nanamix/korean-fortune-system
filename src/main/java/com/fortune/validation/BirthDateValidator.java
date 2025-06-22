package com.fortune.validation;

import java.time.LocalDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import com.fortune.dto.SajuRequest;

/**
 * 생년월일 유효성 검사
 */
@Component
public class BirthDateValidator implements ConstraintValidator<ValidBirthDate, SajuRequest> {

    /**
     * 생년월일 유효성 검사
     * @param request 요청 객체
     * @param context 컨텍스트
     * @return 유효성 여부
     */
    @Override
    public boolean isValid(SajuRequest request, ConstraintValidatorContext context) {
        if (request == null) return false;

        try {
            /**
             * 날짜 유효성 검사
             * 날짜는 1900-2100 사이여야 합니다
             * @param request 요청 객체
             * @param context 컨텍스트
             * @return 유효성 여부
             */
            LocalDate.of(request.getBirthYear(), request.getBirthMonth(), request.getBirthDay());

            /**
             * 시간 유효성 검사
             * 시간은 0-23 사이여야 합니다
             * @param request 요청 객체
             * @param context 컨텍스트
             * @return 유효성 여부
             */
            if (request.getBirthHour() < 0 || request.getBirthHour() > 23) {
                context.buildConstraintViolationWithTemplate("시간은 0-23 사이여야 합니다")
                        .addPropertyNode("birthHour")
                        .addConstraintViolation();
                return false;
            }
            /**
             * 분 유효성 검사
             * 분은 0-59 사이여야 합니다
             * @param request 요청 객체
             * @param context 컨텍스트
             * @return 유효성 여부
             */
            if (request.getBirthMinute() < 0 || request.getBirthMinute() > 59) {
                context.buildConstraintViolationWithTemplate("분은 0-59 사이여야 합니다")
                        .addPropertyNode("birthMinute")
                        .addConstraintViolation();
                return false;
            }

            /**
             * 년도 범위 검사
             * 년도는 1900-2100 사이여야 합니다
             * @param request 요청 객체
             * @param context 컨텍스트
             * @return 유효성 여부
             */
            if (request.getBirthYear() < 1900 || request.getBirthYear() > 2100) {
                context.buildConstraintViolationWithTemplate("년도는 1900-2100 사이여야 합니다")
                        .addPropertyNode("birthYear")
                        .addConstraintViolation();
                return false;
            }

            return true;
        } catch (Exception e) {
            /**
             * 올바르지 않은 날짜입니다
             * @param request 요청 객체
             * @param context 컨텍스트
             * @return 유효성 여부
             */
            context.buildConstraintViolationWithTemplate("올바르지 않은 날짜입니다")
                    .addConstraintViolation();
            return false;
        }
    }
}
