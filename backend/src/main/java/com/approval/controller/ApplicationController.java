package com.approval.controller;

import com.approval.entity.Application;
import com.approval.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody Application application) {
        Application created = applicationService.createApplication(application);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Application> submitApplication(@PathVariable String id) {
        Application application = applicationService.submitApplication(id);
        return ResponseEntity.ok(application);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Application> withdrawApplication(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        String operatorId = request.get("operatorId");
        if (operatorId == null) {
            operatorId = "system";
        }
        Application application = applicationService.withdrawApplication(id, reason, operatorId);
        return ResponseEntity.ok(application);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplication(@PathVariable String id) {
        Application application = applicationService.getApplication(id);
        if (application == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(application);
    }

    @GetMapping("/applicant/{applicantId}")
    public ResponseEntity<List<Application>> getApplicationsByApplicant(@PathVariable String applicantId) {
        List<Application> applications = applicationService.getApplicationsByApplicant(applicantId);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/approver/{approverId}")
    public ResponseEntity<List<Application>> getApplicationsForApprover(@PathVariable String approverId) {
        List<Application> applications = applicationService.getApplicationsForApprover(approverId);
        return ResponseEntity.ok(applications);
    }

    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        List<Application> applications = applicationService.getAllApplications();
        return ResponseEntity.ok(applications);
    }

    @PostMapping("/{id}/modify-amount")
    public ResponseEntity<Application> modifyAmount(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        java.math.BigDecimal newAmount = new java.math.BigDecimal(request.get("newAmount").toString());
        String operatorId = (String) request.get("operatorId");
        String reason = (String) request.get("reason");
        
        if (operatorId == null) {
            operatorId = "system";
        }
        
        Application application = applicationService.modifyAmount(id, newAmount, operatorId, reason);
        return ResponseEntity.ok(application);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<Map<String, Object>> getApplicationStatus(@PathVariable String id) {
        Application application = applicationService.getApplication(id);
        if (application == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("applicationId", application.getId());
        result.put("status", application.getStatus());
        result.put("statusDescription", application.getStatus().getDescription());
        result.put("currentNodeIndex", application.getCurrentNodeIndex());
        result.put("currentApproverName", application.getCurrentApproverName());
        result.put("approvalNodes", application.getApprovalNodes());
        
        return ResponseEntity.ok(result);
    }
}
