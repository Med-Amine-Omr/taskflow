import { http } from './http'
import type { Me, Page, Task, TaskPayload, TaskQuery, TaskStats, UserTaskStats } from '@/types/api'

export const tasksApi = {
  list: (query: TaskQuery) => 
    http.get<Page<Task>>('/api/tasks', { params: query }).then((r) => r.data),
  get: (id: string) => 
    http.get<Task>(`/api/tasks/${id}`).then((r) => r.data),
  create: (payload: TaskPayload) => 
    http.post<Task>('/api/tasks', payload).then((r) => r.data),
  update: (id: string, payload: TaskPayload) => 
    http.put<Task>(`/api/tasks/${id}`, payload).then((r) => r.data),
  toggle: (id: string) => 
    http.patch<Task>(`/api/tasks/${id}/toggle`).then((r) => r.data),
  remove: (id: string) => 
    http.delete(`/api/tasks/${id}`).then(() => undefined),
  stats: () => 
    http.get<TaskStats>('/api/tasks/stats').then((r) => r.data),
}

export const adminApi = {
  list: (query: TaskQuery & { ownerId?: string }) =>
    http.get<Page<Task>>('/api/admin/tasks', { params: query }).then((r) => r.data),
  remove: (id: string) => 
    http.delete(`/api/admin/tasks/${id}`).then(() => undefined),
  stats: () => 
    http.get<UserTaskStats[]>('/api/admin/stats').then((r) => r.data),
}

export const meApi = {
  get: () => http.get<Me>('/api/me').then((r) => r.data),
}