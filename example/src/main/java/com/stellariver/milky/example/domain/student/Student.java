package com.stellariver.milky.example.domain.student;

import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.command.CommandHandler;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.example.domain.student.command.ChangeNameCommand;
import com.stellariver.milky.example.domain.student.event.NameChangeEvent;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Student extends AggregateRoot {

    private Long id;

    private String name;

    @Override
    public String getAggregateId() {
        return id.toString();
    }

    @Override
    public void setAggregateId(String aggregateId) {
        this.id = Long.valueOf(aggregateId);
    }

    @CommandHandler
    public void handle(ChangeNameCommand command, Context context) {
        String oldName = name;
        name = command.getTargetName();
        context.addEvent(new NameChangeEvent(command.getStudentId(), oldName, name));
    }
}
