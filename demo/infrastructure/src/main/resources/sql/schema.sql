
drop table if exists invocation_store;

create table invocation_store
(
    id            bigint      not null,
    operator_id   varchar(10) not null,
    operator_name varchar(20) not null,
    operator_source varchar(50) not null,
    success tinyint not null,
    version int not null,
    deleted int not null,
    gmt_create datetime not null,
    gmt_modified datetime not null,
    constraint invocation_store_id_uk_index
        unique (id)
);

alter table invocation_store
    add primary key (id);

drop table if exists item;

create table item
(
    item_id bigint not null,
    title   varchar(100) not null,
    user_id   bigint not null,
    user_name   varchar(100) not null,
    amount bigint not null,
    store_code varchar(100) not null,
    price varchar(100),
    version int not null,
    deleted int not null,
    channel_enum varchar(20) not null,
    ratio bigint,
    gmt_create datetime not null,
    gmt_modified datetime not null,
    constraint item_item_id_uk_index
        unique (item_id)
);

alter table item
    add primary key (item_id);

drop table if exists inventory;

create table inventory
(
    item_id bigint not null,
    amount bigint not null,
    store_code varchar(100) not null,
    version int not null,
    deleted int not null,
    gmt_create datetime not null,
    gmt_modified datetime not null,
    constraint inventory_item_id_uk_index
        unique (item_id)
);

alter table inventory
    add primary key (item_id);

drop table if exists message_store;

create table message_store
(
    id            bigint       not null,
    aggregate_id  varchar(50)  not null,
    trigger_id    bigint       not null,
    class_name    varchar(200) null,
    invocation_id bigint       not null,
    version int not null,
    deleted int not null,
    gmt_create datetime not null,
    gmt_modified datetime not null,
    constraint message_store_id_uk_index
        unique (id)
);

alter table message_store
    add primary key (id);

drop table if exists unique_id;

create table unique_id
(
    name_space varchar(50) not null,
    id bigint not null,
    step bigint not null,
    version int not null,
    constraint namespace_uk_index unique (name_space)
);

alter table unique_id
    add primary key (name_space);

-- auto-generated definition
create table demo_meta_unit
(
    meta_unit_id   int          null,
    name           varchar(64)  null,
    province       varchar(16)  null,
    unit_type      varchar(16)  null,
    source_id      int          null,
    capacity       varchar(256) null,
    generator_type varchar(16)  null
);