export type TaskPriority = 'LOW' | 'NORMAL' | 'HIGH'

export interface Task {
  id: string
  title: string
  description: string | null
  priority: TaskPriority
  dueDate: string | null // format AAAA-MM-JJ
  completed: boolean
  overdue: boolean
  ownerId: string
  ownerUsername: string
  createdAt: string // date-heure ISO
  updatedAt: string
}

export interface TaskPayload {
  title: string
  description: string | null
  priority: TaskPriority
  dueDate: string | null
}

export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface TaskQuery {
  completed?: boolean
  priority?: TaskPriority
  page?: number
  size?: number
  sort?: 'dueDate' | 'createdAt'
  direction?: 'asc' | 'desc'
}

export interface TaskStats {
  completed: number
  inProgress: number
  overdue: number
  total: number
}

export interface UserTaskStats {
  ownerId: string
  ownerUsername: string
  total: number
  completed: number
  inProgress: number
  overdue: number
}

export interface Me {
  id: string
  username: string
  name: string | null
  email: string | null
  roles: string[]
}

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  fieldErrors: { field: string; message: string }[]
}