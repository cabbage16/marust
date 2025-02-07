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
        System.out.println("attribute = " + attribute);
        System.out.println("encryptor = " + encryptor.encrypt(attribute));
        return encryptor.encrypt(attribute);
    }
    @Override
    public String convertToEntityAttribute(String dbData) {
        System.out.println("dbData = " + dbData);
        System.out.println("decryptor = " + encryptor.decrypt(dbData));
        return encryptor.decrypt(dbData);
    }
}

// l6pQc06QoRgqHM80oHb8YB2MOqIhzj0w
// 8Ec4dp03bCD47I0DZai9MiOnynlmZ7OW