package ubivelox.chat.util;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.util.Set;
import java.util.stream.Collectors;

public class ValidatorUtils {
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    public static <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            String sb = violations.stream()
                    .map(violation -> violation.getMessage() + "\n")
                    .collect(Collectors.joining());
            throw new IllegalArgumentException(sb);
        }
    }
}
