package com.bamdoliro.maru.domain.form.domain.value;

import com.bamdoliro.maru.infrastructure.persistence.converter.StringEncryptedConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Embeddable
public class Address {

    @Convert(converter = StringEncryptedConverter.class)
    @Column(nullable = false)
    private String zoneCode;

    @Convert(converter = StringEncryptedConverter.class)
    @Column(nullable = false)
    private String address;

    @Convert(converter = StringEncryptedConverter.class)
    @Column(nullable = false)
    private String detailAddress;

    @Override
    public String toString() {
        return String.format("(우 %s) %s %s", zoneCode, address, detailAddress);
    }
}
