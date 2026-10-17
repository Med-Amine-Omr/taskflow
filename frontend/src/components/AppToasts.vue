<script setup lang="ts">
import { useNotificationsStore } from '@/stores/notifications'

const notifications = useNotificationsStore()
</script>

<template>
  <div class="toasts" aria-live="polite">
    <div
      v-for="item in notifications.items"
      :key="item.id"
      class="toast"
      :class="`toast--${item.type}`"
      role="status"
      @click="notifications.dismiss(item.id)"
    >
      {{ item.message }}
    </div>
  </div>
</template>