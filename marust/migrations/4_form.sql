drop table if exists public.tbl_form;

create table public.tbl_form
(
    form_id                                        bigserial
        primary key,
    created_at                                     timestamp(6)     not null,
    updated_at                                     timestamp(6)     not null,
    birthday                                       varchar(255)     not null,
    gender                                         varchar(6)       not null,
    name                                           varchar(255)     not null,
    phone_number                                   varchar(255)     not null,
    changed_to_regular                             boolean          not null,
    cover_letter                                   text             not null,
    statement_of_purpose                           text             not null,
    graduation_type                                varchar(25)      not null,
    graduation_year                                varchar(4)       not null,
    school_address                                 varchar(40),
    school_code                                    varchar(10),
    school_location                                varchar(20),
    school_name                                    varchar(21),
    teacher_mobile_phone_number                    varchar(255),
    teacher_name                                   varchar(255),
    teacher_phone_number                           varchar(255),
    examination_number                             bigint
        unique,
    attendance1_absence_count                      integer,
    attendance1_class_absence_count                integer,
    attendance1_early_leave_count                  integer,
    attendance1_lateness_count                     integer,
    attendance2_absence_count                      integer,
    attendance2_class_absence_count                integer,
    attendance2_early_leave_count                  integer,
    attendance2_lateness_count                     integer,
    attendance3_absence_count                      integer,
    attendance3_class_absence_count                integer,
    attendance3_early_leave_count                  integer,
    attendance3_lateness_count                     integer,
    volunteer_time1                                integer,
    volunteer_time2                                integer,
    volunteer_time3                                integer,
    original_type                                  varchar(255),
    address                                        varchar(255)     not null,
    detail_address                                 varchar(255)     not null,
    zone_code                                      varchar(255)     not null,
    parent_name                                    varchar(255)     not null,
    parent_phone_number                            varchar(255)     not null,
    parent_relation                                varchar(20)      not null,
    attendance_score                               integer          not null,
    bonus_score                                    integer          not null,
    coding_test_score                              double precision,
    depth_interview_score                          double precision,
    first_round_score                              double precision not null,
    ncs_score                                      double precision,
    subject_grade_score                            double precision not null,
    third_grade_first_semester_subject_grade_score double precision,
    total_score                                    double precision,
    volunteer_score                                integer          not null,
    status                                         varchar(30)      not null,
    type                                           varchar(30)      not null,
    user_id                                        bigint           not null
        references public.tbl_user
);

drop table if exists public.tbl_subject;

create table public.tbl_subject
(
    form_id           bigint      not null
        references public.tbl_form,
    achievement_level varchar(2)  not null,
    grade             integer,
    score             integer,
    semester          integer,
    subject_name      varchar(15) not null
);

