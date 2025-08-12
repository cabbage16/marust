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
        constraint uk_crr34qvg2qqdfhc1sv0eg5km2
            unique,
    uuid         uuid         not null
        constraint uk_oyrv7l3883ewdbfhd4vtg8w23
            unique
);