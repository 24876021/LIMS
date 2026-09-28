<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <h1 class="text-2xl font-bold text-gray-900">资源管理</h1>
      <button v-if="hasPermission('resource', 'add')" @click="openAddModal" class="btn-primary"><Plus class="w-4 h-4" />新增物资</button>
    </div>

    <div class="card">
      <div class="flex flex-col md:flex-row gap-4"><div class="flex-1 relative"><Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" /><input v-model="searchTerm" placeholder="搜索物资名称、编号..." class="w-full border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent pl-10 pr-4 py-2" /></div><select v-model="categoryFilter" class="input-field w-full md:w-48"><option value="all">全部分类</option><option v-for="c in categories" :key="c" :value="c">{{ c }}</option></select></div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div v-for="r in filteredResources" :key="r.id" class="card hover:shadow-md transition-shadow">
        <div class="flex items-start justify-between mb-4"><div class="w-12 h-12 bg-primary-100 rounded-xl flex items-center justify-center"><Package class="w-6 h-6 text-primary-600" /></div><span :class="getStockStatusClass(r)">{{ getStockStatusLabel(r) }}</span></div>
        <h3 class="font-semibold text-gray-900 mb-1">{{ r.name }}</h3><p class="text-sm text-gray-500 mb-3">{{ r.specification }}</p>
        <div class="space-y-2 text-sm"><div class="flex justify-between"><span class="text-gray-500">库存数量</span><span class="font-medium">{{ r.quantity }} {{ r.unit }}</span></div><div class="flex justify-between"><span class="text-gray-500">单价</span><span class="font-medium text-primary-600">¥{{ r.price || 0 }}</span></div><div class="flex justify-between"><span>存放位置</span><span>{{ r.location }}</span></div><div v-if="r.description" class="mt-2 pt-2 border-t text-gray-500 text-xs line-clamp-2">{{ r.description }}</div></div>
        <div class="flex gap-2 mt-4 pt-4 border-t">
          <button v-if="hasPermission('resource', 'set')" @click="openPurchaseModal(r)" class="flex-1 btn-secondary justify-center text-sm py-2"><TrendingUp class="w-4 h-4" />采购</button>
          <button v-if="hasPermission('resource', 'get')" @click="openUsageModal(r)" class="flex-1 btn-secondary justify-center text-sm py-2"><TrendingDown class="w-4 h-4" />领用</button>
          <button v-if="hasPermission('resource', 'get')" @click="viewDetail(r)" class="p-2 hover:bg-gray-100 rounded-lg"><History class="w-4 h-4" /></button>
          <button v-if="hasPermission('resource', 'set')" @click="openEditModal(r)" class="p-2 hover:bg-gray-100 rounded-lg"><Edit2 class="w-4 h-4" /></button>
        </div>
      </div>
    </div>

    <!-- Add/Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-lg"><div class="p-6 border-b flex justify-between"><h2 class="text-xl font-bold">{{ editing ? '编辑物资' : '新增物资' }}</h2><button @click="showModal=false"><X class="w-5 h-5" /></button></div>
        <form @submit.prevent="handleSubmit" class="p-6 space-y-4"><div><label>物资名称</label><input v-model="formData.name" required class="input-field" /></div><div><label>分类</label><input v-model="formData.category" required class="input-field" /></div><div><label>规格</label><input v-model="formData.specification" required class="input-field" /></div><div class="grid grid-cols-2 gap-4"><div><label>初始数量</label><input type="number" v-model.number="formData.quantity" required class="input-field" /></div><div><label>单位</label><input v-model="formData.unit" required class="input-field" /></div></div><div class="grid grid-cols-2 gap-4"><div><label>单价 (¥)</label><input type="number" v-model.number="formData.price" required class="input-field" step="0.01" /></div><div><label>存放位置</label><input v-model="formData.location" required class="input-field" /></div></div><div><label>描述</label><textarea v-model="formData.description" class="input-field min-h-[80px]" placeholder="请输入物资描述（可选）"></textarea></div><div class="flex gap-3"><button type="button" @click="showModal=false" class="flex-1 btn-secondary">取消</button><button type="submit" class="flex-1 btn-primary">保存</button></div></form></div>
    </div>

    <!-- Purchase Modal -->
    <!-- 采购入库弹窗 -->
    <div v-if="showPurchaseModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-md">
        <div class="p-6 border-b flex justify-between">
          <h2 class="text-xl font-bold">采购入库</h2>
          <button @click="showPurchaseModal=false"><X class="w-5 h-5" /></button>
        </div>
        <form @submit.prevent="handlePurchase" class="p-6 space-y-4">
          <div>
            <label>物资</label>
            <input type="text" :value="selectedResource?.name" disabled class="input-field bg-gray-50" />
          </div>
          <div>
            <label>采购数量</label>
            <input type="number" v-model="purchaseForm.quantity" required class="input-field" />
          </div>
          <div>
            <label>单价</label>
            <input type="number" v-model="purchaseForm.price" required class="input-field" step="0.01" />
          </div>
          <div>
            <label>采购人ID</label>
            <input type="number" v-model="purchaseForm.purchaserId" required class="input-field" />
          </div>
          <div>
            <label>供应商</label>
            <input type="text" v-model="purchaseForm.supplier" class="input-field" placeholder="可选" />
          </div>
          <div class="flex gap-3">
            <button type="button" @click="showPurchaseModal=false" class="flex-1 btn-secondary">取消</button>
            <button type="submit" class="flex-1 btn-primary">确认入库</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Usage Modal -->
    <div v-if="showUsageModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-md"><div class="p-6 border-b flex justify-between"><h2 class="text-xl font-bold">物资领用</h2><button @click="showUsageModal=false"><X class="w-5 h-5" /></button></div>
        <form @submit.prevent="handleUsage" class="p-6 space-y-4"><div><label>物资</label><input type="text" :value="`${selectedResource?.name} (库存: ${selectedResource?.quantity})`" disabled class="input-field bg-gray-50" /></div><div><label>领用数量</label><input type="number" v-model="usageForm.quantity" required class="input-field" :max="selectedResource?.quantity" /></div><div><label>领用人</label><select v-model="usageForm.userId" required class="input-field"><option value="">-- 请选择 --</option><option v-for="p in personnelList" :key="p.id" :value="p.id">{{ p.name || p.username || p.id }}{{ p.code ? ' (' + p.code + ')' : '' }}</option></select></div><div><label>用途</label><textarea v-model="usageForm.purpose" required class="input-field min-h-[80px]"></textarea></div><div class="flex gap-3"><button type="button" @click="showUsageModal=false" class="flex-1 btn-secondary">取消</button><button type="submit" class="flex-1 btn-primary">确认领用</button></div></form></div>
    </div>

    <!-- Detail Modal -->
    <div v-if="showDetailModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto"><div class="p-6 border-b flex justify-between"><h2 class="text-xl font-bold">{{ selectedResource?.name }} - 详细记录</h2><button @click="showDetailModal=false"><X class="w-5 h-5" /></button></div>
        <div class="p-6 space-y-6"><div><h3 class="font-semibold mb-3 flex items-center gap-2"><ShoppingCart class="w-4 h-4" />采购记录</h3><div v-for="record in purchaseRecords" :key="record.id" class="flex justify-between p-3 bg-gray-50 rounded-lg text-sm mb-2"><div><span class="font-medium">{{ record.date }}</span><span class="text-gray-500 ml-2">采购人ID: {{ record.purchaserId }}</span><span v-if="record.supplier" class="text-gray-500 ml-2">供应商: {{ record.supplier }}</span></div><div class="text-right"><div>+{{ record.quantity }} {{ selectedResource?.unit }}</div><div class="text-gray-500">¥{{ record.unitPrice }}</div></div></div><p v-if="!purchaseRecords.length" class="text-gray-400 text-sm">暂无采购记录</p></div>
          <div><h3 class="font-semibold mb-3 flex items-center gap-2"><TrendingDown class="w-4 h-4" />领用记录</h3><div v-for="record in usageRecords" :key="record.id" class="p-3 bg-gray-50 rounded-lg text-sm mb-2"><div class="flex justify-between"><span class="font-medium">{{ record.date }}</span><span class="text-red-600">-{{ record.quantity }} {{ selectedResource?.unit }}</span></div><div>领用人: {{ getPersonnelName(record.userId) }}</div><div class="text-gray-500 text-xs">用途: {{ record.purpose }}</div></div><p v-if="!usageRecords.length" class="text-gray-400 text-sm">暂无领用记录</p></div></div></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Search, Edit2, Package, TrendingUp, TrendingDown, History, X, ShoppingCart } from 'lucide-vue-next'
import { addNotification } from '@/utils/notification'
import { hasPermission } from '@/utils/permission'
import { ElMessage } from 'element-plus'
import { resourceApi, commonApi } from '../services/api'

const resources = ref([])
const searchTerm = ref('')
const categoryFilter = ref('all')
const showModal = ref(false)
const showPurchaseModal = ref(false)
const showUsageModal = ref(false)
const showDetailModal = ref(false)
const editing = ref(false)
const selectedResource = ref(null)
const formData = ref({ name: '', category: '', specification: '', quantity: 0, unit: '', location: '', price: 0, description: '' })
const purchaseForm = ref({ quantity: 0, price: 0, purchaserId: null, supplier: '' })
const usageForm = ref({ quantity: 0, userId: null, purpose: '' })
const personnelList = ref([]) // 人员列表用于领用选择
const purchaseRecords = ref([])
const usageRecords = ref([])

// 根据ID获取人员姓名
const getPersonnelName = (id) => {
  if (!id) return '-'
  const p = personnelList.value.find(p => p.id === id)
  return p ? (p.name || p.username || id) : id
}

const loadData = async () => {
  try {
    const data = await resourceApi.getList()
    resources.value = (Array.isArray(data) ? data : (data?.content || [])).map(r => ({
      ...r,
      // 后端没有 code/minStock/supplier 字段，设置默认值供前端使用
      code: r.code || '',
      minStock: r.minStock || 0,
      supplier: r.supplier || ''
    }))
    // 加载人员列表
    try {
      const pl = await commonApi.getSimpleUserList()
      personnelList.value = Array.isArray(pl) ? pl : (pl?.content || [])
    } catch (e) {
      console.error('加载人员列表失败', e)
      personnelList.value = []
    }
  } catch (e) {
    console.error('加载物资数据失败', e)
    resources.value = []
  }
}

const categories = computed(() => [...new Set(resources.value.map(r => r.category))])
const filteredResources = computed(() => {
  let list = resources.value
  if (searchTerm.value) list = list.filter(r => r.name.includes(searchTerm.value) || r.specification?.includes(searchTerm.value))
  if (categoryFilter.value !== 'all') list = list.filter(r => r.category === categoryFilter.value)
  return list
})

const getStockStatusClass = (r) => {
  if (r.quantity <= r.minStock) return 'status-rejected'
  if (r.quantity <= r.minStock * 1.5) return 'status-pending'
  return 'status-approved'
}
const getStockStatusLabel = (r) => {
  if (r.quantity <= r.minStock) return '库存不足'
  if (r.quantity <= r.minStock * 1.5) return '库存偏低'
  return '库存充足'
}

const openAddModal = () => { editing.value = false; formData.value = { name: '', category: '', specification: '', quantity: 0, unit: '', location: '', price: 0, description: '' }; showModal.value = true }
const openEditModal = (r) => { editing.value = true; selectedResource.value = r; formData.value = { name: r.name, category: r.category, specification: r.specification, quantity: r.quantity, unit: r.unit, location: r.location, price: r.price || 0, description: r.description || '' }; showModal.value = true }
const openPurchaseModal = (r) => { selectedResource.value = r; purchaseForm.value = { quantity: 0, price: 0, purchaserId: null, supplier: '' }; showPurchaseModal.value = true }
const openUsageModal = (r) => { selectedResource.value = r; usageForm.value = { quantity: 0, userId: null, purpose: '' }; showUsageModal.value = true }

const handleSubmit = async () => {
  try {
    // 只发送后端有的字段：name, category, specification, quantity, unit, location, price, description
    const payload = {
      name: formData.value.name,
      category: formData.value.category,
      specification: formData.value.specification,
      quantity: formData.value.quantity,
      unit: formData.value.unit,
      location: formData.value.location,
      price: formData.value.price,
      description: formData.value.description
    }
    if (editing.value) {
      const res = await resourceApi.update(selectedResource.value.id, payload)
      ElMessage.success(res.msg || '更新成功')
      addNotification('物资编辑', `物资 ${selectedResource.value.name} 信息已更新`, 'resources')
    } else {
      const res = await resourceApi.create(payload)
      ElMessage.success(res.msg || '新增成功')
      addNotification('物资新增', `新增物资 ${formData.value.name}`, 'resources')
    }
    showModal.value = false
    await loadData()
  } catch (e) {
    console.error('保存物资失败', e)
    ElMessage.error(e.message)
  }
}
const handleDelete = async (id) => {
  if (confirm('确定删除？')) {
    const found = resources.value.find(r => r.id === id)
    try {
      const res = await resourceApi.delete(id)
      ElMessage.success(res.msg || '删除成功')
      addNotification('物资删除', `物资 ${found?.name} 已从库存中移除`, 'resources')
      await loadData()
    } catch (e) {
      console.error('删除物资失败', e)
      ElMessage.error(e.message)
    }
  }
}
const handlePurchase = async () => {
  try {
    // 前端 price -> 后端 unitPrice，前端 purchaserId -> 后端 purchaserId
    const payload = {
      quantity: purchaseForm.value.quantity,
      unitPrice: purchaseForm.value.price,
      purchaserId: purchaseForm.value.purchaserId,
      supplier: purchaseForm.value.supplier || ''
    }
    const res = await resourceApi.purchase(selectedResource.value.id, payload)
    ElMessage.success(res.msg || '采购入库成功')
    addNotification('采购入库', `${selectedResource.value.name} 入库 ${purchaseForm.value.quantity}${selectedResource.value.unit}，单价 ¥${purchaseForm.value.price}`, 'purchase')
    showPurchaseModal.value = false
    await loadData()
  } catch (e) {
    console.error('采购入库失败', e)
    ElMessage.error(e.message)
  }
}
const handleUsage = async () => {
  if (usageForm.value.quantity > selectedResource.value.quantity) { alert('库存不足！'); return }
  try {
    // 前端 userId -> 后端 userId
    const payload = {
      quantity: usageForm.value.quantity,
      userId: usageForm.value.userId,
      purpose: usageForm.value.purpose
    }
    const res = await resourceApi.usage(selectedResource.value.id, payload)
    ElMessage.success(res.msg || '领用成功')
    addNotification('物资领用', `用户ID ${usageForm.value.userId} 领用了 ${selectedResource.value.name} ${usageForm.value.quantity}${selectedResource.value.unit}，用途：${usageForm.value.purpose}`, 'usage')
    showUsageModal.value = false
    await loadData()
  } catch (e) {
    console.error('物资领用失败', e)
    ElMessage.error(e.message)
  }
}
const viewDetail = async (r) => {
  selectedResource.value = r
  try {
    const data = await resourceApi.getRecords(r.id)
    // 后端返回扁平混合数组，用 unitPrice 字段区分：PurchaseRecord 有 unitPrice，UsageRecord 没有
    const allRecords = Array.isArray(data) ? data : []
    purchaseRecords.value = allRecords.filter(rec => rec.unitPrice != null).map(rec => ({
      id: rec.id,
      date: rec.purchaseTime ? rec.purchaseTime.substring(0, 10) : (rec.createTime ? rec.createTime.substring(0, 10) : ''),
      purchaserId: rec.purchaserId,
      quantity: rec.quantity,
      unitPrice: rec.unitPrice,
      supplier: rec.supplier || ''
    }))
    usageRecords.value = allRecords.filter(rec => rec.unitPrice == null).map(rec => ({
      id: rec.id,
      date: rec.usageTime ? rec.usageTime.substring(0, 10) : (rec.createTime ? rec.createTime.substring(0, 10) : ''),
      userId: rec.userId,
      quantity: rec.quantity,
      purpose: rec.purpose
    }))
  } catch (e) {
    console.error('加载记录失败', e)
    purchaseRecords.value = []
    usageRecords.value = []
  }
  showDetailModal.value = true
}

onMounted(loadData)
</script>