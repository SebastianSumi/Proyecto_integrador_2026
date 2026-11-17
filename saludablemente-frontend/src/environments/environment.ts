export const environment = {
  apiBaseUrl: 'http://localhost:8080',

  keycloak: {
    url: 'http://localhost:8180',
    realm: 'saludablemente-realm-dev',
    clientId: 'saludablemente-frontend',
  },

  minio: {
    publicUrl: 'http://localhost:9000',
    bucketNoticias: 'saludablemente',
    prefijoNoticias: 'noticias',
  },
};
