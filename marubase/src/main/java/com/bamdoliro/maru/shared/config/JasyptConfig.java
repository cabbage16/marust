package com.bamdoliro.maru.shared.config;

import lombok.RequiredArgsConstructor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.salt.StringFixedSaltGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class JasyptConfig {

    @Value("${spring.jasypt.encryptor.key}")
    private String key;

    @Value("${spring.jasypt.encryptor.salt}")
    private String salt;

    @Bean
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setProvider(new BouncyCastleProvider());
        encryptor.setPassword(key);
        encryptor.setAlgorithm("PBEWithSHA256And256BitAES-CBC-BC");
        encryptor.setKeyObtentionIterations(1000);
        encryptor.setPoolSize(1);

        encryptor.setSaltGenerator(new StringFixedSaltGenerator(salt));
        return encryptor;
    }

}
