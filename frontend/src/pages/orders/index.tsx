import {
  Button,
  Card,
  Descriptions,
  Drawer,
  Empty,
  List,
  Modal,
  Space,
  Tag,
  Timeline,
  Typography,
  message,
} from 'antd';
import { CheckCircleOutlined, DollarOutlined, EnvironmentOutlined, EyeOutlined, ShoppingOutlined } from '@ant-design/icons';
import { useEffect, useState } from 'react';
import { apiFetch } from '../../services/api';
import { getOrders, payOrder, confirmOrder, updateOrderStatus, type OrderView, type PaymentView } from '../../services/order/orderService';
import { getCurrentUser } from '../../services/auth/authService';

const statusText: Record<string, string> = {
  PENDING_PUBLISH: '待发布',
  PENDING_PAYMENT: '待支付',
  PAID: '已支付，待确认',
  CONFIRMED: '已确认，待服务',
  IN_SERVICE: '服务中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  CLOSED: '已关闭',
};
const statusColor: Record<string, string> = { PENDING_PUBLISH: 'purple', PENDING_PAYMENT: 'gold', PAID: 'processing', CONFIRMED: 'cyan', IN_SERVICE: 'blue', COMPLETED: 'green', CANCELLED: 'default', CLOSED: 'default' };
const serviceUnitText: Record<string, string> = { ORDER: '按次', HOUR: '按小时', SQUARE_METER: '按平方米' };
const formatDate = (value?: string) => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '未设置';
const formatMoney = (value?: string | number) => value == null || value === '' ? '—' : `¥${value}`;
const roleIsMerchantOrPlatform = (authorities: string[] = []) => authorities.some((authority) =>
  ['pla_user', 'super_admin', 'sys_admin', 'aud_admin'].some((role) => authority.endsWith(role)),
);

export default function OrdersPage() {
  const [items, setItems] = useState<OrderView[]>([]);
  const [merchant, setMerchant] = useState(false);
  const [detail, setDetail] = useState<OrderView>();
  const [detailLoading, setDetailLoading] = useState(false);

  const load = () => void getOrders(0, 100)
    .then((page) => setItems(page.items || []))
    .catch((error) => message.error(error instanceof Error ? error.message : '订单加载失败'));

  useEffect(() => {
    load();
    void getCurrentUser()
      .then((user) => setMerchant(roleIsMerchantOrPlatform(user.authorities || [])))
      .catch(() => undefined);
  }, []);

  const openDetail = async (item: OrderView) => {
    setDetail(item);
    setDetailLoading(true);
    try {
      setDetail(await apiFetch<OrderView>(`/orders/${item.id}`));
    } catch (error) {
      message.error(error instanceof Error ? error.message : '订单详情加载失败');
    } finally {
      setDetailLoading(false);
    }
  };

  const notifyRefresh = () => window.dispatchEvent(new CustomEvent('notifications:refresh'));
  const pay = (item: OrderView) => Modal.confirm({
    title: '确认支付订单？',
    content: `需支付金额：${formatMoney(item.payableAmount)}`,
    okText: '确认支付',
    cancelText: '取消',
    onOk: async () => {
      try {
        const payment = await payOrder(item.id, crypto.randomUUID());
        if (payment.status !== 'SUCCESS') throw new Error('沙盒支付未完成，请稍后重试');
        Modal.success({
          title: '沙盒支付成功',
          content: <div>订单 {payment.orderNo} 已支付 {formatMoney(payment.amount)}。交易号：{payment.providerPaymentId}</div>,
          okText: '知道了',
        });
        notifyRefresh();
        load();
      } catch (error) {
        message.error(error instanceof Error ? error.message : '支付失败');
      }
    },
  });
  const confirm = (item: OrderView) => Modal.confirm({
    title: '确认订单与服务安排？',
    content: '确认报价和服务安排后，订单会进入待服务状态，商家随后可开始并完成服务。',
    okText: '确认订单',
    cancelText: '再看看',
    onOk: async () => {
      try {
        await confirmOrder(item.id);
        message.success('订单已确认');
        notifyRefresh();
        load();
      } catch (error) {
        message.error(error instanceof Error ? error.message : '确认失败');
      }
    },
  });
  const changeStatus = async (item: OrderView, status: string) => {
    try {
      await updateOrderStatus(item.id, status);
      message.success(status === 'IN_SERVICE' ? '服务已开始' : '订单已完成');
      notifyRefresh();
      load();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '状态更新失败');
    }
  };

  const addressText = (item: OrderView) => {
    const address = item.address;
    if (address) return [address.province, address.city, address.district, address.detail].filter(Boolean).join('') || '地址暂未设置';
    if (item.serviceAddressSnapshot) {
      try {
        const snapshot = JSON.parse(item.serviceAddressSnapshot) as Record<string, string>;
        return [snapshot.province, snapshot.city, snapshot.district, snapshot.detail].filter(Boolean).join('') || '地址暂未设置';
      } catch { return '地址信息暂不可用'; }
    }
    return '地址暂未设置';
  };

  return (
    <div className="page-content workflow-page">
      <div className="page-hero">
        <div>
          <Typography.Title level={2}>{merchant ? '商家订单工作台' : '我的订单'}</Typography.Title>
        </div>
        <Tag color="blue">{items.length} 条记录</Tag>
      </div>
      <Card className="soft-card workflow-card">
        <List
          dataSource={items}
          locale={{ emptyText: <Empty description="暂无订单" /> }}
          renderItem={(item) => {
            const actions = [<Button key="detail" type="link" icon={<EyeOutlined />} onClick={() => void openDetail(item)}>查看详情</Button>];
            if (!merchant && item.status === 'PENDING_PAYMENT') actions.push(<Button key="pay" type="primary" onClick={() => pay(item)}>支付</Button>);
            if (!merchant && item.status === 'PAID') actions.push(<Button key="confirm" onClick={() => confirm(item)}>确认订单</Button>);
            if (merchant && item.status === 'CONFIRMED') actions.push(<Button key="start" type="link" onClick={() => void changeStatus(item, 'IN_SERVICE')}>开始服务</Button>);
            if (merchant && item.status === 'IN_SERVICE') actions.push(<Button key="complete" type="link" onClick={() => void changeStatus(item, 'COMPLETED')}>完成订单</Button>);
            return (
              <List.Item actions={actions} className="workflow-list-item">
                <List.Item.Meta
                  avatar={<div className="workflow-icon"><DollarOutlined /></div>}
                  title={<Space wrap><Typography.Text strong>{item.serviceTitleSnapshot || item.orderNo}</Typography.Text><Tag color={statusColor[item.status]}>{statusText[item.status] || '处理中'}</Tag></Space>}
                  description={<Space direction="vertical" size={2}><span>{item.shopName || `商家编号 ${item.shopId}`} · 订单号 {item.orderNo}</span><span className="muted">{formatMoney(item.payableAmount)} · {formatDate(item.scheduledStart)}</span></Space>}
                />
              </List.Item>
            );
          }}
        />
      </Card>
      <Drawer title="订单详细信息" width={Math.min(620, typeof window === 'undefined' ? 620 : window.innerWidth - 24)} open={Boolean(detail)} onClose={() => setDetail(undefined)} loading={detailLoading}>
        {detail && <>
          <div className="detail-heading"><div><Typography.Title level={4}>{detail.serviceTitleSnapshot || '家政服务订单'}</Typography.Title><Typography.Text type="secondary">订单号：{detail.orderNo}</Typography.Text></div><Tag color={statusColor[detail.status]}>{statusText[detail.status] || '处理中'}</Tag></div>
          <Descriptions bordered column={1} size="small" items={[
            { key: 'shop', label: '服务商家', children: detail.shopName || `商家 #${detail.shopId}` },
            { key: 'service', label: '服务项目', children: detail.service ? <>{detail.service.title || detail.serviceTitleSnapshot}{detail.service.referencePrice != null ? ` · 参考价 ¥${detail.service.referencePrice} / ${serviceUnitText[detail.service.pricingUnit || 'ORDER'] || detail.service.pricingUnit}` : ''}</> : detail.serviceTitleSnapshot || '—' },
            { key: 'description', label: '服务说明', children: detail.service?.description || detail.service?.summary || '—' },
            { key: 'time', label: '服务时间', children: `${formatDate(detail.scheduledStart)}${detail.scheduledEnd ? ` 至 ${formatDate(detail.scheduledEnd)}` : ''}` },
            { key: 'amount', label: '订单金额', children: <Typography.Text strong>{formatMoney(detail.payableAmount)}</Typography.Text> },
            { key: 'address', label: <><EnvironmentOutlined /> 服务地址</>, children: addressText(detail) },
            { key: 'worker', label: '外部人员信息', children: detail.workerNameSnapshot || '由平台线下安排' },
            { key: 'remark', label: '订单备注', children: detail.remark || '暂无' },
            ...(detail.paidTime ? [{ key: 'paid', label: '支付时间', children: formatDate(detail.paidTime) }] : []),
            ...(detail.paymentProviderId ? [{ key: 'payment', label: '支付流水号', children: detail.paymentProviderId }] : []),
            { key: 'created', label: '创建时间', children: formatDate(detail.createdTime) },
          ]} />
          <Typography.Title level={5} style={{ marginTop: 24 }}>订单进度</Typography.Title>
          <Timeline items={(detail.statusHistory || []).map((history) => ({ color: history.toStatus === 'COMPLETED' ? 'green' : 'blue', dot: history.toStatus === 'COMPLETED' ? <CheckCircleOutlined /> : undefined, children: <><Typography.Text strong>{statusText[history.toStatus] || history.toStatus}</Typography.Text><div className="muted">{formatDate(history.createdTime)}{history.reason ? ` · ${history.reason}` : ''}</div></> }))} />
        </>}
      </Drawer>
    </div>
  );
}
