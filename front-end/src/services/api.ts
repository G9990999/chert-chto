import axios from 'axios';
import type { AuthResponse, PageResponse, PageSummary } from '../types';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT token to every request
api.interceptors.request.use((config) => {
  const stored = localStorage.getItem('mws-wiki-auth');
  if (stored) {
    try {
      const { state } = JSON.parse(stored);
      if (state?.token) {
        config.headers.Authorization = `Bearer ${state.token}`;
      }
    } catch { /* ignore */ }
  }
  return config;
});

// Auth
export const register = (data: {
  username: string;
  email: string;
  password: string;
  displayName?: string;
}): Promise<AuthResponse> =>
  api.post('/auth/register', data).then((r) => r.data);

export const login = (data: {
  username: string;
  password: string;
}): Promise<AuthResponse> =>
  api.post('/auth/login', data).then((r) => r.data);

// Pages
export const listPages = (): Promise<PageSummary[]> =>
  api.get('/pages').then((r) => r.data);

export const searchPages = (q: string): Promise<PageSummary[]> =>
  api.get('/pages/search', { params: { q } }).then((r) => r.data);

export const getPage = (id: string): Promise<PageResponse> =>
  api.get(`/pages/${id}`).then((r) => r.data);

export const createPage = (data: {
  title: string;
  content: string;
  publicPage: boolean;
  linkedPageIds: string[];
}): Promise<PageResponse> =>
  api.post('/pages', data).then((r) => r.data);

export const updatePage = (
  id: string,
  data: {
    title: string;
    content: string;
    publicPage: boolean;
    linkedPageIds: string[];
  }
): Promise<PageResponse> =>
  api.put(`/pages/${id}`, data).then((r) => r.data);

export const deletePage = (id: string): Promise<void> =>
  api.delete(`/pages/${id}`).then(() => undefined);

export const sharePage = (id: string, userIds: string[]): Promise<void> =>
  api.post(`/pages/${id}/share`, userIds).then(() => undefined);

// Tables (MWS Tables API proxy)
export const getDatasheets = (): Promise<unknown> =>
  api.get('/tables').then((r) => r.data);

export const getFields = (dstId: string): Promise<unknown> =>
  api.get(`/tables/${dstId}/fields`).then((r) => r.data);

export const getRecords = (
  dstId: string,
  pageNum = 1,
  pageSize = 100
): Promise<unknown> =>
  api
    .get(`/tables/${dstId}/records`, { params: { pageNum, pageSize } })
    .then((r) => r.data);

export const getViews = (dstId: string): Promise<unknown> =>
  api.get(`/tables/${dstId}/views`).then((r) => r.data);

export default api;
