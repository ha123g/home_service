import { postJson } from '../api';
export type AgentResponse = { route: string; answer: string; componentType?: string; schemaVersion?: string; data?: unknown; citations?: { title?: string; source?: string; content?: string }[]; requestId?: string };
export const chatAgent = (message: string, sessionId: string, location?: { latitude: number; longitude: number }) => postJson<AgentResponse>('/agent/chat', { message, sessionId, ...(location || {}) });
