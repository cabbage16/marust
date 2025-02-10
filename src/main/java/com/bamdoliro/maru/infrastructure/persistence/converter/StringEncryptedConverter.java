package com.bamdoliro.maru.infrastructure.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;

@RequiredArgsConstructor
@Converter
public class StringEncryptedConverter implements AttributeConverter<String, String> {

    private final StringEncryptor encryptor;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return encryptor.encrypt(attribute);
    }
    @Override
    public String convertToEntityAttribute(String dbData) {
        return encryptor.decrypt(dbData);
    }
}