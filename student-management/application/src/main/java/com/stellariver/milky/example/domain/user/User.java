package com.stellariver.milky.example.domain.user;

import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.domain.support.base.AggregateRoot;
import com.stellariver.milky.domain.support.command.CommandHandler;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.example.domain.user.command.ChangeNameCommand;
import com.stellariver.milky.example.domain.user.event.NameChangeEvent;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends AggregateRoot {

    private final static Logger log = Logger.getLogger(User.class);

    private Long userId;

    private String name;

    private Integer age;

    private String email;

    @Override
    public String getAggregateId() {
        return userId.toString();
    }

    @CommandHandler(requiredKeys = {"grade"})
    public User handle(ChangeNameCommand command, Context context) {
        Integer grade = (Integer) context.get("grade");
        Integer age = (Integer) context.get("age");
        log.with("grade", grade).info("message");
        log.with("age", age).info("message");
        String oldName = name;
        name = command.getTargetName() + grade;
        context.addEvent(new NameChangeEvent(command.getUserId(), oldName, name));
        return this;
    }

}
