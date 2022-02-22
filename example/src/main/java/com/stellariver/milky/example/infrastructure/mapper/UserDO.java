package com.stellariver.milky.example.infrastructure.mapper;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("user")
public class UserDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String name;

    private Integer age;

    private String email;

}
