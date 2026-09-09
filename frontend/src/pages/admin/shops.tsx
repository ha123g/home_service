import { Button, Card, Descriptions, Empty, Input, Modal, Select, Space, Table, Tag } from 'antd';
import { useEffect, useState } from 'react';
import { getShopServices, getShops } from '../../services/shop/shopService';
import type { ShopView } from '../../services/shop/shopTypes';

const statusText: Record<string, string> = { OPEN: '营业中', CLOSED: '已关闭', SUSPENDED: '已暂停' };

export default function AdminShopsPage() {
  const [items, setItems] = useState<ShopView[]>([]);
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<string>();
  const load = () => void getShops({ size: 100, keyword, status }).then((p) => setItems(p.items)).catch(() => undefined);
  useEffect(() => { load(); }, []);
  const detail = async (shop: ShopView) => {
    let serviceNames: string[] = [];
    try { serviceNames = (await getShopServices(shop.id)).map((service) => service.title).filter(Boolean); } catch { /* 详情仍可展示基础商家资料 */ }
    Modal.info({ title: `${shop.shopName} · 商家详情`, width: 680, content: <Descriptions bordered column={1} items={[
    { key: 'address', label: '地址', children: `${shop.province || ''}${shop.city || ''}${shop.district || ''}${shop.addressDetail || ''}` },
    { key: 'service', label: '服务说明', children: shop.serviceArea || '—' },
    { key: 'serviceItems', label: '服务项目', children: serviceNames.length ? serviceNames.join('、') : '—' },
    { key: 'tags', label: '服务标签', children: shop.tags?.length ? shop.tags.join('、') : '—' },
    { key: 'radius', label: '服务覆盖半径', children: shop.serviceRadiusKm ? `${shop.serviceRadiusKm} 公里` : '未设置' },
    { key: 'intro', label: '简介', children: shop.shopIntro || '—' },
    { key: 'phone', label: '联系电话', children: shop.contactPhone || '—' },
    { key: 'status', label: '状态', children: statusText[shop.status] || shop.status },
    { key: 'coordinates', label: '地图位置', children: shop.latitude != null && shop.longitude != null ? `${shop.latitude}, ${shop.longitude}` : '—' },
  ]} /> });
  };
  return <Card className="admin-page-card" title="商家管理"><Space className="admin-filter"><Input placeholder="商家名称或简介" value={keyword} onChange={(e) => setKeyword(e.target.value)} onPressEnter={load} /><Select allowClear placeholder="营业状态" value={status} onChange={setStatus} style={{ width: 150 }} options={['OPEN', 'CLOSED', 'SUSPENDED'].map((value) => ({ value, label: statusText[value] }))} /><Button type="primary" onClick={load}>查询</Button></Space><Table rowKey="id" dataSource={items} locale={{ emptyText: <Empty description="暂无商家" /> }} columns={[{ title: '商家名称', dataIndex: 'shopName' }, { title: '城市', dataIndex: 'city' }, { title: '区域', dataIndex: 'district' }, { title: '状态', dataIndex: 'status', render: (v: string) => <Tag>{statusText[v] || v}</Tag> }, { title: '覆盖半径', render: (_: unknown, shop: ShopView) => shop.serviceRadiusKm ? `${shop.serviceRadiusKm} 公里` : '未设置' }, { title: '操作', render: (_: unknown, shop: ShopView) => <Button type="link" onClick={() => detail(shop)}>查看详情</Button> }]} /></Card>;
}
