package com.approval.controller;

import com.approval.entity.ApprovalNode;
import com.approval.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/approvals")
@CrossOrigin(origins = "*")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    @PostMapping("/{nodeId}/approve")
    public ResponseEntity<ApprovalNode> approve(
            @PathVariable String nodeId,
            @RequestBody Map<String, String> request) {
        String approverId = request.get("approverId");
        String comment = request.get("comment");
        
        if (approverId == null || approverId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        ApprovalNode node = approvalService.approve(nodeId, approverId, comment);
        return ResponseEntity.ok(node);
    }

    @PostMapping("/{nodeId}/reject")
    public ResponseEntity<ApprovalNode> reject(
            @PathVariable String nodeId,
            @RequestBody Map<String, String> request) {
        String approverId = request.get("approverId");
        String reason = request.get("reason");
        String comment = request.get("comment");
        
        if (approverId == null || approverId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        ApprovalNode node = approvalService.reject(nodeId, approverId, reason, comment);
        return ResponseEntity.ok(node);
    }

    @PostMapping("/process-timeout")
    public ResponseEntity<Map<String, Object>> processTimeout(@RequestBody Map<String, String> request) {
        String nodeId = request.get("nodeId");
        
        ApprovalNode node = approvalService.getPendingTimeoutNodes().stream()
                .filter(n -> nodeId.equals(n.getId()))
                .findFirst()
                .orElse(null);
        
        if (node == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "节点不存在或未超时");
            return ResponseEntity.badRequest().body(result);
        }
        
        approvalService.processTimeout(node);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "超时处理完成");
        result.put("action", node.getTimeoutAction());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending-timeouts")
    public ResponseEntity<java.util.List<ApprovalNode>> getPendingTimeouts() {
        java.util.List<ApprovalNode> nodes = approvalService.getPendingTimeoutNodes();
        return ResponseEntity.ok(nodes);
    }
}
