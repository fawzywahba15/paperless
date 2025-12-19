# --- Stage 1: Build (Node.js) ---
FROM node:20-alpine as build
WORKDIR /app

# Dependencies installieren (Clean Install für Reproduzierbarkeit)
COPY package*.json ./
RUN npm ci

# Source-Code kopieren und bauen
COPY . .
# Angular baut die App (Output landet in dist/paperless-web/browser)
RUN npm run build

# --- Stage 2: Runtime (Nginx) ---
FROM nginx:1.27-alpine

# 1) Default Config löschen
RUN rm -f /etc/nginx/conf.d/default.conf

# 2) Das GEBAUTE Frontend kopieren
# WICHTIG: Angular 17+ Application Builder nutzt den Unterordner /browser
COPY --from=build /app/dist/paperless-web/browser /usr/share/nginx/html/

# 3) Eigene Nginx Config kopieren
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
