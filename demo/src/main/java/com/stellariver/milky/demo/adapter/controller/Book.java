package com.stellariver.milky.demo.adapter.controller;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Book {

    String name;

    String price;

    Long number;

}
