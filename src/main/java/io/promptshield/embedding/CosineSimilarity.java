package io.promptshield.embedding;

/**
 * Utility: computes cosine similarity between two L2-normalised vectors.
 * Since TextVectorizer already L2-normalises, this is just a dot product.
 */
public final class CosineSimilarity {

    private CosineSimilarity() {}

    /**
     * Returns a value in [0, 1] — 1.0 means identical direction.
     * Both vectors must have the same length.
     */
    public static double compute(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "Vector length mismatch: " + a.length + " vs " + b.length);
        }
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0 ? 0.0 : dot / denom;
    }

    /**
     * Returns the maximum cosine similarity of {@code input} against any vector in {@code corpus}.
     */
    public static double maxSimilarity(double[] input, double[][] corpus) {
        double max = 0.0;
        for (double[] ref : corpus) {
            max = Math.max(max, compute(input, ref));
        }
        return max;
    }
}
