package com.lostandfound.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

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