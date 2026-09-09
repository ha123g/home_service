import { Alert, Button, Card, InputNumber, Space, Spin, Typography, message } from 'antd';
import { EnvironmentOutlined } from '@ant-design/icons';
import { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../services/api';

type LocationValue = { province?: string; city?: string; district?: string; addressDetail?: string; detail?: string; longitude?: number; latitude?: number };
type Props = { value?: LocationValue; onChange: (value: LocationValue) => void };
let amapLoadPromise: Promise<void> | null = null;
const ensureAmapLoaded = (apiKey: string) => {
  const existing = typeof window !== 'undefined' ? (window as any).AMap : undefined;
  const loadedKey = typeof window !== 'undefined' ? (window as any).__AMAP_LOADED_KEY : undefined;
  const currentScript = typeof document !== 'undefined'
    ? document.querySelector<HTMLScriptElement>('script[data-amap="true"]')
    : null;
  const scriptMatchesKey = !currentScript || currentScript.src.includes(apiKey);
  // 开发热更新可能丢失 __AMAP_LOADED_KEY，但全局对象仍然是可用的；此时直接复用，
  // 不要删除 AMap 后再等待一个已经加载完成、不会再次触发 load 的 script。
  if (existing && typeof existing.Map === 'function' && (loadedKey === apiKey || (loadedKey == null && scriptMatchesKey))) {
    if (typeof window !== 'undefined') (window as any).__AMAP_LOADED_KEY = apiKey;
    return Promise.resolve();
  }
  if (existing && typeof existing.Map === 'function' && !scriptMatchesKey) {
    try { delete (window as any).AMap; } catch { (window as any).AMap = undefined; }
  }
  if (amapLoadPromise) return amapLoadPromise;
  amapLoadPromise = new Promise<void>((resolve, reject) => {
    const started = Date.now();
    let settled = false;
    const fail = (error: Error) => {
      if (settled) return;
      settled = true;
      reject(error);
    };
    const succeed = () => {
      const api = (window as any).AMap;
      if (settled) return;
      if (api && typeof api.Map === 'function') {
        settled = true;
        resolve();
      }
    };
    const finish = () => {
      succeed();
      if (settled) return;
      if (Date.now() - started > 12000) { fail(new Error('地图服务加载失败，请检查 Web-Map-Key 或网络连接')); return; }
      window.setTimeout(finish, 80);
    };
    let script = document.querySelector<HTMLScriptElement>('script[data-amap="true"]');
    if (script?.dataset.amapFailed === 'true'
      || (script && !script.src.includes(encodeURIComponent(apiKey)) && !script.src.includes(apiKey))) {
      script.remove();
      script = null;
      // 清理旧 Key 创建的全局对象，防止开发热更新继续复用旧地图实例。
      try { delete (window as any).AMap; } catch { (window as any).AMap = undefined; }
      try { delete (window as any).__AMAP_LOADED_KEY; } catch { /* ignore */ }
    }
    script = script || document.createElement('script');
    if (!script.dataset.amap) {
      script.dataset.amap = 'true';
      script.src = `https://webapi.amap.com/maps?v=2.0&key=${encodeURIComponent(apiKey)}&plugin=AMap.Geolocation`;
      script.onerror = () => {
        script!.dataset.amapFailed = 'true';
        fail(new Error('地图服务加载失败，请检查 Web-Map-Key 或网络连接'));
      };
      document.head.appendChild(script);
    }
    finish();
  }).then(() => {
    (window as any).__AMAP_LOADED_KEY = apiKey;
  }).finally(() => { amapLoadPromise = null; });
  return amapLoadPromise;
};
export default function AmapPicker({ value, onChange }: Props) {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstance = useRef<any>(null);
  const markerInstance = useRef<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [lastCoords, setLastCoords] = useState<{ lng: number; lat: number }>();
  const [parseFailed, setParseFailed] = useState(false);
  const [parseError, setParseError] = useState('');
  const [retrying, setRetrying] = useState(false);
  const [retryToken, setRetryToken] = useState(0);

  const reverse = async (lng: number, lat: number) => {
    setLastCoords({ lng, lat }); setParseFailed(false); setParseError('');
    if (mapInstance.current && (window as any).AMap?.Marker) {
      if (!markerInstance.current) markerInstance.current = new (window as any).AMap.Marker({ map: mapInstance.current });
      markerInstance.current.setPosition([lng, lat]);
    }
    try {
      const g = await apiFetch<any>(`/map/regeocode?longitude=${lng}&latitude=${lat}`);
      onChange({ ...g, longitude: lng, latitude: lat, addressDetail: g.formattedAddress, detail: g.formattedAddress });
      // 地址回填是表单内部联动，不向用户弹出流程提示；只有失败才需要提醒。
    } catch (e) {
      setParseFailed(true); setParseError(e instanceof Error ? e.message : '地址解析服务暂不可用');
      onChange({ longitude: lng, latitude: lat });
      message.warning('地址解析暂不可用，已保留坐标；请手动选择地区并填写详细地址');
    }
  };

  useEffect(() => {
    let disposed = false;
    const init = async () => {
      setLoading(true);
      try {
        const config = await apiFetch<{apiKey:string;securityCode?:string}>('/map/web-config');
        if (!config.apiKey) throw new Error('未配置高德地图 Key');
        if (config.securityCode) (window as any)._AMapSecurityConfig = { securityJsCode: config.securityCode };
        await ensureAmapLoaded(config.apiKey);
        if (disposed || !mapRef.current) return;
        const A = typeof window !== 'undefined' ? (window as any).AMap : undefined;
        if (!A || typeof A.Map !== 'function') throw new Error('地图服务加载失败，可继续手动填写地址并输入坐标');
        const center = value?.longitude != null && value?.latitude != null
          ? [value.longitude, value.latitude] : [116.397428, 39.90923];
        const map = new A.Map(mapRef.current, { zoom: 12, center });
        mapInstance.current = map;
        map.on('click', async (event: any) => { await reverse(event.lnglat.getLng(), event.lnglat.getLat()); });
        setLoading(false); setRetrying(false); setError('');
        if (value?.longitude == null && navigator.geolocation) {
          navigator.geolocation.getCurrentPosition(async (position) => {
            try {
              const lng = position.coords.longitude; const lat = position.coords.latitude;
              map.setCenter([lng, lat]); map.setZoom(15); await reverse(lng, lat);
            } catch { /* 用户仍可手动点选 */ }
          }, () => undefined, { enableHighAccuracy: true, timeout: 6000, maximumAge: 300000 });
        }
      } catch (e) {
        if (!disposed) {
          setError(e instanceof Error ? e.message : '地图加载失败，可继续手动填写地址并输入坐标');
          setLoading(false); setRetrying(false);
          if (value?.longitude == null && navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
              (position) => onChange({ longitude: position.coords.longitude, latitude: position.coords.latitude }),
              () => undefined,
              { enableHighAccuracy: true, timeout: 6000, maximumAge: 300000 },
            );
          }
        }
      }
    };
    void init(); return () => { disposed = true; markerInstance.current?.setMap?.(null); markerInstance.current = null; mapInstance.current?.destroy?.(); mapInstance.current = null; };
  }, [retryToken]);

  const retryMap = () => { setRetrying(true); setError(''); setRetryToken((token) => token + 1); };
  const updateManualCoordinate = (name: 'longitude' | 'latitude', next: number | null) => {
    const valueNumber = typeof next === 'number' && Number.isFinite(next) ? next : undefined;
    onChange({ ...(value || {}), [name]: valueNumber });
  };
  return <Card size="small" className="map-picker-card" bodyStyle={{ padding: 0 }}>
    <div className="map-picker-toolbar"><Space><EnvironmentOutlined /><Typography.Text strong>服务位置</Typography.Text>{value?.city ? <Typography.Text type="secondary">{value.province}{value.city}{value.district}</Typography.Text> : null}</Space><Typography.Text type="secondary">可直接点击地图选点</Typography.Text></div>
    <div ref={mapRef} style={{ minHeight: 180, height: error ? 'auto' : 300, borderRadius: 8, overflow: 'hidden' }}>
      {loading && <div className="map-picker-loading"><Spin tip="地图加载中…" /></div>}
      {error && <div style={{ padding: 16 }}>
        <Alert type="warning" showIcon message={error} description="仍可选择省、市、区并填写详细地址；也可以手动输入经纬度。" action={<Button size="small" loading={retrying} onClick={retryMap}>重试地图</Button>} />
        <Space className="map-picker-manual-coordinates" wrap>
          <InputNumber aria-label="经度" placeholder="经度（-180 到 180）" min={-180} max={180} precision={6} value={value?.longitude} onChange={(next) => updateManualCoordinate('longitude', next)} />
          <InputNumber aria-label="纬度" placeholder="纬度（-90 到 90）" min={-90} max={90} precision={6} value={value?.latitude} onChange={(next) => updateManualCoordinate('latitude', next)} />
        </Space>
      </div>}
    </div>
    {parseFailed && lastCoords && <Alert type="warning" showIcon message={parseError ? `地址解析失败：${parseError}` : '地址解析失败，已保留坐标；请手动填写地址'} description="坐标已经保留。请在表单中选择省、市、区并填写详细地址，也可以稍后重试解析。" action={<Button size="small" onClick={() => void reverse(lastCoords.lng, lastCoords.lat)}>重试解析</Button>} style={{ margin: '8px 12px' }} />}
  </Card>;
}
