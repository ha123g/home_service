import { Layout, Menu, Typography, Button, Modal, message } from 'antd';
import { Outlet, useLocation, useNavigate } from 'umi';
import { useEffect, useState } from 'react';
import { getCurrentUser } from '../services/auth/authService';
import { hasAnyRole } from '../access';
import { logout } from '../services/auth/authService';
const { Sider, Header, Content } = Layout;
export default function AdminLayout() {
  const navigate = useNavigate(); const location = useLocation();
  const [allowed, setAllowed] = useState(false);
  useEffect(() => { getCurrentUser().then((user) => { if (!hasAnyRole(user, ['username_super_admin', 'username_sec_admin', 'username_aud_admin', 'username_sys_admin'])) { navigate('/'); return; } setAllowed(true); }).catch(() => navigate('/login')); }, [navigate]);
  const items = [{ key: '/admin/dashboard', label: '统计大屏' }, { key: '/admin/users', label: '用户管理' }, { key: '/admin/applications', label: '入驻审核' }, { key: '/admin/shops', label: '商家管理' }, { key: '/admin/rag', label: 'RAG 知识库' }, { key: '/admin/ops', label: '运维', children: [{ key: '/admin/ops/alerts', label: '告警' }, { key: '/admin/ops/logs', label: '日志' }] }];
  const handleLogout = () => Modal.confirm({ title: '确认退出登录？', onOk: async () => { try { await logout(); window.dispatchEvent(new Event('auth:logout')); navigate('/login', { replace: true }); } catch (e) { message.error(e instanceof Error ? e.message : '退出失败'); } } });
  if (!allowed) return null;
  return <Layout className="admin-layout"><Sider theme="light" width={220}><div style={{ padding: 24, fontSize: 18, fontWeight: 600, color: '#2f6f9f' }}>暖居后台</div><Menu mode="inline" selectedKeys={[location.pathname]} items={items} onClick={({ key }) => navigate(key)} /></Sider><Layout><Header style={{ background: '#fff', padding: '0 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}><Typography.Title level={4} style={{ margin: 0 }}>平台管理</Typography.Title><Button onClick={handleLogout}>退出登录</Button></Header><Content className="admin-content"><Outlet /></Content></Layout></Layout>;
}
