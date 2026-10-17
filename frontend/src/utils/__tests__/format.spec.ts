import { describe, expect, it } from 'vitest'
import { PRIORITY_LABELS, formatDate } from '../format'

describe('format', () => {
  it('traduit les priorités comme dans le sujet : basse, normale, haute', () => {
    expect(PRIORITY_LABELS).toEqual({ LOW: 'Basse', NORMAL: 'Normale', HIGH: 'Haute' })
  })

  it('formate une date au format français sans décalage de fuseau', () => {
    expect(formatDate('2026-10-15')).toBe('15/10/2026')
  })

  it("affiche un tiret en l'absence de date", () => {
    expect(formatDate(null)).toBe('—')
  })
})