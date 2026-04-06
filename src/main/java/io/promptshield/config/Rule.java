package io.promptshield.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Rule {
    private String name;
    private String intent;
    private List<String> patterns;

    public String       getName()    { return name; }
    public String       getIntent()  { return intent; }
    public List<String> getPatterns(){ return patterns; }

    public void setName(String name)              { this.name = name; }
    public void setIntent(String intent)          { this.intent = intent; }
    public void setPatterns(List<String> patterns){ this.patterns = patterns; }
}
