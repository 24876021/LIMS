<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <h1 class="text-2xl font-bold text-gray-900">人员管理</h1>
    </div>

    <!-- Tab 切换 -->
    <div class="flex gap-2 border-b border-gray-200">
      <button v-for="tab in tabs" :key="tab.key" @click="activeTab = tab.key" :class="['flex items-center gap-2 px-4 py-3 border-b-2 transition-colors', activeTab === tab.key ? 'border-primary-600 text-primary-600' : 'border-transparent text-gray-500 hover:text-gray-700']">
        <component :is="tab.icon" class="w-4 h-4" />{{ tab.label }}
      </button>
    </div>

    <!-- 人员 Tab -->
    <div v-if="activeTab === 'personnel'">
      <div class="flex justify-end mb-4">
        <div class="flex gap-2">
          <button v-if="hasPermission('personnel', 'set')" @click="openPublishModal" class="btn-primary"><CalendarCheck class="w-4 h-4" />发布考勤</button>
          <button v-if="hasPermission('personnel', 'add')" @click="openAddModal" class="btn-secondary"><Plus class="w-4 h-4" />新增人员</button>
        </div>
      </div>

      <!-- 搜索框 -->
      <div class="card">
        <div class="relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input v-model="searchTerm" type="text" placeholder="搜索姓名、工号..." class="w-full border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent pl-10 pr-4 py-2" />
        </div>
      </div>

      <!-- 多个活跃考勤活动卡片 -->
      <div v-for="activity in activeActivities" :key="activity.id" class="card border-l-4 border-l-primary-600">
        <div class="flex justify-between items-start">
          <div>
            <h3 class="text-lg font-semibold text-gray-900">{{ activity.name || activity.title }}</h3>
            <p class="text-sm text-gray-500 mt-1">开始时间：{{ formatTime(activity.startTime) }} | 结束时间：{{ formatTime(activity.endTime) }}</p>
            <p class="text-sm text-gray-500">倒计时：<span class="font-mono text-primary-600 font-bold">{{ getCountdownText(activity) }}</span></p>
          </div>
          <div class="flex gap-2">
            <button v-if="canSignForOthers" @click="endActivity(activity.id)" class="text-red-600 hover:text-red-700 text-sm">结束签到</button>
            <button @click="refreshActiveActivities" class="text-primary-600 hover:text-primary-700 text-sm">刷新</button>
          </div>
        </div>
        <div class="mt-4 overflow-x-auto">
          <table class="data-table">
            <thead>
            <tr>
              <th>姓名</th>
              <th>工号</th>
              <th>签到状态</th>
              <th>签到时间</th>
              <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="p in getActivityParticipants(activity.id)" :key="p.personnelId">
              <td class="font-medium">{{ p.name }}</td>
              <td>{{ p.code }}</td>
              <td>
                  <span :class="p.signed ? 'status-approved' : 'status-pending'">
                    {{ p.signed ? '已签到' : '未签到' }}
                  </span>
              </td>
              <td>{{ p.signTime ? formatTime(p.signTime) : '-' }}</td>
              <td>
                <button v-if="!p.signed && canSignForOthers" @click="signForPerson(activity.id, p.personnelId)" class="text-sm text-blue-600 hover:underline">代签</button>
                <span v-else-if="!p.signed && !canSignForOthers" class="text-sm text-gray-400">待签到</span>
                <span v-else class="text-sm text-green-600">已完成</span>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 人员列表表格 -->
      <div class="card overflow-hidden">
        <div v-if="loading" class="flex justify-center h-64 items-center"><Loader2 class="w-8 h-8 animate-spin text-primary-600" /></div>
        <div v-else class="table-container">
          <table class="data-table">
            <thead>
            <tr>
              <th>工号</th>
              <th>姓名</th>
              <th>性别</th>
              <th>角色</th>
              <th>部门</th>
              <th>联系方式</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="p in filteredPersonnel" :key="p.id">
              <td class="font-medium">{{ p.code }}</td>
              <td><span>{{ p.name }}</span> <span v-if="!p.userId" class="tag-unlinked"><Users class="w-3 h-3" />未关联</span></td>
              <td>{{ p.gender || '未知' }}</td>
              <td>{{ p.role }}</td>
              <td>{{ getDepartmentName(p) }}</td>
              <td>
                <div class="text-xs">{{ p.email }}</div>
                <div class="text-gray-500">{{ p.phone }}</div>
              </td>
              <td>
                  <span :class="p.status === 'active' ? 'status-approved' : p.status === 'pending' ? 'status-pending' : 'status-rejected'">
                    {{ p.status === 'active' ? '在职' : p.status === 'pending' ? '待确认' : '离职' }}
                  </span>
              </td>
              <td>
                <div class="flex gap-2">
                  <!-- 待确认人员：仅显示审批按钮 -->
                  <template v-if="p.status === 'pending'">
                    <button @click="approveCertification(p)" title="通过认证" class="p-2 hover:bg-green-50 rounded-lg text-green-600"><CheckCircle class="w-4 h-4" /></button>
                    <button @click="rejectCertification(p)" title="拒绝认证" class="p-2 hover:bg-red-50 rounded-lg text-red-600"><X class="w-4 h-4" /></button>
                  </template>
                  <!-- 已确认人员：显示常规操作按钮 -->
                  <template v-else>
                    <button v-if="hasPermission('personnel', 'get')" @click="viewDetail(p)" class="p-2 hover:bg-gray-100 rounded-lg"><Shield class="w-4 h-4" /></button>
                    <button v-if="hasPermission('personnel', 'set')" @click="openEditModal(p)" class="p-2 hover:bg-gray-100 rounded-lg"><Edit2 class="w-4 h-4" /></button>
                    <button v-if="hasPermission('personnel', 'remove')" @click="handleDelete(p.id)" class="p-2 hover:bg-red-50 rounded-lg text-red-600"><Trash2 class="w-4 h-4" /></button>
                  </template>
                </div>
              </td>
            </tr>
            <tr v-if="filteredPersonnel.length === 0">
              <td colspan="8" class="text-center py-8 text-gray-400">暂无数据</td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 部门 Tab -->
    <div v-if="activeTab === 'department'">
      <div class="flex justify-end mb-4">
        <button v-if="hasPermission('personnel', 'add')" @click="openDeptAddModal" class="btn-primary"><Plus class="w-4 h-4" />新增部门</button>
      </div>

      <!-- 部门列表 -->
      <div class="card overflow-hidden">
        <div v-if="deptLoading" class="flex justify-center h-64 items-center"><Loader2 class="w-8 h-8 animate-spin text-primary-600" /></div>
        <div v-else class="table-container">
          <table class="data-table">
            <thead>
            <tr>
              <th>部门ID</th>
              <th>部门名称</th>
              <th>上级部门ID</th>
              <th>负责人</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="d in departments" :key="d.departmentId">
              <td class="font-medium">{{ d.departmentId }}</td>
              <td>{{ d.departmentName }}</td>
              <td>{{ d.parentId || '-' }}</td>
              <td>{{ getLeaderName(d) }}</td>
              <td>{{ d.createTime ? formatTime(d.createTime) : '-' }}</td>
              <td>
                <div class="flex gap-2">
                  <button v-if="hasPermission('personnel', 'set')" @click="openDeptEditModal(d)" class="p-2 hover:bg-gray-100 rounded-lg"><Edit2 class="w-4 h-4" /></button>
                  <button v-if="hasPermission('personnel', 'remove')" @click="handleDeptDelete(d.departmentId)" class="p-2 hover:bg-red-50 rounded-lg text-red-600"><Trash2 class="w-4 h-4" /></button>
                </div>
              </td>
            </tr>
            <tr v-if="departments.length === 0">
              <td colspan="6" class="text-center py-8 text-gray-400">暂无部门数据</td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 权限 Tab -->
    <div v-if="activeTab === 'permission'">
      <div class="flex justify-end mb-4">
        <button v-if="hasPermission('resource', 'add')" @click="openRoleAddModal" class="btn-primary"><Plus class="w-4 h-4" />新增角色</button>
      </div>
      <!-- 角色列表 -->
      <div class="card overflow-hidden">
        <div v-if="rolesLoading" class="flex justify-center h-64 items-center"><Loader2 class="w-8 h-8 animate-spin text-primary-600" /></div>
        <div v-else class="table-container">
          <table class="data-table">
            <thead>
            <tr>
              <th>角色ID</th>
              <th>角色名称</th>
              <th>角色描述</th>
              <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="r in roles" :key="r.roleId || r.id">
              <td class="font-medium">{{ r.roleId || r.id }}</td>
              <td>{{ r.roleName || r.name }}</td>
              <td>{{ r.description }}</td>
              <td>
                <div class="flex gap-2">
                  <button @click="openRoleManageForRole(r)" class="p-2 hover:bg-gray-100 rounded-lg"><Shield class="w-4 h-4" /></button>
                  <button v-if="hasPermission('resource', 'remove')" @click="handleDeleteRole(r.roleId || r.id)" class="p-2 hover:bg-red-50 rounded-lg text-red-600"><Trash2 class="w-4 h-4" /></button>
                </div>
              </td>
            </tr>
            <tr v-if="roles.length === 0">
              <td colspan="4" class="text-center py-8 text-gray-400">暂无角色数据</td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 新增/编辑人员弹窗 -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b flex justify-between"><h2 class="text-xl font-bold">{{ editing ? '编辑人员' : '新增人员' }}</h2><button @click="showModal=false"><X class="w-5 h-5" /></button></div>
        <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
          <div>
            <label>关联系统用户</label>
            <select v-model="formData.userId" @change="onSysUserChange(formData.userId)" class="input-field">
              <option :value="null">不关联（手动填写）</option>
              <option v-for="u in sysUsers" :key="u.userId || u.id" :value="u.userId || u.id">{{ u.name }} ({{ u.account }})</option>
            </select>
          </div>
          <div class="grid grid-cols-2 gap-4"><div><label>工号</label><input v-model="formData.code" required class="input-field" /></div><div><label>姓名</label><input v-model="formData.name" required class="input-field" /></div></div>
          <div class="grid grid-cols-2 gap-4">
            <div><label>角色</label><input v-model="formData.role" required class="input-field" /></div>
            <div>
              <label>部门</label>
              <select v-model="formData.departmentId" required class="input-field">
                <option value="">请选择部门</option>
                <option v-for="d in departments" :key="d.departmentId" :value="d.departmentId">{{ d.departmentName }}</option>
              </select>
            </div>
          </div>
          <div>
            <label>性别</label>
            <select v-model="formData.gender" class="input-field">
              <option value="">请选择性别</option>
              <option value="男">男</option>
              <option value="女">女</option>
              <option value="未知">未知</option>
            </select>
          </div>
          <div><label>邮箱</label><input type="email" v-model="formData.email" required class="input-field" /></div>
          <div><label>电话</label><input type="tel" v-model="formData.phone" required class="input-field" /></div>
          <!-- 认证审批（编辑待确认人员时显示） -->
          <div v-if="editing && formData.status === 'pending'">
            <label class="flex items-center gap-1">认证审批 <Shield class="w-4 h-4 text-yellow-500" /></label>
            <select v-model="formData.status" required class="input-field border-yellow-400 bg-yellow-50">
              <option value="pending" disabled>待确认（当前状态）</option>
              <option value="active">✓ 通过</option>
              <option value="rejected">✗ 拒绝</option>
            </select>
            <p class="text-xs text-gray-500 mt-1">该人员通过自助认证提交，请审核后确认状态。</p>
          </div>
          <!-- 角色设置（关联系统用户时可配置） -->
          <div v-if="formData.userId">
            <label>系统角色</label>
            <select v-model="formData.roleIds" multiple class="input-field" style="min-height: 80px">
              <option v-for="r in allRoles" :key="r.roleId || r.id" :value="String(r.roleId || r.id)">{{ r.description || r.roleName || r.name }}</option>
            </select>
            <p class="text-xs text-gray-500 mt-1">按住 Ctrl 可多选。修改角色后将立即生效。</p>
          </div>
          <div class="flex gap-3 pt-4"><button type="button" @click="showModal=false" class="flex-1 btn-secondary">取消</button><button type="submit" class="flex-1 btn-primary">保存</button></div>
        </form>
      </div>
    </div>

    <!-- 发布考勤弹窗（开始时间取当前实时时间，不显示在界面上） -->
    <div v-if="showPublishModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-md">
        <div class="p-6 border-b flex justify-between">
          <h2 class="text-xl font-bold">发布倒计时考勤</h2>
          <button @click="showPublishModal=false"><X class="w-5 h-5" /></button>
        </div>
        <form @submit.prevent="handlePublishActivity" class="p-6 space-y-4">
          <div><label>考勤标题</label><input v-model="publishForm.title" required class="input-field" placeholder="例如：上午签到" /></div>
          <div><label>持续时长（分钟）</label><input type="number" v-model.number="publishForm.duration" required min="1" class="input-field" /></div>
          <div><label>参与人员</label>
            <div class="border rounded-lg p-3 max-h-48 overflow-y-auto space-y-2">
              <label v-for="p in personnel" :key="p.id" class="flex items-center gap-2" :class="{'opacity-50': !p.userId}">
                <input type="checkbox" v-model="publishForm.selectedIds" :value="p.id" :disabled="!p.userId" class="rounded border-gray-300" />
                <span>{{ p.name }} ({{ p.code }})<span v-if="!p.userId" class="text-xs text-red-500 ml-1">未关联用户，无法发起签到</span></span>
              </label>
            </div>
            <div class="text-sm mt-1">
              <button type="button" @click="selectAllParticipants" class="text-primary-600 hover:underline">全选（仅可选关联用户的人员）</button>
              <span class="mx-2">|</span>
              <button type="button" @click="deselectAllParticipants" class="text-primary-600 hover:underline">取消全选</button>
            </div>
          </div>
          <div class="flex gap-3">
            <button type="button" @click="showPublishModal=false" class="flex-1 btn-secondary">取消</button>
            <button type="submit" class="flex-1 btn-primary">发布考勤</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 人员详情弹窗 -->
    <div v-if="showDetailModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b flex justify-between"><h2 class="text-xl font-bold">{{ selectedPerson?.name }} - 详细信息</h2><button @click="showDetailModal=false"><X class="w-5 h-5" /></button></div>
        <div class="p-6 space-y-6">
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div><span class="text-gray-500">工号:</span> {{ selectedPerson?.code }}</div>
            <div><span class="text-gray-500">姓名:</span> {{ selectedPerson?.name }}</div>
            <div><span class="text-gray-500">性别:</span> {{ selectedPerson?.gender || '未知' }}</div>
            <div><span class="text-gray-500">角色:</span> {{ selectedPerson?.role }}</div>
            <div><span class="text-gray-500">部门:</span> {{ selectedPerson?.department }}</div>
            <div><span class="text-gray-500">状态:</span> <span :class="selectedPerson?.status === 'active' ? 'status-approved' : selectedPerson?.status === 'pending' ? 'status-pending' : 'status-rejected'">{{ selectedPerson?.status === 'active' ? '在职' : selectedPerson?.status === 'pending' ? '待确认' : '离职' }}</span></div>
            <div><span class="text-gray-500">邮箱:</span> {{ selectedPerson?.email }}</div>
            <div><span class="text-gray-500">电话:</span> {{ selectedPerson?.phone }}</div>
          </div>

          <!-- 考勤记录表格 -->
          <div><h3 class="font-semibold mb-3 flex items-center gap-2"><Clock class="w-4 h-4" />考勤记录</h3>
            <div v-for="r in attendanceRecords" :key="r.id" class="p-3 bg-gray-50 rounded-lg text-sm mb-2">
              <div class="flex justify-between items-center flex-wrap gap-2">
                <div>
                  <span class="font-medium">{{ r.date }}</span>
                  <span v-if="r.activityTitle" class="text-xs text-gray-500 ml-2">（{{ r.activityTitle }}）</span>
                </div>
                <div class="flex items-center gap-3">
                  <span v-if="r.attendanceStatus === '迟到'" class="text-red-600 text-xs font-medium">迟到</span>
                  <span v-else-if="r.attendanceStatus === '早退'" class="text-orange-600 text-xs font-medium">早退</span>
                  <span v-else-if="r.attendanceStatus === '缺勤'" class="text-gray-600 text-xs font-medium">缺勤</span>
                  <span v-else-if="r.attendanceStatus === '请假'" class="text-blue-600 text-xs font-medium">请假</span>
                  <span v-else class="text-green-600 text-xs font-medium">正常</span>
                  <select
                      v-model="r.attendanceStatus"
                      @change="updateAttendanceStatus(r)"
                      class="text-xs border rounded px-2 py-1 bg-white"
                  >
                    <option value="正常">正常</option>
                    <option value="迟到">迟到</option>
                    <option value="早退">早退</option>
                    <option value="缺勤">缺勤</option>
                    <option value="请假">请假</option>
                  </select>
                </div>
              </div>
              <div class="text-gray-600 mt-2">签到时间：{{ r.checkIn || '未签到' }}</div>
              <div class="text-gray-500 text-xs">基准时间：{{ r.workStart }} - {{ r.workEnd }}</div>
            </div>
            <p v-if="!attendanceRecords.length" class="text-gray-400 text-sm">暂无考勤记录</p>
          </div>

          <!-- 培训记录 -->
          <div><h3 class="font-semibold mb-3 flex items-center gap-2"><BookOpen class="w-4 h-4" />培训记录</h3>
            <div v-for="r in trainingRecords" :key="r.id" class="p-3 bg-gray-50 rounded-lg text-sm">
              <div class="flex justify-between">
                <span class="font-medium">{{ r.name }}</span>
                <span :class="r.passed ? 'text-green-600' : 'text-red-600'">{{ r.passed ? '已通过' : '未通过' }} ({{ r.score }}分)</span>
              </div>
              <div class="text-gray-500 text-xs mt-1">考试时间: {{ r.date }} | 得分: {{ r.score }}/{{ r.totalScore }}</div>
            </div>
            <p v-if="!trainingRecords.length" class="text-gray-400 text-sm">暂无培训记录</p>
          </div>

          <!-- 权限列表 -->
          <div><h3 class="font-semibold mb-3 flex items-center gap-2"><Shield class="w-4 h-4" />权限列表</h3><div class="flex flex-wrap gap-2"><span v-for="perm in selectedPerson?.permissions" :key="perm" class="px-3 py-1 bg-primary-100 text-primary-700 rounded-full text-sm">{{ formatPermission(perm) }}</span></div></div>
        </div>
      </div>
    </div>

    <!-- 部门管理弹窗 -->
    <div v-if="showDeptModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-md">
        <div class="p-6 border-b flex justify-between">
          <h2 class="text-xl font-bold">{{ deptEditing ? '编辑部门' : '新增部门' }}</h2>
          <button @click="showDeptModal=false"><X class="w-5 h-5" /></button>
        </div>
        <form @submit.prevent="handleDeptSubmit" class="p-6 space-y-4">
          <div><label>部门名称</label><input v-model="deptForm.departmentName" required class="input-field" placeholder="请输入部门名称" /></div>
          <div><label>上级部门ID（可选）</label><input type="number" v-model="deptForm.parentId" class="input-field" placeholder="留空表示顶级部门" /></div>
          <div>
            <label>部门负责人</label>
            <select v-model="deptForm.leaderUserId" class="input-field">
              <option value="">请选择负责人</option>
              <option v-for="p in personnel" :key="p.id" :value="p.userId || p.user_id">{{ p.name }} ({{ p.code }})</option>
            </select>
          </div>
          <div class="flex gap-3 pt-4"><button type="button" @click="showDeptModal=false" class="flex-1 btn-secondary">取消</button><button type="submit" class="flex-1 btn-primary">保存</button></div>
        </form>
      </div>
    </div>

    <!-- 角色权限管理弹窗 -->
    <div v-if="showRoleManageModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b flex justify-between">
          <h2 class="text-xl font-bold">{{ getSelectedRoleName() }} - 权限配置</h2>
          <button @click="showRoleManageModal=false"><X class="w-5 h-5" /></button>
        </div>
        <div class="p-6 space-y-6">
          <!-- 权限列表 -->
          <div>
            <label class="block mb-2 font-medium">权限配置</label>
            <div class="border rounded-lg p-4 bg-gray-50 max-h-96 overflow-y-auto">
              <div v-for="auth in authorities" :key="auth.authorityId || auth.id" class="flex items-center gap-2 py-2 border-b last:border-0">
                <input 
                  type="checkbox" 
                  :value="String(auth.authorityId || auth.id)" 
                  v-model="selectedAuthorityIds"
                  class="rounded border-gray-300"
                />
                <span class="text-sm">{{ auth.authorityName || auth.name }}</span>
                <span class="text-xs text-gray-500">- {{ auth.description }}</span>
              </div>
            </div>
            <p class="text-xs text-gray-500 mt-2">已选择 {{ selectedAuthorityIds.length }} 项权限</p>
          </div>
        </div>
        <div class="p-6 border-t flex gap-3">
          <button type="button" @click="showRoleManageModal=false" class="flex-1 btn-secondary">取消</button>
          <button type="button" @click="saveRoleAuthority" class="flex-1 btn-primary">保存</button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑角色弹窗 -->
    <div v-if="showRoleModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-md">
        <div class="p-6 border-b flex justify-between">
          <h2 class="text-xl font-bold">{{ roleEditing ? '编辑角色' : '新增角色' }}</h2>
          <button @click="showRoleModal=false"><X class="w-5 h-5" /></button>
        </div>
        <form @submit.prevent="handleRoleSubmit" class="p-6 space-y-4">
          <div>
            <label>角色名称</label>
            <input v-model="roleForm.roleName" required class="input-field" placeholder="请输入角色名称" />
          </div>
          <div>
            <label>角色描述</label>
            <textarea v-model="roleForm.description" class="input-field" placeholder="请输入角色描述（可选）" rows="3"></textarea>
          </div>
          <div class="flex gap-3 pt-4">
            <button type="button" @click="showRoleModal=false" class="flex-1 btn-secondary">取消</button>
            <button type="submit" class="flex-1 btn-primary">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { Plus, Search, Edit2, Trash2, Shield, Clock, BookOpen, X, CheckCircle, Loader2, ChevronRight, ChevronDown, CalendarCheck, Users } from 'lucide-vue-next'
import { personnelApi, attendanceActivityApi, userApi, departmentApi, http, roleApi, safetyApi } from '../services/api'
import { moduleOperations, hasPermission, generateFullPermissions } from '@/utils/permission'
import { addNotification } from '@/utils/notification'
import { ElMessage } from 'element-plus'

// ========== 数据定义 ==========
// Tab 配置（根据权限动态过滤）
const tabs = computed(() => {
  const allTabs = [
    { key: 'personnel', label: '人员', icon: Users, permission: 'personnel' },
    { key: 'department', label: '部门', icon: Users, permission: 'personnel' },
    { key: 'permission', label: '权限', icon: Shield, permission: 'resource' }
  ]
  return allTabs.filter(tab => hasPermission(tab.permission))
})
const activeTab = ref('personnel')
// 如果当前 activeTab 不在可见 tabs 中，切换到第一个可见 tab
watch(tabs, (newTabs) => {
  if (newTabs.length > 0 && !newTabs.find(t => t.key === activeTab.value)) {
    activeTab.value = newTabs[0].key
  }
}, { immediate: true })

const personnel = ref([])
const sysUsers = ref([]) // 系统用户列表
const searchTerm = ref('')
const loading = ref(false)
const showModal = ref(false)
const showDetailModal = ref(false)
const showPublishModal = ref(false)
const showRoleManageModal = ref(false)
const roles = ref([])
const allRoles = ref([])  // 所有角色列表（用于人员编辑弹窗的角色多选）
const authorities = ref([])
const selectedRoleId = ref(null)
const selectedAuthorityIds = ref([])
const editing = ref(false)
const selectedPerson = ref(null)
const formData = ref({ code: '', name: '', gender: '', role: '', department: '', email: '', phone: '', permissions: [], userId: null, roleIds: [], status: '' })
const attendanceRecords = ref([])
const trainingRecords = ref([])

// 部门相关数据
const departments = ref([])
const deptLoading = ref(false)
const rolesLoading = ref(false)
const showDeptModal = ref(false)
const deptEditing = ref(false)
const deptForm = ref({ departmentName: '', parentId: null, leaderUserId: null })
// 角色弹窗
const showRoleModal = ref(false)
const roleEditing = ref(false)
const roleForm = ref({ roleId: null, roleName: '', description: '' })

// 选择系统用户后自动填充姓名、手机、邮箱
const onSysUserChange = (userId) => {
  const user = sysUsers.value.find(u => u.userId === userId || u.id === userId)
  if (user) {
    formData.value.name = user.name || formData.value.name
    formData.value.phone = user.phone || formData.value.phone
    formData.value.email = user.email || formData.value.email
  }
}

// 考勤活动（多个）
const activeActivities = ref([])
const activitiesParticipants = ref({})
let countdownIntervals = {}

const canSignForOthers = computed(() => hasPermission('personnel', 'set'))
const publishForm = ref({
  title: '',
  duration: 30,
  selectedIds: []
})

// 权限树配置
const moduleList = [
  { key: 'equipment', label: '设备管理' },
  { key: 'personnel', label: '人员管理' },
  { key: 'resource', label: '资源管理' },
  { key: 'safety', label: '安全管理' },
  { key: 'reservation', label: '预约管理' },
  { key: 'report', label: '报告管理' },
  { key: 'dashboard', label: '仪表盘' }
]
const expandedModules = ref([])

const getOperationLabel = (op) => {
  const labels = {
    all: '全部权限', get: '查看', set: '设置', add: '新增', remove: '删除'
  }
  return labels[op] || op
}

const formatPermission = (perm) => {
  // 后端返回的权限用 `-` 分隔（如 resource-all），前端表单用 `:` 分隔（如 equipment:get）
  const sep = perm.includes(':') ? ':' : '-'
  const [module, op] = perm.split(sep)
  const moduleLabel = moduleList.find(m => m.key === module)?.label || module
  const opLabel = getOperationLabel(op)
  return `${moduleLabel}·${opLabel}`
}

const toggleModule = (key) => {
  if (expandedModules.value.includes(key)) {
    expandedModules.value = expandedModules.value.filter(k => k !== key)
  } else {
    expandedModules.value.push(key)
  }
}

// 计算属性
const filteredPersonnel = computed(() => {
  if (!searchTerm.value) return personnel.value
  return personnel.value.filter(p => p.name.includes(searchTerm.value) || p.code.includes(searchTerm.value))
})

const formatTime = (isoString) => {
  if (!isoString) return ''
  return new Date(isoString).toLocaleString()
}

const getActivityParticipants = (activityId) => activitiesParticipants.value[activityId] || []

const getCountdownText = (activity) => {
  const now = new Date()
  const end = new Date(activity.endTime)
  const diff = Math.floor((end - now) / 1000)
  if (diff <= 0) return '已结束'
  const hours = Math.floor(diff / 3600)
  const minutes = Math.floor((diff % 3600) / 60)
  const seconds = diff % 60
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}

// 刷新所有活跃考勤活动
const refreshActiveActivities = async () => {
  const activities = await attendanceActivityApi.getActiveActivities()
  activeActivities.value = activities
  
  // 清理不在活跃列表中的活动参与者数据
  const activeIds = new Set(activities.map(a => a.id))
  Object.keys(activitiesParticipants.value).forEach(id => {
    if (!activeIds.has(Number(id))) {
      delete activitiesParticipants.value[id]
    }
  })
  
  for (const activity of activities) {
    try {
      const result = await attendanceActivityApi.getActivityStatus(activity.id)
      // 后端返回 AttendanceRecord 列表
      const statusList = Array.isArray(result) ? result : (result?.status || [])
      const participantsWithInfo = statusList
        .map(s => {
          const p = personnel.value.find(p => String(p.id) === String(s.personnelId))
          return {
            ...s,
            personnelId: s.personnelId,
            signed: s.signTime != null || s.status !== 'absent',
            signTime: s.signTime,
            name: p?.name,
            code: p?.code
          }
        })
        .filter(p => p.name && p.code) // 过滤掉人员信息不存在的记录
      activitiesParticipants.value[activity.id] = participantsWithInfo
    } catch (err) {
      console.error(err)
    }
  }
  startAllCountdowns()
}

const startAllCountdowns = () => {
  Object.values(countdownIntervals).forEach(clearInterval)
  countdownIntervals = {}
  for (const activity of activeActivities.value) {
    const interval = setInterval(async () => {
      const now = new Date()
      const end = new Date(activity.endTime)
      if (now >= end) {
        clearInterval(interval)
        try {
          await attendanceActivityApi.endActivity(activity.id)
        } catch (err) {
          console.error('自动结束考勤失败', err)
        }
        await refreshActiveActivities()
      } else {
        activeActivities.value = [...activeActivities.value]
      }
    }, 1000)
    countdownIntervals[activity.id] = interval
  }
}

// 签到（代签）
const signForPerson = async (activityId, personnelId) => {
  try {
    const res = await attendanceActivityApi.sign(activityId, personnelId, 'admin')
    ElMessage.success(res.msg || '代签成功')
    await refreshActiveActivities()
    const person = personnel.value.find(p => p.id === personnelId)
    if (person) {
      addNotification('考勤代签', `已为 ${person.name} 完成签到`, 'personnel')
    }
  } catch (err) {
    ElMessage.error(err.message || '代签失败')
  }
}

// 手动结束考勤
const endActivity = async (activityId) => {
  const activity = activeActivities.value.find(a => a.id === activityId)
  if (!activity) return
  const activityName = activity.name || activity.title
  if (!confirm(`确定要结束"${activityName}"签到吗？结束后将无法继续签到。`)) return
  try {
    const res = await attendanceActivityApi.endActivity(activityId)
    ElMessage.success(res.msg || '考勤已结束')
    await refreshActiveActivities()
    // 如果当前正在查看人员详情，刷新该人员的考勤记录
    if (selectedPerson.value?.id) {
      const records = await personnelApi.getAttendanceRecords(selectedPerson.value.id)
      attendanceRecords.value = (records || []).sort((a, b) => {
        // 使用 sortTime 排序（包含完整日期时间）
        const timeA = a.sortTime || '9999-99-99 99:99:99'
        const timeB = b.sortTime || '9999-99-99 99:99:99'
        return timeB.localeCompare(timeA)
      })
    }
    addNotification('考勤结束', `考勤"${activityName}"已手动结束`, 'personnel')
  } catch (err) {
    ElMessage.error(err.message || '结束考勤失败')
  }
}
// 发布考勤（开始时间取当前实时时间）
const openPublishModal = () => {
  publishForm.value = {
    title: '',
    duration: 30,
    selectedIds: []
  }
  showPublishModal.value = true
}

const selectAllParticipants = () => {
  publishForm.value.selectedIds = personnel.value.filter(p => p.userId).map(p => p.id)
}
const deselectAllParticipants = () => {
  publishForm.value.selectedIds = []
}

const handlePublishActivity = async () => {
  // 过滤掉未关联系统用户的人员
  const validIds = publishForm.value.selectedIds.filter(id => {
    const p = personnel.value.find(item => item.id === id)
    return p && p.userId
  })
  if (validIds.length === 0) {
    alert('所选人员均未关联系统用户，无法发起签到。请先为人员关联系统用户账户。')
    return
  }
  const invalidCount = publishForm.value.selectedIds.length - validIds.length
  if (invalidCount > 0) {
    const ok = confirm(`${invalidCount} 名人员未关联系统用户，将自动跳过这些人员。是否继续？`)
    if (!ok) return
  }
  if (!publishForm.value.title.trim()) {
    alert('请输入考勤标题')
    return
  }
  // 获取当前实时时间作为开始时间
  const now = new Date()
  const startTime = now
  const endTime = new Date(startTime.getTime() + publishForm.value.duration * 60000)
  const currentUser = JSON.parse(localStorage.getItem('labUser') || '{}')
  const toLocalISOString = (date) => {
    const offset = date.getTimezoneOffset()
    const local = new Date(date.getTime() - offset * 60000)
    return local.toISOString().slice(0, 19)
  }
  const activityData = {
    name: publishForm.value.title,
    startTime: toLocalISOString(startTime),
    endTime: toLocalISOString(endTime),
    participantIds: validIds,
    status: 'active',
    createdBy: currentUser.id || null
  }
  try {
    const res = await attendanceActivityApi.create(activityData)
    ElMessage.success(res.msg || '发布成功')
    showPublishModal.value = false
    await refreshActiveActivities()
  } catch (err) {
    ElMessage.error(err.message || '发布失败')
  }
}

// 人员 CRUD
const loadPersonnel = async () => {
  loading.value = true
  try {
    const data = await personnelApi.getList()
    const list = data.content || []
    // 后端字段映射回前端字段：employeeNo -> code, position -> role
    personnel.value = list.map(item => ({
      ...item,
      code: item.employeeNo || item.code,
      role: item.position || item.role
    }))
  } catch (err) {
    alert(err.message)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  // 通过 departmentId 获取部门名称
  const selectedDept = formData.value.departmentId
    ? departments.value.find(d => String(d.departmentId) === String(formData.value.departmentId))
    : null
  const personnelData = {
    userId: formData.value.userId || null,
    employeeNo: formData.value.code,
    name: formData.value.name,
    gender: formData.value.gender || '未知',
    position: formData.value.role,
    department: selectedDept ? selectedDept.departmentName : '',
    departmentId: formData.value.departmentId || null,
    email: formData.value.email,
    phone: formData.value.phone,
    status: formData.value.status || 'active'
  }
  try {
    if (editing.value) {
      const res = await personnelApi.update(selectedPerson.value.id, personnelData)
      ElMessage.success(res.msg || '更新成功')
      addNotification('人员信息更新', `${selectedPerson.value.name} 的信息已更新`, 'personnel')
    } else {
      const res = await personnelApi.create(personnelData)
      ElMessage.success(res.msg || '新增成功')
      addNotification('人员新增', `新增人员 ${formData.value.name}，工号 ${formData.value.code}`, 'personnel')
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
    return
  }
  // 如果关联了系统用户，同步更新用户角色（允许清空所有角色）
  if (formData.value.userId && formData.value.roleIds != null) {
    try {
      const roleIds = formData.value.roleIds.map(Number)
      await http.put(`/sysUser/updateUserRole?userId=${formData.value.userId}&roleIds=${roleIds.join(',')}`)
    } catch (e) {
      console.error('更新用户角色失败', e)
    }
  }
  await loadPersonnel()
  // 如果详情弹窗正打开，刷新该人员的权限列表
  if (showDetailModal.value && selectedPerson.value && selectedPerson.value.userId) {
    try {
      const res = await http.get(`/sysUser/RoleAndAndAuthority?userId=${selectedPerson.value.userId}`)
      if (res && res.data.authority) {
        selectedPerson.value.permissions = res.data.authority
      }
    } catch (e) {
      console.error('刷新权限失败', e)
    }
  }
  showModal.value = false
}

const handleDelete = async (id) => {
  if (confirm('确定删除？')) {
    try {
      const p = personnel.value.find(p => p.id === id)
      const res = await personnelApi.delete(id)
      ElMessage.success(res.msg || '删除成功')
      await loadPersonnel()
      if (p) {
        addNotification('人员删除', `人员 ${p.name} 已从系统中移除`, 'personnel')
      }
    } catch (e) {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

// 快捷审批：通过认证
const formatUserDisplay = (p) => {
  if (!p) return ''
  const uid = p.userId || ''
  const uname = p.userName || ''
  return uid + (uname ? '-' + uname : '')
}

const approveCertification = async (p) => {
  const displayName = formatUserDisplay(p) || p.name
  if (!confirm(`确定通过 ${displayName} 的身份认证？`)) return
  try {
    const res = await personnelApi.update(p.id, { status: 'active' })
    ElMessage.success(res.msg || '认证通过')
    addNotification('认证通过', `${displayName} 的身份认证已通过`, 'personnel')
    await loadPersonnel()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

// 快捷审批：拒绝认证
const rejectCertification = async (p) => {
  const displayName = formatUserDisplay(p) || p.name
  if (!confirm(`确定拒绝 ${displayName} 的身份认证？该记录将被删除。`)) return
  try {
    // 先更新为 rejected 触发后端通知，再删除记录
    const res1 = await personnelApi.update(p.id, { status: 'rejected' })
    const res2 = await personnelApi.delete(p.id)
    ElMessage.success(res2.msg || res1.msg || '已拒绝认证')
    addNotification('认证拒绝', `${displayName} 的身份认证已被拒绝`, 'personnel')
    await loadPersonnel()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const openAddModal = async () => {
  editing.value = false
  formData.value = { code: '', name: '', gender: '', role: '', department: '', departmentId: null, email: '', phone: '', permissions: [], userId: null, roleIds: [], status: '' }
  // 确保部门列表已加载
  if (departments.value.length === 0) {
    await loadDepartments()
  }
  showModal.value = true
}

// ========== 部门管理函数 ==========

// 通过 departmentId 获取部门名称
const getDepartmentName = (p) => {
  if (p.departmentId && departments.value.length > 0) {
    const dept = departments.value.find(d => String(d.departmentId) === String(p.departmentId))
    if (dept) return dept.departmentName
  }
  return p.department || '-'
}

// 通过 leader_user_id 获取负责人姓名
const getLeaderName = (dept) => {
  if (!dept.leader_user_id && !dept.leaderUserId) return '-'
  const leaderId = dept.leader_user_id || dept.leaderUserId
  const person = personnel.value.find(p => String(p.userId) === String(leaderId))
  return person ? person.name : '-'
}

const loadDepartments = async () => {
  deptLoading.value = true
  try {
    const data = await departmentApi.getList()
    departments.value = Array.isArray(data) ? data : (data?.content || [])
  } catch (err) {
    console.error('加载部门失败', err)
  } finally {
    deptLoading.value = false
  }
}

const loadRoles = async () => {
  rolesLoading.value = true
  try {
    const data = await roleApi.getAllRoles()
    roles.value = Array.isArray(data) ? data : (data?.content || [])
  } catch (err) {
    console.error('加载角色失败', err)
  } finally {
    rolesLoading.value = false
  }
}

const openDeptAddModal = () => {
  deptEditing.value = false
  deptForm.value = { departmentName: '', parentId: null, leaderUserId: null }
  showDeptModal.value = true
}

const openDeptEditModal = (dept) => {
  deptEditing.value = true
  deptForm.value = { 
    departmentId: dept.departmentId,
    departmentName: dept.departmentName,
    parentId: dept.parentId,
    leaderUserId: dept.leaderUserId || dept.leader_user_id || null
  }
  showDeptModal.value = true
}

const handleDeptSubmit = async () => {
  try {
    if (deptEditing.value) {
      await departmentApi.update(deptForm.value.departmentId, deptForm.value)
      addNotification('部门更新', `部门 ${deptForm.value.departmentName} 已更新`, 'personnel')
    } else {
      await departmentApi.create(deptForm.value)
      addNotification('部门新增', `新增部门 ${deptForm.value.departmentName}`, 'personnel')
    }
    await loadDepartments()
    showDeptModal.value = false
  } catch (err) {
    alert('保存失败：' + err.message)
  }
}

const handleDeptDelete = async (id) => {
  if (confirm('确定删除该部门？')) {
    try {
      await departmentApi.delete(id)
      await loadDepartments()
      addNotification('部门删除', `部门已删除`, 'personnel')
    } catch (err) {
      alert('删除失败：' + err.message)
    }
  }
}

// 打开角色权限管理弹窗
const openRoleManageModal = async () => {
  showRoleManageModal.value = true
  selectedRoleId.value = null
  selectedAuthorityIds.value = []
  try {
    const [rolesRes, authRes] = await Promise.all([
      roleApi.getAllRoles(),
      roleApi.getAllAuthorities()
    ])
    roles.value = Array.isArray(rolesRes) ? rolesRes : (rolesRes?.content || [])
    authorities.value = Array.isArray(authRes) ? authRes : (authRes?.content || [])
  } catch (e) {
    console.error('加载角色或权限失败', e)
    alert('加载数据失败')
  }
}

// 打开指定角色的权限管理
const openRoleManageForRole = async (role) => {
  showRoleManageModal.value = true
  selectedRoleId.value = role.roleId || role.id
  selectedAuthorityIds.value = []
  try {
    const [rolesRes, authRes] = await Promise.all([
      roleApi.getAllRoles(),
      roleApi.getAllAuthorities()
    ])
    roles.value = Array.isArray(rolesRes) ? rolesRes : (rolesRes?.content || [])
    authorities.value = Array.isArray(authRes) ? authRes : (authRes?.content || [])
    // 加载该角色的权限
    await onRoleChange(selectedRoleId.value)
  } catch (e) {
    console.error('加载角色或权限失败', e)
  }
}

// 选择角色后加载其权限
const onRoleChange = async (roleId) => {
  if (!roleId) {
    selectedAuthorityIds.value = []
    return
  }
  try {
    const authIds = await roleApi.getRoleAuthorities(roleId)
    selectedAuthorityIds.value = Array.isArray(authIds) ? authIds.map(String) : []
  } catch (e) {
    console.error('加载角色权限失败', e)
    selectedAuthorityIds.value = []
  }
}

// 获取当前选中角色的名称
const getSelectedRoleName = () => {
  const role = roles.value.find(r => (r.roleId || r.id) === selectedRoleId.value)
  return role ? (role.roleName || role.name) : '角色权限管理'
}

// 保存角色权限
const saveRoleAuthority = async () => {
  if (!selectedRoleId.value) {
    alert('请先选择角色')
    return
  }
  try {
    const ids = selectedAuthorityIds.value.map(Number)
    await roleApi.updateRoleAuthority(selectedRoleId.value, ids)
    addNotification('角色权限更新', '角色权限已更新', 'personnel')
    showRoleManageModal.value = false
  } catch (e) {
    console.error('保存角色权限失败', e)
    alert('保存失败')
  }
}

// ========== 角色新增/删除 ==========
const openRoleAddModal = () => {
  roleEditing.value = false
  roleForm.value = { roleId: null, roleName: '', description: '' }
  showRoleModal.value = true
}

const handleRoleSubmit = async () => {
  try {
    await roleApi.createRole(roleForm.value)
    addNotification('角色新增', `新增角色 ${roleForm.value.roleName}`, 'personnel')
    showRoleModal.value = false
    await loadRoles()
  } catch (e) {
    console.error('新增角色失败', e)
    alert('新增角色失败：' + (e.message || e))
  }
}

const handleDeleteRole = async (roleId) => {
  if (!confirm('确定删除该角色？删除后已关联该角色的用户将失去对应权限。')) return
  try {
    await roleApi.deleteRole(roleId)
    addNotification('角色删除', '角色已删除', 'personnel')
    await loadRoles()
  } catch (e) {
    console.error('删除角色失败', e)
    alert('删除角色失败：' + (e.message || e))
  }
}

const openEditModal = async (p) => {
  editing.value = true
  selectedPerson.value = p
  formData.value = { ...p, roleIds: [], permissions: [] }
  // 确保部门列表已加载
  if (departments.value.length === 0) {
    await loadDepartments()
  }
  // 如果关联了系统用户，加载其角色
  if (p.userId) {
    try {
      const roleIdsRes = await http.get(`/sysUser/roles?userId=${p.userId}`)
      formData.value.roleIds = Array.isArray(roleIdsRes.data) ? roleIdsRes.data.map(String) : []
    } catch (e) {
      console.error('加载用户角色失败', e)
    }
  }
  showModal.value = true
}

const viewDetail = async (p) => {
  selectedPerson.value = { ...p, permissions: [] }
  const records = await personnelApi.getAttendanceRecords(p.id)
  attendanceRecords.value = (records || []).sort((a, b) => {
    // 使用 sortTime 排序（包含完整日期时间）
    const timeA = a.sortTime || '9999-99-99 99:99:99'
    const timeB = b.sortTime || '9999-99-99 99:99:99'
    return timeB.localeCompare(timeA)
  })
  // 从后端 API 读取该用户的培训考试记录
  try {
    const examRecords = await safetyApi.getUserExamRecords(p.userId || p.id)
    const list = Array.isArray(examRecords) ? examRecords : (examRecords?.content || [])
    trainingRecords.value = list.map(r => ({
      id: r.id,
      name: r.trainingName || '未知培训',
      score: r.score,
      totalScore: r.totalScore,
      date: r.examTime ? r.examTime.substring(0, 10) : '',
      passed: r.passed
    }))
  } catch (e) {
    console.error('加载培训记录失败:', e)
    trainingRecords.value = []
  }
  // 如果关联了系统用户，加载其权限
  if (p.userId) {
    try {
      const res = await http.get(`/sysUser/RoleAndAndAuthority?userId=${p.userId}`)
      if (res && res.data.authority) {
        selectedPerson.value.permissions = res.data.authority
      }
    } catch (e) {
      console.error('加载用户权限失败', e)
    }
  }
  showDetailModal.value = true
}

// 更新考勤状态
const statusValueMap = {
  '正常': 'normal',
  '迟到': 'late',
  '早退': 'early_leave',
  '缺勤': 'absent',
  '请假': 'leave'
}
const updateAttendanceStatus = async (record) => {
  try {
    const mappedStatus = statusValueMap[record.attendanceStatus] || record.attendanceStatus
    const res = await personnelApi.updateAttendanceRecord(record.id, { status: mappedStatus })
    ElMessage.success(res.msg || '考勤状态已更新')
    addNotification('考勤修改', `${selectedPerson.value.name} 的考勤状态已更新`, 'personnel')
  } catch (err) {
    ElMessage.error(err.message || '更新失败')
    await viewDetail(selectedPerson.value)
  }
}

// 生命周期
onMounted(async () => {
  await loadPersonnel()
  await refreshActiveActivities()
  // 加载系统用户列表
  try {
    const users = await userApi.getAllUsers()
    sysUsers.value = Array.isArray(users) ? users : (users?.content || [])
  } catch (e) {
    console.error('加载系统用户列表失败', e)
  }
  // 加载所有角色列表（用于人员编辑弹窗的角色多选）
  try {
    const rolesData = await roleApi.getAllRoles()
    allRoles.value = Array.isArray(rolesData) ? rolesData : (rolesData?.content || [])
  } catch (e) {
    console.error('加载角色列表失败', e)
  }
})

// 监听 tab 切换，加载对应数据
watch(activeTab, async (newTab) => {
  if (newTab === 'department') {
    await loadDepartments()
  } else if (newTab === 'permission') {
    await loadRoles()
  }
})

onUnmounted(() => {
  Object.values(countdownIntervals).forEach(clearInterval)
})
</script>
