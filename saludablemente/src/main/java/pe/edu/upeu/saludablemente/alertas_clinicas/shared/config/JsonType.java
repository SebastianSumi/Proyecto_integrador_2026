package pe.edu.upeu.saludablemente.alertas_clinicas.shared.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;
import java.util.Objects;

public class JsonType implements UserType<Object> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public int getSqlType() {
        return Types.CLOB; // Oracle usa CLOB para JSON
    }

    @Override
    public Class<Object> returnedClass() {
        return Object.class;
    }

    @Override
    public boolean equals(Object x, Object y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Object x) {
        return Objects.hashCode(x);
    }

    @Override
    public Object nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String json = rs.getString(position);
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            throw new HibernateException("Error al deserializar JSON", e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.CLOB);
        } else {
            try {
                String json = OBJECT_MAPPER.writeValueAsString(value);
                st.setString(index, json);
            } catch (JsonProcessingException e) {
                throw new HibernateException("Error al serializar JSON", e);
            }
        }
    }

    @Override
    public Object deepCopy(Object value) {
        // Si es inmutable, retornar el mismo objeto. Si no, implementar copia profunda.
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) {
        return cached;
    }
}