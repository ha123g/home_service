import { Button, Typography } from 'antd';
import { useNavigate } from 'umi';

export default function NotFoundPage() {
  const navigate = useNavigate();
  return <div className="route-error-page"><Typography.Title level={3}>抱歉，页面不存在，请稍后重试</Typography.Title><Button type="primary" onClick={() => navigate('/login', { replace: true })}>返回登录页</Button></div>;
}
