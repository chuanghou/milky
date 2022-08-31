package com.stellariver.milky.demo.domain.item.dependency;

import com.stellariver.milky.demo.basic.NameTypes;
import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.demo.domain.item.repository.UserInfoRepository;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.context.DependencyKey;
import com.stellariver.milky.domain.support.context.DependencyPrepares;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ItemDependencyPrepare implements DependencyPrepares {

    final UserInfoRepository userInfoRepository;

    @DependencyKey("userName")
    public void prepare(ItemCreateCommand command, Context context) {
        UserInfo userInfo = userInfoRepository.getUserInfo(command.getUserId());
        context.putDependency(NameTypes.userInfo, userInfo);
    }
}
