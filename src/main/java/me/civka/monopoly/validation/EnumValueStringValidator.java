package me.civka.monopoly.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumValueStringValidator implements ConstraintValidator<EnumValueMatch, String> {
  private Set<String> allowedValues;

  @Override
  public void initialize(EnumValueMatch annotation) {
    allowedValues =
        Arrays.stream(annotation.enumClass().getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.toSet());
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return false;
    }

    if (!allowedValues.contains(value)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "Invalid enum value! Available values: " + allowedValues)
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
