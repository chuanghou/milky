package com.stellariver.milky.demo.application;

import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.common.tool.util.StreamMap;
import com.stellariver.milky.demo.basic.TypedEnums;
import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.demo.domain.item.command.ItemTitleUpdateCommand;
import com.stellariver.milky.demo.domain.item.dependency.UserInfo;
import com.stellariver.milky.demo.domain.item.repository.ItemRepository;
import com.stellariver.milky.demo.domain.item.repository.UserInfoRepository;
import com.stellariver.milky.domain.support.base.Typed;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static com.stellariver.milky.demo.basic.ErrorEnums.ITEM_NOT_EXIST;

/**
 * @author houchuang
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemAbility {

    ItemRepository itemRepository;

    IdBuilder idBuilder;

    UserInfoRepository userInfoRepository;

    @Transactional(rollbackFor = Throwable.class)
    public Item publishItem(Long userId, String title) {
        Long itemId = idBuilder.build();
        ItemCreateCommand command = ItemCreateCommand.builder().userId(userId)
                .userInfo(userInfoRepository.getUserInfo(userId))
                .itemId(itemId).title(title).amount(0L).storeCode("")
                .build();
        Map<Typed<?>, Object> parameters = StreamMap.<Typed<?>, Object>init()
                .put(TypedEnums.employee, new Employee("110", "tom"))
                .getMap();
        return (Item) CommandBus.accept(command, parameters);
    }


    @Transactional(rollbackFor = Throwable.class)
    public void changeTitle(Long itemId, String newTitle, Employee operator) {
        Optional<Item> itemOptional = itemRepository.queryByIdOptional(itemId);
        BizException.trueThrow(!itemOptional.isPresent(), ITEM_NOT_EXIST.message("找不到相应item，itemId:" + itemId));
        ItemTitleUpdateCommand command = ItemTitleUpdateCommand.builder().itemId(itemId).updateTitle(newTitle).build();
        Map<Typed<?>, Object> parameters = StreamMap.<Typed<?>, Object>init()
                .put(TypedEnums.employee, operator)
                .getMap();
        CommandBus.accept(command, parameters);
    }
}
