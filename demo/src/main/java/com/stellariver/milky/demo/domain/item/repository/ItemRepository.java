package com.stellariver.milky.demo.domain.item.repository;

import com.stellariver.milky.demo.domain.item.Item;

import java.util.Optional;

public interface ItemRepository {

    Optional<Item> getByItemId(Long itemId);

}
