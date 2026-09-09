import { apiFetch, postJson, setCsrfToken } from '../api';
import type { LoginRequest, LoginResponse, UserView } from './authTypes';

export async function initCsrf() {
  const csrf = await apiFetch<{ token: string }>('/auth/csrf');
  setCsrfToken(csrf?.token);
  return csrf;
}

export const getCurrentUser = async (): Promise<UserView> => {
  const response = await apiFetch<LoginResponse>('/auth/me');
  return { id: response.userId, username: response.username, status: 'normal', authorities: response.authorities };
};
export const login = (request: LoginRequest) => postJson<LoginResponse>('/auth/login', request);
export const register = (request: { username: string; password: string; nickname?: string }) => postJson('/auth/register', request);
/**
 * 注销后通知页面级状态（例如 AI 助手）清理会话缓存。
 * 使用 finally 确保后端已失效或网络异常时，前端也不会继续展示上一位用户的数据。
 */
export const logout = async () => {
  try {
    return await postJson('/auth/logout', {});
  } finally {
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('auth:logout'));
    }
  }
};
