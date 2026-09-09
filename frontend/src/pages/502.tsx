import { Button, Typography } from 'antd';
import { useNavigate } from 'umi';

export default function BadGatewayPage() {
  const navigate = useNavigate();
  return <div className="route-error-page"><Typography.Title level={3}>抱歉，服务暂时不可用，请稍后重试</Typography.Title><Button type="primary" onClick={() => navigate('/login', { replace: true })}>返回登录页</Button></div>;
}
