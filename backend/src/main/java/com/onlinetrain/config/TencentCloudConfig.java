package com.onlinetrain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tencent")
public class TencentCloudConfig {

    private Cloud cloud = new Cloud();
    private Cos cos = new Cos();
    private Vod vod = new Vod();

    @Data
    public static class Cloud {
        private String secretId;
        private String secretKey;
    }

    @Data
    public static class Cos {
        private String region;
        private String bucket;
        private String cdnDomain;
    }

    @Data
    public static class Vod {
        private Integer subAppId = 0;
        private String region;
    }
}
