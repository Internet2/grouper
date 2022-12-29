ALTER TABLE grouper_members ADD internal_id NUMBER(38);

CREATE UNIQUE INDEX grouper_mem_internal_id_idx ON grouper_members (internal_id);

ALTER TABLE grouper_members ADD CONSTRAINT members_internal_id_unique unique (internal_id);

CREATE TABLE grouper_dictionary (
  internal_id NUMBER(38) NOT NULL,
  created_on DATE NOT NULL,
  last_referenced DATE not NULL,
  pre_load VARCHAR2(1) DEFAULT 'F' NOT NULL,
  the_text VARCHAR2(4000) NOT NULL,
  PRIMARY KEY (internal_id)
);
CREATE INDEX dictionary_last_referenced_idx ON grouper_dictionary (last_referenced);
CREATE INDEX dictionary_pre_load_idx ON grouper_dictionary (pre_load);
CREATE UNIQUE INDEX dictionary_the_text_idx ON grouper_dictionary (the_text);

CREATE TABLE grouper_data_provider (
  internal_id NUMBER(38) NOT NULL,
  config_id varchar2(100) NOT NULL,
  created_on DATE NOT NULL,
  PRIMARY KEY (internal_id)
);
CREATE UNIQUE INDEX data_provider_config_id_idx ON  grouper_data_provider (config_id);


CREATE TABLE grouper_data_field (
  internal_id NUMBER(38) NOT NULL,
  config_id varchar2(100) NOT NULL,
  created_on DATE NOT NULL,
  PRIMARY KEY (internal_id)
);
CREATE UNIQUE INDEX data_field_config_id_idx ON grouper_data_field (config_id);

CREATE TABLE grouper_data_row (
  internal_id NUMBER(38) NOT NULL,
  created_on DATE NOT NULL,
  config_id varchar2(100) NOT NULL,
  PRIMARY KEY (internal_id)
);
CREATE UNIQUE INDEX grouper_data_row_config_id_idx ON grouper_data_row (config_id);


CREATE TABLE grouper_data_alias (
  internal_id NUMBER(38) NOT NULL,
  data_field_internal_id NUMBER(38) NULL,
  name varchar2(100) NOT NULL,
  lower_name varchar2(100) NOT NULL,
  created_on DATE NOT NULL,
  data_row_internal_id NUMBER(38) NULL,
  alias_type varchar2(1) NULL,
  PRIMARY KEY (internal_id)
);
CREATE INDEX alias_data_field_intrnl_id_idx ON grouper_data_alias (data_field_internal_id);
CREATE UNIQUE INDEX alias_lower_name_idx ON grouper_data_alias (lower_name);
CREATE UNIQUE INDEX alias_name_idx ON grouper_data_alias (name);


ALTER TABLE grouper_data_alias ADD CONSTRAINT grouper_data_alias_fk FOREIGN KEY (data_field_internal_id) REFERENCES grouper_data_field(internal_id);

CREATE TABLE grouper_data_field_assign (
  member_internal_id NUMBER(38) NOT NULL,
  data_field_internal_id NUMBER(38) NOT NULL,
  created_on DATE NOT NULL,
  internal_id NUMBER(38) NOT NULL,
  value_integer NUMBER(38) NULL,
  value_dictionary_internal_id NUMBER(38) NULL,
  data_provider_internal_id NUMBER(38) NOT NULL,
  PRIMARY KEY (internal_id)
);
CREATE INDEX fld_assgn_prvdr_intrnl_id_idx ON grouper_data_field_assign (data_provider_internal_id);
CREATE INDEX fld_assgn_field_intrnl_id_idx ON grouper_data_field_assign (data_field_internal_id);
CREATE INDEX fld_assgn_mbrs_intrnl_id_idx ON grouper_data_field_assign (member_internal_id);
CREATE UNIQUE INDEX fld_assgn_mbr_intrnl_id_idx ON grouper_data_field_assign (member_internal_id, data_field_internal_id, value_integer, value_dictionary_internal_id, data_provider_internal_id);

ALTER TABLE grouper_data_field_assign ADD CONSTRAINT grouper_data_field_assign_fk FOREIGN KEY (data_field_internal_id) REFERENCES  grouper_data_field(internal_id);
ALTER TABLE grouper_data_field_assign ADD CONSTRAINT grouper_data_field_assign_fk_1 FOREIGN KEY (value_dictionary_internal_id) REFERENCES  grouper_dictionary(internal_id);
ALTER TABLE grouper_data_field_assign ADD CONSTRAINT grouper_data_field_assign_fk_2 FOREIGN KEY (member_internal_id) REFERENCES  grouper_members(internal_id);
ALTER TABLE grouper_data_field_assign ADD CONSTRAINT grouper_data_field_assign_fk_3 FOREIGN KEY (data_provider_internal_id) REFERENCES  grouper_data_provider(internal_id);

CREATE TABLE grouper_data_row_assign (
  member_internal_id NUMBER(38) NOT NULL,
  data_row_internal_id NUMBER(38) NOT NULL,
  created_on DATE NOT NULL,
  internal_id NUMBER(38) NOT NULL,
  data_provider_internal_id NUMBER(38) NOT NULL,
  PRIMARY KEY (internal_id)
);

CREATE INDEX rw_assg_dt_prvdr_intrnl_id_idx ON grouper_data_row_assign (data_provider_internal_id);
CREATE INDEX rw_assg_dt_rw_intrnl_id_idx ON grouper_data_row_assign (data_row_internal_id);
CREATE INDEX rw_assg_mbr_intrnl_id_idx ON grouper_data_row_assign (member_internal_id);

ALTER TABLE  grouper_data_row_assign ADD CONSTRAINT grouper_data_row_assign_fk FOREIGN KEY (member_internal_id) REFERENCES grouper_members(internal_id);
ALTER TABLE  grouper_data_row_assign ADD CONSTRAINT grouper_data_row_assign_fk_1 FOREIGN KEY (data_row_internal_id) REFERENCES grouper_data_row(internal_id);
ALTER TABLE  grouper_data_row_assign ADD CONSTRAINT grouper_data_row_assign_fk_2 FOREIGN KEY (data_provider_internal_id) REFERENCES grouper_data_provider(internal_id);

CREATE TABLE grouper_data_row_field_assign (
  data_row_assign_internal_id NUMBER(38) NOT NULL,
  created_on DATE NOT NULL,
  internal_id NUMBER(38) NOT NULL,
  value_integer NUMBER(38) NULL,
  value_dictionary_internal_id NUMBER(38) NULL,
  data_field_internal_id NUMBER(38) NOT NULL,
  PRIMARY KEY (internal_id)
);
CREATE INDEX dt_rw_fld_asg_fld_intrnl_ididx ON grouper_data_row_field_assign (data_field_internal_id);
CREATE INDEX dtrwfldasg_dtrwsg_intrnl_ididx ON grouper_data_row_field_assign (data_row_assign_internal_id);

ALTER TABLE grouper_data_row_field_assign ADD CONSTRAINT grpr_dt_row_field_assign_fk FOREIGN KEY (data_row_assign_internal_id) REFERENCES grouper_data_row_assign(internal_id);
ALTER TABLE grouper_data_row_field_assign ADD CONSTRAINT grpr_dt_row_field_assign_fk_1 FOREIGN KEY (value_dictionary_internal_id) REFERENCES grouper_dictionary(internal_id);
ALTER TABLE grouper_data_row_field_assign ADD CONSTRAINT grpr_dt_row_field_assign_fk_3 FOREIGN KEY (data_field_internal_id) REFERENCES grouper_data_field(internal_id);


CREATE TABLE grouper_data_global_assign (
  data_field_internal_id NUMBER(38) NOT NULL,
  internal_id NUMBER(38) NOT NULL,
  value_integer NUMBER(38) NULL,
  value_dictionary_internal_id NUMBER(38) NULL,
  data_provider_internal_id NUMBER(38) NOT NULL,
  created_on DATE NOT NULL,
  PRIMARY KEY (internal_id)
);
CREATE INDEX grouper_data_global1_idx ON grouper_data_global_assign (data_provider_internal_id);
CREATE INDEX grouper_data_global2_idx ON grouper_data_global_assign (data_field_internal_id);
CREATE INDEX grouper_data_global3_idx ON grouper_data_global_assign (data_field_internal_id, value_integer);
CREATE INDEX grouper_data_global4_idx ON grouper_data_global_assign (data_field_internal_id, value_dictionary_internal_id);


ALTER TABLE grouper_data_global_assign ADD CONSTRAINT grouper_data_global_assign_fk FOREIGN KEY (data_field_internal_id) REFERENCES grouper_data_field(internal_id);
ALTER TABLE grouper_data_global_assign ADD CONSTRAINT grouper_data_global_diction_fk FOREIGN KEY (value_dictionary_internal_id) REFERENCES grouper_dictionary(internal_id);
ALTER TABLE grouper_data_global_assign ADD CONSTRAINT grouper_data_global_prov_fk FOREIGN KEY (data_provider_internal_id) REFERENCES grouper_data_provider(internal_id);


create view grouper_data_field_assign_v as
select gdf.config_id data_field_config_id, gm.subject_id, gd.the_text value_text, gdfa.value_integer,  
gdf.internal_id data_field_internal_id, gdfa.internal_id data_field_assign_internal_id,
gm.subject_source subject_source_id, gm.id member_id
from grouper_data_field gdf, grouper_members gm, grouper_data_field_assign gdfa 
left join grouper_dictionary gd on gdfa.value_dictionary_internal_id = gd.internal_id  
where gdfa.member_internal_id = gm.internal_id
and gdfa.data_field_internal_id = gdf.internal_id;


create view grouper_data_row_assign_v as
select gdr.config_id data_row_config_id, gm.subject_id, 
gdra.internal_id data_row_internal_id, gdra.internal_id data_row_assign_internal_id,
gm.subject_source subject_source_id, gm.id member_id
from grouper_members gm, grouper_data_row_assign gdra, grouper_data_row gdr 
where gdra.member_internal_id = gm.internal_id
and gdr.internal_id = gdra.data_row_internal_id;


create view grouper_data_row_field_asgn_v as
select gdr.config_id data_row_config_id, gdf.config_id data_field_config_id, gm.subject_id, gd.the_text value_text, gdrfa.value_integer,  
gm.subject_source subject_source_id, gm.id member_id, gdf.internal_id data_field_internal_id, gdrfa.internal_id data_field_assign_internal_id, 
gdra.internal_id data_row_internal_id, gdra.internal_id data_row_assign_internal_id
from grouper_data_field gdf, grouper_members gm, grouper_data_row_assign gdra, grouper_data_row gdr,
grouper_data_row_field_assign gdrfa 
left join grouper_dictionary gd on gdrfa.value_dictionary_internal_id = gd.internal_id 
where gdra.member_internal_id = gm.internal_id
and gdrfa.data_field_internal_id = gdf.internal_id
and gdr.internal_id = gdra.data_row_internal_id 
and gdra.internal_id = gdrfa.data_row_assign_internal_id ;

update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V45, ' || history), 1, 3500), db_version = 45 where object_name = 'Grouper';
commit;


