<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import { adminApi } from '@/api/tasks'
import { messageOf } from '@/api/errors'
import { useNotificationsStore } from '@/stores/notifications'
import type { Page, Task, UserTaskStats } from '@/types/api'
import { PRIORITY_LABELS, formatDate, formatDateTime } from '@/utils/format'

const PAGE_SIZE = 10
const notifications = useNotificationsStore()

const stats = ref<UserTaskStats[]>([])
const ownerId = ref('')
const page = ref(0)
const result = ref<Page<Task> | null>(null)
const error = ref('')
const taskToDelete = ref<Task | null>(null)

async function loadStats() {
  stats.value = await adminApi.stats()
}

async function loadTasks() {
  result.value = await adminApi.list({
    ownerId: ownerId.value || undefined,
    page: page.value,
    size: PAGE_SIZE,
    sort: 'createdAt',
    direction: 'desc',
  })
}

async function run(action: () => Promise<unknown>) {
  error.value = ''
  try {
    await action()
  } catch (e) {
    error.value = messageOf(e)
  }
}

watch(ownerId, () => {
  if (page.value !== 0) {
    page.value = 0
  } else {
    run(loadTasks)
  }
})
watch(page, () => run(loadTasks))
onMounted(() => run(() => Promise.all([loadStats(), loadTasks()])))

async function confirmDelete() {
  const task = taskToDelete.value
  if (!task) {
    return
  }
  taskToDelete.value = null
  try {
    await adminApi.remove(task.id)
    notifications.success(`Tâche de ${task.ownerUsername} supprimée`)
    if (result.value && result.value.content.length === 1 && page.value > 0) {
      page.value--
    } else {
      await loadTasks()
    }
    await loadStats()
  } catch (e) {
    notifications.error(messageOf(e))
  }
}
</script>

<template>
  <section>
    <h1>Administration</h1>
    <p v-if="error" class="alert">{{ error }}</p>

    <div class="card admin-block">
      <h2>Statistiques par utilisateur</h2>
      <p v-if="stats.length === 0" class="empty">Aucune tâche enregistrée.</p>
      <div v-else class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Utilisateur</th>
              <th>Total</th>
              <th>Terminées</th>
              <th>En cours</th>
              <th>En retard</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in stats" :key="row.ownerId">
              <td>{{ row.ownerUsername }}</td>
              <td>{{ row.total }}</td>
              <td>{{ row.completed }}</td>
              <td>{{ row.inProgress }}</td>
              <td>{{ row.overdue }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="card admin-block">
      <div class="page-header">
        <h2>Toutes les tâches</h2>
        <label class="filters__inline">
          Utilisateur
          <select v-model="ownerId">
            <option value="">Tous</option>
            <option v-for="row in stats" :key="row.ownerId" :value="row.ownerId">{{ row.ownerUsername }}</option>
          </select>
        </label>
      </div>

      <p v-if="result && result.content.length === 0" class="empty">Aucune tâche.</p>
      <div v-else-if="result" class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Propriétaire</th>
              <th>Titre</th>
              <th>Priorité</th>
              <th>Échéance</th>
              <th>Statut</th>
              <th>Créée le</th>
              <th><span class="sr-only">Actions</span></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="task in result.content" :key="task.id" :class="{ 'row--overdue': task.overdue }">
              <td>{{ task.ownerUsername }}</td>
              <td>{{ task.title }}</td>
              <td>{{ PRIORITY_LABELS[task.priority] }}</td>
              <td>{{ formatDate(task.dueDate) }}</td>
              <td>{{ task.completed ? 'Terminée' : task.overdue ? 'En retard' : 'En cours' }}</td>
              <td>{{ formatDateTime(task.createdAt) }}</td>
              <td>
                <button type="button" class="btn btn--danger" @click="taskToDelete = task">Supprimer</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <PaginationBar v-if="result" :page="result.page" :total-pages="result.totalPages" @change="page = $event" />
    </div>

    <ConfirmDialog
      :open="taskToDelete !== null"
      title="Supprimer la tâche"
      :message="`Supprimer « ${taskToDelete?.title} » de ${taskToDelete?.ownerUsername} ? Cette action est irréversible.`"
      @confirm="confirmDelete"
      @cancel="taskToDelete = null"
    />
  </section>
</template>