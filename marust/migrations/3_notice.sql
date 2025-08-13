create table public.tbl_notice
(
    notice_id  bigserial
        primary key,
    title      varchar(64)   not null,
    content    varchar(1024) not null,
    created_at timestamp(6)  not null,
    updated_at timestamp(6)  not null
);

create table if not exists public.tbl_notice_file
(
    notice_id      bigint not null
            references public.tbl_notice
            on delete cascade,
    file_name varchar(255)
);