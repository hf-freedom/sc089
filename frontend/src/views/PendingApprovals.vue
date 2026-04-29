<template>
  <div>
    <el-card shadow="hover">
      <template #header>
        <span>待我审批</span>
      </template>
      
      <el-table :data="applications" style="width: 100%" v-loading="loading">
        <el-table-column prop="applicationNo" label="申请编号" width="220" />
        <el-table-column prop="title" label="申请标题" min-width="200" />
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="applicationType" label="申请类型" width="120">
          <template #default="scope">
            <el-tag>{{ getTypeText(scope.row.applicationType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="申请金额" width="120">
          <template #default="scope">
            {{ formatAmount(scope.row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="currentNodeIndex" label="当前节点" width="100">
          <template #default="scope">
            第 {{ scope.row.currentNodeIndex + 1 }} 节点
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="viewDetail(scope.row)">
              审批详情
            </el-button>
            <el-button type="primary" link @click="handleApprove(scope.row)">
              通过
            </el-button>
            <el-button type="danger" link @click="handleReject(scope.row)">
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && applications.length === 0" description="暂无待审批申请" />
    </el-card>
    
    <el-dialog
      v-model="approveVisible"
      title="审批通过"
      width="500px"
    >
      <el-form label-width="100px">
        <el-form-item label="审批意见">
          <el-input
            v-model="approveComment"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approveVisible = false">取消</el-button>
          <el-button type="primary" @click="submitApprove" :loading="approveLoading">
            确认通过
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <el-dialog
      v-model="rejectVisible"
      title="审批拒绝"
      width="500px"
    >
      <el-form label-width="100px">
        <el-form-item label="拒绝原因">
          <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="3"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input
            v-model="rejectComment"
            type="textarea"
            :rows="2"
            placeholder="请输入备注说明（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectVisible = false">取消</el-button>
          <el-button type="danger" @click="submitReject" :loading="rejectLoading">
            确认拒绝
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applicationApi, approvalApi } from '../api'

export default {
  name: 'PendingApprovals',
  props: {
    currentUserId: String,
    currentUserName: String
  },
  setup(props) {
    const router = useRouter()
    const loading = ref(false)
    const applications = ref([])
    const currentApplication = ref(null)
    
    const approveVisible = ref(false)
    const approveLoading = ref(false)
    const approveComment = ref('')
    
    const rejectVisible = ref(false)
    const rejectLoading = ref(false)
    const rejectReason = ref('')
    const rejectComment = ref('')
    
    const fetchData = async () => {
      loading.value = true
      try {
        const res = await applicationApi.getByApprover(props.currentUserId)
        applications.value = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      } catch (error) {
        console.error(error)
        ElMessage.error('获取待审批列表失败')
      } finally {
        loading.value = false
      }
    }
    
    onMounted(() => {
      fetchData()
    })
    
    watch(() => props.currentUserId, () => {
      fetchData()
    })
    
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
    
    const formatAmount = (amount) => {
      if (amount == null) return '-'
      return `¥${Number(amount).toFixed(2)}`
    }
    
    const getCurrentNode = (row) => {
      if (row.approvalNodes && row.approvalNodes.length > 0) {
        return row.approvalNodes.find(n => n.status === 'IN_PROGRESS')
      }
      return null
    }
    
    const viewDetail = (row) => {
      router.push(`/detail/${row.id}`)
    }
    
    const handleApprove = (row) => {
      currentApplication.value = row
      approveComment.value = ''
      approveVisible.value = true
    }
    
    const submitApprove = async () => {
      const currentNode = getCurrentNode(currentApplication.value)
      if (!currentNode) {
        ElMessage.error('找不到当前审批节点')
        return
      }
      
      approveLoading.value = true
      try {
        await approvalApi.approve(currentNode.id, {
          approverId: props.currentUserId,
          comment: approveComment.value
        })
        ElMessage.success('审批通过')
        approveVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
        ElMessage.error(error.response?.data?.message || '审批失败')
      } finally {
        approveLoading.value = false
      }
    }
    
    const handleReject = (row) => {
      currentApplication.value = row
      rejectReason.value = ''
      rejectComment.value = ''
      rejectVisible.value = true
    }
    
    const submitReject = async () => {
      if (!rejectReason.value.trim()) {
        ElMessage.warning('请输入拒绝原因')
        return
      }
      
      const currentNode = getCurrentNode(currentApplication.value)
      if (!currentNode) {
        ElMessage.error('找不到当前审批节点')
        return
      }
      
      rejectLoading.value = true
      try {
        await approvalApi.reject(currentNode.id, {
          approverId: props.currentUserId,
          reason: rejectReason.value,
          comment: rejectComment.value
        })
        ElMessage.success('已拒绝')
        rejectVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
        ElMessage.error(error.response?.data?.message || '操作失败')
      } finally {
        rejectLoading.value = false
      }
    }
    
    return {
      loading,
      applications,
      approveVisible,
      approveLoading,
      approveComment,
      rejectVisible,
      rejectLoading,
      rejectReason,
      rejectComment,
      getTypeText,
      formatAmount,
      viewDetail,
      handleApprove,
      submitApprove,
      handleReject,
      submitReject
    }
  }
}
</script>
