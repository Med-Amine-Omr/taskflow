#!/bin/sh
# Génère la configuration d'exécution du frontend à partir des variables
# d'environnement du conteneur. Exécuté automatiquement par l'image Nginx
# officielle au démarrage (scripts du dossier /docker-entrypoint.d/).
set -eu

cat > /usr/share/nginx/html/config.js <<EOF
window.__APP_CONFIG__ = {
  apiUrl: "${API_URL}",
  keycloakUrl: "${KEYCLOAK_URL}",
  keycloakRealm: "${KEYCLOAK_REALM}",
  keycloakClientId: "${KEYCLOAK_CLIENT_ID}"
};
EOF

echo "config.js généré (API : ${API_URL}, Keycloak : ${KEYCLOAK_URL})"