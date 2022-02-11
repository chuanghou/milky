package com.stellariver.milky.example.domain.student.command;

import com.stellariver.milky.domain.support.command.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChangeCommand extends Command {

    private Long studentId;

    @Override
    public String getAggregationId() {
        return studentId.toString();
    }

    @Override
    public void setAggregationId(String aggregationId) {
        studentId = Long.valueOf(aggregationId);
    }
}
