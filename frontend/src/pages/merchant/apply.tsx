import {
  Alert,
  Button,
  Card,
  Cascader,
  Descriptions,
  Form,
  Input,
  InputNumber,
  Select,
  Space,
  Tag,
  Typography,
  Upload,
  message,
} from "antd";
import { UploadOutlined } from "@ant-design/icons";
import { useEffect, useState } from "react";
import { apiFetch, postJson, uploadFile } from "../../services/api";
import {
  getMyApplication,
  type MerchantApplication,
} from "../../services/shop/shopService";
import AmapPicker from "../../components/AmapPicker";
import { CHINA_REGIONS } from "../../constants/chinaRegions";

type ImageItem = {
  imageType: string;
  objectKey: string;
  mimeType: string;
  fileSize: number;
};
const categoryOptions = (tree: any[]) => (tree || []).map((root: any) => {
  const options: { label: string; value: number }[] = [];
  const walk = (node: any, path: string[]) => {
    const next = [...path, String(node.name || "")].filter(Boolean);
    const children = node.children || [];
    if (node.id != null && !children.length) options.push({ label: next.slice(1).join(" / ") || next.join(" / "), value: Number(node.id) });
    children.forEach((child: any) => walk(child, next));
  };
  walk(root, []);
  return { label: root.name, options };
});
const categoryFlatOptions = (tree: any[]) => {
  const result: { label: string; value: number }[] = [];
  const walk = (node: any, path: string[]) => {
    const next = [...path, String(node.name || "")].filter(Boolean);
    const children = node.children || [];
    if (node.id != null && !children.length) result.push({ label: next.join(" / "), value: Number(node.id) });
    children.forEach((child: any) => walk(child, next));
  };
  (tree || []).forEach((node) => walk(node, []));
  return result;
};
const statusText: Record<string, string> = {
  PENDING: "申请中",
  APPROVED: "已入驻",
  REJECTED: "已驳回",
  CANCELLED: "已取消",
};

export default function MerchantApplyPage() {
  const [application, setApplication] = useState<MerchantApplication>();
  const [categoryTree, setCategoryTree] = useState<any[]>([]);
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [catalogError, setCatalogError] = useState("");
  const [catalogReload, setCatalogReload] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [images, setImages] = useState<ImageItem[]>([]);
  const [form] = Form.useForm();
  useEffect(() => {
    let active = true;
    setCatalogLoading(true);
    setCatalogError("");
    Promise.allSettled([apiFetch<any[]>("/service-categories/tree")])
      .then(([categoryResult]) => {
        if (!active) return;
        const tree = categoryResult.status === "fulfilled" ? categoryResult.value || [] : [];
        setCategoryTree(tree);
        if (!tree.length) {
          const reason = categoryResult.status === "rejected" ? "服务分类暂时加载失败" : "平台暂未配置服务分类";
          setCatalogError(reason);
        }
      })
      .catch(() => {
        if (active) setCategoryTree([]);
      })
      .finally(() => { if (active) setCatalogLoading(false); });
    void getMyApplication()
      .then((value) => {
        setApplication(value);
        // 驳回申请重新提交时保留已上传图片，避免用户再次填写后意外丢失证明材料。
        setImages((value.images || []).map((image) => ({
          imageType: image.imageType,
          objectKey: image.objectKey,
          mimeType: image.mimeType || "image/jpeg",
          fileSize: image.fileSize || 0,
        })));
        form.setFieldsValue({
          ...value,
          region: [value.province, value.city, value.district].filter(Boolean),
        });
      })
      .catch(() => undefined);
    return () => { active = false; };
  }, [catalogReload, form]);
  const addImage = (type: string, file: File) => {
    const purpose = type === "LOGO" ? "MERCHANT_LOGO" : "MERCHANT_GALLERY";
    void uploadFile(file, purpose)
      .then((uploaded) =>
        setImages((list) => [
          ...(type === "LOGO"
            ? list.filter((item) => item.imageType !== "LOGO")
            : list),
          {
            imageType: type,
            objectKey: uploaded.objectKey,
            mimeType: uploaded.mimeType,
            fileSize: uploaded.fileSize,
          },
        ]),
      )
      .catch((error) =>
        message.error(error instanceof Error ? error.message : "图片上传失败"),
      );
    return false;
  };
  const submit = async (values: Record<string, any>) => {
    setSubmitting(true);
    try {
      const { region, ...payload } = values;
      const result = await postJson<MerchantApplication>(
        "/merchant/applications",
        { ...payload, images },
      );
      setApplication(result);
      message.success("入驻申请已提交");
    } catch (e) {
      message.error(e instanceof Error ? e.message : "提交失败");
    } finally {
      setSubmitting(false);
    }
  };
  const approved = application?.status === "APPROVED";
  const canApply =
    !application ||
    application.status === "REJECTED" ||
    application.status === "CANCELLED";
  return (
    <div className="page-content merchant-apply-page">
      <Card style={{ maxWidth: 980, margin: "0 auto" }}>
        <Typography.Title level={3}>商家入驻</Typography.Title>
        {application && (
          <Alert
            type={
              approved
                ? "success"
                : application.status === "REJECTED"
                  ? "warning"
                  : "info"
            }
            message={
              approved
                ? "您已完成入驻"
                : `当前状态：${statusText[application.status] || "处理中"}`
            }
            description={application.reviewRemark}
            style={{ marginBottom: 20 }}
          />
        )}
        {!canApply ? (
          <Descriptions
            bordered
            column={1}
            items={[
              {
                key: "shop",
                label: "商铺名称",
                children: application?.shopName,
              },
              {
                key: "contact",
                label: "联系人",
                children: `${application?.realName}（${application?.phone}）`,
              },
              {
                key: "address",
                label: "地址",
                children: `${application?.province || ""}${application?.city || ""}${application?.district || ""}${application?.addressDetail || ""}`,
              },
              {
                key: "intro",
                label: "商铺简介",
                children: application?.intro || "—",
              },
              {
                key: "services",
                label: "服务明细",
                children: application?.serviceItems?.length ? (
                  <Space direction="vertical">
                    {application.serviceItems.map((service) => (
                      <div key={`${service.categoryId}-${service.title}`}>
                        <Tag color="blue">{service.categoryName || "服务分类"}</Tag>
                        {service.title} · 起步价 ¥{service.basePrice} / {service.pricingUnit}
                        {service.summary ? ` · ${service.summary}` : ""}
                      </div>
                    ))}
                  </Space>
                ) : application?.serviceNames?.length ? application.serviceNames.join("、") : application?.serviceArea || "—",
              },
              {
                key: "tags",
                label: "商家标签",
                children: application?.tags?.length ? application.tags.map((tag) => <Tag key={tag}>{tag}</Tag>) : "—",
              },
              {
                key: "radius",
                label: "服务覆盖半径",
                children: application?.serviceRadiusKm ? `${application.serviceRadiusKm} 公里` : "未设置",
              },
              {
                key: "images",
                label: "商铺图片",
                children: (
                  <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
                    {application?.images?.map((img) => (
                      <img
                        key={img.id || img.objectKey}
                        src={
                          img.previewUrl ||
                          `/api/files/preview?objectKey=${encodeURIComponent(img.objectKey)}`
                        }
                        alt={img.imageType}
                        style={{
                          width: 120,
                          height: 80,
                          objectFit: "cover",
                          borderRadius: 8,
                        }}
                      />
                    ))}
                  </div>
                ),
              },
            ]}
          />
        ) : (
          <Form
            form={form}
            layout="vertical"
            onFinish={submit}
            scrollToFirstError
            onFinishFailed={({ errorFields }) => {
              message.warning(
                errorFields[0]?.errors?.[0] || "请检查并补全必填项",
              );
            }}
            onValuesChange={(changed) => {
              if (changed.region) {
                const [province, city, district] = changed.region;
                form.setFieldsValue({ province, city, district });
              }
            }}
          >
            <div className="form-grid-2">
              <Form.Item
                name="realName"
                label="联系人姓名"
                rules={[{ required: true, message: "请输入联系人姓名" }]}
              >
                <Input placeholder="用于审核联系" />
              </Form.Item>
              <Form.Item
                name="phone"
                label="联系电话"
                rules={[{ required: true, message: "请输入联系电话" }]}
              >
                <Input placeholder="请输入手机号" />
              </Form.Item>
            </div>
            <Form.Item
              name="shopName"
              label="门店名称"
              rules={[{ required: true, message: "请输入门店名称" }]}
            >
              <Input placeholder="例如：暖居家政天河店" />
            </Form.Item>
            {catalogError && (
              <Alert
                type="warning"
                showIcon
                message={catalogError}
                action={<Button size="small" onClick={() => setCatalogReload((value) => value + 1)}>重新加载</Button>}
                style={{ marginBottom: 8 }}
              />
            )}
            <Form.Item
              name="categoryIds"
              label="可提供的服务"
              rules={[
                {
                  required: true,
                  type: "array",
                  min: 1,
                  message: "请至少选择一个服务分类",
                },
              ]}
            >
              <Select
                mode="multiple"
                options={categoryOptions(categoryTree) as any}
                showSearch
                optionFilterProp="label"
                maxTagCount="responsive"
                placeholder="先选择平台标准服务分类（可多选）"
                style={{ width: "100%" }}
                loading={catalogLoading}
                notFoundContent={catalogLoading ? "正在加载服务分类" : "暂无可选服务分类"}
                onChange={(values) => form.setFieldValue("categoryIds", Array.isArray(values) ? values.flat(Infinity).filter((item) => typeof item === "number" || (typeof item === "string" && /^\d+$/.test(item))).map((item) => Number(item)) : [])}
              />
            </Form.Item>
            <Form.List name="serviceItems">
              {(fields, { add, remove }) => (
                <Form.Item label="我的服务明细">
                  {fields.map((field) => (
                    <Card key={field.key} size="small" style={{ marginBottom: 12 }}>
                      <Space direction="vertical" style={{ width: "100%" }}>
                        <Form.Item {...field} name={[field.name, "categoryId"]} label="所属分类" rules={[{ required: true, message: "请选择所属分类" }]}>
                          <Select showSearch optionFilterProp="label" options={categoryFlatOptions(categoryTree)} placeholder="选择标准分类" />
                        </Form.Item>
                        <Form.Item {...field} name={[field.name, "title"]} label="服务名称" rules={[{ required: true, message: "请输入服务名称" }]}>
                          <Input placeholder="例如：三室一厅深度保洁" />
                        </Form.Item>
                        <Form.Item {...field} name={[field.name, "summary"]} label="服务简介"><Input placeholder="一句话说明服务内容" /></Form.Item>
                        <Form.Item {...field} name={[field.name, "description"]} label="详细描述"><Input.TextArea rows={2} placeholder="说明服务范围、注意事项等" /></Form.Item>
                        <Space style={{ display: "flex" }}>
                          <Form.Item {...field} name={[field.name, "pricingUnit"]} label="计价单位" rules={[{ required: true, message: "请选择计价单位" }]}><Select style={{ width: 150 }} options={[{ value: "ORDER", label: "按次" }, { value: "HOUR", label: "按小时" }, { value: "SQUARE_METER", label: "按平方米" }]} /></Form.Item>
                          <Form.Item {...field} name={[field.name, "basePrice"]} label="起步/参考价" rules={[{ required: true, message: "请输入起步价" }]}><InputNumber min={0} precision={2} addonAfter="元" /></Form.Item>
                          <Form.Item {...field} name={[field.name, "durationMinutes"]} label="参考时长"><InputNumber min={1} addonAfter="分钟" /></Form.Item>
                          <Button danger type="link" onClick={() => remove(field.name)}>删除</Button>
                        </Space>
                        <Form.Item {...field} name={[field.name, "tags"]} label="服务标签"><Select mode="tags" maxTagCount="responsive" placeholder="输入后回车添加标签" /></Form.Item>
                      </Space>
                    </Card>
                  ))}
                  <Button type="dashed" onClick={() => add({ pricingUnit: "ORDER" })} block>+ 添加一项自定义服务</Button>
                </Form.Item>
              )}
            </Form.List>
            <Form.List name="tags">
              {(fields, { add, remove }) => (
                <Form.Item label="商家标签">
                  {fields.map((field) => (
                    <Space key={field.key} align="baseline" style={{ display: "flex", marginBottom: 8 }}>
                      <Form.Item {...field} rules={[{ required: true, whitespace: true, max: 32, message: "请输入不超过 32 个字的标签" }]} noStyle>
                        <Input placeholder="输入一个服务标签" />
                      </Form.Item>
                      <Button type="link" danger onClick={() => remove(field.name)}>删除</Button>
                    </Space>
                  ))}
                  <Button type="dashed" onClick={() => add()} block>+ 添加标签</Button>
                </Form.Item>
              )}
            </Form.List>
            <div className="form-grid-2">
              <Form.Item name="serviceRadiusKm" label="服务覆盖半径（公里）" rules={[{ required: true, message: "请输入门店周边覆盖半径" }]}>
                <InputNumber min={0.1} max={500} step={0.5} precision={1} style={{ width: "100%" }} placeholder="例如 10，表示门店周边 10 公里" />
              </Form.Item>
              <Form.Item
                name="region"
                label="所在地区"
                rules={[{ required: true, message: "请选择省 / 市 / 区" }]}
              >
                <Cascader
                  options={CHINA_REGIONS}
                  showSearch
                  placeholder="请选择省 / 市 / 区"
                  changeOnSelect
                />
              </Form.Item>
            </div>
            <Form.Item name="province" hidden>
              <Input />
            </Form.Item>
            <Form.Item
              name="city"
              hidden
              rules={[{ required: true, message: "请先选择城市" }]}
            >
              <Input />
            </Form.Item>
            <Form.Item name="district" hidden>
              <Input />
            </Form.Item>
            <Form.Item label="门店位置" required>
              <AmapPicker
                value={form.getFieldsValue()}
                onChange={(location) => {
                  const next = { ...location } as any;
                  if (location.addressDetail || location.detail) next.addressDetail = location.addressDetail || location.detail;
                  form.setFieldsValue(next);
                }}
              />
            </Form.Item>
            <Form.Item
              name="addressDetail"
              label="详细地址"
              rules={[
                { required: true, message: "请补充门牌号、楼栋等详细地址" },
              ]}
            >
              <Input placeholder="地图选点后补充门牌号、楼栋和房间号" />
            </Form.Item>
            <Form.Item
              name="longitude"
              hidden
              rules={[{ required: true, message: "请在地图中选择位置" }]}
            >
              <Input />
            </Form.Item>
            <Form.Item
              name="latitude"
              hidden
              rules={[{ required: true, message: "请在地图中选择位置" }]}
            >
              <Input />
            </Form.Item>
            <Form.Item name="intro" label="门店简介">
              <Input.TextArea
                rows={3}
                maxLength={1024}
                showCount
                placeholder="简单介绍服务特色、营业时间和承接范围"
              />
            </Form.Item>
            <div className="form-grid-2">
              <Form.Item label="门店 Logo">
                <Upload
                  accept="image/*"
                  maxCount={1}
                  showUploadList
                  beforeUpload={(file) => addImage("LOGO", file)}
                >
                  <Button icon={<UploadOutlined />}>上传 Logo</Button>
                </Upload>
              </Form.Item>
              <Form.Item label="门店环境图（可选）">
                <Upload
                  accept="image/*"
                  multiple
                  showUploadList
                  beforeUpload={(file) => addImage("OTHER", file)}
                >
                  <Button icon={<UploadOutlined />}>上传图片</Button>
                </Upload>
              </Form.Item>
            </div>
            <Button
              type="primary"
              htmlType="submit"
              size="large"
              loading={submitting}
            >
              提交入驻申请
            </Button>
          </Form>
        )}
      </Card>
    </div>
  );
}
