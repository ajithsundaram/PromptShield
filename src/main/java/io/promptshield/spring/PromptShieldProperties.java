package io.promptshield.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot configuration properties for PromptShield.
 *
 * application.yml example:
 * <pre>
 * promptshield:
 *   enabled: true
 *   config-source: "classpath:/my-rules.json"
 *   injection-threshold: 0.55
 *   similarity-threshold: 0.65
 *   drift-threshold: 0.15
 *   hot-reload-interval: 300
 * </pre>
 */
@ConfigurationProperties(prefix = "promptshield")
public class PromptShieldProperties {

    /** Enable or disable the library entirely. */
    private boolean enabled = true;

    /**
     * Path to the rules JSON file.
     * Accepts: "classpath:/rules.json", "/absolute/path.json", "https://..."
     * Leave blank to use the bundled defaults.
     */
    private String configSource = "";

    /** Score threshold above which a prompt is classified as INJECTION. */
    private double injectionThreshold = 0.55;

    /** Cosine similarity threshold for EmbeddingModule. */
    private double similarityThreshold = 0.65;

    /** Context drift threshold for ContextModule. */
    private double driftThreshold = 0.15;

    /** Reload rules every N seconds (0 = disabled). */
    private long hotReloadInterval = 0;

    /**
     * Base URL of a local Ollama instance for LLM-backed embeddings.
     * Leave blank to use the built-in TF-IDF vectorizer instead.
     * Example: "http://localhost:11434"
     */
    private String ollamaUrl = "";

    /**
     * Ollama embedding model to use (only applies when ollama-url is set).
     * Run "ollama pull nomic-embed-text" before starting the application.
     */
    private String ollamaModel = "nomic-embed-text";

    // ---- getters / setters ----

    public boolean isEnabled()             { return enabled; }
    public void setEnabled(boolean v)      { this.enabled = v; }

    public String getConfigSource()           { return configSource; }
    public void setConfigSource(String v)     { this.configSource = v; }

    public double getInjectionThreshold()     { return injectionThreshold; }
    public void setInjectionThreshold(double v){ this.injectionThreshold = v; }

    public double getSimilarityThreshold()    { return similarityThreshold; }
    public void setSimilarityThreshold(double v){ this.similarityThreshold = v; }

    public double getDriftThreshold()         { return driftThreshold; }
    public void setDriftThreshold(double v)   { this.driftThreshold = v; }

    public long getHotReloadInterval()        { return hotReloadInterval; }
    public void setHotReloadInterval(long v)  { this.hotReloadInterval = v; }

    public String getOllamaUrl()              { return ollamaUrl; }
    public void setOllamaUrl(String v)        { this.ollamaUrl = v; }

    public String getOllamaModel()            { return ollamaModel; }
    public void setOllamaModel(String v)      { this.ollamaModel = v; }
}
