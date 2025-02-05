package com.killiancorbel.realtimeapi.models;

import java.util.List;
import java.util.Map;

public class Topic {
    private List<String> beginner;
    private List<String> intermediate;
    private List<String> advanced;
    private List<String> expert;

    public List<String> getAdvanced() {
        return advanced;
    }
    public List<String> getBeginner() {
        return beginner;
    }
    public List<String> getExpert() {
        return expert;
    }
    public List<String> getIntermediate() {
        return intermediate;
    }
    public void setAdvanced(List<String> advanced) {
        this.advanced = advanced;
    }
    public void setBeginner(List<String> beginner) {
        this.beginner = beginner;
    }
    public void setExpert(List<String> expert) {
        this.expert = expert;
    }
    public void setIntermediate(List<String> intermediate) {
        this.intermediate = intermediate;
    }
}
