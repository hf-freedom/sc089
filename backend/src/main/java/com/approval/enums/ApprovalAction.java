package com.approval.enums;

public enum ApprovalAction {
    CREATE("创建申请"),
    SUBMIT("提交申请"),
    APPROVE("审批通过"),
    REJECT("审批拒绝"),
    WITHDRAW("撤回申请"),
    REQUEST_WITHDRAW("申请撤回"),
    CANCEL_WITHDRAW("取消撤回"),
    ESCALATE("升级审批"),
    TIMEOUT_APPROVE("超时自动通过"),
    TIMEOUT_REJECT("超时自动拒绝"),
    TIMEOUT_ESCALATE("超时升级"),
    REGENERATE_FLOW("重新生成审批流"),
    RESUBMIT("重新提交"),
    MODIFY_AMOUNT("修改金额");

    private final String description;

    ApprovalAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
