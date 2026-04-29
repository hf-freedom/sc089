package com.approval.enums;

public enum RiskLevel {
    LOW("低风险"),
    MEDIUM("中风险"),
    HIGH("高风险"),
    CRITICAL("极高风险");

    private final String description;

    RiskLevel(String description) {
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
            case CRITICAL: return 4;
            default: return 1;
        }
    }
}
