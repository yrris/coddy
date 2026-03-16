create table if not exists app_user (
    id bigserial primary key,
    email varchar(320) not null unique,
    password_hash varchar(255),
    display_name varchar(128) not null default 'New User',
    avatar_url text,
    auth_provider varchar(32) not null default 'LOCAL',
    provider_user_id varchar(128),
    user_role varchar(32) not null default 'USER',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    is_deleted boolean not null default false
);

create index if not exists idx_app_user_provider
    on app_user (auth_provider, provider_user_id);

create table if not exists app_project (
    id bigserial primary key,
    owner_user_id bigint not null references app_user (id),
    app_name varchar(128) not null,
    description text,
    init_prompt text,
    code_gen_type varchar(32) not null,
    deploy_key varchar(32) not null unique,
    deploy_status varchar(32) not null default 'DRAFT',
    preview_url text,
    cover_url text,
    is_featured boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    is_deleted boolean not null default false
);

create index if not exists idx_app_project_owner
    on app_project (owner_user_id, created_at desc);

create table if not exists chat_history (
    id bigserial primary key,
    project_id bigint not null references app_project (id) on delete cascade,
    sender_type varchar(16) not null,
    content text not null,
    message_status varchar(16) not null default 'SUCCESS',
    created_at timestamptz not null default now()
);

create index if not exists idx_chat_history_project_cursor
    on chat_history (project_id, id desc);
