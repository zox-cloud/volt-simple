package com.example.delivery.controllers;


import com.example.delivery.entity.*;
import com.example.delivery.service.MenuItemService;
import com.example.delivery.service.OrderService;
import com.example.delivery.service.RestaurantService;
import com.example.delivery.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class DeliveryController {
    @Autowired
    private UsersService usersService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private OrderService orderService;




    @GetMapping("/")
    public String getRestaurants(Model model) {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        model.addAttribute("restaurants", restaurants);
        return "index";
    }

    @GetMapping("/restaurant/{id}")
    public String restaurant(@PathVariable Long id, Model model) {
        Restaurant restaurant = restaurantService.getRestaurantById(id);
        if (restaurant == null) {
            throw new RuntimeException("Restaurant not found for ID: " + id);
        }
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("menuItems", menuItemService.getMenuItemsByRestaurantId(id));
        return "restaurant";
    }




    @PostMapping("/order")
    public String placeOrder(@RequestParam("menuItemIds") List<Long> menuItemIds,
                             @RequestParam("quantities") List<Integer> quantities , Model model) {
        Users customer = usersService.findByUserId(1L).orElseThrow(() -> new RuntimeException("User Not Found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = IntStream.range(0 , menuItemIds.size())
                .mapToObj(i -> {
                    Long menuItemId = menuItemIds.get(i);
                    Integer quantity = quantities.get(i);


                    MenuItem menuItem = menuItemService.getMenuItemById(menuItemId);
                    OrderItem orderItem = new OrderItem();
                     orderItem.setMenuItem(menuItem);
                     orderItem.setOrder(order);
                     orderItem.setQuantity(quantity);  // Or dynamically set quantity
                    return orderItem;

                }).collect(Collectors.toList());

        order.setOrderItems(orderItems);
        orderService.saveOrder(order);
        return "redirect:/orders/" + order.getId();

//        List<OrderItem> orderItems = menuItemIds.stream().map(id -> {
//            MenuItem menuItem = menuItemService.getMenuItemById(id);
//            OrderItem orderItem = new OrderItem();
//            orderItem.setMenuItem(menuItem);
//            orderItem.setOrder(order);
//            orderItem.setQuantity(1);  // Or dynamically set quantity
//            return orderItem;
//        }).collect(Collectors.toList());
//
//        order.setOrderItems(orderItems);
//        orderService.saveOrder(order);
//
//        // Redirect to order details page after placing the order
//        return "redirect:/orders/" + order.getId();

    }




    @GetMapping("/orders/confirmation")
    public String orderConfirmation() {
        return "menu";
    }


}
