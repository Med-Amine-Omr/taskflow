import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { getKeycloak, initKeycloak } from './auth/keycloak'
import { useAuthStore } from './stores/auth'

async function bootstrap() {
  // 1) Connexion obligatoire AVANT de créer l'application
  await initKeycloak()

  // 2) Création de l'application
  const app = createApp(App)
  const pinia = createPinia()
  app.use(pinia)
  useAuthStore(pinia).setFromToken(getKeycloak().tokenParsed)

  // 3) Le routeur après Pinia : sa garde a besoin du store
  app.use(router)
  app.mount('#app')
}

bootstrap().catch((error) => {
  console.error(error)
  document.body.innerHTML =
    '<p style="padding:1rem;font-family:sans-serif">Impossible de contacter le serveur ' +
    "d'authentification. Réessayez plus tard.</p>"
})