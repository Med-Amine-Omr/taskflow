import { describe, expect, it } from 'vitest'
import { adminGuard } from '../guards'

describe('adminGuard', () => {
  it('redirige un utilisateur non admin vers la page Accès refusé', () => {
    expect(adminGuard({ meta: { requiresAdmin: true } }, false)).toEqual({ name: 'forbidden' })
  })

  it('laisse passer un administrateur', () => {
    expect(adminGuard({ meta: { requiresAdmin: true } }, true)).toBe(true)
  })

  it('laisse passer tout le monde sur les routes non protégées', () => {
    expect(adminGuard({ meta: {} }, false)).toBe(true)
  })
})