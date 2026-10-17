import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { adminGuard } from './guards'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { title: 'Tableau de bord' },
    },
    {
      path: '/tasks',
      name: 'tasks',
      component: () => import('@/views/TasksView.vue'),
      meta: { title: 'Mes tâches' },
    },
    {
      path: '/tasks/new',
      name: 'task-create',
      component: () => import('@/views/TaskFormView.vue'),
      meta: { title: 'Nouvelle tâche' },
    },
    {
      path: '/tasks/:id/edit',
      name: 'task-edit',
      component: () => import('@/views/TaskFormView.vue'),
      props: true,
      meta: { title: 'Modifier la tâche' },
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/ProfileView.vue'),
      meta: { title: 'Profil' },
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/AdminView.vue'),
      meta: { title: 'Administration', requiresAdmin: true },
    },
    {
      path: '/forbidden',
      name: 'forbidden',
      component: () => import('@/views/ForbiddenView.vue'),
      meta: { title: 'Accès refusé' },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { title: 'Page introuvable' },
    },
  ],
})

// Vérifiée à CHAQUE navigation, y compris quand on tape l'URL directement
router.beforeEach((to) => adminGuard(to, useAuthStore().isAdmin))

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · TaskFlow` : 'TaskFlow'
})

export default router