import { notificationApi } from '@/services/api'

// 获取当前用户ID
const getCurrentUserId = () => {
  try {
    const labUser = JSON.parse(localStorage.getItem('labUser') || '{}')
    return labUser.id || null
  } catch { return null }
}

// 发送通知给自己（调后端 API，存入数据库 + WebSocket 实时推送）
export const addNotification = async (title, content, type = 'info') => {
  const userId = getCurrentUserId()
  if (!userId) return
  try {
    await notificationApi.send({ receiverId: userId, title, content, type })
  } catch (e) {
    console.error('发送通知失败:', e)
  }
}

// 获取所有通知（调后端 API）
export const getNotifications = async () => {
  try {
    const data = await notificationApi.getList()
    return Array.isArray(data) ? data : (data?.content || [])
  } catch (e) {
    console.error('加载通知失败:', e)
    return []
  }
}

// 标记单条已读
export const markAsRead = async (id) => {
  try { await notificationApi.markRead(id) } catch (e) {}
}

// 全部已读
export const markAllRead = async () => {
  try { await notificationApi.markAllRead() } catch (e) {}
}

// 清空所有消息
export const clearAll = async () => {
  try { await notificationApi.clearAll() } catch (e) {}
}

// 获取未读数量
export const getUnreadCount = async () => {
  const list = await getNotifications()
  return list.filter(n => !n.isRead).length
}
