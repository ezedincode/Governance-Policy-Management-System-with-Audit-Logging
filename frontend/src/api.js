import axios from 'axios';

const BASE_URL = import.meta.env.VITE_GATEWAY_URL || 'http://localhost:8088';
const TOKEN_KEY = 'gpm_access_token';

export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

// Pre-configured axios instance that always routes through the gateway.
const api = axios.create({ baseURL: BASE_URL, headers: { 'Content-Type': 'application/json' } });

// Attach the access token from local storage to every outgoing request.
api.interceptors.request.use((config) => {
  const token = tokenStorage.get();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Auto-logout on 401 (expired/invalid token).
api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      tokenStorage.clear();
      if (window.location.pathname !== '/login') window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

export const decodeJwt = (token) => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    return payload;
  } catch {
    return null;
  }
};
