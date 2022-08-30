package com.stellariver.milky.demo.application;//package com.stellariver.milky.demo.application;
//
//import com.stellariver.milky.common.base.Employee;
//import com.stellariver.milky.common.tool.common.BizException;
//import com.stellariver.milky.common.tool.util.StreamMap;
//import com.stellariver.milky.demo.domain.item.Item;
//import com.stellariver.milky.demo.domain.item.command.ItemCreateCommand;
//import com.stellariver.milky.demo.domain.item.command.ItemUpdateCommand;
//import com.stellariver.milky.demo.domain.item.repository.ItemRepository;
//import com.stellariver.milky.domain.support.command.CommandBus;
//import com.stellariver.milky.domain.support.dependency.IdBuilder;
//import com.stellariver.milky.domain.support.util.BeanUtil;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//import java.util.Optional;
//
//import static com.stellariver.milky.demo.basic.ErrorEnum.ITEM_NOT_EXIST;
//
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class ItemService {
//
//    CommandBus commandBus;
//
//    ItemRepository itemRepository;
//
//    public Item publishItem(Long sellerId, String title) {
//        IdBuilder idBuilder = BeanUtil.getBean(IdBuilder.class);
//        Long itemId = idBuilder.build("item");
//        ItemCreateCommand command = ItemCreateCommand.builder()
//                .sellerId(sellerId)
//                .itemId(itemId).title(title).build();
//        Map<String, Object> parameters = StreamMap.<String, Object>init().put("title", title)
//                .put("operator", Employee.system)
//                .getMap();
//        return (Item) CommandBus.accept(command, parameters);
//    }
//
//
//    public void changeTitle(Long itemId, String newTitle, Employee operator) {
//        Optional<Item> itemOptional = itemRepository.getByItemId(itemId);
//        BizException.trueThrow(!itemOptional.isPresent(), ITEM_NOT_EXIST.message("找不到相应item，itemId:" + itemId));
//        ItemUpdateCommand command = ItemUpdateCommand.builder().itemId(itemId).newTitle(newTitle).build();
//        Map<String, Object> parameters = StreamMap.<String, Object>init().put("newTitle", newTitle)
//                .put("operator", operator)
//                .getMap();
//        CommandBus.accept(command, parameters);
//    }
//}
