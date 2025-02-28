package com.killiancorbel.realtimeapi.models;

import jakarta.persistence.*;

@Entity
@Table
public class Stat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String date;
    private String type;
    private int value;

    public Long getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
