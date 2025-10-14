# services/paperless-web/Dockerfile
ARG NGINX_VERSION=1.27-alpine
FROM nginx:${NGINX_VERSION}

# 1) Default vhost entfernen (sonst verwirrend)
RUN rm -f /etc/nginx/conf.d/default.conf

# 2) Statisches Frontend ausliefern
COPY dist/ /usr/share/nginx/html/

# 3) Eigene Hauptkonfiguration
#    (ACHTUNG: Datei muss im Build-Kontext ./services/paperless-web liegen!)
COPY nginx.conf /etc/nginx/nginx.conf

# 4) Build-Zeit-Validierung -> bricht sofort ab, wenn die Config nicht passt
#RUN nginx -t
