package io.promptshield.core;

/**
 * A single detection signal emitted by one DetectionModule.
 */
public class DetectionSignal {

    private final String source;      // which module produced this
    private final double score;       // 0.0 – 1.0  (higher = more suspicious)
    private final String reason;      // human-readable explanation
    private final boolean triggered;  // convenience flag: score > module threshold

    public DetectionSignal(String source, double score, String reason, boolean triggered) {
        this.source    = source;
        this.score     = Math.min(1.0, Math.max(0.0, score));
        this.reason    = reason;
        this.triggered = triggered;
    }

    public String  getSource()    { return source; }
    public double  getScore()     { return score; }
    public String  getReason()    { return reason; }
    public boolean isTriggered()  { return triggered; }

    @Override
    public String toString() {
        return String.format("DetectionSignal{source='%s', score=%.3f, triggered=%s, reason='%s'}",
                source, score, triggered, reason);
    }
}
