package com.bamdoliro.maru.infrastructure.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;

@RequiredArgsConstructor
@Converter
public class LongEncryptedConverter implements AttributeConverter<Long, String> {

    private final StringEncryptor encryptor;

    @Override
    public String convertToDatabaseColumn(Long attribute) {
        if (attribute == null) {
            return null;
        }
        return encryptor.encrypt(attribute.toString());
    }

    @Override
    public Long convertToEntityAttribute(String dbData) {
        if(dbData == null) {
            return null;
        }
        return Long.valueOf(encryptor.decrypt(dbData));
    }

}
