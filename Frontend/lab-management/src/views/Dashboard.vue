<template>
  <div class="space-y-6">
    <!-- Welcome Banner with greeting -->
    <div class="bg-gradient-to-r from-primary-600 to-primary-800 rounded-2xl p-8 text-white">
      <h1 class="text-3xl font-bold mb-2">欢迎使用实验室管理系统</h1>
      <p class="text-primary-100">{{ greeting }}，今天是 {{ currentDate }}</p>
    </div>

    <!-- Stats Cards with hover effects -->
    <div v-if="statsCards.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <div
          v-for="card in statsCards"
          :key="card.label"
          class="card hover:scale-105 hover:shadow-xl transition-all duration-300 cursor-pointer"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500 mb-1">{{ card.label }}</p>
            <p class="text-3xl font-bold text-gray-900">{{ card.value }}</p>
          </div>
          <div :class="`w-12 h-12 rounded-xl bg-${card.color}-100 flex items-center justify-center`">
            <component :is="card.icon" :class="`w-6 h-6 text-${card.color}-600`" />
          </div>
        </div>
      </div>
    </div>
    <div v-else class="card text-center py-12 text-gray-400">
      <p class="text-lg">没有相应模块的权限，请前往个人中心进行身份认证</p>
    </div>

    <!-- Charts Section -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Equipment Status Chart -->
      <div v-if="hasPermission('equipment', 'get')" class="card">
        <h3 class="text-lg font-semibold text-gray-900 mb-6">设备状态分布</h3>
        <div class="h-64">
          <v-chart v-if="equipmentStatus.length" :option="pieOption" autoresize />
        </div>
        <div class="flex justify-center gap-6 mt-4">
          <div v-for="item in equipmentStatus" :key="item.name" class="flex items-center gap-2">
            <div class="w-3 h-3 rounded-full" :style="{ backgroundColor: item.color }"></div>
            <span class="text-sm text-gray-600">{{ item.name }}: {{ item.value }}</span>
          </div>
        </div>
      </div>

      <!-- Weekly Usage Chart -->
      <div v-if="hasPermission('reservation', 'get')" class="card">
        <h3 class="text-lg font-semibold text-gray-900 mb-6">本周设备借用趋势</h3>
        <div class="h-64">
          <v-chart :option="lineOption" autoresize />
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart as ELineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import {
  Cpu, Users, Package, Calendar
} from 'lucide-vue-next'
import { dashboardApi } from '../services/api'
import { hasPermission } from '@/utils/permission'

use([CanvasRenderer, PieChart, ELineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const stats = ref({ equipment: 0, personnel: 0, resources: 0, reservations: 0 })
const equipmentStatus = ref([])
const weeklyUsage = ref([])
const loading = ref(true)

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour >= 5 && hour < 11) {
    return '早上好'
  } else if (hour >= 11 && hour < 13) {
    return '中午好'
  } else if (hour >= 13 && hour < 18) {
    return '下午好'
  } else {
    return '晚上好'
  }
})

const currentDate = computed(() => {
  const date = new Date();
  const ymd = date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
  const week = date.toLocaleDateString('zh-CN', { weekday: 'long' });
  return `${ymd} ${week}`; // 中间加空格
});

const statsCards = computed(() => {
  const cards = []
  if (hasPermission('equipment', 'get')) {
    cards.push({ icon: Cpu, label: '设备总数', value: stats.value.equipment, color: 'blue' })
  }
  if (hasPermission('personnel', 'get')) {
    cards.push({ icon: Users, label: '在册人员', value: stats.value.personnel, color: 'green' })
  }
  if (hasPermission('resource', 'get')) {
    cards.push({ icon: Package, label: '库存物资', value: stats.value.resources.toLocaleString(), color: 'purple' })
  }
  if (hasPermission('reservation', 'get')) {
    cards.push({ icon: Calendar, label: '待审预约', value: stats.value.reservations, color: 'orange' })
  }
  return cards
})

const pieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    data: equipmentStatus.value.map(item => ({ name: item.name, value: item.value, itemStyle: { color: item.color } })),
    label: { show: false }
  }]
}))

const lineOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: weeklyUsage.value.map(w => w.day), axisLabel: { rotate: 0 } },
  yAxis: {
    type: 'value',
    name: '借用次数',
    minInterval: 1,
    axisLabel: {
      formatter: (value) => Math.floor(value) === value ? value : Math.round(value)
    }
  },
  series: [{ type: 'line', data: weeklyUsage.value.map(w => w.usage), smooth: true, lineStyle: { color: '#3b82f6', width: 3 }, symbol: 'circle', symbolSize: 8, areaStyle: { opacity: 0.1, color: '#3b82f6' } }]
}))

const hasAnyDashboardPermission = () =>
  hasPermission('dashboard', 'get') ||
  hasPermission('equipment', 'get') ||
  hasPermission('personnel', 'get') ||
  hasPermission('resource', 'get') ||
  hasPermission('reservation', 'get')

const fetchStats = async () => {
  if (!hasAnyDashboardPermission()) {
    loading.value = false
    return
  }
  try {
    const [statsRes, equipmentStatusRes, weeklyRes] = await Promise.all([
      dashboardApi.getStats(),
      dashboardApi.getEquipmentStatus(),
      dashboardApi.getWeeklyUsage()
    ])
    // 后端字段名映射：equipmentTotal->equipment, personnelTotal->personnel 等
    stats.value = {
      equipment: statsRes.equipment ?? statsRes.equipmentTotal ?? 0,
      personnel: statsRes.personnel ?? statsRes.personnelTotal ?? 0,
      resources: statsRes.resources ?? 0,
      reservations: statsRes.reservations ?? statsRes.pendingReservations ?? 0
    }
    equipmentStatus.value = [
      { name: '可用', value: equipmentStatusRes.available || 0, color: '#10b981' },
      { name: '借用中', value: equipmentStatusRes.borrowed || 0, color: '#f59e0b' },
      { name: '维修中', value: equipmentStatusRes.maintenance || 0, color: '#ef4444' }
    ]
    // 后端返回 {labels: [...], data: [...]}，前端期望 [{day, usage}, ...]
    if (weeklyRes && weeklyRes.labels && weeklyRes.data) {
      weeklyUsage.value = weeklyRes.labels.map((day, i) => ({
        day,
        usage: weeklyRes.data[i] || 0
      }))
    } else if (Array.isArray(weeklyRes)) {
      weeklyUsage.value = weeklyRes
    } else {
      weeklyUsage.value = []
    }
  } catch (err) {
    console.error('加载仪表盘数据失败:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
})
</script>