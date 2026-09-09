import { apiFetch, postJson, putJson } from '../api';
import type { Page } from '../shop/shopTypes';

export type AppointmentServiceView = {
  id: number; title: string; summary?: string; description?: string; pricingUnit?: string;
  basePrice?: number; durationMinutes?: number;
};
export type AppointmentAddressView = {
  id: number; receiverName?: string; receiverPhone?: string; province?: string; city?: string;
  district?: string; detail?: string; longitude?: number; latitude?: number;
};
export type AppointmentStatusHistoryView = {
  id: number; fromStatus?: string; toStatus: string; operatorUserId?: number;
  reason?: string; createdTime: string;
};
export type AppointmentView = {
  id: number; requestNo: string; userId: number; shopId: number; serviceId: number; addressId: number;
  serviceTitle?: string; requirementText: string; preferredStart?: string; preferredEnd?: string;
  contactName?: string; contactPhone?: string; status: string; platformOperatorId?: number;
  platformNote?: string; cancelReason?: string; createdTime: string; updatedTime?: string;
  shopName?: string; service?: AppointmentServiceView; address?: AppointmentAddressView;
  statusHistory?: AppointmentStatusHistoryView[];
};
export const getAppointments = (page = 0, size = 10) => apiFetch<Page<AppointmentView>>(`/appointments?page=${page}&size=${size}`);
export const createAppointment = (body: Record<string, unknown>) => postJson<AppointmentView>('/appointments', body);
export const cancelAppointment = (id: number, reason?: string) => putJson(`/appointments/${id}/cancel`, reason ? { reason } : {});
export const updateAppointmentStatus = (id: number, status: string, platformNote?: string) => putJson(`/appointments/${id}/status`, { status, platformNote });
