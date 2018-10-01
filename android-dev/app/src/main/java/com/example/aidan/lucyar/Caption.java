package com.example.aidan.lucyar;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Caption {
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("confidence")
    @Expose
    private Double confidence;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}

