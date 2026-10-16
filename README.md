# TaskFlow, Gestionnaire de tâches sécurisé

Application web de gestion de tâches dont l'authentification et les autorisations reposent sur Keycloak.

Stack : Vue 3 · Spring Boot 3 (Java 17+) · Keycloak · PostgreSQL · Docker

## Adresses en ligne
À compléter lors du déploiement (Partie 6).

## Identifiants de test
| Utilisateur 	| Mot de passe 	| Rôles 		|
| -------------	| -------------	| -------------	|
| alice 		| Alice@2026 	| user 			|
| bob 			| Bob@2026 		| user, admin 	|

## Architecture
| Composant 		| Technologie 	| Port local 	|
| -----------------	| -------------	| -------------	|
| Frontend 			| Vue 3 		| 5173 			|
| Identité 			| Keycloak 		| 8180 			|
| API 				| Spring Boot 3 | 8081 			|
| Base de données 	| PostgreSQL 	| 5432 			|

## Lancement en local

### Prérequis
- Docker et Docker Compose
- Ports libres : 5173, 8180, 8081, 5432

### Démarrage
1. Cloner le dépôt puis se placer dans le dossier :
   `git clone https://github.com/<ton-pseudo>/taskflow.git && cd taskflow`
2. Créer le fichier d'environnement : `cp .env.example .env`
   (adapter les mots de passe si besoin)
3. Lancer : `docker compose up -d`
4. Vérifier que les services sont `healthy` : `docker compose ps`

### Accès
- Keycloak : http://localhost:8180

### Réinitialiser complètement les données
`docker compose down -v`

## Choix techniques
- Monorepo : frontend, API et configuration dans un seul dépôt.
- Un seul serveur PostgreSQL contenant deux bases séparées (`keycloak` et
  `taskflow`), chacune avec son propre utilisateur (principe du moindre privilège).
  Les bases sont créées automatiquement au premier démarrage par un script
  d'initialisation.
- Toute la configuration passe par des variables d'environnement (`.env`, non
  versionné) ; `.env.example` documente les variables attendues.
- Keycloak tourne en mode `start-dev` en local ; le mode production est utilisé
  pour le déploiement en ligne.
- Keycloak : realm `taskflow`, client public `taskflow-front` en Authorization Code
  + PKCE (S256) ; Direct Access Grants et Implicit désactivés.
- Le rôle `user` fait partie des rôles par défaut du realm : tout nouvel utilisateur le reçoit.
- Le realm (avec les utilisateurs de test) est exporté dans `keycloak/taskflow-realm.json`
  et importé automatiquement au démarrage de Keycloak (`--import-realm`). Si le realm
  existe déjà en base, l'import est ignoré.

## Limites connues
À compléter.

## Limites de l'hébergement
À compléter lors du déploiement (Partie 6).
