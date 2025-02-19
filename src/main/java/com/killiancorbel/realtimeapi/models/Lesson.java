package com.killiancorbel.realtimeapi.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String lesson_key;
    private String title;
    private String description;
    private String language;
    private String image = null;
    private boolean published;
    @OneToMany
    private List<Question> questions = new ArrayList<>();

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void addQuestion(Question question) {
        if (this.questions == null) {
            this.questions = new ArrayList<>();
        }
        this.questions.add(question);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLesson_key() {
        return lesson_key;
    }

    public void setLesson_key(String lesson_key) {
        this.lesson_key = lesson_key;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}
