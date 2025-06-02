package ee.ctob.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class WebSocketUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> T parseAndValidate(String json, Class<T> clazz) throws IllegalArgumentException {
        System.out.println("here3");

        try {
            System.out.println("here3");

            T obj = objectMapper.readValue(json, clazz);
            System.out.println("here3");
            Set<ConstraintViolation<T>> violations = validator.validate(obj);
            System.out.println("here3");
            if (!violations.isEmpty()) {
                String errorMessages = violations.stream()
                        .map(v -> v.getPropertyPath() + " " + v.getMessage())
                        .collect(Collectors.joining(", "));
                System.out.println("here3");
                throw new IllegalArgumentException("Validation failed: " + errorMessages);
            }
            System.out.println("here3");
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
