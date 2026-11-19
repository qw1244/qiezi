package com.cms.video.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cms")
public class CmsProps {

    private Jwt jwt = new Jwt();
    private Upload upload = new Upload();

    @Data
    public static class Jwt {
        private String secret;
        private int userTtlHours = 168;
        private int adminTtlHours = 24;
    }

    @Data
    public static class Upload {
        private String queueKey = "cms:up:queue";
        private int workerThreads = 2;
    }
}
