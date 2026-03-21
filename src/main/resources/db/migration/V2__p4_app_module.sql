alter table if exists app_project
    alter column deploy_key drop not null;

alter table if exists app_project
    add column if not exists deployed_time timestamptz,
    add column if not exists edit_time timestamptz,
    add column if not exists priority integer not null default 0;

create index if not exists idx_app_project_name
    on app_project (app_name);

create index if not exists idx_app_project_deploy_key
    on app_project (deploy_key);

create index if not exists idx_app_project_priority
    on app_project (priority);
