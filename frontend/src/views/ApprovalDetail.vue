<template>
  <div>
    <el-button type="primary" link @click="goBack" style="margin-bottom: 20px;">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>
    
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>申请详情</span>
              <el-tag :type="getStatusType(application?.status)">
                {{ getStatusText(application?.status) }}
              </el-tag>
            </div>
          </template>
          
          <el-descriptions :column="3" border>
            <el-descriptions-item label="申请编号">
              {{ application?.applicationNo }}
            </el-descriptions-item>
            <el-descriptions-item label="申请标题">
              {{ application?.title }}
            </el-descriptions-item>
            <el-descriptions-item label="申请类型">
              <el-tag>{{ getTypeText(application?.applicationType) }}</el-tag>
            </el-descriptions-item>
            
            <el-descriptions-item label="申请金额">
              <span style="color: #f56c6c; font-weight: bold;">
                {{ formatAmount(application?.amount) }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="紧急程度">
              <el-tag :type="getUrgencyType(application?.urgencyLevel)">
                {{ getUrgencyText(application?.urgencyLevel) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="风险等级">
              <el-tag :type="getRiskType(application?.riskLevel)">
                {{ getRiskText(application?.riskLevel) }}
              </el-tag>
            </el-descriptions-item>
            
            <el-descriptions-item label="申请部门">
              {{ application?.department }}
            </el-descriptions-item>
            <el-descriptions-item label="申请人">
              {{ application?.applicantName }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ application?.createdAt }}
            </el-descriptions-item>
            
            <el-descriptions-item label="申请描述" :span="3">
              {{ application?.description || '无' }}
            </el-descriptions-item>
            
            <el-descriptions-item v-if="application?.rejectReason" label="拒绝原因" :span="3">
              <el-tag type="danger">{{ application?.rejectReason }}</el-tag>
            </el-descriptions-item>
            
            <el-descriptions-item v-if="application?.withdrawReason" label="撤回原因" :span="3">
              <el-tag type="warning">{{ application?.withdrawReason }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span>审批进度</span>
          </template>
          
          <el-steps
            :active="activeStep"
            finish-status="success"
            align-center
          >
            <el-step
              v-for="(node, index) in sortedNodes"
              :key="node.id"
              :title="node.nodeName"
              :description="getNodeDescription(node)"
              :status="getStepStatus(node)"
            />
          </el-steps>
          
          <el-table :data="sortedNodes" style="width: 100%; margin-top: 20px;" border>
            <el-table-column prop="nodeIndex" label="节点顺序" width="80">
              <template #default="scope">
                第 {{ scope.row.nodeIndex + 1 }} 节点
              </template>
            </el-table-column>
            <el-table-column prop="nodeName" label="节点名称" />
            <el-table-column prop="approverName" label="审批人" width="120" />
            <el-table-column prop="approverRole" label="角色" width="120" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="getNodeStatusType(scope.row.status)">
                  {{ getNodeStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="assignedAt" label="分配时间" width="180" />
            <el-table-column prop="processedAt" label="处理时间" width="180" />
            <el-table-column prop="comment" label="审批意见" min-width="150" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span>审批记录</span>
          </template>
          
          <el-timeline>
            <el-timeline-item
              v-for="record in sortedRecords"
              :key="record.id"
              :timestamp="record.actionTime"
              :type="getRecordType(record.action)"
              placement="top"
            >
              <el-card>
                <h4 style="margin-bottom: 10px;">
                  <el-tag :type="getRecordType(record.action)">{{ record.actionDescription }}</el-tag>
                </h4>
                <p style="color: #909399; margin: 5px 0;">
                  操作人: {{ record.operatorName }} ({{ record.operatorRole || '用户' }})
                </p>
                <p v-if="record.reason" style="color: #606266; margin: 5px 0;">
                  原因: {{ record.reason }}
                </p>
                <p v-if="record.comment" style="color: #606266; margin: 5px 0;">
                  备注: {{ record.comment }}
                </p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          
          <el-empty v-if="!loading && sortedRecords.length === 0" description="暂无审批记录" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { applicationApi } from '../api'

export default {
  name: 'ApprovalDetail',
  props: {
    currentUserId: String,
    currentUserName: String
  },
  components: {
    ArrowLeft
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(false)
    const application = ref(null)
    
    const fetchData = async () => {
      loading.value = true
      try {
        const res = await applicationApi.get(route.params.id)
        application.value = res.data
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
    
    onMounted(() => {
      fetchData()
    })
    
    const sortedNodes = computed(() => {
      if (!application.value?.approvalNodes) return []
      return [...application.value.approvalNodes].sort((a, b) => a.nodeIndex - b.nodeIndex)
    })
    
    const sortedRecords = computed(() => {
      if (!application.value?.approvalRecords) return []
      return [...application.value.approvalRecords].sort((a, b) => 
        new Date(a.actionTime) - new Date(b.actionTime)
      )
    })
    
    const activeStep = computed(() => {
      if (!application.value) return -1
      return application.value.currentNodeIndex >= 0 ? application.value.currentNodeIndex : 0
    })
    
    const goBack = () => {
      router.back()
    }
    
    const getTypeText = (type) => {
      const typeMap = {
        'PURCHASE': '采购申请',
        'LEAVE': '请假申请',
        'BUDGET': '预算申请',
        'EXPENSE': '报销申请',
        'CONTRACT': '合同审批',
        'ITEM_PURCHASE': '物品采购',
        'HR_APPROVAL': '人事审批'
      }
      return typeMap[type] || type
    }
    
    const getStatusType = (status) => {
      const statusMap = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'SUBMITTED': 'primary',
        'IN_PROGRESS': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'WITHDRAWN': 'info',
        'PENDING_WITHDRAW': 'warning'
      }
      return statusMap[status] || 'info'
    }
    
    const getStatusText = (status) => {
      const statusMap = {
        'DRAFT': '草稿',
        'PENDING': '待提交',
        'SUBMITTED': '已提交',
        'IN_PROGRESS': '审批中',
        'APPROVED': '已通过',
        'REJECTED': '已拒绝',
        'WITHDRAWN': '已撤回',
        'PENDING_WITHDRAW': '待撤回'
      }
      return statusMap[status] || status
    }
    
    const getUrgencyType = (level) => {
      const typeMap = {
        'LOW': 'info',
        'MEDIUM': '',
        'HIGH': 'warning',
        'URGENT': 'danger'
      }
      return typeMap[level] || ''
    }
    
    const getUrgencyText = (level) => {
      const textMap = {
        'LOW': '低',
        'MEDIUM': '中',
        'HIGH': '高',
        'URGENT': '紧急'
      }
      return textMap[level] || level
    }
    
    const getRiskType = (level) => {
      const typeMap = {
        'LOW': 'success',
        'MEDIUM': 'warning',
        'HIGH': 'danger',
        'CRITICAL': 'danger'
      }
      return typeMap[level] || ''
    }
    
    const getRiskText = (level) => {
      const textMap = {
        'LOW': '低风险',
        'MEDIUM': '中风险',
        'HIGH': '高风险',
        'CRITICAL': '极高风险'
      }
      return textMap[level] || level
    }
    
    const formatAmount = (amount) => {
      if (amount == null) return '-'
      return `¥${Number(amount).toFixed(2)}`
    }
    
    const getStepStatus = (node) => {
      const statusMap = {
        'PENDING': 'wait',
        'IN_PROGRESS': 'process',
        'APPROVED': 'success',
        'REJECTED': 'error',
        'SKIPPED': 'success',
        'EXPIRED': 'error',
        'INVALID': 'wait'
      }
      return statusMap[node.status] || 'wait'
    }
    
    const getNodeDescription = (node) => {
      if (node.status === 'IN_PROGRESS') {
        return `当前审批人: ${node.approverName}`
      }
      if (node.status === 'APPROVED') {
        return `已通过: ${node.approverName}`
      }
      if (node.status === 'REJECTED') {
        return `已拒绝: ${node.rejectReason || ''}`
      }
      return `待审批人: ${node.approverName}`
    }
    
    const getNodeStatusType = (status) => {
      const typeMap = {
        'PENDING': 'info',
        'IN_PROGRESS': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'SKIPPED': 'info',
        'EXPIRED': 'danger',
        'INVALID': 'info'
      }
      return typeMap[status] || 'info'
    }
    
    const getNodeStatusText = (status) => {
      const textMap = {
        'PENDING': '待审批',
        'IN_PROGRESS': '审批中',
        'APPROVED': '已通过',
        'REJECTED': '已拒绝',
        'SKIPPED': '已跳过',
        'EXPIRED': '已过期',
        'INVALID': '已失效'
      }
      return textMap[status] || status
    }
    
    const getRecordType = (action) => {
      const typeMap = {
        'CREATE': 'info',
        'SUBMIT': 'primary',
        'APPROVE': 'success',
        'REJECT': 'danger',
        'WITHDRAW': 'warning',
        'REQUEST_WITHDRAW': 'warning',
        'TIMEOUT_APPROVE': 'success',
        'TIMEOUT_REJECT': 'danger',
        'TIMEOUT_ESCALATE': 'warning',
        'REGENERATE_FLOW': 'primary',
        'MODIFY_AMOUNT': 'warning'
      }
      return typeMap[action] || 'info'
    }
    
    return {
      loading,
      application,
      sortedNodes,
      sortedRecords,
      activeStep,
      goBack,
      getTypeText,
      getStatusType,
      getStatusText,
      getUrgencyType,
      getUrgencyText,
      getRiskType,
      getRiskText,
      formatAmount,
      getStepStatus,
      getNodeDescription,
      getNodeStatusType,
      getNodeStatusText,
      getRecordType
    }
  }
}
</script>
