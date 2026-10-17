<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import TaskForm from '@/components/TaskForm.vue'
import { tasksApi } from '@/api/tasks'
import { fieldErrorsOf, messageOf, toApiError } from '@/api/errors'
import { useNotificationsStore } from '@/stores/notifications'
import type { TaskPayload } from '@/types/api'

const props = defineProps<{ id?: string }>()

const router = useRouter()
const notifications = useNotificationsStore()

const isEdit = computed(() => !!props.id)
const initial = ref<TaskPayload | null>(null)
const serverErrors = ref<Record<string, string>>({})
const submitting = ref(false)
const loading = ref(!!props.id)
const loadError = ref('')

onMounted(async () => {
  if (!props.id) {
    return
  }
  try {
    const task = await tasksApi.get(props.id)
    initial.value = {
      title: task.title,
      description: task.description,
      priority: task.priority,
      dueDate: task.dueDate,
    }
  } catch (e) {
    loadError.value = toApiError(e)?.status === 404 ? 'Cette tâche est introuvable.' : messageOf(e)
  } finally {
    loading.value = false
  }
})

async function onSubmit(payload: TaskPayload) {
  submitting.value = true
  serverErrors.value = {}
  try {
    if (props.id) {
      await tasksApi.update(props.id, payload)
      notifications.success('Tâche modifiée')
    } else {
      await tasksApi.create(payload)
      notifications.success('Tâche créée')
    }
    await router.push({ name: 'tasks' })
  } catch (e) {
    serverErrors.value = fieldErrorsOf(e)
    if (Object.keys(serverErrors.value).length === 0) {
      notifications.error(messageOf(e))
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section>
    <h1>{{ isEdit ? 'Modifier la tâche' : 'Nouvelle tâche' }}</h1>

    <p v-if="loading">Chargement…</p>
    <div v-else-if="loadError" class="alert">
      {{ loadError }} <RouterLink to="/tasks">Retour à mes tâches</RouterLink>
    </div>
    <div v-else class="card">
      <TaskForm
        :initial="initial"
        :server-errors="serverErrors"
        :submitting="submitting"
        :submit-label="isEdit ? 'Enregistrer' : 'Créer la tâche'"
        @submit="onSubmit"
        @cancel="router.push({ name: 'tasks' })"
      />
    </div>
  </section>
</template>