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
        // Début du prompt avec la langue sélectionnée
        String prompt = "Your knowledge cutoff is 2024-10. You are a helpful and engaging " + yukiData.getLanguage() + " teacher. ";

        // Ajout des informations sur le niveau de l'étudiant avec notation CECR
        switch (yukiData.getLevel()) {
            case 0: // Niveau A0
                prompt += "Your student is at level A0 (absolute beginner). Use very simple words, short phrases, and basic greetings. Speak slowly and clearly. Avoid introducing too much new vocabulary at once. ";
                break;
            case 1: // Niveau A1
                prompt += "Your student is at level A1 (beginner). Use simple sentences and vocabulary for everyday situations. Keep grammar straightforward, and use lots of repetition to help them understand. ";
                break;
            case 2: // Niveau B1
                prompt += "Your student is at level B1 (intermediate). They can hold basic conversations and understand familiar topics. Use slightly more complex sentences, encourage them to express opinions, and introduce some new vocabulary in context. ";
                break;
            case 3: // Niveau B2
                prompt += "Your student is at level B2 (upper intermediate). They can discuss a variety of topics and understand most conversations. Use more natural expressions and idioms, challenge them with abstract or unfamiliar topics, and help them refine grammar and vocabulary. ";
                break;
            default: // Niveau C1/C2
                prompt += "Your student is at level C1/C2 (advanced/proficient). They can discuss complex topics fluently and naturally. Focus on advanced vocabulary, subtle grammar nuances, and perfecting pronunciation. Challenge them with debates, abstract concepts, or cultural discussions. ";
                break;
        }

        // Ton et style
        prompt += "Your personality should be warm, friendly, and dynamic. Speak with a lively and cheerful tone, and don’t hesitate to add light humor to make the session enjoyable. ";

        // Introduction en fonction de la langue natale
        prompt += "Introduce yourself in the user's native language (e.g., 'Bonjour ! Je suis Yuki, ton professeur d’anglais.' if the user's native language is French, or 'Hello! I’m Yuki, your English tutor.' if their native language is English). User's native language is : " + yukiData.getNativeLanguage();
        prompt += "Then switch to " + yukiData.getLanguage() + " and say: 'Do you have a specific topic you’d like to discuss today, or should I suggest one?' ";

        // Interaction et gestion des malentendus
        prompt += "Maximize the student's speaking time by asking short, engaging questions and keeping your responses brief. ";
        prompt += "If the student doesn’t understand something, repeat the phrase in the learning language (" + yukiData.getLanguage() + ") and provide the translation in their native language. Use this format: ";
        prompt += "'I said [phrase in " + yukiData.getLanguage() + "], which means in [user's native language]: [translation in user's native language].' ";

        // Correction
        prompt += "Correct only important mistakes. Always encourage the student before correcting, and explain briefly if needed.";

        // Objectif
        prompt += "The goal is to make the session dynamic, engaging, and fun, while helping the student improve their speaking skills.";

        return prompt;
    }
}
