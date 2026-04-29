package com.approval.service;

import com.approval.entity.Application;
import com.approval.entity.ApprovalNode;
import com.approval.entity.ApprovalRecord;
import com.approval.enums.ApprovalAction;
import com.approval.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalRecordService {

    @Autowired
    private InMemoryStorage storage;

    public ApprovalRecord createRecord(Application application, ApprovalNode node, 
                                        ApprovalAction action, String description, String reason) {
        ApprovalRecord record = new ApprovalRecord();
        record.setId(storage.generateRecordId());
        record.setApplicationId(application.getId());
        
        if (node != null) {
            record.setNodeId(node.getId());
            record.setNodeIndex(node.getNodeIndex());
        }
        
        record.setOperatorId(application.getApplicantId());
        record.setOperatorName(application.getApplicantName());
        
        record.setAction(action);
        record.setActionDescription(description);
        record.setReason(reason);
        record.setActionTime(LocalDateTime.now());
        
        record.setBeforeStatus(application.getStatus() != null ? 
                application.getStatus().getDescription() : "未知");
        record.setAfterStatus(application.getStatus() != null ? 
                application.getStatus().getDescription() : "未知");
        
        return storage.saveRecord(record);
    }

    public ApprovalRecord createRecordWithOperator(Application application, ApprovalNode node,
                                                    ApprovalAction action, String description, 
                                                    String reason, String operatorId, String operatorName) {
        ApprovalRecord record = new ApprovalRecord();
        record.setId(storage.generateRecordId());
        record.setApplicationId(application.getId());
        
        if (node != null) {
            record.setNodeId(node.getId());
            record.setNodeIndex(node.getNodeIndex());
        }
        
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        
        record.setAction(action);
        record.setActionDescription(description);
        record.setReason(reason);
        record.setActionTime(LocalDateTime.now());
        
        record.setBeforeStatus(application.getStatus() != null ? 
                application.getStatus().getDescription() : "未知");
        record.setAfterStatus(application.getStatus() != null ? 
                application.getStatus().getDescription() : "未知");
        
        return storage.saveRecord(record);
    }

    public List<ApprovalRecord> getRecordsByApplication(String applicationId) {
        return storage.getRecordsByApplication(applicationId).stream()
                .sorted(Comparator.comparing(ApprovalRecord::getActionTime))
                .collect(Collectors.toList());
    }

    public List<ApprovalRecord> getRecordsByNode(String nodeId) {
        return storage.getApprovalRecords().values().stream()
                .filter(r -> nodeId.equals(r.getNodeId()))
                .sorted(Comparator.comparing(ApprovalRecord::getActionTime))
                .collect(Collectors.toList());
    }

    public List<ApprovalRecord> getRecordsByOperator(String operatorId) {
        return storage.getApprovalRecords().values().stream()
                .filter(r -> operatorId.equals(r.getOperatorId()))
                .sorted(Comparator.comparing(ApprovalRecord::getActionTime).reversed())
                .collect(Collectors.toList());
    }
}
