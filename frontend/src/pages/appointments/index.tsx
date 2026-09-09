import {
  Button,
  Card,
  Descriptions,
  Drawer,
  Empty,
  InputNumber,
  List,
  Modal,
  Space,
  Tag,
  Timeline,
  Typography,
  message,
} from 'antd';
import { EnvironmentOutlined, EyeOutlined, ShopOutlined } from '@ant-design/icons';
import { useEffect, useState } from 'react';
import { apiFetch } from '../../services/api';
import {
  cancelAppointment,
  getAppointments,
  type AppointmentView,
  updateAppointmentStatus,
} from '../../services/appointment/appointmentService';
import { createOrderFromAppointment, publishOrder } from '../../services/order/orderService';
import { getCurrentUser } from '../../services/auth/authService';

const statusText: Record<string, string> = {
  PENDING_PLATFORM: '预约申请中',
  CONTACTING: '商家已联系',
  WORKER_ARRANGED: '已安排服务',
  ORDER_PENDING: '待发布订单',
  CANCELLED: '已取消',
  COMPLETED: '已完成',
  CLOSED: '已关闭',
};
const statusColor: Record<string, string> = {
  PENDING_PLATFORM: 'processing',
  CONTACTING: 'gold',
  WORKER_ARRANGED: 'cyan',
  ORDER_PENDING: 'purple',
  CANCELLED: 'default',
  COMPLETED: 'green',
  CLOSED: 'default',
};
const serviceUnitText: Record<string, string> = { ORDER: '按次', HOUR: '按小时', SQUARE_METER: '按平方米' };

const formatDate = (value?: string) => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '未设置';
const formatAddress = (address?: AppointmentView['address']) => {
  if (!address) return '地址信息暂未加载';
  return [address.province, address.city, address.district, address.detail].filter(Boolean).join('') || '地址信息暂未加载';
};
const roleIsMerchantOrPlatform = (authorities: string[] = []) => authorities.some((authority) =>
  ['pla_user', 'super_admin', 'sys_admin', 'aud_admin'].some((role) => authority.endsWith(role)),
);

export default function AppointmentsPage() {
  const [items, setItems] = useState<AppointmentView[]>([]);
  const [merchant, setMerchant] = useState(false);
  const [detail, setDetail] = useState<AppointmentView>();
  const [detailLoading, setDetailLoading] = useState(false);

  const load = () => void getAppointments(0, 100)
    .then((page) => setItems(page.items || []))
    .catch((error) => message.error(error instanceof Error ? error.message : '预约加载失败'));

  useEffect(() => {
    load();
    void getCurrentUser()
      .then((user) => setMerchant(roleIsMerchantOrPlatform(user.authorities || [])))
      .catch(() => undefined);
  }, []);

  const openDetail = async (item: AppointmentView) => {
    setDetail(item);
    setDetailLoading(true);
    try {
      setDetail(await apiFetch<AppointmentView>(`/appointments/${item.id}`));
    } catch (error) {
      message.error(error instanceof Error ? error.message : '预约详情加载失败');
    } finally {
      setDetailLoading(false);
    }
  };

  const notifyRefresh = () => window.dispatchEvent(new CustomEvent('notifications:refresh'));

  const cancel = (item: AppointmentView) => Modal.confirm({
    title: '确认取消预约？',
    content: '取消后商家将不再继续处理此预约。',
    okText: '确认取消',
    cancelText: '保留预约',
    onOk: async () => {
      try {
        await cancelAppointment(item.id);
        message.success('预约已取消');
        notifyRefresh();
        load();
      } catch (error) {
        message.error(error instanceof Error ? error.message : '取消失败');
      }
    },
  });

  const process = async (item: AppointmentView, status: string) => {
    try {
      await updateAppointmentStatus(item.id, status, status === 'CONTACTING' ? '商家已收到预约，正在确认需求' : '商家已确认服务安排');
      message.success(status === 'CONTACTING' ? '已接收预约' : '已标记为安排完成');
      notifyRefresh();
      load();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '操作失败');
    }
  };

  const createOrder = (item: AppointmentView) => {
    let amount: number | null = null;
    Modal.confirm({
      title: '填写本次服务报价',
      content: <InputNumber min={0.01} precision={2} style={{ width: '100%' }} placeholder="请输入商家确认的订单金额" onChange={(value) => { amount = value; }} />,
      okText: '生成并发布订单',
      cancelText: '取消',
      onOk: async () => {
        if (!amount || amount <= 0) {
          message.warning('请输入有效报价');
          return Promise.reject();
        }
        try {
          const order = await createOrderFromAppointment({ appointmentId: item.id, amount, idempotencyKey: crypto.randomUUID() });
          await publishOrder(order.id);
          message.success('订单已发布，用户可查看并支付');
          notifyRefresh();
          load();
        } catch (error) {
          message.error(error instanceof Error ? error.message : '订单发布失败');
        }
      },
    });
  };

  return (
    <div className="page-content workflow-page">
      <div className="page-hero">
        <div>
          <Typography.Title level={2}>{merchant ? '商家预约工作台' : '我的预约'}</Typography.Title>
        </div>
        <Tag color="blue">{items.length} 条记录</Tag>
      </div>
      <Card className="soft-card workflow-card">
        <List
          dataSource={items}
          locale={{ emptyText: <Empty description={merchant ? '暂无待处理预约' : '暂无预约，先去找一家合适的商家吧'} /> }}
          renderItem={(item) => {
            const actions = [<Button key="detail" type="link" icon={<EyeOutlined />} onClick={() => void openDetail(item)}>查看详情</Button>];
            if (!merchant && ['PENDING_PLATFORM', 'CONTACTING'].includes(item.status)) actions.push(<Button key="cancel" danger type="link" onClick={() => cancel(item)}>取消预约</Button>);
            if (merchant && item.status === 'PENDING_PLATFORM') actions.push(<Button key="accept" type="link" onClick={() => void process(item, 'CONTACTING')}>接收预约</Button>);
            if (merchant && item.status === 'CONTACTING') actions.push(<Button key="arrange" type="link" onClick={() => void process(item, 'WORKER_ARRANGED')}>确认安排</Button>);
            if (merchant && item.status === 'WORKER_ARRANGED') actions.push(<Button key="order" type="link" onClick={() => createOrder(item)}>填写报价并发布订单</Button>);
            return (
              <List.Item actions={actions} className="workflow-list-item">
                <List.Item.Meta
                  avatar={<div className="workflow-icon"><ShopOutlined /></div>}
                  title={<Space wrap><Typography.Text strong>{item.serviceTitle || `预约 ${item.requestNo}`}</Typography.Text><Tag color={statusColor[item.status]}>{statusText[item.status] || '处理中'}</Tag></Space>}
                  description={<Space direction="vertical" size={2}><span>{item.shopName || `商家编号 ${item.shopId}`} · 预约编号 {item.requestNo}</span><span className="muted">{item.requirementText || '暂无需求描述'} · {formatDate(item.preferredStart)}</span></Space>}
                />
              </List.Item>
            );
          }}
        />
      </Card>
      <Drawer title="预约详细信息" width={Math.min(620, typeof window === 'undefined' ? 620 : window.innerWidth - 24)} open={Boolean(detail)} onClose={() => setDetail(undefined)} loading={detailLoading}>
        {detail && <>
          <div className="detail-heading"><div><Typography.Title level={4}>{detail.serviceTitle || '家政服务预约'}</Typography.Title><Typography.Text type="secondary">预约编号：{detail.requestNo}</Typography.Text></div><Tag color={statusColor[detail.status]}>{statusText[detail.status] || '处理中'}</Tag></div>
          <Descriptions bordered column={1} size="small" items={[
            { key: 'shop', label: '服务商家', children: detail.shopName || `商家 #${detail.shopId}` },
            { key: 'service', label: '服务项目', children: detail.service ? <>{detail.service.title}{detail.service.basePrice != null ? ` · 起步价 ¥${detail.service.basePrice} / ${serviceUnitText[detail.service.pricingUnit || 'ORDER'] || detail.service.pricingUnit}` : ''}</> : detail.serviceTitle || '—' },
            { key: 'description', label: '服务说明', children: detail.service?.description || detail.service?.summary || '—' },
            { key: 'time', label: '期望时间', children: `${formatDate(detail.preferredStart)}${detail.preferredEnd ? ` 至 ${formatDate(detail.preferredEnd)}` : ''}` },
            { key: 'requirement', label: '需求描述', children: detail.requirementText || '—' },
            { key: 'contact', label: '联系人', children: `${detail.contactName || detail.address?.receiverName || '—'}${detail.contactPhone || detail.address?.receiverPhone ? ` · ${detail.contactPhone || detail.address?.receiverPhone}` : ''}` },
            { key: 'address', label: <><EnvironmentOutlined /> 服务地址</>, children: formatAddress(detail.address) },
            { key: 'note', label: '平台/商家备注', children: detail.platformNote || '暂无' },
            ...(detail.cancelReason ? [{ key: 'cancel', label: '取消原因', children: detail.cancelReason }] : []),
            { key: 'created', label: '提交时间', children: formatDate(detail.createdTime) },
          ]} />
          <Typography.Title level={5} style={{ marginTop: 24 }}>处理进度</Typography.Title>
          <Timeline items={(detail.statusHistory || []).map((history) => ({ color: statusColor[history.toStatus] === 'green' ? 'green' : 'blue', children: <><Typography.Text strong>{statusText[history.toStatus] || history.toStatus}</Typography.Text><div className="muted">{formatDate(history.createdTime)}{history.reason ? ` · ${history.reason}` : ''}</div></> }))} />
        </>}
      </Drawer>
    </div>
  );
}
