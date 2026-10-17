<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { TaskPayload, TaskPriority } from '@/types/api'
import { PRIORITY_LABELS } from '@/utils/format'

const TITLE_MAX = 100

const props = withDefaults(
  defineProps<{
    initial?: TaskPayload | null
    serverErrors?: Record<string, string>
    submitting?: boolean
    submitLabel?: string
  }>(),
  { initial: null, serverErrors: () => ({}), submitting: false, submitLabel: 'Enregistrer' },
)

const emit = defineEmits<{ submit: [payload: TaskPayload]; cancel: [] }>()

const form = reactive({
  title: '',
  description: '',
  priority: 'NORMAL' as TaskPriority,
  dueDate: '',
})
const clientErrors = ref<Record<string, string>>({})

// Pré-remplissage en mode modification
watch(
  () => props.initial,
  (value) => {
    if (value) {
      form.title = value.title
      form.description = value.description ?? ''
      form.priority = value.priority
      form.dueDate = value.dueDate ?? ''
    }
  },
  { immediate: true },
)

// Erreurs affichées : celles de l'API, puis celles du navigateur
const errors = computed(() => ({ ...props.serverErrors, ...clientErrors.value }))

const priorities = Object.keys(PRIORITY_LABELS) as TaskPriority[]

function validate(): boolean {
  const result: Record<string, string> = {}
  const title = form.title.trim()
  if (!title) {
    result.title = 'Le titre est obligatoire'
  } else if (title.length > TITLE_MAX) {
    result.title = `Le titre ne doit pas dépasser ${TITLE_MAX} caractères`
  }
  clientErrors.value = result
  return Object.keys(result).length === 0
}

function onSubmit() {
  if (!validate()) {
    return
  }
  emit('submit', {
    title: form.title.trim(),
    description: form.description.trim() || null,
    priority: form.priority,
    dueDate: form.dueDate || null,
  })
}
</script>

<template>
  <form class="form" novalidate @submit.prevent="onSubmit">
    <div class="field">
      <label for="title">Titre *</label>
      <input
        id="title"
        v-model="form.title"
        type="text"
        :maxlength="TITLE_MAX"
        :aria-invalid="!!errors.title"
        data-test="title"
      />
      <span class="field__hint">{{ form.title.length }} / {{ TITLE_MAX }}</span>
      <p v-if="errors.title" class="field__error" data-test="error-title">{{ errors.title }}</p>
    </div>

    <div class="field">
      <label for="description">Description</label>
      <textarea id="description" v-model="form.description" rows="4" data-test="description" />
      <p v-if="errors.description" class="field__error" data-test="error-description">
        {{ errors.description }}
      </p>
    </div>

    <div class="field">
      <label for="priority">Priorité *</label>
      <select id="priority" v-model="form.priority" :aria-invalid="!!errors.priority" data-test="priority">
        <option v-for="value in priorities" :key="value" :value="value">{{ PRIORITY_LABELS[value] }}</option>
      </select>
      <p v-if="errors.priority" class="field__error" data-test="error-priority">{{ errors.priority }}</p>
    </div>

    <div class="field">
      <label for="dueDate">Date d'échéance</label>
      <input id="dueDate" v-model="form.dueDate" type="date" :aria-invalid="!!errors.dueDate" data-test="dueDate" />
      <p v-if="errors.dueDate" class="field__error" data-test="error-dueDate">{{ errors.dueDate }}</p>
    </div>

    <div class="form__actions">
      <button type="button" class="btn btn--secondary" @click="emit('cancel')">Annuler</button>
      <button type="submit" class="btn btn--primary" :disabled="submitting" data-test="submit">
        {{ submitting ? 'Enregistrement…' : submitLabel }}
      </button>
    </div>
  </form>
</template>