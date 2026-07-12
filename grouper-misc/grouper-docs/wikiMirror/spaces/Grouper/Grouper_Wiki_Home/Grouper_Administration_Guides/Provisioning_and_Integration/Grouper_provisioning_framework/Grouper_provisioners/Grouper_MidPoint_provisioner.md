---
title: "Grouper MidPoint provisioner"
space: Grouper
pageId: 28555467
version: 18
lastUpdated: 2026-07-01T05:38:09.370Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555467/Grouper+MidPoint+provisioner
---

> The midPoint provisioner pushes Grouper groups and memberships into a set of database tables that Evolveum **midPoint** reads from. It is a trimmed-down form of the Grouper SQL provisioner: you supply a database external system and a table-name prefix, and Grouper maintains the `gr_mp_*` tables described below.
> 
> Available in **v2.6.17+** (October 2022).

> **Privileges:** creating and editing a provisioner configuration in the UI requires a Grouper system administrator (a member of the wheel group / running as root). Ordinary users cannot configure provisioners.

## External system: database

The midPoint provisioner uses a database external system. Create one as shown below.

## DDL

The database between Grouper and midPoint can be any supported type. Example DDL for each is below.

### Postgres

```sql
CREATE TABLE gr_mp_groups (
	group_name varchar(1024) NULL, -- Name of group mapped in some way
	id_index int8 NOT NULL, -- This is the integer identifier for a group and foreign key to group attributes and memberships
	display_name varchar(1024) NULL, -- Display name of group mapped in some way
	description varchar(1024) NULL, -- Description of group mapped in some way
	last_modified int8 NOT NULL, -- Millis since 1970, will be sequential and unique
	deleted varchar(1) NOT NULL, -- T or F.  Deleted rows will be removed after they have had time to be processed
	CONSTRAINT gr_mp_groups_pkey PRIMARY KEY (id_index)
);
CREATE INDEX gr_mp_groups_ddx ON gr_mp_groups(display_name);
CREATE INDEX gr_mp_groups_gdx ON gr_mp_groups(group_name);
CREATE UNIQUE INDEX gr_mp_groups_idx ON gr_mp_groups(id_index);
CREATE UNIQUE INDEX gr_mp_groups_ldx ON gr_mp_groups(last_modified);
COMMENT ON TABLE gr_mp_groups IS 'This table holds groups';

COMMENT ON COLUMN gr_mp_groups.group_name IS 'Name of group mapped in some way';
COMMENT ON COLUMN gr_mp_groups.id_index IS 'This is the integer identifier for a group and foreign key to group attributes and memberships';
COMMENT ON COLUMN gr_mp_groups.display_name IS 'Display name of group mapped in some way';
COMMENT ON COLUMN gr_mp_groups.description IS 'Description of group mapped in some way';
COMMENT ON COLUMN gr_mp_groups.last_modified IS 'Millis since 1970, will be sequential and unique';
COMMENT ON COLUMN gr_mp_groups.deleted IS 'T or F.  Deleted rows will be removed after they have had time to be processed';

CREATE TABLE gr_mp_subjects (
	subject_id_index int8 NOT NULL, -- This is the integer identifier for a subject and foreign key to subject attributes and memberships
	subject_id varchar(1024) NULL, -- Subject ID mapped in some way
	last_modified int8 NOT NULL, -- Millis since 1970, will be sequential and unique
	deleted varchar(1) NOT NULL, -- T or F.  Deleted rows will be removed after they have had time to be processed
	CONSTRAINT gr_mp_subjects_pkey PRIMARY KEY (subject_id_index)
);
CREATE UNIQUE INDEX gr_mp_subjects_idx ON gr_mp_subjects(subject_id_index);
CREATE UNIQUE INDEX gr_mp_subjects_ldx ON gr_mp_subjects(last_modified);
CREATE INDEX gr_mp_subjects_sdx ON gr_mp_subjects(subject_id);
COMMENT ON TABLE gr_mp_subjects IS 'This table holds subjects';

COMMENT ON COLUMN gr_mp_subjects.subject_id_index IS 'This is the integer identifier for a subject and foreign key to subject attributes and memberships';
COMMENT ON COLUMN gr_mp_subjects.subject_id IS 'Subject ID mapped in some way';
COMMENT ON COLUMN gr_mp_subjects.last_modified IS 'Millis since 1970, will be sequential and unique';
COMMENT ON COLUMN gr_mp_subjects.deleted IS 'T or F.  Deleted rows will be removed after they have had time to be processed';

CREATE TABLE gr_mp_group_attributes (
	group_id_index int8 NOT NULL, -- This is the integer identifier for a group and foreign key to groups and memberships
	attribute_name varchar(1000) NOT NULL, -- Attribute name for attributes not in the main group table
	attribute_value varchar(4000) NULL, -- Attribute value could be null
	last_modified int8 NOT NULL, -- Millis since 1970, will be sequential and unique
	deleted varchar(1) NOT NULL, -- T or F.  Deleted rows will be removed after they have had time to be processed
	CONSTRAINT gr_mp_group_attributes_fk FOREIGN KEY (group_id_index) REFERENCES gr_mp_groups(id_index) ON DELETE CASCADE
 );
CREATE UNIQUE INDEX gr_mp_group_attributes_idx ON gr_mp_group_attributes(group_id_index, attribute_name, attribute_value);
CREATE UNIQUE INDEX gr_mp_group_attributes_ldx ON gr_mp_group_attributes(last_modified);
COMMENT ON TABLE gr_mp_group_attributes IS 'This table holds group attributes which are one to one or one to many to the groups table';

COMMENT ON COLUMN gr_mp_group_attributes.group_id_index IS 'This is the integer identifier for a group and foreign key to groups and memberships';
COMMENT ON COLUMN gr_mp_group_attributes.attribute_name IS 'Attribute name for attributes not in the main group table';
COMMENT ON COLUMN gr_mp_group_attributes.attribute_value IS 'Attribute value could be null';
COMMENT ON COLUMN gr_mp_group_attributes.last_modified IS 'Millis since 1970, will be sequential and unique';
COMMENT ON COLUMN gr_mp_group_attributes.deleted IS 'T or F.  Deleted rows will be removed after they have had time to be processed';

CREATE TABLE gr_mp_memberships (
	group_id_index int8 NOT NULL, -- This is the foreign key to groups
	subject_id_index int8 NOT NULL, -- This is the foreign key to subjects
	last_modified int8 NOT NULL, -- Millis since 1970, will be sequential and unique
	deleted varchar(1) NOT NULL, -- T or F.  Deleted rows will be removed after they have had time to be processed
	CONSTRAINT gr_mp_memberships_gfk FOREIGN KEY (group_id_index) REFERENCES gr_mp_groups(id_index) ON DELETE CASCADE,
	CONSTRAINT gr_mp_memberships_sfk FOREIGN KEY (subject_id_index) REFERENCES gr_mp_subjects(subject_id_index) ON DELETE CASCADE
 );
CREATE UNIQUE INDEX gr_mp_memberships_idx ON gr_mp_memberships(group_id_index, subject_id_index);
CREATE UNIQUE INDEX gr_mp_memberships_ldx ON gr_mp_memberships(last_modified);
CREATE INDEX gr_mp_memberships_by_subject_idx ON gr_mp_memberships(subject_id_index);
COMMENT ON TABLE gr_mp_memberships IS 'This table holds memberships.  The primary key is group_id_index and subject_id_index';

COMMENT ON COLUMN gr_mp_memberships.group_id_index IS 'This is the foreign key to groups';
COMMENT ON COLUMN gr_mp_memberships.subject_id_index IS 'This is the foreign key to subjects';
COMMENT ON COLUMN gr_mp_memberships.last_modified IS 'Millis since 1970, will be sequential and unique';
COMMENT ON COLUMN gr_mp_memberships.deleted IS 'T or F.  Deleted rows will be removed after they have had time to be processed';

CREATE TABLE gr_mp_subject_attributes (
	subject_id_index int8 NOT NULL, -- This is the integer identifier and foreign key to subjects
	attribute_name varchar(1000) NOT NULL, -- Attribute name for attributes not in the main subject table
	attribute_value varchar(4000) NULL, -- Attribute value could be null
	last_modified int8 NOT NULL, -- Millis since 1970, will be sequential and unique
	deleted varchar(1) NOT NULL, -- T or F.  Deleted rows will be removed after they have had time to be processed
	CONSTRAINT gr_mp_subject_attributes_fk FOREIGN KEY (subject_id_index) REFERENCES gr_mp_subjects(subject_id_index) ON DELETE CASCADE
 );
CREATE UNIQUE INDEX gr_mp_subject_attributes_idx ON gr_mp_subject_attributes(subject_id_index, attribute_name, attribute_value);
CREATE UNIQUE INDEX gr_mp_subject_attributes_ldx ON gr_mp_subject_attributes(last_modified);
COMMENT ON TABLE gr_mp_subject_attributes IS 'This table holds subject attributes which are one to one or one to many to the subjects table';

COMMENT ON COLUMN gr_mp_subject_attributes.subject_id_index IS 'This is the integer identifier and foreign key to subjects';
COMMENT ON COLUMN gr_mp_subject_attributes.attribute_name IS 'Attribute name for attributes not in the main subject table';
COMMENT ON COLUMN gr_mp_subject_attributes.attribute_value IS 'Attribute value could be null';
COMMENT ON COLUMN gr_mp_subject_attributes.last_modified IS 'Millis since 1970, will be sequential and unique';
COMMENT ON COLUMN gr_mp_subject_attributes.deleted IS 'T or F.  Deleted rows will be removed after they have had time to be processed';
```

### MySQL

```sql
CREATE TABLE gr_mp_groups (
  group_name varchar(1024) DEFAULT NULL,
  id_index bigint NOT NULL,
  display_name varchar(1024) DEFAULT NULL,
  description varchar(1024) DEFAULT NULL,
  last_modified bigint NOT NULL,
  deleted varchar(1) NOT NULL,
  PRIMARY KEY (id_index),
  UNIQUE KEY gr_mp_groups_ldx (last_modified),
  UNIQUE KEY gr_mp_groups_idx (id_index),
  KEY gr_mp_groups_ddx (display_name(255)),
  KEY gr_mp_groups_gdx (group_name(255))
);

CREATE TABLE gr_mp_group_attributes (
  group_id_index bigint NOT NULL,
  attribute_name varchar(1000) NOT NULL,
  attribute_value varchar(4000) DEFAULT NULL,
  last_modified bigint NOT NULL,
  deleted varchar(1) NOT NULL,
  UNIQUE KEY gr_mp_group_attributes_ldx (last_modified),
  UNIQUE KEY gr_mp_group_attributes_idx (group_id_index,attribute_name(100),attribute_value(155)),
  CONSTRAINT gr_mp_group_attributes_fk FOREIGN KEY (group_id_index) REFERENCES gr_mp_groups (id_index) ON DELETE CASCADE
 );

CREATE TABLE gr_mp_subjects (
  subject_id_index bigint NOT NULL,
  subject_id varchar(1024) DEFAULT NULL,
  last_modified bigint NOT NULL,
  deleted varchar(1) NOT NULL,
  PRIMARY KEY (subject_id_index),
  UNIQUE KEY gr_mp_subjects_ldx (last_modified),
  UNIQUE KEY gr_mp_subjects_idx (subject_id_index),
  KEY gr_mp_subjects_sdx (subject_id(255))
);

CREATE TABLE gr_mp_subject_attributes (
  subject_id_index bigint NOT NULL,
  attribute_name varchar(1000) NOT NULL,
  attribute_value varchar(4000) DEFAULT NULL,
  last_modified bigint NOT NULL,
  deleted varchar(1) NOT NULL,
  UNIQUE KEY gr_mp_subject_attributes_ldx (last_modified),
  UNIQUE KEY gr_mp_subject_attributes_idx (subject_id_index,attribute_name(100),attribute_value(155)),
  CONSTRAINT gr_mp_subject_attributes_fk FOREIGN KEY (subject_id_index) REFERENCES gr_mp_subjects (subject_id_index) ON DELETE CASCADE
 );

CREATE TABLE gr_mp_memberships (
  group_id_index bigint NOT NULL,
  subject_id_index bigint NOT NULL,
  last_modified bigint NOT NULL,
  deleted varchar(1) NOT NULL,
  UNIQUE KEY gr_mp_memberships_ldx (last_modified),
  UNIQUE KEY gr_mp_memberships_idx (group_id_index,subject_id_index),
  KEY gr_mp_memberships_sfk (subject_id_index),
  CONSTRAINT gr_mp_memberships_gfk FOREIGN KEY (group_id_index) REFERENCES gr_mp_groups (id_index) ON DELETE CASCADE,
  CONSTRAINT gr_mp_memberships_sfk FOREIGN KEY (subject_id_index) REFERENCES gr_mp_subjects (subject_id_index) ON DELETE CASCADE
 );
```

### Oracle

```sql
CREATE TABLE gr_mp_groups (
  group_name varchar2(1024) NULL, -- Name of group mapped in some way
  id_index number(12) NOT NULL, -- This is the integer identifier for a group and foreign key to group attributes and memberships
  display_name varchar2(1024) NULL, -- Display name of group mapped in some way
  description varchar2(1024) NULL, -- Description of group mapped in some way
  last_modified number(12) NOT NULL, -- Millis since 1970, will be sequential and unique
  deleted varchar2(1) NOT NULL, -- T or F.  Deleted rows will be removed after they have had time to be processed
  CONSTRAINT gr_mp_groups_pkey PRIMARY KEY (id_index)
);
CREATE INDEX gr_mp_groups_ddx ON gr_mp_groups(display_name);
CREATE INDEX gr_mp_groups_gdx ON gr_mp_groups(group_name);
CREATE UNIQUE INDEX gr_mp_groups_ldx ON gr_mp_groups(last_modified);
COMMENT ON TABLE gr_mp_groups IS 'This table holds groups';

COMMENT ON COLUMN gr_mp_groups.group_name IS 'Name of group mapped in some way';
COMMENT ON COLUMN gr_mp_groups.id_index IS 'This is the integer identifier for a group and foreign key to group attributes and memberships';
COMMENT ON COLUMN gr_mp_groups.display_name IS 'Display name of group mapped in some way';
COMMENT ON COLUMN gr_mp_groups.description IS 'Description of group mapped in some way';
COMMENT ON COLUMN gr_mp_groups.last_modified IS 'Millis since 1970, will be sequential and unique';
COMMENT ON COLUMN gr_mp_groups.deleted IS 'T or F.  Deleted rows will be removed after they have had time to be processed';

CREATE TABLE gr_mp_subjects (
  subject_id_index number(12) NOT NULL, -- This is the integer identifier for a subject and foreign key to subject attributes and memberships
  subject_id varchar2(1024) NULL, -- Subject ID mapped in some way
  last_modified number(12) NOT NULL, -- Millis since 1970, will be sequential and unique
  deleted varchar2(1) NOT NULL, -- T or F.  Deleted rows will be removed after they have had time to be processed
  CONSTRAINT gr_mp_subjects_pkey PRIMARY KEY (subject_id_index)
);
CREATE UNIQUE INDEX gr_mp_subjects_ldx ON gr_mp_subjects(last_modified);
CREATE INDEX gr_mp_subjects_sdx ON gr_mp_subjects(subject_id);
COMMENT ON TABLE gr_mp_subjects IS 'This table holds subjects';

COMMENT ON COLUMN gr_mp_subjects.subject_id_index IS 'This is the integer identifier for a subject and foreign key to subject attributes and memberships';
COMMENT ON COLUMN gr_mp_subjects.subject_id IS 'Subject ID mapped in some way';
COMMENT ON COLUMN gr_mp_subjects.last_modified IS 'Millis since 1970, will be sequential and unique';
COMMENT ON COLUMN gr_mp_subjects.deleted IS 'T or F.  Deleted rows will be removed after they have had time to be processed';

CREATE TABLE gr_mp_group_attributes (
  group_id_index number(12) NOT NULL, -- This is the integer identifier for a group and foreign key to groups and memberships
  attribute_name varchar2(1000) NOT NULL, -- Attribute name for attributes not in the main group table
  attribute_value varchar2(4000) NULL, -- Attribute value could be null
  last_modified number(12) NOT NULL, -- Millis since 1970, will be sequential and unique
  deleted varchar2(1) NOT NULL, -- T or F.  Deleted rows will be removed after they have had time to be processed
  CONSTRAINT gr_mp_group_attributes_fk FOREIGN KEY (group_id_index) REFERENCES gr_mp_groups(id_index) ON DELETE CASCADE
 );
CREATE UNIQUE INDEX gr_mp_group_attributes_idx ON gr_mp_group_attributes(group_id_index, attribute_name, standard_hash(attribute_value));
CREATE UNIQUE INDEX gr_mp_group_attributes_ldx ON gr_mp_group_attributes(last_modified);
COMMENT ON TABLE gr_mp_group_attributes IS 'This table holds group attributes which are one to one or one to many to the groups table';

COMMENT ON COLUMN gr_mp_group_attributes.group_id_index IS 'This is the integer identifier for a group and foreign key to groups and memberships';
COMMENT ON COLUMN gr_mp_group_attributes.attribute_name IS 'Attribute name for attributes not in the main group table';
COMMENT ON COLUMN gr_mp_group_attributes.attribute_value IS 'Attribute value could be null';
COMMENT ON COLUMN gr_mp_group_attributes.last_modified IS 'Millis since 1970, will be sequential and unique';
COMMENT ON COLUMN gr_mp_group_attributes.deleted IS 'T or F.  Deleted rows will be removed after they have had time to be processed';

CREATE TABLE gr_mp_memberships (
  group_id_index number(12) NOT NULL, -- This is the foreign key to groups
  subject_id_index number(12) NOT NULL, -- This is the foreign key to subjects
  last_modified number(12) NOT NULL, -- Millis since 1970, will be sequential and unique
  deleted varchar2(1) NOT NULL, -- T or F.  Deleted rows will be removed after they have had time to be processed
  CONSTRAINT gr_mp_memberships_gfk FOREIGN KEY (group_id_index) REFERENCES gr_mp_groups(id_index) ON DELETE CASCADE,
  CONSTRAINT gr_mp_memberships_sfk FOREIGN KEY (subject_id_index) REFERENCES gr_mp_subjects(subject_id_index) ON DELETE CASCADE
 );
CREATE UNIQUE INDEX gr_mp_memberships_idx ON gr_mp_memberships(group_id_index, subject_id_index);
CREATE UNIQUE INDEX gr_mp_memberships_ldx ON gr_mp_memberships(last_modified);
CREATE INDEX gr_mp_memberships_by_subject_idx ON gr_mp_memberships(subject_id_index);
COMMENT ON TABLE gr_mp_memberships IS 'This table holds memberships.  The primary key is group_id_index and subject_id_index';

COMMENT ON COLUMN gr_mp_memberships.group_id_index IS 'This is the foreign key to groups';
COMMENT ON COLUMN gr_mp_memberships.subject_id_index IS 'This is the foreign key to subjects';
COMMENT ON COLUMN gr_mp_memberships.last_modified IS 'Millis since 1970, will be sequential and unique';
COMMENT ON COLUMN gr_mp_memberships.deleted IS 'T or F.  Deleted rows will be removed after they have had time to be processed';

CREATE TABLE gr_mp_subject_attributes (
  subject_id_index number(12) NOT NULL, -- This is the integer identifier and foreign key to subjects
  attribute_name varchar2(1000) NOT NULL, -- Attribute name for attributes not in the main subject table
  attribute_value varchar2(4000) NULL, -- Attribute value could be null
  last_modified number(12) NOT NULL, -- Millis since 1970, will be sequential and unique
  deleted varchar2(1) NOT NULL, -- T or F.  Deleted rows will be removed after they have had time to be processed
  CONSTRAINT gr_mp_subject_attributes_fk FOREIGN KEY (subject_id_index) REFERENCES gr_mp_subjects(subject_id_index) ON DELETE CASCADE
 );
CREATE UNIQUE INDEX gr_mp_subject_attributes_idx ON gr_mp_subject_attributes(subject_id_index, attribute_name, standard_hash(attribute_value));
CREATE UNIQUE INDEX gr_mp_subject_attributes_ldx ON gr_mp_subject_attributes(last_modified);
COMMENT ON TABLE gr_mp_subject_attributes IS 'This table holds subject attributes which are one to one or one to many to the subjects table';

COMMENT ON COLUMN gr_mp_subject_attributes.subject_id_index IS 'This is the integer identifier and foreign key to subjects';
COMMENT ON COLUMN gr_mp_subject_attributes.attribute_name IS 'Attribute name for attributes not in the main subject table';
COMMENT ON COLUMN gr_mp_subject_attributes.attribute_value IS 'Attribute value could be null';
COMMENT ON COLUMN gr_mp_subject_attributes.last_modified IS 'Millis since 1970, will be sequential and unique';
COMMENT ON COLUMN gr_mp_subject_attributes.deleted IS 'T or F.  Deleted rows will be removed after they have had time to be processed'; 
```

## Provisioner configuration

The midPoint provisioner is essentially a trimmed-down SQL provisioner. You only enter the prefix for the tables (`midPointTablesPrefix`) and Grouper assumes the rest of each name. For example, with the prefix `gr` shown below, Grouper expects the tables to be named `gr_mp_groups`, `gr_mp_memberships`, and so on.

Deleted rows are not removed immediately; they are flagged and then purged after a configurable interval (`sqlRemoveDeletedDataAfterHours`, default 168 hours = one week).

## Data model

How Grouper maps groups, subjects, and memberships into the database tables:

- The provisioning target is chosen from a single-assign metadata attribute in Grouper.
- The provisioner can map whatever it needs to `group_name`, `display_name`, `id_index`, and `description`.
- `id_index` is a numeric bigint that can be used for efficient foreign keys.
- The group table has common columns that may be used or left empty.
- When Grouper changes a record it updates the `last_modified` column, which can be used as a change log.
- Data is not deleted at first; instead the `deleted` flag is set. Flagged rows are purged after the configured interval (default one week).
- The `deleted` values `T` and `F` are one-character strings, not boolean database types.
- Group attributes are generally single-valued but can be multi-valued.
- The subject `subject_id` must be something midPoint can use to look up a user — ideally the Grouper subject id.
- Two operations will not occur in the same millisecond, so `last_modified` is sequential and unique.

Example data:

### gr_mp_groups

| `group_name` | id_index | display_name | description | `last_modified` | `deleted` |
| --- | --- | --- | --- | --- | --- |
| `some:group` | 34 | Some:Group | This group is here because | xx1 | T |
| `some:other:group` | 45 |  |  | xx2 | F |
| `some:other:group2` | 47 |  |  | xx3 | T |

### gr_mp_group_attributes

| group_id_index | attribute_name | attribute_value | `last_modified` | `deleted` |
| --- | --- | --- | --- | --- |
| `34` | something | someValue | xx1 | T |
| `45` | something1 | anotherValue | xx2 | F |
| `45` | something2 | aValue | xx3 | T |

### gr_mp_memberships

| `group_id_index` | `subject_id_index` | `last_modified` | `deleted` |
| --- | --- | --- | --- |
| `45` | `98` | yy1 | T |
| `34` | `87` | yy2 | F |

### gr_mp_subjects

| `subject_id_index` | `subject_id` | `last_modified` | `deleted` |
| --- | --- | --- | --- |
| `98` | `12345678` | yy1 | T |
| `87` | `98764543` | yy2 | F |
| `87` | `23456789` | yy3 | T |

### gr_mp_subject_attributes

| `subject_id_index` | `attribute_name` | `attribute_value` | last_modified | `deleted` |
| --- | --- | --- | --- | --- |
| `98` | `12345678` | yy1 |  | T |
| `87` | `98764543` | yy2 |  | F |
| `89` | `23456789` | yy3 |  | T |
