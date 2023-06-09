package com.ssafy.bora.repository.broadcast;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.annotations.QueryProjection;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.bora.dto.main.*;
import com.ssafy.bora.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.ssafy.bora.entity.QBroadcast.broadcast;
import static com.ssafy.bora.entity.QStation.station;
import static com.ssafy.bora.entity.QBroadcastOrder.broadcastOrder;

@RequiredArgsConstructor
public class BroadcastRepositoryCustomImpl implements BroadcastRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BroadcastResDTO> findAllByCategoryAndSort(SearchCondition condition, String sortCondition) {
        BooleanBuilder searchBuilder = searchMoodByBuilder(condition);
        OrderSpecifier[] orderSpecifiers = createOrderSpecifier(sortCondition);
        QUser broadUser= broadcast.user;
        return queryFactory
                .select(new QBroadcastResDTO(
                        broadUser.nickName,
                        broadUser.id,
                        broadcast.broadcastImg,
                        station.name,
                        broadcast.title,
                        broadcast.mood,
                        station.category,
                        broadcast.sessionId,
                        station.startTime,
                        station.endTime))
                .from(broadcast)
                .join(station).on(broadUser.eq(station.user))
                .join(broadcastOrder).on(broadUser.eq(broadcastOrder.user))
                .where(
                        broadcast.endBroad.isNull().and(categoryEq(condition.getCategory()))
                        ,
                        searchBuilder)
                .orderBy(orderSpecifiers)
                .fetch();
    }

    private BooleanExpression categoryEq(String category){
        return StringUtils.hasText(category)?station.category.eq(category) :null;
    }
    private BooleanBuilder searchMoodByBuilder(SearchCondition condition) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if(condition.getMood()!=null) {
            for (String s : condition.getMood()) {
                booleanBuilder.or(broadcast.mood.contains(s));
            }
        }
        return booleanBuilder;
    }

    private OrderSpecifier[] createOrderSpecifier(String condition) {
        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();
        if (Objects.isNull(condition)) orderSpecifiers.add(new OrderSpecifier(Order.DESC, broadcast.startBroad));
        else if (condition.equals("maxview")) orderSpecifiers.add((new OrderSpecifier(Order.DESC, station.maxViewer)));
        else if(condition.equals("follow")) orderSpecifiers.add((new OrderSpecifier(Order.DESC, broadcastOrder.cnt)));
        return orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]);
    }
}
