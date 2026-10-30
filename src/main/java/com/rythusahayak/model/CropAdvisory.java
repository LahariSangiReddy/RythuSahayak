package com.rythusahayak.model;

import java.time.LocalDate;

public record CropAdvisory(
        String id,
        String crop,
        String category,
        String title,
        String description,
        String recommendation,
        String growthStage,
        String language,
        LocalDate lastReviewed,
        String source) {
    public String getId() { return id; }
    public String getCrop() { return crop; }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getRecommendation() { return recommendation; }
    public String getGrowthStage() { return growthStage; }
    public String getLanguage() { return language; }
    public LocalDate getLastReviewed() { return lastReviewed; }
    public String getSource() { return source; }
}
