import { Badge, Button, Dropdown, Layout, Modal, Space, Typography, message } from 'antd';
import type { MenuProps } from 'antd';
import { BellOutlined, LogoutOutlined, ProfileOutlined, RobotOutlined, ScheduleOutlined, ShopOutlined, ShoppingOutlined, UserOutlined } from '@ant-design/icons';
import { Outlet, useNavigate } from 'umi';
import { useEffect, useState } from 'react';
import AgentPanel from '../components/AgentPanel';
import { logout } from '../services/auth/authService';
import { getAppointments } from '../services/appointment/appointmentService';
import { getOrders } from '../services/order/orderService';
const { Header, Content } = Layout;

const pendingAppointmentStatuses = new Set(['PENDING_PLATFORM', 'CONTACTING', 'WORKER_ARRANGED']);
const pendingOrderStatuses = new Set(['PENDING_PAYMENT', 'PAID', 'CONFIRMED', 'IN_SERVICE']);

export default function UserLayout() {
  const navigate = useNavigate();
  const [agentOpen, setAgentOpen] = useState(() => typeof window !== 'undefined' && sessionStorage.getItem('agent.open') === 'true');
  const [notificationCount, setNotificationCount] = useState(0);
  useEffect(() => {
    if (typeof window !== 'undefined') sessionStorage.setItem('agent.open', String(agentOpen));
  }, [agentOpen]);
  useEffect(() => {
    let active = true;
    const refreshNotifications = async () => {
      const [appointments, orders] = await Promise.all([
        getAppointments(0, 100).catch(() => undefined),
        getOrders(0, 100).catch(() => undefined),
      ]);
      if (!active) return;
      const appointmentCount = (appointments?.items || []).filter((item) => pendingAppointmentStatuses.has(item.status)).length;
      const orderCount = (orders?.items || []).filter((item) => pendingOrderStatuses.has(item.status)).length;
      setNotificationCount(appointmentCount + orderCount);
    };
    void refreshNotifications();
    window.addEventListener('notifications:refresh', refreshNotifications);
    window.addEventListener('focus', refreshNotifications);
    const timer = window.setInterval(refreshNotifications, 60_000);
    return () => {
      active = false;
      window.removeEventListener('notifications:refresh', refreshNotifications);
      window.removeEventListener('focus', refreshNotifications);
      window.clearInterval(timer);
    };
  }, []);
  const myItems: MenuProps['items'] = [
    { key: 'appointments', icon: <ScheduleOutlined />, label: '我的预约' },
    { key: 'orders', icon: <ShoppingOutlined />, label: '我的订单' },
    { key: 'profile', icon: <ProfileOutlined />, label: '个人信息' },
    { key: 'merchant/apply', icon: <ShopOutlined />, label: '商家入驻' },
    { type: 'divider' },
    { key: 'logout', icon: <LogoutOutlined />, danger: true, label: '退出登录' },
  ];
  const onMyClick: MenuProps['onClick'] = async ({ key }) => {
    if (key === 'logout') {
      Modal.confirm({ title: '确认退出登录？', content: '退出后需要重新登录才能继续使用。', okText: '退出登录', cancelText: '取消', onOk: async () => { try { await logout(); message.success('已退出登录'); navigate('/login', { replace: true }); } catch (error) { message.error(error instanceof Error ? error.message : '退出登录失败'); } } });
      return;
    }
    navigate(`/${key}`);
  };
  return <Layout className={`page-shell ${agentOpen ? 'agent-layout-open' : ''}`}>
    <Header className="user-header" style={{ background: '#fff', display: 'flex', alignItems: 'center', padding: '0 24px', borderBottom: '1px solid #edf1f6' }}>
      <Typography.Title level={4} style={{ margin: 0, color: '#2f6f9f', cursor: 'pointer' }} onClick={() => navigate('/')}>暖居家政</Typography.Title>
      <div style={{ flex: 1 }} />
      <Space size={8}>
        <Badge count={notificationCount || undefined} overflowCount={99} size="small"><Button type="text" aria-label="消息" icon={<BellOutlined />} onClick={() => navigate('/messages')} /></Badge>
        <Dropdown menu={{ items: myItems, onClick: onMyClick }} trigger={['click']} placement="bottomRight">
          <Button type="text" icon={<UserOutlined />}>我的</Button>
        </Dropdown>
        <Button type={agentOpen ? 'default' : 'link'} icon={<RobotOutlined />} onClick={() => setAgentOpen((value) => !value)}>{agentOpen ? '收起助手' : '家政助手'}</Button>
      </Space>
    </Header>
    <div className="user-workspace">
      <Content className="user-content"><Outlet /></Content>
      <AgentPanel open={agentOpen} onClose={() => setAgentOpen(false)} />
    </div>
  </Layout>;
}
