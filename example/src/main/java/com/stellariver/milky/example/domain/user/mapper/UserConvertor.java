package com.stellariver.milky.example.domain.user.mapper;

import com.stellariver.milky.example.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConvertor {

    UserConvertor instance = Mappers.getMapper(UserConvertor.class);

    User to(UserDO userDO);

    UserDO to(User user);

}
