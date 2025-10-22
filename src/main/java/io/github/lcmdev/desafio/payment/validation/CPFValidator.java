package io.github.lcmdev.desafio.payment.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static java.util.Objects.isNull;

public class CPFValidator implements ConstraintValidator<CPF, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isNull(value) || value.trim().isEmpty()) {
            return false;
        }

        String cpf = value.trim().replaceAll("\\D", "");
        if (cpf.length() != 11) {
            return false;
        }

        char first = cpf.charAt(0);
        boolean allSame = true;
        for (int i = 1; i < cpf.length(); i++) {
            if (cpf.charAt(i) != first) {
                allSame = false;
                break;
            }
        }
        if (allSame) {
            return false;
        }

        int[] digits = new int[11];
        for (int i = 0; i < 11; i++) {
            digits[i] = cpf.charAt(i) - '0';
        }

        for (int j = 9; j <= 10; j++) {
            int sum = 0;
            for (int i = 0; i < j; i++) {
                sum += digits[i] * (j + 1 - i);
            }
            int checked = (sum * 10) % 11;
            if (checked == 10) {
                checked = 0;
            }
            if (checked != digits[j]) {
                return false;
            }
        }

        return true;
    }
}
