package com.approval.entity;

import com.approval.enums.NodeStatus;
import com.approval.enums.TimeoutAction;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class ApprovalNode {

    private String id;
    private String applicationId;
    private Integer nodeIndex;
    private String nodeName;
    private String nodeType;
    private String approverId;
    private String approverName;
    private String approverRole;
    
    private NodeStatus status;
    private String comment;
    private String rejectReason;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedAt;
    
    private TimeoutAction timeoutAction;
    private String escalateToApproverId;
    private String escalateToApproverName;
    
    private Integer timeoutMinutes;
    private Boolean isEscalated;
    private String originalApproverId;
    
    private Boolean isReworkRequired;
    private String reworkReason;

    public ApprovalNode() {
        this.status = NodeStatus.PENDING;
        this.isEscalated = false;
        this.isReworkRequired = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getNodeIndex() {
        return nodeIndex;
    }

    public void setNodeIndex(Integer nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
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

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getDueTime() {
        return dueTime;
    }

    public void setDueTime(LocalDateTime dueTime) {
        this.dueTime = dueTime;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
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

    public Integer getTimeoutMinutes() {
        return timeoutMinutes;
    }

    public void setTimeoutMinutes(Integer timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }

    public Boolean getIsEscalated() {
        return isEscalated;
    }

    public void setIsEscalated(Boolean isEscalated) {
        this.isEscalated = isEscalated;
    }

    public String getOriginalApproverId() {
        return originalApproverId;
    }

    public void setOriginalApproverId(String originalApproverId) {
        this.originalApproverId = originalApproverId;
    }

    public Boolean getIsReworkRequired() {
        return isReworkRequired;
    }

    public void setIsReworkRequired(Boolean isReworkRequired) {
        this.isReworkRequired = isReworkRequired;
    }

    public String getReworkReason() {
        return reworkReason;
    }

    public void setReworkReason(String reworkReason) {
        this.reworkReason = reworkReason;
    }
}
