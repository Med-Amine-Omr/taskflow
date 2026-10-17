<script setup lang="ts">
import AppNavbar from '@/components/AppNavbar.vue'
import AppToasts from '@/components/AppToasts.vue'
</script>

<template>
  <AppNavbar />
  <main class="container">
    <RouterView :key="$route.fullPath" />
  </main>
  <AppToasts />
</template>