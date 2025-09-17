# zum starten

docker compose --env-file .env build paperless-service
docker compose --env-file .env up -d paperless-service
docker compose logs -f paperless-service
