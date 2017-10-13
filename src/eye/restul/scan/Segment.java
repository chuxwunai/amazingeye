package eye.restul.scan;

import java.util.regex.Pattern;

public class Segment {
    
    private Pattern pattern;
    
    private String source;
    
    private String variableName;

    public Pattern getPattern() {
        return pattern;
    }

    public String getSource() {
        return source;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

}
