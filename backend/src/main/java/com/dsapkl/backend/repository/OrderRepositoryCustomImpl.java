package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.OrderStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

import static com.dsapkl.backend.entity.QItem.item;
import static com.dsapkl.backend.entity.QItemImage.itemImage;
import static com.dsapkl.backend.entity.QMember.member;
import static com.dsapkl.backend.entity.QOrder.order;
import static com.dsapkl.backend.entity.QOrderItem.orderItem;

public class OrderRepositoryCustomImpl implements OrderRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    public OrderRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<OrderDto> findOrderDetail(Long memberId, OrderStatus orderStatus) {

        List<OrderDto> orderDtos = findOrderDtos(memberId, orderStatus);

        orderDtos.forEach(o-> {
            List<OrderItemDto> findOrderItemDto = findOrderItemDtos(o.getOrderId());
            o.setOrderItemDtoList(findOrderItemDto);
        });

        return orderDtos;
    }

    // 주문 목록 조회 쿼리 (주문취소, 주문 완료 동적 쿼리 TODO)
    @Override
    public List<OrderDto> findOrderDtos(Long memberId, OrderStatus orderStatus) {

        return queryFactory
                .select(new QOrderDto(
                        order.id,
                        order.totalPrice,
                        order.orderDate,
                        order.status
                ))
                .from(order)
                .join(order.member, member)
                .where(member.id.eq(memberId),orderStatusEq(orderStatus))
                .orderBy(order.orderDate.desc())
                .fetch();
    }

    private BooleanExpression orderStatusEq(OrderStatus orderStatus) {

        return orderStatus != null ? order.status.eq(orderStatus) : null;

//        if (orderStatus == null) {
//            return null;
//        }
//        return order.status.eq(orderStatus);
    }

    private List<OrderItemDto> findOrderItemDtos(Long orderId) {

        return queryFactory
                .select(Projections.constructor(OrderItemDto.class,
                        item.name,
                        orderItem.orderPrice,
                        orderItem.count,
                        itemImage.storeName
                ))
                .from(orderItem)
                .join(orderItem.item, item)
                .join(item.itemImageList, itemImage)
                .where(orderItem.order.id.eq(orderId),
                        itemImage.firstImage.eq("Y")  //대표 상품 이미지만 고르기!
                )
                .fetch();
    }



}
