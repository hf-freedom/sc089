package com.approval.enums;

public enum ApplicationStatus {
    DRAFT("草稿"),
    PENDING("待提交"),
    SUBMITTED("已提交"),
    IN_PROGRESS("审批中"),
    APPROVED("已通过"),
    REJECTED("已拒绝"),
    WITHDRAWN("已撤回"),
    PENDING_WITHDRAW("待撤回审批"),
    RESUBMITTED("重新提交");

    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
