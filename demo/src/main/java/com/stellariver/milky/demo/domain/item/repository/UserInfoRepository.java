package com.stellariver.milky.demo.domain.item.repository;

import com.stellariver.milky.demo.domain.item.dependency.UserInfo;

public interface UserInfoRepository {

    UserInfo getUserInfo(Long userId);

}
