package com.stellariver.milky.example.domain.student.context;

import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.context.ContextPrepareKey;
import com.stellariver.milky.domain.support.context.ContextPrepareProcessor;
import com.stellariver.milky.example.domain.student.command.ChangeCommand;
import com.stellariver.milky.example.domain.student.command.ChangeNameCommand;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

public class StudentContextPrepareProcessor implements ContextPrepareProcessor {

    @ContextPrepareKey(prepareKey = "grade")
    public void prepareGrade(ChangeCommand command, Context context) {
        context.put("grade", 730);
    }

    @ContextPrepareKey(prepareKey = "age")
    public void prepareAge(ChangeCommand command, Context context) {
        context.put("age", 18);
    }

}
