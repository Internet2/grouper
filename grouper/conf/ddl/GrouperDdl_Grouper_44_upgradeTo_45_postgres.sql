ALTER TABLE grouper_members ADD COLUMN internal_id BIGINT;

CREATE UNIQUE INDEX grouper_mem_internal_id_idx ON grouper_members (internal_id);

COMMENT ON COLUMN grouper_members.id_index IS 'Sequential id index integer that can we used outside of Grouper';

CREATE TABLE grouper_dictionary (
  internal_id int8 NOT NULL,
  created_on timestamp with time zone NOT NULL,
  last_referenced timestamp with time zone not NULL,
  pre_load varchar(1) NOT NULL DEFAULT 'F'::character varying,
  the_text varchar(4000) NOT NULL,
  CONSTRAINT grouper_dictionary_pk PRIMARY KEY (internal_id)
);
CREATE INDEX grouper_dictionary_last_referenced_idx ON grouper_dictionary USING btree (last_referenced);
CREATE INDEX grouper_dictionary_pre_load_idx ON grouper_dictionary USING btree (pre_load);
CREATE UNIQUE INDEX grouper_dictionary_the_text_idx ON grouper_dictionary USING btree (the_text);

CREATE TABLE public.grouper_data_provider (
  internal_id int8 NOT NULL,
  config_id varchar(100) NOT NULL,
  created_on timestamp with time zone NOT NULL,
  CONSTRAINT grouper_data_loader_config_pk PRIMARY KEY (internal_id)
);
CREATE UNIQUE INDEX grouper_data_loader_config_config_id_idx ON public.grouper_data_provider USING btree (config_id);


CREATE TABLE grouper_data_field (
  internal_id int8 NOT NULL,
  config_id varchar(100) NOT NULL,
  created_on timestamp with time zone NOT NULL,
  CONSTRAINT grouper_data_field_pk PRIMARY KEY (internal_id)
);
CREATE UNIQUE INDEX grouper_data_field_config_id_idx ON grouper_data_field USING btree (config_id);

CREATE TABLE grouper_data_row (
  internal_id int8 NOT NULL,
  created_on timestamp with time zone NOT NULL,
  config_id varchar(100) NOT NULL,
  CONSTRAINT grouper_data_row_pk PRIMARY KEY (internal_id)
);
CREATE UNIQUE INDEX grouper_data_row_config_id_idx ON grouper_data_row USING btree (config_id);


CREATE TABLE grouper_data_alias (
  internal_id int8 NOT NULL,
  data_field_internal_id int8 NULL,
  "name" varchar(100) NOT NULL,
  lower_name varchar(100) NOT NULL,
  created_on timestamp with time zone NOT NULL,
  data_row_internal_id int8 NULL,
  alias_type varchar(1) NULL,
  CONSTRAINT grouper_data_field_alias_pk PRIMARY KEY (internal_id)
);
CREATE INDEX grouper_data_field_alias_data_field_internal_id_idx ON grouper_data_field_alias USING btree (data_field_internal_id);
CREATE UNIQUE INDEX grouper_data_field_alias_lower_name_idx ON grouper_data_field_alias USING btree (lower_name);
CREATE UNIQUE INDEX grouper_data_field_alias_name_idx ON grouper_data_field_alias USING btree (name);


ALTER TABLE grouper_data_alias ADD CONSTRAINT grouper_data_field_alias_fk FOREIGN KEY (data_field_internal_id) REFERENCES grouper_data_field(internal_id);

-- public.grouper_data_field_assign definition

-- Drop table

-- DROP TABLE public.grouper_data_field_assign;

CREATE TABLE public.grouper_data_field_assign (
  member_internal_id int8 NOT NULL,
  data_field_internal_id int8 NOT NULL,
  created_on timestamp with time zone NOT NULL,
  internal_id int8 NOT NULL,
  value_integer int8 NULL,
  value_dictionary_internal_id int8 NULL,
  data_provider_internal_id int8 NOT NULL,
  CONSTRAINT grouper_data_field_assign_pk PRIMARY KEY (internal_id)
);
CREATE INDEX grouper_data_field_assign_data_config_internal_id_idx ON public.grouper_data_field_assign USING btree (data_provider_internal_id);
CREATE INDEX grouper_data_field_assign_data_field_internal_id_idx ON public.grouper_data_field_assign USING btree (data_field_internal_id);
CREATE INDEX grouper_data_field_assign_grouper_members_internal_id_idx ON public.grouper_data_field_assign USING btree (member_internal_id);
CREATE UNIQUE INDEX grouper_data_field_assign_member_internal_id_idx ON public.grouper_data_field_assign USING btree (member_internal_id, data_field_internal_id, value_integer, value_dictionary_internal_id, data_provider_internal_id);

-- Permissions

ALTER TABLE public.grouper_data_field_assign OWNER TO grouper;
GRANT ALL ON TABLE public.grouper_data_field_assign TO grouper;


-- public.grouper_data_field_assign foreign keys

ALTER TABLE public.grouper_data_field_assign ADD CONSTRAINT grouper_data_field_assign_fk FOREIGN KEY (data_field_internal_id) REFERENCES public.grouper_data_field(internal_id);
ALTER TABLE public.grouper_data_field_assign ADD CONSTRAINT grouper_data_field_assign_fk_1 FOREIGN KEY (value_dictionary_internal_id) REFERENCES public.grouper_dictionary(internal_id);
ALTER TABLE public.grouper_data_field_assign ADD CONSTRAINT grouper_data_field_assign_fk_2 FOREIGN KEY (member_internal_id) REFERENCES public.grouper_members(internal_id);
ALTER TABLE public.grouper_data_field_assign ADD CONSTRAINT grouper_data_field_assign_fk_3 FOREIGN KEY (data_provider_internal_id) REFERENCES public.grouper_data_provider(internal_id);

CREATE TABLE public.grouper_data_row_assign (
  member_internal_id int8 NOT NULL,
  data_row_internal_id int8 NOT NULL,
  created_on timestamp with time zone NOT NULL,
  internal_id int8 NOT NULL,
  data_provider_internal_id int8 NOT NULL,
  CONSTRAINT grouper_data_row_assign_pk PRIMARY KEY (internal_id)
);
CREATE INDEX grouper_data_row_assign_data_config_internal_id_idx ON public.grouper_data_row_assign USING btree (data_provider_internal_id);
CREATE INDEX grouper_data_row_assign_data_row_internal_id_idx ON public.grouper_data_row_assign USING btree (data_row_internal_id);
CREATE INDEX grouper_data_row_assign_member_internal_id_idx ON public.grouper_data_row_assign USING btree (member_internal_id);

-- Permissions

ALTER TABLE public.grouper_data_row_assign OWNER TO grouper;
GRANT ALL ON TABLE public.grouper_data_row_assign TO grouper;


-- public.grouper_data_row_assign foreign keys

ALTER TABLE public.grouper_data_row_assign ADD CONSTRAINT grouper_data_row_assign_fk FOREIGN KEY (member_internal_id) REFERENCES public.grouper_members(internal_id);
ALTER TABLE public.grouper_data_row_assign ADD CONSTRAINT grouper_data_row_assign_fk_1 FOREIGN KEY (data_row_internal_id) REFERENCES public.grouper_data_row(internal_id);
ALTER TABLE public.grouper_data_row_assign ADD CONSTRAINT grouper_data_row_assign_fk_2 FOREIGN KEY (data_provider_internal_id) REFERENCES public.grouper_data_provider(internal_id);

CREATE TABLE grouper_data_row_field_assign (
  data_row_assign_internal_id int8 NOT NULL,
  created_on timestamp with time zone NOT NULL,
  internal_id int8 NOT NULL,
  value_integer int8 NULL,
  value_dictionary_internal_id int8 NULL,
  data_field_internal_id int8 NOT NULL,
  CONSTRAINT grouper_data_row_field_assign_pk PRIMARY KEY (internal_id)
);
CREATE INDEX grouper_data_row_field_assign_data_field_internal_id_idx ON grouper_data_row_field_assign USING btree (data_field_internal_id);
CREATE INDEX grouper_data_row_field_assign_data_row_assign_internal_id_idx ON grouper_data_row_field_assign USING btree (data_row_assign_internal_id);

ALTER TABLE grouper_data_row_field_assign ADD CONSTRAINT grouper_data_row_field_assign_fk FOREIGN KEY (data_row_assign_internal_id) REFERENCES grouper_data_row_assign(internal_id);
ALTER TABLE grouper_data_row_field_assign ADD CONSTRAINT grouper_data_row_field_assign_fk_1 FOREIGN KEY (value_dictionary_internal_id) REFERENCES grouper_dictionary(internal_id);
ALTER TABLE grouper_data_row_field_assign ADD CONSTRAINT grouper_data_row_field_assign_fk_3 FOREIGN KEY (data_field_internal_id) REFERENCES grouper_data_field(internal_id);

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

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V45, ' || history) from 1 for 3500), db_version = 45 where object_name = 'Grouper';
commit;


