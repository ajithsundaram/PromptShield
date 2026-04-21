package io.promptshield.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * EmbeddingProvider backed by a local Ollama instance.
 *
 * Prerequisites:
 *   1. Ollama installed and running:  ollama serve
 *   2. Embedding model pulled:        ollama pull nomic-embed-text
 *
 * Calls POST /api/embed — supported by Ollama >= 0.1.26.
 * Sends the full batch in a single HTTP request for efficiency.
 *
 * <h3>Quick start (Spring Boot):</h3>
 * <pre>
 * # application.yml
 * promptshield:
 *   ollama-url: http://localhost:11434
 *   ollama-model: nomic-embed-text
 * </pre>
 *
 * <h3>Quick start (programmatic):</h3>
 * <pre>{@code
 * PromptShield shield = PromptShield.builder()
 *     .ollamaEmbedding()   // default URL + model
 *     .build();
 * }</pre>
 */
public class OllamaEmbeddingProvider implements EmbeddingProvider {

    private static final Logger log = LoggerFactory.getLogger(OllamaEmbeddingProvider.class);

    public static final String DEFAULT_BASE_URL = "http://localhost:11434";
    public static final String DEFAULT_MODEL    = "nomic-embed-text";

    private final String baseUrl;
    private final String model;
    private final HttpClient http;
    private final ObjectMapper mapper;

    /** Connects to http://localhost:11434 using nomic-embed-text. */
    public OllamaEmbeddingProvider() {
        this(DEFAULT_BASE_URL, DEFAULT_MODEL);
    }

    public OllamaEmbeddingProvider(String baseUrl, String model) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.model   = model;
        this.http    = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper  = new ObjectMapper();
        log.info("OllamaEmbeddingProvider configured: url={} model={}", this.baseUrl, this.model);
    }

    @Override
    public double[] embed(String text) {
        return embedBatch(List.of(text)).get(0);
    }

    /**
     * Sends all texts in a single POST /api/embed request.
     *
     * Request body:
     * <pre>
     * { "model": "nomic-embed-text", "input": ["text1", "text2", ...] }
     * </pre>
     *
     * Response body:
     * <pre>
     * { "embeddings": [[0.12, -0.34, ...], [0.56, 0.78, ...]] }
     * </pre>
     */
    @Override
    public List<double[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        try {
            ObjectNode body    = mapper.createObjectNode();
            body.put("model", model);
            ArrayNode inputArr = body.putArray("input");
            texts.forEach(inputArr::add);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/embed"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            log.debug("Ollama embed: {} text(s) via {}", texts.size(), request.uri());

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Ollama returned HTTP " + response.statusCode() + ": " + response.body());
            }

            JsonNode root       = mapper.readTree(response.body());
            JsonNode embeddings = root.get("embeddings");

            if (embeddings == null || !embeddings.isArray()) {
                throw new RuntimeException("Unexpected Ollama response (no 'embeddings' array): " + response.body());
            }

            if (embeddings.size() != texts.size()) {
                throw new RuntimeException(
                        "Ollama returned " + embeddings.size() + " embeddings for " + texts.size() + " inputs");
            }

            List<double[]> result = new ArrayList<>(embeddings.size());
            for (JsonNode embNode : embeddings) {
                double[] vec = new double[embNode.size()];
                for (int i = 0; i < embNode.size(); i++) {
                    vec[i] = embNode.get(i).asDouble();
                }
                result.add(vec);
            }
            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ollama embedding call interrupted", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Ollama /api/embed", e);
        }
    }

    public String getBaseUrl() { return baseUrl; }
    public String getModel()   { return model; }
}
