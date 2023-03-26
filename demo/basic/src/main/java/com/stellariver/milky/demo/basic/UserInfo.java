package com.stellariver.milky.demo.basic;

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
public class UserInfo {

    Long userId;

    String userName;

    boolean permission;

}
