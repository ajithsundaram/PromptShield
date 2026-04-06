package io.promptshield.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleConfig {
    private List<Rule> rules;
    private List<RuleCombination> combinations;

    @JsonProperty("attack_embeddings")
    private List<String> attackEmbeddings;

    public List<Rule>            getRules()            { return rules; }
    public List<RuleCombination> getCombinations()     { return combinations; }
    public List<String>          getAttackEmbeddings() { return attackEmbeddings; }

    public void setRules(List<Rule> rules)                        { this.rules = rules; }
    public void setCombinations(List<RuleCombination> comb)       { this.combinations = comb; }
    public void setAttackEmbeddings(List<String> attackEmbeddings){ this.attackEmbeddings = attackEmbeddings; }
}
