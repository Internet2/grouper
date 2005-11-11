alter table grouper_sessions drop constraint FKF43E3C105000A8E0;
alter table grouper_stems drop constraint FKA9825437A3FBEB83;
alter table grouper_stems drop constraint FKA98254375236F20E;
alter table grouper_groups drop constraint FK723686075236F20E;
alter table grouper_groups drop constraint FK72368607A3FBEB83;
alter table grouper_factors drop constraint FK82080B311BBB05D6;
alter table grouper_factors drop constraint FK82080B311BBB7A35;
alter table grouper_attributes drop constraint FK92BF040A1E2E76DB;
alter table grouper_attributes drop constraint FK92BF040AC8A07680;
drop table grouper_members if exists;
drop table grouper_memberships if exists;
drop table grouper_sessions if exists;
drop table grouper_privileges if exists;
drop table grouper_stems if exists;
drop table grouper_types if exists;
drop table grouper_groups if exists;
drop table grouper_fields if exists;
drop table grouper_factors if exists;
drop table grouper_attributes if exists;
create table grouper_members (
   id char(32) not null,
   subject_id varchar(255),
   subject_source varchar(255),
   subject_type varchar(255),
   member_id varchar(64) not null,
   primary key (id),
   unique (member_id),
   unique (subject_id, subject_source, subject_type)
);
create table grouper_memberships (
   id char(32) not null,
   group_id char(64),
   member_id char(64),
   list_name varchar(255) not null,
   list_type varchar(255) not null,
   via_id char(64),
   depth int,
   primary key (id),
   unique (group_id, member_id, list_name, list_type, via_id, depth)
);
create table grouper_sessions (
   id char(32) not null,
   member_id char(64),
   start_time timestamp not null,
   session_id varchar(64) not null,
   primary key (id),
   unique (session_id)
);
create table grouper_privileges (
   id char(32) not null,
   version integer not null,
   name varchar(255) not null,
   is_access bit not null,
   is_naming bit not null,
   primary key (id)
);
create table grouper_stems (
   id char(32) not null,
   creator_id char(64),
   create_source varchar(255),
   create_time bigint not null,
   stem_description varchar(1024),
   display_extension varchar(255),
   display_name varchar(255),
   stem_extension varchar(255),
   modifier_id char(64),
   modify_source varchar(255),
   modify_time bigint,
   stem_name varchar(255),
   parent_stem char(64),
   stem_id varchar(64) not null,
   primary key (id),
   unique (stem_id)
);
create table grouper_types (
   id char(32) not null,
   version integer not null,
   name varchar(255) not null,
   primary key (id)
);
create table grouper_groups (
   id char(32) not null,
   creator_id char(64),
   create_source varchar(255),
   create_time bigint not null,
   group_description varchar(1024),
   display_extension varchar(255),
   display_name varchar(255),
   group_extension varchar(255),
   modifier_id char(64),
   modify_source varchar(255),
   modify_time bigint,
   group_name varchar(255),
   parent_stem char(64),
   group_id varchar(64) not null,
   primary key (id),
   unique (group_id)
);
create table grouper_fields (
   id char(32) not null,
   field_type varchar(255) not null,
   field_name varchar(255) not null,
   primary key (id),
   unique (field_name)
);
create table grouper_factors (
   id char(32) not null,
   klass varchar(255) not null,
   version integer not null,
   node_a_id char(64),
   node_b_id char(64),
   primary key (id)
);
create table grouper_attributes (
   id char(32) not null,
   version integer not null,
   group_id char(64),
   field_id char(64),
   value varchar(1024) not null,
   primary key (id)
);
alter table grouper_sessions add constraint FKF43E3C105000A8E0 foreign key (member_id) references grouper_members;
alter table grouper_stems add constraint FKA9825437A3FBEB83 foreign key (modifier_id) references grouper_members;
alter table grouper_stems add constraint FKA98254375236F20E foreign key (creator_id) references grouper_members;
alter table grouper_groups add constraint FK723686075236F20E foreign key (creator_id) references grouper_members;
alter table grouper_groups add constraint FK72368607A3FBEB83 foreign key (modifier_id) references grouper_members;
alter table grouper_factors add constraint FK82080B311BBB05D6 foreign key (node_a_id) references grouper_members;
alter table grouper_factors add constraint FK82080B311BBB7A35 foreign key (node_b_id) references grouper_members;
alter table grouper_attributes add constraint FK92BF040A1E2E76DB foreign key (group_id) references grouper_groups;
alter table grouper_attributes add constraint FK92BF040AC8A07680 foreign key (field_id) references grouper_fields;
