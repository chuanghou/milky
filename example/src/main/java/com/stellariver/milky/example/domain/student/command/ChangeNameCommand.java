package com.stellariver.milky.example.domain.student.command;

import com.stellariver.milky.domain.support.command.Command;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChangeNameCommand extends Command {

    private Long studentId;

    private String targetName;

    @Override
    public String getAggregationId() {
        return studentId.toString();
    }

    @Override
    public void setAggregationId(String aggregationId) {
        studentId = Long.valueOf(aggregationId);
    }
}
