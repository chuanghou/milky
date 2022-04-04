package com.stellariver.milky.demo.application;

import com.stellariver.milky.common.base.Employee;
import com.stellariver.milky.common.tool.util.StreamMap;
import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
import com.stellariver.milky.domain.support.command.CommandBus;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.dependency.IdBuilder;
import com.stellariver.milky.domain.support.invocation.Invocation;
import com.stellariver.milky.domain.support.util.BeanUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemService {

    CommandBus commandBus;

    public Item publishItem(String title) {
        IdBuilder idBuilder = BeanUtil.getBean(IdBuilder.class);
        Long itemId = idBuilder.build("item");
        ItemCreateCommand command = ItemCreateCommand.builder().itemId(itemId).title(title).build();
        Map<String, Object> parameters = StreamMap.<String, Object>init().put("title", title)
                .put("operator", Employee.system)
                .getMap();
        Context context = Context.fromParameters(parameters);
        return (Item) commandBus.send(command, context);
    }

}
