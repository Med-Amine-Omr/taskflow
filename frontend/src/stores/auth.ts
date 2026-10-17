import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { KeycloakTokenParsed } from 'keycloak-js'
import { logout as keycloakLogout } from '@/auth/keycloak'

export const useAuthStore = defineStore('auth', () => {
  const username = ref('')
  const fullName = ref('')
  const roles = ref<string[]>([])

  const isAdmin = computed(() => roles.value.includes('admin'))
  const displayName = computed(() => fullName.value || username.value)

  function setFromToken(token: KeycloakTokenParsed | undefined) {
    username.value = (token?.preferred_username as string | undefined) ?? ''
    fullName.value = (token?.name as string | undefined) ?? ''
    roles.value = token?.realm_access?.roles ?? []
  }

  async function logout() {
    await keycloakLogout()
  }

  return { username, fullName, roles, isAdmin, displayName, setFromToken, logout }
})