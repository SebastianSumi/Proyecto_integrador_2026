package pe.edu.upeu.saludablemente.teams.team.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanToIntegerConverter implements AttributeConverter<Boolean, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute ? 1 : 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Integer databaseValue) {
        if (databaseValue == null) {
            return null;
        }
        if (databaseValue == 0) {
            return false;
        }
        if (databaseValue == 1) {
            return true;
        }
        throw new IllegalArgumentException("Expected database boolean value 0 or 1 but got: " + databaseValue);
    }
}