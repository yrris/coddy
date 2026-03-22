package com.yrris.coddy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.deploy")
public class AppDeployProperties {

    private String host = "http://localhost:8765";

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
