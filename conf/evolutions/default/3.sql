
-- !Ups

create table scalademo.orders
(
    id              serial          not null constraint orders_pd primary key,
    userId           bigint         not null,
    totalPrice       decimal(19,2)  not null,
    orderDate        timestamp      not null
);

create unique index orders_id_uindex
    on scalademo.orders (id);

create table scalademo.order_items
(
	id            serial          not null constraint orderitem_pk primary key,
	orderId       bigint          not null,
	productId     bigint          not null,
	price         decimal(19,2)   not null,
	quantity      bigint          not null
);

create unique index orderitem_id_uindex
    on scalademo.order_items (id);

-- !Downs

DROP TABLE scalademo.orders;
DROP TABLE scalademo.order_items;
