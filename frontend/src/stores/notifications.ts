import { ref } from 'vue'
import { defineStore } from 'pinia'

export interface Notification {
  id: number
  type: 'success' | 'error'
  message: string
}

export const useNotificationsStore = defineStore('notifications', () => {
  const items = ref<Notification[]>([])
  let nextId = 1

  function push(type: Notification['type'], message: string) {
    const id = nextId++
    items.value.push({ id, type, message })
    setTimeout(() => dismiss(id), 4000)
  }

  function dismiss(id: number) {
    items.value = items.value.filter((item) => item.id !== id)
  }

  return {
    items,
    dismiss,
    success: (message: string) => push('success', message),
    error: (message: string) => push('error', message),
  }
})