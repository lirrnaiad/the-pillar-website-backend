package com.uep.pillar.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Cloudinary configuration.
 *
 * Provides a configured {@link Cloudinary} bean using credentials from application properties.
 */
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    /**
     * Default folder used for uploads when none is supplied explicitly.
     */
    @Value("${cloudinary.folder:pillar-uploads}")
    private String defaultFolder;

    @Bean
    @ConditionalOnProperty(name = "cloudinary.cloud-name")
    public Cloudinary cloudinary() {
        if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)) {
            throw new IllegalStateException("Cloudinary credentials are missing. Please set cloudinary.cloud-name, cloudinary.api-key, and cloudinary.api-secret.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        );
        return new Cloudinary(config);
    }

    public String getDefaultFolder() {
        return defaultFolder;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
