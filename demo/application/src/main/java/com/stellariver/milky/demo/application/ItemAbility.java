package com.stellariver.milky.demo.application;

import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.tool.common.Typed;
import com.stellariver.milky.common.tool.util.StreamMap;
import com.stellariver.milky.demo.common.enums.ChannelEnum;
import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.demo.domain.item.command.ItemTitleUpdateCommand;
import com.stellariver.milky.demo.domain.item.repository.ItemRepository;
import com.stellariver.milky.demo.domain.item.repository.UserInfoRepository;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.dependency.UniqueIdGetter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static com.stellariver.milky.demo.basic.ErrorEnums.ITEM_NOT_EXIST;
import static com.stellariver.milky.demo.basic.TypedEnums.EMPLOYEE;

/**
 * @author houchuang
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemAbility {

    ItemRepository itemRepository;

    UniqueIdGetter uniqueIdGetter;

    UserInfoRepository userInfoRepository;

    @Transactional
    public Item publishItem(Long userId, String title) {
        Long itemId = uniqueIdGetter.get();
        ItemCreateCommand command = ItemCreateCommand.builder().userId(userId)
                .itemId(itemId).title(title).amount(0L).storeCode("")
                .channelEnum(ChannelEnum.ALI)
                .build();
        Map<Class<? extends Typed<?>>, Object> parameters = StreamMap.<Class<? extends Typed<?>>, Object>init()
                .put(EMPLOYEE.class, new Employee("110", "tom"))
                .getMap();
        return (Item) CommandBus.accept(command, parameters);
    }


    @Transactional
    public void changeTitle(Long itemId, String newTitle, Employee operator) {
        Optional<Item> itemOptional = itemRepository.queryByIdOptional(itemId);
        BizEx.trueThrow(!itemOptional.isPresent(), ITEM_NOT_EXIST.message("找不到相应item，itemId:" + itemId));
        ItemTitleUpdateCommand command = ItemTitleUpdateCommand.builder().itemId(itemId).updateTitle(newTitle).build();
        Map<Class<? extends Typed<?>>, Object> parameters = StreamMap.<Class<? extends Typed<?>>, Object>init()
                .put(EMPLOYEE.class, operator)
                .getMap();
        CommandBus.accept(command, parameters);
    }

}
