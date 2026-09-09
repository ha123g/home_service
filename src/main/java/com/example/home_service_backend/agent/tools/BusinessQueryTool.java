package com.example.home_service_backend.agent.tools;

import com.example.home_service_backend.service.AppointmentService;
import com.example.home_service_backend.service.OrderService;
import com.example.home_service_backend.service.ShopService;
import com.example.home_service_backend.vo.appointment.AppointmentPageView;
import com.example.home_service_backend.vo.merchant.MerchantApplicationView;
import com.example.home_service_backend.vo.order.OrderPageView;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 当前登录用户的只读业务 Tool。模型只能选择 Tool，用户身份、数据范围和资源归属
 * 始终由普通业务 Service 根据 SecurityContext 校验。
 */
@Component
public class BusinessQueryTool {
    private final OrderService orderService;
    private final AppointmentService appointmentService;
    private final ShopService shopService;

    public BusinessQueryTool(OrderService orderService,
                             AppointmentService appointmentService,
                             ShopService shopService) {
        this.orderService = orderService;
        this.appointmentService = appointmentService;
        this.shopService = shopService;
    }

    @Tool(name = "query_my_orders", description = "查询当前登录用户有权查看的订单列表和状态，只读；用户询问有没有订单、订单记录或订单状态时使用")
    public OrderPageView myOrders() {
        return orderService.list(0, 10);
    }

    @Tool(name = "query_my_appointments", description = "查询当前登录用户有权查看的预约申请及状态，只读；用户询问预约消息、预约记录或有没有预约时使用")
    public AppointmentPageView myAppointments() {
        return appointmentService.list(0, 10);
    }

    @Tool(name = "query_my_merchant_application", description = "查询当前登录用户最近一次商家入驻申请、审核状态和审核备注，只读")
    public MerchantApplicationView myMerchantApplication() {
        return shopService.myApplication();
    }
}
