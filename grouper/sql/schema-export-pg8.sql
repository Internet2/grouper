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
drop table grouper_members;
drop table grouper_memberships;
drop table grouper_sessions;
drop table grouper_groups_types;
drop table grouper_stems;
drop table grouper_types;
drop table grouper_groups;
drop table grouper_fields;
drop table grouper_factors;
drop table grouper_attributes;
create table grouper_members (
   id varchar(255) not null,
   subject_id varchar(255),
   subject_source varchar(255),
   subject_type varchar(255) not null,
   member_uuid varchar(255),
   status_type varchar(255),
   status_ttl bigint,
   primary key (id),
   unique (subject_id, subject_source)
);
create table grouper_memberships (
   id varchar(255) not null,
   membership_uuid varchar(255),
   owner_id varchar(255) not null,
   member_id varchar(255) not null,
   list_name varchar(255) not null,
   list_type varchar(255) not null,
   via_id varchar(255),
   depth int,
   parent_membership varchar(255),
   creator_id varchar(255),
   create_time int8 not null,
   status_type varchar(255),
   status_ttl bigint,
   primary key (id),
   unique (membership_uuid, owner_id, member_id, list_name, list_type, via_id, depth)
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
   version int4 not null,
   creator_id varchar(255) not null,
   create_source varchar(255),
   create_time int8 not null,
   description varchar(255),
   display_extension varchar(255) not null,
   display_name varchar(255) not null,
   extension varchar(255),
   modifier_id varchar(255),
   modify_source varchar(255),
   modify_time int8,
   stem_name varchar(255) not null,
   parent_stem varchar(255),
   stem_uuid varchar(255),
   status_type varchar(255),
   status_ttl bigint,
   primary key (id)
);
create table grouper_types (
   id varchar(255) not null,
   name varchar(255) not null unique,
   creator_id varchar(255),
   create_time int8 not null,
   status_type varchar(255),
   status_ttl bigint,
   primary key (id)
);
create table grouper_groups (
   id varchar(255) not null,
   version int4 not null,
   creator_id varchar(255),
   create_source varchar(255),
   create_time int8 not null,
   modifier_id varchar(255),
   modify_source varchar(255),
   modify_time int8,
   parent_stem varchar(255),
   group_uuid varchar(255),
   status_type varchar(255),
   status_ttl bigint,
   primary key (id)
);
create table grouper_fields (
   id varchar(255) not null,
   group_type varchar(255) not null,
   field_type varchar(255) not null,
   field_name varchar(255) not null unique,
   read_priv varchar(255) not null,
   write_priv varchar(255) not null,
   nullable bool,
   primary key (id)
);
create table grouper_factors (
   id varchar(255) not null,
   klass varchar(255) not null,
   factor_uuid varchar(255),
   creator_id varchar(255),
   create_time int8 not null,
   node_a_id varchar(255),
   node_b_id varchar(255),
   status_type varchar(255),
   status_ttl bigint,
   primary key (id)
);
create table grouper_attributes (
   id varchar(255) not null,
   version int4 not null,
   group_id varchar(255),
   field_name varchar(255) not null,
   field_type varchar(255) not null,
   value varchar(1024) not null,
   primary key (id),
   unique (group_id, field_name, field_type)
);
create index member_status_idx on grouper_members (status_type, status_ttl);
create index member_uuid_idx on grouper_members (member_uuid);
create index member_subject_idx on grouper_members (subject_id, subject_source);
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
