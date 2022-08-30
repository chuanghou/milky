package com.stellariver.milky.demo.domain.item.repository;

import com.stellariver.milky.demo.domain.item.dependency.UserInfo;

public interface UserInfoRepository {

    public UserInfo getUserInfo(Long userId);

}
