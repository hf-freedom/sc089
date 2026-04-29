package com.approval.enums;

public enum TimeoutAction {
    AUTO_APPROVE("自动通过"),
    AUTO_REJECT("自动拒绝"),
    ESCALATE("升级审批");

    private final String description;

    TimeoutAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
