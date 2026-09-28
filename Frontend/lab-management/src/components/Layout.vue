<template>
  <div class="min-h-screen bg-gray-50 flex">
    <!-- Mobile Sidebar Overlay -->
    <div v-if="sidebarOpen" class="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden" @click="sidebarOpen = false" />

    <!-- Sidebar (始终固定在左侧) -->
    <aside :class="[
      'fixed inset-y-0 left-0 z-50 w-64 bg-white border-r border-gray-200 transform transition-transform duration-300 ease-in-out',
      sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
    ]">
      <div class="h-full flex flex-col">
        <!-- Logo -->
        <div class="p-6 border-b border-gray-100">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-primary-600 rounded-xl flex items-center justify-center">
              <Cpu class="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 class="font-bold text-lg text-gray-900">实验室管理</h1>
              <p class="text-xs text-gray-500">Lab Management</p>
            </div>
          </div>
        </div>

        <!-- Navigation (动态权限) -->
        <nav class="flex-1 overflow-y-auto p-4 space-y-1">
          <router-link v-for="item in filteredMenuItems" :key="item.path" :to="item.path" custom v-slot="{ isActive, navigate }">
            <div :class="['sidebar-item', { active: isActive }]" @click="navigate">
              <component :is="item.icon" class="w-5 h-5" />
              <span>{{ item.label }}</span>
            </div>
          </router-link>
        </nav>

        <!-- User Info -->
        <div class="p-4 border-t border-gray-100">
          <div class="flex items-center gap-3 mb-3 cursor-pointer hover:bg-gray-50 rounded-lg p-1 -m-1 transition-colors" @click="$router.push('/profile')">
            <div class="w-10 h-10 rounded-full bg-primary-100 flex items-center justify-center overflow-hidden">
              <img v-if="user?.avatar" :src="user.avatar" class="w-full h-full object-cover" />
              <User v-else class="w-5 h-5 text-primary-600" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">{{ user?.name || '管理员' }}</p>
              <p class="text-xs text-gray-500 truncate">{{ userRole || '普通用户' }}</p>
            </div>
          </div>
          <button @click="handleLogout" class="w-full flex items-center gap-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition-colors">
            <LogOut class="w-4 h-4" />
            退出登录
          </button>
        </div>
      </div>
    </aside>

    <!-- Main Content (右侧区域，左侧留出侧边栏宽度) -->
    <div class="flex-1 flex flex-col min-w-0 lg:ml-64">
      <!-- Header -->
      <header class="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-4 lg:px-8">
        <button @click="sidebarOpen = true" class="lg:hidden p-2 hover:bg-gray-100 rounded-lg">
          <Menu class="w-5 h-5 text-gray-600" />
        </button>
        <div class="flex items-center gap-4 flex-1 justify-end">
          <!-- 搜索框 -->
          <div class="hidden md:flex items-center gap-2 bg-gray-100 px-4 py-2 rounded-lg relative">
            <Search class="w-4 h-4 text-gray-400" />
            <input ref="searchInput" type="text" placeholder="搜索设备、人员、物资..." class="bg-transparent border-none outline-none text-sm w-48" @keyup.enter="handleGlobalSearch" @input="handleGlobalSearch" />
            <div v-if="searchResults.length || searchLoading" class="absolute top-full left-0 mt-2 w-80 bg-white rounded-lg shadow-lg border z-50">
              <div v-if="searchLoading" class="p-4 text-center text-gray-400 text-sm">
                <svg class="animate-spin h-4 w-4 mx-auto mb-1 text-primary-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                搜索中...
              </div>
              <template v-else-if="searchResults.length">
                <div v-for="res in searchResults" :key="res.id + res.type" @click="goToResult(res)" class="p-3 hover:bg-gray-50 cursor-pointer border-b last:border-0">
                  <div class="font-medium">{{ res.name }}</div>
                  <div class="text-xs text-gray-500">{{ res.type }} · {{ res.code }}</div>
                </div>
              </template>
              <div v-else class="p-4 text-center text-gray-400 text-sm">未找到相关结果</div>
            </div>
          </div>

          <!-- 消息铃铛 -->
          <div class="relative" ref="notificationContainer">
            <button @click="toggleNotificationPanel" class="relative p-2 hover:bg-gray-100 rounded-lg">
              <Bell class="w-5 h-5 text-gray-600" />
              <span v-if="unreadCount > 0" class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
            </button>
            <div v-if="showPanel" class="absolute right-0 mt-2 w-80 bg-white rounded-xl shadow-lg border border-gray-200 z-50 overflow-hidden">
              <div class="p-4 border-b flex justify-between items-center">
                <h3 class="font-semibold text-gray-900">消息通知</h3>
                <div class="flex gap-2 text-xs">
                  <button @click="handleMarkAllRead" class="text-primary-600 hover:text-primary-700">全部已读</button>
                  <button @click="handleClearAll" class="text-gray-500 hover:text-gray-700">清空</button>
                </div>
              </div>
              <div class="max-h-96 overflow-y-auto">
                <div v-if="notifications.length === 0" class="p-6 text-center text-gray-400 text-sm">暂无消息</div>
                <div v-for="msg in notifications" :key="msg.id" :class="['p-4 border-b hover:bg-gray-50 cursor-pointer transition', msg.isRead ? 'bg-white' : 'bg-blue-50']" @click="handleMarkRead(msg.id)">
                  <div class="flex justify-between items-start">
                    <div class="flex-1">
                      <p class="text-sm font-medium text-gray-900">{{ msg.title }}</p>
                      <p class="text-xs text-gray-500 mt-1">{{ msg.content }}</p>
                      <p v-if="msg.senderName" class="text-xs text-gray-400 mt-1">发送者：{{ msg.senderName }}</p>
                      <p class="text-xs text-gray-400 mt-2">{{ msg.time }}</p>
                    </div>
                    <div v-if="!msg.isRead" class="w-2 h-2 bg-blue-500 rounded-full mt-2"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- 页面内容区域 -->
      <main class="flex-1 overflow-y-auto p-4 lg:p-8">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  LayoutDashboard, Cpu, Users, Package, Shield, Calendar, FileText,
  LogOut, Menu, Bell, Search, User
} from 'lucide-vue-next'
import { notificationApi, equipmentApi, personnelApi, resourceApi, userApi, loginApi } from '@/services/api'

const router = useRouter()
const sidebarOpen = ref(false)
const searchInput = ref(null)
const searchResults = ref([])

// 消息相关
const showPanel = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
const notificationContainer = ref(null)
let ws = null                 // WebSocket 连接实例

// 格式化通知时间
const formatNotificationTime = (timeStr) => {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  if (isNaN(d.getTime())) return String(timeStr)
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

// 所有菜单项
const allMenuItems = [
  { path: '/', icon: LayoutDashboard, label: '仪表盘', permission: 'dashboard' },
  { path: '/equipment', icon: Cpu, label: '设备管理', permission: 'equipment' },
  { path: '/personnel', icon: Users, label: '人员管理', permission: 'personnel' },
  { path: '/resources', icon: Package, label: '资源管理', permission: 'resource' },
  { path: '/safety', icon: Shield, label: '安全管理', permission: 'safety' },
  { path: '/reservation', icon: Calendar, label: '预约管理', permission: 'reservation' },
  { path: '/reports', icon: FileText, label: '报告管理', permission: 'report' },
]

// 获取当前用户权限
const userPermissions = computed(() => {
  const perms = localStorage.getItem('userPermissions')
  return perms ? JSON.parse(perms) : []
})

// 过滤后的菜单
const filteredMenuItems = computed(() => {
  const perms = userPermissions.value
  return allMenuItems.filter(item => {
    if (item.permission === 'dashboard') return true
    return perms.some(p => p.startsWith(`${item.permission}:`))
  })
})

const user = computed(() => {
  const saved = localStorage.getItem('labUser')
  return saved ? JSON.parse(saved) : null
})

const userRole = computed(() => {
  // 优先从 labUser 读取（role 字段已是中文描述）
  const userSaved = localStorage.getItem('labUser')
  if (userSaved) {
    try {
      const u = JSON.parse(userSaved)
      if (u.role) {
        return Array.isArray(u.role) ? u.role.join('、') : String(u.role)
      }
    } catch (e) {}
  }
  // 再从 userRoles 读取
  const saved = localStorage.getItem('userRoles')
  if (saved) {
    try {
      const roles = JSON.parse(saved)
      if (Array.isArray(roles) && roles.length > 0) {
        return roles.join('、')
      }
      if (typeof roles === 'string' && roles) return roles
    } catch (e) {}
  }
  return ''
})

const handleLogout = async () => {
  // 清除所有认证信息（兼容记住我：localStorage + sessionStorage）
  localStorage.removeItem('token')
  localStorage.removeItem('jwtToken')
  localStorage.removeItem('userId')
  localStorage.removeItem('labUser')
  localStorage.removeItem('userPermissions')
  localStorage.removeItem('userRoles')
  localStorage.removeItem('userAuthorities')
  localStorage.removeItem('userAccount')
  localStorage.removeItem('rememberMe')
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('jwtToken')
  sessionStorage.removeItem('userId')
  sessionStorage.removeItem('userAccount')
  sessionStorage.removeItem('userRoles')
  sessionStorage.removeItem('userAuthorities')
  // 调用后端登出接口
  try {
    await loginApi.logout()
  } catch (e) {
    // 即使登出请求失败也继续跳转
  }
  router.push('/login')
}

// ========== 全局搜索（后端 API + 本地缓存 + 防抖）==========
const searchCache = ref({ equipment: [], personnel: [], resources: [] })
const searchLoaded = ref(false)
const searchLoading = ref(false)
let debounceTimer = null

// 首次加载或页面切换后刷新搜索缓存
const loadSearchCache = async () => {
  try {
    const [eqRes, perRes, resRes] = await Promise.allSettled([
      equipmentApi.getList(),
      personnelApi.getList(),
      resourceApi.getList()
    ])
    searchCache.value.equipment = eqRes.status === 'fulfilled' ? (eqRes.value?.content || []) : []
    searchCache.value.personnel = perRes.status === 'fulfilled' ? (perRes.value?.content || []) : []
    searchCache.value.resources = resRes.status === 'fulfilled' ? (resRes.value?.content || []) : []
    searchLoaded.value = true
  } catch (e) {
    console.error('[搜索] 缓存加载失败', e)
  }
}

// 带防抖的搜索
const handleGlobalSearch = () => {
  // 清除之前的定时器
  if (debounceTimer) clearTimeout(debounceTimer)
  const keyword = searchInput.value?.value?.trim().toLowerCase()
  if (!keyword) {
    searchResults.value = []
    return
  }
  // 如果缓存还没加载好，先加载再搜
  if (!searchLoaded.value) {
    searchLoading.value = true
    debounceTimer = setTimeout(async () => {
      await loadSearchCache()
      searchLoading.value = false
      doSearch(keyword)
    }, 300)
    return
  }
  // 已有缓存，直接搜（200ms 防抖）
  debounceTimer = setTimeout(() => doSearch(keyword), 200)
}

const doSearch = (keyword) => {
  const { equipment, personnel, resources } = searchCache.value
  const eqResults = equipment
    .filter(e => e.name?.toLowerCase().includes(keyword) || e.code?.toLowerCase().includes(keyword))
    .map(e => ({ id: e.id, name: e.name, type: '设备', code: e.code || '', route: '/equipment' }))
  const perResults = personnel
    .filter(p => p.name?.toLowerCase().includes(keyword) || p.code?.toLowerCase().includes(keyword))
    .map(p => ({ id: p.id, name: p.name, type: '人员', code: p.code || '', route: '/personnel' }))
  const resResults = resources
    .filter(r => r.name?.toLowerCase().includes(keyword) || r.specification?.toLowerCase().includes(keyword))
    .map(r => ({ id: r.id, name: r.name, type: '物资', code: r.specification || '', route: '/resources' }))
  searchResults.value = [...eqResults, ...perResults, ...resResults].slice(0, 8)
}

const goToResult = (item) => {
  router.push(item.route)
  searchResults.value = []
  if (searchInput.value) searchInput.value.value = ''
}

// ========== 消息通知（后端 API + WebSocket 实时推送）==========
const loadNotifications = async () => {
  try {
    const data = await notificationApi.getList()
    const list = Array.isArray(data) ? data : (data?.content || [])
    notifications.value = list.map(n => ({
      id: n.id,
      title: n.title,
      content: n.content,
      time: formatNotificationTime(n.createTime),
      isRead: n.isRead,
      senderName: n.senderName || ''
    }))
    unreadCount.value = list.filter(n => !n.isRead).length
  } catch (e) {
    console.error('加载消息失败', e)
  }
}

const toggleNotificationPanel = (e) => {
  e.stopPropagation()
  showPanel.value = !showPanel.value
  if (showPanel.value) loadNotifications()
}

const handleMarkRead = async (id) => {
  try { await notificationApi.markRead(id) } catch (e) {}
  loadNotifications()
}

const handleMarkAllRead = async () => {
  try { await notificationApi.markAllRead() } catch (e) {}
  loadNotifications()
}

const handleClearAll = async () => {
  if (!confirm('确定清空所有消息吗？')) return
  try { await notificationApi.clearAll() } catch (e) {}
  loadNotifications()
}

// WebSocket 连接（原生 JS WebSocket，对应后端 /ws/{userId}）
const connectWebSocket = () => {
  const userStr = localStorage.getItem('labUser')
  if (!userStr) return
  let userId = ''
  try { userId = JSON.parse(userStr)?.id } catch (e) { return }
  if (!userId) return

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws/${userId}`
  ws = new WebSocket(wsUrl)

  ws.onopen = () => { console.log('[WS] 连接成功') }

  ws.onmessage = async (event) => {
    // 纯文本消息（forceLogout / refreshPermissions）不在 JSON 解析范围内
    if (event.data === 'forceLogout') {
      alert('账号在其他地方登录，被迫下线')
      router.push('/login')
      return
    }
    if (event.data === 'refreshPermissions') {
      await refreshPermissionsFromServer()
      window.location.reload()
      return
    }

    try {
      const msg = JSON.parse(event.data)
      if (msg.type === 'notification' && msg.data) {
        // 实时插入新消息
        const n = msg.data
        notifications.value.unshift({
          id: n.id,
          title: n.title,
          content: n.content,
          time: formatNotificationTime(n.time || n.createTime),
          isRead: false,
          senderName: n.senderName || ''
        })
        unreadCount.value += 1
        // 浏览器通知（若用户已授权）
        if (window.Notification && Notification.permission === 'granted') {
          try { new Notification(n.title || '新消息', { body: n.content || '' }) } catch (e) {}
        }
      }
    } catch (e) {
      console.error('[WS] 消息解析失败', e)
    }
  }

  ws.onclose = () => {
    console.log('[WS] 连接断开，3秒后重连...')
    setTimeout(connectWebSocket, 3000)
  }

  ws.onerror = (err) => {
    console.error('[WS] 连接错误', err)
  }
}

// 从服务端刷新用户权限并更新 localStorage（角色变更后无需重新登录）
const refreshPermissionsFromServer = async () => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId')
  if (!token || !userId) return
  try {
    const data = await userApi.getRoleAndAuthority(userId)
    if (data) {
      const storage = localStorage.getItem('rememberMe') === 'true' ? localStorage : sessionStorage
      const roles = data.role || []
      const authorities = data.authority || []
      storage.setItem('userRoles', JSON.stringify(roles))
      storage.setItem('userAuthorities', JSON.stringify(authorities))
      localStorage.setItem('userPermissions', JSON.stringify(authorities))
      // 同步更新 labUser 中的 role，使左下角角色名实时更新
      const labUserStr = localStorage.getItem('labUser')
      if (labUserStr) {
        try {
          const labUser = JSON.parse(labUserStr)
          labUser.role = roles
          localStorage.setItem('labUser', JSON.stringify(labUser))
        } catch (e) {}
      }
      console.log('[权限] 已从服务端刷新权限和角色缓存')
    }
  } catch (e) {
    console.error('[权限] 刷新失败', e)
  }
}

// ========== 点击空白关闭消息面板 ==========
const handleClickOutside = (event) => {
  if (showPanel.value && notificationContainer.value && !notificationContainer.value.contains(event.target)) {
    showPanel.value = false
  }
}

onMounted(() => {
  refreshPermissionsFromServer()
  loadNotifications()
  connectWebSocket()
  loadSearchCache() // 预加载搜索数据缓存
  // 请求浏览器通知权限（可选）
  if (window.Notification && Notification.permission === 'default') {
    Notification.requestPermission()
  }
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  if (ws) { ws.close(); ws = null }
  document.removeEventListener('click', handleClickOutside)
})
</script>