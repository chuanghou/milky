package com.stellariver.milky.example.domain.student.event;

import com.stellariver.milky.domain.support.event.Event;
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
public class NameChangeEvent extends Event {

    private Long id;

    private String oldName;

    private String newName;

    @Override
    public String getAggregationId() {
        return id.toString();
    }

    @Override
    public void setAggregationId(String aggregationId) {
        id = Long.valueOf(aggregationId);
    }
}
