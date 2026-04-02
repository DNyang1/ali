package com.finalProject.ali.order.service;

import com.finalProject.ali.cart.domain.Cart;
import com.finalProject.ali.cart.mapper.CartItemMapper;
import com.finalProject.ali.cart.mapper.CartMapper;
import com.finalProject.ali.order.domain.Order;
import com.finalProject.ali.order.domain.OrderItem;
import com.finalProject.ali.order.dto.*;
import com.finalProject.ali.order.mapper.OrderItemMapper;
import com.finalProject.ali.order.mapper.OrderMapper;
import com.finalProject.ali.pricing.PricingService;
import com.finalProject.ali.product.dao.SkuStockDAO;
//태민
import com.finalProject.ali.product.dao.CustomOrderSheetDAO;

import com.finalProject.ali.product.dto.CustomOrderSheetDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SkuStockDAO skuStockDAO;
    private final PricingService pricingService;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    //태민
    private final CustomOrderSheetDAO customOrderSheetDAO;

    @Override
    @Transactional
    public OrderCreateResponse createOrder(String userId, OrderCreateRequest request) {

        // 1. 서버 측 가격 재검산 및 총액 검증
        AtomicLong computedTotalAmount = new AtomicLong(0L);
        request.getItems().forEach(item -> {
            long serverUnitPrice = pricingService.getFinalUnitPrice(item.getSkuId(), item.getQuantity());
            computedTotalAmount.addAndGet(serverUnitPrice * item.getQuantity());
        });

        if (computedTotalAmount.get() != request.getTotalAmount()) {
            throw new IllegalArgumentException("주문 금액이 일치하지 않습니다. (서버: " + computedTotalAmount.get() + ", 요청: " + request.getTotalAmount() + ")");
        }

        // 2. 주문 생성
        Order order = new Order();
        order.setUserId(userId);
        order.setAddressId(request.getAddressId());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus("CREATED");
        orderMapper.insert(order);
        //태민
        CustomOrderSheetDTO tmpSheet = null;
        if (request.getSheetId() != null) {
            customOrderSheetDAO.linkOrderId(request.getSheetId(), order.getOrderId());
            tmpSheet = customOrderSheetDAO.findById(request.getSheetId());
        }
        final CustomOrderSheetDTO sheet = tmpSheet;
        final String sheetOptionsText = (sheet == null ? null : sheet.getOptionsText());

        // 3. 주문 아이템 생성 및 재고 선점
        Cart activeCart = cartMapper.findActiveCart(userId);
        
        request.getItems().forEach(item -> {
            // 재고 차감 (선점)
            int affectedRows = skuStockDAO.deductStock(item.getSkuId(), item.getQuantity());
            if (affectedRows == 0) {
                throw new IllegalArgumentException("상품 재고가 부족합니다. (SKU: " + item.getSkuId() + ")");
            }

            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getOrderId());
            oi.setSkuId(item.getSkuId());
            oi.setQuantity(item.getQuantity());
            
            // 클라이언트 값 대신 서버 재계산 가격 사용 권장 (여기서는 검증 완료했으므로 그대로 사용)
            oi.setUnitPrice(item.getUnitPrice());
            oi.setProductName(item.getProductName());
            
            //태민
            if (sheet != null) {
                oi.setOptionSummary(sheetOptionsText);
            } else {
                oi.setOptionSummary(item.getOptionSummary());
            }

            orderItemMapper.insert(oi);

            // 4. 장바구니 상품 제거
            if (activeCart != null) {
                cartItemMapper.deleteByCartIdAndSkuId(activeCart.getCartId(), item.getSkuId());
            }
        });


        OrderCreateResponse res = new OrderCreateResponse();
        res.setOrderId(order.getOrderId());
        res.setTotalAmount(order.getTotalAmount());
        return res;
    }

    @Override
    public List<OrderSummaryResponse> getMyOrders(String userId) {
        return orderMapper.findMyOrders(userId);
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long orderId, String userId) {

        Order order = orderMapper.findByIdAndUser(orderId, userId);

        if (order == null) {
            throw new IllegalArgumentException("주문을 찾을수 없음");
        }

        List<OrderItemResponse> items = orderItemMapper.findByOrderId(orderId);

        OrderDetailResponse res = new OrderDetailResponse();
        res.setOrderId(order.getOrderId());
        res.setStatus(order.getStatus());
        res.setTotalAmount(order.getTotalAmount());
        res.setCreatedAt(order.getCreatedAt().toString());
        res.setItems(items);

        return res;
    }

    @Override
    public List<SupplierOrderItemResponse> getSupOrders(String supplierId) {
        return orderMapper.findSupplierOrderItems(supplierId);
    }

    @Override
    public SupplierOrderItemDetailResponse getSupOrderDetail(Long orderItemId, String supplierId) {
        return orderItemMapper.findSupplierOrderItemDetail(orderItemId, supplierId);
    }

    @Override
    public void shipOrderItem(Long orderItemId, String supplierId, SupplierShipRequest request) {
        orderItemMapper.updateSupplierShipping(
                orderItemId,
                supplierId,
                request.getCarrier(),
                request.getTrackingNo(),
                "SHIPPED");
        skuStockDAO.deductOnShip(orderItemId);
    }


}
