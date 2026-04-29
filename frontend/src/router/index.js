import { createRouter, createWebHistory } from 'vue-router'
import ApplicationForm from '../views/ApplicationForm.vue'
import MyApplications from '../views/MyApplications.vue'
import PendingApprovals from '../views/PendingApprovals.vue'
import ApprovalDetail from '../views/ApprovalDetail.vue'
import RuleManagement from '../views/RuleManagement.vue'
import TimeoutManagement from '../views/TimeoutManagement.vue'

const routes = [
  {
    path: '/',
    redirect: '/form'
  },
  {
    path: '/form',
    name: 'ApplicationForm',
    component: ApplicationForm
  },
  {
    path: '/my-applications',
    name: 'MyApplications',
    component: MyApplications
  },
  {
    path: '/pending',
    name: 'PendingApprovals',
    component: PendingApprovals
  },
  {
    path: '/detail/:id',
    name: 'ApprovalDetail',
    component: ApprovalDetail
  },
  {
    path: '/rules',
    name: 'RuleManagement',
    component: RuleManagement
  },
  {
    path: '/timeout',
    name: 'TimeoutManagement',
    component: TimeoutManagement
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
