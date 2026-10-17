<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const menuOpen = ref(false)

function closeMenu() {
  menuOpen.value = false
}
</script>

<template>
  <header class="navbar">
    <RouterLink to="/" class="navbar__brand" @click="closeMenu">TaskFlow</RouterLink>

    <button
      type="button"
      class="navbar__toggle"
      :aria-expanded="menuOpen"
      aria-label="Ouvrir le menu"
      @click="menuOpen = !menuOpen"
    >
      ☰
    </button>

    <nav class="navbar__menu" :class="{ 'navbar__menu--open': menuOpen }">
      <RouterLink to="/" @click="closeMenu">Tableau de bord</RouterLink>
      <RouterLink to="/tasks" @click="closeMenu">Mes tâches</RouterLink>
      <RouterLink to="/profile" @click="closeMenu">Profil</RouterLink>
      <RouterLink v-if="auth.isAdmin" to="/admin" data-test="admin-link" @click="closeMenu">
        Administration
      </RouterLink>
      <span class="navbar__user" data-test="username">{{ auth.displayName }}</span>
      <button type="button" class="btn btn--secondary" data-test="logout" @click="auth.logout()">
        Déconnexion
      </button>
    </nav>
  </header>
</template>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #1e293b;
  color: #fff;
}
.navbar__brand {
  color: #fff;
  font-weight: 700;
  font-size: 1.25rem;
  text-decoration: none;
}
.navbar__toggle {
  display: none;
  min-height: 40px;
  padding: 0.25rem 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 6px;
  background: none;
  color: #fff;
  font-size: 1.25rem;
  cursor: pointer;
}
.navbar__menu {
  display: flex;
  align-items: center;
  gap: 1rem;
}
.navbar__menu a {
  color: #cbd5e1;
  text-decoration: none;
}
.navbar__menu a.router-link-exact-active {
  color: #fff;
  font-weight: 600;
}
.navbar__user {
  font-weight: 600;
}

/* Mobile : menu repliable */
@media (max-width: 768px) {
  .navbar__toggle {
    display: block;
  }
  .navbar__menu {
    display: none;
    width: 100%;
    flex-direction: column;
    align-items: stretch;
    gap: 0.75rem;
    padding-top: 0.5rem;
  }
  .navbar__menu--open {
    display: flex;
  }
}
</style>