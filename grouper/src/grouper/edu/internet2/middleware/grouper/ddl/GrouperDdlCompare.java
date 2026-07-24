package edu.internet2.middleware.grouper.ddl;

import java.sql.Connection;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.Platform;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Column;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.ForeignKey;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Index;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.IndexColumn;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Reference;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.platform.SqlBuilder;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils.DbMetadataBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;

public class GrouperDdlCompare {

  public static void main(String[] args) {
    
    GrouperStartup.startup();
    
    GrouperDdlCompareResult grouperDdlCompareResult = new GrouperDdlCompare().compareDatabase();
    
    System.out.println(grouperDdlCompareResult.getResult().toString());
    
  }
  
  private GrouperDdlCompareResult result = null;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDdlCompare.class);
  
  /**
   * 
   * @return the result
   */
  public GrouperDdlCompareResult compareDatabase() {
    
    result = new GrouperDdlCompareResult();
    
    
    Platform platform = GrouperDdlUtils.retrievePlatform(false);
    
    //convenience to get the url, user, etc of the grouper db, helps get db connection
    GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile("grouper");
    
    Connection connection = null;
    
    String schema = grouperDb.getUser().toUpperCase();
    
    //postgres needs lower I think
    if (platform.getName().toLowerCase().contains("postgre")) {
      schema = schema.toLowerCase();
    }
        
    try {
      connection = grouperDb.connection();

      List<String> objectNames = GrouperDdlUtils.retrieveObjectNames();
      //System.err.println(GrouperUtil.toStringForLog(objectNames));
      for (String objectName : objectNames) {
        compareDatabaseForObject(objectName, connection, schema, platform);
      }

    } finally {
      GrouperUtil.closeQuietly(connection);
    }
    
    this.result.getResult().insert(0, "\n");

    if (this.result.getErrorCount() == 0 && this.result.getWarningCount() == 0) {
      String summary = "SUCCESS: Database DDL is correct!\n";
      this.result.getResult().insert(0, summary);
      this.result.getResult().append("\n\n" + summary);
    } else if (this.result.getErrorCount() > 0) {
      String summary = "ERROR: Database DDL has " + this.result.getErrorCount() 
        + " errors and " + this.result.getWarningCount() + " warnings!\n";
      this.result.getResult().insert(0, summary);
      this.result.getResult().append("\n\n" + summary);
    } else if (this.result.getWarningCount() > 0) {
      String summary = "WARNING: Database DDL has " + this.result.getWarningCount() + " warnings!\n";
      this.result.getResult().insert(0, summary);
      this.result.getResult().append("\n\n" + summary);
    }
    this.result.getResult().insert(0, "\n");

    return result;
    
  }

  private void compareDatabaseForObject(String objectName, Connection connection, String schema, Platform platform) {

    if (StringUtils.equals("Subject", objectName)) {
      return;
    }

    if (StringUtils.equals("GrouperLoader", objectName)) {
      LOG.warn("GrouperLoader should not be in the Grouper_ddl table, deleting");
      HibernateSession.bySqlStatic().executeSql("delete from grouper_ddl where object_name = 'GrouperLoader'");
      return;
    }
    
    Class<Enum> objectEnumClass = null;
    
    try {
      objectEnumClass = GrouperDdlUtils.retrieveDdlEnum(objectName);
    } catch (RuntimeException e) {
      //if this is grouper or subject, we have problems
      if (StringUtils.equals(objectName, "Grouper") || StringUtils.equals(objectName, "Subject")) {
        throw e;
      }
      return;
    }
    
    //this is the version in java
    int javaVersion = GrouperDdlUtils.retrieveDdlJavaVersion(objectName); 
    
    DdlVersionable ddlVersionableJava = GrouperDdlUtils.retieveVersion(objectName, javaVersion);
    
    StringBuilder historyBuilder = GrouperDdlUtils.retrieveHistory(objectName);
    
    //this is the version in the db
    int realDbVersion = GrouperDdlUtils.retrieveDdlDbVersion(objectName);
    DdlVersionable dbDdlVersionableDatabase = GrouperDdlUtils.retieveVersion(objectName, realDbVersion);
    GrouperVersion grouperVersionDatabase = new GrouperVersion(dbDdlVersionableDatabase.getGrouperVersion());
    GrouperVersion grouperVersionJava = new GrouperVersion(ddlVersionableJava.getGrouperVersion());

    boolean versionMismatch = javaVersion != realDbVersion;

    boolean okIfSameMajorAndMinorVersion = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("registry.auto.ddl.okIfSameMajorAndMinorVersion", true);
    boolean sameMajorAndMinorVersion = grouperVersionDatabase == null ? false : grouperVersionDatabase.sameMajorMinorArg(grouperVersionJava);

    if (versionMismatch && okIfSameMajorAndMinorVersion && sameMajorAndMinorVersion) {
      versionMismatch = false;
    }

    this.result.getResult().append("Note: Database version for " + objectName + ": " + realDbVersion + " (" + grouperVersionDatabase + ")\n");
    this.result.getResult().append("Note: Java version for " + objectName + ": " + javaVersion + " (" + grouperVersionJava + ")\n");

    // explain the frozen DDL version: the GrouperDdl model stops at V47 ("DON'T ADD ANY MORE Vs") and
    // all schema changes after that are delivered as Upgrade Tasks, so a version that sits at 47 forever
    // is expected and not a sign of a problem.
    if (StringUtils.equals(objectName, "Grouper")) {
      this.result.getResult().append("Note: " + javaVersion + " is the final Grouper DDL model version (the model is frozen here on purpose).  "
          + "Schema changes after version " + javaVersion + " are applied by Upgrade Tasks (Configure -> Upgrade tasks), "
          + "not by bumping this version, so just because the database and Java versions both say " + javaVersion + " does not mean the database DDL is correct.\n");
    }

    if (realDbVersion == javaVersion) {
      this.result.getResult().append("Success: Database version is the same as the Java codebase Grouper version\n");
    }
    if (realDbVersion > javaVersion) {
      this.result.getResult().append("Warning: Database version is greater than the Java codebase Grouper version.  This is probably ok.  You should run the DDL compare using the grouper JVM with the same version.  You should run all JVMs at the same version\n");
    }
    if (realDbVersion < javaVersion) {
      this.result.getResult().append("Error: Database version is less than the Java codebase Grouper version.  The registry needs to be updated\n");
    }

    if (historyBuilder.length() == 0) {
      this.result.getResult().append("Note: History: " + historyBuilder + "\n");
    }
    
    DbMetadataBean dbMetadataBean = GrouperDdlUtils.findDbMetadataBean(ddlVersionableJava);
    
    //to be safe lets only deal with tables related to this object
    platform.getModelReader().setDefaultTablePattern(dbMetadataBean.getDefaultTablePattern());
    //platform.getModelReader().setDefaultTableTypes(new String[]{"TABLES"});
    platform.getModelReader().setDefaultSchemaPattern(dbMetadataBean.getSchema());
      
    SqlBuilder sqlBuilder = platform.getSqlBuilder();

    {
      //it needs a name, just use "grouper"
      Database currentDatabase = platform.readModelFromDatabase(connection, GrouperDdlUtils.PLATFORM_NAME, null, null, null);
      addTablesFromDatabase(currentDatabase);
    }

    {
      Database javaDatabase = generateJavaDatabase(objectName, connection, schema, platform,
          javaVersion, ddlVersionableJava, sqlBuilder);
  
      addTablesFromJava(javaDatabase);
    }

    analyzeFunctions(objectName);
    analyzeTables();
    analyzeViews();
    
    this.result.getResult().append("\nNote: the -deep SQL script is only for the Grouper team internally and should not be run without advice from the Grouper team!" );

//    for (GrouperDdlCompareTable grouperDdlCompareTable : this.result.getGrouperDdlCompareTables().values()) {
//      System.out.println(grouperDdlCompareTable.getName());
//      
//    }

//    for (GrouperDdlCompareView grouperDdlCompareView : this.result.getGrouperViewsInJava().values()) {
//      System.out.println(grouperDdlCompareView.getName());
//    }
    
  }
  
  private void analyzeFunctions(String objectName) {
    if (!StringUtils.equalsIgnoreCase(objectName, "Grouper")) {
      return;
    }
    analyzeFunction("grouper_to_timestamp");
    analyzeFunction("grouper_to_timestamp_utc");
  }
  
  private void analyzeFunction(String functionName) {
    if (!GrouperDdlUtils.doesFunctionExist(functionName)) {
      this.result.getResult().append("ERROR: Function '"+ functionName +"' does not exist.\n");
      this.result.errorIncrement();      
    } else {
      this.result.getResult().append("Success: Function '"+ functionName + "' exists.\n");
    }
  }

  private void analyzeTables() {
    // analyze tables
    for (String tableName : this.result.getGrouperDdlCompareTables().keySet()) {
      
      GrouperDdlCompareTable grouperDdlCompareTable = this.result.getGrouperDdlCompareTables().get(tableName);
      
      Table databaseTable = grouperDdlCompareTable.getDatabaseTable();
      Table javaTable = grouperDdlCompareTable.getJavaTable();

      StringBuilder tableErrors = new StringBuilder();
      StringBuilder tableWarnings = new StringBuilder();
      StringBuilder tableNotes = new StringBuilder();
      
      if (databaseTable == null) {
        grouperDdlCompareTable.setMissing(true);
        tableErrors.append("Missing table.  ");
      } else if (javaTable == null) {
        grouperDdlCompareTable.setExtra(true);
        tableWarnings.append("Extra table.  ");
      } else {
        
        analyzeColumns(grouperDdlCompareTable.getGrouperDdlCompareColumns(), tableErrors, tableWarnings, tableNotes);
        analyzeIndexes(grouperDdlCompareTable.getGrouperDdlCompareIndexes(), tableErrors, tableWarnings, tableNotes);
        analyzeForeignKeys(grouperDdlCompareTable.getDatabaseForeignKeys(), 
            grouperDdlCompareTable.getJavaForeignKeys(), tableErrors, tableWarnings, tableNotes);
        
      }
      grouperDdlCompareTable.setCorrect(true);
      if (tableErrors.length() > 0) {
        this.result.getResult().append("ERROR: ");
        grouperDdlCompareTable.setCorrect(false);
        this.result.errorIncrement();
      }
      if (tableWarnings.length() > 0) {
        this.result.getResult().append("Warning: ");
        grouperDdlCompareTable.setCorrect(false);
        this.result.warningIncrement();
      }
      if (grouperDdlCompareTable.isCorrect()) {
        this.result.getResult().append("Success: ");
      }
      this.result.getResult().append("Table '" + tableName + "': ").append(tableErrors).append(tableWarnings).append(tableNotes);
      if (grouperDdlCompareTable.isCorrect()) {
        this.result.getResult().append("Table is up to date.  " 
            + grouperDdlCompareTable.getGrouperDdlCompareColumns().size() + " columns, " 
            + grouperDdlCompareTable.getGrouperDdlCompareIndexes().size() + " indexes, "
            + grouperDdlCompareTable.getDatabaseForeignKeys().size() + " foreign keys."
            );
      }
      this.result.getResult().append("\n");
    }
  }

  private void analyzeForeignKeys(Map<String, ForeignKey> databaseForeignKeys,
      Map<String, ForeignKey> javaForeignKeys, StringBuilder tableErrors,
      StringBuilder tableWarnings, StringBuilder tableNotes) {
    
    Set<String> foreignKeyNames = new TreeSet<String>();
    
    foreignKeyNames.addAll(databaseForeignKeys.keySet());
    foreignKeyNames.addAll(javaForeignKeys.keySet());
    
    // analyze columns
    for (String foreignKeyName : foreignKeyNames) {
      
      ForeignKey databaseForeignKey = databaseForeignKeys.get(foreignKeyName);
      ForeignKey javaForeignKey = javaForeignKeys.get(foreignKeyName);

      if (databaseForeignKey == null) {
        tableErrors.append("Missing foreign key '" + foreignKeyName + "'.  ");
      } else if (javaForeignKey == null) {
        tableWarnings.append("Extra foreign key '" + foreignKeyName + "'.  ");
      } else {

        if (!StringUtils.equalsIgnoreCase(databaseForeignKey.toVerboseString(), javaForeignKey.toVerboseString())) {
          
          if (databaseForeignKey.getReferenceCount() != javaForeignKey.getReferenceCount()) {

            tableErrors.append("Foreign key '" + foreignKeyName + "'.  column count '" + databaseForeignKey.getReferenceCount() 
              + "' should be '" + javaForeignKey.getReferenceCount() + "'.  ");

          } else {          

            if (!StringUtils.equalsIgnoreCase(databaseForeignKey.getForeignTableName(), javaForeignKey.getForeignTableName())) {
              
              tableErrors.append("Foreign key '" + foreignKeyName + "'.  foreign table '" + databaseForeignKey.getForeignTableName()
                + "' should be '" + javaForeignKey.getForeignTableName() + "'.  ");

            }
            
            for (int i=0;i<databaseForeignKey.getReferenceCount();i++) {
             
              Reference databaseReference = databaseForeignKey.getReferences()[i];
              Reference javaReference = javaForeignKey.getReferences()[i];

              if (!StringUtils.equalsIgnoreCase(databaseReference.getForeignColumnName(), javaReference.getForeignColumnName())) {
                
                tableErrors.append("Foreign key '" + foreignKeyName + "'.  foreign col '" + databaseReference.getForeignColumnName()
                  + "' should be '" + javaReference.getForeignColumnName() + "'.  ");

              }

              if (!StringUtils.equalsIgnoreCase(databaseReference.getLocalColumnName(), javaReference.getLocalColumnName())) {
                
                tableErrors.append("Foreign key '" + foreignKeyName + "'.  local col '" + databaseReference.getLocalColumnName()
                  + "' should be '" + javaReference.getLocalColumnName() + "'.  ");

              }

            }


          }
          
          tableNotes.append("Database foreign key: " + databaseForeignKey.toVerboseString() 
            + ", java foreign key: " + javaForeignKey.toVerboseString() + ".  ");
          
        }

      }
    }
    
  }

  /**
   * return true if ok
   * @param grouperDdlCompareColumns
   * @param tableErrors
   * @param tableWarnings
   * @param tableNotes
   * @return
   */
  private void analyzeColumns(
      Map<String, GrouperDdlCompareColumn> grouperDdlCompareColumns,
      StringBuilder tableErrors, StringBuilder tableWarnings, StringBuilder tableNotes) {
    // analyze columns
    for (String columnName : grouperDdlCompareColumns.keySet()) {
      
      GrouperDdlCompareColumn grouperDdlCompareColumn = grouperDdlCompareColumns.get(columnName);
      
      Column databaseColumn = grouperDdlCompareColumn.getDatabaseColumn();
      Column javaColumn = grouperDdlCompareColumn.getJavaColumn();

      if (databaseColumn == null) {
        grouperDdlCompareColumn.setMissing(true);
        tableErrors.append("Missing column '" + columnName + "'.  ");
      } else if (javaColumn == null) {
        grouperDdlCompareColumn.setExtra(true);
        tableErrors.append("Extra column '" + columnName + "'.  ");
      } else {
        
        if (!StringUtils.equals(databaseColumn.getDefaultValue(), javaColumn.getDefaultValue())) {
          tableErrors.append("Column '" + columnName + "' default value '" + databaseColumn.getDefaultValue() 
            + "' should be '" + javaColumn.getDefaultValue() + "'.  ");
          
        }
        if (databaseColumn.isOfTextType() !=  javaColumn.isOfTextType()) {
          tableWarnings.append("Column '" + columnName + "' text type '" + databaseColumn.isOfTextType()
            + "' should be '" + javaColumn.isOfTextType() + "'.  ");
        }
        if (databaseColumn.isOfNumericType() !=  javaColumn.isOfNumericType()) {
          tableWarnings.append("Column '" + columnName + "' numeric type '" + databaseColumn.isOfNumericType()
            + "' should be '" + javaColumn.isOfNumericType() + "'.  ");
        }
        if (databaseColumn.isOfSpecialType() !=  javaColumn.isOfSpecialType()) {
          tableWarnings.append("Column '" + columnName + "' special type '" + databaseColumn.isOfSpecialType()
            + "' should be '" + javaColumn.isOfSpecialType() + "'.  ");
        }
        // GRP-7175: suppress a false-positive precision mismatch that only shows up on Oracle for
        // grouper_audit_entry's int01..int05 / duration_microseconds / hibernate_version_number.
        // Two things combine to produce it:
        //  1. The expected (java) model is built by reading the live DB and replaying the ddlutils
        //     steps.  Almost every column uses ddlutilsFindOrCreateColumn, which leaves an existing
        //     column's size at whatever the DB reported - so on Oracle it inherits NUMBER(38) and
        //     matches.  These audit columns are the only ones that go through ddlutilsFixSizeColumn,
        //     which force-overwrites the modeled size to 19, pushing the expected value away from
        //     the DB's 38.
        //  2. The ddlutils Oracle platform maps java BIGINT to the fixed native type "NUMBER(38)"
        //     (see Oracle8Platform) and reads NUMBER(38,0) back as BIGINT (see Oracle8ModelReader).
        //     The 38 is baked into the type string, so the pinned precision of 19 never reaches the
        //     generated DDL - the column installs as NUMBER(38).  mysql/postgres store bigint as a
        //     19-digit type, which matches the pinned 19, so only Oracle diverges.
        // So when both sides are BIGINT and the database is the expected NUMBER(38), the precision
        // difference is cosmetic - treat it as equal.  Narrower integers are untouched: SMALLINT maps
        // to NUMBER(5), DECIMAL/NUMERIC to NUMBER(size), so a genuinely narrower column does not read
        // back as NUMBER(38)/BIGINT and is still compared normally.
        boolean oracleBigintPrecisionEquivalent = GrouperDdlUtils.isOracle()
            && databaseColumn.getTypeCode() == Types.BIGINT
            && javaColumn.getTypeCode() == Types.BIGINT
            && databaseColumn.getSizeAsInt() == 38;

        if (!oracleBigintPrecisionEquivalent && !StringUtils.equals(databaseColumn.getSize(), javaColumn.getSize())) {
          tableWarnings.append("Column '" + columnName + "' size '" + databaseColumn.getSize()
            + "' should be '" + javaColumn.getSize() + "'.  ");
        }
        if (!oracleBigintPrecisionEquivalent && databaseColumn.getPrecisionRadix() !=  javaColumn.getPrecisionRadix()) {
          tableWarnings.append("Column '" + columnName + "' precision '" + databaseColumn.getPrecisionRadix()
            + "' should be '" + javaColumn.getPrecisionRadix() + "'.  ");
        }
        if (databaseColumn.getScale() !=  javaColumn.getScale()) {
          tableWarnings.append("Column '" + columnName + "' scale '" + databaseColumn.getScale()
            + "' should be '" + javaColumn.getScale() + "'.  ");
        }
        if (databaseColumn.isPrimaryKey() !=  javaColumn.isPrimaryKey()) {
          tableErrors.append("Column '" + columnName + "' primary key '" + databaseColumn.isPrimaryKey()
            + "' should be '" + javaColumn.isPrimaryKey() + "'.  ");
        }
        if (databaseColumn.isRequired() !=  javaColumn.isRequired()) {
          tableErrors.append("Column '" + columnName + "' required '" + databaseColumn.isRequired()
            + "' should be '" + javaColumn.isRequired() + "'.  ");
        }
      }
    }
  }

  private void addTablesFromJava(Database javaDatabase) {
    // add in tables from java
    for (Table table : javaDatabase.getTables()) {
      GrouperDdlCompareTable grouperDdlCompareTable =  this.result.getGrouperDdlCompareTables().get(table.getName().toLowerCase());
      if (grouperDdlCompareTable == null) {
        grouperDdlCompareTable = new GrouperDdlCompareTable();
        grouperDdlCompareTable.setName(table.getName().toLowerCase());
        this.result.getGrouperDdlCompareTables().put(grouperDdlCompareTable.getName(), grouperDdlCompareTable);
      }
      grouperDdlCompareTable.setJavaTable(table);
      
      for (Column column : table.getColumns()) {

        String columnName = column.getName().toLowerCase();

        GrouperDdlCompareColumn grouperDdlCompareColumn = grouperDdlCompareTable.getGrouperDdlCompareColumns().get(columnName);

        if (grouperDdlCompareColumn == null) {
          
          grouperDdlCompareColumn = new GrouperDdlCompareColumn();
          grouperDdlCompareColumn.setName(columnName);
          grouperDdlCompareTable.getGrouperDdlCompareColumns().put(columnName, grouperDdlCompareColumn);
          
        }
        
        grouperDdlCompareColumn.setJavaColumn(column);
        
      }
      
      for (ForeignKey foreignKey : GrouperUtil.nonNull(table.getForeignKeys(), ForeignKey.class)) {
        
        grouperDdlCompareTable.getJavaForeignKeys().put(foreignKey.getName().toLowerCase(), foreignKey);
        
      }

      for (Index index : table.getIndices()) {
        String indexName = index.getName().toLowerCase();

        GrouperDdlCompareIndex grouperDdlCompareIndex = grouperDdlCompareTable.getGrouperDdlCompareIndexes().get(indexName);
        
        if (grouperDdlCompareIndex == null) {
          grouperDdlCompareIndex = new GrouperDdlCompareIndex();
          grouperDdlCompareTable.getGrouperDdlCompareIndexes().put(indexName, grouperDdlCompareIndex);
          grouperDdlCompareIndex.setName(indexName);
          
        }
        grouperDdlCompareIndex.setJavaIndex(index);
        

        for (IndexColumn indexColumn : index.getColumns()) {
          String indexColumnName = indexColumn.getName().toLowerCase();

          GrouperDdlCompareIndexColumn grouperDdlCompareColumn = grouperDdlCompareIndex.getGrouperDdlCompareColumns().get(indexColumnName);
          
          if (grouperDdlCompareColumn == null) {
            grouperDdlCompareColumn = new GrouperDdlCompareIndexColumn();
            grouperDdlCompareIndex.getGrouperDdlCompareColumns().put(indexColumnName, grouperDdlCompareColumn);
            grouperDdlCompareColumn.setName(indexColumnName);
            
          }

          grouperDdlCompareColumn.setJavaColumn(indexColumn);
          
        }

      }


    }
  }

  private Database generateJavaDatabase(String objectName, Connection connection,
      String schema, Platform platform, int javaVersion,
      DdlVersionable ddlVersionableJava, SqlBuilder sqlBuilder) {
    Database javaDatabase = platform.readModelFromDatabase(connection, GrouperDdlUtils.PLATFORM_NAME, null, null, null);
    
    //get this to the current version
    GrouperDdlUtils.upgradeDatabaseVersion(javaDatabase, null, 0, objectName, javaVersion, 
        new StringBuilder(), new StringBuilder(), platform, connection, schema, sqlBuilder);
      
    {
      Database javaDatabaseBlank = platform.readModelFromDatabase(connection, GrouperDdlUtils.PLATFORM_NAME, null, null, null);

      //drop all views since postgres will drop view cascade (and we dont know about it), and cant create or replace with changes
      DdlVersionBean tempDdlVersionBean = new DdlVersionBean(objectName, platform, connection, schema, sqlBuilder, javaDatabaseBlank, javaDatabase, 
          new StringBuilder(), true, javaVersion, new StringBuilder(), 0);
      tempDdlVersionBean.setGrouperDdlCompareResult(this.result);
      GrouperDdlUtils.ddlVersionBeanThreadLocalAssign(tempDdlVersionBean);
      try {
        ddlVersionableJava.addAllForeignKeysViewsEtc(tempDdlVersionBean);

      } finally {
        GrouperDdlUtils.ddlVersionBeanThreadLocalClear();
      }
    }
    return javaDatabase;
  }

  private void addTablesFromDatabase(Database currentDatabase) {
    // add in tables from database
    for (Table table : currentDatabase.getTables()) {
      GrouperDdlCompareTable grouperDdlCompareTable = new GrouperDdlCompareTable();
      grouperDdlCompareTable.setName(table.getName().toLowerCase());
      grouperDdlCompareTable.setDatabaseTable(table);
      this.result.getGrouperDdlCompareTables().put(grouperDdlCompareTable.getName(), grouperDdlCompareTable);

      for (Column column : table.getColumns()) {
        GrouperDdlCompareColumn grouperDdlCompareColumn = new GrouperDdlCompareColumn();
        String columnName = column.getName().toLowerCase();

        grouperDdlCompareTable.getGrouperDdlCompareColumns().put(columnName, grouperDdlCompareColumn);
        grouperDdlCompareColumn.setDatabaseColumn(column);
        
        grouperDdlCompareColumn.setName(columnName);
        
      }

      for (ForeignKey foreignKey : GrouperUtil.nonNull(table.getForeignKeys(), ForeignKey.class)) {
        
        grouperDdlCompareTable.getDatabaseForeignKeys().put(foreignKey.getName().toLowerCase(), foreignKey);
        
      }

      for (Index index : table.getIndices()) {
        GrouperDdlCompareIndex grouperDdlCompareIndex = new GrouperDdlCompareIndex();
        String indexName = index.getName().toLowerCase();

        grouperDdlCompareTable.getGrouperDdlCompareIndexes().put(indexName, grouperDdlCompareIndex);
        grouperDdlCompareIndex.setDatabaseIndex(index);
        
        grouperDdlCompareIndex.setName(indexName);

        for (IndexColumn indexColumn : index.getColumns()) {
          GrouperDdlCompareIndexColumn grouperDdlCompareIndexColumn = new GrouperDdlCompareIndexColumn();
          String indexColumnName = indexColumn.getName().toLowerCase();

          grouperDdlCompareIndex.getGrouperDdlCompareColumns().put(indexColumnName, grouperDdlCompareIndexColumn);
          grouperDdlCompareIndexColumn.setDatabaseColumn(indexColumn);
          
          grouperDdlCompareIndexColumn.setName(indexColumnName);
          
        }

      }

    }
  }

  /**
   * return true if ok
   * @param grouperDdlCompareIndexes
   * @param tableErrors
   * @param tableWarnings
   * @param tableNotes
   * @return
   */
  private void analyzeIndexes(
      Map<String, GrouperDdlCompareIndex> grouperDdlCompareIndexes,
      StringBuilder tableErrors, StringBuilder tableWarnings, StringBuilder tableNotes) {
    // analyze columns
    for (String indexName : grouperDdlCompareIndexes.keySet()) {
      
      GrouperDdlCompareIndex grouperDdlCompareIndex = grouperDdlCompareIndexes.get(indexName);
      
      Index databaseIndex = grouperDdlCompareIndex.getDatabaseIndex();
      Index javaIndex = grouperDdlCompareIndex.getJavaIndex();
  
      if (databaseIndex == null) {
        grouperDdlCompareIndex.setMissing(true);
        tableErrors.append("Missing index '" + indexName + "'.  ");
      } else if (javaIndex == null) {
        grouperDdlCompareIndex.setExtra(true);
        tableWarnings.append("Extra index '" + indexName + "'.  ");
      } else {
        
        if (!StringUtils.equals(databaseIndex.toVerboseString().toLowerCase(), javaIndex.toVerboseString().toLowerCase())) {
          if (databaseIndex.isUnique() != javaIndex.isUnique()) {
            tableErrors.append("Index '" + indexName + "' unique '" + databaseIndex.isUnique() 
              + "' should be '" + javaIndex.isUnique() + "'.  ");
          }
          
          analyzeIndexColumns(indexName, grouperDdlCompareIndex.getGrouperDdlCompareColumns(), tableErrors, tableWarnings, tableNotes);
          tableNotes.append("Database index: " + databaseIndex.toVerboseString() + ", java index: " + databaseIndex.toVerboseString() + ".  ");
        }
      }
    }
  }

  /**
   * @param grouperDdlCompareColumns
   * @param tableErrors
   * @param tableWarnings
   * @param tableNotes
   * @return
   */
  private void analyzeIndexColumns(String indexName, 
      Map<String, GrouperDdlCompareIndexColumn> grouperDdlCompareIndexColumns,
      StringBuilder tableErrors, StringBuilder tableWarnings, StringBuilder tableNotes) {
    
    // analyze columns
    for (String columnName : grouperDdlCompareIndexColumns.keySet()) {
      
      GrouperDdlCompareIndexColumn grouperDdlCompareIndexColumn = grouperDdlCompareIndexColumns.get(columnName);
      
      IndexColumn databaseIndexColumn = grouperDdlCompareIndexColumn.getDatabaseColumn();
      IndexColumn javaIndexColumn = grouperDdlCompareIndexColumn.getJavaColumn();

      if (databaseIndexColumn == null) {
        grouperDdlCompareIndexColumn.setMissing(true);
        tableErrors.append("Index '" + indexName + "' missing column '" + columnName + "'.  ");
      } else if (javaIndexColumn == null) {
        grouperDdlCompareIndexColumn.setExtra(true);
        tableErrors.append("Index '" + indexName + "' extra column '" + columnName + "'.  ");
      } else {
        
        if (!StringUtils.equals(databaseIndexColumn.getSize(), javaIndexColumn.getSize())) {
          tableWarnings.append("Index '" + indexName + "' column '" + columnName + "' size '" + databaseIndexColumn.getSize() 
            + "' should be '" + javaIndexColumn.getSize() + "'.  ");
        }
        
        if (databaseIndexColumn.getOrdinalPosition() != javaIndexColumn.getOrdinalPosition()) {
          tableWarnings.append("Index '" + indexName + "' column '" + columnName + "' ordinal position '" + databaseIndexColumn.getOrdinalPosition()
            + "' should be '" + javaIndexColumn.getOrdinalPosition() + "'.  ");
        }
      }
    }
  }

  private void analyzeViews() {
    // analyze views
    for (String viewName : this.result.getGrouperViewsInJava().keySet()) {
      
      GrouperDdlCompareView grouperDdlCompareView = this.result.getGrouperViewsInJava().get(viewName);
      
      StringBuilder viewErrors = new StringBuilder();
      StringBuilder viewWarnings = new StringBuilder();
      StringBuilder viewNotes = new StringBuilder();
      
      GcTableSyncTableMetadata viewMetadata = null;
      
      try {
        viewMetadata = GcTableSyncTableMetadata.retrieveTableMetadataFromDatabase("grouper", grouperDdlCompareView.getName());
      } catch (Exception e) {
        LOG.error("error getting metadata on view: " + grouperDdlCompareView.getName());
      }

      if (viewMetadata == null) {
        grouperDdlCompareView.setMissing(true);
        viewErrors.append("Missing view.  ");
      } else {

        Set<String> databaseColumns = new TreeSet<String>();
        Set<String> javaColumns = new TreeSet<String>();

        for (GrouperDdlCompareColumn grouperDdlCompareColumn : grouperDdlCompareView.getGrouperDdlCompareColumns()) {
          
          javaColumns.add(grouperDdlCompareColumn.getName().toLowerCase());
          
        }

        for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : viewMetadata.getColumnMetadata()) {
          
          databaseColumns.add(gcTableSyncColumnMetadata.getColumnName().toLowerCase());
          
        }

        if (databaseColumns.size() != javaColumns.size()) {

          viewErrors.append("column count " + databaseColumns.size() + " but should be " + javaColumns.size() + ".  ");
        }
        {
          Set<String> missingColumns = new TreeSet<String>(javaColumns);
          missingColumns.removeAll(databaseColumns);
          if (missingColumns.size() > 0) {
            viewErrors.append("missing columns: " + GrouperUtil.join(missingColumns.iterator(), ", ") + ".  ");
          }
        }
        {
          Set<String> extraColumns = new TreeSet<String>(databaseColumns);
          extraColumns.removeAll(javaColumns);
          if (extraColumns.size() > 0) {
            viewErrors.append("extra columns: " + GrouperUtil.join(extraColumns.iterator(), ", ") + ".  ");
          }
        }
        
      }
      grouperDdlCompareView.setCorrect(true);
      if (viewErrors.length() > 0) {
        this.result.getResult().append("ERROR: ");
        grouperDdlCompareView.setCorrect(false);
        this.result.errorIncrement();
      }
      if (viewWarnings.length() > 0) {
        this.result.getResult().append("Warning: ");
        grouperDdlCompareView.setCorrect(false);
        this.result.warningIncrement();
      }
      if (grouperDdlCompareView.isCorrect()) {
        this.result.getResult().append("Success: ");
      }
      this.result.getResult().append("View '" + viewName + "': ").append(viewErrors).append(viewWarnings).append(viewNotes);
      if (grouperDdlCompareView.isCorrect()) {
        this.result.getResult().append("View is up to date.  " 
            + grouperDdlCompareView.getGrouperDdlCompareColumns().size() + " columns." );
      }
      this.result.getResult().append("\n");
    }
  }  
}