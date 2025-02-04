package com.killiancorbel.realtimeapi.models;

import java.util.List;
import java.util.Map;

public class Topic {
    private Map<String, List<String>> levels;

    public Map<String, List<String>> getLevels() {
        return levels;
    }

    public void setLevels(Map<String, List<String>> levels) {
        this.levels = levels;
    }
}
