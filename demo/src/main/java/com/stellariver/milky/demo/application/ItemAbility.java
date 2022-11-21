package com.stellariver.milky.demo.application;

import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.tool.exception.BizException;
import com.stellariver.milky.common.tool.util.StreamMap;
import com.stellariver.milky.demo.basic.NameTypes;
import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.demo.domain.item.command.ItemTitleUpdateCommand;
import com.stellariver.milky.demo.domain.item.repository.ItemRepository;
import com.stellariver.milky.domain.support.base.NameType;
import com.stellariver.milky.domain.support.command.CommandBus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static com.stellariver.milky.demo.basic.ErrorEnums.ITEM_NOT_EXIST;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemAbility {

    ItemRepository itemRepository;

    @Transactional
    public Item publishItem(Long userId, String title) {
        ItemCreateCommand command = ItemCreateCommand.builder().userId(userId)
                .itemId(1L).title(title).build();
        Map<NameType<?>, Object> parameters = StreamMap.<NameType<?>, Object>init()
                .put(NameTypes.employee, new Employee("110", "tom"))
                .getMap();
        return (Item) CommandBus.accept(command, parameters);
    }


    @Transactional
    public void changeTitle(Long itemId, String newTitle, Employee operator) {
        Optional<Item> itemOptional = itemRepository.queryByIdOptional(itemId);
        BizException.trueThrow(!itemOptional.isPresent(), ITEM_NOT_EXIST.message("找不到相应item，itemId:" + itemId));
        ItemTitleUpdateCommand command = ItemTitleUpdateCommand.builder().itemId(itemId).updateTitle(newTitle).build();
        Map<NameType<?>, Object> parameters = StreamMap.<NameType<?>, Object>init()
                .put(NameTypes.employee, new Employee("110", "tom"))
                .getMap();
        CommandBus.accept(command, parameters);
    }
}