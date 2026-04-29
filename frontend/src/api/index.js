import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export const applicationApi = {
  create: (data) => api.post('/applications', data),
  submit: (id) => api.post(`/applications/${id}/submit`),
  withdraw: (id, data) => api.post(`/applications/${id}/withdraw`, data),
  get: (id) => api.get(`/applications/${id}`),
  getByApplicant: (applicantId) => api.get(`/applications/applicant/${applicantId}`),
  getByApprover: (approverId) => api.get(`/applications/approver/${approverId}`),
  getAll: () => api.get('/applications'),
  getStatus: (id) => api.get(`/applications/status/${id}`),
  modifyAmount: (id, data) => api.post(`/applications/${id}/modify-amount`, data)
}

export const approvalApi = {
  approve: (nodeId, data) => api.post(`/approvals/${nodeId}/approve`, data),
  reject: (nodeId, data) => api.post(`/approvals/${nodeId}/reject`, data),
  getPendingTimeouts: () => api.get('/approvals/pending-timeouts'),
  processTimeout: (data) => api.post('/approvals/process-timeout', data)
}

export const ruleApi = {
  getAll: () => api.get('/rules'),
  get: (id) => api.get(`/rules/${id}`),
  create: (data) => api.post('/rules', data),
  update: (id, data) => api.put(`/rules/${id}`, data),
  delete: (id) => api.delete(`/rules/${id}`),
  toggle: (id) => api.post(`/rules/${id}/toggle`)
}

export default api
