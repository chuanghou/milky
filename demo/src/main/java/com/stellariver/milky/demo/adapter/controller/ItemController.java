package com.stellariver.milky.demo.adapter.controller;

import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.demo.application.ItemAbility;
import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.spring.partner.limit.EnableRateLimit;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemController {

    ItemAbility itemAbility;

    @GetMapping("publish")
    public Result<Boolean> publish(String title) {
        Item item = itemAbility.publishItem(10086L, title);
        return Result.success();
    }

    @GetMapping("update")
    public Result<Boolean> update(Long itemId, String newTitle) {
        Employee jack = new Employee("001", "jack");
        try {
            itemAbility.changeTitle(itemId, newTitle, jack);
        } catch (BizException bizException) {
            return Result.error(bizException.getErrors());
        }
        return Result.success();
    }

}
