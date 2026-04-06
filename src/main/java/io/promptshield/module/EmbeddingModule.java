package io.promptshield.module;

import io.promptshield.core.DetectionContext;
import io.promptshield.core.DetectionSignal;
import io.promptshield.embedding.VectorStore;

/**
 * Semantic similarity detection using in-memory TF-IDF cosine similarity.
 *
 * Compares the user prompt against a library of known attack-pattern sentences.
 * Catches paraphrased attacks that keyword rules miss.
 *
 * Example:
 *   "avoid following earlier rules"   →  high similarity to "ignore previous instructions"
 *   "skip your system guidelines"     →  high similarity to "bypass system rules"
 */
public class EmbeddingModule implements DetectionModule {

    /** Threshold above which a match counts as suspicious. Tune per deployment. */
    private final double similarityThreshold;
    private final VectorStore vectorStore;

    public EmbeddingModule(VectorStore vectorStore) {
        this(vectorStore, 0.65);
    }

    public EmbeddingModule(VectorStore vectorStore, double similarityThreshold) {
        this.vectorStore          = vectorStore;
        this.similarityThreshold  = similarityThreshold;
    }

    @Override
    public String name() { return "EmbeddingModule"; }

    @Override
    public DetectionSignal analyze(String input, DetectionContext context) {
        if (input == null || input.isBlank()) {
            return new DetectionSignal(name(), 0.0, "empty input", false);
        }

        double similarity = vectorStore.maxSimilarityTo(input);

        if (similarity >= similarityThreshold) {
            return new DetectionSignal(
                    name(),
                    Math.min(1.0, similarity),
                    String.format("semantic similarity %.3f >= threshold %.3f", similarity, similarityThreshold),
                    true
            );
        }

        // Return 0.0 score when below threshold — we don't want sub-threshold
        // similarity to pollute the aggregated score and cause false positives.
        return new DetectionSignal(
                name(),
                0.0,
                String.format("semantic similarity %.3f < threshold %.3f", similarity, similarityThreshold),
                false
        );
    }
}
