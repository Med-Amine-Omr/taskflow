import axios, { type AxiosError } from 'axios'
import { getConfig } from '@/config'
import { getKeycloak, getValidToken } from '@/auth/keycloak'

export const http = axios.create()

// Avant chaque requête : adresse de l'API + jeton valide (renouvelé si besoin)
http.interceptors.request.use(async (request) => {
  request.baseURL = getConfig().apiUrl
  const token = await getValidToken()
  request.headers.Authorization = `Bearer ${token}`
  return request
})

// Après chaque réponse : un 401 signifie que la session n'est plus valide
http.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      getKeycloak().login()
    }
    return Promise.reject(error)
  },
)