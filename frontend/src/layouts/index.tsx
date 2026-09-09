import { Outlet, useLocation, useNavigate } from 'umi';
import { useEffect, useState } from 'react';
import { getCurrentUser, initCsrf } from '../services/auth/authService';
import UserLayout from './UserLayout';
import AdminLayout from './AdminLayout';
export default function RootLayout() {
  const { pathname } = useLocation();
  const navigate = useNavigate();
  const [checking, setChecking] = useState(!['/login', '/register', '/502', '/404'].includes(pathname));
  useEffect(() => {
    const onBackendUnavailable = () => navigate('/502', { replace: true });
    const onAuthExpired = () => navigate('/login', { replace: true });
    window.addEventListener('backend:unavailable', onBackendUnavailable);
    window.addEventListener('auth:expired', onAuthExpired);
    if (pathname === '/502' || pathname === '/404') {
      setChecking(false);
      return () => {
        window.removeEventListener('backend:unavailable', onBackendUnavailable);
        window.removeEventListener('auth:expired', onAuthExpired);
      };
    }
    let active = true;
    setChecking(true);
    initCsrf()
      .then(() => pathname === '/login' || pathname === '/register' ? undefined : getCurrentUser())
      .then(() => { if (active) setChecking(false); })
      .catch((error: unknown) => {
        if (!active) return;
        setChecking(false);
        if (error instanceof Error && error.message.includes('登录已失效')) navigate('/login', { replace: true });
      });
    return () => {
      active = false;
      window.removeEventListener('backend:unavailable', onBackendUnavailable);
      window.removeEventListener('auth:expired', onAuthExpired);
    };
  }, [navigate, pathname]);
  if (['/login', '/register', '/502', '/404'].includes(pathname)) return <Outlet />;
  if (checking) return <div className="route-loading">正在检查登录状态…</div>;
  if (pathname.startsWith('/admin')) return <AdminLayout />;
  return <UserLayout />;
}
