package com.approval.enums;

public enum UrgencyLevel {
    LOW("低"),
    MEDIUM("中"),
    HIGH("高"),
    URGENT("紧急");

    private final String description;

    UrgencyLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
    
    public int getLevel() {
        switch (this) {
            case LOW: return 1;
            case MEDIUM: return 2;
            case HIGH: return 3;
            case URGENT: return 4;
            default: return 1;
        }
    }
}
