import axios from 'axios'

const API_BASE_URL = ''

// ========== Axios 实例 ==========
const instance = axios.create({
    baseURL: API_BASE_URL,
    timeout: 30000
})

// ========== 请求拦截器 — 自动注入 Token ==========
instance.interceptors.request.use(config => {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    if (token) {
        config.headers['Authorization'] = token
    }
    return config
}, error => Promise.reject(error))

// ========== 响应拦截器 — 统一解析后端的 Result 封装 ==========
// Result 类：{ code: int, msg: String, data: Object }
// - Result.success() → code=200, msg="操作成功！"
// - Result.fail()    → code=400, msg="操作失败！", data=错误详情
// - Result.error()   → code=-1,  msg=错误信息,  data=null
instance.interceptors.response.use(
    response => {
        const result = response.data
        // 204 No Content
        if (result === '' || result === null) return null
        // 统一解析 Result 格式
        if (result && result.code !== undefined) {
            if (result.code === 200) {
                return {
                    data: result.data,
                    msg: result.msg || result.message || ''
                }
            }
            // Result.fail() 把错误信息放在 data（code=400, msg="操作失败！"）
            // Result.error() 把错误信息放在 msg（code=-1, data=null）
            const errMsg = (typeof result.data === 'string' && result.data)
                || result.msg || result.message || `请求失败: ${result.code}`
            return Promise.reject(new Error(errMsg))
        }
        // 无 code 字段 → 返回原始数据（如验证码接口）
        return result
    },
    error => {
        // 401 未授权 → 清除登录态，跳转登录页
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('labUser')
            window.location.href = '/login'
        }
        return Promise.reject(new Error('网络异常，请重试'))
    }
)

// ========== HTTP 工具方法 ==========
export const http = {
    get: (url) => instance.get(url),
    post: (url, data) => instance.post(url, data),
    put: (url, data) => instance.put(url, data),
    delete: (url) => instance.delete(url),
    // 文件上传（FormData，axios 自动设置 Content-Type: multipart/form-data + boundary）
    upload: (url, formData) => instance.post(url, formData)
}

// ========== 登录/用户 API ==========
export const loginApi = {
    // 登录：接收 { account, password, code, userKey }，JSON 提交
    login: async (loginRequest) => {
        return http.post('/login', loginRequest)
    },
    logout: async () => {
        return http.post('/logout')
    },
    getCaptcha: async () => {
        const res = await http.get('/captcha')
        return res.data
    }
}

export const userApi = {
    getInfo: async (userId) => {
        const res = await http.get(`/sysUser/user?userId=${userId}`)
        return res.data
    },
    getAllUsers: async () => {
        const res = await http.get('/sysUser/AllUsers')
        return res.data
    },
    getRoleAndAuthority: async (userId) => {
        const res = await http.get(`/sysUser/RoleAndAndAuthority?userId=${userId}`)
        return res.data
    },
    register: async (data) => {
        return http.post('/sysUser/register', data)
    },
    getAvatarHistory: async (userId) => {
        const res = await http.get(`/sysUser/avatarHistory?userId=${userId}`)
        return res.data
    },
    deleteAvatar: async (userId, filename) => {
        return http.delete(`/sysUser/deleteAvatar?userId=${userId}&filename=${encodeURIComponent(filename)}`)
    }
}

// ========== 设备状态映射 ==========
const mapEquipmentStatusFromBackend = (status) => {
    if (status === 'normal') return 'available'
    return status
}

const mapEquipmentStatusToBackend = (status) => {
    if (status === 'available') return 'normal'
    return status
}

// ========== 设备 API ==========
export const equipmentApi = {
    getList: async (params = {}) => {
        const res = await http.get('/api/equipment')
        const data = res.data
        let list = Array.isArray(data) ? data : (data?.content || data?.records || [])
        list = list.map(item => ({
            ...item,
            status: mapEquipmentStatusFromBackend(item.status)
        }))
        if (params.status && params.status !== 'all') {
            list = list.filter(e => e.status === params.status)
        }
        if (params.search) {
            list = list.filter(e => e.name?.includes(params.search) || e.code?.includes(params.search))
        }
        return { content: list }
    },
    create: async (data) => {
        const mapped = { ...data, status: mapEquipmentStatusToBackend(data.status) }
        if (mapped.purchaseDate && !mapped.purchaseDate.includes('T')) {
            mapped.purchaseDate = mapped.purchaseDate + 'T00:00:00'
        }
        return http.post('/api/equipment', mapped)
    },
    update: async (id, data) => {
        const mapped = { ...data, status: mapEquipmentStatusToBackend(data.status) }
        if (mapped.purchaseDate && !mapped.purchaseDate.includes('T')) {
            mapped.purchaseDate = mapped.purchaseDate + 'T00:00:00'
        }
        return http.put(`/api/equipment/${id}`, mapped)
    },
    delete: async (id) => {
        return http.delete(`/api/equipment/${id}`)
    },
    borrow: async (id, data) => {
        return http.post(`/api/equipment/${id}/borrow?userId=${data.userId}&purpose=${encodeURIComponent(data.purpose || '')}`)
    },
    return: async (id, data) => {
        const userId = data?.userId || ''
        return http.post(`/api/equipment/${id}/return?userId=${userId}`)
    },
    maintenance: async (id, data) => {
        return http.post(`/api/equipment/${id}/maintenance`, data)
    },
    completeMaintenance: async (id, data) => {
        return http.post(`/api/equipment/${id}/maintenance/complete`, data)
    },
    getMaintenanceRecords: async (id) => {
        const res = await http.get(`/api/equipment/${id}/maintenance-records`)
        return res.data
    },
    getUsageRecords: async (id) => {
        const res = await http.get(`/api/equipment/${id}/usage-records`)
        return res.data
    }
}

// ========== 人员 API ==========
export const personnelApi = {
    getList: async () => {
        const res = await http.get('/api/personnel')
        const data = res.data
        return { content: Array.isArray(data) ? data : (data?.content || data?.records || []) }
    },
    create: async (data) => {
        return http.post('/api/personnel', data)
    },
    update: async (id, data) => {
        return http.put(`/api/personnel/${id}`, data)
    },
    delete: async (id) => {
        return http.delete(`/api/personnel/${id}`)
    },
    getAttendanceRecords: async (id) => {
        const res = await http.get(`/api/personnel/${id}/attendance`)
        return res.data
    },
    getTrainingRecords: async (id) => {
        const res = await http.get('/api/safety/trainings')
        const data = res.data
        const list = Array.isArray(data) ? data : (data?.content || data?.records || [])
        return list.filter(r => r.personnelId === id)
    },
    updateAttendanceRecord: async (recordId, updates) => {
        return http.put(`/api/personnel/attendance/${recordId}`, updates)
    },
    getCertification: async () => {
        const res = await http.get('/api/personnel/certification')
        return res.data
    },
    certify: async (data) => {
        return http.post('/api/personnel/certify', data)
    }
}

// ========== 考勤活动 API ==========
export const attendanceActivityApi = {
    getList: async () => {
        const res = await http.get('/api/attendance-activities')
        return res.data
    },
    getActiveActivities: async () => {
        const res = await http.get('/api/attendance-activities/active')
        return res.data
    },
    create: async (data) => {
        const participantIds = data.participantIds || []
        const params = participantIds.length > 0 ? `?participantIds=${participantIds.join(',')}` : ''
        const activityData = { ...data }
        delete activityData.participantIds
        return http.post(`/api/attendance-activities${params}`, activityData)
    },
    sign: async (activityId, personnelId, signerId) => {
        return http.post(`/api/attendance-activities/${activityId}/sign?personnelId=${personnelId}`)
    },
    endActivity: async (activityId) => {
        return http.post(`/api/attendance-activities/${activityId}/end`)
    },
    getActivityStatus: async (activityId) => {
        const res = await http.get(`/api/attendance-activities/${activityId}/status`)
        return res.data
    }
}

// ========== 个人考勤公共接口 ==========
export const myAttendanceApi = {
    getMyAttendance: async () => {
        const res = await http.get('/api/attendance/my')
        return res.data
    },
    signIn: async (activityId) => {
        return http.post(`/api/attendance/my/${activityId}/sign`)
    }
}

// ========== 实验室 API ==========
export const labApi = {
    getList: async () => {
        const res = await http.get('/api/labs')
        const data = res.data
        return { content: Array.isArray(data) ? data : (data?.content || data?.records || []) }
    },
    create: async (data) => {
        return http.post('/api/labs', data)
    },
    update: async (id, data) => {
        return http.put(`/api/labs/${id}`, data)
    },
    delete: async (id) => {
        return http.delete(`/api/labs/${id}`)
    }
}

// ========== 预约 API ==========
export const reservationApi = {
    getList: async () => {
        const res = await http.get('/api/reservations')
        const data = res.data
        return { content: Array.isArray(data) ? data : (data?.content || data?.records || []) }
    },
    create: async (data) => {
        return http.post('/api/reservations', data)
    },
    approve: async (id, approverId, remark = '') => {
        return http.post(`/api/reservations/${id}/approve?approverId=${approverId}&remark=${encodeURIComponent(remark)}`)
    },
    reject: async (id, remark = '') => {
        return http.post(`/api/reservations/${id}/reject?remark=${encodeURIComponent(remark)}`)
    },
    delete: async (id) => {
        return http.delete(`/api/reservations/${id}`)
    }
}

// ========== 物资 API ==========
export const resourceApi = {
    getList: async () => {
        const res = await http.get('/api/resources')
        const data = res.data
        return { content: Array.isArray(data) ? data : (data?.content || data?.records || []) }
    },
    create: async (data) => {
        return http.post('/api/resources', data)
    },
    update: async (id, data) => {
        return http.put(`/api/resources/${id}`, data)
    },
    delete: async (id) => {
        return http.delete(`/api/resources/${id}`)
    },
    purchase: async (id, data) => {
        return http.post(`/api/resources/${id}/purchase`, data)
    },
    usage: async (id, data) => {
        return http.post(`/api/resources/${id}/usage`, data)
    },
    getRecords: async (id) => {
        const res = await http.get(`/api/resources/${id}/records`)
        return res.data
    }
}

// ========== 消息通知 API ==========
export const notificationApi = {
    send: async (data) => {
        const res = await http.post('/notification/send', data)
        return res.data
    },
    getList: async () => {
        const res = await http.get('/notification/list')
        return res.data
    },
    markRead: async (id) => {
        const res = await http.put(`/notification/read/${id}`)
        return res.data
    },
    markAllRead: async () => {
        const res = await http.put('/notification/read-all')
        return res.data
    },
    clearAll: async () => {
        const res = await http.delete('/notification/clear')
        return res.data
    },
    getUsers: async () => {
        const res = await http.get('/sysUser/AllUsers')
        return res.data || []
    }
}

// ========== 公共 API ==========
export const commonApi = {
    getSimpleUserList: async () => {
        const res = await http.get('/sysUser/simple-list')
        return { content: Array.isArray(res.data) ? res.data : [] }
    }
}

// ========== 安全管理 API ==========
export const safetyApi = {
    getRegulations: async () => {
        const res = await http.get('/api/safety/regulations')
        const data = res.data
        return { content: Array.isArray(data) ? data : (data?.content || data?.records || []) }
    },
    createRegulation: async (data) => {
        return http.post('/api/safety/regulations', data)
    },
    updateRegulation: async (id, data) => {
        return http.put(`/api/safety/regulations/${id}`, data)
    },
    deleteRegulation: async (id) => {
        return http.delete(`/api/safety/regulations/${id}`)
    },
    getTrainings: async () => {
        const res = await http.get('/api/safety/trainings')
        const data = res.data
        return { content: Array.isArray(data) ? data : (data?.content || data?.records || []) }
    },
    createTraining: async (data) => {
        return http.post('/api/safety/trainings', data)
    },
    updateTraining: async (id, data) => {
        return http.put(`/api/safety/trainings/${id}`, data)
    },
    deleteTraining: async (id) => {
        return http.delete(`/api/safety/trainings/${id}`)
    },
    getQuestions: async (trainingId) => {
        const params = trainingId ? `?trainingId=${trainingId}` : ''
        const res = await http.get(`/api/safety/questions${params}`)
        const data = res.data
        return { content: Array.isArray(data) ? data : (data?.content || data?.records || []) }
    },
    createQuestion: async (data) => {
        return http.post('/api/safety/questions', data)
    },
    updateQuestion: async (id, data) => {
        return http.post(`/api/safety/questions/${id}/update`, data)
    },
    deleteQuestion: async (id) => {
        return http.delete(`/api/safety/questions/${id}`)
    },
    submitExam: async (data) => {
        return http.post('/api/safety/exam/submit', data)
    },
    getMyExamRecords: async () => {
        const res = await http.get('/api/safety/exam/my-records')
        return res.data
    },
    getUserExamRecords: async (userId) => {
        const res = await http.get(`/api/safety/exam-records/user/${userId}`)
        return res.data
    },
    getExamStats: async () => {
        const res = await http.get('/api/safety/exam-records/stats')
        return res.data
    },
    getInspections: async () => {
        const res = await http.get('/api/safety/inspections')
        const data = res.data
        return { content: Array.isArray(data) ? data : (data?.content || data?.records || []) }
    },
    createInspection: async (data) => {
        return http.post('/api/safety/inspections', data)
    },
    updateInspection: async (id, data) => {
        return http.put(`/api/safety/inspections/${id}`, data)
    },
    deleteInspection: async (id) => {
        return http.delete(`/api/safety/inspections/${id}`)
    },
    getIncidents: async () => {
        const res = await http.get('/api/safety/incidents')
        const data = res.data
        return { content: Array.isArray(data) ? data : (data?.content || data?.records || []) }
    },
    createIncident: async (data) => {
        return http.post('/api/safety/incidents', data)
    },
    updateIncident: async (id, data) => {
        return http.put(`/api/safety/incidents/${id}`, data)
    },
    deleteIncident: async (id) => {
        return http.delete(`/api/safety/incidents/${id}`)
    }
}

// ========== 报告 API ==========
export const reportApi = {
    getList: async () => {
        const res = await http.get('/api/reports')
        const data = res.data
        return { content: Array.isArray(data) ? data : (data?.content || data?.records || []) }
    },
    create: async (data) => {
        return http.post('/api/reports', data)
    },
    grade: async (id, data) => {
        return http.post(`/api/reports/${id}/grade`, data)
    },
    reject: async (id, data) => {
        return http.post(`/api/reports/${id}/reject`, data)
    },
    // 导出接口返回文件流，不走 Result 解析，直接返回 Blob
    export: async () => {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token')
        const response = await axios.get('/api/reports/export', {
            headers: token ? { 'Authorization': token } : {},
            responseType: 'blob'
        })
        return response.data
    }
}

// ========== 角色管理 API ==========
export const roleApi = {
    getAllRoles: async () => {
        const res = await http.get('/role/AllRoles')
        return res.data
    },
    getAllAuthorities: async () => {
        const res = await http.get('/role/AllAuthorities')
        return res.data
    },
    getRoleAuthorities: async (roleId) => {
        const res = await http.get(`/role/Authorities?roleId=${roleId}`)
        return res.data
    },
    updateRoleAuthority: async (roleId, authorityIds) => {
        const ids = authorityIds.join(',')
        return http.put(`/role/updateRoleAuthority?roleId=${roleId}&authorityIds=${ids}`)
    },
    createRole: async (data) => {
        return http.post('/role/insert', data)
    },
    deleteRole: async (roleId) => {
        return http.delete(`/role/delete/${roleId}`)
    },
    getMapping: async () => {
        const res = await http.get('/role/mapping')
        return res.data
    }
}

// ========== 部门管理 API ==========
export const departmentApi = {
    getList: async () => {
        const res = await http.get('/api/departments')
        return res.data
    },
    create: async (data) => {
        return http.post('/api/departments', data)
    },
    update: async (id, data) => {
        return http.put(`/api/departments/${id}`, data)
    },
    delete: async (id) => {
        return http.delete(`/api/departments/${id}`)
    }
}

// ========== 仪表盘 API ==========
export const dashboardApi = {
    getStats: async () => {
        const res = await http.get('/api/dashboard/stats')
        return res.data
    },
    getEquipmentStatus: async () => {
        const res = await http.get('/api/dashboard/equipment-status')
        const data = res.data
        if (data && typeof data === 'object') {
            if (data.normal !== undefined && data.available === undefined) {
                data.available = data.normal
                delete data.normal
            }
        }
        return data
    },
    getWeeklyUsage: async () => {
        const res = await http.get('/api/dashboard/weekly-usage')
        return res.data
    }
}
