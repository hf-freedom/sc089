package com.approval.enums;

public enum ApplicationType {
    PURCHASE("采购申请"),
    LEAVE("请假申请"),
    BUDGET("预算申请"),
    EXPENSE("报销申请"),
    CONTRACT("合同审批"),
    ITEM_PURCHASE("物品采购"),
    HR_APPROVAL("人事审批");

    private final String description;

    ApplicationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
