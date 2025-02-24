export const environment = {
  production: false,
  redirect: 'https://module-management.ase.cit.tum.de',
  serverUrl: 'https://module-management.ase.cit.tum.de/api',
  keycloak: {
    url: 'https://module-management.ase.cit.tum.de/auth',
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
