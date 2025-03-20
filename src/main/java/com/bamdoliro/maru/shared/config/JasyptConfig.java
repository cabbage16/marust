package com.bamdoliro.maru.shared.config;

import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
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
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(key);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");

        config.setSaltGenerator(new StringFixedSaltGenerator(salt));
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        encryptor.setConfig(config);
        return encryptor;
    }

}
