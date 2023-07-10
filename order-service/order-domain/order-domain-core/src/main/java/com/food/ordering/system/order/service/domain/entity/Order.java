package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<OrderId> {
    private final CustomerId customerId;
    private final RestaurantId restaurantId;
    private final StreetAddress deliveryAddress;
    private final Money price;
    private final List<OrderItem> items;

    //not final zato sto se setuju tokom biznis logike nakon kreiranja Order entity
    private TrackingId trackingId;
    private OrderStatus orderStatus;
    private List<String> failureMessages;

    private Order(Builder builder) {
        super.setId(builder.orderId);
        customerId = builder.customerId;
        restaurantId = builder.restaurantId;
        deliveryAddress = builder.deliveryAddress;
        price = builder.price;
        items = builder.items;
        trackingId = builder.trackingId;
        orderStatus = builder.orderStatus;
        failureMessages = builder.failureMessages;
    }


    public CustomerId getCustomerId() {return customerId;}

    public RestaurantId getRestaurantId() {return restaurantId;}

    public StreetAddress getDeliveryAddress() {return deliveryAddress;}

    public Money getPrice() {return price;}

    public List<OrderItem> getItems() {return items;}

    public TrackingId getTrackingId() {return trackingId;}

    public OrderStatus getOrderStatus() {return orderStatus;}

    public List<String> getFailureMessages() {return failureMessages;}


    public static final class Builder {
        private OrderId orderId;
        private CustomerId customerId;
        private RestaurantId restaurantId;
        private StreetAddress deliveryAddress;
        private Money price;
        private List<OrderItem> items;
        private TrackingId trackingId;
        private OrderStatus orderStatus;
        private List<String> failureMessages;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder deliveryAddress(StreetAddress val) {
            deliveryAddress = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder items(List<OrderItem> val) {
            items = val;
            return this;
        }

        public Builder trackingId(TrackingId val) {
            trackingId = val;
            return this;
        }

        public Builder orderStatus(OrderStatus val) {
            orderStatus = val;
            return this;
        }

        public Builder failureMessages(List<String> val) {
            failureMessages = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }


    //critical business rules

    //validateOrder() - it validate initial order status, total price and item prices
    //initializeOrder() - initialise default values
    //pay()- check if status is PENDING and update to PAID
    //approve() - PAID ---> APPROVED
    //initCancel() - PAID ---> CANCELING
    //cancel() - CANCELING or PENDING ---> CANCELED

    //za validate  and initalize Order koristimo OrderItem i Product entity

    public void validateOrder(){
        validateInitialOrder();
        validateTotalPrice();
        validateItemsPrice();
    }

    public void initializeOrder(){
        setId(new OrderId(UUID.randomUUID()));
        trackingId=new TrackingId(UUID.randomUUID());
        orderStatus=OrderStatus.PENDING;
        initializeOrderItems();
    }

    /////STATE CHANGING

    public void pay(){
        if(orderStatus != OrderStatus.PENDING){
            throw new OrderDomainException("Order is not in correct state for paying!");
        }
        orderStatus = OrderStatus.PAID;
    }

    public void approve(){
        if(orderStatus != OrderStatus.PAID){
            throw new OrderDomainException("Order is not in correct state for approve!");
        }
        orderStatus = OrderStatus.APPROVED;
    }

    public void initCancel(List<String> failureMessages){
        if(orderStatus != OrderStatus.PAID){
            throw new OrderDomainException("Order is not in correct state for canceling!");
        }
        orderStatus = OrderStatus.CANCELING;
        updateFailureMessages(failureMessages);
    }

    public void cancel(List<String> failureMessages){
        if(!(orderStatus == OrderStatus.PAID || orderStatus == OrderStatus.PENDING)){
            throw new OrderDomainException("Order is not in correct state for cancel!");
        }
        orderStatus = OrderStatus.CANCELING;
        updateFailureMessages(failureMessages);
    }

    private void updateFailureMessages(List<String> failureMessages) {
        if(this.failureMessages != null && failureMessages != null){
            this.failureMessages.addAll(failureMessages.stream().filter(message -> !message.isEmpty() ).toList());
        }
        if(this.failureMessages == null){
            this.failureMessages = failureMessages;
        }
    }



    private void validateInitialOrder() {
        if(orderStatus != null || getId() != null){
            throw new OrderDomainException("Order is not in correct state for initialization!");
        }
    }
    private void validateTotalPrice() {
        if(price == null || !price.isGreaterThenZero() ){
            throw new OrderDomainException("Total price must be greater then zero!");
        }
    }
    private void validateItemsPrice() {
        Money orderItemsTotal= items.stream().map(orderItem -> {
            validateItemPrice(orderItem);
            return orderItem.getSubTotal();
        }).reduce(Money.ZERO, Money::add);

        if(!price.equals(orderItemsTotal)){
            throw new OrderDomainException("Total price: " + price.getAmount()
            + "is not equal to Order items total: " + orderItemsTotal.getAmount() + "!");
        }
    }

    private void validateItemPrice(OrderItem orderItem) {
        if(!orderItem.isPriceValid()){
            throw new OrderDomainException("Order item price: " + orderItem.getPrice().getAmount() +
                    " is not valid for product " + orderItem.getProduct().getId().getValue());
        }
    }



    private void initializeOrderItems() {
        long itemId=1;

        for(OrderItem orderItem : items){
            orderItem.initializeOrderItem(super.getId(), new OrderItemId(itemId++));
        }
    }


}
