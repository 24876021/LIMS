<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <h1 class="text-2xl font-bold text-gray-900">设备管理</h1>
      <button v-if="hasPermission('equipment', 'add')" @click="openAddModal" class="btn-primary"><Plus class="w-4 h-4" />新增设备</button>
    </div>

    <!-- Filters -->
    <div class="card">
      <div class="flex flex-col md:flex-row gap-4">
        <div class="flex-1 relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input type="text" v-model="searchTerm" placeholder="搜索设备名称、编号..." class="w-full border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent pl-10 pr-4 py-2" />
        </div>
        <select v-model="statusFilter" class="input-field w-full md:w-48">
          <option value="all">全部状态</option>
          <option value="available">可用</option>
          <option value="borrowed">借用中</option>
          <option value="maintenance">维修中</option>
        </select>
      </div>
    </div>

    <!-- Table -->
    <div class="card overflow-hidden">
      <div v-if="loading" class="flex items-center justify-center h-64"><Loader2 class="w-8 h-8 animate-spin text-primary-600" /></div>
      <div v-else class="table-container">
        <table class="data-table">
          <thead>
          <tr><th>设备编号</th><th>设备名称</th><th>分类</th><th>位置</th><th>价格</th><th>状态</th><th>借用人</th><th>上次维护</th><th>操作</th></tr>
          </thead>
          <tbody>
          <tr v-for="eq in filteredEquipment" :key="eq.id">
            <td class="font-medium">{{ eq.code }}</td>
            <td>{{ eq.name }}</td>
            <td>{{ eq.category }}</td>
            <td>{{ eq.location }}</td>
            <td>{{ eq.price ? '¥' + eq.price : '-' }}</td>
            <td><span :class="getStatusBadgeClass(eq.status)">{{ getStatusLabel(eq.status) }}</span></td>
            <td>{{ eq.borrower || '-' }}</td>
            <td>{{ eq.lastMaintenance || '-' }}</td>
            <td>
              <div class="flex items-center gap-2">
                <button v-if="hasPermission('equipment', 'get')" @click="viewDetail(eq)" class="p-2 hover:bg-gray-100 rounded-lg" title="详情"><History class="w-4 h-4" /></button>
                <button v-if="hasPermission('equipment', 'get') && eq.status === 'available'" @click="openBorrowModal(eq)" class="p-2 hover:bg-blue-50 rounded-lg text-blue-600" title="借用"><RotateCcw class="w-4 h-4" /></button>
                <button v-if="hasPermission('equipment', 'set') && eq.status === 'available'" @click="openMaintenanceModal(eq)" class="p-2 hover:bg-orange-50 rounded-lg text-orange-600" title="维修"><Wrench class="w-4 h-4" /></button>
                <button v-if="hasPermission('equipment', 'get') && eq.status === 'borrowed'" @click="handleReturn(eq)" class="p-2 hover:bg-green-50 rounded-lg text-green-600" title="归还"><RotateCcw class="w-4 h-4" /></button>
                <button v-if="hasPermission('equipment', 'set') && eq.status === 'maintenance'" @click="completeMaintenance(eq)" class="p-2 hover:bg-green-50 rounded-lg text-green-600" title="完成维修"><CheckCircle class="w-4 h-4" /></button>
                <button v-if="hasPermission('equipment', 'set')" @click="openEditModal(eq)" class="p-2 hover:bg-gray-100 rounded-lg"><Edit2 class="w-4 h-4" /></button>
                <button v-if="hasPermission('equipment', 'remove')" @click="handleDelete(eq.id)" class="p-2 hover:bg-red-50 rounded-lg text-red-600"><Trash2 class="w-4 h-4" /></button>
              </div>
            </td>
          </tr>
          <tr v-if="filteredEquipment.length === 0"><td colspan="8" class="text-center py-8 text-gray-400">暂无数据</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Add/Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b flex justify-between items-center"><h2 class="text-xl font-bold">{{ editing ? '编辑设备' : '新增设备' }}</h2><button @click="showModal=false"><X class="w-5 h-5" /></button></div>
        <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
          <div class="grid grid-cols-2 gap-4"><div><label>设备编号</label><input type="text" v-model="formData.code" required class="input-field" /></div><div><label>设备名称</label><input type="text" v-model="formData.name" required class="input-field" /></div></div>
          <div><label>分类</label><input type="text" v-model="formData.category" required class="input-field" /></div>
          <div><label>存放位置</label><input type="text" v-model="formData.location" required class="input-field" /></div>
          <div class="grid grid-cols-2 gap-4">
            <div><label>价格（元）</label><input type="number" v-model="formData.price" step="0.01" class="input-field" placeholder="可选" /></div>
            <div><label>采购日期</label><input type="date" v-model="formData.purchaseDate" required class="input-field" /></div>
          </div>
          <div><label>设备描述</label><textarea v-model="formData.description" class="input-field min-h-[80px]" placeholder="可选"></textarea></div>
          <div class="flex gap-3"><button type="button" @click="showModal=false" class="flex-1 btn-secondary">取消</button><button type="submit" class="flex-1 btn-primary">保存</button></div>
        </form>
      </div>
    </div>

    <!-- Borrow Modal -->
    <div v-if="showBorrowModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-md"><div class="p-6 border-b flex justify-between"><h2 class="text-xl font-bold">设备借用</h2><button @click="showBorrowModal=false"><X class="w-5 h-5" /></button></div>
        <form @submit.prevent="handleBorrow" class="p-6 space-y-4"><div><label>设备</label><input type="text" :value="selectedEquipment?.name" disabled class="input-field bg-gray-50" /></div><div><label>借用人</label><select v-model="borrowForm.borrowerId" required class="input-field"><option value="">-- 请选择 --</option><option v-for="p in personnelList" :key="p.userId" :value="String(p.userId)">{{ p.name || p.username }}{{ p.code ? ' (' + p.code + ')' : '' }}</option></select></div><div><label>用途</label><textarea v-model="borrowForm.purpose" required class="input-field min-h-[100px]"></textarea></div><div class="flex gap-3"><button type="button" @click="showBorrowModal=false" class="flex-1 btn-secondary">取消</button><button type="submit" class="flex-1 btn-primary">确认借用</button></div></form></div>
    </div>

    <!-- Maintenance Modal -->
    <div v-if="showMaintenanceModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-md"><div class="p-6 border-b flex justify-between"><h2 class="text-xl font-bold">设备维修</h2><button @click="showMaintenanceModal=false"><X class="w-5 h-5" /></button></div>
        <form @submit.prevent="handleMaintenance" class="p-6 space-y-4"><div><label>设备</label><input type="text" :value="selectedEquipment?.name" disabled class="input-field bg-gray-50" /></div><div><label>问题描述</label><textarea v-model="maintenanceForm.description" required class="input-field"></textarea></div><div class="grid grid-cols-2 gap-4"><div><label>费用</label><input type="number" v-model="maintenanceForm.cost" class="input-field" /></div><div><label>维修人员</label><input type="text" v-model="maintenanceForm.technician" required class="input-field" /></div></div><div class="flex gap-3"><button type="button" @click="showMaintenanceModal=false" class="flex-1 btn-secondary">取消</button><button type="submit" class="flex-1 btn-primary">提交维修</button></div></form></div>
    </div>

    <!-- Detail Modal -->
    <div v-if="showDetailModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b flex justify-between"><h2 class="text-xl font-bold">设备详情 - {{ selectedEquipment?.name }}</h2><button @click="showDetailModal=false"><X class="w-5 h-5" /></button></div>
        <div class="p-6 space-y-6">
          <div class="grid grid-cols-2 gap-4 text-sm"><div><span class="text-gray-500">编号:</span> {{ selectedEquipment?.code }}</div><div><span class="text-gray-500">分类:</span> {{ selectedEquipment?.category }}</div><div><span class="text-gray-500">位置:</span> {{ selectedEquipment?.location }}</div><div><span class="text-gray-500">价格:</span> {{ selectedEquipment?.price ? '¥' + selectedEquipment?.price : '-' }}</div><div><span class="text-gray-500">状态:</span> <span :class="getStatusBadgeClass(selectedEquipment?.status)">{{ getStatusLabel(selectedEquipment?.status) }}</span></div><div><span class="text-gray-500">采购日期:</span> {{ selectedEquipment?.purchaseDate ? selectedEquipment.purchaseDate.split('T')[0] : '-' }}</div><div><span class="text-gray-500">上次维护:</span> {{ selectedEquipment?.lastMaintenance || '-' }}</div></div>
          <div v-if="selectedEquipment?.description" class="text-sm"><span class="text-gray-500">设备描述:</span> {{ selectedEquipment?.description }}</div>
          <div><h3 class="font-semibold mb-3">维修记录</h3><div v-for="record in maintenanceRecords" :key="record.id" class="p-3 bg-gray-50 rounded-lg text-sm mb-2"><div class="flex justify-between"><span class="font-medium">{{ record.startDate }}</span><span class="text-orange-600">¥{{ record.cost }}</span></div><div>{{ record.type }} - {{ record.description }}</div><div class="text-gray-500 text-xs">技师: {{ record.technician }} {{ record.endDate ? `(完成于 ${record.endDate})` : '(维修中)' }}</div></div><p v-if="!maintenanceRecords.length" class="text-gray-400 text-sm">暂无维修记录</p></div>
          <div><h3 class="font-semibold mb-3">使用记录</h3><div v-for="record in usageRecords" :key="record.id" class="p-3 bg-gray-50 rounded-lg text-sm mb-2"><div class="flex justify-between"><span class="font-medium">{{ record.borrower }}</span><span class="text-gray-500">{{ record.borrowDate }} 至 {{ record.returnDate || '借用中' }}</span></div><div>用途: {{ record.purpose }}</div></div><p v-if="!usageRecords.length" class="text-gray-400 text-sm">暂无使用记录</p></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Search, Edit2, Trash2, Wrench, RotateCcw, History, X, Loader2, CheckCircle } from 'lucide-vue-next'
import { equipmentApi, commonApi } from '@/services/api'
import { addNotification } from '@/utils/notification'
import { hasPermission } from '@/utils/permission'
import { ElMessage } from 'element-plus'

const equipment = ref([])
const searchTerm = ref('')
const statusFilter = ref('all')
const loading = ref(false)
const showModal = ref(false)
const showBorrowModal = ref(false)
const showMaintenanceModal = ref(false)
const showDetailModal = ref(false)
const editing = ref(false)
const selectedEquipment = ref(null)
const formData = ref({ code: '', name: '', category: '', location: '', purchaseDate: '', status: 'available' })
const borrowForm = ref({ borrowerId: '', borrowerName: '', purpose: '' })
const maintenanceForm = ref({ description: '', cost: 0, technician: '' })
const maintenanceRecords = ref([])
const usageRecords = ref([])
const personnelList = ref([]) // 人员列表用于借用选择

const filteredEquipment = computed(() => {
  let list = equipment.value
  if (searchTerm.value) list = list.filter(e => e.name.includes(searchTerm.value) || e.code.includes(searchTerm.value))
  if (statusFilter.value !== 'all') list = list.filter(e => e.status === statusFilter.value)
  return list
})

const loadEquipment = async () => {
  loading.value = true
  try {
    // 并行加载人员列表和设备列表
    const [pl, data] = await Promise.all([
      commonApi.getSimpleUserList().catch(e => { console.error('加载人员列表失败', e); return [] }),
      equipmentApi.getList()
    ])
    personnelList.value = Array.isArray(pl) ? pl : (pl?.content || [])
    const list = data.content || []
    // 立即显示列表（不等待使用/维修记录）
    equipment.value = list

    // 异步并行查询每台设备的使用记录和维修记录，不阻塞列表渲染
    Promise.all(list.map(async (eq) => {
      try {
        const [usageRes, maintRes] = await Promise.allSettled([
          equipmentApi.getUsageRecords(eq.id),
          equipmentApi.getMaintenanceRecords(eq.id)
        ])
        // 借用人
        const recList = usageRes.status === 'fulfilled' ? (Array.isArray(usageRes.value) ? usageRes.value : (usageRes.value?.content || [])) : []
        const activeRecord = recList.find(r => r.status === 'borrowing' && !r.returnTime)
        if (activeRecord) {
          eq.activeBorrowerId = String(activeRecord.userId)
          const borrower = personnelList.value.find(p => String(p.userId) === String(activeRecord.userId) || String(p.user_id) === String(activeRecord.userId))
          eq.borrower = borrower?.name || borrower?.employeeNo || `用户${activeRecord.userId}`
        }
        // 上次维护时间（取最近一条已完成的维修记录）
        const maintList = maintRes.status === 'fulfilled' ? (Array.isArray(maintRes.value) ? maintRes.value : (maintRes.value?.content || [])) : []
        const completedMaint = maintList.filter(r => r.status === 'completed' && r.completeTime)
        if (completedMaint.length > 0) {
          completedMaint.sort((a, b) => (b.completeTime || '').localeCompare(a.completeTime || ''))
          eq.lastMaintenance = completedMaint[0].completeTime ? completedMaint[0].completeTime.split('T')[0] : completedMaint[0].completeTime
        }
      } catch (e) { /* 忽略单个设备查询失败 */ }
    }))
  } catch (err) {
    console.error('加载设备列表失败', err)
    alert(err?.message || '加载失败')
  } finally { loading.value = false }
}

const getStatusBadgeClass = (status) => ({ available: 'status-available', borrowed: 'status-borrowed', maintenance: 'status-maintenance' }[status] || '')
const getStatusLabel = (status) => ({ available: '可用', borrowed: '借用中', maintenance: '维修中' }[status] || status)

const openAddModal = () => { editing.value = false; formData.value = { code: '', name: '', category: '', location: '', purchaseDate: '', status: 'available' }; showModal.value = true }
const openEditModal = (eq) => { editing.value = true; selectedEquipment.value = eq; formData.value = { ...eq }; showModal.value = true }
const openBorrowModal = (eq) => { selectedEquipment.value = eq; borrowForm.value = { borrowerId: '', borrowerName: '', purpose: '' }; showBorrowModal.value = true }
const openMaintenanceModal = (eq) => { selectedEquipment.value = eq; maintenanceForm.value = { description: '', cost: 0, technician: '' }; showMaintenanceModal.value = true }

const handleSubmit = async () => {
  try {
    if (editing.value) {
      const res = await equipmentApi.update(selectedEquipment.value.id, formData.value)
      ElMessage.success(res.msg || '更新成功')
    } else {
      const res = await equipmentApi.create(formData.value)
      ElMessage.success(res.msg || '新增成功')
    }
    await loadEquipment()
    showModal.value = false
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}
const handleDelete = async (id) => {
  if (!confirm('确定删除？')) return
  try {
    const res = await equipmentApi.delete(id)
    ElMessage.success(res.msg || '删除成功')
    await loadEquipment()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}
const handleReturn = async (eq) => {
  const borrowerId = eq.activeBorrowerId
  if (!borrowerId) {
    ElMessage.error('未找到借用记录，无法归还')
    return
  }
  try {
    const res = await equipmentApi.return(eq.id, { userId: borrowerId })
    ElMessage.success(res.msg || '归还成功')
    await loadEquipment()
  } catch (e) {
    ElMessage.error(e.message || '归还失败')
  }
}
const handleBorrow = async () => {
  const selectedPerson = personnelList.value.find(p => String(p.userId) === borrowForm.value.borrowerId)
  const borrowerName = selectedPerson?.name || selectedPerson?.username || '未知'
  try {
    const res = await equipmentApi.borrow(selectedEquipment.value.id, { userId: borrowForm.value.borrowerId, borrower: borrowerName, purpose: borrowForm.value.purpose })
    ElMessage.success(res.msg || '借用成功')
    await loadEquipment()
    showBorrowModal.value = false
    addNotification('设备借用', `${borrowerName} 借用了 ${selectedEquipment.value.name}，用途：${borrowForm.value.purpose}`, 'borrow')
  } catch (e) {
    ElMessage.error(e.message || '借用失败')
  }
}
const handleMaintenance = async () => {
  const labUser = JSON.parse(localStorage.getItem('labUser') || '{}')
  const payload = {
    faultDescription: maintenanceForm.value.description,
    repairCost: String(maintenanceForm.value.cost),
    repairPerson: maintenanceForm.value.technician,
    status: 'pending',
    reporterId: selectedEquipment.value.activeBorrowerId ? Number(selectedEquipment.value.activeBorrowerId) : (labUser.id || labUser.userId || null)
  }
  try {
    const res = await equipmentApi.maintenance(selectedEquipment.value.id, payload)
    ElMessage.success(res.msg || '提交维修成功')
    await loadEquipment()
    showMaintenanceModal.value = false
    addNotification('设备维修', `${selectedEquipment.value.name} 已提交维修申请`, 'maintenance')
  } catch (e) {
    ElMessage.error(e.message || '提交维修失败')
  }
}
const completeMaintenance = async (eq) => {
  if (!confirm('确认该设备已完成维修吗？')) return
  try {
    const rawRecords = await equipmentApi.getMaintenanceRecords(eq.id)
    const records = (Array.isArray(rawRecords) ? rawRecords : (rawRecords?.content || []))
    const activeRecord = records.find(r => r.status === 'pending' || r.status === 'in_progress')
    if (!activeRecord) {
      ElMessage.error('未找到进行中的维修记录')
      return
    }
    const payload = {
      id: activeRecord.id,
      completeTime: new Date().toISOString().split('T')[0] + 'T00:00:00',
      repairResult: '维修完成'
    }
    const res = await equipmentApi.completeMaintenance(eq.id, payload)
    ElMessage.success(res.msg || '完成维修成功')
    await loadEquipment()
    addNotification('维修完成', `${eq.name} 已完成维修，现在可用`, 'maintenance')
  } catch (e) {
    ElMessage.error(e.message || '完成维修失败')
  }
}
const viewDetail = async (eq) => {
  selectedEquipment.value = eq
  const rawRecords = await equipmentApi.getMaintenanceRecords(eq.id)
  // 后端字段反向映射：faultDescription->description, repairCost->cost, repairPerson->technician, completeTime->endDate
  maintenanceRecords.value = (Array.isArray(rawRecords) ? rawRecords : (rawRecords?.content || [])).map(rec => ({
    ...rec,
    description: rec.faultDescription || rec.description || '',
    cost: rec.repairCost || rec.cost || 0,
    technician: rec.repairPerson || rec.technician || '',
    startDate: rec.startDate || rec.createTime?.split('T')[0] || '',
    endDate: rec.completeTime?.split('T')[0] || rec.endDate || ''
  }))
  const rawUsageRecords = await equipmentApi.getUsageRecords(eq.id)
  usageRecords.value = (Array.isArray(rawUsageRecords) ? rawUsageRecords : (rawUsageRecords?.content || [])).map(rec => ({
    ...rec,
    borrower: rec.borrower || (rec.userId ? personnelList.value.find(p => String(p.userId) === String(rec.userId))?.name : '') || `用户${rec.userId || rec.user_id}`,
    borrowDate: rec.borrowTime ? rec.borrowTime.split('T')[0] : (rec.borrowDate || ''),
    returnDate: rec.returnTime ? rec.returnTime.split('T')[0] : (rec.returnDate || ''),
    purpose: rec.purpose || ''
  }))
  showDetailModal.value = true
}

onMounted(loadEquipment)
</script>