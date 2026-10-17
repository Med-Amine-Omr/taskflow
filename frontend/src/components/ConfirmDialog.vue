<script setup lang="ts">
withDefaults(
  defineProps<{
    open: boolean
    title: string
    message: string
    confirmLabel?: string
  }>(),
  { confirmLabel: 'Supprimer' },
)

const emit = defineEmits<{ confirm: []; cancel: [] }>()
</script>

<template>
  <div v-if="open" class="dialog-backdrop" data-test="confirm-dialog" @click.self="emit('cancel')">
    <div class="dialog" role="alertdialog" aria-modal="true" aria-labelledby="confirm-dialog-title">
      <h2 id="confirm-dialog-title">{{ title }}</h2>
      <p>{{ message }}</p>
      <div class="dialog__actions">
        <button type="button" class="btn btn--secondary" data-test="cancel" @click="emit('cancel')">
          Annuler
        </button>
        <button type="button" class="btn btn--danger" data-test="confirm" @click="emit('confirm')">
          {{ confirmLabel }}
        </button>
      </div>
    </div>
  </div>
</template>