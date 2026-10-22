package com.lostandfound.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

/**
 * Checks the @StrongPassword rule. Length is counted in UTF-8 bytes because that is what
 * BCrypt's 72 limit applies to.
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    /** null passes here on purpose: pair this with @NotBlank to make the field required. */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null) return true;                 // use @NotBlank to require it
        int bytes = value.getBytes(StandardCharsets.UTF_8).length;
        if (bytes < 8 || bytes > 72) return false;
        boolean letter = false, digit = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isLetter(c)) letter = true;
            else if (Character.isDigit(c)) digit = true;
        }
        return letter && digit;
    }
}