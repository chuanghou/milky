-- auto-generated definition
create table id_builder
(
    id         bigint auto_increment,
    name_space varchar(50) not null,
    constraint id_builder_id_uindex
        unique (id)
);

alter table id_builder
    add primary key (id);


-- auto-generated definition
create table invocation_store
(
    id            bigint      not null,
    operator_id   varchar(10) not null,
    operator_name varchar(20) not null,
    operator_source varchar(50) not null,
    constraint invocation_store_id_uindex
        unique (id)
);

alter table invocation_store
    add primary key (id);


-- auto-generated definition
create table item
(
    item_id bigint auto_increment,
    title   varchar(100) not null,
    seller_id   bigint not null,
    user_name   varchar(100) not null,
    constraint item_item_id_uindex
        unique (item_id)
);

alter table item
    add primary key (item_id);


-- auto-generated definition
create table message_store
(
    id            bigint       not null,
    aggregate_id  varchar(50)  not null,
    trigger_id    bigint       not null,
    class_name    varchar(200) null,
    invocation_id bigint       not null,
    constraint message_store_id_uindex
        unique (id)
);

alter table message_store
    add primary key (id);
