// Centralized API service. Every backend call goes through here so the base
// URL, JWT attachment and error handling are defined once.
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const TOKEN_KEY = 'ec_token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

export class ApiError extends Error {
  constructor(message, status, fieldErrors) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors || null;
  }
}

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const token = getToken();
  if (auth && token) headers['Authorization'] = `Bearer ${token}`;

  let response;
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError('Cannot reach the server. Is the backend running?', 0);
  }

  if (response.status === 204) return null;

  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = null;
    }
  }

  if (!response.ok) {
    if (response.status === 401) {
      setToken(null);
      window.dispatchEvent(new Event('expertconnect:unauthorized'));
    }
    const message =
      (data && typeof data === 'object' && (data.message || data.error)) ||
      ({400:'Please check the information and try again.',401:'Your session has expired. Please log in again.',403:'You do not have permission to do that.',404:'The requested item could not be found.',409:'This request conflicts with existing information.',500:'The server could not complete the request.'}[response.status] || `Request failed (${response.status}).`);
    const fieldErrors = data && typeof data === 'object' ? data.errors : null;
    throw new ApiError(message, response.status, fieldErrors);
  }

  return data;
}

export const api = {
  auth: {
    register: (payload) => request('/api/auth/register', { method: 'POST', body: payload, auth: false }),
    login: (payload) => request('/api/auth/login', { method: 'POST', body: payload, auth: false }),
  },
  users: {
    me: () => request('/api/users/me'),
    all: () => request('/api/users'),
    byId: (id) => request(`/api/users/${id}`),
  },
  questions: {
    list: () => request('/api/questions', { auth: false }),
    byId: (id) => request(`/api/questions/${id}`, { auth: false }),
    byCategory: (category) => request(`/api/questions/category/${category}`, { auth: false }),
    byUser: (userId) => request(`/api/questions/user/${userId}`, { auth: false }),
    create: (payload) => request('/api/questions', { method: 'POST', body: payload }),
    update: (id, payload) => request(`/api/questions/${id}`, { method: 'PUT', body: payload }),
    remove: (id) => request(`/api/questions/${id}`, { method: 'DELETE' }),
  },
  experts: {
    apply: (payload) => request('/api/experts/apply', { method: 'POST', body: payload }),
    mine: () => request('/api/experts/me'),
    all: () => request('/api/experts'),
    byStatus: (status) => request(`/api/experts/status/${status}`),
    byId: (id) => request(`/api/experts/${id}`),
    approve: (id) => request(`/api/experts/${id}/approve`, { method: 'PUT' }),
    reject: (id) => request(`/api/experts/${id}/reject`, { method: 'PUT' }),
  },
  answers: {
    forQuestion: (questionId) => request(`/api/questions/${questionId}/answers`, { auth: false }),
    create: (questionId, payload) =>
      request(`/api/questions/${questionId}/answers`, { method: 'POST', body: payload }),
    update: (answerId, payload) => request(`/api/answers/${answerId}`, { method: 'PUT', body: payload }),
    remove: (answerId) => request(`/api/answers/${answerId}`, { method: 'DELETE' }),
    accept: (questionId, answerId) =>
      request(`/api/questions/${questionId}/answers/${answerId}/accept`, { method: 'PUT' }),
  },
};
