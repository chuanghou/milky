package com.stellariver.milky.demo.adapter.repository.domain;

import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.infrastructure.database.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.ItemDOMapper;
import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.dependency.DomainRepository;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemDomainRepository implements DomainRepository<Item> {

    ItemDOMapper itemDOMapper;

    @Override
    public void save(Item item, Context context) {
        ItemDO itemDO = ItemDO.builder().itemId(item.getItemId())
                .title(item.getTitle())
                .sellerId(item.getUserId())
                .userName(item.getUserName())
                .build();
        itemDOMapper.insert(itemDO);
    }

    @Override
    public Optional<Item> getByAggregateId(String aggregateId, Context context) {
        Long itemId = Long.valueOf(aggregateId);
        ItemDO itemDO = itemDOMapper.selectById(itemId);
        if (itemDO != null) {
            Item item = Item.builder().itemId(itemDO.getItemId()).title(itemDO.getTitle()).build();
            return Optional.of(item);
        }
        return Optional.empty();
    }

    @Override
    public void updateByAggregateId(Item item, Context context) {
        ItemDO itemDO = ItemDO.builder().itemId(item.getItemId()).title(item.getTitle()).build();
        itemDOMapper.updateById(itemDO);
    }

}
