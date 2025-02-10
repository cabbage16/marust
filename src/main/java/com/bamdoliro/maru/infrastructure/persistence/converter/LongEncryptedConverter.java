package com.bamdoliro.maru.infrastructure.persistence.converter;

import jakarta.persistence.Converter;
import org.jasypt.encryption.StringEncryptor;

@Converter
public class LongEncryptedConverter extends GenericEncryptedConverter<Long> {

    public LongEncryptedConverter(StringEncryptor encryptor) {
        super(encryptor, Long::valueOf);
    }
}
