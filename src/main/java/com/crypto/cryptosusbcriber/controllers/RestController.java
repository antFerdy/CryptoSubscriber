package com.crypto.cryptosusbcriber.controllers;


import com.crypto.cryptosusbcriber.model.Order;
import net.openhft.chronicle.core.values.LongValue;
import net.openhft.chronicle.map.ChronicleMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@org.springframework.web.bind.annotation.RestController
public class RestController {

    @Autowired
    private ChronicleMap<LongValue, Order> df;

    @GetMapping("/api/best")
    public Flux<Map<String, List<Order>>> get10Best() { //TODO refactor to functional style
        Map<String, List<Order>> orders = df.values().stream().collect(Collectors.groupingBy(Order::getSide));
        orders.get("Buy").sort((o1, o2) -> Float.compare(o2.getPrice(), o1.getPrice()));
        orders.get("Sell").sort((o1, o2) -> Float.compare(o1.getPrice(), o2.getPrice()));

        List<Order> limitedOrders = orders.get("Buy").stream().limit(5).collect(Collectors.toList());
        orders.put("Buy", limitedOrders);

        limitedOrders = orders.get("Sell").stream().limit(5).collect(Collectors.toList());
        orders.put("Sell", limitedOrders);

        return Flux.just(orders);

    }
}
