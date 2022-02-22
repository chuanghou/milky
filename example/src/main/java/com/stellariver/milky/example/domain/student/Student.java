package com.stellariver.milky.example.domain.student;

import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.command.CommandHandler;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.example.domain.student.command.ChangeNameCommand;
import com.stellariver.milky.example.domain.student.event.NameChangeEvent;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Student extends AggregateRoot {

    private final static Logger log = Logger.getLogger(Student.class);

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

    @CommandHandler(requiredKeys = {"grade"})
    public Student handle(ChangeNameCommand command, Context context) {
        Integer grade = (Integer) context.get("grade");
        Integer age = (Integer) context.get("age");
        log.with("grade", grade).info("message");
        log.with("age", age).info("message");
        String oldName = name;
        name = command.getTargetName() + grade;
        context.addEvent(new NameChangeEvent(command.getStudentId(), oldName, name));
        return this;
    }

}
