package io.promptshield.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleCombination {
    /** Intents that must ALL appear for this combination to fire. */
    @JsonProperty("if")
    private List<String> ifIntents;
    private double score;
    private String reason;

    public List<String> getIfIntents() { return ifIntents; }
    public double       getScore()     { return score; }
    public String       getReason()    { return reason; }

    public void setIfIntents(List<String> ifIntents) { this.ifIntents = ifIntents; }
    public void setScore(double score)               { this.score = score; }
    public void setReason(String reason)             { this.reason = reason; }
}
