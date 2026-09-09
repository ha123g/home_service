import { apiFetch, postJson, putJson } from '../api';
import type { Page } from '../shop/shopTypes';
export type OrderAddressView = {
  receiverName?: string; receiverPhone?: string; province?: string; city?: string; district?: string;
  detail?: string; longitude?: number; latitude?: number;
};
export type OrderServiceView = {
  id: number; title?: string; summary?: string; description?: string; pricingUnit?: string;
  referencePrice?: number; durationMinutes?: number;
};
export type OrderStatusHistoryView = {
  id: number; fromStatus?: string; toStatus: string; operatorUserId?: number;
  reason?: string; createdTime: string;
};
export type PaymentView = {
  paymentId: number; orderNo: string; provider: string; providerPaymentId: string;
  amount: string | number; status: string; checkoutUrl?: string;
};
export type OrderView = {
  id: number; orderNo: string; userId: number; shopId: number; serviceId: number; workerId?: number;
  addressId: number; scheduledStart?: string; scheduledEnd?: string; serviceAddressSnapshot?: string;
  serviceTitleSnapshot?: string; workerNameSnapshot?: string; originAmount?: string; payableAmount?: string;
  sourceRequestId?: number; publishedTime?: string; confirmedTime?: string; status: string; remark?: string;
  expireTime?: string; paidTime?: string; cancelledTime?: string; paymentProviderId?: string;
  createdTime: string; updatedTime?: string; shopName?: string; service?: OrderServiceView;
  address?: OrderAddressView; statusHistory?: OrderStatusHistoryView[];
};
export const getOrders = (page = 0, size = 10) => apiFetch<Page<OrderView>>(`/orders?page=${page}&size=${size}`);
export const payOrder = (id: number, idempotencyKey: string) => postJson<PaymentView>(`/orders/${id}/pay`, { idempotencyKey });
export const confirmOrder = (id: number) => putJson(`/orders/${id}/confirm`);
export const createOrderFromAppointment = (body: Record<string, unknown>) => postJson<OrderView>('/orders/from-appointment', body);
export const publishOrder = (id: number, expireMinutes = 1440) => putJson<OrderView>(`/orders/${id}/publish`, { expireMinutes });
export const updateOrderStatus = (id: number, status: string, reason?: string) => putJson<OrderView>(`/orders/${id}/status`, { status, reason });
