package com.bamdoliro.maru.infrastructure.persistence.converter;

import jakarta.persistence.Converter;
import org.jasypt.encryption.StringEncryptor;

import java.time.LocalDate;

@Converter
public class LocalDateEncryptedConverter extends GenericEncryptedConverter<LocalDate> {

    public LocalDateEncryptedConverter(StringEncryptor encryptor) {
        super(encryptor, LocalDate::parse);
    }
}
