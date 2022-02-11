package com.stellariver.milky.example.domain.student.command;

import com.stellariver.milky.domain.support.command.Command;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChangeNameCommand extends ChangeCommand {

    private String targetName;

    public ChangeNameCommand(Long studentId, String targetName) {
        super(studentId);
        this.targetName = targetName;
    }

}
