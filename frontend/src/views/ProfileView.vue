<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { meApi } from '@/api/tasks'
import { messageOf } from '@/api/errors'
import type { Me } from '@/types/api'

const me = ref<Me | null>(null)
const error = ref('')

onMounted(async () => {
  try {
    me.value = await meApi.get()
  } catch (e) {
    error.value = messageOf(e)
  }
})
</script>

<template>
  <section>
    <h1>Profil</h1>
    <p v-if="error" class="alert">{{ error }}</p>
    <p v-else-if="!me">Chargement…</p>
    <dl v-else class="card profile">
      <dt>Nom</dt>
      <dd>{{ me.name || '—' }}</dd>
      <dt>Nom d'utilisateur</dt>
      <dd>{{ me.username }}</dd>
      <dt>Email</dt>
      <dd>{{ me.email || '—' }}</dd>
      <dt>Rôles</dt>
      <dd>
        <span v-for="role in me.roles" :key="role" class="badge badge--normal">{{ role }}</span>
      </dd>
    </dl>
  </section>
</template>