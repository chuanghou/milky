package com.stellariver.milky.example.domain.user.repository;

import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.repository.DomainRepository;
import com.stellariver.milky.example.domain.user.User;
import com.stellariver.milky.example.domain.user.mapper.UserConvertor;
import com.stellariver.milky.example.domain.user.mapper.UserDO;
import com.stellariver.milky.example.domain.user.mapper.UserDOMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepository implements DomainRepository<User> {

    final UserDOMapper userDOMapper;

    @Override
    public User getByAggregateId(String aggregateId, Context context) {
        UserDO userDO = userDOMapper.selectById(aggregateId);
        return UserConvertor.instance.to(userDO);
    }

    @Override
    public void save(User user, Context context) {
        UserDO userDO = UserConvertor.instance.to(user);
        UserDO dbUserDO = userDOMapper.selectById(userDO.getId());
        if (dbUserDO == null) {
            userDOMapper.insert(userDO);
        } else {
            userDOMapper.updateById(userDO);
        }
    }

}
