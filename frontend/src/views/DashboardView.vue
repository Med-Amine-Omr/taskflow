<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { tasksApi } from '@/api/tasks'
import { messageOf } from '@/api/errors'
import type { Task, TaskStats } from '@/types/api'
import { PRIORITY_LABELS, formatDate } from '@/utils/format'

const stats = ref<TaskStats | null>(null)
const upcoming = ref<Task[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const [statsResult, page] = await Promise.all([
      tasksApi.stats(),
      tasksApi.list({ completed: false, sort: 'dueDate', direction: 'asc', size: 5 }),
    ])
    stats.value = statsResult
    upcoming.value = page.content.filter((task) => task.dueDate !== null)
  } catch (e) {
    error.value = messageOf(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section>
    <h1>Tableau de bord</h1>

    <p v-if="loading">Chargement…</p>
    <p v-else-if="error" class="alert">{{ error }}</p>

    <template v-else>
      <div v-if="stats" class="stats">
        <div class="card stat">
          <div class="stat__value">{{ stats.completed }}</div>
          <div class="stat__label">Terminées</div>
        </div>
        <div class="card stat">
          <div class="stat__value">{{ stats.inProgress }}</div>
          <div class="stat__label">En cours</div>
        </div>
        <div class="card stat stat--overdue">
          <div class="stat__value">{{ stats.overdue }}</div>
          <div class="stat__label">En retard</div>
        </div>
      </div>

      <div class="card">
        <div class="page-header">
          <h2>5 prochaines échéances</h2>
          <RouterLink to="/tasks">Voir toutes mes tâches</RouterLink>
        </div>
        <p v-if="upcoming.length === 0" class="empty">Aucune tâche à venir avec une échéance.</p>
        <ul v-else class="upcoming">
          <li v-for="task in upcoming" :key="task.id" :class="{ 'upcoming--overdue': task.overdue }">
            <span class="upcoming__date">{{ formatDate(task.dueDate) }}</span>
            <span class="upcoming__title">{{ task.title }}</span>
            <span class="badge" :class="`badge--${task.priority.toLowerCase()}`">
              {{ PRIORITY_LABELS[task.priority] }}
            </span>
            <span v-if="task.overdue" class="badge badge--overdue">En retard</span>
          </li>
        </ul>
      </div>
    </template>
  </section>
</template>