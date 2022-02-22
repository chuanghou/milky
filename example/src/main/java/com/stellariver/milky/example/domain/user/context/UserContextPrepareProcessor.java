package com.stellariver.milky.example.domain.user.context;

import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.context.ContextPrepareKey;
import com.stellariver.milky.domain.support.context.ContextPrepareProcessor;
import com.stellariver.milky.example.domain.user.command.ChangeCommand;

public class UserContextPrepareProcessor implements ContextPrepareProcessor {

    @ContextPrepareKey(prepareKey = "grade")
    public void prepareGrade(ChangeCommand command, Context context) {
        context.put("grade", 730);
    }

    @ContextPrepareKey(prepareKey = "age")
    public void prepareAge(ChangeCommand command, Context context) {
        context.put("age", 18);
    }

}
