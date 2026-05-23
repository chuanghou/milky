package com.stellariver.milky.demo.client.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author houchuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDTO {

    Long itemId;

    String title;

    Long userId;

    String userName;

    Long amount;

    String storeCode;

}
