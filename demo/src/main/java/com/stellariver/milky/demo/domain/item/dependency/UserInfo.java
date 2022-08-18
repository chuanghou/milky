package com.stellariver.milky.demo.domain.item.dependency;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfo {

    Long userId;

    String userName;

    boolean permission;

}