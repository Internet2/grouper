/*
 * @author mchyzer $Id: GrouperDdlUtils.java,v 1.2 2008-07-25 06:17:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.io.StringWriter;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.NonUniqueIndex;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.UniqueIndex;
import org.apache.ddlutils.platform.SqlBuilder;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperDdl;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class GrouperDdlUtils {

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperDdlUtils.class);

  /**
   * retrieve the ddl utils platform
   * @return the platform object
   */
  public static Platform retrievePlatform() {
    String ddlUtilsDbnameOverride = GrouperConfig.getProperty("ddlutils.dbname.override");
    Platform platform = null;

    //convenience to get the url, user, etc of the grouper db
    GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile("grouper");

    if (StringUtils.isBlank(ddlUtilsDbnameOverride)) {
      platform = PlatformFactory.createNewPlatformInstance(grouperDb.getDriver(),
          grouperDb.getUrl());
    } else {
      platform = PlatformFactory.createNewPlatformInstance(ddlUtilsDbnameOverride);
    }

    return platform;
  }

  /**
   * kick off bootstrap
   * @param args
   */
  public static void main(String[] args) {
    GrouperStartup.startup();
  }
  
  /**
   * startup the process, if the version table is not there, print out that ddl
   */
  @SuppressWarnings("unchecked")
  public static void bootstrap() {
    
    
    Platform platform = retrievePlatform();
    
    //this is in the config or just in the driver
    String dbname = platform.getName();
    
    LOG.info("Ddl db name is: '" + dbname + "'");
    
    //convenience to get the url, user, etc of the grouper db, helps get db connection
    GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile("grouper");
    
    Connection connection = null;

    StringBuilder result = new StringBuilder();
    
    try {
      connection = grouperDb.connection();

      List<String> objectNames = retrieveObjectNames();
      
      for (String objectName : objectNames) {

        Class<Enum> objectEnumClass = retrieveDdlEnum(objectName);
        
        //this is the version in java
        int javaVersion = retrieveDdlJavaVersion(objectName); 
        
        DdlVersionable ddlVersionable = retieveVersion(objectName, javaVersion);
        
        StringBuilder historyBuilder = retrieveHistory(objectName);
        
        //this is the version in the db
        int dbVersion = retrieveDdlDbVersion(objectName);

        //see if same version, just continue, all good
        if (javaVersion == dbVersion) {
          continue;
        }
        
        //if the java is less than db, then grouper was rolled back... that might not be good
        if (javaVersion < dbVersion) {
          LOG.warn("Java version of db object name: " + objectName + " is " 
              + javaVersion + " which is less than the dbVersion " + dbVersion
              + ".  This means grouper was upgraded and rolled back?  Check in the enum "
              + objectEnumClass.getName() + " for details on if things are compatible.");
          //not much we can do here... good luck!
          continue;
        }

        //pattern to get only certain objects (e.g. GROUPERLOADER% )
        String defaultTablePattern = ddlVersionable.getDefaultTablePattern(); 
        //to be safe lets only deal with tables related to this object
        platform.getModelReader().setDefaultTablePattern(defaultTablePattern);
        //platform.getModelReader().setDefaultTableTypes(new String[]{"TABLES"});

        SqlBuilder sqlBuilder = platform.getSqlBuilder();

        //the db version is less than the java version
        //lets go up one version at a time until we are current
        for (int version = dbVersion+1; version<=javaVersion;version++) {

          ddlVersionable = retieveVersion(objectName, version);
          //we just want a script, see if one exists for this version
          String script = findScriptOverride(ddlVersionable, dbname);
          
          //if there was no override
          if (StringUtils.isBlank(script)) {
            
            //it needs a name, just use "grouper"
            Database oldDatabase = platform.readModelFromDatabase(connection, "grouper", null,
                grouperDb.getUser().toUpperCase(), null);
            Database newDatabase = platform.readModelFromDatabase(connection, "grouper", null,
                grouperDb.getUser().toUpperCase(), null);
            
            //get this to the previous version
            upgradeDatabaseVersion(oldDatabase, dbVersion, objectName, version-1);
            //get this to the current version
            upgradeDatabaseVersion(newDatabase, dbVersion, objectName, version);
            
            //upgrade to version: version
            //we need to upgrade from one version to another, but dont want to get the version from the DB, so 
            //call protected method via reflection
            StringWriter buffer = new StringWriter();
            sqlBuilder.setWriter(buffer);
            try {
              
              sqlBuilder.alterDatabase(oldDatabase, newDatabase, null);
            } catch (Exception e) {
              throw new RuntimeException("Problem with object name: " + objectName, e);
            }
            
            //GrouperUtil.callMethod(sqlBuilder.getClass(), sqlBuilder, "processTableStructureChanges",
            //  new Class[]{Database.class, Database.class, Table.class, Table.class, Map.class, List.class},
            //  new Object[]{oldDatabase, newDatabase, oldDatabase.findTable("grouper_ext_loader_log"), table, null, GrouperUtil.toList(addColumnChange)});

            script = buffer.toString();
            
            //String ddl = platform.getAlterTablesSql(connection, database);


          }
          //make sure no single quotes in any of these...
          String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
          //is this db independent?  if not, figure out what the issues are and fix so we can have comments
          String summary = timestamp + ": upgrade " + objectName + " from V" + (version-1) + " to V" + version;
          result.append("/* " + summary + " */\n");
          historyBuilder.insert(0, summary + ", ");
          
          String historyString = StringUtils.abbreviate(historyBuilder.toString(), 4000);

          if (!StringUtils.isBlank(script)) {
            result.append(script).append("\n\n");
          }
          if (version == 1) {
            result.append("insert into grouper_ddl (id, object_name, db_version, " +
            		"last_updated, history) values ('" + GrouperUuid.getUuid() 
                +  "', '" + objectName + "', 1, '" + timestamp + "', \n'" + historyString + "');\n");
          } else {
            result.append("update grouper_ddl set db_version = " + version 
                + ", last_updated = '" + timestamp + "', \nhistory = '" + historyString 
                + "' where object_name = '" + objectName + "';\n");
          }
          result.append("commit;\n\n");
        }
      }
      


    } finally {
      GrouperUtil.closeQuietly(connection);
    }

    String resultString = result.toString();
    
    if (StringUtils.isNotBlank(resultString)) {
      resultString = "Database requires updates:\n\n" + resultString + "\n\n";
      LOG.error(resultString);
      System.out.println("\n\n######################\n\n" + resultString 
          + "\n\n######################\n\n");
    }
    
  }

  /**
   * find history for a certain object name
   * @param objectName
   * @return the history or new stringbuilder if none available
   */
  public static StringBuilder retrieveHistory(String objectName) {
    retrieveDdlsFromCache();
    for (Hib3GrouperDdl hib3GrouperDdl : GrouperUtil.nonNull(cachedDdls)) {
      if (StringUtils.equals(objectName, hib3GrouperDdl.getObjectName())) {
        return hib3GrouperDdl.getHistory() == null ? new StringBuilder() 
          : new StringBuilder(hib3GrouperDdl.getHistory());
      }
    }
    return new StringBuilder();
  }
  
  /**
   * retrieve a version of a ddl object versionable
   * @param objectName
   * @param version
   * @return the ddl versionable
   */
  public static DdlVersionable retieveVersion(String objectName, int version) {
    
    String enumName = "V" + version;
    Class<Enum> enumClass = retrieveDdlEnum(objectName);
    
    try {
      return (DdlVersionable)Enum.valueOf(enumClass, enumName);
    } catch (Exception e) {
      throw new RuntimeException("Cant find version " + version + "(" + enumName 
          + ")  in objectName: " + objectName + ", " + enumClass.getName(), e);
    }
  }
  
  /** cache the ddls */
  private static List<Hib3GrouperDdl> cachedDdls = null;

  /**
   * get the version of a ddl object in the DB
   * @param objectName
   * @return the version or -1 if not in the DB
   */
  public static int retrieveDdlJavaVersion(String objectName) {
    
    Class<Enum> objectEnum = retrieveDdlEnum(objectName);
    
    //call currentVersion with reflection
    Integer currentVersion = (Integer)GrouperUtil.callMethod(objectEnum, "currentVersion");
    
    return currentVersion;
    
  }
  
  /**
   * the relationship between object name and the enum is as follows.  If the object name
   * is "Grouper", then the enum is edu.internet2.middleware.grouper.ddl.GrouperDdl
   * @param objectName
   * @return the enum
   */
  @SuppressWarnings("unchecked")
  public static Class<Enum> retrieveDdlEnum(String objectName) {
    return GrouperUtil.forName("edu.internet2.middleware.grouper.ddl." + objectName + "Ddl");
  }
  
  /**
   * get the cached ddls from the db and update the hibernate version if they are there
   */
  private static void retrieveDdlsFromCache() {
    //lazy load the cached ddls
    if (cachedDdls == null) {
      try {
        cachedDdls = retrieveDdlsFromDb();
        
        for (Hib3GrouperDdl hib3GrouperDdl : GrouperUtil.nonNull(cachedDdls)) {
          
          String objectName = hib3GrouperDdl.getObjectName();
          
          int ddlJavaVersion = retrieveDdlJavaVersion(objectName);
          
          LOG.info("Current java version for ddl '" + objectName + "' is " + ddlJavaVersion);
        }
      } catch (Exception e) {
        //just log, maybe the table isnt there
        LOG.error("maybe the grouper_ddl table isnt there... if that is the reason its ok.", e);
      }
    
      cachedDdls = GrouperUtil.defaultIfNull(cachedDdls, new ArrayList<Hib3GrouperDdl>());
    }
    

  }
  
  /**
   * get the object names from the 
   * @return the list of object names
   */
  public static List<String> retrieveObjectNames() {
    //init stuff
    retrieveDdlsFromCache();
    
    List<String> objectNames = new ArrayList<String>();
    for (Hib3GrouperDdl hib3GrouperDdl : cachedDdls) {
      objectNames.add(hib3GrouperDdl.getObjectName());
    }
    
    //make sure Grouper is in there
    if (!objectNames.contains("Grouper")) {
      objectNames.add("Grouper");
    }
    if (!objectNames.contains("Subject")) {
      objectNames.add("Subject");
    }
    if (GrouperConfig.getPropertyBoolean("ddlutils.exclude.subject.tables", false)) {
      objectNames.remove("Subject");
    }
    
    
    return objectNames;
  }
  
  /**
   * get the version of a ddl object in the DB
   * @param objectName
   * @return the version or -1 if not in the DB
   */
  public static int retrieveDdlDbVersion(String objectName) {

    //init stuff
    retrieveDdlsFromCache();
    
    //find the ddl in the list
    Hib3GrouperDdl hib3GrouperDdl = Hib3GrouperDdl.findInList(cachedDdls, objectName);
    if (hib3GrouperDdl != null) {
      return hib3GrouperDdl.getDbVersion();
    }
    return 0;
    
  }
  
  /**
   * get all the ddls, put grouper at the front
   * @return the ddls
   */
  @SuppressWarnings("unchecked")
  public static List<Hib3GrouperDdl> retrieveDdlsFromDb() {

    List<Hib3GrouperDdl> grouperDdls = HibernateSession.byCriteriaStatic().list(Hib3GrouperDdl.class, null); 

    //move the grouper one to the front
    if (grouperDdls != null) {
      for (int i = 0; i < GrouperUtil.length(grouperDdls); i++) {
        Hib3GrouperDdl hib3GrouperDdl = grouperDdls.get(i);
        if (StringUtils.equals(hib3GrouperDdl.getObjectName(), "Grouper")) {
          grouperDdls.remove(i);
          grouperDdls.add(0, hib3GrouperDdl);
          break;
        }
      }
    }
    return grouperDdls;

  }

  /**
   * <pre>
   * File name must be objectName.V#.dbname.sql
   * e.g. Grouper.5.oracle10.sql
   * 
   * The dbname must be a valid ddlutils dbname:
   * axion, cloudscape, db2, db2v8, derby, firebird, hsqldb, interbase, maxdb, mckoi, 
   * mssql, mysql, mysql5, oracle, oracle10, oracle9, postgresql, sapdb, sybase, sybasease15
   *
   * Also the following catchalls are acceptable: oracleall, mysqlall, db2all, sybaseall
   * </pre>
   * @param dbObjectVersion e.g. Grouper or GrouperLoader
   * @param dbname e.g. oracle10 or mysql5
   * @return the script or blank if it is not found
   */
  public static String findScriptOverride(DdlVersionable dbObjectVersion, String dbname) {
    String objectName = dbObjectVersion.getObjectName();
    int version = dbObjectVersion.getVersion();
    //lets see if there is a specific one:
    String script = findScriptOverride(objectName, version, dbname);
    if (StringUtils.isBlank(script)) {
      //now see if there is a general one...
      String generalName = null;
      //this is not an exact science...  but here is the algorithm
      if (dbname.startsWith("oracle")) {
        generalName = "oracleall";
      } else if (dbname.startsWith("mysql")) {
        generalName = "mysqlall";
      } else if (dbname.startsWith("db2")) {
        generalName = "db2all";
      } else if (dbname.startsWith("sybase")) {
        generalName = "sybaseall";
      }
      if (StringUtils.isNotBlank(generalName)) {
        script = findScriptOverride(objectName, version, generalName);
      }
    }
    return script;
  }

  /**
   * <pre>
   * get an override file (exact, dont look for the all ones like oracleall)
   * File name must be objectName.V#.dbname.sql
   * e.g. Grouper.5.oracle10.sql
   * 
   * </pre>
   * @param objectName
   * @param version
   * @param dbNameExact
   * @return the script or null if none found
   */
  public static String findScriptOverride(String objectName, int version, String dbNameExact) {
    String resourceName = "/ddl/" + objectName + "." + version + "." + dbNameExact + ".sql";
    String script = null;
    script = GrouperUtil.readResourceIntoString(resourceName, true);
    return script;
  }
  
  /**
   * find a version from an enum version int
   * @param ddlVersion
   * @return the version
   */
  public static int versionIntFromEnum(Enum ddlVersion) {
    String name = ddlVersion.name();
    if (!name.startsWith("V")) {
      throw new RuntimeException("Version enums must start with V: " + name);
    }
    String version = name.substring(1);
    return GrouperUtil.intValue(version);

  }
  
  
  /**
   * find the object name from the db object version
   * @param dbObjectVersion
   * @return the object name
   */
  public static String objectName(Enum dbObjectVersion) {
    String className = dbObjectVersion.getDeclaringClass().getSimpleName();
    
    //now we have GrouperEnum, strip off the Enum part
    if (!className.endsWith("Ddl")) {
      throw new RuntimeException("Db object version classes MUST end in Ddl! '" + className + "'");
    }
    String objectName = className.substring(0, className.length()-3);
    return objectName;
  }
  
  /**
   * get a database object of a certain version based on the existing database, and tack on
   * all the enums up to the version we want (if any)
   * @param baseVersion
   * @param baseDatabaseVersion
   * @param objectName
   * @param requestedVersion
   */
  public static void upgradeDatabaseVersion(Database baseVersion, int baseDatabaseVersion, 
      String objectName, int requestedVersion) {
    if (baseDatabaseVersion == requestedVersion) {
      return;
    }
    //loop up to the version we need
    for (int version = baseDatabaseVersion+1; version<=requestedVersion; version++) {
      //get the enum
      DdlVersionable ddlVersionable = retieveVersion(objectName, version);
      //do an incremental update
      ddlVersionable.updateVersionFromPrevious(baseVersion);
    }
  }
  
  /**
   * find or create table
   * @param database
   * @param tableName
   * @param description currently not used, but eventually can be the data dictionary comment
   * @return the table
   */
  public static Table ddlutilsFindOrCreateTable(Database database, String tableName, String description) {
    Table table = database.findTable(tableName);
    if (table == null) {
      table = new Table();
      table.setName(tableName);
      table.setDescription(description);
      database.addTable(table);
    }
    return table;
  }

  /**
   * add an index on a table.  drop a misnamed or a misuniqued index which is existing
   * @param database
   * @param tableName
   * @param indexName 
   * @param unique
   * @param columnNames
   * @return the index which is the new one, or existing one if it already exists
   */
  public static Index ddlutilsFindOrCreateIndex(Database database, String tableName, String indexName, 
      boolean unique, String... columnNames) {
    Table table = GrouperDdlUtils.ddlutilsFindTable(database,tableName);

    //search for the index
    OUTERLOOP:
    for (Index existingIndex : table.getIndices()) {
      if (existingIndex.getColumnCount() == columnNames.length) {
        
        //no need to check if unique.  you dont want two of the same index, one unique, the other not
        //look through existing columns (order is important)
        //see if this is not a match
        for (int i=0;i<columnNames.length;i++) {
          if (!StringUtils.equalsIgnoreCase(existingIndex.getColumn(i).getName(), columnNames[i])) {
            continue OUTERLOOP;
          }
        }
        
        //if we made it this far, it is the same index!
        
        //if exactly the same, leave it be
        if (unique == existingIndex.isUnique() && StringUtils.equalsIgnoreCase(indexName, existingIndex.getName())) {
          return existingIndex;
        }
        
        table.removeIndex(existingIndex);
      }
    }
    
    Index index = unique ? new UniqueIndex() : new NonUniqueIndex();
    index.setName(indexName);
    
    for (String columnName : columnNames) {
      
      Column column = GrouperDdlUtils.ddlutilsFindColumn(table, columnName);
      IndexColumn nameColumn = new IndexColumn(column);
      index.addColumn(nameColumn);
      
    }
    
    table.addIndex(index);
    return index;
  }

  /**
   * add a foreign key on a table.  drop a misnamed foreign key which is existing
   * @param database
   * @param tableName
   * @param foreignKeyName 
   * @param foreignTableName 
   * @param localColumnName
   * @param foreignColumnName 
   * @return the foreign key which is the new one, or existing one if it already exists
   */
  public static ForeignKey ddlutilsFindOrCreateForeignKey(Database database, String tableName, String foreignKeyName, 
      String foreignTableName, String localColumnName, String foreignColumnName) {
    return ddlutilsFindOrCreateForeignKey(database, tableName, foreignKeyName, foreignTableName, 
        GrouperUtil.toList(localColumnName), GrouperUtil.toList(foreignColumnName));
  }

  /**
   * add a foreign key on a table.  drop a misnamed foreign key which is existing
   * @param database
   * @param tableName
   * @param foreignKeyName 
   * @param foreignTableName 
   * @param localColumnNames 
   * @param foreignColumnNames 
   * @return the foreign key which is the new one, or existing one if it already exists
   */
  public static ForeignKey ddlutilsFindOrCreateForeignKey(Database database, String tableName, String foreignKeyName, 
      String foreignTableName, List<String> localColumnNames, List<String> foreignColumnNames) {
    
    //validate inputs
    if (localColumnNames.size() != foreignColumnNames.size()) {
      throw new RuntimeException("Local col size must equal foreign col size: " 
          + localColumnNames.size() + " != " + foreignColumnNames.size());
    }
    
    Table table = GrouperDdlUtils.ddlutilsFindTable(database,tableName);
    Table foreignTable = GrouperDdlUtils.ddlutilsFindTable(database,foreignTableName);
    
    //search for the foreign key
    OUTERLOOP:
    for (ForeignKey foreignKey : table.getForeignKeys()) {
      if (foreignKey.getReferences().length == localColumnNames.size()) {
        for (int i=0;i<localColumnNames.size();i++) {

          Reference reference = foreignKey.getReferences()[i];
          
          //if this isnt a match
          if (!StringUtils.equalsIgnoreCase(reference.getForeignColumnName(), foreignColumnNames.get(i))
              || !StringUtils.equalsIgnoreCase(reference.getLocalColumnName(), localColumnNames.get(i))) {
            continue OUTERLOOP;
          }
        }
        
        //if we made it this far, it is the same foreign key!
        
        //if exactly the same, leave it be
        if (StringUtils.equalsIgnoreCase(foreignKeyName, foreignKey.getName())) {
          return foreignKey;
        }
        
        table.removeForeignKey(foreignKey);
      }
    }
    
    ForeignKey foreignKey = new ForeignKey(foreignKeyName);
    foreignKey.setForeignTableName(foreignTableName);
    
    for (int i=0;i<localColumnNames.size();i++) {
      
      Column localColumn = GrouperDdlUtils.ddlutilsFindColumn(table, localColumnNames.get(i));
      Column foreignColumn = GrouperDdlUtils.ddlutilsFindColumn(foreignTable, foreignColumnNames.get(i));
      
      Reference reference = new Reference(localColumn, foreignColumn);
      
      foreignKey.addReference(reference);
    }
    
    table.addForeignKey(foreignKey);
    return foreignKey;
  }
  
  /**
   * find table, if not exist, throw exception
   * @param database
   * @param tableName
   * @return the table
   */
  public static Table ddlutilsFindTable(Database database, String tableName) {
    Table table = database.findTable(tableName);
    if (table == null) {
      throw new RuntimeException("Cant find table: '" + tableName 
          + "', perhaps you need to rollback your ddl version in the DB and sync up");
    }
    return table;
  }
  
  /**
   * find column, if not exist, throw exception
   * @param table table to get column from
   * @param columnName column name of column (case insensitive)
   * @return the column
   */
  public static Column ddlutilsFindColumn(Table table, String columnName) {
    Column[] columns = table.getColumns();
    
    for (Column column : GrouperUtil.nonNull(columns)) {
      
      if (StringUtils.equalsIgnoreCase(columnName, column.getName())) {
        return column;
      }
    }
    
    throw new RuntimeException("Cant find table: '" + table.getName() 
        + "' columns: '" + columnName + "', perhaps you need to rollback your ddl version in the DB and sync up");
  }
  
  /**
   * find or create column with various properties
   * @param table 
   * @param columnName 
   * @param description not used, but eventually can be used for data dictionary
   * @param typeCode from java.sql.Types
   * @param size string, can be a simple int, or comma separated, see ddlutils docs
   * @param primaryKey this should only be true for new tables
   * @param required this should probably only be true for new tables (since ddlutils will copy to temp table)
   * @return the column
   */
  public static Column ddlutilsFindOrCreateColumn(Table table, String columnName, String description, 
      int typeCode, String size, boolean primaryKey, boolean required) {

    Column column = table.findColumn(columnName);
    
    if (column == null) {
      column = new Column();
      column.setName(columnName);
      //just add to end of columns
      table.addColumn(column);
    }

    column.setPrimaryKey(primaryKey);
    column.setRequired(required);
    column.setDescription(description);
    column.setTypeCode(typeCode);
    column.setSize(size);
    return column;
  }
  
}
