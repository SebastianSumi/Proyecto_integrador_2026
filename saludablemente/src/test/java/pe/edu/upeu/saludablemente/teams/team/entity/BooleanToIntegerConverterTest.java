package pe.edu.upeu.saludablemente.teams.team.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BooleanToIntegerConverterTest {

    private final BooleanToIntegerConverter converter = new BooleanToIntegerConverter();

    @Test
    void convertsBooleanValuesToOracleNumberValues() {
        assertThat(converter.convertToDatabaseColumn(true)).isEqualTo(1);
        assertThat(converter.convertToDatabaseColumn(false)).isZero();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertsOracleNumberValuesToBooleanValues() {
        assertThat(converter.convertToEntityAttribute(1)).isTrue();
        assertThat(converter.convertToEntityAttribute(0)).isFalse();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void rejectsUnexpectedDatabaseValues() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute(2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expected database boolean value 0 or 1 but got: 2");
    }
}