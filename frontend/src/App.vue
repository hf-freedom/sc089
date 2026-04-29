<template>
  <el-container style="height: 100vh">
    <el-header style="text-align: right; font-size: 12px; background: #545c64; color: #fff; padding: 0 20px; display: flex; align-items: center; justify-content: space-between;">
      <h2 style="margin: 0; color: #fff; font-size: 18px;">多阶段审批流系统</h2>
      <div style="display: flex; align-items: center; gap: 20px;">
        <span>当前用户: {{ currentUserName }}</span>
        <el-select v-model="currentUserId" size="small" @change="handleUserChange" style="width: 200px;">
          <el-option label="申请人 - 李明 (user001)" value="user001"></el-option>
          <el-option label="部门经理 - 张三 (dept_manager_001)" value="dept_manager_001"></el-option>
          <el-option label="财务经理 - 李四 (finance_manager_001)" value="finance_manager_001"></el-option>
          <el-option label="财务总监 - 王五 (finance_director_001)" value="finance_director_001"></el-option>
          <el-option label="CEO - 赵六 (ceo_001)" value="ceo_001"></el-option>
          <el-option label="法务 - 钱七 (legal_001)" value="legal_001"></el-option>
          <el-option label="风控经理 - 孙八 (risk_manager_001)" value="risk_manager_001"></el-option>
          <el-option label="人事经理 - 周九 (hr_manager_001)" value="hr_manager_001"></el-option>
        </el-select>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px" style="background-color: #545c64">
        <el-menu
          :default-active="$route.path"
          class="el-menu-vertical-demo"
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
          router
        >
          <el-menu-item index="/form">
            <el-icon><Document /></el-icon>
            <span>申请表单</span>
          </el-menu-item>
          <el-menu-item index="/my-applications">
            <el-icon><List /></el-icon>
            <span>我的申请</span>
          </el-menu-item>
          <el-menu-item index="/pending">
            <el-icon><Clock /></el-icon>
            <span>待我审批</span>
          </el-menu-item>
          <el-menu-item index="/rules">
            <el-icon><Setting /></el-icon>
            <span>审批规则</span>
          </el-menu-item>
          <el-menu-item index="/timeout">
            <el-icon><Timer /></el-icon>
            <span>超时管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view :currentUserId="currentUserId" :currentUserName="currentUserName" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { ref, computed } from 'vue'
import { Document, List, Clock, Setting, Timer } from '@element-plus/icons-vue'

export default {
  name: 'App',
  components: {
    Document,
    List,
    Clock,
    Setting,
    Timer
  },
  setup() {
    const userMap = {
      'user001': '李明',
      'dept_manager_001': '张三',
      'finance_manager_001': '李四',
      'finance_director_001': '王五',
      'ceo_001': '赵六',
      'legal_001': '钱七',
      'risk_manager_001': '孙八',
      'hr_manager_001': '周九'
    }
    
    const currentUserId = ref('user001')
    const currentUserName = computed(() => userMap[currentUserId.value] || '未知用户')
    
    const handleUserChange = () => {
      // 用户切换时刷新页面数据
    }
    
    return {
      currentUserId,
      currentUserName,
      handleUserChange
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html, body {
  height: 100%;
}
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
}
</style>
