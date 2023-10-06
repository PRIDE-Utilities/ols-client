package uk.ac.ebi.pride.utilities.ols.web.service.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public interface Joinable {

    default String access(Field field) {
        try {
            field.setAccessible(true);
            return (String) field.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    default String join() {
        return Arrays.stream(getClass().getDeclaredFields())
                .map(this::access)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));
    }
}
