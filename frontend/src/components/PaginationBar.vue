<script setup lang="ts">
defineProps<{ page: number; totalPages: number }>()
const emit = defineEmits<{ change: [page: number] }>()
</script>

<template>
  <nav v-if="totalPages > 1" class="pagination" aria-label="Pagination">
    <button type="button" class="btn btn--secondary" :disabled="page === 0" @click="emit('change', page - 1)">
      Précédent
    </button>
    <span>Page {{ page + 1 }} / {{ totalPages }}</span>
    <button
      type="button"
      class="btn btn--secondary"
      :disabled="page >= totalPages - 1"
      @click="emit('change', page + 1)"
    >
      Suivant
    </button>
  </nav>
</template>