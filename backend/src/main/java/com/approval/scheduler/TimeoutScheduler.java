package com.approval.scheduler;

import com.approval.entity.ApprovalNode;
import com.approval.service.ApprovalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TimeoutScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TimeoutScheduler.class);

    @Autowired
    private ApprovalService approvalService;

    @Scheduled(fixedRate = 60000)
    public void checkTimeoutNodes() {
        logger.debug("开始检查超时审批节点...");
        
        try {
            List<ApprovalNode> timeoutNodes = approvalService.getPendingTimeoutNodes();
            
            for (ApprovalNode node : timeoutNodes) {
                try {
                    logger.info("处理超时节点: {} - 审批人: {}", node.getId(), node.getApproverName());
                    approvalService.processTimeout(node);
                } catch (Exception e) {
                    logger.error("处理超时节点失败: {}", node.getId(), e);
                }
            }
            
            if (!timeoutNodes.isEmpty()) {
                logger.info("已处理 {} 个超时审批节点", timeoutNodes.size());
            }
        } catch (Exception e) {
            logger.error("检查超时节点任务执行失败", e);
        }
    }
}
