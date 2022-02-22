package com.stellariver.milky.example.domain.user.command;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChangeNameCommand extends ChangeCommand {

    private String targetName;

    public ChangeNameCommand(Long userId, String targetName) {
        super(userId);
        this.targetName = targetName;
    }

}
