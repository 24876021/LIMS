// 定义每个模块对应的操作权限映射
// 与数据库 authority 表保持一致: all/get/set/add/remove
export const moduleOperations = {
    equipment: ['all', 'get', 'set', 'add', 'remove'],
    personnel: ['all', 'get', 'set', 'add', 'remove'],
    resource: ['all', 'get', 'set', 'add', 'remove'],
    safety: ['all', 'get', 'set', 'add', 'remove'],
    reservation: ['all', 'get', 'set', 'add', 'remove'],
    report: ['all', 'get', 'set', 'add', 'remove'],
    dashboard: ['all', 'get']
}

// 根据模块列表生成完整操作权限（兼容旧数据：如果只有模块名，自动赋予该模块下所有操作）
export function generateFullPermissions(modulePermissions = []) {
    const fullPerms = new Set()
    modulePermissions.forEach(perm => {
        if (moduleOperations[perm]) {
            // 模块级权限：添加该模块下所有操作
            moduleOperations[perm].forEach(op => fullPerms.add(`${perm}:${op}`))
        } else {
            // 已经是具体操作权限格式，直接添加
            fullPerms.add(perm)
        }
    })
    return Array.from(fullPerms)
}

// 检查是否有某个操作权限
// 支持 :all 通配符：拥有 module:all 等同于拥有该模块下所有操作权限
export function hasPermission(module, operation = null) {
    const perms = localStorage.getItem('userPermissions')
    if (!perms) return false
    const permissions = JSON.parse(perms)
    if (operation === null) {
        // 检查是否有该模块的任何操作权限（用于菜单显示）
        return permissions.some(p => p.startsWith(`${module}:`))
    }
    // 精确匹配 或 :all 通配
    return permissions.includes(`${module}:${operation}`) || permissions.includes(`${module}:all`)
}

// 获取当前用户所有权限
export function getUserPermissions() {
    const perms = localStorage.getItem('userPermissions')
    return perms ? JSON.parse(perms) : []
}

// 保存权限（登录时调用）
export function setUserPermissions(permissions) {
    localStorage.setItem('userPermissions', JSON.stringify(permissions))
}
