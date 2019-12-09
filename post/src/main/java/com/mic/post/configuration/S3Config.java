package com.mic.post.configuration;

import com.mic.s3client.AmazonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Value("${amazonProperties.region}")
    private String region;
    @Value("${amazonProperties.bucketName}")
    private String bucketName;

    @Bean
    public AmazonClient amazonClient(){
        return new AmazonClient(region, bucketName);
    }
}
