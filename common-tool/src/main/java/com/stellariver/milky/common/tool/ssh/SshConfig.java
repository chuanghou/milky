package com.stellariver.milky.common.tool.ssh;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SshConfig {

    String ip;
    @Builder.Default
    Integer port = 22;
    String username;
    String password;

}
