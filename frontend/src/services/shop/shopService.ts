import { apiFetch, putJson } from '../api';
import type { MerchantServiceItem, Page, ServiceCategory, ServiceListingView, ShopView } from './shopTypes';

export const getCategories = () => apiFetch<ServiceCategory[]>('/service-categories/tree');
export const getShops = (params: Record<string, string | number | undefined> = {}) => {
  const query = new URLSearchParams();
  Object.entries({ page: 0, size: 12, ...params }).forEach(([key, value]) => {
    if (value !== undefined && String(value) !== '') query.set(key, String(value));
  });
  return apiFetch<Page<ShopView>>(`/shops?${query.toString()}`);
};
export const getShop = (id: string | number) => apiFetch<ShopView>(`/shops/${id}`);
export const getShopServices = (id: string | number) => apiFetch<ServiceListingView[]>(`/shops/${id}/services`);
export const getServiceCatalog = () => apiFetch<ServiceListingView[]>('/service-listings/catalog');
export type MerchantApplicationImage = { id?: number; imageType: string; objectKey: string; mimeType?: string; fileSize?: number; sha256?: string; previewUrl?: string };
export type MerchantApplication = { id: number; applyNo?: string; realName: string; phone: string; shopName: string; intro?: string; serviceArea?: string; serviceRadiusKm?: number; addressDetail: string; province?: string; city: string; district?: string; longitude: number; latitude: number; status: string; reviewRemark?: string; images?: MerchantApplicationImage[]; serviceIds?: number[]; serviceNames?: string[]; tags?: string[]; categoryIds?: number[]; serviceItems?: MerchantServiceItem[] };
export const getMyApplication = () => apiFetch<MerchantApplication>('/merchant/applications/me');
export const getApplications = (status = 'PENDING', page = 0, size = 20) => apiFetch<{ items: MerchantApplication[]; total: number; page: number; size: number }>(`/merchant/applications?status=${status}&page=${page}&size=${size}`);
export const reviewApplication = (id: number, body: { decision: string; remark?: string }) => putJson<MerchantApplication>(`/merchant/applications/${id}/review`, body);
