package org.yearup.service;

import org.springframework.stereotype.Service;
import org.yearup.models.*;
import org.yearup.repository.OrderLineItemRepository;
import org.yearup.repository.OrderRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;



import java.time.LocalDateTime;

@Service
public class OrderService
{
    private final OrderRepository orderRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final ShoppingCartService shoppingCartService;
    private final ProfileService profileService;

    public OrderService(OrderRepository orderRepository,
                        OrderLineItemRepository orderLineItemRepository,
                        ShoppingCartService shoppingCartService,
                        ProfileService profileService)
    {
        this.orderRepository = orderRepository;
        this.orderLineItemRepository = orderLineItemRepository;
        this.shoppingCartService = shoppingCartService;
        this.profileService = profileService;
    }

    public Order checkout(int userId)
    {
        // grabbing cart for what they're buying, profile for where it's shipping to
        ShoppingCart cart = shoppingCartService.getByUserId(userId);

        // so reject this before touching the database at all
        if (cart.getItems().isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot checkout an empty cart");
        }

        Profile profile = profileService.getByUserId(userId);

        // save the order header first so we get a real orderId to attach line items to
        Order order = new Order();
        order.setUserId(userId);
        order.setDate(LocalDateTime.now());
        order.setAddress(profile.getAddress());
        order.setCity(profile.getCity());
        order.setState(profile.getState());
        order.setZip(profile.getZip());
        order.setShippingAmount(0); // no shipping calc logic in scope, just defaulting to 0 for now

        Order savedOrder = orderRepository.save(order);

        // one line item per product that was in the cart
        for (ShoppingCartItem item : cart.getItems().values())
        {
            OrderLineItem lineItem = new OrderLineItem();
            lineItem.setOrderId(savedOrder.getOrderId());
            lineItem.setProductId(item.getProduct().getProductId());
            lineItem.setSalesPrice(item.getProduct().getPrice());
            lineItem.setQuantity(item.getQuantity());
            lineItem.setDiscount(item.getDiscountPercent());

            orderLineItemRepository.save(lineItem);
        }

        // order's placed, cart's done being a cart
        shoppingCartService.clearCart(userId);

        return savedOrder;
    }
}