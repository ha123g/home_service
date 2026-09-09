import {
  Button,
  Card,
  Cascader,
  DatePicker,
  Descriptions,
  Alert,
  Form,
  Input,
  Modal,
  Select,
  Space,
  Typography,
  message,
} from "antd";
import { Carousel, Image, Tag } from "antd";
import dayjs from "dayjs";
import { useEffect, useState } from "react";
import { useParams } from "umi";
import { getShop, getShopServices } from "../../services/shop/shopService";
import { apiFetch, postJson } from "../../services/api";
import { createAppointment } from "../../services/appointment/appointmentService";
import type { ServiceListingView, ShopView } from "../../services/shop/shopTypes";
import AmapPicker from "../../components/AmapPicker";
import { CHINA_REGIONS } from "../../constants/chinaRegions";
const serviceSelectGroups = (tree: any[], services: ServiceListingView[]) => {
  const allowed = new Set(services.map((service) => service.id));
  const groups = (tree || []).map((root: any) => {
    const options: { label: string; value: number }[] = [];
    const walk = (node: any, path: string[]) => {
      const nextPath = [...path, String(node.name || "")].filter(Boolean);
      (node.services || []).forEach((service: any) => {
        if (allowed.has(service.id)) options.push({
          label: nextPath.length > 1 && String(nextPath.at(-1)) !== String(service.title)
            ? `${nextPath.slice(1).join(" / ")} · ${service.title}` : String(service.title),
          value: Number(service.id),
        });
      });
      (node.children || []).forEach((child: any) => walk(child, nextPath));
    };
    walk(root, []);
    const unique = Array.from(new Map(options.map((item) => [item.value, item])).values());
    return unique.length ? { label: root.name, options: unique } : null;
  }).filter(Boolean);
  return groups.length ? groups : services.map((service) => ({ label: service.title, value: service.id }));
};
export default function ShopDetailPage() {
  const { id } = useParams();
  const [shop, setShop] = useState<ShopView>();
  const [services, setServices] = useState<ServiceListingView[]>([]);
  const [serviceTree, setServiceTree] = useState<any[]>([]);
  const [servicesLoading, setServicesLoading] = useState(true);
  const [servicesError, setServicesError] = useState("");
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();
  useEffect(() => {
    if (!id) return;
    let active = true;
    setServicesLoading(true);
    setServicesError("");
    Promise.allSettled([
      getShop(id),
      getShopServices(id),
      apiFetch<any[]>('/service-listings/catalog/tree'),
    ]).then(([shopResult, servicesResult, treeResult]) => {
      if (!active) return;
      if (shopResult.status === "fulfilled") setShop(shopResult.value);
      else setServicesError("商家信息加载失败，请刷新重试");
      const serviceValues = servicesResult.status === "fulfilled" ? servicesResult.value || [] : [];
      setServices(serviceValues);
      if (servicesResult.status === "rejected") setServicesError("服务项目暂时加载失败，请稍后重试");
      const tree = treeResult.status === "fulfilled" ? treeResult.value || [] : [];
      setServiceTree(tree);
      setServicesLoading(false);
    }).catch(() => { if (active) { setServicesError("服务项目暂时加载失败，请稍后重试"); setServicesLoading(false); } });
    return () => { active = false; };
  }, [id]);
  if (!shop)
    return (
      <div className="page-content">
        <Card loading />
      </div>
    );
  const submit = (v: any) =>
    Modal.confirm({
      title: "确认提交预约？",
      content: "提交后平台将联系并安排服务。",
      okText: "确认预约",
      cancelText: "取消",
      onOk: async () => {
        setSubmitting(true);
        try {
          let addressId = v.addressId;
          if (!addressId) {
            const a = await postJson<any>("/addresses", {
              province: v.province,
              city: v.city,
              district: v.district,
              detail: v.detail,
              longitude: v.longitude,
              latitude: v.latitude,
              receiverName: v.contactName,
              receiverPhone: v.contactPhone,
            });
            addressId = a.id;
            form.setFieldValue("addressId", addressId);
          }
          await createAppointment({
            shopId: shop.id,
            serviceId: v.serviceId,
            addressId,
            serviceTitleInput: v.serviceTitleInput,
            requirementText: v.requirementText,
            preferredStart: v.preferredStart.toISOString(),
            preferredEnd: v.preferredEnd?.toISOString(),
            contactName: v.contactName,
            contactPhone: v.contactPhone,
            idempotencyKey: v.idempotencyKey || crypto.randomUUID(),
          });
          message.success("预约已提交");
          window.dispatchEvent(new CustomEvent("notifications:refresh"));
          setOpen(false);
          form.resetFields();
        } catch (e) {
          message.error(e instanceof Error ? e.message : "预约失败");
        } finally { setSubmitting(false);
        }
      },
    });
  return (
    <div className="page-content">
      <Card className="soft-card">
        <Typography.Title level={2}>{shop.shopName}</Typography.Title>
        <Typography.Paragraph>
          {shop.shopIntro || "专业家政服务商家"}
        </Typography.Paragraph>
        {shop.galleryUrls?.length ? <div className="shop-gallery">
          <Carousel autoplay dots>
            {shop.galleryUrls.map((url) => <div key={url}><Image src={url} preview style={{ width: "100%", height: "100%", objectFit: "cover" }} /></div>)}
          </Carousel>
        </div> : null}
        <div className="shop-detail-service-tags">
          {shop.serviceNames?.map((name) => <Tag color="blue" key={name}>{name}</Tag>)}
          {shop.tags?.map((tag) => <Tag key={tag}>{tag}</Tag>)}
        </div>
        {services.length ? <div style={{ display: "grid", gap: 12, marginTop: 16 }}>
          {services.map((service) => <Card key={service.id} size="small" title={service.title} extra={service.basePrice != null ? <Tag color="blue">起步价 ¥{service.basePrice}</Tag> : null}>
            {service.summary ? <Typography.Paragraph type="secondary" style={{ marginBottom: 6 }}>{service.summary}</Typography.Paragraph> : null}
            {service.description ? <Typography.Paragraph ellipsis={{ rows: 3 }} style={{ marginBottom: 6 }}>{service.description}</Typography.Paragraph> : null}
            <Typography.Text type="secondary">计价：{service.pricingUnit === "HOUR" ? "按小时" : service.pricingUnit === "SQUARE_METER" ? "按平方米" : "按次"}{service.durationMinutes ? ` · 参考时长 ${service.durationMinutes} 分钟` : ""}</Typography.Text>
          </Card>)}
        </div> : null}
        <Descriptions
          items={[
            {
              key: "address",
              label: "地址",
              children:
                shop.addressDetail ||
                `${shop.city || ""}${shop.district || ""}`,
            },
            {
              key: "area",
              label: "服务覆盖",
              children: shop.serviceRadiusKm ? `门店周边 ${shop.serviceRadiusKm} 公里${shop.serviceArea ? `（${shop.serviceArea}）` : ""}` : (shop.serviceArea || "以商家说明为准"),
            },
            {
              key: "status",
              label: "状态",
              children: shop.status === "OPEN" ? "营业中" : "暂不营业",
            },
          ]}
        />
        {servicesError && <Alert type="warning" showIcon message={servicesError} style={{ marginTop: 16 }} />}
        <Button
          type="primary"
          disabled={shop.status !== "OPEN"}
          onClick={() => setOpen(true)}
          style={{ marginTop: 20 }}
        >
          预约服务
        </Button>
      </Card>
      <Modal
        title="提交预约"
        open={open}
        onCancel={() => setOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={submitting}
        destroyOnClose
        width={640}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={submit}
          onFinishFailed={({ errorFields }) => message.warning(errorFields[0]?.errors?.[0] || "请补全预约信息")}
          initialValues={{
            preferredStart: dayjs().add(1, "day").hour(9).minute(0),
            idempotencyKey: crypto.randomUUID(),
          }}
        >
          <Form.Item
            name="serviceId"
            label="服务项目"
            rules={[{ required: true }]}
          >
            <Select
              showSearch
              optionFilterProp="label"
              style={{ width: "100%" }}
              placeholder={servicesLoading ? "正在加载服务" : services.length ? "请选择服务项目" : "该商家暂未配置服务项目"}
              options={serviceSelectGroups(serviceTree, services) as any}
              loading={servicesLoading}
              notFoundContent={servicesLoading ? "正在加载服务" : "暂无可用服务"}
              onChange={(serviceId) => form.setFieldValue("serviceTitleInput", services.find((service) => service.id === serviceId)?.title)}
            />
          </Form.Item>
          <Form.Item name="serviceTitleInput" hidden><Input /></Form.Item>
          <Form.Item name="addressId" hidden><Input /></Form.Item>
          <Form.Item name="idempotencyKey" hidden><Input /></Form.Item>
          <Form.Item
            name="preferredStart"
            label="预约开始时间"
            rules={[{ required: true }]}
          >
            <DatePicker
              showTime
              format="YYYY-MM-DD HH:mm"
              style={{ width: "100%" }}
            />
          </Form.Item>
          <Form.Item name="preferredEnd" label="预约结束时间">
            <DatePicker
              showTime
              format="YYYY-MM-DD HH:mm"
              style={{ width: "100%" }}
            />
          </Form.Item>
          <Form.Item
            name="requirementText"
            label="需求描述"
            rules={[{ required: true }]}
          >
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="region" label="所在地区" rules={[{ required: true, message: "请选择省 / 市 / 区" }]}>
            <Cascader options={CHINA_REGIONS} showSearch placeholder="请选择省 / 市 / 区" changeOnSelect onChange={(path) => { const [province, city, district] = path as string[]; form.setFieldsValue({ province, city, district }); }} />
          </Form.Item>
          <Form.Item name="province" hidden><Input /></Form.Item><Form.Item name="city" hidden rules={[{ required: true, message: "请选择城市" }]}><Input /></Form.Item><Form.Item name="district" hidden><Input /></Form.Item>
          <Form.Item label="服务位置"><AmapPicker value={form.getFieldsValue()} onChange={(location) => form.setFieldsValue(location)} /></Form.Item>
          <Form.Item name="detail" label="详细地址" rules={[{ required: true }]}><Input placeholder="地图选点后，请补充门牌号等详细信息" /></Form.Item>
          <Form.Item name="longitude" hidden rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="latitude" hidden rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item
            name="contactName"
            label="联系人"
            rules={[{ required: true }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="contactPhone"
            label="联系电话"
            rules={[{ required: true }]}
          >
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
