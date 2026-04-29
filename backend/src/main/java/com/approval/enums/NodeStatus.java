package com.approval.enums;

public enum NodeStatus {
    PENDING("待审批"),
    IN_PROGRESS("审批中"),
    APPROVED("已通过"),
    REJECTED("已拒绝"),
    SKIPPED("已跳过"),
    EXPIRED("已过期"),
    INVALID("已失效");

    private final String description;

    NodeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
