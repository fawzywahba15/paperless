# Wichtig: ARG vor FROM, sonst kommt der Build-Arg nicht an
ARG NGINX_VERSION=1.27-alpine
FROM nginx:${NGINX_VERSION}

# Wir sind im Kontext ./services/paperless-web,
# d.h. "dist/" existiert direkt hier
COPY dist/ /usr/share/nginx/html/

# Diese Datei liegt relativ zum Repo-Root unter compose/nginx/nginx.conf.
# Weil unser Kontext NICHT das Repo-Root ist, kopieren wir sie zuerst rein:
# (Alternative: lege eine nginx.conf-Kopie in services/paperless-web/nginx.conf)
COPY nginx.conf /etc/nginx/nginx.conf
