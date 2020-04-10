package com.crypto.cryptosusbcriber.services;

import com.crypto.cryptosusbcriber.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.core.values.LongValue;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.values.Values;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class WebSocketSubscriber {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChronicleMap<LongValue, Order> df;

    @PostConstruct
    public void init() {
        WebSocketClient client = new ReactorNettyWebSocketClient();
        client.execute(URI.create("wss://testnet.bitmex.com/realtime"), new Handler()).subscribe();

    }

    public void handlePayload(String jsonStr) {
        log.debug(jsonStr);
        Stream.of(jsonStr)
                .map(this::readTree)
                .filter(jsonNode -> jsonNode.has("action"))
                .forEach(jsonNode -> {
                    String action = jsonNode.get("action").textValue();
                    log.info(action);

                    if(action.equals("delete"))     {
                        deleteFromDataFrame(jsonNode);
                    } else {
                        fillDataFrame(jsonNode);
                    }
                    log.info("Size of df {}", df.size());
                });
    }

    private void deleteFromDataFrame(JsonNode jsonNode) {
        getOrders(jsonNode).forEach(order -> {
            LongValue key = Values.newHeapInstance(LongValue.class);
            key.addValue(order.getId());
            log.debug("Removing order {}", df.get(key));
            df.remove(key);
        });
    }

    private void fillDataFrame(JsonNode jsonNode) {
        getOrders(jsonNode).forEach(order -> {
            LongValue key = Values.newHeapInstance(LongValue.class);
            key.addValue(order.getId());
            df.put(key, order);
        });
    }

    @NotNull
    private List<Order> getOrders(JsonNode jsonNode) {
        try {
            ObjectReader reader = objectMapper.readerFor(new TypeReference<List<Order>>() {
            });
            List<Order> orders = reader.readValue(jsonNode.get("data"));
            log.debug("size of limit orders {}", orders.size());
            return orders;
        } catch (Exception e) {
            log.error("Json parsing exception", e);
            throw new RuntimeException(e);
        }
    }

    private JsonNode readTree(String s) {
        try {
            return objectMapper.readTree(s);
        } catch (JsonProcessingException e) {
            log.error("Json parsing exception", e);
            throw new RuntimeException(e);
        }
    }


    public class Handler implements WebSocketHandler {

        @Override
        public List<String> getSubProtocols() {
            return null;
        }

        @Override
        public Mono<Void> handle(WebSocketSession session) {
            return session.send(Mono.just(session.textMessage("{\"op\": \"subscribe\", \"args\": [\"orderBookL2_25:XBTUSD\"]}")))
                    .thenMany(session.receive())
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(WebSocketSubscriber.this::handlePayload)
                    .then();
        }
    }
}
