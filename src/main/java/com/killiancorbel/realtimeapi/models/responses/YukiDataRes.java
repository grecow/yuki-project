package com.killiancorbel.realtimeapi.models.responses;

import com.killiancorbel.realtimeapi.models.YukiData;

public class YukiDataRes {
    private int tokens;
    private String prompt;

    public YukiDataRes(YukiData yukiData) {
        this.tokens = yukiData.getTokens();
        this.prompt = getPromptFromModel(yukiData);
    }

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getPromptFromModel(YukiData yukiData) {
        String prompt = "Your knowledge cutoff is 2024-10. You are a helpful " + yukiData.getLanguage() + " teacher.";
        switch (yukiData.getLevel()) {
            case 0:
                prompt += "Your student is a beginner.";
                break;
            case 1:
                prompt += "Your student has basic knowledge.";
                break;
            case 2:
                prompt += "Your student can have basic conversation.";
                break;
            default:
                prompt += "Your student can discuss many topic on depth.";
                break;
        }
        prompt += "Your voice and personality should be warm and engaging, with a lively and playful tone. Talk quickly.";
        return prompt;
    }
}
