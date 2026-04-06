package io.promptshield.module;

import io.promptshield.config.ConfigLoader;
import io.promptshield.config.Rule;
import io.promptshield.config.RuleCombination;
import io.promptshield.config.RuleConfig;
import io.promptshield.core.DetectionContext;
import io.promptshield.core.DetectionSignal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Keyword + combination rule engine loaded from JSON config.
 *
 * Algorithm:
 *  1. Tokenise input to lower-case words.
 *  2. Match each rule's patterns against tokens → collect active intents.
 *  3. For each combination where ALL required intents are active, add its score.
 *  4. Return the highest combined score as the signal.
 */
public class RuleBasedModule implements DetectionModule {

    private final ConfigLoader configLoader;

    public RuleBasedModule(ConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    @Override
    public String name() { return "RuleBasedModule"; }

    @Override
    public DetectionSignal analyze(String input, DetectionContext context) {
        RuleConfig cfg = configLoader.get();
        if (cfg == null || cfg.getRules() == null) {
            return new DetectionSignal(name(), 0.0, "no rules loaded", false);
        }

        String lower = input.toLowerCase();

        // Step 1: Collect active intents
        Set<String> activeIntents = new HashSet<>();
        for (Rule rule : cfg.getRules()) {
            if (rule.getPatterns() == null) continue;
            for (String pattern : rule.getPatterns()) {
                if (lower.contains(pattern.toLowerCase())) {
                    activeIntents.add(rule.getIntent());
                    break;
                }
            }
        }

        if (activeIntents.isEmpty()) {
            return new DetectionSignal(name(), 0.0, "no patterns matched", false);
        }

        // Step 2: Evaluate combinations
        double maxScore = 0.0;
        String matchedReason = "individual intent(s) matched: " + activeIntents;

        List<RuleCombination> combinations = cfg.getCombinations();
        if (combinations != null) {
            for (RuleCombination combo : combinations) {
                if (combo.getIfIntents() != null
                        && activeIntents.containsAll(combo.getIfIntents())) {
                    if (combo.getScore() > maxScore) {
                        maxScore      = combo.getScore();
                        matchedReason = combo.getReason();
                    }
                }
            }
        }

        // Partial match: if no combination fired but intents were found, give a base score
        if (maxScore == 0.0 && !activeIntents.isEmpty()) {
            maxScore = 0.3;
        }

        return new DetectionSignal(name(), maxScore, matchedReason, maxScore >= 0.5);
    }
}
