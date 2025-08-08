package com.bamdoliro.maru.infrastructure.persistence.converter;

import org.jasypt.encryption.StringEncryptor;

public class StringEncryptedConverter extends GenericEncryptedConverter<String> {

    public StringEncryptedConverter(StringEncryptor encryptor) {
        super(encryptor, s -> s);
    }
}