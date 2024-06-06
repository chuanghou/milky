package com.stellariver.milky.domain.support.command;

import com.stellariver.milky.domain.support.base.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author houchuang
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
abstract public class Command extends Message {

    public int lockExpireMils() {
        return 5000;
    }

}
