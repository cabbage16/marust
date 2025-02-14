package com.bamdoliro.maru.infrastructure.persistence.converter.mapper;

@FunctionalInterface
public interface DecryptionMapper<T> {
    T map(String value);
}
