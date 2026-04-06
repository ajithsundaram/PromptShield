package io.promptshield.embedding;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Lightweight in-memory TF-IDF style vectorizer.
 *
 * Usage:
 *   1. Call fit(corpus) once to build the vocabulary.
 *   2. Call vectorize(text) to get a sparse double[] over the vocabulary.
 *   3. Use CosineSimilarity.compute() to compare vectors.
 *
 * No external ML libraries required — pure Java.
 */
public class TextVectorizer {

    private static final Pattern TOKEN = Pattern.compile("[a-zA-Z']+");

    /** Stop-words to strip before vectorising. */
    private static final Set<String> STOP_WORDS = Set.of(
            "a","an","the","and","or","but","in","on","at","to","for",
            "of","with","by","from","is","are","was","were","be","been",
            "have","has","had","do","does","did","will","would","should",
            "could","may","might","shall","can","not","no","i","you","we",
            "they","he","she","it","me","him","her","us","them","my","your",
            "our","their","this","that","these","those","as","so","if","then"
    );

    private final Map<String, Integer> vocabulary = new LinkedHashMap<>();
    private boolean fitted = false;

    /**
     * Build vocabulary from a collection of sentences.
     * Call this once with all known attack texts + a sample of benign inputs.
     */
    public synchronized void fit(Collection<String> corpus) {
        vocabulary.clear();
        int idx = 0;
        for (String doc : corpus) {
            for (String token : tokenize(doc)) {
                if (!vocabulary.containsKey(token)) {
                    vocabulary.put(token, idx++);
                }
            }
        }
        fitted = true;
    }

    /**
     * Convert text to a TF vector aligned with the fitted vocabulary.
     * Unknown tokens are ignored (open-vocabulary handled via overlap).
     */
    public double[] vectorize(String text) {
        if (!fitted) throw new IllegalStateException("Call fit() before vectorize()");
        double[] vec = new double[vocabulary.size()];
        List<String> tokens = tokenize(text);
        if (tokens.isEmpty()) return vec;
        for (String token : tokens) {
            Integer idx = vocabulary.get(token);
            if (idx != null) vec[idx] += 1.0;
        }
        // L2-normalise
        double norm = 0;
        for (double v : vec) norm += v * v;
        norm = Math.sqrt(norm);
        if (norm > 0) for (int i = 0; i < vec.length; i++) vec[i] /= norm;
        return vec;
    }

    public int vocabularySize() { return vocabulary.size(); }

    // -------------------------------------------------------------------------

    static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        var m = TOKEN.matcher(text.toLowerCase());
        while (m.find()) {
            String tok = m.group();
            if (tok.length() > 1 && !STOP_WORDS.contains(tok)) {
                tokens.add(tok);
            }
        }
        return tokens;
    }
}
