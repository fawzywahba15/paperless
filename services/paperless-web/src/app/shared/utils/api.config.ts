// Zentrale Konfiguration für API-Endpunkte
// Nutzt relative Pfade, damit Nginx (Docker) und Proxy (Local) funktionieren.
export const API_CONFIG = {
  BASE_URL: '/api',
  ENDPOINTS: {
    DOCUMENTS: '/documents',
    SHARE: '/share'
  }
};
