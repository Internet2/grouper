---
name: grouper-ddl
description: |
  Guide for adding DDL changes (indexes, tables, columns, comments, foreign keys) to the Grouper
  codebase. Use this skill whenever the user asks to add a database index, create a new table,
  add a column, add database comments, or make any DDL schema change in Grouper. Also trigger
  when the user mentions upgrade tasks that involve DDL, database compares, install SQL files,
  GrouperDdl version classes, or ddlutils. This skill covers the full checklist: install SQL files
  (postgres/oracle/mysql), DDL version classes for database compares, upgrade tasks, and the AI
  DDL reference file.
---

# Grouper DDL Change Guide

Every DDL change in Grouper touches multiple files in a predictable pattern. The database compare
system (ddlutils) uses Java code to know what the schema should look like, so raw SQL in install
files alone is not enough. Follow this checklist so nothing gets missed.

## Checklist for Any DDL Change

1. **Install SQL files** (3 files) - the DDL for fresh installs
2. **DDL version class** (GrouperDdlX_Y_Z.java) - for database compares / auto-DDL
3. **Call from GrouperDdl.java V47** - wire the version class into the compare engine
4. **Upgrade task** (UpgradeTaskVNN.java) - applies the change to existing databases
   (prefer reusing the current release's existing task; one task accumulates a release's changes)
5. **Register upgrade task** in UpgradeTasks.java enum (only when creating a new task for a new release)
6. **AI DDL reference** (aiGshDdl.txt) - keep the AI reference in sync

## File Locations

- **Install SQL (postgres)**: `grouper/conf/ddl/GrouperDdl_Grouper_install_postgres.sql`
- **Install SQL (oracle)**: `grouper/conf/ddl/GrouperDdl_Grouper_install_oracle.sql`
- **Install SQL (mysql)**: `grouper/conf/ddl/GrouperDdl_Grouper_install_mysql.sql`
- **DDL version classes**: `grouper/src/grouper/edu/internet2/middleware/grouper/ddl/GrouperDdlX_Y_Z.java`
- **GrouperDdl enum**: `grouper/src/grouper/edu/internet2/middleware/grouper/ddl/GrouperDdl.java`
- **Upgrade tasks**: `grouper/src/grouper/edu/internet2/middleware/grouper/app/upgradeTasks/UpgradeTaskVNN.java`
- **Upgrade tasks enum**: `grouper/src/grouper/edu/internet2/middleware/grouper/app/upgradeTasks/UpgradeTasks.java`
- **AI DDL reference**: `grouper/misc/aiGsh/aiGshDdl.txt`

## Step 1: Install SQL Files

Add the DDL to all three install SQL files. The syntax is standard SQL and identical across
databases for most operations (CREATE TABLE, CREATE INDEX, ALTER TABLE). Key differences:

### MySQL differences
- **VARCHAR index columns** need a prefix length: `column_name(100)` not just `column_name`. This also
  applies when you *widen* an already-indexed VARCHAR: a single-column index on `VARCHAR(1024)` (4096
  bytes in utf8mb4) exceeds InnoDB's 3072-byte key limit, so the index must become a prefix index
  (`column_name(255)`). In the version-class index def, encode the prefix in the column string
  (`"name(255)"`); ddlutils keeps it for mysql and strips it for postgres/oracle (which index the full
  column). See `member_name_idx` / `group_name_idx` for the pattern.
- **DROP INDEX** syntax: `DROP INDEX idx_name ON table_name` (postgres/oracle omit `ON table_name`)
- **`MODIFY col VARCHAR(n)` rewrites the whole column definition** - it drops NOT NULL unless you
  re-specify it (`MODIFY col VARCHAR(1024) NOT NULL`). Oracle's `MODIFY (col VARCHAR2(n))` keeps the
  existing NOT NULL/NULL and does not need it re-specified.
- **BIGINT** instead of Oracle's `NUMBER(38)`
- **DATETIME** instead of postgres's `timestamp` or Oracle's `DATE`

### Oracle differences
- Uses `NUMBER(38)` for big integers instead of `BIGINT`
- Uses `DATE` for timestamps
- Uses `VARCHAR2` in some places

### Finding the right location
Search for the table name in the install SQL to find where to add indexes or columns.
For new tables, find a logical neighbor and place nearby.

## Widening or altering an existing column (views + indexes)

Changing the type/size of a column that already exists (e.g. widening `VARCHAR(255)` to `VARCHAR(1024)`)
has two gotchas the simple "add a column" path does not.

### 1. Views block the ALTER on postgres only
A column referenced by a view behaves differently per database when you `ALTER` its type:
- **postgres** - HARD blocks it: `cannot alter type of a column used by a view or rule`. You must drop
  the dependent views first, alter, then recreate them.
- **oracle** - allows `ALTER ... MODIFY (col VARCHAR2(n))`; dependent views just go `INVALID` and
  auto-recompile on next use. No drop needed.
- **mysql** - allows it; views are not type-bound to base columns for a widen. No drop needed.

The DDL upgrade task runs while views still exist (the `GrouperDdlEngine` drop-all-views / recreate-all
pass is a separate cycle), so on postgres your upgrade task must drop the blocking views itself. Precedent:
`UpgradeTaskV21` does `DROP VIEW grouper_sql_cache_mship_v` before its `ALTER`, and never recreates it -
`GrouperDdlEngine.addViewsAndForeignKeysIfNeeded` regenerates all views at the end of the run. To find the
exact dependent views on a real postgres DB, query `pg_depend`/`pg_rewrite` (join `pg_attribute` on the
column) rather than eyeballing the install SQL.

Two strategies, depending on risk tolerance:
- **Auto (V21 style):** in the upgrade task, drop each dependent view (guarded by `assertTableThere`),
  run the `ALTER`, let the engine recreate the views.
- **Manual (warn-only) for postgres:** when the dependent-view set is large/risky, have the upgrade task
  do the `ALTER` automatically on oracle/mysql, but on postgres only check the width and `LOG.error` a
  "MANUAL ACTION REQUIRED" message, then return normally so the upgrade is not blocked - and document the
  drop-view / alter / recreate-view SQL in the release notes for the DBA. IMPORTANT: when going warn-only,
  `doesUpgradeTaskHaveDdlWorkToDo()` must return **false** on postgres, otherwise with auto-DDL turned off
  the framework throws "There's DDL work to do that has been configured not to be automatic..." and blocks
  the whole upgrade. See `UpgradeTaskV43` (GRP-7076) for this pattern.

### 2. Idempotent width checks
Use `GrouperDdlUtils.getColumnSize(tableName, columnName)` (returns the declared length via JDBC metadata,
works across all three DBs) to skip columns already at the target width, so the task is safe to re-run.

### 3. mysql index surgery when widening past the key limit
If the widened column is indexed and the new size exceeds mysql's InnoDB key limit (see MySQL differences
above), the upgrade task must, on mysql: `DROP INDEX`, `MODIFY` the column (re-specifying NOT NULL/NULL),
then recreate the index as a `(255)` prefix. Oracle/postgres keep the full-column index.

## Step 2: DDL Version Class (for Database Compares)

Create or update a `GrouperDdlX_Y_Z.java` class. This is what the database compare system
uses to know the expected schema. Without this, Grouper will report schema mismatches.

### Creating a new version class

Use the Grouper version the change ships in (e.g., `6_2_0` for version 6.2.0). If the
class already exists, add your method to it.

```java
package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

public class GrouperDdlX_Y_Z {

  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    // Use GrouperDdl.V47 - the latest and only active version
    boolean buildingToThisVersionAtLeast = GrouperDdl.V47.getVersion() <= buildingToVersion;
    return buildingToThisVersionAtLeast;
  }

  // Methods go here (see patterns below)
}
```

### Adding indexes

```java
static void addMyTableIndexes(DdlVersionBean ddlVersionBean, Database database) {
  if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
    return;
  }
  if (ddlVersionBean.didWeDoThis("vX_Y_Z_addMyTableIndexes", true)) {
    return;
  }

  Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "table_name");

  GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
      "index_name", false,  // false = non-unique, true = unique
      "column1", "column2");
}
```

### Adding a table

```java
static void addMyNewTable(Database database, DdlVersionBean ddlVersionBean) {
  if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
    return;
  }
  if (ddlVersionBean.didWeDoThis("vX_Y_Z_addMyNewTable", true)) {
    return;
  }

  Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "table_name");

  // Primary key column
  GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
      Types.BIGINT, "20", true, true);  // isPrimaryKey=true, isRequired=true

  // Required column
  GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "config_id",
      Types.VARCHAR, "100", false, true);  // isPrimaryKey=false, isRequired=true

  // Optional/nullable column
  GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "description",
      Types.VARCHAR, "4000", false, false);  // isPrimaryKey=false, isRequired=false
}
```

### Adding comments (Oracle/Postgres only, MySQL ignores them)

```java
static void addMyTableComments(Database database, DdlVersionBean ddlVersionBean) {
  if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
    return;
  }
  if (ddlVersionBean.didWeDoThis("vX_Y_Z_addMyTableComments", true)) {
    return;
  }

  GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
      "table_name", "description of the table");

  GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
      "table_name", "column_name", "description of the column");
}
```

### Adding foreign keys

```java
GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, table.getName(),
    "fk_constraint_name", "referenced_table_name",
    GrouperUtil.toList("local_col1", "local_col2"),
    GrouperUtil.toList("referenced_col1", "referenced_col2"));
```

### Common column types
- `Types.BIGINT`, size `"20"` - for internal IDs, timestamps in micros
- `Types.VARCHAR`, size `"1"` - for boolean flags (T/F)
- `Types.VARCHAR`, size `"50"` to `"256"` - for identifiers and names
- `Types.VARCHAR`, size `"4000"` - for large text / descriptions
- `Types.INTEGER`, size `"10"` - for counts

## Step 3: Wire Into GrouperDdl.java V47

Add the call inside `V47.updateVersionFromPrevious()` in `GrouperDdl.java`. This is the
**only** active version - the comment says "DON'T ADD ANY MORE Vs". Add the call before
the closing brace of the method, following the existing pattern:

```java
// Inside V47's updateVersionFromPrevious method, before the closing }
GrouperDdlX_Y_Z.addMyNewTable(database, ddlVersionBean);
GrouperDdlX_Y_Z.addMyTableIndexes(ddlVersionBean, database);
GrouperDdlX_Y_Z.addMyTableComments(database, ddlVersionBean);
```

Note: the parameter order convention varies (some methods take `database, ddlVersionBean`,
others take `ddlVersionBean, database`). Match the signature of the method you're calling.

## Step 4: Upgrade Task

The upgrade task applies the DDL change to existing databases during the upgrade process.
It uses raw SQL, not the ddlutils framework.

### Prefer ONE upgrade task per release - reuse, don't multiply

**Default: add your DDL to the existing upgrade task for the current release rather than
creating a new one.** A release should generally have a single `UpgradeTaskVNN` that
accumulates all of that release's DDL changes, each guarded independently (e.g. its own
`assertIndexExists` / `doesConstraintExistOracle` check). This keeps the `UpgradeTasks`
enum short and the upgrade history readable instead of one task per change.

To find the current release's task, look at the highest `UpgradeTaskVNN` and check its
`versionIntroduced()` - if it matches the release you are shipping in, add to it. Only
create a new task when starting a genuinely new release (the existing top task ships in an
older version). When you do reuse a task, you skip Step 5 (no new enum entry) entirely.

### Adding to an existing upgrade task

If there's an existing task for the same release, add your DDL there (this is the common case).

### Creating a new upgrade task (only when starting a new release)

```java
package edu.internet2.middleware.grouper.app.upgradeTasks;

import org.apache.commons.logging.Log;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskVNN implements UpgradeTasksInterface {

  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskVNN.class);

  // REQUIRED for DDL tasks - tells the system this task modifies schema
  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    return true;
  }

  // REQUIRED for DDL tasks
  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }

  @Override
  public GrouperVersion versionIntroduced() {
    // Use the Grouper version this ships in (check all maintained branches)
    return GrouperVersion.valueOfIgnoreCase("X.Y.Z");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        // Your DDL operations here (see patterns below)

        return null;
      }
    });
  }
}
```

### Upgrade task patterns

**Creating an index** (always check existence first - upgrade tasks may re-run):
```java
if (!GrouperDdlUtils.assertIndexExists("table_name", "index_name")) {
  new GcDbAccess().sql("CREATE INDEX index_name ON table_name (col1, col2)").executeSql();
  if (otherJobInput != null) {
    otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
    otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index index_name");
  }
}
```

**Dropping and recreating an index** (e.g., changing unique to non-unique):
```java
if (GrouperDdlUtils.assertIndexExists("table_name", "index_name")) {
  if (GrouperDdlUtils.isMysql()) {
    new GcDbAccess().sql("DROP INDEX index_name ON table_name").executeSql();
  } else {
    new GcDbAccess().sql("DROP INDEX index_name").executeSql();
  }
}
// Then CREATE INDEX as above
```

**Adding comments** (Oracle/Postgres only):
```java
if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isPostgres()) {
  new GcDbAccess().sql("COMMENT ON TABLE table_name IS 'description'").executeSql();
  new GcDbAccess().sql("COMMENT ON COLUMN table_name.col IS 'description'").executeSql();
}
```

**MySQL-specific index on VARCHAR columns**:
```java
if (GrouperDdlUtils.isMysql()) {
  new GcDbAccess().sql("CREATE INDEX idx ON tbl (varchar_col(100), other_col)").executeSql();
} else {
  new GcDbAccess().sql("CREATE INDEX idx ON tbl (varchar_col, other_col)").executeSql();
}
```

**Checking if a table exists** (for optional tables like provisioner tables):
```java
if (GrouperDdlUtils.assertTableThere(true, "table_name")) {
  // table exists, safe to modify
}
```

## Step 5: Register in UpgradeTasks.java (New Tasks Only)

If you created a new upgrade task class, add it to the `UpgradeTasks` enum in
`grouper/src/grouper/edu/internet2/middleware/grouper/app/upgradeTasks/UpgradeTasks.java`.

Find the last entry (e.g., V40) and add a new one after it:

```java
  },
  V41{
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV41();
    }
  }
```

The version number must be sequential (no gaps from the last entry).

## Step 6: Update AI DDL Reference

Update `grouper/misc/aiGsh/aiGshDdl.txt` to match the postgres install SQL. This file
is a copy of the postgres DDL used as an AI reference. Search for the relevant table and
add the new DDL in the same location as in the postgres install SQL.

## Reference: Key Utility Methods

- `GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName)` - find or create table
- `GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, name, type, size, isPK, isRequired)` - find or create column
- `GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, indexName, isUnique, columns...)` - find or create index
- `GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, tableName, fkName, refTable, localCols, refCols)` - foreign key
- `GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, tableName, comment)` - table comment
- `GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, columnName, comment)` - column comment
- `GrouperDdlUtils.assertIndexExists(tableName, indexName)` - check if index exists (for upgrade tasks)
- `GrouperDdlUtils.assertTableThere(true, tableName)` - check if table exists
- `GrouperDdlUtils.isOracle()` / `GrouperDdlUtils.isPostgres()` / `GrouperDdlUtils.isMysql()` - DB type checks
- `GcDbAccess().sql("...").executeSql()` - execute raw SQL (used in upgrade tasks)
