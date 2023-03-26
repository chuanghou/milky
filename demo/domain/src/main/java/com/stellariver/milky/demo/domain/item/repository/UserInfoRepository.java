package com.stellariver.milky.demo.domain.item.repository;

import com.stellariver.milky.demo.basic.UserInfo;
import com.stellariver.milky.domain.support.dependency.Traced;

/**
 * @author houchuang
 */
public interface UserInfoRepository {

    @Traced
    UserInfo getUserInfo(Long userId);

}
