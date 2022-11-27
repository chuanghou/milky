package com.stellariver.milky.demo.adapter.controller;

import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.demo.application.ItemAbility;
import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.domain.item.repository.ItemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("item")
public class ItemController {

    ItemAbility itemAbility;

    ItemRepository itemRepository;

    @GetMapping("publish")
    public Result<Item> publish(String title) {
        Item item = itemAbility.publishItem(10086L, title);
        return Result.success(item);
    }

    @GetMapping("get")
    public Result<Item> getItem(Long itemId) {
        Item item = itemRepository.queryById(itemId);
        return Result.success(item);
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
