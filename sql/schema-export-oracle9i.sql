alter table grouper_memberships drop constraint FK2B2D6F0A5236F20E;
alter table grouper_memberships drop constraint FK2B2D6F0A5000A8E0;
alter table grouper_memberships drop constraint FK2B2D6F0ACC93A68B;
alter table grouper_sessions drop constraint FKF43E3C105000A8E0;
alter table grouper_groups_types drop constraint FKFBD60411E2E76DB;
alter table grouper_groups_types drop constraint FKFBD6041CD26E040;
alter table grouper_stems drop constraint FKA98254373C82913E;
alter table grouper_stems drop constraint FKA9825437A3FBEB83;
alter table grouper_stems drop constraint FKA98254375236F20E;
alter table grouper_types drop constraint FKA992D9E65236F20E;
alter table grouper_groups drop constraint FK723686075236F20E;
alter table grouper_groups drop constraint FK723686073C82913E;
alter table grouper_groups drop constraint FK72368607A3FBEB83;
alter table grouper_fields drop constraint FK6FFE2AEC4C7188FA;
alter table grouper_factors drop constraint FK82080B315236F20E;
alter table grouper_factors drop constraint FK82080B311BBB05D6;
alter table grouper_factors drop constraint FK82080B311BBB7A35;
alter table grouper_attributes drop constraint FK92BF040A1E2E76DB;
drop table grouper_members cascade constraints;
drop table grouper_memberships cascade constraints;
drop table grouper_sessions cascade constraints;
drop table grouper_groups_types cascade constraints;
drop table grouper_stems cascade constraints;
drop table grouper_types cascade constraints;
drop table grouper_groups cascade constraints;
drop table grouper_fields cascade constraints;
drop table grouper_factors cascade constraints;
drop table grouper_attributes cascade constraints;
create table grouper_members (
   id varchar2(128) not null,
   subject_id varchar2(255) not null,
   subject_source varchar2(255) not null,
   subject_type varchar2(255) not null,
   member_uuid varchar2(128) not null,
   status_type varchar2(128),
   status_ttl number(19,0),
   primary key (id),
   unique (subject_id, subject_source, subject_type)
);
create table grouper_memberships (
   id varchar2(128) not null,
   membership_uuid varchar2(128),
   owner_id varchar2(128) not null,
   member_id varchar2(128) not null,
   list_name varchar2(32) not null,
   list_type varchar2(32) not null,
   via_id varchar2(128),
   depth int,
   parent_membership varchar2(128),
   creator_id varchar2(128),
   create_time number(19,0) not null,
   status_type varchar2(128),
   status_ttl number(19,0),
   primary key (id),
   unique (membership_uuid, owner_id, member_id, list_name, list_type, via_id, depth)
);
create table grouper_sessions (
   id varchar2(128) not null,
   member_id varchar2(128),
   start_time date not null,
   session_uuid varchar2(128),
   primary key (id)
);
create table grouper_groups_types (
   group_id varchar2(128) not null,
   type_id varchar2(128) not null,
   primary key (group_id, type_id)
);
create table grouper_stems (
   id varchar2(128) not null,
   version number(10,0) not null,
   creator_id varchar2(128) not null,
   create_source varchar2(255),
   create_time number(19,0) not null,
   description varchar2(255),
   display_extension varchar2(255) not null,
   display_name varchar2(255) not null,
   extension varchar2(255) not null,
   modifier_id varchar2(128),
   modify_source varchar2(255),
   modify_time number(19,0),
   name varchar2(255) not null,
   parent_stem varchar2(128),
   stem_uuid varchar2(128),
   status_type varchar2(128),
   status_ttl number(19,0),
   primary key (id)
);
create table grouper_types (
   id varchar2(128) not null,
   name varchar2(255) not null unique,
   creator_id varchar2(128),
   create_time number(19,0) not null,
   status_type varchar2(128),
   status_ttl number(19,0),
   primary key (id)
);
create table grouper_groups (
   id varchar2(128) not null,
   version number(10,0) not null,
   creator_id varchar2(128),
   create_source varchar2(255),
   create_time number(19,0) not null,
   modifier_id varchar2(128),
   modify_source varchar2(255),
   modify_time number(19,0),
   parent_stem varchar2(128),
   group_uuid varchar2(128),
   status_type varchar2(128),
   status_ttl number(19,0),
   primary key (id)
);
create table grouper_fields (
   id varchar2(128) not null,
   group_type varchar2(128) not null,
   field_type varchar2(32) not null,
   field_name varchar2(32) not null unique,
   read_priv varchar2(128) not null,
   write_priv varchar2(128) not null,
   nullable number(1,0),
   primary key (id)
);
create table grouper_factors (
   id varchar2(128) not null,
   klass varchar2(255) not null,
   factor_uuid varchar2(128),
   creator_id varchar2(128),
   create_time number(19,0) not null,
   node_a_id varchar2(128),
   node_b_id varchar2(128),
   status_type varchar2(128),
   status_ttl number(19,0),
   primary key (id)
);
create table grouper_attributes (
   id varchar2(128) not null,
   version number(10,0) not null,
   group_id varchar2(128),
   field_name varchar2(32) not null,
   field_type varchar2(32) not null,
   value varchar2(1024) not null,
   primary key (id)
);
create index member_subjectsource_idx on grouper_members (subject_source);
create index member_subjectid_idx on grouper_members (subject_id);
create index member_status_idx on grouper_members (status_type, status_ttl);
create index member_uuid_idx on grouper_members (member_uuid);
create index member_subjecttype_idx on grouper_members (subject_type);
create index membership_depth_idx on grouper_memberships (depth);
create index membership_owner_idx on grouper_memberships (owner_id);
create index membership_status_idx on grouper_memberships (status_type, status_ttl);
create index membership_member_idx on grouper_memberships (member_id);
create index membership_field_idx on grouper_memberships (list_name, list_type);
create index membership_parent_idx on grouper_memberships (parent_membership);
create index membership_via_idx on grouper_memberships (via_id);
alter table grouper_memberships add constraint FK2B2D6F0A5236F20E foreign key (creator_id) references grouper_members;
alter table grouper_memberships add constraint FK2B2D6F0A5000A8E0 foreign key (member_id) references grouper_members;
alter table grouper_memberships add constraint FK2B2D6F0ACC93A68B foreign key (parent_membership) references grouper_memberships;
alter table grouper_sessions add constraint FKF43E3C105000A8E0 foreign key (member_id) references grouper_members;
alter table grouper_groups_types add constraint FKFBD60411E2E76DB foreign key (group_id) references grouper_groups;
alter table grouper_groups_types add constraint FKFBD6041CD26E040 foreign key (type_id) references grouper_types;
create index stem_status_idx on grouper_stems (status_type, status_ttl);
create index stem_extn_idx on grouper_stems (extension);
create index stem_name_idx on grouper_stems (name);
create index stem_createtime_idx on grouper_stems (create_time);
create index stem_uuid_idx on grouper_stems (stem_uuid);
alter table grouper_stems add constraint FKA98254373C82913E foreign key (parent_stem) references grouper_stems;
alter table grouper_stems add constraint FKA9825437A3FBEB83 foreign key (modifier_id) references grouper_members;
alter table grouper_stems add constraint FKA98254375236F20E foreign key (creator_id) references grouper_members;
create index grouptype_status_idx on grouper_types (status_type, status_ttl);
alter table grouper_types add constraint FKA992D9E65236F20E foreign key (creator_id) references grouper_members;
create index group_status_idx on grouper_groups (status_type, status_ttl);
create index group_uuid_idx on grouper_groups (group_uuid);
alter table grouper_groups add constraint FK723686075236F20E foreign key (creator_id) references grouper_members;
alter table grouper_groups add constraint FK723686073C82913E foreign key (parent_stem) references grouper_stems;
alter table grouper_groups add constraint FK72368607A3FBEB83 foreign key (modifier_id) references grouper_members;
alter table grouper_fields add constraint FK6FFE2AEC4C7188FA foreign key (group_type) references grouper_types;
create index factor_status_idx on grouper_factors (status_type, status_ttl);
create index factor_uuid_idx on grouper_factors (factor_uuid);
alter table grouper_factors add constraint FK82080B315236F20E foreign key (creator_id) references grouper_members;
alter table grouper_factors add constraint FK82080B311BBB05D6 foreign key (node_a_id) references grouper_memberships;
alter table grouper_factors add constraint FK82080B311BBB7A35 foreign key (node_b_id) references grouper_memberships;
create index attribute_field_idx on grouper_attributes (field_name, field_type);
alter table grouper_attributes add constraint FK92BF040A1E2E76DB foreign key (group_id) references grouper_groups;
