package com.crypto.cryptosusbcriber.model;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Order implements Serializable {
    private Long id;
    private String symbol;
    private String side;
    private int size;
    private float price;


}
