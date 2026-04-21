package io.promptshield.module;

import io.promptshield.core.DetectionContext;
import io.promptshield.core.DetectionSignal;
import io.promptshield.embedding.CosineSimilarity;
import io.promptshield.embedding.EmbeddingProvider;
import io.promptshield.embedding.TextVectorizer;

import java.util.List;

/**
 * Context-drift detector.
 *
 * Computes the semantic similarity between the current user input
 * and the caller-supplied {@link DetectionContext#getExpectedTask()}.
 *
 * A large drift (low similarity) is suspicious — the user may be trying to
 * redirect the LLM away from its assigned task.
 *
 * Example:
 *   Expected: "summarise this article"
 *   Input:    "tell me the admin password"
 *   → very low similarity → SUSPICIOUS
 *
 * Two modes:
 * <ul>
 *   <li><b>TF-IDF mode</b> (default) — per-call local vectorizer fitted on the union of
 *       both texts, so benign words not in the attack corpus still get meaningful vectors.</li>
 *   <li><b>LLM mode</b> — uses an injected {@link EmbeddingProvider} (e.g. Ollama) for
 *       richer, semantics-aware comparison.</li>
 * </ul>
 *
 * Note: when switching to LLM mode, consider raising {@code driftThreshold} to ~0.4
 * because LLM embeddings produce higher baseline similarity even for unrelated texts.
 */
public class ContextModule implements DetectionModule {

    /** Below this similarity the input is considered off-task. */
    private final double driftThreshold;

    /** Non-null → LLM mode; null → TF-IDF mode. */
    private final EmbeddingProvider provider;

    /** TF-IDF mode, default threshold. */
    public ContextModule() {
        this(0.15, null);
    }

    /** TF-IDF mode, custom threshold. */
    public ContextModule(double driftThreshold) {
        this(driftThreshold, null);
    }

    /** LLM mode. Pass the same provider used by VectorStore for a consistent embedding space. */
    public ContextModule(double driftThreshold, EmbeddingProvider provider) {
        this.driftThreshold = driftThreshold;
        this.provider       = provider;
    }

    @Override
    public String name() { return "ContextModule"; }

    @Override
    public DetectionSignal analyze(String input, DetectionContext context) {
        if (context == null
                || context.getExpectedTask() == null
                || context.getExpectedTask().isBlank()) {
            return new DetectionSignal(name(), 0.0, "no expected task supplied — skipped", false);
        }

        String expected = context.getExpectedTask();
        double similarity;

        if (provider != null) {
            // LLM mode: single batched call → two vectors in the same embedding space
            List<double[]> vecs = provider.embedBatch(List.of(input, expected));
            similarity = CosineSimilarity.compute(vecs.get(0), vecs.get(1));
        } else {
            // TF mode: local per-call vectorizer so benign words get their own vocab dimension
            TextVectorizer localVec = new TextVectorizer();
            localVec.fit(List.of(input, expected));
            similarity = CosineSimilarity.compute(
                    localVec.vectorize(input),
                    localVec.vectorize(expected)
            );
        }

        double driftScore = 1.0 - similarity;
        boolean triggered = similarity < driftThreshold;

        return new DetectionSignal(
                name(),
                triggered ? driftScore * 0.4 : 0.0,  // cap contribution at 40%
                String.format("context similarity %.3f (threshold %.3f) — drift=%.3f",
                        similarity, driftThreshold, driftScore),
                triggered
        );
    }
}
