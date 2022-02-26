package com.stellariver.milky.example.domain.user.context;

import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.context.PrepareKey;
import com.stellariver.milky.domain.support.context.PrepareProcessor;
import com.stellariver.milky.example.domain.user.command.ChangeCommand;

public class UserPrepareProcessor implements PrepareProcessor {

    @PrepareKey("grade")
    public void prepareGrade(ChangeCommand command, Context context) {
        context.put("grade", 730);
    }

    @PrepareKey("age")
    public void prepareAge(ChangeCommand command, Context context) {
        context.put("age", 18);
    }

}
