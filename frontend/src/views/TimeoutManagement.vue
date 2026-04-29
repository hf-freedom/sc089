<template>
  <div>
    <el-card shadow="hover">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>超时管理</span>
          <div>
            <el-button type="primary" @click="fetchData">
              <el-icon><Refresh /></el-icon>
              刷新列表
            </el-button>
            <el-button type="warning" @click="processAllTimeouts" :loading="processing">
              <el-icon><Clock /></el-icon>
              处理全部超时
            </el-button>
          </div>
        </div>
      </template>
      
      <el-alert
        title="说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      >
        <template #default>
          <p>系统默认每 1 分钟自动检查超时审批节点。</p>
          <p>超时动作类型：</p>
          <ul>
            <li><strong>自动通过(AUTO_APPROVE)</strong>：超时后自动审批通过</li>
            <li><strong>自动拒绝(AUTO_REJECT)</strong>：超时后自动拒绝</li>
            <li><strong>升级审批(ESCALATE)</strong>：超时后升级给指定的更高一级审批人</li>
          </ul>
        </template>
      </el-alert>
      
      <el-table :data="timeoutNodes" style="width: 100%" v-loading="loading" border>
        <el-table-column prop="id" label="节点ID" width="120" />
        <el-table-column prop="applicationId" label="申请ID" width="180" />
        <el-table-column prop="nodeName" label="节点名称" min-width="150" />
        <el-table-column prop="approverName" label="审批人" width="100" />
        <el-table-column prop="approverRole" label="审批角色" width="100" />
        <el-table-column prop="assignedAt" label="分配时间" width="180" />
        <el-table-column prop="dueTime" label="到期时间" width="180">
          <template #default="scope">
            <el-tag type="danger">{{ scope.row.dueTime }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="timeoutMinutes" label="超时设置" width="100">
          <template #default="scope">
            {{ scope.row.timeoutMinutes }} 分钟
          </template>
        </el-table-column>
        <el-table-column prop="timeoutAction" label="超时动作" width="120">
          <template #default="scope">
            <el-tag :type="getTimeoutActionType(scope.row.timeoutAction)">
              {{ getTimeoutActionText(scope.row.timeoutAction) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="escalateToApproverName" label="升级对象" width="120">
          <template #default="scope">
            <span v-if="scope.row.timeoutAction === 'ESCALATE'">
              {{ scope.row.escalateToApproverName || '-' }}
            </span>
            <span v-else style="color: #909399;">不适用</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="processSingleTimeout(scope.row)">
              手动触发
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && timeoutNodes.length === 0" description="暂无超时审批节点" />
    </el-card>
    
    <el-card shadow="hover" style="margin-top: 20px;">
      <template #header>
        <span>系统自动检查</span>
      </template>
      
      <div style="display: flex; align-items: center; gap: 20px;">
        <span>当前自动检查间隔：</span>
        <el-tag type="primary">每 1 分钟</el-tag>
        <span style="color: #909399;">
          (由后端定时任务自动执行，无需手动操作)
        </span>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Clock } from '@element-plus/icons-vue'
import { approvalApi } from '../api'

export default {
  name: 'TimeoutManagement',
  components: {
    Refresh,
    Clock
  },
  setup() {
    const loading = ref(false)
    const processing = ref(false)
    const timeoutNodes = ref([])
    
    const fetchData = async () => {
      loading.value = true
      try {
        const res = await approvalApi.getPendingTimeouts()
        timeoutNodes.value = res.data
      } catch (error) {
        console.error(error)
        ElMessage.error('获取超时列表失败')
      } finally {
        loading.value = false
      }
    }
    
    onMounted(() => {
      fetchData()
    })
    
    const getTimeoutActionText = (action) => {
      const map = {
        'AUTO_APPROVE': '自动通过',
        'AUTO_REJECT': '自动拒绝',
        'ESCALATE': '升级审批'
      }
      return map[action] || action
    }
    
    const getTimeoutActionType = (action) => {
      const map = {
        'AUTO_APPROVE': 'success',
        'AUTO_REJECT': 'danger',
        'ESCALATE': 'warning'
      }
      return map[action] || 'info'
    }
    
    const processSingleTimeout = async (node) => {
      try {
        await ElMessageBox.confirm(
          `确定要手动触发超时处理吗？\n动作: ${getTimeoutActionText(node.timeoutAction)}\n审批人: ${node.approverName}`,
          '确认触发',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        processing.value = true
        await approvalApi.processTimeout({ nodeId: node.id })
        ElMessage.success('超时处理完成')
        fetchData()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
          ElMessage.error('处理失败')
        }
      } finally {
        processing.value = false
      }
    }
    
    const processAllTimeouts = async () => {
      if (timeoutNodes.value.length === 0) {
        ElMessage.warning('暂无超时节点需要处理')
        return
      }
      
      try {
        await ElMessageBox.confirm(
          `确定要处理全部 ${timeoutNodes.value.length} 个超时节点吗？`,
          '确认处理',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        processing.value = true
        let successCount = 0
        let failCount = 0
        
        for (const node of timeoutNodes.value) {
          try {
            await approvalApi.processTimeout({ nodeId: node.id })
            successCount++
          } catch (error) {
            failCount++
          }
        }
        
        ElMessage.success(`处理完成：成功 ${successCount} 个，失败 ${failCount} 个`)
        fetchData()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
          ElMessage.error('处理失败')
        }
      } finally {
        processing.value = false
      }
    }
    
    return {
      loading,
      processing,
      timeoutNodes,
      getTimeoutActionText,
      getTimeoutActionType,
      fetchData,
      processSingleTimeout,
      processAllTimeouts
    }
  }
}
</script>
