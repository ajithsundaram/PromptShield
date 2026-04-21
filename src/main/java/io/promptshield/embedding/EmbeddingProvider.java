package io.promptshield.embedding;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy interface for text-to-vector embedding.
 *
 * Implementations:
 *   - {@link OllamaEmbeddingProvider} — local Ollama model (e.g. nomic-embed-text)
 *   - Built-in TF-IDF path in {@link VectorStore} / {@link io.promptshield.module.ContextModule}
 *     (used when no provider is configured)
 *
 * Usage:
 * <pre>{@code
 * PromptShield shield = PromptShield.builder()
 *     .ollamaEmbedding()          // http://localhost:11434, nomic-embed-text
 *     .build();
 * }</pre>
 */
public interface EmbeddingProvider {

    /**
     * Embed a single text into a dense float vector.
     */
    double[] embed(String text);

    /**
     * Embed multiple texts in one call.
     * Default delegates to {@link #embed(String)} per item; implementations
     * should override to use a single batched API request.
     */
    default List<double[]> embedBatch(List<String> texts) {
        return texts.stream().map(this::embed).collect(Collectors.toList());
    }
}
