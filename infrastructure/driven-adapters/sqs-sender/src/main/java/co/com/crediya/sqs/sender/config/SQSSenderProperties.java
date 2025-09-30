package co.com.crediya.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs")
public record SQSSenderProperties(
     String region,
     String queueUrl,
     String queueUrl2,
     String queueUrl3,
     String endpoint){
}
