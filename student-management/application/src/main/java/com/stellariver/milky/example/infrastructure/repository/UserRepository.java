package com.stellariver.milky.example.infrastructure.repository;

import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.repository.DomainRepository;
import com.stellariver.milky.example.domain.user.User;
import com.stellariver.milky.example.infrastructure.mapper.UserDO;
import com.stellariver.milky.example.infrastructure.mapper.UserDOMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepository implements DomainRepository<User> {

    final UserDOMapper userDOMapper;

    @Override
    public User getByAggregateId(String aggregateId, Context context) {
        UserDO userDO = userDOMapper.selectById(aggregateId);
        if (userDO == null) {
            return null;
        }
        return User.builder().userId(userDO.getId()).age(userDO.getAge())
                .name(userDO.getName()).email(userDO.getEmail()).build();
    }

    @Override
    public User getByAggregateId(String aggregateId) {
        UserDO userDO = userDOMapper.selectById(aggregateId);
        return User.builder().userId(userDO.getId()).age(userDO.getAge())
                .name(userDO.getName()).email(userDO.getEmail()).build();
    }

    @Override
    public void save(User user, Context context) {
        UserDO userDO = UserDO.builder()
                .id(user.getUserId())
                .age(user.getAge())
                .name(user.getName())
                .email(user.getEmail()).build();
        UserDO dbUserDO = userDOMapper.selectById(userDO.getId());
        if (dbUserDO == null) {
            userDOMapper.insert(userDO);
        } else {
            userDOMapper.updateById(userDO);
        }
    }

}
