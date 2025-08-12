create table public.tbl_question
(
    question_id bigserial
        primary key,
    created_at  timestamp(6)  not null,
    updated_at  timestamp(6)  not null,
    category    varchar(30)   not null,
    content     varchar(1024) not null,
    title       varchar(64)   not null
);