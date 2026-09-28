<template>
  <!-- 根容器：用户信息卡片 + 统计卡片 + 详情 + 考勤记录 -->
  <div class="space-y-6">
    <!-- 用户信息卡片 -->
    <div class="relative bg-gradient-to-r from-primary-600 to-primary-800 rounded-2xl p-8 text-white">
      <div class="flex items-center gap-6">
        <div class="relative">
          <div class="w-20 h-20 rounded-full bg-white/20 flex items-center justify-center border-4 border-white/30 overflow-hidden">
            <img v-if="user?.avatar" :src="user.avatar" class="w-full h-full object-cover" />
            <User v-else class="w-10 h-10" />
          </div>
          <label class="absolute bottom-0 right-0 w-6 h-6 bg-white rounded-full flex items-center justify-center cursor-pointer hover:bg-gray-100 shadow-lg">
            <Camera class="w-3 h-3 text-gray-600" />
            <input type="file" accept="image/*" class="hidden" @change="handleAvatarUpload" />
          </label>
        </div>
        <div>
          <h1 class="text-2xl font-bold">{{ user?.name || user?.username || '用户' }}</h1>
          <p class="text-primary-100 mt-1">账号：{{ user?.username || '-' }}</p>
          <div class="flex gap-2 mt-2">
            <span v-for="role in userRoles" :key="role" class="px-3 py-1 bg-white/20 rounded-full text-sm">
              {{ role }}
            </span>
          </div>
          <div class="flex gap-4 mt-2 text-sm text-primary-100">
            <span>性别：{{ user?.sex || '未知' }}</span>
            <span v-if="userDepartment">部门：{{ userDepartment }}</span>
          </div>
        </div>
      </div>
      <!-- 历史头像按钮 -->
      <button @click="showAvatarHistory" class="absolute bottom-3 right-3 w-8 h-8 bg-white/20 rounded-full flex items-center justify-center cursor-pointer hover:bg-white/30 transition-colors" title="历史头像">
        <History class="w-4 h-4 text-white" />
      </button>
      <!-- 发送通知按钮 -->
      <button @click="openSendDialog" class="absolute bottom-3 right-12 w-8 h-8 bg-white/20 rounded-full flex items-center justify-center cursor-pointer hover:bg-white/30 transition-colors" title="发送通知">
        <Send class="w-4 h-4 text-white" />
      </button>
    </div>

    <!-- 身份认证 -->
    <div class="card">
      <div class="flex items-center justify-between cursor-pointer" @click="certExpanded = !certExpanded">
        <h3 class="text-lg font-semibold text-gray-900 flex items-center gap-2">
          <ShieldCheck class="w-5 h-5 text-primary-600" /> 身份认证
        </h3>
        <div class="flex items-center gap-3">
          <!-- 已认证（已有人员记录关联） -->
          <span v-if="certPersonnel && certPersonnel.status !== 'pending'" class="px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm font-medium">已认证</span>
          <!-- 待确认 -->
          <span v-else-if="certPersonnel && certPersonnel.status === 'pending'" class="px-3 py-1 bg-yellow-100 text-yellow-700 rounded-full text-sm font-medium">审核中</span>
          <!-- 未认证 -->
          <span v-else class="px-3 py-1 bg-gray-100 text-gray-500 rounded-full text-sm font-medium">未认证</span>
          <ChevronDown class="w-5 h-5 text-gray-400 transition-transform" :class="{ 'rotate-180': certExpanded }" />
        </div>
      </div>

      <div v-show="certExpanded" class="mt-4">
        <!-- 已认证：显示人员信息 -->
        <div v-if="certPersonnel" class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div><span class="block text-xs text-gray-400 mb-1">工号</span><span class="text-sm text-gray-800">{{ certPersonnel.employeeNo || '-' }}</span></div>
          <div><span class="block text-xs text-gray-400 mb-1">姓名</span><span class="text-sm text-gray-800">{{ certPersonnel.name || '-' }}</span></div>
          <div><span class="block text-xs text-gray-400 mb-1">部门</span><span class="text-sm text-gray-800">{{ certPersonnel.department || '-' }}</span></div>
          <div><span class="block text-xs text-gray-400 mb-1">职位</span><span class="text-sm text-gray-800">{{ certPersonnel.position || '-' }}</span></div>
          <div><span class="block text-xs text-gray-400 mb-1">性别</span><span class="text-sm text-gray-800">{{ certPersonnel.gender || '-' }}</span></div>
          <div><span class="block text-xs text-gray-400 mb-1">手机</span><span class="text-sm text-gray-800">{{ certPersonnel.phone || '-' }}</span></div>
          <div><span class="block text-xs text-gray-400 mb-1">邮箱</span><span class="text-sm text-gray-800">{{ certPersonnel.email || '-' }}</span></div>
          <div><span class="block text-xs text-gray-400 mb-1">提交时间</span><span class="text-sm text-gray-800">{{ formatTime(certPersonnel.createTime) || '-' }}</span></div>
        </div>

        <!-- 未认证：显示认证表单 -->
        <div v-else>
          <p class="text-sm text-gray-500 mb-4">
            <Info class="w-4 h-4 inline mr-1" />
            认证后您的信息将出现在人员管理列表中，由管理员确认后正式生效。每个账户仅可认证一次。
          </p>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">工号 <span class="text-red-500">*</span></label>
              <input v-model="certForm.employeeNo" type="text" placeholder="请输入工号"
                     class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">职位</label>
              <input v-model="certForm.position" type="text" placeholder="请输入职位"
                     class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">姓名 <span class="text-red-500">*</span></label>
              <input v-model="certForm.name" type="text" placeholder="请输入真实姓名"
                     class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">性别</label>
              <select v-model="certForm.gender" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500">
                <option value="未知">请选择</option>
                <option value="男">男</option>
                <option value="女">女</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">手机号</label>
              <input v-model="certForm.phone" type="text" placeholder="请输入手机号"
                     class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">邮箱</label>
              <input v-model="certForm.email" type="text" placeholder="请输入邮箱"
                     class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
          </div>
          <div class="flex justify-end">
            <button @click="handleCertify" :disabled="certifying"
                    class="px-6 py-2 text-sm text-white bg-primary-600 hover:bg-primary-700 rounded-lg flex items-center gap-2 disabled:opacity-50 transition-colors">
              <CheckCircle v-if="!certifying" class="w-4 h-4" />
              <Loader2 v-else class="w-4 h-4 animate-spin" />
              {{ certifying ? '提交中...' : '提交认证' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <div class="card hover:shadow-lg transition-shadow cursor-pointer" @click="hasPermission('equipment', 'get') && scrollTo('borrowed')">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500 mb-1">借用的设备</p>
            <template v-if="hasPermission('equipment', 'get')">
              <p class="text-3xl font-bold text-gray-900">{{ borrowedEquipment.length }}</p>
            </template>
            <p v-else class="text-sm text-gray-400 italic">无权限</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-blue-100 flex items-center justify-center">
            <Cpu class="w-6 h-6 text-blue-600" />
          </div>
        </div>
      </div>
      <div class="card hover:shadow-lg transition-shadow cursor-pointer" @click="hasPermission('reservation', 'get') && scrollTo('reservations')">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500 mb-1">预约的实验室</p>
            <template v-if="hasPermission('reservation', 'get')">
              <p class="text-3xl font-bold text-gray-900">{{ myReservations.length }}</p>
            </template>
            <p v-else class="text-sm text-gray-400 italic">无权限</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-green-100 flex items-center justify-center">
            <Calendar class="w-6 h-6 text-green-600" />
          </div>
        </div>
      </div>
      <div class="card hover:shadow-lg transition-shadow cursor-pointer" @click="hasPermission('resource', 'get') && scrollTo('resources')">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500 mb-1">领用的资源</p>
            <template v-if="hasPermission('resource', 'get')">
              <p class="text-3xl font-bold text-gray-900">{{ usedResources.length }}</p>
            </template>
            <p v-else class="text-sm text-gray-400 italic">无权限</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-purple-100 flex items-center justify-center">
            <Package class="w-6 h-6 text-purple-600" />
          </div>
        </div>
      </div>
      <div class="card hover:shadow-lg transition-shadow cursor-pointer" @click="scrollTo('attendance')">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500 mb-1">待签到考勤</p>
            <p class="text-3xl font-bold text-gray-900">{{ pendingAttendance.length }}</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-orange-100 flex items-center justify-center">
            <ClipboardCheck class="w-6 h-6 text-orange-600" />
          </div>
        </div>
      </div>
    </div>

    <!-- 详细信息区域 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 借用的设备 -->
      <div id="borrowed" class="card">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-gray-900 flex items-center gap-2">
            <Cpu class="w-5 h-5 text-blue-600" /> 借用的设备
          </h3>
          <span class="text-sm text-gray-400">{{ borrowedEquipment.length }} 项</span>
        </div>
        <div v-if="borrowedEquipment.length === 0" class="text-center py-8 text-gray-400">
          <PackageOpen class="w-12 h-12 mx-auto mb-2 opacity-50" />
          <p>暂无借用设备</p>
        </div>
        <div v-else class="space-y-3">
          <div v-for="eq in borrowedEquipment" :key="eq.id" class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <div>
              <p class="font-medium text-gray-900">{{ eq.equipmentName }}</p>
              <p class="text-sm text-gray-500">{{ eq.equipmentCode || '-' }} · {{ eq.equipmentLocation || '-' }}</p>
              <p class="text-xs text-gray-400">借用日期: {{ eq.borrowDate || eq.date || '-' }}</p>
              <p v-if="eq.purpose" class="text-xs text-gray-400">用途: {{ eq.purpose }}</p>
            </div>
            <div class="text-right">
              <span class="status-pending text-xs px-2 py-1 rounded-full">借用中</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 预约的实验室 -->
      <div id="reservations" class="card">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-gray-900 flex items-center gap-2">
            <Calendar class="w-5 h-5 text-green-600" /> 预约的实验室
          </h3>
          <span class="text-sm text-gray-400">{{ myReservations.length }} 项</span>
        </div>
        <div v-if="myReservations.length === 0" class="text-center py-8 text-gray-400">
          <Calendar class="w-12 h-12 mx-auto mb-2 opacity-50" />
          <p>暂无预约</p>
        </div>
        <div v-else class="space-y-3">
          <div v-for="res in myReservations" :key="res.id" class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <div>
              <p class="font-medium text-gray-900">{{ res.labName }}</p>
              <p class="text-sm text-gray-500">{{ res.date }} {{ res.timeSlot }}</p>
              <p v-if="res.purpose" class="text-xs text-gray-400">用途: {{ res.purpose }}</p>
            </div>
            <div class="text-right">
              <span :class="getStatusClass(res.status)" class="text-xs px-2 py-1 rounded-full">
                {{ getStatusLabel(res.status) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 领用的资源 -->
      <div id="resources" class="card">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-gray-900 flex items-center gap-2">
            <Package class="w-5 h-5 text-purple-600" /> 领用的资源
          </h3>
          <span class="text-sm text-gray-400">{{ usedResources.length }} 项</span>
        </div>
        <div v-if="usedResources.length === 0" class="text-center py-8 text-gray-400">
          <Package class="w-12 h-12 mx-auto mb-2 opacity-50" />
          <p>暂无领用资源</p>
        </div>
        <div v-else class="space-y-3">
          <div v-for="res in usedResources" :key="res.id" class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <div>
              <p class="font-medium text-gray-900">{{ res.resourceName }}</p>
              <p class="text-sm text-gray-500">{{ res.date || '-' }} · {{ res.quantity || '-' }} {{ res.resourceUnit || '' }}</p>
              <p v-if="res.purpose" class="text-xs text-gray-400">用途: {{ res.purpose }}</p>
            </div>
            <div class="text-right">
              <span class="status-approved text-xs px-2 py-1 rounded-full">已领用</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 待签到考勤 -->
      <div id="attendance" class="card">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-gray-900 flex items-center gap-2">
            <ClipboardCheck class="w-5 h-5 text-orange-600" /> 待签到考勤
          </h3>
          <span class="text-sm text-gray-400">{{ pendingAttendance.length }} 项</span>
        </div>
        <div v-if="pendingAttendance.length === 0" class="text-center py-8 text-gray-400">
          <ClipboardCheck class="w-12 h-12 mx-auto mb-2 opacity-50" />
          <p>暂无待签到考勤</p>
        </div>
        <div v-else class="space-y-3">
          <div v-for="act in pendingAttendance" :key="act.id" class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <div>
              <p class="font-medium text-gray-900">{{ act.name || '考勤活动' }}</p>
              <p class="text-sm text-gray-500">
                开始：{{ formatTime(act.startTime) }} | 结束：{{ formatTime(act.endTime) }}
              </p>
            </div>
            <div class="text-right">
              <button @click="handleSignIn(act.id)"
                      :disabled="signingInId === act.id"
                      class="btn-primary text-xs px-3 py-1.5 rounded-full flex items-center gap-1">
                <CheckCircle v-if="signingInId !== act.id" class="w-3 h-3" />
                <Loader2 v-else class="w-3 h-3 animate-spin" />
                {{ signingInId === act.id ? '签到中...' : '立即签到' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 考勤记录 -->
    <div class="card">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold text-gray-900 flex items-center gap-2">
          <FileText class="w-5 h-5 text-gray-600" /> 考勤记录
        </h3>
        <span class="text-sm text-gray-400">{{ attendanceRecords.length }} 条</span>
      </div>
      <div v-if="attendanceRecords.length === 0" class="text-center py-8 text-gray-400">
        <FileText class="w-12 h-12 mx-auto mb-2 opacity-50" />
        <p>暂无考勤记录</p>
      </div>
      <div v-else class="overflow-x-auto">
        <table class="data-table">
          <thead>
            <tr>
              <th>日期</th>
              <th>活动名称</th>
              <th>签到时间</th>
              <th>基准时间</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in attendanceRecords" :key="r.id">
              <td>{{ r.date || '-' }}</td>
              <td>{{ r.activityTitle || '-' }}</td>
              <td>{{ r.checkIn || '未签到' }}</td>
              <td>{{ r.workStart || '-' }} ~ {{ r.workEnd || '-' }}</td>
              <td>
                <span :class="getAttendanceStatusClass(r.attendanceStatus)" class="text-xs px-2 py-1 rounded-full">
                  {{ r.attendanceStatus || r.status || '-' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <!-- 历史头像弹窗（Vue 3 多根模板合法） -->
  <div v-if="avatarHistoryVisible" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
    <div class="bg-white rounded-2xl w-full max-w-lg max-h-[80vh] flex flex-col">
      <div class="p-5 border-b flex justify-between items-center">
        <h2 class="text-lg font-bold">历史头像</h2>
        <button @click="avatarHistoryVisible = false" class="p-1 hover:bg-gray-100 rounded-lg"><X class="w-5 h-5" /></button>
      </div>
      <div class="p-5 overflow-y-auto flex-1">
        <div v-if="avatarHistoryLoading" class="text-center py-8 text-gray-400">加载中...</div>
        <div v-else-if="!avatarHistoryList.length" class="text-center py-8 text-gray-400">暂无历史头像</div>
        <div v-else class="grid grid-cols-3 gap-4">
          <div v-for="item in avatarHistoryList" :key="item.filename" class="relative group">
            <div class="aspect-square rounded-xl overflow-hidden border-2 bg-gray-50 cursor-pointer hover:shadow-md transition-shadow"
                 :class="item.url === user?.avatar ? 'border-primary-500' : 'border-gray-200'"
                 @click="handleSetAvatar(item)">
              <img :src="item.url" class="w-full h-full object-cover" />
            </div>
            <div v-if="item.url === user?.avatar" class="absolute top-1 left-1 bg-primary-500 text-white text-xs px-1.5 py-0.5 rounded">当前</div>
            <div v-if="item.url !== user?.avatar" class="absolute bottom-1 right-1 opacity-0 group-hover:opacity-100 transition-opacity">
              <button @click.stop="handleDeleteAvatar(item.filename)"
                      class="w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600">
                <Trash2 class="w-3 h-3" />
              </button>
            </div>
            <p class="text-xs text-gray-400 mt-1 truncate">{{ formatFileSize(item.size) }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 发送通知弹窗 -->
  <Teleport to="body">
    <div v-if="sendDialogVisible" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-md flex flex-col">
        <div class="p-5 border-b flex justify-between items-center">
          <h2 class="text-lg font-bold">发送通知</h2>
          <button @click="sendDialogVisible = false" class="p-1 hover:bg-gray-100 rounded-lg"><X class="w-5 h-5" /></button>
        </div>
        <div class="p-5 space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              接收用户
              <span class="ml-1 text-xs text-gray-400">（已选 {{ sendForm.receiverIds.length }} 人）</span>
            </label>
            <!-- 搜索框 -->
            <div class="relative mb-1">
              <input v-model="userSearch" type="text" placeholder="搜索姓名或账号..."
                     class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
            <!-- 用户多选列表 -->
            <div class="border border-gray-300 rounded-lg max-h-40 overflow-y-auto">
              <div v-if="filteredUserList.length === 0" class="px-3 py-4 text-center text-sm text-gray-400">暂无用户</div>
              <label v-for="u in filteredUserList" :key="u.userId"
                     class="flex items-center gap-2 px-3 py-2 hover:bg-gray-50 cursor-pointer border-b last:border-0 transition-colors"
                     :class="sendForm.receiverIds.includes(u.userId) ? 'bg-primary-50' : ''">
                <input type="checkbox" :value="u.userId" v-model="sendForm.receiverIds"
                       class="w-4 h-4 text-primary-600 rounded border-gray-300 focus:ring-primary-500" />
                <span class="text-sm text-gray-800">{{ u.name || u.account }}</span>
                <span class="text-xs text-gray-400">{{ u.account }}</span>
              </label>
            </div>
            <!-- 快捷操作 -->
            <div class="flex gap-3 mt-1">
              <button type="button" @click="sendForm.receiverIds = filteredUserList.map(u => u.userId)"
                      class="text-xs text-primary-600 hover:text-primary-700">全选</button>
              <button type="button" @click="sendForm.receiverIds = []"
                      class="text-xs text-gray-500 hover:text-gray-700">清空</button>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">标题</label>
            <input v-model="sendForm.title" type="text" placeholder="通知标题"
                   class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">内容</label>
            <textarea v-model="sendForm.content" rows="3" placeholder="通知内容"
                      class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 resize-none"></textarea>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">类型</label>
            <select v-model="sendForm.type" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500">
              <option value="info">普通消息</option>
              <option value="warning">警告</option>
              <option value="system">系统通知</option>
            </select>
          </div>
        </div>
        <div class="p-5 border-t flex justify-end gap-2">
          <button @click="sendDialogVisible = false" class="px-4 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg">取消</button>
          <button @click="handleSendNotification" :disabled="sending"
                  class="px-4 py-2 text-sm text-white bg-primary-600 hover:bg-primary-700 rounded-lg flex items-center gap-1 disabled:opacity-50">
            <Send v-if="!sending" class="w-4 h-4" />
            <Loader2 v-else class="w-4 h-4 animate-spin" />
            {{ sending ? '发送中...' : '发送' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { User, Cpu, Calendar, Package, ClipboardCheck, FileText, PackageOpen, CheckCircle, Loader2, Camera, History, Trash2, X, Send, ShieldCheck, Info, ChevronDown } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { equipmentApi, reservationApi, resourceApi, personnelApi, attendanceActivityApi, commonApi, labApi, departmentApi, notificationApi, myAttendanceApi, userApi, http } from '@/services/api'
import { hasPermission } from '@/utils/permission'

// 用户信息
const user = computed(() => {
  const saved = localStorage.getItem('labUser')
  return saved ? JSON.parse(saved) : null
})

const userRoles = computed(() => {
  const saved = localStorage.getItem('userRoles')
  if (saved) {
    try {
      const roles = JSON.parse(saved)
      return Array.isArray(roles) ? roles : [roles]
    } catch (e) {
      return []
    }
  }
  return []
})

const userId = computed(() => user.value?.id)
const myPersonnelId = ref(null) // 当前用户对应的 Personnel 主键 ID

// 用户部门信息
const departmentList = ref([])
const myPersonnelDeptId = ref(null)

const userDepartment = computed(() => {
  const deptId = myPersonnelDeptId.value
  if (!deptId || departmentList.value.length === 0) return ''
  const dept = departmentList.value.find(d => String(d.departmentId) === String(deptId))
  return dept?.departmentName || ''
})

// 数据
const borrowedEquipment = ref([])
const myReservations = ref([])
const usedResources = ref([])
const pendingAttendance = ref([])
const attendanceRecords = ref([])
const signingInId = ref(null)
const labList = ref([])
const avatarHistoryVisible = ref(false)
const avatarHistoryList = ref([])
const avatarHistoryLoading = ref(false)

// 认证相关
const certPersonnel = ref(null)
const certExpanded = ref(false)
const certifying = ref(false)
const certForm = ref({
  employeeNo: '',
  name: '',
  gender: '未知',
  phone: '',
  email: '',
  position: '',
  departmentId: null
})

// 发送通知相关
const sendDialogVisible = ref(false)
const sending = ref(false)
const sendForm = ref({ receiverIds: [], title: '', content: '', type: 'info' })
const userList = ref([])
const userSearch = ref('')

// 过滤后的用户列表（搜索）
const filteredUserList = computed(() => {
  const kw = userSearch.value.trim().toLowerCase()
  if (!kw) return userList.value
  return userList.value.filter(u =>
    (u.name && u.name.toLowerCase().includes(kw)) ||
    (u.account && u.account.toLowerCase().includes(kw))
  )
})

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// 显示历史头像
const showAvatarHistory = async () => {
  avatarHistoryVisible.value = true
  avatarHistoryLoading.value = true
  try {
    const data = await userApi.getAvatarHistory(userId.value)
    avatarHistoryList.value = data || []
  } catch (e) {
    console.error('获取历史头像失败', e)
  } finally {
    avatarHistoryLoading.value = false
  }
}

// 删除历史头像
const handleDeleteAvatar = async (filename) => {
  if (!confirm('确定要删除这个头像吗？')) return
  try {
    const res = await userApi.deleteAvatar(userId.value, filename)
    ElMessage.success(res.msg || '删除成功')
    avatarHistoryList.value = avatarHistoryList.value.filter(i => i.filename !== filename)
  } catch (e) {
    console.error('删除头像失败', e)
    ElMessage.error(e.message || '删除失败')
  }
}

// 切换头像
const handleSetAvatar = async (item) => {
  if (item.url === user.value?.avatar) return
  try {
    const fd = new FormData()
    fd.append('userId', userId.value)
    fd.append('avatarUrl', item.url)
    const res = await http.upload('/sysUser/uploadAvatar', fd)
    ElMessage.success(res.msg || '切换成功')
    const updatedUser = { ...user.value, avatar: item.url }
    localStorage.setItem('labUser', JSON.stringify(updatedUser))
    window.location.reload()
  } catch (e) {
    console.error('切换头像失败', e)
    ElMessage.error(e.message || '切换失败')
  }
}

// 头像上传处理
const handleAvatarUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    alert('请上传图片文件')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    alert('图片大小不能超过 2MB')
    return
  }
  try {
    if (!userId.value) {
      alert('用户未登录')
      return
    }
    const formData = new FormData()
    formData.append('userId', userId.value)
    formData.append('file', file)
    const uploadRes = await http.upload('/sysUser/uploadAvatar', formData)
    ElMessage.success(uploadRes.msg || '头像上传成功')
    const updatedUser = { ...user.value, avatar: uploadRes.data || uploadRes }
    localStorage.setItem('labUser', JSON.stringify(updatedUser))
    window.location.reload()
  } catch (err) {
    console.error('头像上传失败:', err)
    ElMessage.error(err.message || '头像上传失败')
  }
}

// 加载用户列表
const loadUserList = async () => {
  try {
    userList.value = await notificationApi.getUsers()
  } catch (e) {
    console.error('加载用户列表失败', e)
    userList.value = []
  }
}

// 打开发送通知弹窗
const openSendDialog = async () => {
  sendForm.value = { receiverIds: [], title: '', content: '', type: 'info' }
  userSearch.value = ''
  await loadUserList()
  sendDialogVisible.value = true
}

// 发送通知（支持多接收人）
const handleSendNotification = async () => {
  if (sendForm.value.receiverIds.length === 0) { alert('请至少选择一个接收用户'); return }
  if (!sendForm.value.title.trim()) { alert('请填写通知标题'); return }
  if (!sendForm.value.content.trim()) { alert('请填写通知内容'); return }
  sending.value = true
  try {
    // 逐一发送给每个接收人
    await Promise.all(sendForm.value.receiverIds.map(receiverId =>
      notificationApi.send({
        receiverId,
        title: sendForm.value.title,
        content: sendForm.value.content,
        type: sendForm.value.type
      })
    ))
    ElMessage.success(`发送成功！已发送给 ${sendForm.value.receiverIds.length} 位用户`)
    sendDialogVisible.value = false
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  } finally {
    sending.value = false
  }
}

// 签到处理
const handleSignIn = async (activityId) => {
  if (!myPersonnelId.value) {
    alert('当前用户未关联人员信息，无法签到')
    return
  }
  signingInId.value = activityId
  try {
    const signInRes = await myAttendanceApi.signIn(activityId)
    await loadData()
    ElMessage.success(signInRes.msg || '签到成功！')
  } catch (err) {
    ElMessage.error(err.message || '签到失败')
  } finally {
    signingInId.value = null
  }
}

// 加载认证状态
const loadCertification = async () => {
  try {
    const data = await personnelApi.getCertification()
    if (data) {
      certPersonnel.value = data
      // 如果用户已认证但 labUser 中缺少个人信息，同步更新
      const labUser = JSON.parse(localStorage.getItem('labUser') || '{}')
      let labUpdated = false
      if (data.phone && !labUser.phone) { labUser.phone = data.phone; labUpdated = true }
      if (data.email && !labUser.email) { labUser.email = data.email; labUpdated = true }
      if (labUpdated) {
        localStorage.setItem('labUser', JSON.stringify(labUser))
      }
      return
    }
  } catch (e) {
    console.error('加载认证状态失败', e)
  }
  // 未认证或加载失败：预填表单（从 labUser 读取账户信息，姓名需手动输入真实姓名）
  certPersonnel.value = null
  const labUser = JSON.parse(localStorage.getItem('labUser') || '{}')
  certForm.value.name = ''
  certForm.value.gender = labUser.sex || '未知'
  certForm.value.phone = labUser.phone || ''
  certForm.value.email = labUser.email || ''
}

// 提交认证
const handleCertify = async () => {
  if (!certForm.value.employeeNo.trim()) {
    alert('请输入工号')
    return
  }
  if (!certForm.value.name.trim()) {
    alert('请输入真实姓名')
    return
  }
  certifying.value = true
  try {
    const res = await personnelApi.certify({
      employeeNo: certForm.value.employeeNo,
      name: certForm.value.name,
      gender: certForm.value.gender,
      phone: certForm.value.phone,
      email: certForm.value.email,
      position: certForm.value.position,
      departmentId: certForm.value.departmentId
    })
    ElMessage.success(res.msg || '认证提交成功！请等待管理员确认。')
    await loadCertification()
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    certifying.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  if (isNaN(d.getTime())) return time
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

// 预约状态
const getStatusLabel = (status) => {
  const map = { pending: '待审批', approved: '已通过', rejected: '已拒绝', completed: '已完成', cancelled: '已取消' }
  return map[status] || status || '-'
}

const getStatusClass = (status) => {
  const map = { pending: 'status-pending', approved: 'status-approved', rejected: 'status-rejected', completed: 'status-approved', cancelled: 'status-rejected' }
  return map[status] || 'status-pending'
}

// 考勤状态样式
const getAttendanceStatusClass = (status) => {
  const map = { '正常': 'status-approved', '迟到': 'status-pending', '早退': 'status-pending', '缺勤': 'status-rejected', '请假': 'status-pending' }
  return map[status] || 'status-approved'
}

// 滚动到指定区域
const scrollTo = (id) => {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

// 加载数据
const loadData = async () => {
  try {
    // 加载认证状态
    loadCertification()

    // 获取当前用户的 Personnel ID 和考勤信息（走公共接口，无需人员管理权限）
    try {
      const myData = await myAttendanceApi.getMyAttendance()
      const d = myData || {}
      myPersonnelId.value = d.personnelId || null
      myPersonnelDeptId.value = null
      // 待签到考勤
      pendingAttendance.value = d.pendingActivities || []
      // 考勤记录
      const recordList = d.attendanceRecords || []
      attendanceRecords.value = recordList.sort((a, b) => {
        const timeA = a.sortTime || '9999-99-99 99:99:99'
        const timeB = b.sortTime || '9999-99-99 99:99:99'
        return timeB.localeCompare(timeA)
      })
    } catch (e) {
      console.error('加载考勤信息失败', e)
      myPersonnelId.value = null
    }

    const requests = []
    if (hasPermission('equipment', 'get')) requests.push(equipmentApi.getList())
    else requests.push(Promise.reject('no_permission'))
    if (hasPermission('reservation', 'get')) requests.push(reservationApi.getList())
    else requests.push(Promise.reject('no_permission'))
    if (hasPermission('resource', 'get')) requests.push(resourceApi.getList())
    else requests.push(Promise.reject('no_permission'))
    if (hasPermission('reservation', 'get')) requests.push(labApi.getList())
    else requests.push(Promise.reject('no_permission'))

    const [equipmentList, reservations, resources, labs] = await Promise.allSettled(requests)

    if (labs.status === 'fulfilled') {
      labList.value = labs.value?.content || labs.value || []
    }

    // 借用的设备
    if (equipmentList.status === 'fulfilled') {
      const list = equipmentList.value?.content || equipmentList.value || []
      const allBorrowRecords = []
      for (const eq of list) {
        try {
          const records = await equipmentApi.getUsageRecords(eq.id)
          const recList = Array.isArray(records) ? records : (records?.content || [])
          const myRecords = recList.filter(r =>
            (String(r.userId) === String(userId.value) || String(r.borrowerId) === String(userId.value)) &&
            !r.returnDate && !r.returnTime
          )
          myRecords.forEach(r => {
            allBorrowRecords.push({
              ...r,
              equipmentName: eq.name,
              equipmentCode: eq.code,
              equipmentLocation: eq.location
            })
          })
        } catch (e) {}
      }
      borrowedEquipment.value = allBorrowRecords.sort((a, b) => {
        const dateA = a.borrowDate || a.date || ''
        const dateB = b.borrowDate || b.date || ''
        return dateB.localeCompare(dateA)
      })
    }

    // 预约的实验室
    if (reservations.status === 'fulfilled') {
      const list = reservations.value?.content || reservations.value || []
      const myRes = list.filter(r =>
        String(r.userId) === String(userId.value) &&
        r.status !== 'rejected' &&
        r.status !== 'cancelled'
      )
      const reservationsWithLab = myRes.map(r => {
        const lab = labList.value?.find(l => l.id === r.labId)
        const startTime = r.startTime ? new Date(r.startTime) : null
        const endTime = r.endTime ? new Date(r.endTime) : null
        return {
          ...r,
          labName: lab?.name || `实验室${r.labId}`,
          date: startTime ? startTime.toLocaleDateString('zh-CN') : '-',
          timeSlot: startTime && endTime
            ? `${startTime.toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'})} - ${endTime.toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'})}`
            : '',
          purpose: r.purpose || ''
        }
      })
      myReservations.value = reservationsWithLab.sort((a, b) => {
        const dateA = a.startTime || a.date || ''
        const dateB = b.startTime || b.date || ''
        return dateB.localeCompare(dateA)
      })
    }

    // 领用的资源
    if (resources.status === 'fulfilled') {
      const list = resources.value?.content || resources.value || []
      const allUsageRecords = []
      for (const resource of list) {
        try {
          const records = await resourceApi.getRecords(resource.id)
          const recList = Array.isArray(records) ? records : (records?.content || [])
          const myRecords = recList.filter(r => String(r.userId) === String(userId.value))
          myRecords.forEach(r => {
            allUsageRecords.push({
              ...r,
              resourceName: resource.name,
              resourceUnit: resource.unit
            })
          })
        } catch (e) {}
      }
      usedResources.value = allUsageRecords.sort((a, b) => {
        const dateA = a.date || a.usageTime || ''
        const dateB = b.date || b.usageTime || ''
        return dateB.localeCompare(dateA)
      })
    }
  } catch (err) {
    console.error('加载个人中心数据失败:', err)
  }
}

onMounted(async () => {
  loadData()
  try {
    const deptRes = await departmentApi.getList()
    departmentList.value = Array.isArray(deptRes) ? deptRes : (deptRes?.content || [])
  } catch (e) {
    console.error('加载部门列表失败', e)
  }
})
</script>
