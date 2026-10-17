import { isAxiosError } from 'axios'
import type { ApiError } from '@/types/api'

/** Renvoie l'erreur au format unique de l'API, ou null (ex. serveur injoignable). */
export function toApiError(error: unknown): ApiError | null {
  if (isAxiosError(error)) {
    const data = error.response?.data
    if (data && typeof data === 'object' && 'status' in data) {
      return data as ApiError
    }
  }
  return null
}

/** Transforme fieldErrors en { champ: message } pour l'affichage sous chaque champ. */
export function fieldErrorsOf(error: unknown): Record<string, string> {
  const result: Record<string, string> = {}
  for (const fieldError of toApiError(error)?.fieldErrors ?? []) {
    if (!result[fieldError.field]) {
      result[fieldError.field] = fieldError.message
    }
  }
  return result
}

/** Message lisible pour l'utilisateur. */
export function messageOf(error: unknown): string {
  return toApiError(error)?.message ?? 'Le serveur est injoignable. Vérifiez votre connexion.'
}