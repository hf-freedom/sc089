package com.approval.config;

import com.approval.entity.ApprovalRule;
import com.approval.enums.ApplicationType;
import com.approval.enums.RiskLevel;
import com.approval.enums.TimeoutAction;
import com.approval.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RuleInitializer implements CommandLineRunner {

    @Autowired
    private InMemoryStorage storage;

    @Override
    public void run(String... args) {
        initApprovalRules();
    }

    private void initApprovalRules() {
        ApprovalRule rule1 = new ApprovalRule();
        rule1.setId(storage.generateRuleId());
        rule1.setRuleName("部门经理审批-小额");
        rule1.setPriority(1);
        rule1.setIsActive(true);
        rule1.setApplicationType(null);
        rule1.setMinAmount(BigDecimal.ZERO);
        rule1.setMaxAmount(new BigDecimal("10000"));
        rule1.setRiskLevel(RiskLevel.LOW);
        rule1.setApproverId("dept_manager_001");
        rule1.setApproverName("张三");
        rule1.setApproverRole("部门经理");
        rule1.setNodeName("部门经理审批");
        rule1.setTimeoutMinutes(24 * 60);
        rule1.setTimeoutAction(TimeoutAction.AUTO_APPROVE);
        rule1.setRequireRecheckOnAmountChange(true);
        storage.saveRule(rule1);

        ApprovalRule rule2 = new ApprovalRule();
        rule2.setId(storage.generateRuleId());
        rule2.setRuleName("部门经理审批-采购");
        rule2.setPriority(2);
        rule2.setIsActive(true);
        rule2.setApplicationType(ApplicationType.PURCHASE);
        rule2.setMinAmount(new BigDecimal("10000"));
        rule2.setMaxAmount(new BigDecimal("50000"));
        rule2.setRiskLevel(RiskLevel.LOW);
        rule2.setApproverId("dept_manager_001");
        rule2.setApproverName("张三");
        rule2.setApproverRole("部门经理");
        rule2.setNodeName("采购申请-部门经理审批");
        rule2.setTimeoutMinutes(12 * 60);
        rule2.setTimeoutAction(TimeoutAction.ESCALATE);
        rule2.setEscalateToApproverId("finance_manager_001");
        rule2.setEscalateToApproverName("李四");
        rule2.setRequireRecheckOnAmountChange(true);
        storage.saveRule(rule2);

        ApprovalRule rule3 = new ApprovalRule();
        rule3.setId(storage.generateRuleId());
        rule3.setRuleName("财务经理审批-中等金额");
        rule3.setPriority(3);
        rule3.setIsActive(true);
        rule3.setApplicationType(null);
        rule3.setMinAmount(new BigDecimal("50000"));
        rule3.setMaxAmount(new BigDecimal("200000"));
        rule3.setRiskLevel(RiskLevel.MEDIUM);
        rule3.setApproverId("finance_manager_001");
        rule3.setApproverName("李四");
        rule3.setApproverRole("财务经理");
        rule3.setNodeName("财务经理审批");
        rule3.setTimeoutMinutes(12 * 60);
        rule3.setTimeoutAction(TimeoutAction.AUTO_APPROVE);
        rule3.setRequireRecheckOnAmountChange(true);
        storage.saveRule(rule3);

        ApprovalRule rule4 = new ApprovalRule();
        rule4.setId(storage.generateRuleId());
        rule4.setRuleName("财务总监审批-大额");
        rule4.setPriority(4);
        rule4.setIsActive(true);
        rule4.setApplicationType(null);
        rule4.setMinAmount(new BigDecimal("200000"));
        rule4.setMaxAmount(new BigDecimal("1000000"));
        rule4.setRiskLevel(RiskLevel.HIGH);
        rule4.setApproverId("finance_director_001");
        rule4.setApproverName("王五");
        rule4.setApproverRole("财务总监");
        rule4.setNodeName("财务总监审批");
        rule4.setTimeoutMinutes(8 * 60);
        rule4.setTimeoutAction(TimeoutAction.ESCALATE);
        rule4.setEscalateToApproverId("ceo_001");
        rule4.setEscalateToApproverName("赵六");
        rule4.setRequireRecheckOnAmountChange(true);
        storage.saveRule(rule4);

        ApprovalRule rule5 = new ApprovalRule();
        rule5.setId(storage.generateRuleId());
        rule5.setRuleName("CEO审批-超巨额");
        rule5.setPriority(5);
        rule5.setIsActive(true);
        rule5.setApplicationType(null);
        rule5.setMinAmount(new BigDecimal("1000000"));
        rule5.setMaxAmount(null);
        rule5.setRiskLevel(RiskLevel.CRITICAL);
        rule5.setApproverId("ceo_001");
        rule5.setApproverName("赵六");
        rule5.setApproverRole("CEO");
        rule5.setNodeName("CEO最终审批");
        rule5.setTimeoutMinutes(24 * 60 * 3);
        rule5.setTimeoutAction(TimeoutAction.AUTO_APPROVE);
        rule5.setRequireRecheckOnAmountChange(true);
        storage.saveRule(rule5);

        ApprovalRule rule6 = new ApprovalRule();
        rule6.setId(storage.generateRuleId());
        rule6.setRuleName("合同审批-法务");
        rule6.setPriority(10);
        rule6.setIsActive(true);
        rule6.setApplicationType(ApplicationType.CONTRACT);
        rule6.setMinAmount(BigDecimal.ZERO);
        rule6.setMaxAmount(null);
        rule6.setRiskLevel(RiskLevel.MEDIUM);
        rule6.setApproverId("legal_001");
        rule6.setApproverName("钱七");
        rule6.setApproverRole("法务");
        rule6.setNodeName("法务审核");
        rule6.setTimeoutMinutes(24 * 60);
        rule6.setTimeoutAction(TimeoutAction.AUTO_REJECT);
        rule6.setRequireRecheckOnAmountChange(false);
        storage.saveRule(rule6);

        ApprovalRule rule7 = new ApprovalRule();
        rule7.setId(storage.generateRuleId());
        rule7.setRuleName("高风险额外审批");
        rule7.setPriority(20);
        rule7.setIsActive(true);
        rule7.setApplicationType(null);
        rule7.setMinAmount(null);
        rule7.setMaxAmount(null);
        rule7.setRiskLevel(RiskLevel.HIGH);
        rule7.setApproverId("risk_manager_001");
        rule7.setApproverName("孙八");
        rule7.setApproverRole("风控经理");
        rule7.setNodeName("风控审核");
        rule7.setTimeoutMinutes(24 * 60);
        rule7.setTimeoutAction(TimeoutAction.AUTO_REJECT);
        rule7.setRequireRecheckOnAmountChange(false);
        storage.saveRule(rule7);

        ApprovalRule rule8 = new ApprovalRule();
        rule8.setId(storage.generateRuleId());
        rule8.setRuleName("人事审批-请假");
        rule8.setPriority(1);
        rule8.setIsActive(true);
        rule8.setApplicationType(ApplicationType.LEAVE);
        rule8.setMinAmount(null);
        rule8.setMaxAmount(null);
        rule8.setRiskLevel(RiskLevel.LOW);
        rule8.setApproverId("hr_manager_001");
        rule8.setApproverName("周九");
        rule8.setApproverRole("人事经理");
        rule8.setNodeName("人事审批");
        rule8.setTimeoutMinutes(24 * 60);
        rule8.setTimeoutAction(TimeoutAction.AUTO_APPROVE);
        rule8.setRequireRecheckOnAmountChange(false);
        storage.saveRule(rule8);
    }
}
