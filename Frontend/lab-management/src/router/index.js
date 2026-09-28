import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/Layout.vue'
import Dashboard from '@/views/Dashboard.vue'
import Equipment from '@/views/Equipment.vue'
import Personnel from '@/views/Personnel.vue'
import Resources from '@/views/Resources.vue'
import Safety from '@/views/Safety.vue'
import Reservation from '@/views/Reservation.vue'
import Reports from '@/views/Reports.vue'
import Profile from '@/views/Profile.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: Login,
        meta: { requiresAuth: false }
    },
    {
        path: '/register',
        name: 'Register',
        component: Register,
        meta: { requiresAuth: false }
    },
    {
        path: '/',
        component: Layout,
        meta: { requiresAuth: true },
        children: [
            { path: '', name: 'Dashboard', component: Dashboard, meta: { permission: 'dashboard' } },
            { path: 'equipment', name: 'Equipment', component: Equipment, meta: { permission: 'equipment' } },
            { path: 'personnel', name: 'Personnel', component: Personnel, meta: { permission: 'personnel' } },
            { path: 'resources', name: 'Resources', component: Resources, meta: { permission: 'resource' } },
            { path: 'safety', name: 'Safety', component: Safety, meta: { permission: 'safety' } },
            { path: 'reservation', name: 'Reservation', component: Reservation, meta: { permission: 'reservation' } },
            { path: 'reports', name: 'Reports', component: Reports, meta: { permission: 'report' } },
            { path: 'profile', name: 'Profile', component: Profile, meta: { permission: 'dashboard' } },
        ]
    },
    // 404 路由（必须放最后）
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        redirect: '/login'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 不需要登录的路径白名单
const whiteList = ['/login', '/register']

router.beforeEach((to, from, next) => {
    // 支持记住我：从 localStorage 或 sessionStorage 获取 token
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')

    if (token) {
        // 已登录：访问登录/注册页 → 重定向到首页
        if (whiteList.includes(to.path)) {
            next('/')
            return
        }
        // 已登录：检查模块权限
        if (to.meta.permission) {
            const permissions = JSON.parse(localStorage.getItem('userPermissions') || '[]')
            const hasModulePerm = permissions.some(p => p.startsWith(`${to.meta.permission}:`))
            // dashboard 是首页，所有登录用户可见（页面内自行处理无权限提示）
            // 其他模块页面需要对应权限才能进入
            if (!hasModulePerm && to.meta.permission !== 'dashboard') {
                next('/')
                return
            }
        }
        next()
    } else {
        // 未登录：白名单内路径放行，其余全部跳转登录页
        if (whiteList.includes(to.path)) {
            next()
        } else {
            next('/login')
        }
    }
})

export default router
