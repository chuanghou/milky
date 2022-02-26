package com.stellariver.milky.example.adpater.controller;

import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.example.domain.user.User;
import com.stellariver.milky.example.domain.user.command.ChangeNameCommand;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class UserController {

    @Resource
    private CommandBus commandBus;

    @RequestMapping("changeName")
    public Result<User> changeName(Long userId, String targetName) {
        ChangeNameCommand changeNameCommand = new ChangeNameCommand(userId, targetName);
        User user = (User) commandBus.send(changeNameCommand);
        return Result.success(user);
    }

}
