<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <h1 class="text-2xl font-bold text-gray-900">安全管理</h1>
      <button v-if="hasPermission('safety', 'add')" @click="openAddModal" class="btn-primary"><Plus class="w-4 h-4" />新增{{ currentTabLabel }}</button>
    </div>

    <div class="flex gap-2 border-b border-gray-200">
      <button v-for="tab in tabs" :key="tab.key" @click="activeTab = tab.key" :class="['flex items-center gap-2 px-4 py-3 border-b-2 transition-colors', activeTab === tab.key ? 'border-primary-600 text-primary-600' : 'border-transparent text-gray-500 hover:text-gray-700']"><component :is="tab.icon" class="w-4 h-4" />{{ tab.label }}</button>
    </div>

    <div>
      <!-- 安全制度 -->
      <div v-if="activeTab === 'regulations'" class="space-y-4">
        <div v-for="reg in regulations" :key="reg.id" class="card">
          <div class="flex items-start justify-between">
            <div class="flex items-start gap-4">
              <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center"><BookOpen class="w-6 h-6 text-blue-600" /></div>
              <div>
                <h3 class="font-semibold text-gray-900">{{ reg.title }}</h3>
                <p class="text-sm text-primary-600 mb-2">{{ reg.category }}</p>
                <p class="text-sm text-gray-600 line-clamp-2">{{ reg.content }}</p>
                <p class="text-xs text-gray-400 mt-2">更新于 {{ reg.updateDate }}</p>
              </div>
            </div>
            <div class="flex gap-2">
              <button v-if="hasPermission('safety', 'set')" @click="openEditModal(reg)" class="p-2 hover:bg-gray-100 rounded-lg"><Edit2 class="w-4 h-4" /></button>
              <button v-if="hasPermission('safety', 'remove')" @click="handleDelete(reg.id, 'regulations')" class="p-2 hover:bg-red-50 rounded-lg text-red-600"><Trash2 class="w-4 h-4" /></button>
            </div>
          </div>
        </div>
      </div>

      <!-- 安全培训 -->
      <div v-if="activeTab === 'trainings'" class="space-y-4">
        <div v-for="t in trainings" :key="t.id" class="card">
          <div class="flex items-start justify-between">
            <div class="flex items-start gap-4">
              <div class="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center">
                <Shield class="w-6 h-6 text-purple-600" />
              </div>
              <div>
                <h3 class="font-semibold text-gray-900">{{ t.name }}</h3>
                <div class="flex gap-4 mt-1 text-sm text-gray-500">
                  <span>开始日期: {{ t.startDate }}</span>
                  <span>结束日期: {{ t.endDate }}</span>
                  <span>培训师: {{ getPersonnelByUserId(t.createdBy).name || '-' }}</span>
                  <span>限时: {{ t.timeLimit || 30 }}分钟</span>
                  <span>完成人数: {{ completedCounts[t.id] || 0 }}人</span>
                  <span>题目数: {{ getQuestionCount(t.id) }}题</span>
                </div>
              </div>
            </div>
            <div class="flex flex-col items-end gap-2">
              <span :class="t.status === 'completed' ? 'status-approved' : t.status === 'published' ? 'bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full text-xs font-medium' : 'status-pending'">{{ t.status === 'completed' ? '已完成' : t.status === 'published' ? '已发布' : '待完成' }}</span>
              <div class="flex gap-2">
                <button v-if="hasPermission('safety', 'set')" @click="openQuestionModal(t)" class="p-2 hover:bg-blue-50 rounded-lg text-blue-600" title="管理题目"><FileQuestion class="w-4 h-4" /></button>
                <button v-if="t.status === 'upcoming'" @click="startExam(t)" class="p-2 hover:bg-purple-50 rounded-lg text-purple-600" title="开始做题"><Play class="w-4 h-4" /></button>
                <button v-if="hasPermission('safety', 'set') && t.status === 'upcoming'" @click="completeTraining(t)" class="p-2 hover:bg-green-50 rounded-lg text-green-600" title="标记完成"><CheckCircle class="w-4 h-4" /></button>
                <button v-if="hasPermission('safety', 'set')" @click="openEditModal(t)" class="p-2 hover:bg-gray-100 rounded-lg"><Edit2 class="w-4 h-4" /></button>
                <button v-if="hasPermission('safety', 'remove')" @click="handleDelete(t.id, 'trainings')" class="p-2 hover:bg-red-50 rounded-lg text-red-600"><Trash2 class="w-4 h-4" /></button>
              </div>
            </div>
          </div>
        </div>
        <div v-if="trainings.length === 0" class="text-center py-8 text-gray-400">暂无培训数据</div>
      </div>

      <!-- 安全检查 -->
      <div v-if="activeTab === 'inspections'" class="space-y-4">
        <div v-for="ins in inspections" :key="ins.id" class="card">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-4">
              <div :class="['w-12 h-12 rounded-xl flex items-center justify-center', ins.status === 'passed' ? 'bg-green-100' : 'bg-yellow-100']">
                <CheckCircle v-if="ins.status === 'passed'" class="w-6 h-6 text-green-600" />
                <AlertTriangle v-else class="w-6 h-6 text-yellow-600" />
              </div>
              <div>
                <h3 class="font-semibold text-gray-900">安全检查 - {{ ins.date }}</h3>
                <p class="text-sm text-gray-500">检查员: {{ getPersonnelByUserId(ins.inspectorId).name || '-' }}</p>
                <div class="flex gap-4 mt-2 text-sm">
                  <span>检查项: {{ ins.items }}</span>
                  <span :class="ins.issues > 0 ? 'text-red-600' : 'text-green-600'">问题项: {{ ins.issues }}</span>
                </div>
              </div>
            </div>
            <div class="flex flex-col items-end gap-2">
              <span :class="ins.status === 'passed' ? 'status-approved' : 'status-pending'">{{ ins.status === 'passed' ? '通过' : '待整改' }}</span>
              <div class="flex gap-2">
                <button v-if="hasPermission('safety', 'set') && ins.status === 'issues'" @click="resolveInspection(ins.id)" class="text-sm text-green-600 hover:text-green-700">✓ 整改完成</button>
                <button v-if="hasPermission('safety', 'set')" @click="openEditModal(ins)" class="p-2 hover:bg-gray-100 rounded-lg"><Edit2 class="w-4 h-4" /></button>
                <button v-if="hasPermission('safety', 'remove')" @click="handleDelete(ins.id, 'inspections')" class="p-2 hover:bg-red-50 rounded-lg text-red-600"><Trash2 class="w-4 h-4" /></button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 事故处理 -->
      <div v-if="activeTab === 'incidents'" class="space-y-4">
        <div v-for="inc in incidents" :key="inc.id" class="card border-l-4 border-l-red-500">
          <div class="flex items-start justify-between">
            <div>
              <div class="flex items-center gap-2 mb-2">
                <h3 class="font-semibold text-gray-900">{{ inc.type }}</h3>
                <span :class="['status-badge', getSeverityClass(inc.severity)]">{{ getSeverityLabel(inc.severity) }}</span>
              </div>
              <p class="text-sm text-gray-600 mb-2">{{ inc.description }}</p>
              <div class="flex gap-4 text-sm text-gray-500">
                <span>发生时间: {{ inc.date }}</span>
                <span>地点: {{ inc.location || '-' }}</span>
                <span>处理人: {{ getPersonnelByUserId(inc.reporterId).name || '-' }}</span>
              </div>
            </div>
            <div class="flex flex-col items-end gap-2">
              <span :class="inc.status === 'resolved' ? 'status-approved' : 'status-rejected'">{{ inc.status === 'resolved' ? '已解决' : '处理中' }}</span>
              <div class="flex gap-2">
                <button v-if="hasPermission('safety', 'set') && inc.status !== 'resolved'" @click="resolveIncident(inc.id)" class="text-sm text-green-600 hover:text-green-700">✓ 标记解决</button>
                <button v-if="hasPermission('safety', 'set')" @click="openEditModal(inc)" class="p-2 hover:bg-gray-100 rounded-lg"><Edit2 class="w-4 h-4" /></button>
                <button v-if="hasPermission('safety', 'remove')" @click="handleDelete(inc.id, 'incidents')" class="p-2 hover:bg-red-50 rounded-lg text-red-600"><Trash2 class="w-4 h-4" /></button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗（统一复用） -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b flex justify-between">
          <h2 class="text-xl font-bold">{{ editing ? '编辑' : '新增' }}{{ currentTabLabel }}</h2>
          <button @click="showModal=false"><X class="w-5 h-5" /></button>
        </div>
        <form @submit.prevent="handleSave" class="p-6 space-y-4">
          <!-- 安全制度表单 -->
          <template v-if="activeTab === 'regulations'">
            <div><label>制度标题</label><input v-model="editForm.title" required class="input-field" /></div>
            <div><label>分类</label><input v-model="editForm.category" required class="input-field" /></div>
            <div><label>内容</label><textarea v-model="editForm.content" required class="input-field min-h-[120px]"></textarea></div>
          </template>
          <!-- 安全培训表单 -->
          <template v-if="activeTab === 'trainings'">
            <div><label>培训名称</label><input v-model="editForm.name" required class="input-field" /></div>
            <div class="grid grid-cols-2 gap-4">
              <div><label>开始日期</label><input type="date" v-model="editForm.startDate" required class="input-field" /></div>
              <div><label>结束日期</label><input type="date" v-model="editForm.endDate" required class="input-field" /></div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div><label>培训师</label>
              <select v-model="editForm.createdBy" class="input-field">
                <option :value="null">请选择培训师</option>
                <option v-for="p in personnelList" :key="p.id || p.userId" :value="p.userId || p.user_id">{{ p.name }}（{{ p.employeeNo || p.employee_no || '' }}）</option>
              </select>
            </div>
              <div><label>答题限时（分钟）</label><input type="number" v-model.number="editForm.timeLimit" required class="input-field" min="1" placeholder="如：30" /></div>
            </div>
          </template>
          <!-- 安全检查表单 -->
          <template v-if="activeTab === 'inspections'">
            <div><label>检查日期</label><input type="date" v-model="editForm.date" required class="input-field" /></div>
            <div><label>检查员 <span class="text-red-500">*</span></label>
              <select v-model="editForm.inspectorId" required class="input-field" :class="{ 'border-red-400': !editForm.inspectorId }">
                <option :value="null" disabled>请选择检查员</option>
                <option v-for="p in personnelList.filter(p => p.userId || p.user_id)" :key="p.id" :value="p.userId || p.user_id">{{ p.name }}（{{ p.employeeNo || p.employee_no || '' }}）</option>
              </select>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div><label>检查项数</label><input type="number" v-model.number="editForm.items" required class="input-field" /></div>
              <div><label>问题项数</label><input type="number" v-model.number="editForm.issues" required class="input-field" /></div>
            </div>
          </template>
          <!-- 事故处理表单 -->
          <template v-if="activeTab === 'incidents'">
            <div><label>事故类型</label><input v-model="editForm.type" required class="input-field" /></div>
            <div><label>发生日期</label><input type="date" v-model="editForm.date" required class="input-field" /></div>
            <div><label>发生地点</label><input v-model="editForm.location" required class="input-field" /></div>
            <div><label>严重程度</label><select v-model="editForm.severity" class="input-field"><option value="minor">轻微</option><option value="major">严重</option><option value="critical">重大</option></select></div>
            <div><label>事故描述</label><textarea v-model="editForm.description" required class="input-field"></textarea></div>
            <div><label>处理人</label>
              <select v-model="editForm.reporterId" class="input-field">
                <option :value="null">请选择处理人</option>
                <option v-for="p in personnelList" :key="p.id || p.userId" :value="p.userId || p.user_id">{{ p.name }}（{{ p.employeeNo || p.employee_no || '' }}）</option>
              </select>
            </div>
          </template>
          <div class="flex gap-3 pt-4">
            <button type="button" @click="showModal=false" class="flex-1 btn-secondary">取消</button>
            <button type="submit" class="flex-1 btn-primary">保存</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 培训完成弹窗 -->
    <div v-if="showCompleteModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-md">
        <div class="p-6 border-b flex justify-between">
          <h2 class="text-xl font-bold">完成培训</h2>
          <button @click="showCompleteModal=false"><X class="w-5 h-5" /></button>
        </div>
        <form @submit.prevent="confirmComplete" class="p-6 space-y-4">
          <div><label>培训名称</label><input :value="selectedTraining?.name" disabled class="input-field bg-gray-50" /></div>
          <div><label>实际参与人数</label><input type="number" v-model="completeForm.participants" required class="input-field" min="1" /></div>
          <div class="flex gap-3">
            <button type="button" @click="showCompleteModal=false" class="flex-1 btn-secondary">取消</button>
            <button type="submit" class="flex-1 btn-primary">确认完成</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 题目管理弹窗 -->
    <div v-if="showQuestionModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-3xl max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b flex justify-between items-center">
          <h2 class="text-xl font-bold">管理题目 - {{ selectedTraining?.name }}</h2>
          <button @click="showQuestionModal=false"><X class="w-5 h-5" /></button>
        </div>
        <div class="p-6 space-y-4">
          <!-- 添加新题目表单 -->
          <div class="bg-gray-50 p-4 rounded-xl space-y-3">
            <h3 class="font-semibold text-gray-700">添加新题目</h3>
            <div><label>题目类型</label>
              <select v-model="newQuestion.type" @change="onQuestionTypeChange" class="input-field">
                <option value="single">单选题</option>
                <option value="multi">多选题</option>
                <option value="judge">判断题</option>
              </select>
            </div>
            <div><label>题目内容</label><textarea v-model="newQuestion.content" required class="input-field" rows="2" placeholder="请输入题目内容"></textarea></div>
            
            <!-- 选项区域：根据题型动态显示 -->
            <div v-if="newQuestion.type === 'judge'" class="grid grid-cols-2 gap-3">
              <div><label>选项 A（正确）</label><input v-model="newQuestion.options[0]" required class="input-field" placeholder="正确" /></div>
              <div><label>选项 B（错误）</label><input v-model="newQuestion.options[1]" required class="input-field" placeholder="错误" /></div>
            </div>
            <div v-else class="grid grid-cols-2 gap-3">
              <div><label>选项 A</label><input v-model="newQuestion.options[0]" required class="input-field" placeholder="选项A" /></div>
              <div><label>选项 B</label><input v-model="newQuestion.options[1]" required class="input-field" placeholder="选项B" /></div>
              <div><label>选项 C</label><input v-model="newQuestion.options[2]" required class="input-field" placeholder="选项C" /></div>
              <div><label>选项 D</label><input v-model="newQuestion.options[3]" required class="input-field" placeholder="选项D" /></div>
            </div>
            
            <!-- 答案区域：根据题型动态显示 -->
            <div v-if="newQuestion.type === 'multi'">
              <label>正确答案（多选）</label>
              <div class="flex gap-4 mt-2">
                <label class="flex items-center gap-2">
                  <input type="checkbox" v-model="newQuestion.multiAnswers" value="0" class="rounded border-gray-300" />
                  <span>选项 A</span>
                </label>
                <label class="flex items-center gap-2">
                  <input type="checkbox" v-model="newQuestion.multiAnswers" value="1" class="rounded border-gray-300" />
                  <span>选项 B</span>
                </label>
                <label class="flex items-center gap-2">
                  <input type="checkbox" v-model="newQuestion.multiAnswers" value="2" class="rounded border-gray-300" />
                  <span>选项 C</span>
                </label>
                <label class="flex items-center gap-2">
                  <input type="checkbox" v-model="newQuestion.multiAnswers" value="3" class="rounded border-gray-300" />
                  <span>选项 D</span>
                </label>
              </div>
            </div>
            <div v-else><label>正确答案</label>
              <select v-model="newQuestion.correctAnswer" class="input-field">
                <option v-if="newQuestion.type === 'judge'" value="0">正确（A）</option>
                <option v-if="newQuestion.type === 'judge'" value="1">错误（B）</option>
                <option v-if="newQuestion.type !== 'judge'" value="0">选项 A</option>
                <option v-if="newQuestion.type !== 'judge'" value="1">选项 B</option>
                <option v-if="newQuestion.type !== 'judge'" value="2">选项 C</option>
                <option v-if="newQuestion.type !== 'judge'" value="3">选项 D</option>
              </select>
            </div>
            <button @click="addQuestion" class="btn-primary w-full"><Plus class="w-4 h-4" />添加题目</button>
          </div>
          <!-- 题目列表 -->
          <div class="space-y-3">
            <h3 class="font-semibold text-gray-700">已有题目 ({{ getQuestionsByTraining(selectedTraining?.id).length }})</h3>
            <div v-for="(q, idx) in getQuestionsByTraining(selectedTraining?.id)" :key="q.id" class="border rounded-lg p-4">
              <div class="flex justify-between items-start">
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-1">
                    <p class="font-medium text-gray-900">{{ idx + 1 }}. {{ q.content }}</p>
                    <span class="px-2 py-0.5 rounded text-xs font-medium"
                      :class="{ 'bg-blue-100 text-blue-700': q.type === 'single', 'bg-purple-100 text-purple-700': q.type === 'multi', 'bg-orange-100 text-orange-700': q.type === 'judge' }">
                      {{ q.type === 'single' ? '单选' : q.type === 'multi' ? '多选' : q.type === 'judge' ? '判断' : '单选' }}
                    </span>
                  </div>
                  <div class="grid grid-cols-2 gap-2 mt-2 text-sm">
                    <span v-for="(opt, optIdx) in q.options" :key="optIdx" 
                      @click="updateQuestionAnswer(q, optIdx)"
                      :class="['px-2 py-1 rounded cursor-pointer transition-colors', 
                        q.type === 'multi' 
                          ? (q.correctAnswer || '').split(',').includes(String(optIdx)) ? 'bg-green-100 text-green-700 border-2 border-green-300' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                          : parseInt(q.correctAnswer) === optIdx ? 'bg-green-100 text-green-700 border-2 border-green-300' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                      ]">
                      {{ ['A', 'B', 'C', 'D'][optIdx] }}. {{ opt }}
                    </span>
                  </div>
                </div>
                <button @click="deleteQuestion(q.id)" class="p-2 hover:bg-red-50 rounded-lg text-red-600 ml-2"><Trash2 class="w-4 h-4" /></button>
              </div>
            </div>
            <div v-if="getQuestionsByTraining(selectedTraining?.id).length === 0" class="text-center py-4 text-gray-400">暂无题目</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 做题弹窗 -->
    <div v-if="showExamModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b flex justify-between items-center">
          <h2 class="text-xl font-bold">安全培训测试 - {{ selectedTraining?.name }}</h2>
          <button @click="closeExamModal"><X class="w-5 h-5" /></button>
        </div>
        <div class="p-6 space-y-6">
          <!-- 做题模式 -->
          <div v-if="!examCompleted" class="space-y-4">
            <!-- 倒计时显示 -->
            <div class="flex justify-between items-center">
              <div class="flex items-center gap-2">
                <Clock class="w-5 h-5" :class="remainingTime < 60 ? 'text-red-600 animate-pulse' : 'text-primary-600'" />
                <span class="text-lg font-mono font-bold" :class="remainingTime < 60 ? 'text-red-600' : 'text-gray-700'">
                  {{ formatTime(remainingTime) }}
                </span>
                <span class="text-sm text-gray-500">剩余时间</span>
              </div>
              <span class="text-sm text-gray-500">限时 {{ selectedTraining?.timeLimit || 30 }} 分钟</span>
            </div>
            <div class="flex justify-between items-center text-sm text-gray-500">
              <span>题目 {{ currentQuestionIndex + 1 }} / {{ examQuestions.length }}</span>
              <span>进度: {{ Math.round((currentQuestionIndex / examQuestions.length) * 100) }}%</span>
            </div>
            <div class="w-full bg-gray-200 rounded-full h-2">
              <div class="bg-primary-600 h-2 rounded-full transition-all" :style="{ width: ((currentQuestionIndex + 1) / examQuestions.length * 100) + '%' }"></div>
            </div>
            <div v-if="examQuestions[currentQuestionIndex]" class="space-y-4">
              <p class="text-lg font-medium text-gray-900">{{ currentQuestionIndex + 1 }}. {{ examQuestions[currentQuestionIndex].content }}</p>
              <div class="space-y-2">
                <label v-for="(opt, optIdx) in examQuestions[currentQuestionIndex].options" :key="optIdx"
                       class="flex items-center gap-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50 transition-colors"
                       :class="{ 'border-primary-500 bg-primary-50': examAnswers[currentQuestionIndex] === optIdx.toString() }">
                  <input type="radio" :name="'q' + currentQuestionIndex" :value="optIdx.toString()" v-model="examAnswers[currentQuestionIndex]" class="w-4 h-4 text-primary-600" />
                  <span>{{ ['A', 'B', 'C', 'D'][optIdx] }}. {{ opt }}</span>
                </label>
              </div>
            </div>
            <div class="flex justify-between pt-4">
              <button @click="currentQuestionIndex--" :disabled="currentQuestionIndex === 0" class="btn-secondary" :class="{ 'opacity-50 cursor-not-allowed': currentQuestionIndex === 0 }">上一题</button>
              <button v-if="currentQuestionIndex < examQuestions.length - 1" @click="nextQuestion" class="btn-primary">下一题</button>
              <button v-else @click="submitExam" class="btn-primary">提交答案</button>
            </div>
          </div>
          <!-- 结果展示 -->
          <div v-else class="text-center space-y-4">
            <div class="w-20 h-20 mx-auto rounded-full flex items-center justify-center" :class="examScore >= 60 ? 'bg-green-100' : 'bg-red-100'">
              <Trophy v-if="examScore >= 60" class="w-10 h-10 text-green-600" />
              <XCircle v-else class="w-10 h-10 text-red-600" />
            </div>
            <h3 class="text-2xl font-bold" :class="examScore >= 60 ? 'text-green-600' : 'text-red-600'">
              {{ examScore >= 60 ? '测试通过!' : '测试未通过' }}
            </h3>
            <p class="text-gray-600">您的得分: <span class="text-3xl font-bold">{{ examScore }}</span> / 100</p>
            <p class="text-sm text-gray-500">答对 {{ correctCount }} 题，共 {{ examQuestions.length }} 题</p>
            <div class="space-y-3 text-left mt-6">
              <h4 class="font-semibold text-gray-700">答题详情:</h4>
              <div v-for="(q, idx) in examQuestions" :key="q.id" class="border rounded-lg p-3" :class="examAnswers[idx] === q.correctAnswer ? 'border-green-200 bg-green-50' : 'border-red-200 bg-red-50'">
                <p class="font-medium">{{ idx + 1 }}. {{ q.content }}</p>
                <p class="text-sm mt-1">
                  <span :class="examAnswers[idx] === q.correctAnswer ? 'text-green-600' : 'text-red-600'">
                    您的答案: {{ examAnswers[idx] !== undefined ? ['A', 'B', 'C', 'D'][examAnswers[idx]] : '未作答' }}
                  </span>
                  <span v-if="examAnswers[idx] !== q.correctAnswer" class="text-green-600 ml-4">
                    正确答案: {{ ['A', 'B', 'C', 'D'][q.correctAnswer] }}
                  </span>
                </p>
              </div>
            </div>
            <button @click="closeExamModal" class="btn-primary w-full mt-4">关闭</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, BookOpen, Shield, CheckCircle, AlertTriangle, X, Edit2, Trash2, FileQuestion, Play, Trophy, XCircle, Clock } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { hasPermission } from '@/utils/permission'
import { addNotification } from '@/utils/notification'
import { safetyApi, commonApi } from '@/services/api'

const activeTab = ref('regulations')
const regulations = ref([])
const trainings = ref([])
const inspections = ref([])
const incidents = ref([])
const questions = ref([])
const completedCounts = ref({}) // 各培训完成人数统计
const personnelList = ref([]) // 人员列表，用于获取处理人姓名

const showModal = ref(false)
const editing = ref(false)
const editForm = ref({})
const currentEditId = ref(null)

const showCompleteModal = ref(false)
const selectedTraining = ref(null)
const completeForm = ref({ participants: 0 })

// 题目管理相关
const showQuestionModal = ref(false)
const newQuestion = ref({
  content: '',
  options: ['', '', '', ''],
  correctAnswer: '0',
  type: 'single',
  multiAnswers: []
})

// 做题相关
const showExamModal = ref(false)
const examQuestions = ref([])
const examAnswers = ref([])
const currentQuestionIndex = ref(0)
const examCompleted = ref(false)
const examScore = ref(0)
const correctCount = ref(0)

// 倒计时相关
const remainingTime = ref(0)
const countdownTimer = ref(null)

const tabs = [
  { key: 'regulations', label: '安全制度', icon: BookOpen },
  { key: 'trainings', label: '安全培训', icon: Shield },
  { key: 'inspections', label: '安全检查', icon: CheckCircle },
  { key: 'incidents', label: '事故处理', icon: AlertTriangle }
]

const currentTabLabel = computed(() => tabs.find(t => t.key === activeTab.value)?.label.slice(0, -2) || '')

const loadData = async () => {
  try {
    const [regRes, trainRes, insRes, incRes, qRes, statsRes] = await Promise.all([
      safetyApi.getRegulations(),
      safetyApi.getTrainings(),
      safetyApi.getInspections(),
      safetyApi.getIncidents(),
      safetyApi.getQuestions(),
      safetyApi.getExamStats().catch(() => ({})) // 非阻塞，失败返回空
    ])
    completedCounts.value = (statsRes && typeof statsRes === 'object' && !Array.isArray(statsRes)) ? statsRes : {}
    regulations.value = (regRes.content || []).map(r => ({
      ...r,
      category: r.type || '',
    }))
    trainings.value = (trainRes.content || []).map(t => ({
      id: t.id,
      name: t.title || '',
      startDate: t.trainingTime ? t.trainingTime.substring(0, 10) : '',
      endDate: t.location || '',
      createdBy: t.createdBy || null,
      timeLimit: t.content ? (parseInt(t.content.match(/答题限时:\s*(\d+)/)?.[1]) || 30) : 30,
      participants: 0,
      status: 'upcoming', // 默认待完成，后面根据考试记录覆盖
      createTime: t.createTime
    }))
    // 加载当前用户的考试记录，设置培训完成状态
    const labUser = JSON.parse(localStorage.getItem('labUser') || '{}')
    const currentUserId = labUser.id || labUser.userId || null
    try {
      const passedIds = await safetyApi.getMyExamRecords()
      const passedSet = new Set(Array.isArray(passedIds) ? passedIds : (passedIds?.content || []))
      trainings.value.forEach(t => {
        if (passedSet.has(t.id)) {
          t.status = 'completed'
        } else if (currentUserId && t.createdBy && Number(t.createdBy) === Number(currentUserId)) {
          // 创建者不需要考自己创建的培训，标记为已发布
          t.status = 'published'
        }
      })
    } catch (e) {
      console.warn('加载考试记录失败:', e)
    }
    // 安全检查字段映射：后端 -> 前端
    inspections.value = (insRes.content || []).map(ins => ({
      id: ins.id,
      date: ins.inspectionTime ? ins.inspectionTime.substring(0, 10) : '',
      inspectorId: ins.inspectorId || null,
      items: ins.content ? (ins.content.match(/检查项:\s*(\d+)/)?.[1] || 0) : 0,
      issues: ins.content ? (ins.content.match(/问题项:\s*(\d+)/)?.[1] || 0) : 0,
      status: ins.result || 'passed',
      title: ins.title,
      content: ins.content,
      createTime: ins.createTime
    }))
    incidents.value = (incRes.content || []).map(inc => {
      const desc = inc.description || ''
      return {
        id: inc.id,
        type: inc.title || '',
        date: inc.incidentTime ? inc.incidentTime.substring(0, 10) : '',
        location: inc.location || '',
        severity: inc.level || '一般',
        description: desc.split('\n')[0] || '',
        reporterId: inc.reporterId || null,
        status: inc.status || 'processing',
        createTime: inc.createTime
      }
    })
    // 后端字段映射：question->content, answer->correctAnswer, optionA/B/C/D->options数组
    questions.value = (qRes.content || []).map(q => ({
      ...q,
      content: q.question || q.content || '',
      correctAnswer: q.answer || q.correctAnswer || '',
      options: q.optionA ? [q.optionA, q.optionB, q.optionC, q.optionD].filter(Boolean) : (typeof q.options === 'string' ? JSON.parse(q.options) : q.options),
      type: q.type || 'single'
    }))
  } catch (e) {
    console.error('加载安全管理数据失败:', e)
  }

  // 人员列表通过安全管理模块专用接口加载，不依赖人员管理权限
  try {
    const perRes = await commonApi.getSimpleUserList()
    personnelList.value = perRes.content || []
  } catch (e) {
    console.warn('加载人员列表失败，人员姓名将显示为 -')
  }
}

// 根据 userId 查找人员姓名
const getPersonnelByUserId = (userId) => {
  if (!userId) return { name: '', employeeNo: '' }
  const personnel = personnelList.value.find(p => p.userId === userId || p.user_id === userId)
  if (personnel) {
    return {
      name: personnel.name || '',
      employeeNo: personnel.employeeNo || personnel.employee_no || ''
    }
  }
  return { name: '', employeeNo: '' }
}

// 获取指定培训的题目数量
const getQuestionCount = (trainingId) => {
  if (!trainingId) return 0
  const filtered = questions.value.filter(q => q.trainingId === trainingId || Number(q.trainingId) === Number(trainingId))
  // 如果该培训没有专属题目，但有未关联培训的通用题目，则显示通用题目数量
  if (filtered.length === 0) {
    const unassigned = questions.value.filter(q => !q.trainingId)
    return unassigned.length
  }
  return filtered.length
}

// 获取指定培训的题目列表
const getQuestionsByTraining = (trainingId) => {
  if (!trainingId) return []
  const filtered = questions.value.filter(q => q.trainingId === trainingId || Number(q.trainingId) === Number(trainingId))
  // 如果该培训没有专属题目，但有未关联培训的通用题目，则使用通用题目
  if (filtered.length === 0) {
    const unassigned = questions.value.filter(q => !q.trainingId)
    return unassigned
  }
  return filtered
}

// 打开题目管理弹窗
const openQuestionModal = (training) => {
  selectedTraining.value = training
  newQuestion.value = { content: '', options: ['', '', '', ''], correctAnswer: '0', type: 'single', multiAnswers: [] }
  showQuestionModal.value = true
}

// 题型切换时重置选项和答案
const onQuestionTypeChange = () => {
  if (newQuestion.value.type === 'judge') {
    // 判断题只需要两个选项
    newQuestion.value.options = ['正确', '错误', '', '']
    newQuestion.value.correctAnswer = '0'
    newQuestion.value.multiAnswers = []
  } else if (newQuestion.value.type === 'multi') {
    // 多选题需要4个选项，答案改为数组
    newQuestion.value.options = ['', '', '', '']
    newQuestion.value.multiAnswers = []
    newQuestion.value.correctAnswer = ''
  } else {
    // 单选题
    newQuestion.value.options = ['', '', '', '']
    newQuestion.value.correctAnswer = '0'
    newQuestion.value.multiAnswers = []
  }
}

// 添加题目
const addQuestion = async () => {
  if (!newQuestion.value.content.trim()) {
    alert('请输入题目内容')
    return
  }
  
  // 根据题型验证选项
  if (newQuestion.value.type === 'judge') {
    if (!newQuestion.value.options[0]?.trim() || !newQuestion.value.options[1]?.trim()) {
      alert('请填写正确和错误两个选项')
      return
    }
  } else {
    if (newQuestion.value.options.slice(0, 4).some(opt => !opt?.trim())) {
      alert('请填写所有选项')
      return
    }
  }
  
  // 多选题验证答案
  if (newQuestion.value.type === 'multi' && newQuestion.value.multiAnswers.length === 0) {
    alert('请选择至少一个正确答案')
    return
  }

  try {
    // 处理答案格式
    let answerValue = newQuestion.value.correctAnswer
    if (newQuestion.value.type === 'multi') {
      // 多选题答案用逗号分隔，如 "0,1,2"
      answerValue = newQuestion.value.multiAnswers.sort().join(',')
    }
    
    const data = {
      trainingId: selectedTraining.value.id,
      question: newQuestion.value.content,
      optionA: newQuestion.value.options[0] || '',
      optionB: newQuestion.value.options[1] || '',
      optionC: newQuestion.value.type === 'judge' ? '' : (newQuestion.value.options[2] || ''),
      optionD: newQuestion.value.type === 'judge' ? '' : (newQuestion.value.options[3] || ''),
      answer: answerValue,
      type: newQuestion.value.type || 'single'
    }
    const res = await safetyApi.createQuestion(data)
    ElMessage.success(res.msg || '添加成功')
    // 重新加载题目列表（字段映射）
    const qRes = await safetyApi.getQuestions()
    questions.value = (qRes.content || []).map(q => ({
      ...q,
      content: q.question || q.content || '',
      correctAnswer: q.answer || q.correctAnswer || '',
      options: q.optionA ? [q.optionA, q.optionB, q.optionC, q.optionD].filter(Boolean) : (typeof q.options === 'string' ? JSON.parse(q.options) : q.options),
      type: q.type || 'single'
    }))

    // 重置表单
    newQuestion.value = { content: '', options: ['', '', '', ''], correctAnswer: '0', type: 'single', multiAnswers: [] }
  } catch (e) {
    console.error('添加题目失败:', e)
    ElMessage.error(e.message)
  }
}

// 更新题目答案（点击选项直接修改）
const updateQuestionAnswer = async (question, optionIdx) => {
  try {
    let newAnswer = ''
    if (question.type === 'multi') {
      // 多选题：切换选中状态
      const currentAnswers = (question.correctAnswer || '').split(',').filter(a => a !== '')
      const idx = currentAnswers.indexOf(String(optionIdx))
      if (idx > -1) {
        currentAnswers.splice(idx, 1)
      } else {
        currentAnswers.push(String(optionIdx))
      }
      newAnswer = currentAnswers.sort().join(',')
    } else {
      // 单选题/判断题：直接设置
      newAnswer = String(optionIdx)
    }
    
    // 更新后端 - 只传递后端需要的字段
    const res = await safetyApi.updateQuestion(question.id, {
      id: question.id,
      question: question.question || question.content,
      optionA: question.optionA || (question.options ? question.options[0] : ''),
      optionB: question.optionB || (question.options ? question.options[1] : ''),
      optionC: question.optionC || (question.options ? question.options[2] : ''),
      optionD: question.optionD || (question.options ? question.options[3] : ''),
      answer: newAnswer,
      type: question.type || 'single'
    })
    ElMessage.success(res.msg || '更新成功')
    
    // 更新本地数据
    question.correctAnswer = newAnswer
    question.answer = newAnswer
    
  } catch (e) {
    console.error('更新答案失败:', e)
    ElMessage.error(e.message || '更新答案失败')
  }
}

// 删除题目
const deleteQuestion = async (questionId) => {
  if (!confirm('确定删除这道题目吗？')) return
  const question = questions.value.find(q => q.id === questionId)
  try {
    const res = await safetyApi.deleteQuestion(questionId)
    ElMessage.success(res.msg || '删除成功')
    questions.value = questions.value.filter(q => q.id !== questionId)
    if (question) {
    }
  } catch (e) {
    console.error('删除题目失败:', e)
    ElMessage.error(e.message)
  }
}

// 开始做题
const startExam = (training) => {
  const trainingQuestions = getQuestionsByTraining(training.id)
  if (trainingQuestions.length === 0) {
    alert('该培训暂无题目，请联系管理员添加题目')
    return
  }

  selectedTraining.value = training
  examQuestions.value = trainingQuestions
  examAnswers.value = new Array(trainingQuestions.length).fill(undefined)
  currentQuestionIndex.value = 0
  examCompleted.value = false
  examScore.value = 0
  correctCount.value = 0

  // 初始化倒计时（分钟转秒）
  remainingTime.value = (training.timeLimit || 30) * 60
  startCountdown()

  showExamModal.value = true
}

// 启动倒计时
const startCountdown = () => {
  // 清除之前的定时器
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
  countdownTimer.value = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      // 时间到，自动提交
      clearInterval(countdownTimer.value)
      submitExam()
    }
  }, 1000)
}

// 格式化时间显示（秒 -> MM:SS）
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 下一题
const nextQuestion = () => {
  if (currentQuestionIndex.value < examQuestions.value.length - 1) {
    currentQuestionIndex.value++
  }
}

// 关闭做题弹窗
const closeExamModal = () => {
  // 清除倒计时定时器
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
  showExamModal.value = false
}

// 提交答案
const submitExam = async () => {
  // 检查是否所有题目都已作答
  const unanswered = examAnswers.value.findIndex(a => a === undefined)
  if (unanswered !== -1) {
    if (!confirm(`还有 ${examAnswers.value.filter(a => a === undefined).length} 道题未作答，确定要提交吗？`)) {
      currentQuestionIndex.value = unanswered
      return
    }
  }

  // 计算得分
  let correct = 0
  examQuestions.value.forEach((q, idx) => {
    if (examAnswers.value[idx] === q.correctAnswer) {
      correct++
    }
  })

  correctCount.value = correct
  examScore.value = Math.round((correct / examQuestions.value.length) * 100)
  examCompleted.value = true

  // 调用后端 API 提交考试分数（后端从 token 获取 userId，不需要前端传）
  const labUser = JSON.parse(localStorage.getItem('labUser') || '{}')
  try {
    const res = await safetyApi.submitExam({
      trainingId: selectedTraining.value.id,
      score: examScore.value,
      totalScore: 100,
      passed: examScore.value >= 60
    })
  } catch (e) {
    console.error('提交考试分数失败:', e)
  }

  // 记录活动

  // 如果通过测试，可以自动标记培训完成
  if (examScore.value >= 60) {
    addNotification('培训测试', `恭喜您通过「${selectedTraining.value.name}」测试，得分: ${examScore.value}分`, 'safety')
  }

  // 将培训状态标记为已完成（本地即时反馈，刷新后从后端考试记录重新加载）
  const trainingIdx = trainings.value.findIndex(t => t.id === selectedTraining.value.id)
  if (trainingIdx !== -1) {
    trainings.value[trainingIdx].status = 'completed'
  }
}

const handleSave = async () => {
  const newItem = { ...editForm.value, updateDate: new Date().toISOString().split('T')[0] }
  let action = ''
  try {
    if (activeTab.value === 'regulations') {
      const regulationData = {
        title: newItem.title,
        content: newItem.content,
        type: newItem.category
      }
      if (editing.value) {
        const res = await safetyApi.updateRegulation(currentEditId.value, regulationData)
        ElMessage.success(res.msg || '更新成功')
        const idx = regulations.value.findIndex(i => i.id === currentEditId.value)
        if (idx !== -1) regulations.value[idx] = { ...regulations.value[idx], ...newItem }
        action = `编辑安全制度：${newItem.title}`
      } else {
        const res = await safetyApi.createRegulation(regulationData)
        ElMessage.success(res.msg || '新增成功')
        regulations.value.push({ ...newItem, id: res.data?.id || Date.now() })
        action = `新增安全制度：${newItem.title}`
      }
    } else if (activeTab.value === 'trainings') {
      // 字段映射：前端字段 -> 后端 SafetyTraining 实体
      const trainingData = {
        title: newItem.name,
        content: `答题限时: ${newItem.timeLimit || 30}分钟`,
        trainingTime: newItem.startDate ? newItem.startDate + 'T00:00:00' : null,
        location: newItem.endDate || '',
        createdBy: newItem.createdBy || null
      }
      if (editing.value) {
        const res = await safetyApi.updateTraining(currentEditId.value, trainingData)
        ElMessage.success(res.msg || '更新成功')
        const idx = trainings.value.findIndex(i => i.id === currentEditId.value)
        if (idx !== -1) trainings.value[idx] = { ...trainings.value[idx], ...newItem }
        action = `编辑培训：${newItem.name}`
      } else {
        const res = await safetyApi.createTraining(trainingData)
        ElMessage.success(res.msg || '新增成功')
        trainings.value.push({ ...newItem, id: res.data?.id || Date.now() })
        action = `新增培训：${newItem.name}`
      }
    } else if (activeTab.value === 'inspections') {
      if (!newItem.inspectorId) {
        alert('请选择检查员！')
        return
      }
      // 字段映射：前端字段 -> 后端 SafetyInspection 实体
      const inspectionData = {
        title: `安全检查 - ${newItem.date}`,
        content: `检查项: ${newItem.items || 0}, 问题项: ${newItem.issues || 0}`,
        result: newItem.issues > 0 ? 'issues' : 'passed',
        inspectionTime: newItem.date ? newItem.date + 'T00:00:00' : null,
        inspectorId: newItem.inspectorId || null
      }
      if (editing.value) {
        const res = await safetyApi.updateInspection(currentEditId.value, inspectionData)
        ElMessage.success(res.msg || '更新成功')
        const idx = inspections.value.findIndex(i => i.id === currentEditId.value)
        if (idx !== -1) inspections.value[idx] = { ...inspections.value[idx], ...newItem }
        action = `编辑安全检查：${newItem.date}`
      } else {
        const res = await safetyApi.createInspection(inspectionData)
        ElMessage.success(res.msg || '新增成功')
        inspections.value.push({ ...newItem, id: res.data?.id || Date.now() })
        action = `新增安全检查：${newItem.date}`
      }
    } else if (activeTab.value === 'incidents') {
      const incidentData = {
        title: newItem.type,
        description: newItem.description || '',
        level: newItem.severity || '一般',
        status: newItem.status || 'processing',
        incidentTime: newItem.date ? newItem.date + 'T00:00:00' : null,
        reporterId: newItem.reporterId || null,
        location: newItem.location || ''
      }
      if (editing.value) {
        const res = await safetyApi.updateIncident(currentEditId.value, incidentData)
        ElMessage.success(res.msg || '更新成功')
        const idx = incidents.value.findIndex(i => i.id === currentEditId.value)
        if (idx !== -1) incidents.value[idx] = { ...incidents.value[idx], ...newItem }
        action = `编辑事故：${newItem.type}`
      } else {
        const res = await safetyApi.createIncident(incidentData)
        ElMessage.success(res.msg || '新增成功')
        incidents.value.push({ ...newItem, id: res.data?.id || Date.now() })
        action = `新增事故：${newItem.type}`
      }
    }
    showModal.value = false
    editing.value = false
    editForm.value = {}
  } catch (e) {
    console.error('保存失败:', e)
    ElMessage.error(e.message)
  }
}

const handleDelete = async (id, storeKey) => {
  if (!confirm('确定删除吗？')) return
  let item = null
  try {
    if (storeKey === 'regulations') {
      item = regulations.value.find(i => i.id === id)
      const delRes = await safetyApi.deleteRegulation(id)
      ElMessage.success(delRes.msg || '删除成功')
      regulations.value = regulations.value.filter(i => i.id !== id)
    } else if (storeKey === 'trainings') {
      item = trainings.value.find(i => i.id === id)
      const delRes = await safetyApi.deleteTraining(id)
      ElMessage.success(delRes.msg || '删除成功')
      trainings.value = trainings.value.filter(i => i.id !== id)
    } else if (storeKey === 'inspections') {
      item = inspections.value.find(i => i.id === id)
      const delRes = await safetyApi.deleteInspection(id)
      ElMessage.success(delRes.msg || '删除成功')
      inspections.value = inspections.value.filter(i => i.id !== id)
    } else if (storeKey === 'incidents') {
      item = incidents.value.find(i => i.id === id)
      const delRes = await safetyApi.deleteIncident(id)
      ElMessage.success(delRes.msg || '删除成功')
      incidents.value = incidents.value.filter(i => i.id !== id)
    }
  } catch (e) {
    console.error('删除失败:', e)
    ElMessage.error(e.message)
  }
}

const openAddModal = () => {
  editing.value = false
  currentEditId.value = null
  if (activeTab.value === 'regulations') editForm.value = { title: '', category: '', content: '' }
  else if (activeTab.value === 'trainings') editForm.value = { name: '', startDate: '', endDate: '', createdBy: null, timeLimit: 30 }
  else if (activeTab.value === 'inspections') editForm.value = { date: '', inspectorId: null, items: 0, issues: 0 }
  else if (activeTab.value === 'incidents') editForm.value = { type: '', date: '', location: '', severity: 'minor', description: '', reporterId: null }
  showModal.value = true
}

const openEditModal = (item) => {
  editing.value = true
  currentEditId.value = item.id
  editForm.value = { ...item }
  showModal.value = true
}

const completeTraining = (training) => {
  selectedTraining.value = training
  completeForm.value.participants = training.participants || 0
  showCompleteModal.value = true
}
const confirmComplete = async () => {
  try {
    const res = await safetyApi.updateTraining(selectedTraining.value.id, {
      status: 'completed',
      participants: completeForm.value.participants
    })
    ElMessage.success(res.msg || '操作成功')
    const idx = trainings.value.findIndex(t => t.id === selectedTraining.value.id)
    if (idx !== -1) {
      trainings.value[idx].status = 'completed'
      trainings.value[idx].participants = completeForm.value.participants
    }
  } catch (e) {
    console.error('完成培训失败:', e)
    ElMessage.error(e.message)
  }
  showCompleteModal.value = false
}

const resolveInspection = async (id) => {
  try {
    const idx = inspections.value.findIndex(i => i.id === id)
    if (idx !== -1) {
      const updateData = { result: 'passed' }
      const res = await safetyApi.updateInspection(id, updateData)
      ElMessage.success(res.msg || '整改成功')
      inspections.value[idx].status = 'passed'
      inspections.value[idx].issues = 0
      addNotification('安全检查', `安全检查 ${inspections.value[idx].date} 整改完成`, 'safety')
    }
  } catch (e) {
    console.error('整改完成操作失败:', e)
    ElMessage.error(e.message)
  }
}

const resolveIncident = async (id) => {
  try {
    const idx = incidents.value.findIndex(i => i.id === id)
    if (idx !== -1) {
      const res = await safetyApi.updateIncident(id, { ...incidents.value[idx], status: 'resolved' })
      ElMessage.success(res.msg || '操作成功')
      incidents.value[idx].status = 'resolved'
      addNotification('事故处理', `${incidents.value[idx].type} 已解决`, 'safety')
    }
  } catch (e) {
    console.error('标记解决操作失败:', e)
    ElMessage.error(e.message)
  }
}

const getSeverityClass = (sev) => ({ minor: 'status-pending', major: 'status-rejected', critical: 'status-maintenance' }[sev] || '')
const getSeverityLabel = (sev) => ({ minor: '轻微', major: '严重', critical: '重大' }[sev] || sev)

onMounted(loadData)
</script>
