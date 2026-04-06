package io.promptshield.module;

import io.promptshield.core.DetectionContext;
import io.promptshield.core.DetectionSignal;
import io.promptshield.embedding.CosineSimilarity;
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
 * Note: uses a local, per-call TextVectorizer fit on the union of the two
 * texts so that comparison uses a shared vocabulary — benign words not in the
 * attack corpus still get meaningful vectors.
 */
public class ContextModule implements DetectionModule {

    /** Below this similarity the input is considered off-task. */
    private final double driftThreshold;

    public ContextModule() {
        this(0.15);
    }

    public ContextModule(double driftThreshold) {
        this.driftThreshold = driftThreshold;
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

        // Fit a local vectorizer on the union of both texts so benign words
        // get their own vocabulary dimension, not just attack-corpus words.
        TextVectorizer localVec = new TextVectorizer();
        localVec.fit(List.of(input, expected));

        double[] inputVec    = localVec.vectorize(input);
        double[] expectedVec = localVec.vectorize(expected);

        double similarity = CosineSimilarity.compute(inputVec, expectedVec);
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
