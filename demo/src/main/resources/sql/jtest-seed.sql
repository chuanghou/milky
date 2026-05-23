-- jtest 集成测：供 item/update 等接口使用（profile jtest 时加载）
insert into item (id, title, user_id, user_name, amount, store_code, price, version, deleted, channel_enum, ratio, gmt_create, gmt_modified)
values (90001, 'jtest_seed', 10086, 'tom', 0, '', null, 0, 0, 'ALI', null, current_timestamp, current_timestamp);
