import {
  Button,
  Card,
  Cascader,
  DatePicker,
  Divider,
  Drawer,
  Empty,
  Input,
  InputNumber,
  List,
  Select,
  Space,
  Spin,
  Tag,
  Typography,
  Upload,
  message,
} from "antd";
import { SendOutlined, RobotOutlined, UploadOutlined } from "@ant-design/icons";
import { useEffect, useRef, useState } from "react";
import type { PointerEvent as ReactPointerEvent } from "react";
import { useNavigate } from "umi";
import dayjs from "dayjs";
import { chatAgent, type AgentResponse } from "../services/agent/agentService";
import { getCurrentUser, logout } from "../services/auth/authService";
import { apiFetch, uploadFile } from "../services/api";
import { CHINA_REGIONS } from "../constants/chinaRegions";
import AmapPicker from "./AmapPicker";
type Props = { open: boolean; onClose: () => void };
type Message = {
  role: "user" | "assistant";
  content: string;
  result?: AgentResponse;
};
type Conversation = { id: string; title: string; updatedAt: number; messages: Message[] };
const formFieldsNotPersisted = new Set([
  "oldPassword", "newPassword", "confirmPassword", "contactPhone", "contactName",
  "phone", "realName", "longitude", "latitude",
]);
const sanitizeFormValues = (values: Record<string, Record<string, unknown>>) => Object.fromEntries(
  Object.entries(values).map(([index, fields]) => [
    index,
    Object.fromEntries(Object.entries(fields).filter(([name]) => !formFieldsNotPersisted.has(name))),
  ]),
);
const isAppointmentEndpoint = (endpoint: unknown) => String(endpoint).endsWith("/appointments");
const serviceSelectGroups = (tree: any[], services: any[], allowedIds?: Set<number>) => {
  const groups = (tree || []).map((root: any) => {
    const options: { label: string; value: number }[] = [];
    const walk = (node: any, path: string[]) => {
      const nextPath = [...path, String(node.name || "")].filter(Boolean);
      (node.services || []).forEach((service: any) => {
        const id = Number(service.id);
        if (Number.isFinite(id) && (!allowedIds || allowedIds.has(id))) options.push({
          label: nextPath.length > 1 && String(nextPath.at(-1)) !== String(service.title)
            ? `${nextPath.slice(1).join(" / ")} · ${service.title}` : String(service.title),
          value: id,
        });
      });
      (node.children || []).forEach((child: any) => walk(child, nextPath));
    };
    walk(root, []);
    const unique = Array.from(new Map(options.map((item) => [item.value, item])).values());
    return unique.length ? { label: root.name, options: unique } : null;
  }).filter(Boolean);
  return groups.length ? groups : (services || []).filter((service: any) => service.id != null && (!allowedIds || allowedIds.has(Number(service.id))))
    .map((service: any) => ({ label: service.title, value: Number(service.id) }));
};
const categorySelectOptions = (tree: any[]) => {
  const options: { label: string; value: number }[] = [];
  const walk = (node: any, path: string[]) => {
    const next = [...path, String(node.name || "")].filter(Boolean);
    const children = node.children || [];
    if (node.id != null && !children.length) options.push({ label: next.join(" / "), value: Number(node.id) });
    children.forEach((child: any) => walk(child, next));
  };
  (tree || []).forEach((root: any) => walk(root, []));
  return options;
};
const appointmentFormFallback = (shopId: number) => ({
  endpoint: "/api/appointments",
  method: "POST",
  confirmationRequired: true,
  fields: [
    { name: "shopId", label: "商家", inputType: "shop", required: true, userConfirmationRequired: true },
    { name: "serviceId", label: "服务项目", inputType: "service", required: true, userConfirmationRequired: true },
    { name: "region", label: "服务地区", inputType: "region", required: true, userConfirmationRequired: true },
    { name: "detail", label: "服务地址", inputType: "map-address", required: true, userConfirmationRequired: true },
    { name: "requirementText", label: "服务需求", inputType: "textarea", required: true, userConfirmationRequired: true },
    { name: "preferredStart", label: "期望开始时间", inputType: "datetime", required: true, userConfirmationRequired: true },
    { name: "preferredEnd", label: "期望结束时间", inputType: "datetime", required: false, userConfirmationRequired: true },
    { name: "contactName", label: "联系人", inputType: "text", required: true, userConfirmationRequired: true },
    { name: "contactPhone", label: "联系电话", inputType: "tel", required: true, userConfirmationRequired: true },
    { name: "idempotencyKey", label: "幂等键", inputType: "hidden", required: false, userConfirmationRequired: false },
  ],
  defaults: { shopId },
});
export default function AgentPanel({ open, onClose }: Props) {
  const navigate = useNavigate();
  const greeting: Message = { role: "assistant", content: "你好！我可以帮你找家政商家、回答服务问题，或生成预约 / 入驻等业务表单。涉及提交、支付或账户修改时，会先让你确认。" };
  const [userKey, setUserKey] = useState("anonymous");
  const [messages, setMessages] = useState<Message[]>([greeting]);
  const [value, setValue] = useState("");
  const [loading, setLoading] = useState(false);
  const [sessionId, setSessionId] = useState(() => `web-${crypto.randomUUID()}`);
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [historyOpen, setHistoryOpen] = useState(false);
  const [panelWidth, setPanelWidth] = useState(420);
  const panelRef = useRef<HTMLElement>(null);
  const draggingRef = useRef(false);
  const [formValues, setFormValues] = useState<
    Record<string, Record<string, unknown>>
  >({});
  const [hydratedUserKey, setHydratedUserKey] = useState<string | null>(null);
  const [formBusy, setFormBusy] = useState<string>();
  const [viewportWidth, setViewportWidth] = useState(() => typeof window === "undefined" ? 1024 : window.innerWidth);
  const [userLocation, setUserLocation] = useState<{ latitude: number; longitude: number }>();
  useEffect(() => {
    const onResize = () => setViewportWidth(window.innerWidth);
    window.addEventListener("resize", onResize);
    return () => window.removeEventListener("resize", onResize);
  }, []);
  useEffect(() => { if (!open) return; void getCurrentUser().then((user) => setUserKey(String(user.id))).catch(() => setUserKey("anonymous")); }, [open]);
  useEffect(() => {
    if (!open || !navigator.geolocation) return;
    navigator.geolocation.getCurrentPosition(
      (position) => setUserLocation({ latitude: position.coords.latitude, longitude: position.coords.longitude }),
      () => undefined,
      { timeout: 5000, maximumAge: 300000 },
    );
  }, [open]);
  const storageKey = (kind: string) => `agent.${kind}.user.${userKey}`;
  useEffect(() => {
    if (userKey === "anonymous") return;
    try {
      const raw = sessionStorage.getItem(storageKey("messages"));
      const savedFormValues = sessionStorage.getItem(storageKey("formValues"));
      const storedSession = sessionStorage.getItem(storageKey("sessionId"));
      setMessages(raw ? JSON.parse(raw) : [greeting]);
      setFormValues(savedFormValues ? JSON.parse(savedFormValues) : {});
      setSessionId(storedSession || `web-${crypto.randomUUID()}`);
      const historyRaw = localStorage.getItem(storageKey("history"));
      const now = Date.now();
      const history: Conversation[] = historyRaw ? JSON.parse(historyRaw) : [];
      setConversations(history.filter((item) => now - item.updatedAt < 24 * 60 * 60 * 1000));
      const widthRaw = localStorage.getItem(storageKey("width"));
      if (widthRaw) setPanelWidth(Math.min(Math.max(Number(widthRaw), 320), Math.floor(window.innerWidth * 0.7)));
      setHydratedUserKey(userKey);
    } catch { setMessages([greeting]); setFormValues({}); setHydratedUserKey(userKey); }
  }, [userKey]);
  useEffect(() => { if (userKey !== "anonymous") sessionStorage.setItem(storageKey("sessionId"), sessionId); }, [sessionId, userKey]);
  useEffect(() => {
    const clearOnLogout = () => { setMessages([greeting]); setFormValues({}); setValue(""); setSessionId(`web-${crypto.randomUUID()}`); setHydratedUserKey(null); setUserKey("anonymous"); };
    window.addEventListener("auth:logout", clearOnLogout);
    return () => window.removeEventListener("auth:logout", clearOnLogout);
  }, []);
  useEffect(() => {
    if (userKey === "anonymous" || hydratedUserKey !== userKey) return;
    try {
      const persistedMessages = messages.map((item) => item.result && !String(item.result.componentType || "").endsWith("_FORM")
        ? { ...item, result: { ...item.result, data: undefined } }
        : item) as Message[];
      sessionStorage.setItem(storageKey("messages"), JSON.stringify(persistedMessages));
      sessionStorage.setItem(storageKey("formValues"), JSON.stringify(sanitizeFormValues(formValues)));
      const firstUser = messages.find((item) => item.role === "user");
      const current: Conversation = { id: sessionId, title: firstUser?.content.slice(0, 28) || "新对话", updatedAt: Date.now(), messages: persistedMessages };
      const next = [current, ...conversations.filter((item) => item.id !== sessionId)].slice(0, 20);
      localStorage.setItem(storageKey("history"), JSON.stringify(next));
      setConversations(next);
    } catch {
      /* storage quota/private mode */
    }
  }, [formValues, hydratedUserKey, messages, sessionId, userKey]);
  useEffect(() => {
    const move = (event: PointerEvent) => { if (!draggingRef.current || !panelRef.current) return; const rect = panelRef.current.getBoundingClientRect(); const next = Math.min(Math.max(rect.right - event.clientX, 320), Math.floor(window.innerWidth * 0.7)); setPanelWidth(next); };
    const stop = () => { if (draggingRef.current) { draggingRef.current = false; localStorage.setItem(storageKey("width"), String(panelWidth)); } };
    window.addEventListener("pointermove", move); window.addEventListener("pointerup", stop);
    return () => { window.removeEventListener("pointermove", move); window.removeEventListener("pointerup", stop); };
  }, [panelWidth, userKey]);
  const [catalog, setCatalog] = useState<any[]>([]);
  const [catalogTree, setCatalogTree] = useState<any[]>([]);
  const [categoryTree, setCategoryTree] = useState<any[]>([]);
  const [catalogLoading, setCatalogLoading] = useState(false);
  const [shops, setShops] = useState<any[]>([]);
  const [orders, setOrders] = useState<any[]>([]);
  const [shopServices, setShopServices] = useState<Record<string, any[]>>({});
  useEffect(() => {
    if (!open) return;
    setCatalogLoading(true);
    void Promise.all([
      apiFetch<any[]>("/service-listings/catalog").catch(() => []),
      apiFetch<any[]>("/service-listings/catalog/tree").catch(() => []),
      apiFetch<any[]>("/service-categories/tree").catch(() => []),
      apiFetch<any>("/shops?size=100").catch(() => ({ items: [] })),
      apiFetch<any>("/orders?size=100").catch(() => ({ items: [] })),
    ])
      .then(([c, tree, categories, s, orderPage]) => {
        setCatalog(c || []);
        setCatalogTree(tree || []);
        setCategoryTree(categories || []);
        setShops(s?.items || []);
        setOrders(orderPage?.items || []);
      })
      .catch(() => undefined)
      .finally(() => setCatalogLoading(false));
  }, [open]);
  const send = async () => {
    const text = value.trim();
    if (!text || loading) return;
    setValue("");
    setMessages((m) => [...m, { role: "user", content: text }]);
    setLoading(true);
    try {
      const result = await chatAgent(text, sessionId, userLocation);
      setMessages((m) => [
        ...m,
        {
          role: "assistant",
          content: result.answer || "暂时没有可用回答。",
          result,
        },
      ]);
    } catch (e) {
      setMessages((m) => [
        ...m,
        {
          role: "assistant",
          content: e instanceof Error ? e.message : "请求失败，请稍后重试。",
        },
      ]);
    } finally {
      setLoading(false);
    }
  };
  const startAppointment = async (shop: any) => {
    if (loading || !shop?.id) return;
    setLoading(true);
    try {
      const [result, services] = await Promise.all([
        chatAgent(`我想预约${shop.shopName || "这家商家"}`, sessionId, userLocation),
        apiFetch<any[]>(`/shops/${shop.id}/services`).catch(() => []),
      ]);
      const returnedComponent = result.data && typeof result.data === "object" ? result.data as any : undefined;
      // 商家卡片的预约入口必须始终渲染预约表单。模型只负责选择业务工具，
      // 如果旧服务或模型响应缺少组件 Schema，使用同一白名单契约的安全兜底，
      // 避免只显示“请填写预约信息”却没有可填写内容。
      const component = Array.isArray(returnedComponent?.fields)
        ? { ...returnedComponent, defaults: { ...(returnedComponent.defaults || {}), shopId: shop.id } }
        : appointmentFormFallback(Number(shop.id));
      // 推荐结果可能来自分页之外的商家，确保预约表单的商家下拉仍能显示当前选择。
      setShops((all) => all.some((item) => String(item.id) === String(shop.id)) ? all : [shop, ...all]);
      setShopServices((all) => ({ ...all, [String(shop.id)]: services || [] }));
      setMessages((all) => [...all, {
        role: "assistant",
        content: "已选中这家商家，请填写预约信息。",
        result: { ...result, componentType: "APPOINTMENT_FORM", schemaVersion: "appointment-form.v1", data: component },
      }]);
    } catch (e) {
      setMessages((all) => [...all, {
        role: "assistant",
        content: e instanceof Error ? e.message : "预约表单暂时无法打开，请稍后重试。",
      }]);
    } finally {
      setLoading(false);
    }
  };
  if (!open) return null;
  const setField = (index: number, name: string, next: unknown) => {
    setFormValues((all) => ({
      ...all,
      [String(index)]: { ...(all[String(index)] || {}), [name]: next },
    }));
    if (name === "shopId" && next)
      void apiFetch<any[]>(`/shops/${next}/services`)
        .then((list) =>
          setShopServices((all) => ({ ...all, [String(next)]: list || [] })),
        )
        .catch(() => undefined);
  };
  const submitInline = async (item: Message, index: number) => {
    const component: any = item.result?.data;
    if (!component?.endpoint) return;
    const fields = component.fields || [];
    const current = {
      ...(component.defaults || {}),
      ...(formValues[String(index)] || {}),
    } as any;
    if (
      String(component.endpoint).endsWith("/users/me/password") &&
      current.newPassword !== current.confirmPassword
    ) {
      message.error("两次输入的新密码不一致");
      return;
    }
    const missing = fields.find(
      (f: any) =>
        f.required &&
        f.inputType !== "hidden" &&
        f.inputType !== "map-address" &&
        (current[f.name] == null ||
          current[f.name] === "" ||
          (Array.isArray(current[f.name]) && !current[f.name].length)),
    );
    if (missing) {
      message.error(`请填写${missing.label}`);
      return;
    }
    if (
      (String(component.endpoint).endsWith("/merchant/applications") ||
        String(component.endpoint).endsWith("/appointments")) &&
      (current.longitude == null || current.latitude == null)
    ) {
      message.error("请在地图中选择位置");
      return;
    }
    if (
      String(component.endpoint).endsWith("/merchant/applications") &&
      !current.addressDetail
    ) {
      message.error("请填写详细地址");
      return;
    }
    if (isAppointmentEndpoint(component.endpoint) && !String(current.detail || "").trim()) {
      message.error("请填写服务详细地址");
      return;
    }
    if (String(component.endpoint).endsWith("/merchant/applications")) {
      const categoryIds = Array.isArray(current.categoryIds) ? current.categoryIds : [];
      const serviceItems = Array.isArray(current.serviceItems) ? current.serviceItems : [];
      if (!categoryIds.length) {
        message.error("请选择至少一个服务分类");
        return;
      }
      if (!serviceItems.length) {
        message.error("请至少添加一项自定义服务");
        return;
      }
      const invalidItem = serviceItems.find((service: any) =>
        !service || service.categoryId == null || !String(service.title || "").trim()
        || !String(service.pricingUnit || "").trim() || service.basePrice == null,
      );
      if (invalidItem) {
        message.error("请补全每项服务的分类、名称、计价单位和起步价");
        return;
      }
    }
    if (
      String(component.endpoint).endsWith("/users/me/password") &&
      !current.confirmPassword
    ) {
      message.error("请再次输入新密码");
      return;
    }
    setFormBusy(String(index));
    try {
      let payload = { ...current };
      const isAppointment = isAppointmentEndpoint(component.endpoint);
      if (isAppointment && !payload.addressId) {
        const address = await apiFetch<any>("/addresses", {
          method: "POST",
          body: JSON.stringify({
            province: payload.province,
            city: payload.city,
            district: payload.district,
            detail: payload.detail,
            receiverName: payload.contactName,
            receiverPhone: payload.contactPhone,
            longitude: payload.longitude,
            latitude: payload.latitude,
          }),
        });
        payload = { ...payload, addressId: address.id };
        setField(index, "addressId", address.id);
        delete payload.location;
        delete payload.region;
        delete payload.province;
        delete payload.city;
        delete payload.district;
        delete payload.detail;
      }
      if (String(component.endpoint).endsWith("/merchant/applications"))
        delete payload.region;
      if (Array.isArray(payload.tags)) payload.tags = payload.tags.map((tag: unknown) => String(tag).trim()).filter(Boolean);
      if (Array.isArray(payload.serviceItems)) {
        payload.serviceItems = payload.serviceItems.map((service: any) => ({
          ...service,
          title: String(service.title || "").trim(),
          summary: service.summary == null ? undefined : String(service.summary).trim(),
          description: service.description == null ? undefined : String(service.description).trim(),
          pricingUnit: String(service.pricingUnit || "").trim().toUpperCase(),
          tags: Array.isArray(service.tags)
            ? service.tags.map((tag: unknown) => String(tag).trim()).filter(Boolean)
            : [],
        }));
      }
      if (payload.preferredStart && typeof payload.preferredStart !== "string")
        payload.preferredStart = payload.preferredStart.toISOString();
      if (payload.preferredEnd && typeof payload.preferredEnd !== "string")
        payload.preferredEnd = payload.preferredEnd.toISOString();
      if (!payload.idempotencyKey) {
        payload.idempotencyKey = crypto.randomUUID();
        // 幂等键必须回填到当前表单，重试或刷新后仍复用同一个业务请求。
        setField(index, "idempotencyKey", payload.idempotencyKey);
      }
      // Resolve path parameters from values selected in the inline form.
      let endpoint = String(component.endpoint).replace(/^\/api/, "");
      endpoint = endpoint.replace(/\{(\w+)\}/g, (_match: string, name: string) => {
        const selected = payload[name];
        if (selected == null || selected === "") {
          throw new Error(`请选择${name === "orderId" ? "订单" : name}`);
        }
        delete payload[name];
        return encodeURIComponent(String(selected));
      });
      const method = String(component.method || "POST").toUpperCase();
      const submitted = await apiFetch<any>(endpoint, { method, body: JSON.stringify(payload) });
      const isMerchantApplication = String(component.endpoint).endsWith("/merchant/applications");
      const isPassword = String(component.endpoint).endsWith("/users/me/password");
      if (isAppointment || String(component.endpoint).includes("/orders/")) {
        window.dispatchEvent(new CustomEvent("notifications:refresh"));
      }
      // 写入成功后用普通只读接口回查，向用户展示数据库确认过的结果。
      let confirmed = submitted;
      try {
        if (isAppointment && submitted?.id) confirmed = await apiFetch<any>(`/appointments/${submitted.id}`);
        if (isMerchantApplication) confirmed = await apiFetch<any>("/merchant/applications/me");
      } catch {
        // 写操作已经成功时，回查失败不应把成功误报为失败；使用提交接口的安全响应。
      }
      const proof = isPassword
        ? "密码已修改成功，请使用新密码重新登录。"
        : isAppointment
        ? `预约已提交${confirmed?.requestNo ? `，预约编号 ${confirmed.requestNo}` : ""}，商家会在预约列表中处理。`
        : isMerchantApplication
          ? `入驻申请已提交${confirmed?.applyNo ? `，申请编号 ${confirmed.applyNo}` : ""}，当前状态为${confirmed?.status === "APPROVED" ? "已通过" : "申请中"}。`
          : String(component.endpoint).endsWith("/users/me/profile")
            ? "个人资料已保存，你可以继续查询或办理其他事项。"
            : "已完成提交，你可以继续查询最新状态。";
      setMessages((m) => [
        ...m,
        {
          role: "assistant",
          content: proof,
        },
      ]);
      if (isPassword) {
        try { await logout(); } catch { /* 密码已修改，仍需清理本地状态 */ }
        navigate("/login", { replace: true });
      }
    } catch (e) {
      message.error(
        e instanceof Error ? e.message : "提交失败，请检查填写内容",
      );
    } finally {
      setFormBusy(undefined);
    }
  };
  const renderInlineForm = (item: Message, index: number) => {
    const component: any = item.result?.data;
    if (!component?.fields) return null;
    const values = {
      ...(component.defaults || {}),
      ...(formValues[String(index)] || {}),
    } as Record<string, any>;
    const renderField = (field: any) => {
      const val: any =
        values[field.name] ?? component.defaults?.[field.name] ?? "";
      let control: any;
      if (field.inputType === "hidden") return null;
      if (field.inputType === "map-address")
        control = (
          <>
            <AmapPicker
              value={values as any}
              onChange={(location) =>
                setFormValues((all) => ({
                  ...all,
                  [String(index)]: { ...(all[String(index)] || {}), ...location },
                }))
              }
            />
            {(field.name === "detail" || field.name === "addressDetail") && (
              <Input
                style={{ marginTop: 8 }}
                value={String(val)}
                placeholder="补充门牌号、楼栋和房间号"
                onChange={(e) => setField(index, field.name, e.target.value)}
              />
            )}
          </>
        );
      else if (field.inputType === "cos-upload")
        control = (
          <Upload
            accept="image/png,image/jpeg,image/webp"
            showUploadList
            beforeUpload={(file) => {
              const purpose =
                field.name === "avatarUrl" ? "AVATAR" : "MERCHANT_GALLERY";
              void uploadFile(file, purpose)
                .then((uploaded) => {
                  if (field.name === "images")
                    setField(index, field.name, [
                      ...(Array.isArray(val) ? val : []),
                      {
                        imageType: "OTHER",
                        objectKey: uploaded.objectKey,
                        mimeType: uploaded.mimeType,
                        fileSize: uploaded.fileSize,
                        sha256: uploaded.sha256,
                      },
                    ]);
                  else setField(index, field.name, uploaded.objectKey);
                  message.success("图片上传成功");
                })
                .catch((error) =>
                  message.error(
                    error instanceof Error ? error.message : "图片上传失败",
                  ),
                );
              return false;
            }}
          >
            <Button icon={<UploadOutlined />}>选择图片</Button>
          </Upload>
        );
      else if (field.inputType === "textarea")
        control = (
          <Input.TextArea
            rows={3}
            value={String(val)}
            onChange={(e) => setField(index, field.name, e.target.value)}
          />
        );
      else if (field.inputType === "select")
        control = (
          <Select
            style={{ width: "100%" }}
            value={val || undefined}
            options={[
              { value: "UNKNOWN", label: "未设置" },
              { value: "MALE", label: "男" },
              { value: "FEMALE", label: "女" },
            ]}
            onChange={(v) => setField(index, field.name, v)}
          />
        );
      else if (field.inputType === "number")
        control = <InputNumber min={field.min ?? 0.1} max={field.max ?? 500} step={field.step ?? 0.5} precision={1} style={{ width: "100%" }} value={typeof val === "number" ? val : val ? Number(val) : undefined} onChange={(v) => setField(index, field.name, v)} />;
      else if (field.inputType === "category-multiple")
        control = (
          <Select
            mode="multiple"
            showSearch
            optionFilterProp="label"
            maxTagCount="responsive"
            style={{ width: "100%" }}
            value={Array.isArray(val) ? val : []}
            options={categorySelectOptions(categoryTree)}
            loading={catalogLoading}
            notFoundContent={catalogLoading ? "正在加载服务分类" : "暂无可用服务分类"}
            onChange={(v) => setField(index, field.name, Array.isArray(v) ? v.map((item) => Number(item)) : [])}
          />
        );
      else if (field.inputType === "service-items") {
        const serviceItems = Array.isArray(val) ? val : [];
        const updateService = (itemIndex: number, name: string, next: unknown) => {
          const nextItems = serviceItems.map((service: any, currentIndex: number) =>
            currentIndex === itemIndex ? { ...service, [name]: next } : service,
          );
          setField(index, field.name, nextItems);
        };
        control = (
          <div className="agent-service-items-editor">
            {serviceItems.map((service: any, itemIndex: number) => (
              <Card key={`${itemIndex}-${service.title || "service"}`} size="small" style={{ marginBottom: 10 }}>
                <Space direction="vertical" style={{ width: "100%" }}>
                  <Select showSearch optionFilterProp="label" style={{ width: "100%" }} value={service.categoryId} options={categorySelectOptions(categoryTree)} placeholder="所属标准分类" onChange={(v) => updateService(itemIndex, "categoryId", v)} />
                  <Input value={service.title || ""} maxLength={128} placeholder="服务名称，例如：三室一厅深度保洁" onChange={(e) => updateService(itemIndex, "title", e.target.value)} />
                  <Input value={service.summary || ""} maxLength={512} placeholder="服务简介" onChange={(e) => updateService(itemIndex, "summary", e.target.value)} />
                  <Input.TextArea value={service.description || ""} rows={2} maxLength={2048} placeholder="详细描述服务范围和注意事项" onChange={(e) => updateService(itemIndex, "description", e.target.value)} />
                  <Space style={{ display: "flex", flexWrap: "wrap" }}>
                    <Select style={{ width: 120 }} value={service.pricingUnit || "ORDER"} options={[{ value: "ORDER", label: "按次" }, { value: "HOUR", label: "按小时" }, { value: "SQUARE_METER", label: "按平方米" }]} onChange={(v) => updateService(itemIndex, "pricingUnit", v)} />
                    <InputNumber min={0} precision={2} value={service.basePrice} addonAfter="元" placeholder="起步价" onChange={(v) => updateService(itemIndex, "basePrice", v)} />
                    <InputNumber min={1} value={service.durationMinutes} addonAfter="分钟" placeholder="参考时长" onChange={(v) => updateService(itemIndex, "durationMinutes", v)} />
                    <Button danger type="link" onClick={() => setField(index, field.name, serviceItems.filter((_: any, currentIndex: number) => currentIndex !== itemIndex))}>删除</Button>
                  </Space>
                  <Select mode="tags" style={{ width: "100%" }} value={Array.isArray(service.tags) ? service.tags : []} maxTagCount="responsive" placeholder="输入服务标签后回车添加" onChange={(v) => updateService(itemIndex, "tags", v)} />
                </Space>
              </Card>
            ))}
            <Button type="dashed" block onClick={() => setField(index, field.name, [...serviceItems, { pricingUnit: "ORDER", tags: [] }])}>+ 添加一项自定义服务</Button>
          </div>
        );
      }
      else if (field.inputType === "service-multiple")
        control = (
          <Select
            mode="multiple"
            showSearch
            optionFilterProp="label"
            maxTagCount="responsive"
            style={{ width: "100%" }}
            value={Array.isArray(val) ? val : []}
            options={serviceSelectGroups(catalogTree, catalog) as any}
            loading={catalogLoading}
            notFoundContent={catalogLoading ? "正在加载服务" : "暂无可用服务"}
            onChange={(v) => setField(index, field.name, Array.isArray(v) ? v.filter((item) => typeof item === "number" || (typeof item === "string" && /^\d+$/.test(item))).map((item) => Number(item)) : [])}
          />
        );
      else if (field.inputType === "tags") {
        const tags = Array.isArray(val) ? val : [];
        control = (
          <div className="agent-tags-editor">
            {tags.map((tag: unknown, tagIndex: number) => (
              <Space key={`${String(tag)}-${tagIndex}`} style={{ display: "flex", marginBottom: 8 }}>
                <Input value={String(tag)} maxLength={32} onChange={(e) => { const next = [...tags]; next[tagIndex] = e.target.value; setField(index, field.name, next); }} placeholder="输入服务标签" />
                <Button type="link" danger onClick={() => setField(index, field.name, tags.filter((_, currentIndex) => currentIndex !== tagIndex))}>删除</Button>
              </Space>
            ))}
            <Button type="dashed" block onClick={() => setField(index, field.name, [...tags, ""])}>+ 添加标签</Button>
          </div>
        );
      }
      else if (field.inputType === "service") {
        const shopSelected = values.shopId != null && values.shopId !== "";
        const servicesLoaded = !shopSelected || Object.prototype.hasOwnProperty.call(shopServices, String(values.shopId));
        const serviceOptions = shopSelected
          ? (shopServices[String(values.shopId)] || [])
          : catalog;
        const allowed = new Set(serviceOptions.map((item: any) => item.id));
        const groupedOptions = serviceSelectGroups(catalogTree, serviceOptions, allowed);
        control = <Select showSearch optionFilterProp="label" style={{ width: "100%" }} value={val || undefined} options={groupedOptions as any} loading={!servicesLoaded} notFoundContent={!servicesLoaded ? "正在加载服务" : "暂无可用服务"} onChange={(v) => setField(index, field.name, v)} />;
      } else if (field.inputType === "order")
        control = (
          <Select
            showSearch
            optionFilterProp="label"
            style={{ width: "100%" }}
            value={val || undefined}
            options={orders
              .filter((order) => order.status === "PENDING_PAYMENT")
              .map((order) => ({
                value: order.id,
                label: `${order.orderNo || `订单 ${order.id}`} · ${order.serviceTitleSnapshot || "服务"} · ¥${order.payableAmount ?? "-"}`,
              }))}
            placeholder={orders.length ? "请选择待支付订单" : "暂无待支付订单"}
            onChange={(v) => setField(index, field.name, v)}
          />
        );
      else if (field.inputType === "shop")
        control = (
          <Select
            showSearch
            optionFilterProp="label"
            style={{ width: "100%" }}
            value={val || undefined}
            options={shops.map((s) => ({ value: s.id, label: s.shopName }))}
            onChange={(v) => {
              setField(index, field.name, v);
              setField(index, "serviceId", undefined);
            }}
          />
        );
      else if (field.inputType === "region")
        control = (
          <Cascader
            options={CHINA_REGIONS}
            style={{ width: "100%" }}
            value={val as any}
            onChange={(path) => {
              const p = path as string[];
              setField(index, field.name, p);
              setField(index, "province", p[0]);
              setField(index, "city", p[1]);
              setField(index, "district", p[2]);
            }}
          />
        );
      else if (field.inputType === "datetime")
        control = (
          <DatePicker
            showTime
            style={{ width: "100%" }}
            value={
              val
                ? typeof val === "string"
                  ? (dayjs(val).isValid() ? dayjs(val) : undefined)
                  : (val as any)
                : undefined
            }
            onChange={(v) => setField(index, field.name, v)}
          />
        );
      else
        control = (
          <Input
            type={
              field.inputType === "password"
                ? "password"
                : field.inputType === "tel"
                  ? "tel"
                  : "text"
            }
            value={String(val)}
            placeholder={undefined}
            onChange={(e) => setField(index, field.name, e.target.value)}
          />
        );
      return (
        <div key={field.name} className="agent-form-control">
          <Typography.Text>
            {field.label}
            {field.required ? " *" : ""}
          </Typography.Text>
          {control}
        </div>
      );
    };
    const fields = [
      ...component.fields,
      ...(String(component.endpoint).endsWith("/users/me/password")
        ? [
            {
              name: "confirmPassword",
              label: "确认新密码",
              inputType: "password",
              required: true,
            },
          ]
        : []),
    ];
    return (
      <div className="agent-form-preview">
        {fields.map(renderField)}
        <Button
          type="primary"
          loading={formBusy === String(index)}
          onClick={() => void submitInline(item, index)}
        >
          确认提交
        </Button>
      </div>
    );
  };
  const clearConversation = () => {
    setMessages([greeting]);
    setSessionId(`web-${crypto.randomUUID()}`);
    setFormValues({});
  };
  const selectConversation = (item: Conversation) => {
    setSessionId(item.id);
    setMessages(item.messages);
    setHistoryOpen(false);
  };
  const startResize = (event: ReactPointerEvent<HTMLDivElement>) => {
    if (viewportWidth <= 768) return;
    event.preventDefault();
    draggingRef.current = true;
    event.currentTarget.setPointerCapture?.(event.pointerId);
  };
  const renderQueryResult = (item: Message) => {
    if (item.result?.componentType !== "TEXT" || !item.result.data || typeof item.result.data !== "object") return null;
    const data: any = item.result.data;
    if (!Array.isArray(data.items)) return null;
    const isOrder = data.kind === "ORDER"
      || data.items.some((entry: any) => entry.orderNo || entry.serviceTitleSnapshot)
      || /订单/.test(`${item.content} ${item.result?.answer || ""}`);
    const statusLabel: Record<string, string> = {
      PENDING_PUBLISH: "待发布", PENDING_PAYMENT: "待支付", PAID: "已支付", CONFIRMED: "已确认",
      IN_SERVICE: "服务中", COMPLETED: "已完成", CANCELLED: "已取消", CLOSED: "已关闭",
      PENDING_PLATFORM: "申请中", CONTACTING: "联系中", WORKER_ARRANGED: "已安排",
    };
    if (!data.items.length) {
      return <div className="agent-query-list"><Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={isOrder ? "当前没有订单" : "当前没有预约记录"} /></div>;
    }
    return (
      <div className="agent-query-list" aria-label={isOrder ? "订单查询结果" : "预约查询结果"}>
        {data.items.slice(0, 5).map((entry: any) => (
          <div className="agent-query-row" key={entry.id || entry.orderNo || entry.requestNo}>
            <Typography.Text strong>{entry.serviceTitleSnapshot || entry.serviceTitle || (isOrder ? entry.orderNo : entry.requestNo) || "服务记录"}</Typography.Text>
            <Tag>{statusLabel[entry.status] || "处理中"}</Tag>
          </div>
        ))}
      </div>
    );
  };
  return (
    <section ref={panelRef} className="agent-panel" aria-label="家政助手" style={{ width: viewportWidth <= 768 ? "100%" : panelWidth, flexBasis: viewportWidth <= 768 ? "100%" : panelWidth }}>
      <div className="agent-panel-resize-handle" onPointerDown={startResize} role="separator" aria-label="调整助手面板宽度" />
      <div className="agent-panel-header">
        <Typography.Title level={4} style={{ margin: 0 }}>
          <RobotOutlined /> 家政助手
        </Typography.Title>
        <div>
          <Button type="link" onClick={() => setHistoryOpen(true)}>历史</Button>
          <Button
            type="link"
            onClick={clearConversation}
          >
            清空
          </Button>
          <Button type="text" onClick={onClose}>
            收起
          </Button>
        </div>
      </div>
      <Divider style={{ margin: "8px 0" }} />
      <div className="agent-panel-messages">
        <List
          dataSource={messages}
          renderItem={(item, index) => (
            <List.Item
              style={{
                justifyContent:
                  item.role === "user" ? "flex-end" : "flex-start",
                border: 0,
              }}
            >
              <Card
                size="small"
                className={`agent-message-card agent-message-${item.role}`}
                style={{
                  width: item.role === "assistant" ? "100%" : undefined,
                  maxWidth: item.role === "assistant" ? "100%" : "92%",
                  background: item.role === "user" ? "#e6f4ff" : "#f7f9fc",
                }}
              >
                <Typography.Paragraph
                  style={{ whiteSpace: "pre-wrap", marginBottom: 4 }}
                >
                  {item.content}
                </Typography.Paragraph>
                {item.result && (
                  <>
                    {renderInlineForm(item, index)}
                    {renderQueryResult(item)}
                    {item.result.componentType === "SHOP_LIST" &&
                    item.result.data &&
                    typeof item.result.data === "object" &&
                    "items" in (item.result.data as any) ? (
                      <div className="agent-shop-list">
                        {((item.result.data as any).items || []).map((shop: any) => (
                          <div className="agent-shop-card" key={shop.id}>
                            <div className="agent-shop-card-main">
                              <Typography.Text strong>{shop.serviceNames?.slice(0, 2).join(" · ") || "家政服务"}</Typography.Text>
                              <Tag color="green">{shop.status === "OPEN" ? "营业中" : "暂不营业"}</Tag>
                            </div>
                            <Typography.Text type="secondary" className="agent-shop-card-meta">
                              {[...(shop.tags || []).slice(0, 3), shop.city, shop.district].filter(Boolean).join(" · ") || "家政服务商家"}
                              {shop.distanceKm != null ? ` · ${shop.distanceKm} km` : ""}
                            </Typography.Text>
                            <div className="agent-shop-card-actions">
                              <Button size="small" type="primary" disabled={shop.status !== "OPEN"} onClick={() => void startAppointment(shop)}>预约</Button>
                              <Button size="small" type="link" onClick={() => navigate(`/shops/${shop.id}`)}>查看详情</Button>
                            </div>
                          </div>
                        ))}
                      </div>
                    ) : null}
                    {item.result.citations?.length ? (
                      <Typography.Text
                        type="secondary"
                        style={{ fontSize: 12 }}
                      >
                        来源：
                        {item.result.citations
                          .map((c) => c.title || c.source)
                          .filter(Boolean)
                          .join("、")}
                      </Typography.Text>
                    ) : null}
                  </>
                )}
              </Card>
            </List.Item>
          )}
        />
      </div>
      <Spin spinning={loading} />
      <div className="agent-composer">
        <Input.TextArea
          value={value}
          onChange={(e) => setValue(e.target.value)}
          onPressEnter={(e) => {
            if (!e.shiftKey) {
              e.preventDefault();
              void send();
            }
          }}
          autoSize={{ minRows: 1, maxRows: 4 }}
          placeholder="描述你的需求，Enter 发送，Shift+Enter 换行"
        />
        <Button
          type="primary"
          aria-label="发送消息"
          icon={<SendOutlined />}
          loading={loading}
          disabled={!value.trim()}
          onClick={() => void send()}
        >
          发送
        </Button>
      </div>
      <Drawer title="历史会话（保留 24 小时）" open={historyOpen} onClose={() => setHistoryOpen(false)} width={320}>
        <List dataSource={conversations} locale={{ emptyText: "暂无历史会话" }} renderItem={(item) => <List.Item actions={[<Button type="link" danger onClick={() => { const next = conversations.filter((entry) => entry.id !== item.id); setConversations(next); localStorage.setItem(storageKey("history"), JSON.stringify(next)); }}>删除</Button>]}><Button type="text" block style={{ textAlign: "left" }} onClick={() => selectConversation(item)}>{item.title}<Typography.Text type="secondary" style={{ display: "block", fontSize: 12 }}>{new Date(item.updatedAt).toLocaleString()}</Typography.Text></Button></List.Item>} />
      </Drawer>
    </section>
  );
}
