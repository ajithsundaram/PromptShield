package io.promptshield.embedding;

import java.util.List;

/**
 * In-memory store of pre-computed attack-pattern vectors.
 *
 * Supports two embedding backends:
 * <ul>
 *   <li>TF-IDF (default) — no external dependencies, uses {@link TextVectorizer}</li>
 *   <li>LLM-backed — richer semantics via an {@link EmbeddingProvider} (e.g. Ollama)</li>
 * </ul>
 *
 * Rebuilt whenever the config is hot-reloaded.
 */
public class VectorStore {

    private final TextVectorizer vectorizer;   // non-null only in TF mode
    private final EmbeddingProvider provider;  // non-null only in LLM mode

    private volatile double[][] attackVectors;
    private volatile List<String> attackTexts;

    /** TF-IDF backed (default, no external dependencies). */
    public VectorStore(List<String> attackTexts) {
        this.vectorizer = new TextVectorizer();
        this.provider   = null;
        rebuild(attackTexts);
    }

    /**
     * LLM-backed. Calls the embedding API for all attack texts at startup
     * and again on each hot-reload.
     */
    public VectorStore(List<String> attackTexts, EmbeddingProvider provider) {
        this.vectorizer = null;
        this.provider   = provider;
        rebuild(attackTexts);
    }

    /** Re-compute all attack vectors (called on construction and hot-reload). */
    public synchronized void rebuild(List<String> texts) {
        this.attackTexts = List.copyOf(texts);
        if (provider != null) {
            List<double[]> vecs = provider.embedBatch(texts);
            this.attackVectors  = vecs.toArray(new double[0][]);
        } else {
            vectorizer.fit(texts);
            this.attackVectors = texts.stream()
                    .map(vectorizer::vectorize)
                    .toArray(double[][]::new);
        }
    }

    /**
     * Returns the maximum cosine similarity between the input text
     * and any known attack-pattern text.
     */
    public double maxSimilarityTo(String input) {
        double[] inputVec = embedOne(input);
        return CosineSimilarity.maxSimilarity(inputVec, attackVectors);
    }

    /**
     * Returns the cosine similarity between two plain texts
     * (useful for context-drift detection).
     */
    public double similarity(String a, String b) {
        double[] va = embedOne(a);
        double[] vb = embedOne(b);
        return CosineSimilarity.compute(va, vb);
    }

    private double[] embedOne(String text) {
        return provider != null ? provider.embed(text) : vectorizer.vectorize(text);
    }

    public int attackPatternCount() { return attackTexts.size(); }
}
