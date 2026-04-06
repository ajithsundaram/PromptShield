package io.promptshield.embedding;

import java.util.List;

/**
 * In-memory store of pre-computed attack-pattern vectors.
 *
 * Rebuilt whenever the config is hot-reloaded.
 */
public class VectorStore {

    private final TextVectorizer vectorizer;
    private volatile double[][] attackVectors;
    private volatile List<String> attackTexts;

    public VectorStore(List<String> attackTexts) {
        this.vectorizer  = new TextVectorizer();
        rebuild(attackTexts);
    }

    /** Re-fit the vectorizer and recompute all attack vectors. */
    public synchronized void rebuild(List<String> texts) {
        this.attackTexts = List.copyOf(texts);
        vectorizer.fit(texts);
        this.attackVectors = texts.stream()
                .map(vectorizer::vectorize)
                .toArray(double[][]::new);
    }

    /**
     * Returns the maximum cosine similarity between the input text
     * and any known attack-pattern text.
     */
    public double maxSimilarityTo(String input) {
        double[] inputVec = vectorizer.vectorize(input);
        return CosineSimilarity.maxSimilarity(inputVec, attackVectors);
    }

    /**
     * Returns the cosine similarity between two plain texts
     * (useful for context-drift detection).
     */
    public double similarity(String a, String b) {
        double[] va = vectorizer.vectorize(a);
        double[] vb = vectorizer.vectorize(b);
        return CosineSimilarity.compute(va, vb);
    }

    public int attackPatternCount() { return attackTexts.size(); }
}
