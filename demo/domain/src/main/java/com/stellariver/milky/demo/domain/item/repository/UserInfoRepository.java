package com.stellariver.milky.demo.domain.item.repository;

import com.stellariver.milky.demo.basic.UserInfo;

/**
 * @author houchuang
 */
public interface UserInfoRepository {

    UserInfo getUserInfo(Long userId);

}
