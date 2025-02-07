package com.bamdoliro.maru.domain.form.domain.value;

import com.bamdoliro.maru.infrastructure.persistence.converter.StringEncryptedConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Embeddable
public class Teacher {

    @Convert(converter = StringEncryptedConverter.class)
    @Column(name = "teacher_name", nullable = true)
    private String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "teacher_phone_number", nullable = true)),
    })
    private PhoneNumber phoneNumber;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "teacher_mobile_phone_number", nullable = true)),
    })
    private PhoneNumber mobilePhoneNumber;
}
