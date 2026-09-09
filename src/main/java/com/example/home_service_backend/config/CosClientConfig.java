package com.example.home_service_backend.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosClientConfig {
    @Bean(destroyMethod = "shutdown")
    COSClient tencentCosClient(CosProperties properties) {
        if (!properties.configured()) {
            return null;
        }
        COSCredentials credentials = new BasicCOSCredentials(properties.secretId().trim(), properties.secretKey().trim());
        ClientConfig clientConfig = new ClientConfig(new Region(properties.region()));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        clientConfig.setConnectionTimeout((int) properties.connectTimeout().toMillis());
        clientConfig.setSocketTimeout((int) properties.readTimeout().toMillis());
        clientConfig.setMaxErrorRetry(properties.maxRetries());
        return new COSClient(credentials, clientConfig);
    }
}
