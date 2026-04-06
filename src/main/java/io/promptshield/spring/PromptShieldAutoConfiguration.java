package io.promptshield.spring;

import io.promptshield.core.PromptShield;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration.
 *
 * Registers a {@link PromptShield} bean when:
 *   - Spring Boot is on the classpath
 *   - promptshield.enabled=true (default)
 *   - No existing PromptShield bean is present (user can override)
 */
@AutoConfiguration
@EnableConfigurationProperties(PromptShieldProperties.class)
@ConditionalOnProperty(prefix = "promptshield", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PromptShieldAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PromptShield promptShield(PromptShieldProperties props) {
        return PromptShield.builder()
                .configSource(props.getConfigSource())
                .injectionThreshold(props.getInjectionThreshold())
                .similarityThreshold(props.getSimilarityThreshold())
                .driftThreshold(props.getDriftThreshold())
                .hotReloadInterval(props.getHotReloadInterval())
                .build();
    }
}
