package com.bamdoliro.maru.infrastructure.persistence.converter;

import com.bamdoliro.maru.infrastructure.persistence.converter.mapper.DecryptionMapper;
import jakarta.persistence.AttributeConverter;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;

@RequiredArgsConstructor
public class GenericEncryptedConverter<T> implements AttributeConverter<T, String> {

    private final StringEncryptor encryptor;
    private final DecryptionMapper<T> decryptionMapper;

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (attribute == null) return null;
        return encryptor.encrypt(attribute.toString());
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return decryptionMapper.map(encryptor.decrypt(dbData));
    }
}
