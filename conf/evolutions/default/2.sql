
-- !Ups

create table scalademo.users
(
    id         serial       not null constraint users_pk primary key,
    firstName       varchar(64)  not null,
    lastName        varchar(64)  not null,
    password        varchar(128) not null,
    email           varchar(100) not null,
    role            varchar(16)  not null,
    birthDate       timestamp    not null,
    address         varchar(100)  not null,
    phoneNumber     varchar(16)  not null
);

create unique index users_id_uindex
    on scalademo.users (id);

create unique index users_email_uindex
    on scalademo.users (email);

create table scalademo.products
(
	id              serial          not null constraint posts_pk primary key,
	productName     varchar(16)  not null,
	price           decimal(19,2) not null,
	expDate         timestamp    not null
);

create unique index products_id_uindex
    on scalademo.products (id);

-- !Downs

DROP TABLE scalademo.users;
DROP TABLE scalademo.products;
