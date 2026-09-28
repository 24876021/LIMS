<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <h1 class="text-2xl font-bold text-gray-900">预约管理</h1>
      <div class="flex gap-2">
        <button v-if="activeTab === 'labStatus' && hasPermission('reservation', 'set')" @click="openLabManageModal" class="btn-secondary"><MapPin class="w-4 h-4" />管理实验室</button>
        <button v-if="hasPermission('reservation', 'add') && activeTab === 'reservations'" @click="showModal = true" class="btn-primary"><Plus class="w-4 h-4" />新建预约</button>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="flex gap-2">
      <button @click="activeTab = 'labStatus'" :class="activeTab === 'labStatus' ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'" class="px-4 py-2 rounded-lg font-medium transition-colors">
        <MapPin class="w-4 h-4 inline mr-1" />实验室状态
      </button>
      <button @click="activeTab = 'reservations'" :class="activeTab === 'reservations' ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'" class="px-4 py-2 rounded-lg font-medium transition-colors">
        <Calendar class="w-4 h-4 inline mr-1" />预约列表
      </button>
    </div>

    <!-- 实验室状态总览 -->
    <div v-if="activeTab === 'labStatus'">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-gray-700">实验室实时状态</h2>
        <span class="text-sm text-gray-500">当前时间: {{ currentTimeStr }}</span>
      </div>
      <div v-if="labOptions.length === 0" class="text-center py-12 text-gray-500">
        <MapPin class="w-12 h-12 mx-auto mb-3 text-gray-300" />
        <p>暂无实验室数据</p>
        <button v-if="hasPermission('reservation', 'set')" @click="openLabManageModal" class="mt-3 text-primary-600 hover:underline">添加实验室</button>
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div v-for="lab in labStatuses" :key="lab.name" class="card relative overflow-hidden">
          <!-- 状态指示条 -->
          <div class="absolute top-0 left-0 right-0 h-1.5" :class="lab.isOccupied ? 'bg-red-500' : 'bg-green-500'"></div>
          <div class="pt-2">
            <div class="flex items-start justify-between mb-3">
              <div class="flex items-start gap-3">
                <div class="w-12 h-12 rounded-xl flex items-center justify-center" :class="lab.isOccupied ? 'bg-red-100' : 'bg-green-100'">
                  <MapPin class="w-6 h-6" :class="lab.isOccupied ? 'text-red-600' : 'text-green-600'" />
                </div>
                <div>
                  <h3 class="font-semibold text-gray-900">{{ lab.name }}</h3>
                  <p class="text-sm text-gray-500">容量: {{ lab.capacity }}人</p>
                  <p v-if="lab.location" class="text-sm text-gray-500 flex items-center gap-1">
                    <MapPin class="w-3 h-3" />{{ lab.location }}
                  </p>
                </div>
              </div>
              <span class="px-3 py-1 rounded-full text-sm font-medium" :class="lab.isOccupied ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'">
                {{ lab.isOccupied ? '占用中' : '空闲' }}
              </span>
            </div>
            <!-- 占用详情 -->
            <div v-if="lab.isOccupied && lab.currentReservation" class="mt-3 p-3 bg-gray-50 rounded-lg text-sm space-y-1">
              <div class="flex items-center gap-2 text-gray-600">
                <Users class="w-4 h-4" />
                <span>{{ lab.currentReservation.applicant }} · {{ lab.currentReservation.department }}</span>
              </div>
              <div class="flex items-center gap-2 text-gray-600">
                <Clock class="w-4 h-4" />
                <span>{{ lab.currentReservation.startTime }} - {{ lab.currentReservation.endTime }}</span>
              </div>
              <div class="flex items-center gap-2 text-gray-600">
                <MapPin class="w-4 h-4" />
                <span>{{ lab.currentReservation.purpose }}</span>
              </div>
            </div>
            <!-- 今日预约概览 -->
            <div class="mt-3 pt-3 border-t border-gray-100">
              <p class="text-xs text-gray-500 mb-2">今日预约 ({{ lab.todayReservations.length }}条)</p>
              <div v-if="lab.todayReservations.length > 0" class="space-y-1">
                <div v-for="tr in lab.todayReservations" :key="tr.id" class="flex items-center justify-between text-xs">
                  <span class="text-gray-600">{{ tr.startTime }}-{{ tr.endTime }}</span>
                  <span class="px-1.5 py-0.5 rounded" :class="tr.status === 'approved' ? 'bg-green-100 text-green-700' : tr.status === 'pending' ? 'bg-yellow-100 text-yellow-700' : 'bg-gray-100 text-gray-500'">{{ getStatusLabel(tr.status) }}</span>
                </div>
              </div>
              <p v-else class="text-xs text-gray-400">暂无预约</p>
            </div>
            <!-- 实验室描述 -->
            <div v-if="lab.description" class="mt-3 pt-3 border-t border-gray-100">
              <p class="text-xs text-gray-500 mb-1">描述</p>
              <p class="text-xs text-gray-600 line-clamp-2">{{ lab.description }}</p>
            </div>
            <!-- 可用设备 -->
            <div class="mt-3 pt-3 border-t border-gray-100">
              <p class="text-xs text-gray-500 mb-2">可用设备</p>
              <div class="flex flex-wrap gap-1">
                <span v-for="eq in lab.equipment" :key="eq" class="px-2 py-0.5 bg-gray-100 text-gray-600 rounded text-xs">{{ eq }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 预约列表 -->
    <div v-if="activeTab === 'reservations'">
      <div class="card">
        <div class="flex flex-col md:flex-row gap-4">
          <div class="flex-1 relative">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input v-model="searchTerm" placeholder="搜索实验室、申请人..." class="w-full border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent pl-10 pr-4 py-2" />
          </div>
          <select v-model="statusFilter" class="input-field w-full md:w-48">
            <option value="all">全部状态</option>
            <option value="pending">待审核</option>
            <option value="approved">已通过</option>
            <option value="rejected">已拒绝</option>
          </select>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div v-for="r in filteredReservations" :key="r.id" class="card">
          <div class="flex items-start justify-between mb-4">
            <div class="flex items-start gap-3">
              <div class="w-12 h-12 bg-primary-100 rounded-xl flex items-center justify-center">
                <Calendar class="w-6 h-6 text-primary-600" />
              </div>
              <div>
                <h3 class="font-semibold text-gray-900">{{ r.labName }}</h3>
                <p class="text-sm text-gray-500">{{ r.applicant }}{{ r.department ? ' · ' + r.department : '' }}</p>
              </div>
            </div>
            <span :class="getStatusBadgeClass(r.status)">{{ getStatusLabel(r.status) }}</span>
          </div>
          <div class="space-y-2 text-sm mb-4">
            <div class="flex items-center gap-2 text-gray-600">
              <Clock class="w-4 h-4" />
              <span>{{ r.date }} {{ r.startTime }} - {{ r.endTime }}</span>
            </div>
            <div class="flex items-center gap-2 text-gray-600">
              <Users class="w-4 h-4" />
              <span>{{ r.attendees }}人参加</span>
            </div>
            <div v-if="r.location" class="flex items-center gap-2 text-gray-600">
              <MapPin class="w-4 h-4" />
              <span>位置: {{ r.location }}</span>
            </div>
            <div class="flex items-start gap-2 text-gray-600">
              <FileText class="w-4 h-4 mt-0.5" />
              <span>用途: {{ r.purpose }}</span>
            </div>
            <div v-if="r.description" class="text-xs text-gray-500 bg-gray-50 p-2 rounded">
              {{ r.description }}
            </div>
            <div class="flex flex-wrap gap-2 mt-2">
              <span v-for="eq in r.equipment" :key="eq" class="px-2 py-1 bg-gray-100 text-gray-600 rounded text-xs">{{ eq }}</span>
            </div>
          </div>
          <!-- 审批信息：仅在已审批（非 pending）时显示 -->
          <div v-if="r.status !== 'pending' && (r.approveTime || r.approveRemark)" class="text-sm p-3 rounded-lg mb-4"
            :class="r.status === 'approved' ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'">
            <div class="flex items-center gap-2 mb-1 font-medium"
              :class="r.status === 'approved' ? 'text-green-700' : 'text-red-700'">
              <CheckCircle v-if="r.status === 'approved'" class="w-4 h-4" />
              <XCircle v-else class="w-4 h-4" />
              <span>{{ r.status === 'approved' ? '审批通过' : '审批拒绝' }}</span>
            </div>
            <div v-if="r.approveTime" class="text-gray-600 mt-1">
              <span class="text-gray-500">审批时间：</span>{{ formatApproveTime(r.approveTime) }}
            </div>
            <div v-if="r.approveRemark" class="text-gray-600 mt-1">
              <span class="text-gray-500">审批备注：</span>{{ r.approveRemark }}
            </div>
          </div>
          <div v-if="hasPermission('reservation', 'set')" class="flex gap-2 pt-4 border-t">
            <template v-if="r.status === 'pending'">
              <button @click="handleApprove(r.id)" class="flex-1 bg-green-50 text-green-600 py-2 rounded-lg hover:bg-green-100"><CheckCircle class="w-4 h-4 inline mr-1" />通过</button>
              <button @click="handleReject(r.id)" class="flex-1 bg-red-50 text-red-600 py-2 rounded-lg hover:bg-red-100"><XCircle class="w-4 h-4 inline mr-1" />拒绝</button>
            </template>
            <button @click="handleDeleteReservation(r.id, r.labName)" class="flex-1 bg-gray-100 text-gray-600 py-2 rounded-lg hover:bg-red-50 hover:text-red-600"><Trash2 class="w-4 h-4 inline mr-1" />删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 审批弹窗 -->
    <div v-if="showApproveModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-md">
        <div class="p-6 border-b flex justify-between">
          <h2 class="text-xl font-bold">{{ approveAction === 'approve' ? '审批通过' : '审批拒绝' }}</h2>
          <button @click="showApproveModal=false"><X class="w-5 h-5" /></button>
        </div>
        <form @submit.prevent="confirmApprove" class="p-6 space-y-4">
          <div>
            <label>审批备注（可选）</label>
            <textarea v-model="approveRemark" class="input-field min-h-[100px]" placeholder="请输入审批备注或拒绝原因..."></textarea>
          </div>
          <div class="flex gap-3">
            <button type="button" @click="showApproveModal=false" class="flex-1 btn-secondary">取消</button>
            <button type="submit" class="flex-1 btn-primary">{{ approveAction === 'approve' ? '确认通过' : '确认拒绝' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 新建预约 Modal -->
    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div class="bg-white rounded-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
          <div class="p-6 border-b flex justify-between">
            <h2 class="text-xl font-bold">新建预约</h2>
            <button @click="showModal=false"><X class="w-5 h-5" /></button>
          </div>
          <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
            <div>
              <label>选择实验室</label>
              <select v-model="formData.labName" required class="input-field" @change="onLabChange">
                <option value="">请选择</option>
                <option v-for="lab in labOptions" :key="lab.name" :value="lab.name">{{ lab.name }} (容量{{ lab.capacity }}人)</option>
              </select>
              <p v-if="labOptions.length === 0" class="text-sm text-gray-500 mt-1">暂无可用实验室，请先添加实验室</p>
            </div>
            <div v-if="selectedLab" class="p-3 bg-gray-50 rounded-lg text-sm space-y-2">
              <div v-if="selectedLab.location">
                <p class="font-medium mb-1">位置:</p>
                <p class="text-gray-600 flex items-center gap-1">
                  <MapPin class="w-3 h-3" />{{ selectedLab.location }}
                </p>
              </div>
              <div v-if="selectedLab.description">
                <p class="font-medium mb-1">描述:</p>
                <p class="text-gray-600">{{ selectedLab.description }}</p>
              </div>
              <div>
                <p class="font-medium mb-1">可用设备:</p>
                <div class="flex flex-wrap gap-2">
                  <span v-for="eq in selectedLab.equipment" :key="eq" class="px-2 py-1 bg-white rounded text-gray-600">{{ eq }}</span>
                </div>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div><label>预约日期</label><input type="date" v-model="formData.date" required class="input-field" /></div>
              <div><label>参加人数</label><input type="number" v-model="formData.attendees" required min="1" class="input-field" /></div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div><label>开始时间</label><input type="time" v-model="formData.startTime" required class="input-field" /></div>
              <div><label>结束时间</label><input type="time" v-model="formData.endTime" required class="input-field" /></div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div><label>申请人</label><select v-model="formData.applicant" @change="onApplicantChange" required class="input-field"><option value="">-- 请选择 --</option><option v-for="p in personnelList" :key="p.id" :value="p.name || p.username">{{ p.name || p.username || p.id }}{{ p.code ? ' (' + p.code + ')' : '' }}</option></select></div>
              <div><label>所属部门</label><input v-model="formData.department" class="input-field" placeholder="选择申请人后自动填充" readonly /></div>
            </div>
            <div><label>实验用途</label><textarea v-model="formData.purpose" required class="input-field min-h-[80px]"></textarea></div>
            <div><label>所需设备 (选填)</label><input v-model="formData.equipmentStr" placeholder="多个设备用逗号分隔" class="input-field" /></div>
            <div class="flex gap-3">
              <button type="button" @click="showModal=false" class="flex-1 btn-secondary">取消</button>
              <button type="submit" class="flex-1 btn-primary" :disabled="labOptions.length === 0">提交预约</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- 实验室管理 Modal -->
    <Teleport to="body">
      <div v-if="showLabManageModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div class="bg-white rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
          <div class="p-6 border-b flex justify-between items-center">
            <h2 class="text-xl font-bold">实验室管理</h2>
            <button @click="showLabManageModal=false"><X class="w-5 h-5" /></button>
          </div>
          <div class="p-6 space-y-6">
            <!-- 新增/编辑实验室表单 -->
            <div class="p-4 bg-gray-50 rounded-lg">
              <h3 class="font-medium mb-3">{{ editingLabIndex === -1 ? '新增实验室' : '编辑实验室' }}</h3>
              <div class="space-y-3">
                <div>
                  <label class="text-sm">实验室名称</label>
                  <input v-model="labForm.name" placeholder="如：生物实验室 A101" class="input-field" />
                </div>
                <div>
                  <label class="text-sm">实验室位置</label>
                  <input v-model="labForm.location" placeholder="如：教学楼A栋5楼" class="input-field" />
                </div>
                <div class="grid grid-cols-2 gap-3">
                  <div>
                    <label class="text-sm">容量（人）</label>
                    <input type="number" v-model.number="labForm.capacity" min="1" placeholder="20" class="input-field" />
                  </div>
                  <div>
                    <label class="text-sm">设备（用逗号分隔）</label>
                    <input v-model="labForm.equipmentStr" placeholder="显微镜,离心机,培养箱" class="input-field" />
                  </div>
                </div>
                <div>
                  <label class="text-sm">实验室描述</label>
                  <textarea v-model="labForm.description" placeholder="实验室用途、特点等描述..." class="input-field min-h-[80px]"></textarea>
                </div>
                <div class="flex gap-2">
                  <button @click="saveLab" class="btn-primary flex-1">{{ editingLabIndex === -1 ? '添加' : '保存' }}</button>
                  <button v-if="editingLabIndex !== -1" @click="cancelEditLab" class="btn-secondary">取消</button>
                </div>
              </div>
            </div>

            <!-- 实验室列表 -->
            <div>
              <h3 class="font-medium mb-3">现有实验室 ({{ labOptions.length }}个)</h3>
              <div v-if="labOptions.length === 0" class="text-center py-8 text-gray-400">
                <MapPin class="w-10 h-10 mx-auto mb-2" />
                <p>暂无实验室，请添加</p>
              </div>
              <div v-else class="space-y-2">
                <div v-for="(lab, index) in labOptions" :key="lab.name" class="flex items-center justify-between p-3 border rounded-lg hover:bg-gray-50">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center">
                      <MapPin class="w-5 h-5 text-primary-600" />
                    </div>
                    <div>
                      <p class="font-medium">{{ lab.name }}</p>
                      <p class="text-sm text-gray-500">容量: {{ lab.capacity }}人 | 设备: {{ lab.equipment.join(', ') }}</p>
                    </div>
                  </div>
                  <div class="flex gap-1">
                    <button @click="editLab(index)" class="p-2 text-blue-600 hover:bg-blue-50 rounded"><Edit2 class="w-4 h-4" /></button>
                    <button @click="deleteLab(index)" class="p-2 text-red-600 hover:bg-red-50 rounded"><Trash2 class="w-4 h-4" /></button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Plus, Calendar, Clock, Users, CheckCircle, XCircle, Search, X, MapPin, Edit2, Trash2, FileText } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { addNotification } from '@/utils/notification'
import { hasPermission } from '@/utils/permission'
import { labApi, reservationApi, commonApi } from '../services/api'

const activeTab = ref('labStatus')
const reservations = ref([])
const personnelList = ref([]) // 人员列表用于申请人选择
const searchTerm = ref('')
const statusFilter = ref('all')
const showModal = ref(false)
const showApproveModal = ref(false)
const approveAction = ref('') // 'approve' 或 'reject'
const approveRemark = ref('')
const approveTargetId = ref(null)
const formData = ref({ labId: '', labName: '', date: '', startTime: '', endTime: '', attendees: 1, applicant: '', department: '', purpose: '', equipmentStr: '' })
const selectedLab = ref(null)
const currentTime = ref(new Date())
let timeTimer = null

// 实验室数据 - 从后端 API 加载
const labOptions = ref([])

// 实验室管理相关
const showLabManageModal = ref(false)
const editingLabIndex = ref(-1)
const labForm = ref({ name: '', capacity: 20, equipmentStr: '', location: '', description: '' })

// 将后端实验室数据转换为前端需要的格式
const mapLabFromBackend = (lab) => ({
  id: lab.id,
  name: lab.name,
  capacity: lab.capacity,
  location: lab.location || '',
  description: lab.description || '',
  equipment: typeof lab.equipment === 'string' ? JSON.parse(lab.equipment) : (lab.equipment || []),
  status: lab.status
})

// 将后端预约数据转换为前端需要的格式
const mapReservationFromBackend = (r) => {
  // 后端 startTime 是 LocalDateTime 字符串，如 "2026-06-01T14:00:00"
  const startDateTime = r.startTime || ''
  const dateOnly = startDateTime.substring(0, 10)
  const startTimeOnly = startDateTime.length > 16 ? startDateTime.substring(11, 16) : startDateTime.substring(11)

  const endDateTime = r.endTime || ''
  const endTimeOnly = endDateTime.length > 16 ? endDateTime.substring(11, 16) : endDateTime.substring(11)

  // 后端只有 labId，需要从 labOptions 匹配 labName
  const matchedLab = labOptions.value.find(l => l.id === r.labId)
  const labName = matchedLab?.name || `实验室${r.labId}`

  // 通过 userId 从人员列表中查找真实姓名，格式：ID-用户名
  const matchedUser = personnelList.value.find(p => p.userId === r.userId)
  const userName = matchedUser?.name || matchedUser?.username
  const applicantName = userName
    ? `${r.userId}-${userName}`
    : `${r.userId}`

  return {
    id: r.id,
    labId: r.labId,
    labName: labName,
    applicant: applicantName,
    department: matchedUser?.departmentName || '',
    date: dateOnly,
    startTime: startTimeOnly,
    endTime: endTimeOnly,
    attendees: r.attendees || 0,
    purpose: r.purpose || '',
    equipment: typeof r.equipment === 'string' ? JSON.parse(r.equipment) : (r.equipment || []),
    status: r.status,
    approverId: r.approverId,
    approveTime: r.approveTime,
    approveRemark: r.approveRemark,
    createTime: r.createTime
  }
}

// 加载实验室数据
const loadLabData = async () => {
  try {
    const data = await labApi.getList()
    const list = Array.isArray(data) ? data : (data?.content || data?.records || [])
    labOptions.value = list.map(mapLabFromBackend)
  } catch (e) {
    console.error('加载实验室数据失败', e)
    labOptions.value = []
  }
}

// 打开实验室管理弹窗
const openLabManageModal = () => {
  showLabManageModal.value = true
  resetLabForm()
}

// 重置实验室表单
const resetLabForm = () => {
  labForm.value = { name: '', capacity: 20, equipmentStr: '', location: '', description: '' }
  editingLabIndex.value = -1
}

// 保存实验室（新增或编辑）
const saveLab = async () => {
  if (!labForm.value.name.trim()) {
    alert('请输入实验室名称')
    return
  }
  if (labForm.value.capacity < 1) {
    alert('容量必须大于0')
    return
  }

  const equipment = labForm.value.equipmentStr
    ? labForm.value.equipmentStr.split(/[,，]/).map(s => s.trim()).filter(s => s)
    : []

  const labData = {
    name: labForm.value.name.trim(),
    capacity: labForm.value.capacity,
    equipment: JSON.stringify(equipment),
    location: labForm.value.location.trim(),
    description: labForm.value.description.trim()
  }

  try {
    if (editingLabIndex.value === -1) {
      // 新增
      if (labOptions.value.some(l => l.name === labData.name)) {
        alert('实验室名称已存在')
        return
      }
      const createRes = await labApi.create(labData)
      ElMessage.success(createRes.msg || '实验室添加成功')
      addNotification('实验室管理', `新增实验室 ${labData.name}`, 'reservation')
    } else {
      // 编辑
      const lab = labOptions.value[editingLabIndex.value]
      if (labOptions.value.some((l, i) => l.name === labData.name && i !== editingLabIndex.value)) {
        alert('实验室名称已存在')
        return
      }
      const updateRes = await labApi.update(lab.id, labData)
      ElMessage.success(updateRes.msg || '实验室更新成功')
    }
    await loadLabData()
    resetLabForm()
  } catch (e) {
    console.error('保存实验室失败', e)
    ElMessage.error(e.message || '保存实验室失败，请重试')
  }
}

// 编辑实验室
const editLab = (index) => {
  editingLabIndex.value = index
  const lab = labOptions.value[index]
  labForm.value = {
    name: lab.name,
    capacity: lab.capacity,
    equipmentStr: lab.equipment.join(','),
    location: lab.location || '',
    description: lab.description || ''
  }
}

// 取消编辑
const cancelEditLab = () => {
  resetLabForm()
}

// 删除实验室
const deleteLab = async (index) => {
  const lab = labOptions.value[index]
  // 检查是否有未完成的预约
  const hasActiveReservation = reservations.value.some(r =>
    r.labName === lab.name &&
    r.status !== 'rejected' &&
    new Date(r.date + 'T' + r.endTime) > new Date()
  )

  if (hasActiveReservation) {
    if (!confirm(`警告：${lab.name} 存在未完成的预约，删除后这些预约将无法正常使用。确定要删除吗？`)) {
      return
    }
  } else {
    if (!confirm(`确定要删除 ${lab.name} 吗？`)) {
      return
    }
  }

  try {
    const deleteRes = await labApi.delete(lab.id)
    ElMessage.success(deleteRes.msg || '实验室删除成功')
    addNotification('实验室管理', `删除实验室 ${lab.name}`, 'reservation')
    await loadLabData()
    // 如果正在编辑该实验室，重置表单
    if (editingLabIndex.value === index) {
      resetLabForm()
    } else if (editingLabIndex.value > index) {
      editingLabIndex.value--
    }
  } catch (e) {
    console.error('删除实验室失败', e)
    ElMessage.error(e.message || '删除实验室失败，请重试')
  }
}

// 当前时间格式化显示
const currentTimeStr = computed(() => {
  const d = currentTime.value
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
})

// 实验室状态计算
const labStatuses = computed(() => {
  const now = currentTime.value
  const todayStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  const nowMinutes = now.getHours() * 60 + now.getMinutes()

  return labOptions.value.map(lab => {
    // 获取该实验室今日已通过的预约
    const todayRes = reservations.value.filter(r =>
      r.labName === lab.name &&
      r.date === todayStr &&
      r.status === 'approved'
    )

    // 检查当前时间是否在某个预约的时间段内
    const currentRes = todayRes.find(r => {
      const [sh, sm] = r.startTime.split(':').map(Number)
      const [eh, em] = r.endTime.split(':').map(Number)
      const startMinutes = sh * 60 + sm
      const endMinutes = eh * 60 + em
      return nowMinutes >= startMinutes && nowMinutes < endMinutes
    })

    // 获取今日所有预约（包含待审核）
    const allTodayRes = reservations.value.filter(r =>
      r.labName === lab.name && r.date === todayStr
    )

    return {
      name: lab.name,
      capacity: lab.capacity,
      location: lab.location,
      description: lab.description,
      equipment: lab.equipment,
      isOccupied: !!currentRes,
      currentReservation: currentRes || null,
      todayReservations: allTodayRes.sort((a, b) => a.startTime.localeCompare(b.startTime))
    }
  })
})

const loadData = async () => {
  // 先加载人员列表，mapReservationFromBackend 会用到它匹配用户名
  try {
    const pl = await commonApi.getSimpleUserList()
    personnelList.value = Array.isArray(pl) ? pl : (pl?.content || [])
  } catch (e) {
    console.error('加载人员列表失败', e)
    personnelList.value = []
  }
  // 再加载预约数据
  try {
    const data = await reservationApi.getList()
    const list = Array.isArray(data) ? data : (data?.content || data?.records || [])
    reservations.value = list.map(mapReservationFromBackend)
  } catch (e) {
    console.error('加载预约数据失败', e)
    reservations.value = []
  }
}

const filteredReservations = computed(() => {
  let list = reservations.value
  if (searchTerm.value) list = list.filter(r => r.labName.includes(searchTerm.value) || r.applicant.includes(searchTerm.value))
  if (statusFilter.value !== 'all') list = list.filter(r => r.status === statusFilter.value)
  return list
})

const getStatusBadgeClass = (status) => ({ pending: 'status-pending', approved: 'status-approved', rejected: 'status-rejected' }[status] || '')
const getStatusLabel = (status) => ({ pending: '待审核', approved: '已通过', rejected: '已拒绝' }[status] || status)

// 格式化审批时间（后端 LocalDateTime 字符串 -> 可读格式）
const formatApproveTime = (approveTime) => {
  if (!approveTime) return ''
  try {
    const date = new Date(approveTime)
    if (isNaN(date.getTime())) return approveTime
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    const h = String(date.getHours()).padStart(2, '0')
    const min = String(date.getMinutes()).padStart(2, '0')
    const s = String(date.getSeconds()).padStart(2, '0')
    return `${y}-${m}-${d} ${h}:${min}:${s}`
  } catch {
    return approveTime
  }
}

const onLabChange = () => {
  const lab = labOptions.value.find(l => l.name === formData.value.labName)
  selectedLab.value = lab
  if (lab) formData.value.labId = lab.id
}

// 选择申请人后自动填充部门
const onApplicantChange = () => {
  const selected = personnelList.value.find(p => p.name === formData.value.applicant || p.username === formData.value.applicant)
  formData.value.department = selected?.departmentName || ''
}

const handleSubmit = async () => {
  const equipment = formData.value.equipmentStr
    ? formData.value.equipmentStr.split(/[,，]/).map(s => s.trim()).filter(s => s)
    : []

  // 组合日期和时间为后端需要的 LocalDateTime 格式
  const startDateTime = `${formData.value.date}T${formData.value.startTime}:00`
  const endDateTime = `${formData.value.date}T${formData.value.endTime}:00`

  // 获取当前用户ID
  const userStr = localStorage.getItem('labUser')
  const user = userStr ? JSON.parse(userStr) : null
  const userId = user?.id || null

  // 匹配实验室ID
  const matchedLab = labOptions.value.find(l => l.name === formData.value.labName)
  const labId = matchedLab?.id || null

  const submitData = {
    labId: labId,
    userId: userId,
    startTime: startDateTime,
    endTime: endDateTime,
    purpose: formData.value.purpose
  }

  try {
    const res = await reservationApi.create(submitData)
    ElMessage.success(res.msg || '预约提交成功')
    addNotification('新建预约', `${formData.value.applicant} 提交了 ${formData.value.labName} 的预约申请`, 'reservation')
    showModal.value = false
    formData.value = { labId: '', labName: '', date: '', startTime: '', endTime: '', attendees: 1, applicant: '', department: '', purpose: '', equipmentStr: '' }
    selectedLab.value = null
    await loadData()
  } catch (e) {
    console.error('提交预约失败', e)
    ElMessage.error(e.message || '提交预约失败，请重试')
  }
}
const handleApprove = (id) => {
  approveTargetId.value = id
  approveAction.value = 'approve'
  approveRemark.value = ''
  showApproveModal.value = true
}

const handleReject = (id) => {
  approveTargetId.value = id
  approveAction.value = 'reject'
  approveRemark.value = ''
  showApproveModal.value = true
}

const confirmApprove = async () => {
  const id = approveTargetId.value
  const remark = approveRemark.value
  
  try {
    // 获取当前用户信息作为 approverId
    const userStr = localStorage.getItem('labUser')
    const user = userStr ? JSON.parse(userStr) : null
    const approverId = user?.id || user?.code || 'admin'

    if (approveAction.value === 'approve') {
      const approveRes = await reservationApi.approve(id, approverId, remark)
      ElMessage.success(approveRes.msg || '审批通过')
      const res = reservations.value.find(r => r.id === id)
      addNotification('预约审核', `${res.labName} 的预约申请已通过`, 'reservation')
    } else {
      const rejectRes = await reservationApi.reject(id, remark)
      ElMessage.success(rejectRes.msg || '审批拒绝')
      const res = reservations.value.find(r => r.id === id)
      addNotification('预约审核', `${res.labName} 的预约申请被拒绝`, 'reservation')
    }
    
    showApproveModal.value = false
    await loadData()
  } catch (e) {
    console.error('审批操作失败', e)
    ElMessage.error(e.message || '审批操作失败，请重试')
  }
}

const handleDeleteReservation = async (id, labName) => {
  if (!confirm(`确定要删除该预约吗？\n实验室: ${labName}`)) return

  try {
    const res = await reservationApi.delete(id)
    ElMessage.success(res.msg || '删除成功')
    addNotification('预约管理', `已删除 ${labName} 的预约`, 'reservation')
    await loadData()
  } catch (e) {
    console.error('删除预约失败', e)
    ElMessage.error(e.message || '删除失败，请重试')
  }
}

onMounted(async () => {
  await loadLabData()
  await loadData()
  // 每秒更新当前时间，实现实时刷新
  timeTimer = setInterval(() => {
    currentTime.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer)
})
</script>
