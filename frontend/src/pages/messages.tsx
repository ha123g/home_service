import { Button, Card, Empty, List, Space, Tag, Typography } from 'antd';
import { BellOutlined, ScheduleOutlined, ShoppingOutlined } from '@ant-design/icons';
import { useEffect, useState } from 'react';
import { useNavigate } from 'umi';
import { getAppointments, type AppointmentView } from '../services/appointment/appointmentService';
import { getOrders, type OrderView } from '../services/order/orderService';

export default function MessagesPage() {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState<AppointmentView[]>([]);
  const [orders, setOrders] = useState<OrderView[]>([]);
  useEffect(() => { void getAppointments(0, 100).then((data) => setAppointments(data.items || [])).catch(() => undefined); void getOrders(0, 100).then((data) => setOrders(data.items || [])).catch(() => undefined); }, []);
  const notices = [
    ...appointments.filter((item) => ['PENDING_PLATFORM', 'CONTACTING', 'WORKER_ARRANGED'].includes(item.status)).map((item) => ({ key: `a-${item.id}`, text: `预约「${item.serviceTitle || item.requestNo}」${item.status === 'PENDING_PLATFORM' ? '等待商家接收' : '状态有更新'}`, tag: '预约', onClick: () => navigate('/appointments') })),
    ...orders.filter((item) => ['PENDING_PAYMENT', 'PAID', 'CONFIRMED', 'IN_SERVICE'].includes(item.status)).map((item) => ({ key: `o-${item.id}`, text: `订单「${item.serviceTitleSnapshot || item.orderNo}」有待处理事项`, tag: '订单', onClick: () => navigate('/orders') })),
  ];
  return <div className="page-content"><Card className="soft-card"><Typography.Title level={3}><BellOutlined /> 消息中心</Typography.Title><Typography.Paragraph className="muted">预约和订单状态变化会在这里提示，点击可直接处理。</Typography.Paragraph>{notices.length ? <List dataSource={notices} renderItem={(notice) => <List.Item actions={[<Button type="link" onClick={notice.onClick}>查看</Button>]}><List.Item.Meta title={notice.text} /><Tag>{notice.tag}</Tag></List.Item>} /> : <Empty description="暂无待处理消息" image={Empty.PRESENTED_IMAGE_SIMPLE} />}<Space style={{ marginTop: 16 }}><Button icon={<ScheduleOutlined />} onClick={() => navigate('/appointments')}>查看预约</Button><Button type="primary" icon={<ShoppingOutlined />} onClick={() => navigate('/orders')}>查看订单</Button></Space></Card></div>;
}
