import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, RouterLinkStub } from '@vue/test-utils'
import { createPinia, setActivePinia, type Pinia } from 'pinia'
import AppNavbar from '../AppNavbar.vue'
import { useAuthStore } from '@/stores/auth'
import { logout } from '@/auth/keycloak'

// Keycloak n'est jamais appelé pour de vrai dans les tests
vi.mock('@/auth/keycloak', () => ({ logout: vi.fn<() => Promise<void>>() }))
describe('AppNavbar', () => {
  let pinia: Pinia

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()
  })

  function mountAs(username: string, roles: string[]) {
    const auth = useAuthStore()
    auth.username = username
    auth.roles = roles
    return mount(AppNavbar, {
      global: {
        plugins: [pinia],
        stubs: { RouterLink: RouterLinkStub },
      },
    })
  }

  it("affiche le nom de l'utilisateur connecté", () => {
    const wrapper = mountAs('alice', ['user'])
    expect(wrapper.get('[data-test="username"]').text()).toBe('alice')
  })

  it('masque le lien Administration pour un utilisateur sans rôle admin', () => {
    const wrapper = mountAs('alice', ['user'])
    expect(wrapper.find('[data-test="admin-link"]').exists()).toBe(false)
  })

  it('affiche le lien Administration pour un administrateur', () => {
    const wrapper = mountAs('bob', ['user', 'admin'])
    expect(wrapper.find('[data-test="admin-link"]').exists()).toBe(true)
  })

  it('déconnecte côté Keycloak au clic sur Déconnexion', async () => {
    const wrapper = mountAs('alice', ['user'])
    await wrapper.get('[data-test="logout"]').trigger('click')
    expect(logout).toHaveBeenCalledTimes(1)
  })
})