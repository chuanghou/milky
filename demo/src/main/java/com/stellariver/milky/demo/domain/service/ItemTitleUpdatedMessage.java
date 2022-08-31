package com.stellariver.milky.demo.domain.service;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemTitleUpdatedMessage {

    Long itemId;

    String oldTitle;

    String newTitle;

}
