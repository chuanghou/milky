package com.stellariver.milky.demo.adapter.repository;

import com.stellariver.milky.demo.domain.item.dependency.UserInfo;
import com.stellariver.milky.demo.domain.item.repository.UserInfoRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserInfoRepositoryImpl implements UserInfoRepository {

    @Override
    public UserInfo getUserInfo(Long userId) {
        return UserInfo.builder().userId(userId).userName("小明").permission(true).build();
    }

}
