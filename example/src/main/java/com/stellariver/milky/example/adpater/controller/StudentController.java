package com.stellariver.milky.example.adpater.controller;

import com.stellariver.milky.client.base.Result;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.example.domain.student.command.ChangeNameCommand;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class StudentController {

    @Resource
    private CommandBus commandBus;

    @RequestMapping("changeName")
    public Result<Void> changeName(Long studentId, String targetName) {
        ChangeNameCommand changeNameCommand = new ChangeNameCommand(studentId, targetName);
        Object result = commandBus.send(changeNameCommand);
        System.out.println(result);
        return Result.success();
    }

}
