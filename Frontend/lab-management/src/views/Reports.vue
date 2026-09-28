<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <h1 class="text-2xl font-bold text-gray-900">报告管理</h1>
      <div class="flex gap-2">
        <button v-if="hasPermission('report', 'add')" @click="openSubmitModal" class="btn-primary"><FileText class="w-4 h-4" />提交报告</button>
        <button v-if="hasPermission('report', 'set')" @click="exportToCSV" class="btn-secondary"><Download class="w-4 h-4" />导出数据</button>
      </div>
    </div>

    <!-- Stats Cards: 只有管理权限(report:set)才显示 -->
    <div v-if="hasPermission('report', 'set')" class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div class="card"><p class="text-sm text-gray-500 mb-1">报告总数</p><p class="text-3xl font-bold text-gray-900">{{ stats.total }}</p></div>
      <div class="card"><p class="text-sm text-gray-500 mb-1">待评分</p><p class="text-3xl font-bold text-orange-600">{{ stats.pending }}</p></div>
      <div class="card"><p class="text-sm text-gray-500 mb-1">已评分</p><p class="text-3xl font-bold text-green-600">{{ stats.graded }}</p></div>
      <div class="card"><p class="text-sm text-gray-500 mb-1">平均分</p><p class="text-3xl font-bold text-primary-600">{{ stats.avgScore }}</p></div>
    </div>
    <div v-else-if="hasPermission('report', 'get')" class="card text-center py-4 text-gray-500">
      <p>我的报告（仅显示本人提交的报告）</p>
    </div>

    <!-- Filters -->
    <div class="card">
      <div class="flex flex-col md:flex-row gap-4">
        <div class="flex-1 relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input v-model="searchTerm" placeholder="搜索报告标题、学生姓名..." class="w-full border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent pl-10 pr-4 py-2" />
        </div>
        <select v-model="statusFilter" class="input-field w-full md:w-40">
          <option value="all">全部状态</option>
          <option value="pending">待评分</option>
          <option value="graded">已评分</option>
          <option value="rejected">已退回</option>
        </select>
        <select v-model="courseFilter" class="input-field w-full md:w-40">
          <option value="all">全部课题</option>
          <option v-for="c in courses" :key="c" :value="c">{{ c }}</option>
        </select>
      </div>
    </div>

    <!-- Reports Table -->
    <div class="card overflow-hidden">
      <div class="table-container">
        <table class="data-table">
          <thead>
          <tr>
            <th>报告标题</th>
            <th>学生</th>
            <th>课题</th>
            <th>提交日期</th>
            <th>状态</th>
            <th>分数</th>
            <th>审核人</th>
            <th>操作</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="r in filteredReports" :key="r.id">
            <td>
              <div class="font-medium text-gray-900">{{ r.title }}</div>
              <div class="text-xs text-gray-500">工号: {{ r.studentId || '-' }}</div>
            </td>
            <td>{{ r.student }}</td>
            <td>{{ r.course }}</td>
            <td>{{ r.submitDate }}</td>
            <td><span :class="getStatusBadgeClass(r.status)">{{ getStatusLabel(r.status) }}</span></td>
            <td><span v-if="r.score !== null" :class="getScoreClass(r.score)">{{ r.score }}</span><span v-else class="text-gray-400">-</span></td>
            <td>
              <span v-if="r.reviewer" class="text-sm text-gray-700">{{ r.reviewer }}</span>
              <span v-else class="text-gray-400">-</span>
            </td>
            <td>
              <div class="flex gap-2">
                <button v-if="hasPermission('report', 'get')" @click="viewDetail(r)" class="p-2 hover:bg-gray-100 rounded-lg"><Eye class="w-4 h-4" /></button>
                <template v-if="hasPermission('report', 'set') && r.status === 'pending'">
                  <button @click="openGradeModal(r)" class="p-2 hover:bg-green-50 rounded-lg text-green-600"><Star class="w-4 h-4" /></button>
                  <button @click="openRejectModal(r)" class="p-2 hover:bg-red-50 rounded-lg text-red-600"><XCircle class="w-4 h-4" /></button>
                </template>
              </div>
            </td>
          </tr>
          <tr v-if="filteredReports.length === 0">
            <td colspan="8" class="text-center py-8 text-gray-400">暂无数据</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 提交报告弹窗 -->
    <div v-if="showSubmitModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b flex justify-between">
          <h2 class="text-xl font-bold">提交报告</h2>
          <button @click="showSubmitModal=false"><X class="w-5 h-5" /></button>
        </div>
        <form @submit.prevent="handleSubmitReport" class="p-6 space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div><label>学生</label><input type="text" v-model="submitForm.student" disabled class="input-field bg-gray-50" /></div>
            <div><label>提交日期</label><input type="text" v-model="submitForm.submitDate" disabled class="input-field bg-gray-50" /></div>
          </div>
          <div><label>课题</label><input type="text" v-model="submitForm.course" placeholder="请输入课题名称" class="input-field" /></div>
          <div><label>报告标题 *</label><input type="text" v-model="submitForm.title" required class="input-field" /></div>
          <div><label>报告内容 *</label><textarea v-model="submitForm.content" required class="input-field min-h-[150px]"></textarea></div>
          <div>
            <label>上传附件</label>
            <div
              class="mt-1 border-2 border-dashed rounded-lg p-4 text-center transition-colors cursor-pointer"
              :class="isDragging ? 'border-primary-500 bg-primary-50' : 'border-gray-300 hover:border-primary-400'"
              @click="$refs.attachmentInput.click()"
              @dragover="handleDragOver"
              @dragleave="handleDragLeave"
              @drop="handleDrop"
            >
              <Upload class="w-8 h-8 mx-auto text-gray-400 mb-2" />
              <p class="text-sm text-gray-500" v-if="!isDragging">点击或拖拽文件到此处上传</p>
              <p class="text-sm text-primary-600 font-medium" v-else>松开鼠标以上传文件</p>
              <p class="text-xs text-gray-400 mt-1">支持 doc、docx、pdf、xls、xlsx、ppt、pptx、zip、rar 等，单个文件不超过 10MB</p>
            </div>
            <input ref="attachmentInput" type="file" class="hidden" accept=".doc,.docx,.pdf,.xls,.xlsx,.ppt,.pptx,.zip,.rar,.jpg,.png,.txt" @change="handleAttachmentUpload" />
            <div v-if="submitForm.attachments?.length" class="mt-2 space-y-2">
              <div v-for="(att, index) in submitForm.attachments" :key="index" class="flex items-center justify-between p-2 bg-gray-50 rounded-lg text-sm">
                <div class="flex items-center gap-2 min-w-0">
                  <Paperclip class="w-4 h-4 text-gray-400 flex-shrink-0" />
                  <span class="truncate">{{ att.filename }}</span>
                  <span class="text-gray-400 flex-shrink-0">{{ formatFileSize(att.size) }}</span>
                </div>
                <button type="button" @click="removeAttachment(index)" class="text-red-500 hover:text-red-700 flex-shrink-0">
                  <X class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
          <div class="flex gap-3">
            <button type="button" @click="showSubmitModal=false" class="flex-1 btn-secondary">取消</button>
            <button type="submit" class="flex-1 btn-primary">提交报告</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 退回理由弹窗 -->
    <div v-if="showRejectModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-md">
        <div class="p-6 border-b flex justify-between">
          <h2 class="text-xl font-bold">退回报告</h2>
          <button @click="showRejectModal=false"><X class="w-5 h-5" /></button>
        </div>
        <form @submit.prevent="confirmReject" class="p-6 space-y-4">
          <div><label>退回理由 *</label><textarea v-model="rejectForm.reason" required class="input-field min-h-[100px]" placeholder="请填写退回理由..."></textarea></div>
          <div class="flex gap-3">
            <button type="button" @click="showRejectModal=false" class="flex-1 btn-secondary">取消</button>
            <button type="submit" class="flex-1 btn-primary">确认退回</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Detail Modal -->
    <div v-if="showDetailModal && selectedReport" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b flex justify-between">
          <h2 class="text-xl font-bold">报告详情</h2>
          <button @click="showDetailModal=false"><X class="w-5 h-5" /></button>
        </div>
        <div class="p-6 space-y-6">
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div><span class="text-gray-500">报告标题:</span> {{ selectedReport.title }}</div>
            <div><span>学生:</span> {{ selectedReport.student }} ({{ selectedReport.studentId || '-' }})</div>
            <div><span>课题:</span> {{ selectedReport.course }}</div>
            <div><span>提交日期:</span> {{ selectedReport.submitDate }}</div>
            <div><span>状态:</span> <span :class="getStatusBadgeClass(selectedReport.status)">{{ getStatusLabel(selectedReport.status) }}</span></div>
            <div><span>分数:</span> <span v-if="selectedReport.score !== null" :class="getScoreClass(selectedReport.score)">{{ selectedReport.score }}</span><span v-else class="text-gray-400">未评分</span></div>
            <div v-if="selectedReport.reviewer"><span class="text-gray-500">审核人:</span> {{ selectedReport.reviewer }}</div>
            <div v-if="selectedReport.reviewTime"><span class="text-gray-500">审核时间:</span> {{ selectedReport.reviewTime }}</div>
          </div>
          <div><h3 class="font-semibold mb-3">报告内容</h3><div class="p-4 bg-gray-50 rounded-lg text-sm whitespace-pre-wrap">{{ selectedReport.content }}</div></div>
          <div v-if="parseAttachments(selectedReport.attachment).length">
            <h3 class="font-semibold mb-3">附件</h3>
            <div class="space-y-2">
              <a v-for="(att, i) in parseAttachments(selectedReport.attachment)" :key="i"
                 :href="att.url" :download="att.filename"
                 class="flex items-center gap-2 p-3 bg-gray-50 rounded-lg text-sm hover:bg-gray-100 transition-colors">
                <Paperclip class="w-4 h-4 text-primary-500" />
                <span class="flex-1 truncate">{{ att.filename }}</span>
                <Download class="w-4 h-4 text-gray-400" />
              </a>
            </div>
          </div>
          <div v-if="selectedReport.feedback"><h3 class="font-semibold mb-3">评语</h3><div class="p-4 bg-blue-50 rounded-lg text-sm text-blue-800">{{ selectedReport.feedback }}</div></div>
          <div v-if="selectedReport.rejectReason"><h3 class="font-semibold mb-3">退回理由</h3><div class="p-4 bg-red-50 rounded-lg text-sm text-red-700">{{ selectedReport.rejectReason }}</div></div>
        </div>
      </div>
    </div>

    <!-- Grade Modal -->
    <div v-if="showGradeModal && selectedReport" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-md">
        <div class="p-6 border-b flex justify-between">
          <h2 class="text-xl font-bold">评分</h2>
          <button @click="showGradeModal=false"><X class="w-5 h-5" /></button>
        </div>
        <form @submit.prevent="handleGrade" class="p-6 space-y-4">
          <div><label>报告</label><input type="text" :value="selectedReport.title" disabled class="input-field bg-gray-50" /></div>
          <div><label>学生</label><input type="text" :value="selectedReport.student" disabled class="input-field bg-gray-50" /></div>
          <div><label>分数 (0-100)</label><input type="number" v-model="gradeForm.score" required min="0" max="100" class="input-field" /></div>
          <div><label>评语</label><textarea v-model="gradeForm.feedback" required class="input-field min-h-[100px]"></textarea></div>
          <div class="flex gap-3">
            <button type="button" @click="showGradeModal=false" class="flex-1 btn-secondary">取消</button>
            <button type="submit" class="flex-1 btn-primary">提交评分</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, CheckCircle, XCircle, Star, Download, Eye, X, FileText, Upload, Paperclip } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { hasPermission } from '@/utils/permission'
import { addNotification } from '@/utils/notification'
import { reportApi, commonApi, http } from '@/services/api'

const reports = ref([])
const personnelList = ref([]) // 人员列表，用于获取工号
const searchTerm = ref('')
const statusFilter = ref('all')
const courseFilter = ref('all')
const showDetailModal = ref(false)
const showGradeModal = ref(false)
const showSubmitModal = ref(false)
const showRejectModal = ref(false)
const selectedReport = ref(null)
const gradeForm = ref({ score: '', feedback: '' })
const submitForm = ref({
  title: '',
  content: '',
  student: '',      // 当前用户名
  course: '',       // 课题（用户自由输入）
  submitDate: '',   // 提交日期
  attachments: []   // [{url, filename, size}]
})
const rejectForm = ref({ reason: '' })
let pendingRejectId = null
const uploadingAttachment = ref(false)
const isDragging = ref(false)

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// 上传附件（支持点击和拖拽）
const uploadFile = async (file) => {
  if (!file) return
  if (file.size > 10 * 1024 * 1024) {
    alert('文件大小不能超过 10MB')
    return
  }
  uploadingAttachment.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const result = await http.upload('/api/reports/uploadAttachment', formData)
    submitForm.value.attachments.push({
      url: result.data.url,
      filename: result.data.filename || file.name,
      size: file.size
    })
  } catch (e) {
    alert('附件上传失败：' + e.message)
  } finally {
    uploadingAttachment.value = false
  }
}

// 点击上传
const handleAttachmentUpload = async (event) => {
  const file = event.target.files[0]
  await uploadFile(file)
  // 清空 input 以便重复选择同一文件
  event.target.value = ''
}

// 拖拽上传
const handleDragOver = (event) => {
  event.preventDefault()
  isDragging.value = true
}

const handleDragLeave = (event) => {
  event.preventDefault()
  isDragging.value = false
}

const handleDrop = async (event) => {
  event.preventDefault()
  isDragging.value = false
  const files = event.dataTransfer.files
  if (files.length > 0) {
    await uploadFile(files[0])
  }
}

// 移除附件
const removeAttachment = (index) => {
  submitForm.value.attachments.splice(index, 1)
}

// 解析附件字符串为列表
const parseAttachments = (attachment) => {
  if (!attachment) return []
  return attachment.split(',').filter(Boolean).map(item => {
    const parts = item.split('|')
    return { url: parts[0], filename: parts[1] || parts[0].split('/').pop() }
  })
}

// 后端状态 approved -> 前端 graded 的映射
const mapStatusFromBackend = (status) => {
  if (status === 'approved') return 'graded'
  return status
}

// 通过 userId 获取人员信息（姓名和工号）
const getPersonnelByUserId = (userId) => {
  if (!userId) return { name: '', employeeNo: '' }
  const personnel = personnelList.value.find(p => p.userId === userId || p.user_id === userId)
  return {
    name: personnel?.name || '',
    employeeNo: personnel?.employeeNo || personnel?.employee_no || ''
  }
}

const loadData = async () => {
  try {
    // 先加载人员列表，用于获取姓名和工号
    const personnelRes = await commonApi.getSimpleUserList()
    personnelList.value = personnelRes.content || []
    // 加载报告列表（无权限时不发请求）
    if (hasPermission('report', 'get')) {
      const res = await reportApi.getList()
      // 后端返回的数据需要做字段映射
      reports.value = (res.content || []).map(r => {
        const personnelInfo = getPersonnelByUserId(r.userId)
        const reviewerInfo = getPersonnelByUserId(r.reviewerId)
        return {
          ...r,
          status: mapStatusFromBackend(r.status),
          // 后端 comment -> 前端 feedback
          feedback: r.comment || r.feedback || '',
          // 后端没有 rejectReason 字段，退回时 comment 同时作为 rejectReason
          rejectReason: r.status === 'rejected' ? (r.comment || '') : null,
          // 从人员表获取姓名和工号
          student: personnelInfo.name || r.student || '',
          studentId: personnelInfo.employeeNo,
          // 后端有 course 字段
          course: r.course || '',
          // submitTime -> submitDate
          submitDate: r.submitTime ? new Date(r.submitTime).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }) : '',
          // 审核人信息
          reviewer: reviewerInfo.name || r.reviewer || '',
          // 审核时间
          reviewTime: r.reviewTime ? new Date(r.reviewTime).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : ''
        }
      })
    }
  } catch (e) {
    console.error('加载报告数据失败:', e)
  }
}

const stats = computed(() => {
  const total = reports.value.length
  const pending = reports.value.filter(r => r.status === 'pending').length
  const graded = reports.value.filter(r => r.status === 'graded')
  const avgScore = graded.length ? Math.round(graded.reduce((s, r) => s + r.score, 0) / graded.length) : 0
  return { total, pending, graded: graded.length, avgScore }
})

const courses = computed(() => [...new Set(reports.value.map(r => r.course))])
const filteredReports = computed(() => {
  let list = reports.value
  if (searchTerm.value) list = list.filter(r => r.title.includes(searchTerm.value) || r.student.includes(searchTerm.value))
  if (statusFilter.value !== 'all') list = list.filter(r => r.status === statusFilter.value)
  if (courseFilter.value !== 'all') list = list.filter(r => r.course === courseFilter.value)
  return list
})

const getStatusBadgeClass = (status) => ({ pending: 'status-pending', graded: 'status-approved', rejected: 'status-rejected' }[status] || '')
const getStatusLabel = (status) => ({ pending: '待评分', graded: '已评分', rejected: '已退回' }[status] || status)
const getScoreClass = (score) => {
  if (score >= 90) return 'text-green-600 font-bold'
  if (score >= 80) return 'text-blue-600 font-bold'
  if (score >= 60) return 'text-yellow-600 font-bold'
  return 'text-red-600 font-bold'
}

const openSubmitModal = async () => {
  const labUser = JSON.parse(localStorage.getItem('labUser') || '{}')
  const userId = labUser.id || labUser.userId
  const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })

  // 确保人员列表已加载
  if (personnelList.value.length === 0) {
    const personnelRes = await commonApi.getSimpleUserList()
    personnelList.value = personnelRes.content || []
  }

  const personnelInfo = getPersonnelByUserId(userId)
  submitForm.value = {
    title: '',
    content: '',
    student: personnelInfo.name || labUser.name || labUser.username || '未知用户',
    course: '',
    submitDate: today,
    attachments: []
  }
  showSubmitModal.value = true
}

const handleSubmitReport = async () => {
  try {
    const labUser = JSON.parse(localStorage.getItem('labUser') || '{}')
    const userId = labUser.id || labUser.userId
    const personnelInfo = getPersonnelByUserId(userId)
    // 发送后端有的字段：title, content, userId, attachment, course
    const payload = {
      title: submitForm.value.title,
      content: submitForm.value.content,
      userId: userId,
      course: submitForm.value.course || null,
      attachment: submitForm.value.attachments.length
        ? submitForm.value.attachments.map(a => `${a.url}|${a.filename}`).join(',')
        : null
    }
    const createRes = await reportApi.create(payload)
    ElMessage.success(createRes.msg || '报告提交成功')
    const newReport = createRes.data
    // 后端可能返回 null 或完整报告对象
    const mapped = {
      ...(newReport || {}),
      id: newReport?.id || Date.now(),
      title: submitForm.value.title,
      content: submitForm.value.content,
      attachment: payload.attachment,
      status: mapStatusFromBackend(newReport?.status || 'pending'),
      feedback: newReport?.comment || '',
      rejectReason: null,
      student: personnelInfo.name || submitForm.value.student,
      studentId: personnelInfo.employeeNo,
      course: submitForm.value.course,
      submitDate: submitForm.value.submitDate
    }
    reports.value.push(mapped)
    showSubmitModal.value = false
    // 清空表单
    submitForm.value = { title: '', content: '', student: '', course: '', submitDate: '', attachments: [] }
    addNotification('报告提交', `提交了报告《${mapped.title}》`, 'reports')
  } catch (e) {
    console.error('提交报告失败:', e)
    ElMessage.error(e.message || '提交报告失败，请重试')
  }
}

const viewDetail = (r) => { selectedReport.value = r; showDetailModal.value = true }
const openGradeModal = (r) => { selectedReport.value = r; gradeForm.value = { score: '', feedback: '' }; showGradeModal.value = true }

const openRejectModal = (r) => {
  pendingRejectId = r.id
  rejectForm.value.reason = ''
  showRejectModal.value = true
}
const confirmReject = async () => {
  if (!rejectForm.value.reason.trim()) {
    alert('请填写退回理由')
    return
  }
  try {
    const labUser = JSON.parse(localStorage.getItem('labUser') || '{}')
    const userId = labUser.id || labUser.userId
    const reviewerInfo = getPersonnelByUserId(userId)
    const rejectRes = await reportApi.reject(pendingRejectId, { comment: rejectForm.value.reason, reviewerId: userId })
    ElMessage.success(rejectRes.msg || '报告已退回')
    const idx = reports.value.findIndex(r => r.id === pendingRejectId)
    if (idx !== -1) {
      reports.value[idx].status = 'rejected'
      reports.value[idx].rejectReason = rejectForm.value.reason
      // 更新审核人信息和审核时间
      reports.value[idx].reviewer = reviewerInfo.name || labUser.name || labUser.username || '未知用户'
      reports.value[idx].reviewTime = new Date().toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
      addNotification('报告退回', `${reports.value[idx].student} 的报告《${reports.value[idx].title}》已退回，理由：${rejectForm.value.reason}`, 'reports')
    }
  } catch (e) {
    console.error('退回报告失败:', e)
    ElMessage.error(e.message || '退回报告失败，请重试')
  }
  showRejectModal.value = false
  pendingRejectId = null
}

const handleGrade = async () => {
  try {
    const labUser = JSON.parse(localStorage.getItem('labUser') || '{}')
    const userId = labUser.id || labUser.userId
    const reviewerInfo = getPersonnelByUserId(userId)
    const gradeRes = await reportApi.grade(selectedReport.value.id, {
      score: parseInt(gradeForm.value.score),
      comment: gradeForm.value.feedback,
      reviewerId: userId
    })
    ElMessage.success(gradeRes.msg || '评分成功')
    const idx = reports.value.findIndex(r => r.id === selectedReport.value.id)
    if (idx !== -1) {
      reports.value[idx].status = 'graded'
      reports.value[idx].score = parseInt(gradeForm.value.score)
      reports.value[idx].feedback = gradeForm.value.feedback
      reports.value[idx].rejectReason = null
      // 更新审核人信息和审核时间
      reports.value[idx].reviewer = reviewerInfo.name || labUser.name || labUser.username || '未知用户'
      reports.value[idx].reviewTime = new Date().toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
      addNotification('报告评分', `${selectedReport.value.student} 的报告《${selectedReport.value.title}》已评分 ${gradeForm.value.score} 分`, 'reports')
    }
  } catch (e) {
    console.error('评分失败:', e)
    ElMessage.error(e.message || '评分失败，请重试')
  }
  showGradeModal.value = false
}

const exportToCSV = async () => {
  try {
    const blob = await reportApi.export()
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '实验室报告.csv')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('导出失败:', e)
    alert('导出失败，请重试')
  }
}

onMounted(loadData)
</script>
