package com.stellariver.milky.example.domain.user.command;

import com.stellariver.milky.domain.support.command.Command;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChangeCommand extends Command {

    private Long userId;

    @Override
    public String getAggregationId() {
        return userId.toString();
    }

    @Override
    public void setAggregationId(String aggregationId) {
        userId = Long.valueOf(aggregationId);
    }
}
