import Keycloak from 'keycloak-js'
import { getConfig } from '@/config'

let keycloak: Keycloak | null = null

/** Instance unique de Keycloak (équivalent d'un service singleton Angular). */
export function getKeycloak(): Keycloak {
  if (!keycloak) {
    const config = getConfig()
    keycloak = new Keycloak({
      url: config.keycloakUrl,
      realm: config.keycloakRealm,
      clientId: config.keycloakClientId,
    })
  }
  return keycloak
}

/** Connexion obligatoire avant l'affichage de l'application. */
export async function initKeycloak(): Promise<void> {
  const kc = getKeycloak()
  await kc.init({
    onLoad: 'login-required',
    pkceMethod: 'S256',
    checkLoginIframe: false,
  })
  // Renouvellement en tâche de fond quand le jeton expire
  kc.onTokenExpired = () => {
    kc.updateToken(30).catch(() => kc.login())
  }
}

/** Renvoie un jeton valide, en le renouvelant s'il expire dans moins de 30 secondes. */
export async function getValidToken(): Promise<string> {
  const kc = getKeycloak()
  try {
    await kc.updateToken(30)
  } catch {
    await kc.login()
    throw new Error('Session expirée')
  }
  if (!kc.token) {
    throw new Error('Jeton absent')
  }
  return kc.token
}

/** Déconnexion côté Keycloak (fin de la session SSO), puis retour à l'application. */
export function logout(): Promise<void> {
  return getKeycloak().logout({ redirectUri: `${window.location.origin}/` })
}
