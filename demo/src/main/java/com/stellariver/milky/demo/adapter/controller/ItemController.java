//package com.stellariver.milky.demo.adapter.controller;
//
//import com.stellariver.milky.common.base.Employee;
//import com.stellariver.milky.common.base.Result;
//import com.stellariver.milky.common.tool.common.BizException;
//import com.stellariver.milky.demo.domain.item.Item;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class ItemController {
//
//
//    @GetMapping("publish")
//    public Result<Boolean> publish(String title) {
//        Item item = itemService.publishItem(10086L, title);
//        return Result.success();
//    }
//
//    @GetMapping("update")
//    public Result<Boolean> update(Long itemId, String newTitle) {
//        Employee jack = new Employee("001", "jack");
//        try {
//            itemService.changeTitle(itemId, newTitle, jack);
//        } catch (BizException bizException) {
//            return Result.with(bizException.getErrors());
//        }
//        return Result.success();
//    }
//
//}
