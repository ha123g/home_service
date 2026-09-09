export type ApiEnvelope<T> = { code: string; message: string; data: T };

let csrfToken = '';

export const setCsrfToken = (token: string) => { csrfToken = token || ''; };

export const getCsrfToken = () => csrfToken;

export async function apiFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  if (init.body && !(init.body instanceof FormData) && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
  const method = (init.method || 'GET').toUpperCase();
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method) && csrfToken) headers.set('X-XSRF-TOKEN', csrfToken);
  let response: Response;
  try {
    response = await fetch(`/api${path}`, { ...init, headers, credentials: 'include' });
  } catch (error) {
    // A network failure means the backend/proxy is unavailable. Let the root
    // layout present the 502 page instead of leaving
    // an unhandled promise rejection in a business page.
    window.dispatchEvent(new CustomEvent('backend:unavailable'));
    throw new Error('后端服务暂不可用，请稍后重试', { cause: error });
  }
  let payload: ApiEnvelope<T> | undefined;
  try { payload = await response.json(); } catch { /* empty response */ }
  if (response.status === 401) {
    if (path === '/auth/login') {
      throw new Error(payload?.message || '账号或密码错误');
    }
    // 会话已由后端判定失效时，立即清理页面中的用户私有缓存（包括 AI 对话）。
    window.dispatchEvent(new CustomEvent('auth:logout'));
    window.dispatchEvent(new CustomEvent('auth:expired'));
    throw new Error('登录已失效，请重新登录');
  }
  // Umi's development proxy returns 502/503/504 when the Spring Boot
  // backend is stopped. Surface the same application-level event as a real
  // network failure so the root route can show the 502 page.
  // 只有代理/网关没有返回项目统一 JSON 时，才认为整个后端不可用。
  // 高德、模型、COS 等下游依赖的 502/503 是正常业务错误，应显示其明确消息。
  if ([502, 503, 504].includes(response.status) && !payload) {
    window.dispatchEvent(new CustomEvent('backend:unavailable'));
    throw new Error('后端服务暂不可用，请稍后重试');
  }
  if (!response.ok || (payload && payload.code !== '0000')) {
    throw new Error(payload?.message || `请求失败（${response.status}）`);
  }
  return (payload?.data ?? payload) as T;
}

export const postJson = <T>(path: string, body: unknown) =>
  apiFetch<T>(path, { method: 'POST', body: JSON.stringify(body) });
export const putJson = <T>(path: string, body: unknown = {}) =>
  apiFetch<T>(path, { method: 'PUT', body: JSON.stringify(body) });

export type FileUploadView = { objectKey: string; purpose: string; mimeType: string; fileSize: number; sha256: string; previewUrl: string };
export const uploadFile = (file: File, purpose: string) => {
  const body = new FormData();
  body.append('file', file);
  body.append('purpose', purpose);
  return apiFetch<FileUploadView>('/files', { method: 'POST', body });
};
