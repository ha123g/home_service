export type ServiceCategory = { id: number; parentId?: number; name: string; children?: ServiceCategory[] };
export type ShopView = {
  id: number; shopName: string; shopLogoUrl?: string; shopIntro?: string; contactPhone?: string; serviceArea?: string; serviceRadiusKm?: number;
  addressDetail?: string; province?: string; city?: string; district?: string;
  latitude?: number; longitude?: number; distanceKm?: number; status: string;
  serviceNames?: string[]; tags?: string[]; galleryUrls?: string[];
};
export type Page<T> = { items: T[]; total: number; page: number; size: number };
export type ServiceListingView = { id: number; shopId: number; title: string; summary?: string; description?: string; pricingUnit: string; basePrice?: number; durationMinutes?: number; status: string };
export type MerchantServiceItem = { categoryId: number; categoryName?: string; title: string; summary?: string; description?: string; pricingUnit: string; basePrice: number; durationMinutes?: number; tags?: string[] };
