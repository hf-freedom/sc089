package com.approval.entity;

import com.approval.enums.ApplicationType;
import com.approval.enums.RiskLevel;
import com.approval.enums.TimeoutAction;
import com.approval.enums.UrgencyLevel;

import java.math.BigDecimal;

public class ApprovalRule {

    private String id;
    private String ruleName;
    private Integer priority;
    private Boolean isActive;
    
    private ApplicationType applicationType;
    
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    
    private RiskLevel riskLevel;
    private UrgencyLevel urgencyLevel;
    
    private String approverId;
    private String approverName;
    private String approverRole;
    private String nodeName;
    
    private Integer timeoutMinutes;
    private TimeoutAction timeoutAction;
    private String escalateToApproverId;
    private String escalateToApproverName;
    
    private Boolean requireRecheckOnAmountChange;
    
    private String description;

    public ApprovalRule() {
        this.isActive = true;
        this.requireRecheckOnAmountChange = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public UrgencyLevel getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(UrgencyLevel urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getApproverRole() {
        return approverRole;
    }

    public void setApproverRole(String approverRole) {
        this.approverRole = approverRole;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Integer getTimeoutMinutes() {
        return timeoutMinutes;
    }

    public void setTimeoutMinutes(Integer timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }

    public TimeoutAction getTimeoutAction() {
        return timeoutAction;
    }

    public void setTimeoutAction(TimeoutAction timeoutAction) {
        this.timeoutAction = timeoutAction;
    }

    public String getEscalateToApproverId() {
        return escalateToApproverId;
    }

    public void setEscalateToApproverId(String escalateToApproverId) {
        this.escalateToApproverId = escalateToApproverId;
    }

    public String getEscalateToApproverName() {
        return escalateToApproverName;
    }

    public void setEscalateToApproverName(String escalateToApproverName) {
        this.escalateToApproverName = escalateToApproverName;
    }

    public Boolean getRequireRecheckOnAmountChange() {
        return requireRecheckOnAmountChange;
    }

    public void setRequireRecheckOnAmountChange(Boolean requireRecheckOnAmountChange) {
        this.requireRecheckOnAmountChange = requireRecheckOnAmountChange;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
