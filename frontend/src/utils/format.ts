import type { TaskPriority } from '@/types/api'

/** Libellés du sujet : basse, normale, haute. */
export const PRIORITY_LABELS: Record<TaskPriority, string> = {
  LOW: 'Basse',
  NORMAL: 'Normale',
  HIGH: 'Haute',
}

/** "2026-10-15" -> "15/10/2026" */
export function formatDate(isoDate: string | null): string {
  if (!isoDate) {
    return '—'
  }
  const [year, month, day] = isoDate.split('-').map(Number)
  if (!year || !month || !day) {
    return isoDate // format inattendu : on l'affiche tel quel plutôt que planter
  }
  return new Date(year, month - 1, day).toLocaleDateString('fr-FR')
}

/** Date-heure ISO -> "15/10/2026 14:30" */
export function formatDateTime(isoDateTime: string): string {
  return new Date(isoDateTime).toLocaleString('fr-FR', { dateStyle: 'short', timeStyle: 'short' })
}