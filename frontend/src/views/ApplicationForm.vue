<template>
  <div class="form-container">
    <el-card shadow="hover">
      <template #header>
        <span>新建申请</span>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 600px"
      >
        <el-form-item label="申请标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入申请标题" />
        </el-form-item>
        
        <el-form-item label="申请描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入申请描述"
          />
        </el-form-item>
        
        <el-form-item label="申请类型" prop="applicationType">
          <el-select v-model="form.applicationType" placeholder="请选择申请类型" style="width: 100%">
            <el-option label="采购申请" value="PURCHASE" />
            <el-option label="请假申请" value="LEAVE" />
            <el-option label="预算申请" value="BUDGET" />
            <el-option label="报销申请" value="EXPENSE" />
            <el-option label="合同审批" value="CONTRACT" />
            <el-option label="物品采购" value="ITEM_PURCHASE" />
            <el-option label="人事审批" value="HR_APPROVAL" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="申请金额" prop="amount">
          <el-input-number
            v-model="form.amount"
            :precision="2"
            :min="0"
            placeholder="请输入申请金额"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="紧急程度" prop="urgencyLevel">
          <el-select v-model="form.urgencyLevel" placeholder="请选择紧急程度" style="width: 100%">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="风险等级" prop="riskLevel">
          <el-select v-model="form.riskLevel" placeholder="请选择风险等级" style="width: 100%">
            <el-option label="低风险" value="LOW" />
            <el-option label="中风险" value="MEDIUM" />
            <el-option label="高风险" value="HIGH" />
            <el-option label="极高风险" value="CRITICAL" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="申请部门" prop="department">
          <el-select v-model="form.department" placeholder="请选择申请部门" style="width: 100%">
            <el-option label="研发部" value="研发部" />
            <el-option label="市场部" value="市场部" />
            <el-option label="财务部" value="财务部" />
            <el-option label="人事部" value="人事部" />
            <el-option label="销售部" value="销售部" />
            <el-option label="法务部" value="法务部" />
            <el-option label="运营部" value="运营部" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">
            创建并提交申请
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { applicationApi } from '../api'

export default {
  name: 'ApplicationForm',
  props: {
    currentUserId: String,
    currentUserName: String
  },
  setup(props) {
    const router = useRouter()
    const { proxy } = getCurrentInstance()
    const loading = ref(false)
    const formRef = ref(null)
    
    const form = reactive({
      title: '',
      description: '',
      applicationType: '',
      amount: 0,
      urgencyLevel: 'MEDIUM',
      riskLevel: 'LOW',
      department: ''
    })
    
    const rules = {
      title: [
        { required: true, message: '请输入申请标题', trigger: 'blur' }
      ],
      applicationType: [
        { required: true, message: '请选择申请类型', trigger: 'change' }
      ],
      amount: [
        { required: true, message: '请输入申请金额', trigger: 'blur' }
      ],
      urgencyLevel: [
        { required: true, message: '请选择紧急程度', trigger: 'change' }
      ],
      riskLevel: [
        { required: true, message: '请选择风险等级', trigger: 'change' }
      ],
      department: [
        { required: true, message: '请选择申请部门', trigger: 'change' }
      ]
    }
    
    const submitForm = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return
      
      loading.value = true
      try {
        const submitData = {
          ...form,
          applicantId: props.currentUserId,
          applicantName: props.currentUserName
        }
        
        const createRes = await applicationApi.create(submitData)
        const appId = createRes.data.id
        
        await applicationApi.submit(appId)
        
        ElMessage.success('申请创建并提交成功！')
        router.push('/my-applications')
      } catch (error) {
        console.error(error)
        ElMessage.error(error.response?.data?.message || '创建申请失败')
      } finally {
        loading.value = false
      }
    }
    
    const resetForm = () => {
      formRef.value.resetFields()
    }
    
    return {
      formRef,
      form,
      rules,
      loading,
      submitForm,
      resetForm
    }
  }
}
</script>

<style scoped>
.form-container {
  max-width: 800px;
}
</style>
