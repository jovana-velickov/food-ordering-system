package com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantapproval;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;

//not trigger by clients
public interface RestaurantApprovalListener {

    void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse);

    void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse);

}
