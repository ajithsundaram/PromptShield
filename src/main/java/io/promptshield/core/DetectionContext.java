package io.promptshield.core;

/**
 * Context supplied by the caller to enable context-aware detection.
 * e.g., the task the LLM was originally asked to do so we can detect semantic drift.
 */
public class DetectionContext {

    private final String expectedTask;
    private final String userId;
    private final String sessionId;

    private DetectionContext(Builder builder) {
        this.expectedTask = builder.expectedTask;
        this.userId = builder.userId;
        this.sessionId = builder.sessionId;
    }

    public String getExpectedTask() { return expectedTask; }
    public String getUserId()       { return userId; }
    public String getSessionId()    { return sessionId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String expectedTask = "";
        private String userId = "anonymous";
        private String sessionId = "default";

        public Builder expectedTask(String task)   { this.expectedTask = task;   return this; }
        public Builder userId(String userId)        { this.userId = userId;       return this; }
        public Builder sessionId(String sessionId)  { this.sessionId = sessionId; return this; }

        public DetectionContext build() { return new DetectionContext(this); }
    }
}
