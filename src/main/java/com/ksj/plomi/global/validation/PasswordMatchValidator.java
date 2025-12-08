package com.ksj.plomi.global.validation;

import com.ksj.plomi.domain.auth.dto.SignupRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, SignupRequestDto> {

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
    }

    @Override
    public boolean isValid(SignupRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getPassword() == null || dto.getPassword2() == null) {
            return true;
        }

        boolean isValid = dto.getPassword().equals(dto.getPassword2());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("password2")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
