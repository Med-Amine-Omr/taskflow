# TaskFlow, Gestionnaire de tâches sécurisé

![CI](https://github.com/Med-Amine-Omr/taskflow/actions/workflows/ci.yml/badge.svg)

Application web de gestion de tâches dont l'authentification et les autorisations reposent sur Keycloak. Un utilisateur gère ses propres tâches ; un administrateur consulte et supprime les tâches de tous les utilisateurs.

Stack : Vue 3 · Spring Boot 3 (Java 21) · Keycloak 26 · PostgreSQL 16 · Docker

## Sommaire
- [Adresses en ligne](#adresses-en-ligne)
- [Identifiants de test](#identifiants-de-test)
- [Architecture](#architecture)
- [Lancement en local](#lancement-en-local)
- [Tests](#tests)
- [Intégration continue](#intégration-continue)
- [Déploiement](#déploiement)
- [Choix techniques](#choix-techniques)
- [Limites connues](#limites-connues)
- [Limites de l'hébergement](#limites-de-lhébergement)

## Adresses en ligne

| Service 				| Adresse 												|
| --------------------- | -----------------------------------------------------	|
| Application 			| https://taskflow-front-g71y.onrender.com 				|
| API 					| https://taskflow-api-ycma.onrender.com 				|
| Documentation OpenAPI | https://taskflow-api-ycma.onrender.com/swagger-ui.html|
| Health check de l'API | https://taskflow-api-ycma.onrender.com/actuator/health|
| Keycloak 				| https://taskflow-keycloak.onrender.com 				|

> Premier accès : l'API et Keycloak sont hébergés gratuitement et s'endorment après 15 minutes d'inactivité. Le premier chargement peut prendre plusieurs minutes (surtout Keycloak). Les accès suivants sont rapides.

## Identifiants de test
| Utilisateur 	| Mot de passe 	| Rôles 		| Accès 							|
| -------------	| -------------	| -------------	| ---------------------------------	|
| alice 		| Alice@2026 	| user 			| Ses propres tâches 				|
| bob 			| Bob@2026 		| user, admin 	| Ses tâches + page Administration 	|

Ces comptes sont valables en local comme en ligne (ils sont dans le realm importé).

## Architecture
| Composant 		| Technologie 	| Port local 	|
| -----------------	| -------------	| -------------	|
| Frontend | Vue 3 (Vite, Pinia, Vue Router) servi par Nginx | 5173 |
| Identité | Keycloak 26 (realm `taskflow`) | 8180 |
| API 				| Spring Boot 3 | 8081 			|
| Base de données 	| PostgreSQL 16	| 5432 			|

Flux d'authentification : le frontend redirige vers Keycloak (Authorization Code + PKCE), reçoit un jeton JWT, l'envoie à l'API à chaque requête (`Authorization: Bearer`) ; l'API vérifie la signature, l'émetteur et les rôles (`realm_access.roles`).

### Structure du dépôt
    taskflow/
    ├── backend/            API Spring Boot (Dockerfile, migrations Flyway, tests)
    ├── frontend/           Application Vue 3 (Dockerfile, Nginx, tests Vitest)
    ├── keycloak/           Export du realm + image Keycloak de production
    ├── docker/postgres/    Script de création des bases locales
    ├── .github/workflows/  Intégration continue
    ├── docker-compose.yml  Lancement complet en une commande
    └── .env.example        Modèle de configuration

### Endpoints de l'API

| Méthode | URL | Rôle | Description |
| --- | --- | --- | --- |
| GET | /api/me | user | Utilisateur connecté |
| GET | /api/tasks | user | Mes tâches (pagination, filtres `completed`, `priority`, tri `dueDate`/`createdAt`) |
| GET | /api/tasks/{id} | user | Détail d'une de mes tâches |
| POST | /api/tasks | user | Création |
| PUT | /api/tasks/{id} | user | Modification |
| PATCH | /api/tasks/{id}/toggle | user | Changement de statut |
| DELETE | /api/tasks/{id} | user | Suppression |
| GET | /api/tasks/stats | user | Terminées / en cours / en retard |
| GET | /api/admin/tasks | admin | Toutes les tâches (filtre `ownerId`) |
| DELETE | /api/admin/tasks/{id} | admin | Suppression de n'importe quelle tâche |
| GET | /api/admin/stats | admin | Statistiques par utilisateur et par statut |

Format d'erreur unique :

    { "timestamp": "...", "status": 400, "error": "Bad Request",
      "message": "Données invalides", "path": "/api/tasks",
      "fieldErrors": [ { "field": "title", "message": "Le titre est obligatoire" } ] }

## Lancement en local

### Prérequis
- Docker Desktop (ou Docker Engine) avec Docker Compose, c'est-à-dire la commande `docker compose` (et non l'ancienne commande `docker-compose`)
- Ports libres : 5173, 8180, 8081, 5432

### Démarrage (une seule commande)
    git clone https://github.com/Med-Amine-Omr/taskflow.git
    cd taskflow
    cp .env.example .env
    docker compose up -d --build

Le premier démarrage prend 2 à 3 minutes (construction des images). Les quatre services doivent apparaître `healthy` :

    docker compose ps

| Service 								| Adresse 																|
| ------------------------------------- | --------------------------------------------------------------------- |
| Application 							| http://localhost:5173 												|
| API 									| http://localhost:8081 												|
| Documentation OpenAPI (Swagger UI) 	| http://localhost:8081/swagger-ui.html 								|
| Health check de l'API 				| http://localhost:8081/actuator/health 								|
| Console Keycloak 						| http://localhost:8180 (identifiants `KC_BOOTSTRAP_ADMIN_*` du `.env`)	|

Au démarrage, la base PostgreSQL crée les bases `keycloak` et `taskflow`, Keycloak importe le realm `taskflow` (avec alice et bob), et l'API applique ses migrations Flyway.

### Arrêter / réinitialiser
    docker compose down        # arrête (les données sont conservées)
    docker compose down -v     # arrête et supprime toutes les données

### Mode développement (rechargement à chaud)
    docker compose up -d postgres keycloak
    cd backend  && ./mvnw spring-boot:run     # Windows : .\mvnw.cmd spring-boot:run
    cd frontend && npm install && npm run dev

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
- Images Docker multi-étapes : JDK/Maven et Node uniquement pour la compilation ; images finales JRE 21 (utilisateur non root) et Nginx.
- Frontend : une seule image pour tous les environnements ; `config.js` est généré au démarrage du conteneur à partir de ses variables d'environnement.
- Nginx : repli vers `index.html` pour les routes de l'application monopage, cache long des fichiers versionnés, aucun cache pour `index.html` et `config.js`.
- Health checks Docker sur les quatre services (API : `/actuator/health`) et démarrage ordonné (`depends_on: service_healthy`) : PostgreSQL → Keycloak → API → frontend.
- Dans Docker, l'API récupère les clés de Keycloak via le réseau interne (`keycloak:8080`) tout en validant l'issuer public (`localhost:8180`).
- Variables sensibles obligatoires (`${VAR:?...}`) : Compose refuse de démarrer sans `.env`.

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

## Intégration continue

GitHub Actions (`.github/workflows/ci.yml`), à chaque push sur toutes les branches :
1. **API** : `./mvnw verify` (compilation et tous les tests, dont le test d'intégration PostgreSQL via Testcontainers) ; rapports de tests publiés en artefact.
2. **Frontend** (en parallèle) : lint, vérification TypeScript, tests de composants (Vitest), build de production.
3. **Images Docker** : construction des deux images, si les deux jobs précédents ont réussi.

## Déploiement

Hébergement sur Render (offre gratuite), région Frankfurt, HTTPS fourni par Render :
- Frontend : site statique (build Vite ; adresses injectées via les variables `VITE_*`) ;
- API et Keycloak : services Docker construits depuis `backend/Dockerfile` et `keycloak/Dockerfile` (Keycloak pré-construit, démarré en mode production) ;
- PostgreSQL : base Render, schéma `public` pour l'API, schéma `keycloak` pour Keycloak.

Le realm est importé au premier démarrage de Keycloak ; ses URL (`TASKFLOW_FRONT_URL`, `TASKFLOW_API_URL`) sont injectées par variables d'environnement.

Déploiement automatique : chaque service est en mode *Auto-Deploy : After CI Checks Pass* sur la branche `main`. Après un push, Render attend la réussite de toutes les vérifications GitHub Actions avant de déployer ; si un test échoue, rien n'est déployé.

Secrets : uniquement dans les variables d'environnement de Render (mot de passe de la base, compte admin Keycloak). Aucun secret dans le dépôt ni dans son historique (vérifié avec Gitleaks).

## Limites connues

- Pas d'inscription libre : les comptes sont créés dans Keycloak (le rôle `user` est attribué automatiquement à tout nouvel utilisateur).
- Le filtre par utilisateur de l'administration ne liste que les utilisateurs ayant au moins une tâche (issus de `/api/admin/stats`) ; il n'y a pas d'endpoint de liste des  utilisateurs.
- L'administrateur peut consulter et supprimer les tâches des autres, mais pas les modifier (conformément au sujet).
- La règle « en retard » dépend du fuseau `APP_TIMEZONE` (Europe/Paris par défaut).
- Le masquage du lien Administration côté frontend est un confort d'affichage ; la sécurité est assurée par l'API (403).
- En local, Keycloak tourne en mode `start-dev` (HTTP) ; le mode production est utilisé en ligne.
- Pas de tests de bout en bout (navigateur), non demandés par le sujet.
- Pas de limitation du nombre de requêtes (rate limiting) sur l'API.

## Limites de l'hébergement

- Mise en veille des services web gratuits après 15 minutes sans trafic ; réveil en environ une minute (plusieurs minutes pour Keycloak sur 0,1 CPU).
- 512 Mo de RAM et 0,1 CPU par service : démarrages lents, Keycloak à la limite de la mémoire.
- 750 heures d'instance gratuites par mois pour l'ensemble des services.
- Base PostgreSQL gratuite : 1 Go, sans sauvegarde, **expire le JJ/MM/AAAA** (30 jours après sa création le JJ/MM/AAAA).
- Une seule base gratuite : Keycloak et l'API partagent la base et l'utilisateur PostgreSQL (séparation par schémas). En local, ce sont deux bases et deux utilisateurs distincts.
