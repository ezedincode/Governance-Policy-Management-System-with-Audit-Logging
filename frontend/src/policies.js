import api from './api';

// All calls go through the gateway; the bearer token is attached by the interceptor.
export const policiesApi = {
  list: () => api.get('/policies').then((r) => r.data),
  get: (id) => api.get(`/policies/${id}`).then((r) => r.data),
  create: ({ title, description, createdBy }) =>
    api.post('/policies', { title, description, createdBy }).then((r) => r.data),
  submit: (id) => api.post(`/policies/${id}/submit`).then((r) => r.data),
  approve: (id) => api.post(`/policies/${id}/approve`).then((r) => r.data),
  reject: (id) => api.post(`/policies/${id}/reject`).then((r) => r.data),
};
