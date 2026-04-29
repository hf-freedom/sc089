package com.approval.service;

import com.approval.entity.Application;
import com.approval.entity.ApprovalNode;
import com.approval.entity.ApprovalRecord;
import com.approval.enums.ApplicationStatus;
import com.approval.enums.ApprovalAction;
import com.approval.enums.NodeStatus;
import com.approval.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    @Autowired
    private InMemoryStorage storage;

    @Autowired
    private ApprovalRuleEngine approvalRuleEngine;

    @Autowired
    private ApprovalRecordService recordService;

    public Application createApplication(Application application) {
        String applicationId = storage.generateApplicationId();
        String applicationNo = generateApplicationNo();
        
        application.setId(applicationId);
        application.setApplicationNo(applicationNo);
        application.setStatus(ApplicationStatus.DRAFT);
        application.setOriginalAmount(application.getAmount());
        application.setCurrentNodeIndex(-1);
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        
        storage.saveApplication(application);
        
        recordService.createRecord(application, null, ApprovalAction.CREATE, "创建申请", null);
        
        return application;
    }

    @Transactional
    public Application submitApplication(String applicationId) {
        Application application = storage.getApplication(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        
        if (application.getStatus() != ApplicationStatus.DRAFT && 
            application.getStatus() != ApplicationStatus.RESUBMITTED) {
            throw new IllegalStateException("只有草稿或重新提交状态可以提交");
        }
        
        storage.clearNodesByApplication(applicationId);
        
        List<ApprovalNode> nodes = approvalRuleEngine.generateApprovalNodes(application);
        
        for (ApprovalNode node : nodes) {
            storage.saveNode(node);
        }
        
        application.setApprovalNodes(nodes);
        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmittedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        application.setCurrentNodeIndex(-1);
        
        storage.saveApplication(application);
        
        recordService.createRecord(application, null, ApprovalAction.SUBMIT, "提交申请", null);
        
        if (!nodes.isEmpty()) {
            activateNextNode(application);
        }
        
        return application;
    }

    public void activateNextNode(Application application) {
        List<ApprovalNode> nodes = storage.getNodesByApplication(application.getId());
        
        int nextIndex = application.getCurrentNodeIndex() + 1;
        
        if (nextIndex >= nodes.size()) {
            completeApplication(application);
            return;
        }
        
        ApprovalNode nextNode = nodes.get(nextIndex);
        
        nextNode.setStatus(NodeStatus.IN_PROGRESS);
        nextNode.setAssignedAt(LocalDateTime.now());
        storage.saveNode(nextNode);
        
        application.setStatus(ApplicationStatus.IN_PROGRESS);
        application.setCurrentNodeIndex(nextIndex);
        application.setCurrentApproverId(nextNode.getApproverId());
        application.setCurrentApproverName(nextNode.getApproverName());
        application.setUpdatedAt(LocalDateTime.now());
        
        storage.saveApplication(application);
    }

    public void completeApplication(Application application) {
        application.setStatus(ApplicationStatus.APPROVED);
        application.setCompletedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        application.setCurrentApproverId(null);
        application.setCurrentApproverName(null);
        
        storage.saveApplication(application);
    }

    @Transactional
    public Application withdrawApplication(String applicationId, String reason, String operatorId) {
        Application application = storage.getApplication(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        
        List<ApprovalNode> nodes = storage.getNodesByApplication(applicationId);
        
        boolean hasProcessedNode = nodes.stream()
                .anyMatch(n -> n.getStatus() == NodeStatus.APPROVED || 
                              n.getStatus() == NodeStatus.REJECTED);
        
        if (application.getCurrentNodeIndex() < 0 || !hasProcessedNode) {
            application.setStatus(ApplicationStatus.WITHDRAWN);
            application.setWithdrawReason(reason);
            application.setUpdatedAt(LocalDateTime.now());
            application.setCurrentApproverId(null);
            application.setCurrentApproverName(null);
            
            for (ApprovalNode node : nodes) {
                if (node.getStatus() == NodeStatus.PENDING || node.getStatus() == NodeStatus.IN_PROGRESS) {
                    node.setStatus(NodeStatus.INVALID);
                    storage.saveNode(node);
                }
            }
            
            storage.saveApplication(application);
            
            recordService.createRecord(application, null, ApprovalAction.WITHDRAW, "直接撤回申请", reason);
        } else {
            application.setStatus(ApplicationStatus.PENDING_WITHDRAW);
            application.setWithdrawReason(reason);
            application.setUpdatedAt(LocalDateTime.now());
            
            storage.saveApplication(application);
            
            recordService.createRecord(application, null, ApprovalAction.REQUEST_WITHDRAW, "申请撤回审批", reason);
        }
        
        return application;
    }

    @Transactional
    public Application modifyAmount(String applicationId, BigDecimal newAmount, String operatorId, String reason) {
        Application application = storage.getApplication(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        
        if (application.getStatus() != ApplicationStatus.IN_PROGRESS &&
            application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException("只有审批中或已提交状态可以修改金额");
        }
        
        BigDecimal oldAmount = application.getAmount();
        
        if (approvalRuleEngine.needRecheckOnAmountChange(application, oldAmount, newAmount)) {
            application.setAmount(newAmount);
            application.setIsModified(true);
            application.setModifiedBy(operatorId);
            application.setUpdatedAt(LocalDateTime.now());
            
            storage.saveApplication(application);
            
            recordService.createRecord(application, null, ApprovalAction.MODIFY_AMOUNT, 
                    "修改金额: " + oldAmount + " -> " + newAmount, reason);
            
            regenerateApprovalFlow(applicationId, operatorId);
        } else {
            application.setAmount(newAmount);
            application.setIsModified(true);
            application.setModifiedBy(operatorId);
            application.setUpdatedAt(LocalDateTime.now());
            
            storage.saveApplication(application);
            
            recordService.createRecord(application, null, ApprovalAction.MODIFY_AMOUNT, 
                    "修改金额: " + oldAmount + " -> " + newAmount + "（无需重新审批）", reason);
        }
        
        return application;
    }

    @Transactional
    public Application regenerateApprovalFlow(String applicationId, String operatorId) {
        Application application = storage.getApplication(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        
        List<ApprovalNode> oldNodes = storage.getNodesByApplication(applicationId);
        
        List<ApprovalNode> newNodes = approvalRuleEngine.generateApprovalNodes(application);
        
        for (ApprovalNode oldNode : oldNodes) {
            if (oldNode.getStatus() == NodeStatus.PENDING || oldNode.getStatus() == NodeStatus.IN_PROGRESS) {
                oldNode.setStatus(NodeStatus.INVALID);
                storage.saveNode(oldNode);
            }
        }
        
        for (ApprovalNode newNode : newNodes) {
            boolean alreadyApproved = oldNodes.stream()
                    .anyMatch(old -> old.getStatus() == NodeStatus.APPROVED &&
                                    old.getApproverId().equals(newNode.getApproverId()));
            
            if (alreadyApproved) {
                newNode.setStatus(NodeStatus.APPROVED);
                newNode.setProcessedAt(LocalDateTime.now());
            }
            
            storage.saveNode(newNode);
        }
        
        application.setApprovalNodes(newNodes);
        application.setUpdatedAt(LocalDateTime.now());
        
        int currentApprovedCount = (int) newNodes.stream()
                .filter(n -> n.getStatus() == NodeStatus.APPROVED)
                .count();
        
        if (currentApprovedCount < newNodes.size()) {
            application.setCurrentNodeIndex(currentApprovedCount - 1);
            
            for (int i = 0; i < newNodes.size(); i++) {
                if (newNodes.get(i).getStatus() == NodeStatus.PENDING) {
                    application.setCurrentNodeIndex(i - 1);
                    activateNextNode(application);
                    break;
                }
            }
        } else {
            completeApplication(application);
        }
        
        storage.saveApplication(application);
        
        recordService.createRecord(application, null, ApprovalAction.REGENERATE_FLOW, 
                "重新生成审批链路", "金额修改导致规则变化");
        
        return application;
    }

    public Application getApplication(String applicationId) {
        Application application = storage.getApplication(applicationId);
        if (application != null) {
            List<ApprovalNode> nodes = storage.getNodesByApplication(applicationId);
            application.setApprovalNodes(nodes);
            
            List<ApprovalRecord> records = recordService.getRecordsByApplication(applicationId);
            application.setApprovalRecords(records);
        }
        return application;
    }

    public List<Application> getApplicationsByApplicant(String applicantId) {
        return storage.getAllApplications().stream()
                .filter(app -> applicantId.equals(app.getApplicantId()))
                .peek(app -> {
                    app.setApprovalNodes(storage.getNodesByApplication(app.getId()));
                    app.setApprovalRecords(recordService.getRecordsByApplication(app.getId()));
                })
                .collect(Collectors.toList());
    }

    public List<Application> getApplicationsForApprover(String approverId) {
        return storage.getAllApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.IN_PROGRESS)
                .filter(app -> approverId.equals(app.getCurrentApproverId()))
                .peek(app -> {
                    app.setApprovalNodes(storage.getNodesByApplication(app.getId()));
                    app.setApprovalRecords(recordService.getRecordsByApplication(app.getId()));
                })
                .collect(Collectors.toList());
    }

    public List<Application> getAllApplications() {
        return storage.getAllApplications().stream()
                .peek(app -> {
                    app.setApprovalNodes(storage.getNodesByApplication(app.getId()));
                    app.setApprovalRecords(recordService.getRecordsByApplication(app.getId()));
                })
                .collect(Collectors.toList());
    }

    private String generateApplicationNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "APP" + LocalDateTime.now().format(formatter) + 
                String.format("%04d", (int)(Math.random() * 10000));
    }
}
