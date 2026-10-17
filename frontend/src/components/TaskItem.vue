<script setup lang="ts">
import type { Task } from '@/types/api'
import { PRIORITY_LABELS, formatDate } from '@/utils/format'

defineProps<{ task: Task }>()

const emit = defineEmits<{
  toggle: [id: string]
  delete: [task: Task]
}>()
</script>

<template>
  <li
    class="task"
    :class="{ 'task--overdue': task.overdue, 'task--done': task.completed }"
    data-test="task-item"
  >
    <input
      type="checkbox"
      class="task__check"
      :checked="task.completed"
      :aria-label="task.completed ? 'Marquer comme non terminée' : 'Marquer comme terminée'"
      data-test="toggle"
      @change="emit('toggle', task.id)"
    />

    <div class="task__body">
      <p class="task__title">{{ task.title }}</p>
      <p v-if="task.description" class="task__description">{{ task.description }}</p>
      <div class="task__meta">
        <span class="badge" :class="`badge--${task.priority.toLowerCase()}`">
          {{ PRIORITY_LABELS[task.priority] }}
        </span>
        <span>Échéance : {{ formatDate(task.dueDate) }}</span>
        <span v-if="task.overdue" class="badge badge--overdue" data-test="overdue-badge">En retard</span>
      </div>
    </div>

    <div class="task__actions">
      <RouterLink :to="{ name: 'task-edit', params: { id: task.id } }" class="btn btn--secondary">
        Modifier
      </RouterLink>
      <button type="button" class="btn btn--danger" data-test="delete" @click="emit('delete', task)">
        Supprimer
      </button>
    </div>
  </li>
</template>