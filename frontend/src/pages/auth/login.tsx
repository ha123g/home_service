import { Button, Card, Form, Input, Typography, message } from 'antd';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { history, Link } from 'umi';
import { login } from '../../services/auth/authService';
import { hasAnyRole } from '../../access';
export default function LoginPage() {
  const submit = async (values: { username: string; password: string }) => { try { const result = await login(values); message.success('登录成功'); const user = { id: result.userId, username: result.username, authorities: result.authorities }; history.replace(hasAnyRole(user, ['username_super_admin', 'username_sec_admin', 'username_aud_admin', 'username_sys_admin']) ? '/admin/dashboard' : '/'); } catch (error) { message.error(error instanceof Error ? error.message : '登录失败'); } };
  return <div className="auth-page"><Card className="soft-card auth-card"><Typography.Title level={2}>暖居家政</Typography.Title><Typography.Paragraph className="muted">让可靠的家政服务离你更近</Typography.Paragraph><Form layout="vertical" onFinish={submit}><Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}><Input prefix={<UserOutlined />} placeholder="用户名" /></Form.Item><Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}><Input.Password prefix={<LockOutlined />} placeholder="密码" /></Form.Item><Button type="primary" htmlType="submit" block>登录</Button></Form><div className="auth-link"><Link to="/register">注册</Link></div></Card></div>;
}
