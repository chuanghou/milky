package com.stellariver.milky.demo.adapter.repository;

import com.stellariver.milky.demo.domain.item.Item;
import com.stellariver.milky.demo.domain.item.repository.ItemRepository;
import com.stellariver.milky.demo.infrastructure.database.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.ItemDOMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemRepositoryImpl implements ItemRepository {

    ItemDOMapper itemDOMapper;

    @Override
    public Optional<Item> getByItemId(Long itemId) {
        ItemDO itemDO = itemDOMapper.selectById(itemId);
        if (itemDO != null) {
            Item item = Item.builder().itemId(itemDO.getItemId()).title(itemDO.getTitle()).build();
            return Optional.of(item);
        }
        return Optional.empty();
    }
}
