package com.stellariver.milky.example.domain.user.command;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ChangeNameCommand extends ChangeCommand {

    final private String targetName;

    public ChangeNameCommand(Long userId, String targetName) {
        super(userId);
        this.targetName = targetName;

    }

}
