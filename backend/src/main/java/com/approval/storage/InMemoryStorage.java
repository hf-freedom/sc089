package com.approval.storage;

import com.approval.entity.Application;
import com.approval.entity.ApprovalNode;
import com.approval.entity.ApprovalRecord;
import com.approval.entity.ApprovalRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryStorage {

    private final Map<String, Application> applications = new ConcurrentHashMap<>();
    private final Map<String, ApprovalNode> approvalNodes = new ConcurrentHashMap<>();
    private final Map<String, ApprovalRule> approvalRules = new ConcurrentHashMap<>();
    private final Map<String, ApprovalRecord> approvalRecords = new ConcurrentHashMap<>();
    private final Map<String, List<ApprovalNode>> nodesByApplication = new ConcurrentHashMap<>();
    private final Map<String, List<ApprovalRecord>> recordsByApplication = new ConcurrentHashMap<>();
    
    private final AtomicLong applicationIdCounter = new AtomicLong(1);
    private final AtomicLong nodeIdCounter = new AtomicLong(1);
    private final AtomicLong ruleIdCounter = new AtomicLong(1);
    private final AtomicLong recordIdCounter = new AtomicLong(1);

    public String generateApplicationId() {
        return "APP" + String.format("%06d", applicationIdCounter.getAndIncrement());
    }

    public String generateNodeId() {
        return "NODE" + String.format("%06d", nodeIdCounter.getAndIncrement());
    }

    public String generateRuleId() {
        return "RULE" + String.format("%06d", ruleIdCounter.getAndIncrement());
    }

    public String generateRecordId() {
        return "REC" + String.format("%06d", recordIdCounter.getAndIncrement());
    }

    public Application saveApplication(Application application) {
        applications.put(application.getId(), application);
        return application;
    }

    public Application getApplication(String id) {
        return applications.get(id);
    }

    public List<Application> getAllApplications() {
        return new ArrayList<>(applications.values());
    }

    public ApprovalNode saveNode(ApprovalNode node) {
        approvalNodes.put(node.getId(), node);
        
        List<ApprovalNode> nodes = nodesByApplication.computeIfAbsent(node.getApplicationId(), k -> new ArrayList<>());
        
        boolean found = false;
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId().equals(node.getId())) {
                nodes.set(i, node);
                found = true;
                break;
            }
        }
        
        if (!found) {
            nodes.add(node);
        }
        
        return node;
    }

    public ApprovalNode getNode(String id) {
        return approvalNodes.get(id);
    }

    public List<ApprovalNode> getNodesByApplication(String applicationId) {
        return new ArrayList<>(nodesByApplication.getOrDefault(applicationId, new ArrayList<>()));
    }

    public void clearNodesByApplication(String applicationId) {
        List<ApprovalNode> nodes = nodesByApplication.remove(applicationId);
        if (nodes != null) {
            for (ApprovalNode node : nodes) {
                approvalNodes.remove(node.getId());
            }
        }
    }

    public ApprovalRule saveRule(ApprovalRule rule) {
        approvalRules.put(rule.getId(), rule);
        return rule;
    }

    public ApprovalRule getRule(String id) {
        return approvalRules.get(id);
    }

    public List<ApprovalRule> getAllRules() {
        return new ArrayList<>(approvalRules.values());
    }

    public ApprovalRecord saveRecord(ApprovalRecord record) {
        approvalRecords.put(record.getId(), record);
        recordsByApplication.computeIfAbsent(record.getApplicationId(), k -> new ArrayList<>()).add(record);
        return record;
    }

    public List<ApprovalRecord> getRecordsByApplication(String applicationId) {
        return new ArrayList<>(recordsByApplication.getOrDefault(applicationId, new ArrayList<>()));
    }

    public Map<String, Application> getApplications() {
        return applications;
    }

    public Map<String, ApprovalNode> getApprovalNodes() {
        return approvalNodes;
    }

    public Map<String, ApprovalRule> getApprovalRules() {
        return approvalRules;
    }

    public Map<String, ApprovalRecord> getApprovalRecords() {
        return approvalRecords;
    }
}
