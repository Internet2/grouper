alter table grouper_memberships drop constraint FK2B2D6F0A5000A8E0;
alter table grouper_memberships drop constraint FK2B2D6F0ACC93A68B;
alter table grouper_sessions drop constraint FKF43E3C105000A8E0;
alter table grouper_groups_types drop constraint FKFBD60411E2E76DB;
alter table grouper_groups_types drop constraint FKFBD6041CD26E040;
alter table grouper_stems drop constraint FKA98254373C82913E;
alter table grouper_stems drop constraint FKA9825437A3FBEB83;
alter table grouper_stems drop constraint FKA98254375236F20E;
alter table grouper_groups drop constraint FK723686075236F20E;
alter table grouper_groups drop constraint FK723686073C82913E;
alter table grouper_groups drop constraint FK72368607A3FBEB83;
alter table grouper_fields drop constraint FK6FFE2AEC4C7188FA;
alter table grouper_factors drop constraint FK82080B311BBB05D6;
alter table grouper_factors drop constraint FK82080B311BBB7A35;
alter table grouper_attributes drop constraint FK92BF040A1E2E76DB;
drop table grouper_members if exists;
drop table grouper_memberships if exists;
drop table grouper_sessions if exists;
drop table grouper_groups_types if exists;
drop table grouper_stems if exists;
drop table grouper_types if exists;
drop table grouper_groups if exists;
drop table grouper_fields if exists;
drop table grouper_factors if exists;
drop table grouper_attributes if exists;
create table grouper_members (
   id varchar(255) not null,
   subject_id varchar(255),
   subject_source varchar(255),
   subject_type varchar(255) not null,
   member_uuid varchar(255),
   primary key (id),
   unique (subject_id, subject_source)
);
create table grouper_memberships (
   id varchar(255) not null,
   owner_id varchar(255) not null,
   member_id varchar(255) not null,
   list_name varchar(255) not null,
   list_type varchar(255) not null,
   via_id varchar(255),
   depth int,
   parent_membership varchar(255),
   primary key (id),
   unique (owner_id, member_id, list_name, list_type, via_id, depth)
);
create table grouper_sessions (
   id varchar(255) not null,
   member_id varchar(255),
   start_time timestamp not null,
   session_uuid varchar(255),
   primary key (id)
);
create table grouper_groups_types (
   group_id varchar(255) not null,
   type_id varchar(255) not null,
   primary key (group_id, type_id)
);
create table grouper_stems (
   id varchar(255) not null,
   version integer not null,
   creator_id varchar(255) not null,
   create_source varchar(255),
   create_time bigint not null,
   description varchar(255),
   display_extension varchar(255) not null,
   display_name varchar(255) not null,
   extension varchar(255),
   modifier_id varchar(255),
   modify_source varchar(255),
   modify_time bigint,
   stem_name varchar(255) not null,
   parent_stem varchar(255),
   stem_uuid varchar(255),
   primary key (id)
);
create table grouper_types (
   id varchar(255) not null,
   name varchar(255) not null,
   primary key (id),
   unique (name)
);
create table grouper_groups (
   id varchar(255) not null,
   version integer not null,
   creator_id varchar(255),
   create_source varchar(255),
   create_time bigint not null,
   modifier_id varchar(255),
   modify_source varchar(255),
   modify_time bigint,
   parent_stem varchar(255),
   group_uuid varchar(255),
   primary key (id)
);
create table grouper_fields (
   id varchar(255) not null,
   group_type varchar(255) not null,
   field_type varchar(255) not null,
   field_name varchar(255) not null,
   read_priv varchar(255) not null,
   write_priv varchar(255) not null,
   nullable bit,
   primary key (id),
   unique (field_name)
);
create table grouper_factors (
   id varchar(255) not null,
   klass varchar(255) not null,
   node_a_id varchar(255),
   node_b_id varchar(255),
   primary key (id)
);
create table grouper_attributes (
   id varchar(255) not null,
   version integer not null,
   group_id varchar(255),
   field_name varchar(255) not null,
   field_type varchar(255) not null,
   value varchar(1024) not null,
   primary key (id),
   unique (group_id, field_name, field_type)
);
alter table grouper_memberships add constraint FK2B2D6F0A5000A8E0 foreign key (member_id) references grouper_members;
alter table grouper_memberships add constraint FK2B2D6F0ACC93A68B foreign key (parent_membership) references grouper_memberships;
alter table grouper_sessions add constraint FKF43E3C105000A8E0 foreign key (member_id) references grouper_members;
alter table grouper_groups_types add constraint FKFBD60411E2E76DB foreign key (group_id) references grouper_groups;
alter table grouper_groups_types add constraint FKFBD6041CD26E040 foreign key (type_id) references grouper_types;
alter table grouper_stems add constraint FKA98254373C82913E foreign key (parent_stem) references grouper_stems;
alter table grouper_stems add constraint FKA9825437A3FBEB83 foreign key (modifier_id) references grouper_members;
alter table grouper_stems add constraint FKA98254375236F20E foreign key (creator_id) references grouper_members;
alter table grouper_groups add constraint FK723686075236F20E foreign key (creator_id) references grouper_members;
alter table grouper_groups add constraint FK723686073C82913E foreign key (parent_stem) references grouper_stems;
alter table grouper_groups add constraint FK72368607A3FBEB83 foreign key (modifier_id) references grouper_members;
alter table grouper_fields add constraint FK6FFE2AEC4C7188FA foreign key (group_type) references grouper_types;
alter table grouper_factors add constraint FK82080B311BBB05D6 foreign key (node_a_id) references grouper_members;
alter table grouper_factors add constraint FK82080B311BBB7A35 foreign key (node_b_id) references grouper_members;
alter table grouper_attributes add constraint FK92BF040A1E2E76DB foreign key (group_id) references grouper_groups;
