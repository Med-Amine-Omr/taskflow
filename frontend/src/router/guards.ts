import type { RouteLocationNormalized, RouteLocationRaw } from 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAdmin?: boolean
  }
}

/** Refuse l'accès aux routes réservées aux administrateurs. */
export function adminGuard(
  to: Pick<RouteLocationNormalized, 'meta'>,
  isAdmin: boolean,
): true | RouteLocationRaw {
  if (to.meta.requiresAdmin && !isAdmin) {
    return { name: 'forbidden' }
  }
  return true
}