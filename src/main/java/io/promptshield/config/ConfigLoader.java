package io.promptshield.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Loads RuleConfig from a classpath resource, file path, or URL.
 * Supports optional background hot-reload at a configurable interval.
 */
public class ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DEFAULT_RULES = "/default-rules.json";

    private final String configSource;          // classpath:/..., file:/..., or http(s)://...
    private final AtomicReference<RuleConfig> config = new AtomicReference<>();
    private ScheduledExecutorService scheduler;

    public ConfigLoader(String configSource) {
        this.configSource = configSource;
        this.config.set(load());
    }

    /** Start hot-reload; refreshes config every {@code intervalSeconds}. */
    public void enableHotReload(long intervalSeconds) {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "promptshield-config-reload");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                RuleConfig fresh = load();
                config.set(fresh);
                log.info("PromptShield: rules reloaded from {}", configSource);
            } catch (Exception e) {
                log.warn("PromptShield: failed to reload config — keeping current rules", e);
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    public void shutdown() {
        if (scheduler != null) scheduler.shutdownNow();
    }

    public RuleConfig get() {
        return config.get();
    }

    // -------------------------------------------------------------------------

    private RuleConfig load() {
        try {
            if (configSource == null || configSource.isBlank()) {
                return loadFromClasspath(DEFAULT_RULES);
            }
            if (configSource.startsWith("classpath:")) {
                return loadFromClasspath(configSource.substring("classpath:".length()));
            }
            if (configSource.startsWith("http://") || configSource.startsWith("https://")) {
                return loadFromUrl(configSource);
            }
            // treat as file path
            return loadFromFile(configSource);
        } catch (Exception e) {
            log.error("PromptShield: could not load config from '{}', falling back to defaults", configSource, e);
            try { return loadFromClasspath(DEFAULT_RULES); } catch (Exception ex) {
                throw new IllegalStateException("PromptShield: could not load default rules", ex);
            }
        }
    }

    private RuleConfig loadFromClasspath(String path) throws Exception {
        try (InputStream is = ConfigLoader.class.getResourceAsStream(path)) {
            if (is == null) throw new IllegalArgumentException("Classpath resource not found: " + path);
            return MAPPER.readValue(is, RuleConfig.class);
        }
    }

    private RuleConfig loadFromFile(String path) throws Exception {
        return MAPPER.readValue(new File(path), RuleConfig.class);
    }

    private RuleConfig loadFromUrl(String url) throws Exception {
        return MAPPER.readValue(URI.create(url).toURL(), RuleConfig.class);
    }
}
