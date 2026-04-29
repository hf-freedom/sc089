package com.approval.controller;

import com.approval.entity.ApprovalRule;
import com.approval.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin(origins = "*")
public class RuleController {

    @Autowired
    private InMemoryStorage storage;

    @GetMapping
    public ResponseEntity<List<ApprovalRule>> getAllRules() {
        List<ApprovalRule> rules = storage.getAllRules();
        rules.sort((a, b) -> {
            if (a.getPriority() == null && b.getPriority() == null) return 0;
            if (a.getPriority() == null) return 1;
            if (b.getPriority() == null) return -1;
            return a.getPriority() - b.getPriority();
        });
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovalRule> getRule(@PathVariable String id) {
        ApprovalRule rule = storage.getRule(id);
        if (rule == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rule);
    }

    @PostMapping
    public ResponseEntity<ApprovalRule> createRule(@RequestBody ApprovalRule rule) {
        rule.setId(storage.generateRuleId());
        if (rule.getIsActive() == null) {
            rule.setIsActive(true);
        }
        storage.saveRule(rule);
        return ResponseEntity.ok(rule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApprovalRule> updateRule(@PathVariable String id, @RequestBody ApprovalRule rule) {
        ApprovalRule existingRule = storage.getRule(id);
        if (existingRule == null) {
            return ResponseEntity.notFound().build();
        }
        
        rule.setId(id);
        storage.saveRule(rule);
        return ResponseEntity.ok(rule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable String id) {
        ApprovalRule rule = storage.getRule(id);
        if (rule == null) {
            return ResponseEntity.notFound().build();
        }
        
        storage.getApprovalRules().remove(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<ApprovalRule> toggleRule(@PathVariable String id) {
        ApprovalRule rule = storage.getRule(id);
        if (rule == null) {
            return ResponseEntity.notFound().build();
        }
        
        rule.setIsActive(rule.getIsActive() == null ? false : !rule.getIsActive());
        storage.saveRule(rule);
        return ResponseEntity.ok(rule);
    }
}
