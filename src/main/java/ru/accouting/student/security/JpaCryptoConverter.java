package ru.accouting.student.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Setter;

@Converter
@Setter
public class JpaCryptoConverter implements AttributeConverter<String, String> {

    @Setter
    private static AesGcmEncryptor encryptor;

    // Срабатывает автоматически при сохранении сущности в базу данных
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return encryptor.encrypt(attribute);
    }

    // Срабатывает автоматически при чтении данных из базы
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return encryptor.decrypt(dbData);
    }
}