# TaskFlow, Gestionnaire de tâches sécurisé

Application web de gestion de tâches dont l'authentification et les autorisations reposent sur Keycloak.

Stack : Vue 3 · Spring Boot 3 (Java 17+) · Keycloak · PostgreSQL · Docker

## Adresses en ligne
À compléter lors du déploiement (Partie 6).

## Identifiants de test
| Utilisateur 	| Mot de passe 	| Rôles 		|
| -------------	| -------------	| -------------	|
| alice 		| à définir 	| user 			|
| bob 			| à définir 	| user, admin 	|

## Architecture
| Composant 		| Technologie 	| Port local 	|
| -----------------	| -------------	| -------------	|
| Frontend 			| Vue 3 		| 5173 			|
| Identité 			| Keycloak 		| 8180 			|
| API 				| Spring Boot 3 | 8081 			|
| Base de données 	| PostgreSQL 	| 5432 			|

## Lancement en local
À compléter.

## Choix techniques
- Monorepo : frontend, API et configuration dans un seul dépôt.

## Limites connues
À compléter.

## Limites de l'hébergement
À compléter lors du déploiement (Partie 6).
