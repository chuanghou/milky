-- auto-generated definition
drop table if exists id_builder;

create table id_builder
(
    id bigint auto_increment,
    name_space varchar(50) not null,
    start bigint not null,
    unique_id bigint not null,
    step int not null,
    ceiling bigint not null,
    duty char(16) not null,
    alarm_threshold bigint not null,
    version int not null,
    deleted int not null,
    gmt_create datetime not null,
    gmt_modified datetime not null,
    constraint id_builder_namespace_uk_index
        unique (name_space)
);

alter table id_builder
    add primary key (id);

drop table if exists invocation_store;
-- auto-generated definition
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
-- auto-generated definition
create table item
(
    item_id bigint not null,
    title   varchar(100) not null,
    user_id   bigint not null,
    user_name   varchar(100) not null,
    amount bigint not null,
    store_code varchar(100),
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
-- auto-generated definition
create table inventory
(
    item_id bigint not null,
    amount bigint not null,
    store_code varchar(100),
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
-- auto-generated definition
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