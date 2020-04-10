package com.crypto.cryptosusbcriber.config;

import com.crypto.cryptosusbcriber.model.Order;
import net.openhft.chronicle.core.values.LongValue;
import net.openhft.chronicle.map.ChronicleMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChronicleConfig {

    @Bean
    public ChronicleMap<LongValue, Order> buildChronicle() { //TODO make persistent
        return ChronicleMap.of(LongValue.class, Order.class)
                .name("level2")
                .entries(50)
                .averageValue(Order.builder().id(15599304700L).side("sell").price(6952).symbol("XBTUSD").build())
                .create();
    }



}
