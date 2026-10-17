# ============================================================
#  Étape 1 : build de l'application Vue (Node + npm)
# ============================================================
FROM node:22-alpine AS build
WORKDIR /app

# 1) D'abord les fichiers de dépendances : couche mise en cache
COPY package.json package-lock.json ./
RUN npm ci

# 2) Ensuite le code, puis le build de production (type-check + vite build)
COPY . .
RUN npm run build

# ============================================================
#  Étape 2 : service des fichiers statiques (Nginx)
# ============================================================
FROM nginx:1.28-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
COPY docker/40-generate-config.sh /docker-entrypoint.d/40-generate-config.sh
RUN chmod +x /docker-entrypoint.d/40-generate-config.sh

EXPOSE 80