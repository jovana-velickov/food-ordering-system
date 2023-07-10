package com.food.ordering.system.order.service.domain.ports.output.repository;

import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

import java.util.Optional;

public interface OrderRepository {
//repository implementation responsibility is to convert entity Order into JPA entity object and save into database
    Order save(Order order);

    Optional<Order> findByTrackingId(TrackingId trackingId);
}
