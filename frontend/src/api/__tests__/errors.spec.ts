import { describe, expect, it } from 'vitest'
import { fieldErrorsOf, messageOf } from '../errors'

function axiosErrorWith(data: unknown) {
  return { isAxiosError: true, response: { data } }
}

describe("lecture du format d'erreur de l'API", () => {
  const apiError = {
    timestamp: '2026-10-17T10:00:00Z',
    status: 400,
    error: 'Bad Request',
    message: 'Données invalides',
    path: '/api/tasks',
    fieldErrors: [
      { field: 'title', message: 'Le titre est obligatoire' },
      { field: 'priority', message: 'Valeur invalide' },
    ],
  }

  it('transforme fieldErrors en dictionnaire champ -> message', () => {
    expect(fieldErrorsOf(axiosErrorWith(apiError))).toEqual({
      title: 'Le titre est obligatoire',
      priority: 'Valeur invalide',
    })
  })

  it("renvoie le message de l'API", () => {
    expect(messageOf(axiosErrorWith(apiError))).toBe('Données invalides')
  })

  it('renvoie un message générique si le serveur est injoignable', () => {
    expect(messageOf(new Error('Network Error'))).toBe('Le serveur est injoignable. Vérifiez votre connexion.')
    expect(fieldErrorsOf(new Error('Network Error'))).toEqual({})
  })
})