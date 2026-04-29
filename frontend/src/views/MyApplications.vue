<template>
  <div>
    <el-card shadow="hover">
      <template #header>
        <span>我的申请</span>
      </template>
      
      <el-table :data="applications" style="width: 100%" v-loading="loading">
        <el-table-column prop="applicationNo" label="申请编号" width="220" />
        <el-table-column prop="title" label="申请标题" min-width="200" />
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
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="viewDetail(scope.row)">
              查看详情
            </el-button>
            <el-button 
              v-if="canWithdraw(scope.row)" 
              type="warning" 
              link 
              @click="handleWithdraw(scope.row)"
            >
              撤回申请
            </el-button>
            <el-button 
              v-if="canResubmit(scope.row)" 
              type="success" 
              link 
              @click="handleResubmit(scope.row)"
            >
              重新提交
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && applications.length === 0" description="暂无申请数据" />
    </el-card>
    
    <el-dialog
      v-model="withdrawVisible"
      title="撤回申请"
      width="500px"
    >
      <el-form label-width="100px">
        <el-form-item label="撤回原因">
          <el-input
            v-model="withdrawReason"
            type="textarea"
            :rows="3"
            placeholder="请输入撤回原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="withdrawVisible = false">取消</el-button>
          <el-button type="primary" @click="submitWithdraw" :loading="withdrawLoading">
            确认撤回
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
import { applicationApi } from '../api'

export default {
  name: 'MyApplications',
  props: {
    currentUserId: String,
    currentUserName: String
  },
  setup(props) {
    const router = useRouter()
    const loading = ref(false)
    const applications = ref([])
    const withdrawVisible = ref(false)
    const withdrawLoading = ref(false)
    const withdrawReason = ref('')
    const currentApplication = ref(null)
    
    const fetchData = async () => {
      loading.value = true
      try {
        const res = await applicationApi.getByApplicant(props.currentUserId)
        applications.value = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      } catch (error) {
        console.error(error)
        ElMessage.error('获取申请列表失败')
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
    
    const formatAmount = (amount) => {
      if (amount == null) return '-'
      return `¥${Number(amount).toFixed(2)}`
    }
    
    const canWithdraw = (row) => {
      return ['SUBMITTED', 'IN_PROGRESS'].includes(row.status)
    }
    
    const canResubmit = (row) => {
      return ['REJECTED', 'WITHDRAWN'].includes(row.status)
    }
    
    const viewDetail = (row) => {
      router.push(`/detail/${row.id}`)
    }
    
    const handleWithdraw = (row) => {
      currentApplication.value = row
      withdrawReason.value = ''
      withdrawVisible.value = true
    }
    
    const submitWithdraw = async () => {
      if (!withdrawReason.value.trim()) {
        ElMessage.warning('请输入撤回原因')
        return
      }
      
      withdrawLoading.value = true
      try {
        await applicationApi.withdraw(currentApplication.value.id, {
          reason: withdrawReason.value,
          operatorId: props.currentUserId
        })
        ElMessage.success('撤回申请成功')
        withdrawVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
        ElMessage.error(error.response?.data?.message || '撤回失败')
      } finally {
        withdrawLoading.value = false
      }
    }
    
    const handleResubmit = async (row) => {
      try {
        await ElMessageBox.confirm('确定要重新提交此申请吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await applicationApi.submit(row.id)
        ElMessage.success('重新提交成功')
        fetchData()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
          ElMessage.error(error.response?.data?.message || '重新提交失败')
        }
      }
    }
    
    return {
      loading,
      applications,
      withdrawVisible,
      withdrawLoading,
      withdrawReason,
      getTypeText,
      getStatusType,
      getStatusText,
      formatAmount,
      canWithdraw,
      canResubmit,
      viewDetail,
      handleWithdraw,
      submitWithdraw,
      handleResubmit
    }
  }
}
</script>
