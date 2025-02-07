package com.bamdoliro.maru.infrastructure.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Converter
public class LocalDateEncryptedConverter implements AttributeConverter<LocalDate, String> {

    private final StringEncryptor encryptor;


    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        return encryptor.encrypt(attribute.toString());
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        return LocalDate.parse(encryptor.decrypt(dbData));
    }
}
