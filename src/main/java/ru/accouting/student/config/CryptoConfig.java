package ru.accouting.student.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import ru.accouting.student.security.AesGcmEncryptor;
import ru.accouting.student.security.JpaCryptoConverter;

import jakarta.annotation.PostConstruct;
import java.util.Base64;

@Configuration
public class CryptoConfig {

    @Value("${app.crypto.key}")
    private String base64Key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        JpaCryptoConverter.setEncryptor(new AesGcmEncryptor(keyBytes));
    }
}