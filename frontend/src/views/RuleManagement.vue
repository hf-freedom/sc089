<template>
  <div>
    <el-card shadow="hover">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>审批规则管理</span>
          <el-button type="primary" @click="openCreateDialog">
            <el-icon><Plus /></el-icon>
            新增规则
          </el-button>
        </div>
      </template>
      
      <el-table :data="rules" style="width: 100%" v-loading="loading" border>
        <el-table-column prop="id" label="规则ID" width="100" />
        <el-table-column prop="ruleName" label="规则名称" min-width="180" />
        <el-table-column prop="priority" label="优先级" width="80" sortable>
          <template #default="scope">
            <el-tag size="small">{{ scope.row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicationType" label="申请类型" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.applicationType" size="small">
              {{ getTypeText(scope.row.applicationType) }}
            </el-tag>
            <span v-else style="color: #909399;">所有类型</span>
          </template>
        </el-table-column>
        <el-table-column label="金额范围" width="180">
          <template #default="scope">
            <div>
              <span v-if="scope.row.minAmount != null">
                ≥ {{ formatAmount(scope.row.minAmount) }}
              </span>
              <span v-if="scope.row.minAmount != null && scope.row.maxAmount != null">
                &nbsp;/&nbsp;
              </span>
              <span v-if="scope.row.maxAmount != null">
                ≤ {{ formatAmount(scope.row.maxAmount) }}
              </span>
              <span v-if="scope.row.minAmount == null && scope.row.maxAmount == null" style="color: #909399;">
                不限金额
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.riskLevel" :type="getRiskType(scope.row.riskLevel)" size="small">
              {{ getRiskText(scope.row.riskLevel) }}
            </el-tag>
            <span v-else style="color: #909399;">不限</span>
          </template>
        </el-table-column>
        <el-table-column prop="approverName" label="审批人" width="100" />
        <el-table-column prop="nodeName" label="节点名称" width="120" />
        <el-table-column prop="timeoutMinutes" label="超时时间" width="100">
          <template #default="scope">
            <span v-if="scope.row.timeoutMinutes">
              {{ scope.row.timeoutMinutes }} 分钟
            </span>
            <span v-else style="color: #909399;">无限制</span>
          </template>
        </el-table-column>
        <el-table-column prop="timeoutAction" label="超时动作" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.timeoutAction" size="small">
              {{ getTimeoutActionText(scope.row.timeoutAction) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.isActive ? 'success' : 'info'">
              {{ scope.row.isActive ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="toggleRule(scope.row)">
              {{ scope.row.isActive ? '禁用' : '启用' }}
            </el-button>
            <el-button type="primary" link @click="openEditDialog(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" link @click="deleteRule(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && rules.length === 0" description="暂无审批规则" />
    </el-card>
    
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑规则' : '新增规则'"
      width="800px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规则名称" prop="ruleName">
              <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-input-number v-model="form.priority" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="申请类型" prop="applicationType">
              <el-select v-model="form.applicationType" placeholder="请选择（空为所有类型）" clearable style="width: 100%">
                <el-option label="采购申请" value="PURCHASE" />
                <el-option label="请假申请" value="LEAVE" />
                <el-option label="预算申请" value="BUDGET" />
                <el-option label="报销申请" value="EXPENSE" />
                <el-option label="合同审批" value="CONTRACT" />
                <el-option label="物品采购" value="ITEM_PURCHASE" />
                <el-option label="人事审批" value="HR_APPROVAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="风险等级" prop="riskLevel">
              <el-select v-model="form.riskLevel" placeholder="请选择（空为所有等级）" clearable style="width: 100%">
                <el-option label="低风险" value="LOW" />
                <el-option label="中风险" value="MEDIUM" />
                <el-option label="高风险" value="HIGH" />
                <el-option label="极高风险" value="CRITICAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最小金额" prop="minAmount">
              <el-input-number v-model="form.minAmount" :precision="2" :min="0" placeholder="不限制请留空" clearable style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大金额" prop="maxAmount">
              <el-input-number v-model="form.maxAmount" :precision="2" :min="0" placeholder="不限制请留空" clearable style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="审批人ID" prop="approverId">
              <el-input v-model="form.approverId" placeholder="如: dept_manager_001" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="审批人姓名" prop="approverName">
              <el-input v-model="form.approverName" placeholder="如: 张三" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="审批人角色" prop="approverRole">
              <el-input v-model="form.approverRole" placeholder="如: 部门经理" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="节点名称" prop="nodeName">
              <el-input v-model="form.nodeName" placeholder="如: 部门经理审批" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="超时时间(分钟)" prop="timeoutMinutes">
              <el-input-number v-model="form.timeoutMinutes" :min="1" placeholder="超时时间" clearable style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="超时动作" prop="timeoutAction">
              <el-select v-model="form.timeoutAction" placeholder="请选择超时动作" style="width: 100%">
                <el-option label="自动通过" value="AUTO_APPROVE" />
                <el-option label="自动拒绝" value="AUTO_REJECT" />
                <el-option label="升级审批" value="ESCALATE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否启用" prop="isActive">
              <el-switch v-model="form.isActive" active-text="启用" inactive-text="禁用" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="规则描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入规则描述（可选）" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { ruleApi } from '../api'

export default {
  name: 'RuleManagement',
  components: {
    Plus
  },
  setup() {
    const loading = ref(false)
    const submitLoading = ref(false)
    const rules = ref([])
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const formRef = ref(null)
    const editId = ref(null)
    
    const form = reactive({
      ruleName: '',
      priority: 1,
      applicationType: null,
      minAmount: null,
      maxAmount: null,
      riskLevel: null,
      approverId: '',
      approverName: '',
      approverRole: '',
      nodeName: '',
      timeoutMinutes: 24 * 60,
      timeoutAction: 'AUTO_APPROVE',
      isActive: true,
      description: '',
      requireRecheckOnAmountChange: false
    })
    
    const formRules = {
      ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
      priority: [{ required: true, message: '请输入优先级', trigger: 'blur' }],
      approverId: [{ required: true, message: '请输入审批人ID', trigger: 'blur' }],
      approverName: [{ required: true, message: '请输入审批人姓名', trigger: 'blur' }],
      nodeName: [{ required: true, message: '请输入节点名称', trigger: 'blur' }],
      timeoutAction: [{ required: true, message: '请选择超时动作', trigger: 'change' }]
    }
    
    const fetchData = async () => {
      loading.value = true
      try {
        const res = await ruleApi.getAll()
        rules.value = res.data
      } catch (error) {
        console.error(error)
        ElMessage.error('获取规则列表失败')
      } finally {
        loading.value = false
      }
    }
    
    onMounted(() => {
      fetchData()
    })
    
    const getTypeText = (type) => {
      const map = {
        'PURCHASE': '采购申请',
        'LEAVE': '请假申请',
        'BUDGET': '预算申请',
        'EXPENSE': '报销申请',
        'CONTRACT': '合同审批',
        'ITEM_PURCHASE': '物品采购',
        'HR_APPROVAL': '人事审批'
      }
      return map[type] || type
    }
    
    const getRiskType = (level) => {
      const map = {
        'LOW': 'success',
        'MEDIUM': 'warning',
        'HIGH': 'danger',
        'CRITICAL': 'danger'
      }
      return map[level] || ''
    }
    
    const getRiskText = (level) => {
      const map = {
        'LOW': '低风险',
        'MEDIUM': '中风险',
        'HIGH': '高风险',
        'CRITICAL': '极高风险'
      }
      return map[level] || level
    }
    
    const getTimeoutActionText = (action) => {
      const map = {
        'AUTO_APPROVE': '自动通过',
        'AUTO_REJECT': '自动拒绝',
        'ESCALATE': '升级审批'
      }
      return map[action] || action
    }
    
    const formatAmount = (amount) => {
      if (amount == null) return '0'
      return Number(amount).toLocaleString()
    }
    
    const openCreateDialog = () => {
      isEdit.value = false
      editId.value = null
      Object.assign(form, {
        ruleName: '',
        priority: 1,
        applicationType: null,
        minAmount: null,
        maxAmount: null,
        riskLevel: null,
        approverId: '',
        approverName: '',
        approverRole: '',
        nodeName: '',
        timeoutMinutes: 24 * 60,
        timeoutAction: 'AUTO_APPROVE',
        isActive: true,
        description: '',
        requireRecheckOnAmountChange: false
      })
      dialogVisible.value = true
    }
    
    const openEditDialog = (row) => {
      isEdit.value = true
      editId.value = row.id
      Object.assign(form, {
        ruleName: row.ruleName,
        priority: row.priority,
        applicationType: row.applicationType,
        minAmount: row.minAmount,
        maxAmount: row.maxAmount,
        riskLevel: row.riskLevel,
        approverId: row.approverId,
        approverName: row.approverName,
        approverRole: row.approverRole,
        nodeName: row.nodeName,
        timeoutMinutes: row.timeoutMinutes,
        timeoutAction: row.timeoutAction,
        isActive: row.isActive,
        description: row.description,
        requireRecheckOnAmountChange: row.requireRecheckOnAmountChange || false
      })
      dialogVisible.value = true
    }
    
    const submitForm = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return
      
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await ruleApi.update(editId.value, form)
          ElMessage.success('更新成功')
        } else {
          await ruleApi.create(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
        ElMessage.error(error.response?.data?.message || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
    
    const toggleRule = async (row) => {
      try {
        await ruleApi.toggle(row.id)
        ElMessage.success(row.isActive ? '已禁用' : '已启用')
        fetchData()
      } catch (error) {
        console.error(error)
        ElMessage.error('操作失败')
      }
    }
    
    const deleteRule = async (row) => {
      try {
        await ElMessageBox.confirm('确定要删除此规则吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await ruleApi.delete(row.id)
        ElMessage.success('删除成功')
        fetchData()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
          ElMessage.error('删除失败')
        }
      }
    }
    
    return {
      loading,
      submitLoading,
      rules,
      dialogVisible,
      isEdit,
      formRef,
      form,
      formRules,
      getTypeText,
      getRiskType,
      getRiskText,
      getTimeoutActionText,
      formatAmount,
      openCreateDialog,
      openEditDialog,
      submitForm,
      toggleRule,
      deleteRule
    }
  }
}
</script>
