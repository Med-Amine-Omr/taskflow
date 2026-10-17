<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import TaskItem from '@/components/TaskItem.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import { tasksApi } from '@/api/tasks'
import { messageOf } from '@/api/errors'
import { useNotificationsStore } from '@/stores/notifications'
import type { Page, Task, TaskPriority } from '@/types/api'
import { PRIORITY_LABELS } from '@/utils/format'

const PAGE_SIZE = 10
const notifications = useNotificationsStore()

const filters = reactive({
  status: '' as '' | 'todo' | 'done',
  priority: '' as '' | TaskPriority,
  sort: 'createdAt,desc',
})
const page = ref(0)
const result = ref<Page<Task> | null>(null)
const loading = ref(false)
const error = ref('')
const taskToDelete = ref<Task | null>(null)

async function load() {
  loading.value = true
  error.value = ''
  const [sort, direction] = filters.sort.split(',') as ['dueDate' | 'createdAt', 'asc' | 'desc']
  try {
    result.value = await tasksApi.list({
      completed: filters.status === '' ? undefined : filters.status === 'done',
      priority: filters.priority || undefined,
      sort,
      direction,
      page: page.value,
      size: PAGE_SIZE,
    })
  } catch (e) {
    error.value = messageOf(e)
  } finally {
    loading.value = false
  }
}

// Changement de filtre : retour à la première page
watch(filters, () => {
  if (page.value !== 0) {
    page.value = 0 // le watch(page) rechargera
  } else {
    load()
  }
})
watch(page, load)
onMounted(load)

async function onToggle(id: string) {
  try {
    const updated = await tasksApi.toggle(id)
    if (filters.status !== '') {
      await load() // la tâche ne correspond peut-être plus au filtre
    } else if (result.value) {
      result.value.content = result.value.content.map((task) => (task.id === id ? updated : task))
    }
  } catch (e) {
    notifications.error(messageOf(e))
  }
}

async function confirmDelete() {
  const task = taskToDelete.value
  if (!task) {
    return
  }
  taskToDelete.value = null
  try {
    await tasksApi.remove(task.id)
    notifications.success('Tâche supprimée')
    if (result.value && result.value.content.length === 1 && page.value > 0) {
      page.value-- // la page devient vide : on revient à la précédente
    } else {
      await load()
    }
  } catch (e) {
    notifications.error(messageOf(e))
  }
}
</script>

<template>
  <section>
    <div class="page-header">
      <h1>Mes tâches</h1>
      <RouterLink :to="{ name: 'task-create' }" class="btn btn--primary">Nouvelle tâche</RouterLink>
    </div>

    <div class="filters">
      <label>
        Statut
        <select v-model="filters.status">
          <option value="">Toutes</option>
          <option value="todo">À faire</option>
          <option value="done">Terminées</option>
        </select>
      </label>
      <label>
        Priorité
        <select v-model="filters.priority">
          <option value="">Toutes</option>
          <option v-for="(label, value) in PRIORITY_LABELS" :key="value" :value="value">{{ label }}</option>
        </select>
      </label>
      <label>
        Tri
        <select v-model="filters.sort">
          <option value="createdAt,desc">Plus récentes d'abord</option>
          <option value="createdAt,asc">Plus anciennes d'abord</option>
          <option value="dueDate,asc">Échéance la plus proche</option>
          <option value="dueDate,desc">Échéance la plus lointaine</option>
        </select>
      </label>
    </div>

    <p v-if="loading && !result">Chargement…</p>
    <p v-else-if="error" class="alert">{{ error }}</p>
    <template v-else-if="result">
      <p v-if="result.content.length === 0" class="empty">Aucune tâche ne correspond à ces critères.</p>
      <ul v-else class="task-list">
        <TaskItem
          v-for="task in result.content"
          :key="task.id"
          :task="task"
          @toggle="onToggle"
          @delete="taskToDelete = $event"
        />
      </ul>
      <PaginationBar :page="result.page" :total-pages="result.totalPages" @change="page = $event" />
    </template>

    <ConfirmDialog
      :open="taskToDelete !== null"
      title="Supprimer la tâche"
      :message="`Voulez-vous vraiment supprimer « ${taskToDelete?.title} » ? Cette action est irréversible.`"
      @confirm="confirmDelete"
      @cancel="taskToDelete = null"
    />
  </section>
</template>