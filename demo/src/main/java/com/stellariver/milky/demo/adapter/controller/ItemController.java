package com.stellariver.milky.demo.adapter.controller;

import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.demo.application.ItemService;
import com.stellariver.milky.demo.domain.item.Item;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemController {

    ItemService itemService;

    @GetMapping("publish")
    public Result<Boolean> publish(String title) {
        Item item = itemService.publishItem(title);
        return Result.success();
    }

}
