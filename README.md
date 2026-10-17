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

### Lancer l'API en dehors de Docker (développement)
PostgreSQL et Keycloak doivent tourner (`docker compose up -d postgres keycloak`).
L'API lit automatiquement le fichier `.env` de la racine.

    cd backend
    ./mvnw spring-boot:run        # Windows : .\mvnw.cmd spring-boot:run

- API : http://localhost:8081
- Swagger UI : http://localhost:8081/swagger-ui.html
- Health check : http://localhost:8081/actuator/health

### Lancer le frontend en dehors de Docker (développement)
PostgreSQL, Keycloak et l'API doivent tourner. Vite lit les variables `VITE_*` du
fichier `.env` à la racine du dépôt.

    cd frontend
    npm install
    npm run dev

- Application : http://localhost:5173

## Choix techniques
- Monorepo : frontend, API et configuration dans un seul dépôt.
- Un seul serveur PostgreSQL contenant deux bases séparées (`keycloak` et `taskflow`), chacune avec son propre utilisateur (principe du moindre privilège). Les bases sont créées automatiquement au premier démarrage par un script d'initialisation.
- Toute la configuration passe par des variables d'environnement (`.env`, non versionné) ; `.env.example` documente les variables attendues.
- Keycloak tourne en mode `start-dev` en local ; le mode production est utilisé pour le déploiement en ligne.
- Keycloak : realm `taskflow`, client public `taskflow-front` en Authorization Code + PKCE (S256) ; Direct Access Grants et Implicit désactivés.
- Le rôle `user` fait partie des rôles par défaut du realm : tout nouvel utilisateur le reçoit.
- Le realm (avec les utilisateurs de test) est exporté dans `keycloak/taskflow-realm.json` et importé automatiquement au démarrage de Keycloak (`--import-realm`). Si le realm existe déjà en base, l'import est ignoré.
- API en couches (controller / service / repository) ; les entités ne sortent jamais du service, seuls des DTO (records Java) sont exposés.
- Schéma géré par Flyway (`db/migration`), Hibernate en mode `validate`.
- Identifiants en UUID (non devinables).
- Le propriétaire est le claim `sub` du jeton (identifiant stable) ; le nom affiché vient de `preferred_username`.
- Accès à la tâche d'un autre utilisateur : réponse 404 (et non 403) pour ne pas révéler son existence.
- Rôles lus dans `realm_access.roles` et convertis en autorités `ROLE_user` / `ROLE_admin`.
- L'API récupère les clés de Keycloak via `KEYCLOAK_JWK_SET_URI` et vérifie l'issuer public `KEYCLOAK_ISSUER_URI` (indispensable quand l'API tourne dans Docker).
- Format d'erreur unique (timestamp, status, error, message, path, fieldErrors), y compris pour les 401 et 403 produits par Spring Security.
- Statistiques : « terminée » = completed ; « en retard » = non terminée et échéance antérieure à aujourd'hui (fuseau `APP_TIMEZONE`) ; « en cours » = le reste.
- Priorités exposées en anglais (`LOW`, `NORMAL`, `HIGH`), traduites dans l'interface.
- Tri limité à une liste blanche (`dueDate`, `createdAt`), taille de page max 100.
- Tests d'intégration sur un vrai PostgreSQL (Testcontainers) plutôt que H2, pour tester les mêmes migrations Flyway et le même SQL qu'en production.
- Frontend Vue 3 (Composition API, `<script setup>`, TypeScript), Vue Router, Pinia, Axios.
- Pas de librairie de composants : CSS maison responsive (menu repliable, cartes, tableaux défilants), pour garder un code léger et des tests simples.
- `keycloak-js` initialisé avant le montage de l'application (`login-required`, PKCE S256) : aucun écran n'est accessible sans connexion.
- Jeton renouvelé avant chaque appel (`updateToken(30)`) et à son expiration.
- Garde de navigation `beforeEach` sur la route Administration (y compris accès par URL directe) ; la sécurité réelle reste assurée par l'API.
- Déconnexion via `keycloak.logout()` : la session SSO est fermée côté Keycloak.
- Configuration sans adresse en dur : variables `VITE_*` en développement, `public/config.js` généré au démarrage du conteneur en Docker.
- « 5 prochaines échéances » : tâches non terminées avec échéance, triées par date (les tâches en retard apparaissent en premier).
- Le filtre par utilisateur de l'administration liste les utilisateurs ayant au moins une tâche (issus de `/api/admin/stats`).

## Tests

### API (Spring Boot)
    cd backend
    ./mvnw verify          # Windows : .\mvnw.cmd verify

Aucune instance Keycloak n'est nécessaire : les jetons sont simulés avec `spring-security-test` et le décodeur JWT est remplacé par un mock.

- Tests web (`*WebTest`, `@WebMvcTest`) : réponses 200, 401 et 403, validation et format d'erreur unique. Ils ne nécessitent ni base de données, ni Docker.
- Tests unitaires : règle « en retard » (horloge fixe), statistiques, pagination et tri.
- Test d'intégration (`TaskApiIntegrationTest`) : parcours complet sur un vrai PostgreSQL 16 lancé par Testcontainers (Docker requis), incluant l'isolation des données entre utilisateurs et les endpoints d'administration.

### Frontend (Vue)
    cd frontend
    npm run test:unit -- --run

Tests unitaires de composants avec Vitest et Vue Test Utils (environnement jsdom), sans Keycloak (module simulé) ni API :
- `AppNavbar` : nom affiché, lien Administration réservé aux admins, déconnexion Keycloak ;
- `TaskItem` : mise en évidence des tâches en retard, changement de statut, suppression ;
- `TaskForm` : validation, données envoyées, affichage des erreurs de l'API par champ ;
- `ConfirmDialog` : confirmation et annulation ;
- garde de navigation : redirection des non-admins (y compris par URL directe) ;
- utilitaires : lecture du format d'erreur unique, libellés et dates.

## Limites connues
À compléter.

## Limites de l'hébergement
À compléter lors du déploiement (Partie 6).
