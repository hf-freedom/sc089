package com.approval.service;

import com.approval.entity.Application;
import com.approval.entity.ApprovalNode;
import com.approval.entity.ApprovalRule;
import com.approval.enums.ApplicationType;
import com.approval.enums.TimeoutAction;
import com.approval.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalRuleEngine {

    @Autowired
    private InMemoryStorage storage;

    public List<ApprovalRule> matchRules(Application application) {
        List<ApprovalRule> allRules = storage.getAllRules();
        
        return allRules.stream()
                .filter(ApprovalRule::getIsActive)
                .filter(rule -> matchesApplicationType(rule, application.getApplicationType()))
                .filter(rule -> matchesAmountRange(rule, application.getAmount()))
                .filter(rule -> matchesRiskLevel(rule, application))
                .sorted(Comparator.comparingInt(ApprovalRule::getPriority))
                .collect(Collectors.toList());
    }

    public List<ApprovalNode> generateApprovalNodes(Application application) {
        List<ApprovalRule> matchedRules = matchRules(application);
        List<ApprovalNode> nodes = new ArrayList<>();
        
        int nodeIndex = 0;
        
        List<ApprovalRule> sortedRules = matchedRules.stream()
                .sorted(Comparator.comparingInt(ApprovalRule::getPriority))
                .collect(Collectors.toList());
        
        for (ApprovalRule rule : sortedRules) {
            if (!isDuplicateApprover(nodes, rule.getApproverId())) {
                ApprovalNode node = createNodeFromRule(application, rule, nodeIndex++);
                nodes.add(node);
            }
        }
        
        if (nodes.isEmpty()) {
            ApprovalNode defaultNode = createDefaultNode(application, nodeIndex);
            nodes.add(defaultNode);
        }
        
        return nodes;
    }

    private boolean isDuplicateApprover(List<ApprovalNode> nodes, String approverId) {
        return nodes.stream()
                .anyMatch(node -> approverId.equals(node.getApproverId()));
    }

    private boolean matchesApplicationType(ApprovalRule rule, ApplicationType applicationType) {
        if (rule.getApplicationType() == null) {
            return true;
        }
        return rule.getApplicationType() == applicationType;
    }

    private boolean matchesAmountRange(ApprovalRule rule, BigDecimal amount) {
        if (amount == null) {
            return true;
        }
        
        if (rule.getMinAmount() == null && rule.getMaxAmount() == null) {
            return true;
        }
        
        if (rule.getMinAmount() != null && amount.compareTo(rule.getMinAmount()) < 0) {
            return false;
        }
        
        if (rule.getMaxAmount() != null && amount.compareTo(rule.getMaxAmount()) > 0) {
            return false;
        }
        
        return true;
    }

    private boolean matchesRiskLevel(ApprovalRule rule, Application application) {
        if (rule.getRiskLevel() == null) {
            return true;
        }
        
        if (application.getRiskLevel() == null) {
            return false;
        }
        
        return application.getRiskLevel().getLevel() >= rule.getRiskLevel().getLevel();
    }

    private ApprovalNode createNodeFromRule(Application application, ApprovalRule rule, int nodeIndex) {
        ApprovalNode node = new ApprovalNode();
        node.setId(storage.generateNodeId());
        node.setApplicationId(application.getId());
        node.setNodeIndex(nodeIndex);
        node.setNodeName(rule.getNodeName() != null ? rule.getNodeName() : "审批节点" + (nodeIndex + 1));
        node.setNodeType("APPROVAL");
        node.setApproverId(rule.getApproverId());
        node.setApproverName(rule.getApproverName());
        node.setApproverRole(rule.getApproverRole());
        node.setAssignedAt(LocalDateTime.now());
        
        if (rule.getTimeoutMinutes() != null && rule.getTimeoutMinutes() > 0) {
            node.setTimeoutMinutes(rule.getTimeoutMinutes());
            node.setDueTime(LocalDateTime.now().plusMinutes(rule.getTimeoutMinutes()));
        }
        
        node.setTimeoutAction(rule.getTimeoutAction() != null ? rule.getTimeoutAction() : TimeoutAction.AUTO_APPROVE);
        node.setEscalateToApproverId(rule.getEscalateToApproverId());
        node.setEscalateToApproverName(rule.getEscalateToApproverName());
        
        return node;
    }

    private ApprovalNode createDefaultNode(Application application, int nodeIndex) {
        ApprovalNode node = new ApprovalNode();
        node.setId(storage.generateNodeId());
        node.setApplicationId(application.getId());
        node.setNodeIndex(nodeIndex);
        node.setNodeName("默认审批节点");
        node.setNodeType("APPROVAL");
        node.setApproverId("manager001");
        node.setApproverName("默认审批人");
        node.setApproverRole("部门经理");
        node.setAssignedAt(LocalDateTime.now());
        node.setTimeoutMinutes(60 * 24);
        node.setDueTime(LocalDateTime.now().plusMinutes(60 * 24));
        node.setTimeoutAction(TimeoutAction.AUTO_APPROVE);
        
        return node;
    }

    public boolean needRecheckOnAmountChange(Application application, BigDecimal oldAmount, BigDecimal newAmount) {
        if (oldAmount == null || newAmount == null) {
            return true;
        }
        
        if (oldAmount.compareTo(newAmount) == 0) {
            return false;
        }
        
        List<ApprovalRule> rules = storage.getAllRules();
        for (ApprovalRule rule : rules) {
            if (rule.getRequireRecheckOnAmountChange() != null && rule.getRequireRecheckOnAmountChange()) {
                boolean oldInRange = isInRange(rule, oldAmount);
                boolean newInRange = isInRange(rule, newAmount);
                
                if (oldInRange != newInRange) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private boolean isInRange(ApprovalRule rule, BigDecimal amount) {
        if (rule.getMinAmount() == null && rule.getMaxAmount() == null) {
            return true;
        }
        
        if (rule.getMinAmount() != null && amount.compareTo(rule.getMinAmount()) < 0) {
            return false;
        }
        
        if (rule.getMaxAmount() != null && amount.compareTo(rule.getMaxAmount()) > 0) {
            return false;
        }
        
        return true;
    }
}
