export const environment = {
  production: false,
  redirect: 'http://localhost:4200',
  serverUrl: 'http://localhost:8080/api',
  keycloak: {
    url: 'http://localhost:8081',
    realm: 'module-management',
    clientId: 'module-management'
  },
  umami: {
    enabled: false,
    scriptUrl: '',
    websiteId: '',
    domains: ''
  }
};
