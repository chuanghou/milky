package com.stellariver.milky.demo;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.stellariver.milky.common.base.BizEx;
import com.stellariver.milky.demo.common.enums.ChannelEnum;
import com.stellariver.milky.demo.infrastructure.database.entity.InvocationStoreDO;
import com.stellariver.milky.demo.infrastructure.database.entity.ItemDO;
import com.stellariver.milky.demo.infrastructure.database.mapper.InvocationStoreMapper;
import com.stellariver.milky.demo.infrastructure.database.mapper.ItemDOMapper;
import com.stellariver.milky.infrastructure.base.database.CursorOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * 覆盖 {@link com.stellariver.milky.infrastructure.base.database.MilkyMapper}：
 * 乐观锁封装、游标读取（Item 表）。
 * <p>
 * 主键统一为父类 {@code id}（{@code @TableId}），与
 * {@link com.stellariver.milky.infrastructure.base.database.AbstractMpDO}
 * 中 {@code @TableLogic(delval = "id")} 一致；{@link ItemDO} 通过 {@code itemId} 访问同一主键值。
 * </p>
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MilkyMapperTest {

        private static final long INV_BASE_ID = 9_600_000_000L;
        private static final long ITEM_CURSOR_BASE = 9_610_000_000L;

        @Autowired
        InvocationStoreMapper invocationStoreMapper;

        @Autowired
        ItemDOMapper itemDOMapper;

        @Autowired
        JdbcTemplate jdbcTemplate;

        @Test
        void updateByIdOrThrow_successAndConflict() {
                jdbcTemplate.update("DELETE FROM invocation_store WHERE id >= ? AND id < ?", INV_BASE_ID,
                                INV_BASE_ID + 100);

                InvocationStoreDO row = InvocationStoreDO.builder()
                                .id(INV_BASE_ID)
                                .operatorId("a")
                                .operatorName("n")
                                .operatorSource("s")
                                .success(true)
                                .build();
                Assertions.assertEquals(1, invocationStoreMapper.insert(row));

                InvocationStoreDO loaded = invocationStoreMapper.selectById(INV_BASE_ID);
                Assertions.assertNotNull(loaded);
                loaded.setOperatorId("b");
                invocationStoreMapper.updateByIdOrThrow(loaded);

                InvocationStoreDO stale = invocationStoreMapper.selectById(INV_BASE_ID);
                Assertions.assertNotNull(stale);
                stale.setOperatorId("c");
                stale.setVersion(stale.getVersion() - 1);
                Assertions.assertThrows(BizEx.class,
                                () -> invocationStoreMapper.updateByIdOrThrow(stale));
        }

        @Test
        void deleteByIdOrThrow_successAndNotFound() {
                jdbcTemplate.update("DELETE FROM invocation_store WHERE id >= ? AND id < ?", INV_BASE_ID + 100,
                                INV_BASE_ID + 200);

                InvocationStoreDO row = InvocationStoreDO.builder()
                                .id(INV_BASE_ID + 1)
                                .operatorId("x")
                                .operatorName("y")
                                .operatorSource("z")
                                .success(false)
                                .build();
                Assertions.assertEquals(1, invocationStoreMapper.insert(row));

                invocationStoreMapper.deleteById(INV_BASE_ID + 1);
                Assertions.assertDoesNotThrow(
                                () -> invocationStoreMapper.deleteById(INV_BASE_ID + 1));
        }

        @Test
        void cursorIterator_itemByItemIdColumn() {
                jdbcTemplate.update("DELETE FROM item WHERE id >= ? AND id < ?", ITEM_CURSOR_BASE,
                                ITEM_CURSOR_BASE + 50);

                List<Long> ids = LongStream.range(0, 5).map(i -> ITEM_CURSOR_BASE + i).boxed()
                                .collect(Collectors.toList());
                for (Long itemId : ids) {
                        ItemDO item = ItemDO.builder()
                                        .itemId(itemId)
                                        .title("cursor-" + itemId)
                                        .userId(1L)
                                        .userName("u")
                                        .amount(0L)
                                        .storeCode("cursor-store")
                                        .channelEnum(ChannelEnum.JD)
                                        .build();
                        Assertions.assertEquals(1, itemDOMapper.insert(item));
                }

                CursorOptions<ItemDO> opts = CursorOptions
                                .of(() -> Wrappers.lambdaQuery(ItemDO.class).eq(ItemDO::getStoreCode, "cursor-store"),
                                                ItemDO::getId)
                                .cursorColumn("id")
                                .batchSize(2);

                List<Long> scanned = new ArrayList<>();
                itemDOMapper.cursorConsumer(item -> scanned.add(item.getItemId()), opts);
                Collections.sort(scanned);
                Assertions.assertEquals(ids, scanned);
        }

        @Test
        void cursorIterator_mappedToLong() {
                jdbcTemplate.update("DELETE FROM item WHERE id >= ? AND id < ?", ITEM_CURSOR_BASE + 100,
                                ITEM_CURSOR_BASE + 150);

                ItemDO item = ItemDO.builder()
                                .itemId(ITEM_CURSOR_BASE + 100)
                                .title("map-one")
                                .userId(1L)
                                .userName("u")
                                .amount(0L)
                                .storeCode("map-store")
                                .channelEnum(ChannelEnum.JD)
                                .build();
                Assertions.assertEquals(1, itemDOMapper.insert(item));

                CursorOptions<ItemDO> opts = CursorOptions
                                .of(() -> Wrappers.lambdaQuery(ItemDO.class).eq(ItemDO::getStoreCode, "map-store"),
                                                ItemDO::getId)
                                .cursorColumn("id");

                List<Long> mappedIds = new ArrayList<>();
                itemDOMapper.cursorConsumer(opts, ItemDO::getId, mappedIds::add);
                Assertions.assertEquals(Collections.singletonList(ITEM_CURSOR_BASE + 100), mappedIds);
        }
}
