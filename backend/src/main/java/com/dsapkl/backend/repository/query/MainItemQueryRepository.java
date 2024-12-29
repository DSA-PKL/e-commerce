package com.dsapkl.backend.repository.query;

import com.dsapkl.backend.controller.dto.ItemSearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.dsapkl.backend.entity.QItem.item;
import static com.dsapkl.backend.entity.QItemImage.itemImage;

@Repository
public class MainItemQueryRepository {

    private final JPAQueryFactory queryFactory;

    public MainItemQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Page<MainItemDto> findMainItem(Pageable pageable, ItemSearchCondition condition) {

        List<MainItemDto> content = queryFactory
                .select(new QMainItemDto(
                        item.id,
                        item.name,
                        item.price,
                        itemImage.storeName
                ))
                .from(item)
                .join(item.itemImageList, itemImage)
                .where(itemImage.firstImage.eq("Y"),
                        itemNameContain(condition.getItemName()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        //COUNTQUERY 에서 join이 필요가 있을까? 대표 이미지
        JPAQuery<Long> total = queryFactory
                .select(item.count())
                .where(itemNameContain(condition.getItemName()))
                .from(item);

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);  //CountQuery 최적화


        /**
         * SELECT COUNT(I.ITEM_ID) FROM ITEM I
         * JOIN ITEM_IMAGE IM
         * ON I.ITEM_ID = IM.ITEM_ID
         * WHERE IM.FIRST_IMAGE='Y';
         *
         * SELECT COUNT(I.ITEM_ID) FROM ITEM I;
         *
         * SELECT * FROM ITEM I
         * JOIN ITEM_IMAGE IM
         * ON I.ITEM_ID = IM.ITEM_ID;
         */
    }
    private BooleanExpression itemNameContain(String itemName) {
        return StringUtils.hasText(itemName) ? item.name.contains(itemName) : null;
    }
}
