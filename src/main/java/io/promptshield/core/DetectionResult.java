package io.promptshield.core;

import java.util.List;

/**
 * Aggregated result returned to the caller after all DetectionModules have run.
 */
public class DetectionResult {

    private final boolean injectionDetected;
    private final double  aggregatedScore;      // 0.0 – 1.0
    private final String  verdict;              // SAFE | SUSPICIOUS | INJECTION
    private final List<DetectionSignal> signals;

    public DetectionResult(double aggregatedScore,
                           List<DetectionSignal> signals,
                           double injectionThreshold) {
        this.aggregatedScore   = aggregatedScore;
        this.signals           = List.copyOf(signals);
        this.injectionDetected = aggregatedScore >= injectionThreshold;
        this.verdict = resolveVerdict(aggregatedScore, injectionThreshold);
    }

    private static String resolveVerdict(double score, double threshold) {
        if (score >= threshold)        return "INJECTION";
        if (score >= threshold * 0.6)  return "SUSPICIOUS";
        return "SAFE";
    }

    public boolean isInjectionDetected() { return injectionDetected; }
    public double  getAggregatedScore()  { return aggregatedScore; }
    public String  getVerdict()          { return verdict; }
    public List<DetectionSignal> getSignals() { return signals; }

    @Override
    public String toString() {
        return String.format("DetectionResult{verdict='%s', score=%.3f, signals=%d}",
                verdict, aggregatedScore, signals.size());
    }
}
