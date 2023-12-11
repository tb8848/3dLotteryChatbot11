package com.config;

import com.beans.Dictionary;
import com.service.DictionaryService;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioClientConfig {
    @Autowired
    private DictionaryService dictionaryService;

    @Bean
    public MinioClient minioClient() {
        Dictionary url = dictionaryService.getDicByCode("system", "MinioUrl");
        Dictionary accessKey = dictionaryService.getDicByCode("system", "MinioAccessKey");
        Dictionary secretKey = dictionaryService.getDicByCode("system", "MinioSecretKey");
        return MinioClient.builder().credentials(accessKey.getValue(), secretKey.getValue()).endpoint(url.getValue()).build();
    }
}
