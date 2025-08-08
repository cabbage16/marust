package com.bamdoliro.maru.domain.form.domain.value;

import com.bamdoliro.maru.infrastructure.persistence.converter.StringEncryptedConverter;
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
public class PhoneNumber {

    @Convert(converter = StringEncryptedConverter.class)
    private String value;

    @Override
    public String toString() {
        if (value == null) {
            return "";
        }
        if (value.length() == 10) {
            return value.substring(0, 3) + "-" + value.substring(3, 6) + "-" + value.substring(6);
        } else if (value.length() == 11) {
            return value.substring(0, 3) + "-" + value.substring(3, 7) + "-" + value.substring(7);
        }

        return value;
    }
}
