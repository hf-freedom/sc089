package com.approval.service;

import com.approval.entity.Application;
import com.approval.entity.ApprovalNode;
import com.approval.enums.ApplicationStatus;
import com.approval.enums.ApprovalAction;
import com.approval.enums.NodeStatus;
import com.approval.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApprovalService {

    @Autowired
    private InMemoryStorage storage;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApprovalRecordService recordService;

    @Transactional
    public ApprovalNode approve(String nodeId, String approverId, String comment) {
        ApprovalNode node = storage.getNode(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("审批节点不存在");
        }

        if (node.getStatus() != NodeStatus.IN_PROGRESS) {
            throw new IllegalStateException("只有审批中的节点可以处理");
        }

        if (!approverId.equals(node.getApproverId())) {
            throw new IllegalStateException("您没有权限审批此节点");
        }

        Application application = storage.getApplication(node.getApplicationId());
        if (application == null) {
            throw new IllegalArgumentException("申请不存在");
        }

        node.setStatus(NodeStatus.APPROVED);
        node.setComment(comment);
        node.setProcessedAt(LocalDateTime.now());
        storage.saveNode(node);

        recordService.createRecordWithOperator(application, node, 
                ApprovalAction.APPROVE, "审批通过", comment, 
                approverId, node.getApproverName());

        applicationService.activateNextNode(application);

        return node;
    }

    @Transactional
    public ApprovalNode reject(String nodeId, String approverId, String reason, String comment) {
        ApprovalNode node = storage.getNode(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("审批节点不存在");
        }

        if (node.getStatus() != NodeStatus.IN_PROGRESS) {
            throw new IllegalStateException("只有审批中的节点可以处理");
        }

        if (!approverId.equals(node.getApproverId())) {
            throw new IllegalStateException("您没有权限审批此节点");
        }

        Application application = storage.getApplication(node.getApplicationId());
        if (application == null) {
            throw new IllegalArgumentException("申请不存在");
        }

        node.setStatus(NodeStatus.REJECTED);
        node.setRejectReason(reason);
        node.setComment(comment);
        node.setProcessedAt(LocalDateTime.now());
        storage.saveNode(node);

        List<ApprovalNode> nodes = storage.getNodesByApplication(application.getId());
        for (ApprovalNode n : nodes) {
            if (n.getStatus() == NodeStatus.PENDING) {
                n.setStatus(NodeStatus.INVALID);
                storage.saveNode(n);
            }
        }

        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectReason(reason);
        application.setCompletedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        application.setCurrentApproverId(null);
        application.setCurrentApproverName(null);
        storage.saveApplication(application);

        recordService.createRecordWithOperator(application, node, 
                ApprovalAction.REJECT, "审批拒绝: " + reason, comment, 
                approverId, node.getApproverName());

        return node;
    }

    @Transactional
    public void processTimeout(ApprovalNode node) {
        if (node.getStatus() != NodeStatus.IN_PROGRESS) {
            return;
        }

        Application application = storage.getApplication(node.getApplicationId());
        if (application == null) {
            return;
        }

        switch (node.getTimeoutAction()) {
            case AUTO_APPROVE:
                handleAutoApprove(node, application);
                break;
            case AUTO_REJECT:
                handleAutoReject(node, application);
                break;
            case ESCALATE:
                handleEscalate(node, application);
                break;
        }
    }

    private void handleAutoApprove(ApprovalNode node, Application application) {
        node.setStatus(NodeStatus.APPROVED);
        node.setComment("超时自动通过");
        node.setProcessedAt(LocalDateTime.now());
        storage.saveNode(node);

        recordService.createRecord(application, node, 
                ApprovalAction.TIMEOUT_APPROVE, "超时自动通过", null);

        applicationService.activateNextNode(application);
    }

    private void handleAutoReject(ApprovalNode node, Application application) {
        node.setStatus(NodeStatus.REJECTED);
        node.setRejectReason("超时自动拒绝");
        node.setProcessedAt(LocalDateTime.now());
        storage.saveNode(node);

        List<ApprovalNode> nodes = storage.getNodesByApplication(application.getId());
        for (ApprovalNode n : nodes) {
            if (n.getStatus() == NodeStatus.PENDING) {
                n.setStatus(NodeStatus.INVALID);
                storage.saveNode(n);
            }
        }

        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectReason("超时自动拒绝");
        application.setCompletedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        application.setCurrentApproverId(null);
        application.setCurrentApproverName(null);
        storage.saveApplication(application);

        recordService.createRecord(application, node, 
                ApprovalAction.TIMEOUT_REJECT, "超时自动拒绝", null);
    }

    private void handleEscalate(ApprovalNode node, Application application) {
        if (node.getEscalateToApproverId() == null) {
            handleAutoApprove(node, application);
            return;
        }

        String originalApproverId = node.getApproverId();
        String originalApproverName = node.getApproverName();

        node.setOriginalApproverId(originalApproverId);
        node.setApproverId(node.getEscalateToApproverId());
        node.setApproverName(node.getEscalateToApproverName());
        node.setIsEscalated(true);
        node.setAssignedAt(LocalDateTime.now());
        
        if (node.getTimeoutMinutes() != null) {
            node.setDueTime(LocalDateTime.now().plusMinutes(node.getTimeoutMinutes()));
        }
        
        storage.saveNode(node);

        application.setCurrentApproverId(node.getEscalateToApproverId());
        application.setCurrentApproverName(node.getEscalateToApproverName());
        application.setUpdatedAt(LocalDateTime.now());
        storage.saveApplication(application);

        recordService.createRecordWithOperator(application, node, 
                ApprovalAction.TIMEOUT_ESCALATE, 
                "超时升级: " + originalApproverName + " -> " + node.getEscalateToApproverName(), 
                null, originalApproverId, originalApproverName);
    }

    public List<ApprovalNode> getPendingTimeoutNodes() {
        LocalDateTime now = LocalDateTime.now();
        return storage.getApprovalNodes().values().stream()
                .filter(node -> node.getStatus() == NodeStatus.IN_PROGRESS)
                .filter(node -> node.getDueTime() != null)
                .filter(node -> node.getDueTime().isBefore(now))
                .collect(java.util.stream.Collectors.toList());
    }
}
