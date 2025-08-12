create table public.tbl_user
(
    user_id      bigserial
        primary key,
    created_at   timestamp(6) not null,
    updated_at   timestamp(6) not null,
    authority    varchar(10)  not null,
    name         varchar(255) not null,
    password     varchar(60)  not null,
    phone_number varchar(255) not null
            unique,
    uuid         uuid         not null
            unique
);