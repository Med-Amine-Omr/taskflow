export interface AppConfig {
  apiUrl: string
  keycloakUrl: string
  keycloakRealm: string
  keycloakClientId: string
}

let cached: AppConfig | null = null

function required(name: string, value: string | undefined): string {
  if (!value) {
    throw new Error(`Configuration manquante : ${name}`)
  }
  return value
}

/**
 * Configuration de l'application.
 * Priorité : window.__APP_CONFIG__ (Docker / production), puis variables VITE_ (.env).
 */
export function getConfig(): AppConfig {
  if (cached) {
    return cached
  }
  const runtime = window.__APP_CONFIG__ ?? {}
  const env = import.meta.env
  cached = {
    apiUrl: required('apiUrl / VITE_API_URL', runtime.apiUrl || env.VITE_API_URL),
    keycloakUrl: required('keycloakUrl / VITE_KEYCLOAK_URL', runtime.keycloakUrl || env.VITE_KEYCLOAK_URL),
    keycloakRealm: required('keycloakRealm / VITE_KEYCLOAK_REALM', runtime.keycloakRealm || env.VITE_KEYCLOAK_REALM),
    keycloakClientId: required(
      'keycloakClientId / VITE_KEYCLOAK_CLIENT_ID',
      runtime.keycloakClientId || env.VITE_KEYCLOAK_CLIENT_ID,
    ),
  }
  return cached
}