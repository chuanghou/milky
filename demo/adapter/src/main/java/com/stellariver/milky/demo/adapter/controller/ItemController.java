package com.stellariver.milky.demo.adapter.controller;

import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.base.ExceptionType;
import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.common.tool.util.Json;
import com.stellariver.milky.demo.application.ItemAbility;
import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.domain.item.repository.ItemRepository;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author houchuang
 */
@CustomLog
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
    public Result<Item> getItem() {
        Item build = Item.builder().itemId(1L).amount(100L).price("d").build();
        log.info(Json.toJson(build));
        methodA();
        return Result.success(build);
    }

    private void methodA() {
        methodB();
    }

    private void methodB() {
        log.error("TestT", new RuntimeException("TESTroke"));
    }

    @GetMapping("update")
    public Result<Boolean> update(Long itemId, String newTitle) {
        Employee jack = new Employee("001", "jack");
        try {
            itemAbility.changeTitle(itemId, newTitle, jack);
        } catch (BizEx bizEx) {
            return Result.error(bizEx.getErrors(), ExceptionType.BIZ);
        }
        return Result.success();
    }

}
