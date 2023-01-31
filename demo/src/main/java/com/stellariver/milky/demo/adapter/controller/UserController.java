package com.stellariver.milky.demo.adapter.controller;

import com.stellariver.milky.common.base.Result;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserController {



    @PostMapping("postForBook")
    public Result<Book> postForBook() {
        Book book = Book.builder().number(34L).price("0.04").name("xiyouji").build();
        return Result.success(book);
    }


}
