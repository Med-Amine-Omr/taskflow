#!/bin/bash
# Crée les bases et utilisateurs dédiés à Keycloak et à l'API TaskFlow.
# Exécuté automatiquement par l'image PostgreSQL au PREMIER démarrage uniquement.
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER $KEYCLOAK_DB_USER WITH PASSWORD '$KEYCLOAK_DB_PASSWORD';
    CREATE DATABASE $KEYCLOAK_DB_NAME OWNER $KEYCLOAK_DB_USER;

    CREATE USER $TASKFLOW_DB_USER WITH PASSWORD '$TASKFLOW_DB_PASSWORD';
    CREATE DATABASE $TASKFLOW_DB_NAME OWNER $TASKFLOW_DB_USER;
EOSQL