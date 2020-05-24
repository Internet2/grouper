package edu.internet2.middleware.grouper.ddl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.internal.SessionImpl;
import org.hibernate.persister.entity.AbstractEntityPersister;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperDdl;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils.DbMetadataBean;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateParam;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class GrouperDdlDataMigration {

  public static void main(String[] args) {
    
    String result = new GrouperDdlDataMigration().assignDatabaseFrom("grouper").assignDatabaseTo("mysqlDb").migrateDatabase();
    
    System.out.println(result);
    
  }
 
  private String databaseFrom;
  
  private String databaseTo;
  
  public GrouperDdlDataMigration assignDatabaseFrom(String theDatabaseFrom) {
    this.databaseFrom = theDatabaseFrom;
    return this;
  }
  
  public GrouperDdlDataMigration assignDatabaseTo(String theDatabaseTo) {
    this.databaseTo = theDatabaseTo;
    return this;
  }
  
  /**
   * 
   * @param <T>
   * @param theClass
   * @return the list
   */
  private static <T> List<T> hqlList(String connectionName, String hql, List<HibernateParam> bindVarNameParams) {

    GrouperContext.incrementQueryCount();

    Session session = null;
    
    try {
      session = Hib3DAO.session(connectionName);
      
      Query query = session.createQuery(hql);
      HibUtils.attachBindValues(query, bindVarNameParams);
      List list = query.list();
      
      evictCollection(session, list);
  
      return list;
    } finally {
      sessionEnd(session, false);
    }    
  }
  
  private static void sessionEnd(Session session, boolean hadTransaction) {
    
    if (session == null) {
      return;
    }
    //if we are readonly, and we have work, then that is bad
    if (hadTransaction && session.isDirty()) {
      try {
        ((SessionImpl)session).connection().rollback();
      } catch (SQLException sqle) {
        //ignore
      }
    }
    
    //put all the queries on the wire
    session.flush();

    //clear out session to avoid duplicate objects in session
    session.clear();
    
    try {
      // if already closed (not sure why), just ignore
      if (session.isConnected() && session.isOpen()) {
        session.close();
      }
    } catch (Exception e) {
      // swallow the exception... no throwing, no logging
    }

  }

  

  private static void evict(Session session,
      Object object, boolean flushBeforeEvict) {

    //not sure it could ever be null...
    if (object != null) {
      if (flushBeforeEvict) {
        session.flush();
      }
      try {
        session.evict(object);
      } catch (Exception e) {}
    }
  }

  /**
   * <pre>
   * evict a list of objects from hibernate.  do this always for two reasons:
   * 1. If you edit an object that is in the hibernate session, and commit, it will
   * commit those changes magically.  Only objects called session.save(obj) or 
   * update etc should be committed
   * 2. If you select an object, then try to store it back (but have a different
   * reference, e.g. if the DTO went through it, then you will get an exception:
   * "a different object with the same identifier value was already associated with the session"
   * </pre>
   * @param hibernateSession grouper hibernateSession
   * @param list of objects from hibernate to evict
   * @param onlyEvictIfNotNew true to only evict if this is a nested tx
   */
  private static void evictCollection(Session session,  Collection<?> list) {
    if (list == null) {
      return;
    }
    session.flush();
    for (Object object : list) {
      evict(session, object, false);
    }
  }

  private StringBuilder result;
  
  private Map<String, Object> debugMap = Collections.synchronizedMap(new LinkedHashMap<String, Object>());
  
  public String migrateDatabase() {
    debugMap.put("elapsed", DurationFormatUtils.formatDuration(0, "HH:mm:ss.S"));
    final boolean[] done = new boolean[] {false};
    final long[] started = new long[] {System.currentTimeMillis()};
    Thread statusThread = new Thread(new Runnable() {

      @Override
      public void run() {
        long lastLogMillis = System.currentTimeMillis();
        while (true) {
          for (int i=0;i<30;i++) {
            if (done[0]) {
              return;
            }
            try {
              Thread.sleep(1000);
            } catch (InterruptedException ie) {
              return;
            }
            if (System.currentTimeMillis() - lastLogMillis > 15000) { 
              debugMap.put("elapsed", DurationFormatUtils.formatDuration(System.currentTimeMillis() - started[0], "HH:mm:ss.S"));
              System.out.println(GrouperUtil.mapToString(debugMap));
              lastLogMillis = System.currentTimeMillis();
            }
          }
        }
      }
      
    });

    statusThread.setDaemon(true);
    statusThread.start();
    
    try {
    
      this.result = new StringBuilder();
  
      boolean tablesExist = false;
      try {
        // lets see what we are working with
        List<Hib3GrouperDdl> hib3GrouperDdls = hqlList(databaseTo, "from Hib3GrouperDdl", null);
        tablesExist = true;
        if (GrouperUtil.length(hib3GrouperDdls) > 0) {
          Hib3GrouperDdl hib3GrouperDdl = Hib3GrouperDdl.findInList(hib3GrouperDdls,  "Grouper");
          result.append("Warning: tables exist in destination, assuming will continue where left off if last run failed.  Assuming constraints are dropped and tables exist.  Only blank target tables will be migrated\n");
        }
      } catch (Exception e) {
        // tables not there?
      }
      
      
      String state = "STEP1: are grouper tables are in destination? " + tablesExist;
      debugMap.put("state", state);
      result.append(state + "\n");
      
      // add tables to destination
      GrouperDdlScript grouperDdlScript = new GrouperDdlScript().assignDatabaseConnection(this.databaseTo).parseScript("Grouper_install");
      GrouperDdlScript subjectDdlScript = new GrouperDdlScript().assignDatabaseConnection(this.databaseTo).parseScript("Subject_install");
      GrouperDdlScript grouperDdlWorkerScript = new GrouperDdlScript().assignDatabaseConnection(this.databaseTo).parseScript("Grouper_createDdlWorker");
  
      GrouperDdlScript[] grouperDdlScripts = new GrouperDdlScript[] {grouperDdlScript, subjectDdlScript, grouperDdlWorkerScript};
      
      if (!tablesExist) {
        state = "STEP2: creating tables in destination...";
        debugMap.put("state", state);
        result.append(state+"\n");
        for (GrouperDdlScript current : grouperDdlScripts) {
          current.runTableScript();
        }
        state = "STEP2: complete";
        result.append(state + "\n");
        debugMap.put("state", state);
      } else {
        state = "STEP2: no need to create tables in destination...";
        debugMap.put("state", state);
        result.append(state + "\n");
      }
      
      state = "STEP3: analyzing tables and columns...";
      debugMap.put("state", state);
      result.append(state + "\n");
      compareTablesAndColumns();
      state = "STEP3: complete";
      debugMap.put("state", state);
      result.append(state + "\n");
  
      state = "STEP4: syncing tables...";
      debugMap.put("state", state);
      result.append(state + "\n");

      for (String tableName : tableToEntityName.keySet()) {
        syncTable(tableName, tableToEntityName.get(tableName));
      }

      state = "STEP4: complete";
      result.append(state + "\n");

      state = "STEP5: creating indexes, foreign keys, and views in destination...";
      debugMap.put("state", state);
      result.append(state+"\n");
      for (GrouperDdlScript current : grouperDdlScripts) {
        current.runIndexScript();
      }
      for (GrouperDdlScript current : grouperDdlScripts) {
        current.runForeignKeyScript();
      }
      for (GrouperDdlScript current : grouperDdlScripts) {
        current.runViewScript();
      }
      state = "STEP5: complete";
      result.append(state + "\n");
      debugMap.put("state", state);

      result.append("Took: " + DurationFormatUtils.formatDuration(System.currentTimeMillis() - started[0], "HH:mm:ss.S") + "\n");
      
      return result.toString();
    } catch (RuntimeException re) {
      debugMap.put("state", "error");
      re.printStackTrace();
      LOG.error("error", re);
      throw re;
    } finally {
      done[0] = true;
      statusThread.interrupt();
      GrouperUtil.threadJoin(statusThread);
      System.out.println(GrouperUtil.mapToString(debugMap));
      LOG.warn("Database migration output: " + GrouperUtil.mapToString(debugMap));
    }
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDdlDataMigration.class);

  /**
   * sync a table
   * @param tableName
   * @param entityName
   */
  private void syncTable(final String tableName, final String entityName) {
    
    debugMap.put("table", tableName);
    
    final String idPropertyName = (String)(tableToPrimaryKeyProperty.get(tableName) instanceof String ? tableToPrimaryKeyProperty.get(tableName) : null);
    
    // increment an integer with a list of work to do
    final Map<Integer, List> workToDo = Collections.synchronizedMap(new LinkedHashMap<Integer, List>());
    
    final long[] rowsFrom = new long[] {-1};
    final long[] rowsTo = new long[] {-1};

    final int[] numberOfIndexesForWork = new int[] {-1};

    final boolean[] skipping = new boolean[] {false};
    
    final RuntimeException[] runtimeException = new RuntimeException[] {null};
    Thread fromThread = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          rowsFrom[0] = new GcDbAccess().connectionName(GrouperDdlDataMigration.this.databaseFrom).sql("select count(1) from " + tableName).select(long.class);
          debugMap.put("rowsFrom", rowsFrom[0]);
  
          if (rowsFrom[0] == 0) {
            numberOfIndexesForWork[0] = 0;
            return;
          }
          
          if (idPropertyName == null) {
            // composite key... punt
            List objects = hqlList(databaseFrom, "from " + entityName, null);
            workToDo.put(0, objects);
            numberOfIndexesForWork[0] = 1;
  
          } else {
            //lets get all ids
            debugMap.put("selectingIdsFrom", true);
            List primaryKeys = hqlList(databaseFrom, "select " + idPropertyName + " from " + entityName, null);
            Collections.sort(primaryKeys);
            rowsFrom[0] = GrouperUtil.length(primaryKeys);
            debugMap.remove("selectingIdsFrom");
            int batchSize = 10000;
            
            int numberOfBatches = GrouperUtil.batchNumberOfBatches(GrouperUtil.length(primaryKeys), batchSize);
            
            for (int i=0;i<numberOfBatches;i++) {
              
              if (numberOfIndexesForWork[0] == -2) {
                return;
              }

              // dont get too far ahead
              while(workToDo.size() > 3) {
                GrouperUtil.sleep(100);
                if (numberOfIndexesForWork[0] == -2) {
                  return;
                }
              }
              
              List primaryKeysBatch = GrouperUtil.batchList(primaryKeys, batchSize, i);
              
              debugMap.put("selectingBatch", i);
              
              List<HibernateParam> hibernateParams = GrouperUtil.toList(
                  new HibernateParam("idStart", primaryKeysBatch.get(0), primaryKeysBatch.get(0).getClass()),
                  new HibernateParam("idEnd", primaryKeysBatch.get(primaryKeysBatch.size()-1), primaryKeysBatch.get(primaryKeysBatch.size()-1).getClass()));
              
              List objects = hqlList(databaseFrom, "from " + entityName + " where " + idPropertyName 
                  + " >= :idStart and " + idPropertyName + " <= :idEnd", hibernateParams);
  
              workToDo.put(i, objects);
              
            }
            numberOfIndexesForWork[0] = numberOfBatches;
            debugMap.remove("selectingBatch");
          }        
        } catch (RuntimeException e) {
         runtimeException[0] = e;
         e.printStackTrace();
         LOG.error("error", e);
         numberOfIndexesForWork[0] = -2;
        }
      }

      
    });

    fromThread.start();

    try {
      rowsTo[0] = new GcDbAccess().connectionName(GrouperDdlDataMigration.this.databaseTo).sql("select count(1) from " + tableName).select(long.class);
      debugMap.put("rowsTo", rowsTo[0]);
      if (rowsTo[0] > 0) {
        debugMap.put("skipping", "rows exist in destination, skipping this table");
        skipping[0] = true;
      } else {
        int workIndexToDo = 0;
        while(true) {
          if (numberOfIndexesForWork[0] == -2) {
            break;
          }
          if (numberOfIndexesForWork[0] >= 0 && workIndexToDo >= numberOfIndexesForWork[0]) {
            break;
          }
          GrouperUtil.sleep(100);
          
          // dont get too far ahead
          if (workToDo.containsKey(workIndexToDo)) {
            debugMap.put("insertingBatch", workIndexToDo);
            List rows = workToDo.get(workIndexToDo);
            workToDo.remove(workIndexToDo);
            saveBatch(databaseTo, rows, entityName);
            rowsTo[0]+=GrouperUtil.length(rows);
            debugMap.put("rowsTo", rowsTo[0]);
            workIndexToDo++;
          }
        }          
      }
     } catch (RuntimeException e) {
      numberOfIndexesForWork[0] = -2;
      throw e;
     }
    
    GrouperUtil.threadJoin(fromThread);
 
    if (runtimeException[0] != null) {
      throw runtimeException[0];
    }
    
    if (skipping[0]) {
      this.result.append("Warning: skipping table: " + tableName + " since rows exist in destination\n");
    } else {
      if (rowsFrom[0] == rowsTo[0]) {
        this.result.append("Success: table: " + tableName + " migrated " + rowsTo[0] + " rows\n");
      } else {
        error = true;
        this.result.append("Error: table: " + tableName + " rows in source: " + rowsFrom[0] + ", rows in destination: " + rowsTo[0] + "\n");
      }
    }
    debugMap.remove("table");
    debugMap.remove("rowsFrom");
    debugMap.remove("rowsTo");

  }
  private boolean error = false;
  
  private void compareTablesAndColumns() {
    Map<String, Set<String>> hibernateTablesToColumns = retrieveHibernateTablesAndColumns();
    Map<String, Set<String>> databaseTablesToColumns = retrieveDatabaseTablesAndColumns(this.databaseFrom);

    this.result.append("SUCCESS: found " + databaseTablesToColumns.size() + " tables to sync\n");
    this.result.append("SUCCESS: found " + hibernateTablesToColumns.size() + " objects to sync\n");
    
    Set<String> tablesNotInDatabase = new HashSet<String>(hibernateTablesToColumns.keySet());
    tablesNotInDatabase.removeAll(databaseTablesToColumns.keySet());
    
    if (tablesNotInDatabase.size() > 0) {
      this.result.append("Warning: these tables are not in the database: " + GrouperUtil.toStringForLog(tablesNotInDatabase) + "\n");
    }
    
    Set<String> tablesNotInHibernate = new HashSet<String>(databaseTablesToColumns.keySet());
    tablesNotInHibernate.removeAll(hibernateTablesToColumns.keySet());
    
    if (tablesNotInHibernate.size() > 0) {
      this.result.append("Warning: these tables are not in hibernate: " + GrouperUtil.toStringForLog(tablesNotInHibernate) + "\n");
    }
    
    databaseTablesToColumns.keySet().removeAll(tablesNotInHibernate);
    hibernateTablesToColumns.keySet().removeAll(tablesNotInDatabase);
    tableToEntityName.keySet().removeAll(tablesNotInDatabase);

    this.result.append("SUCCESS: will sync " + hibernateTablesToColumns.size() + " objects\n");
    
    for (String tableName : databaseTablesToColumns.keySet()) {
      Set<String> columnsInDatabase = new TreeSet<String>(databaseTablesToColumns.get(tableName));
      Set<String> columnsInHibernate = new TreeSet<String>(hibernateTablesToColumns.get(tableName));
      
      Set<String> columnsNotInDatabase = new TreeSet<String>(columnsInHibernate);
      columnsNotInDatabase.removeAll(columnsInDatabase);
      
      if (GrouperUtil.length(columnsNotInDatabase) > 0) {
        throw new RuntimeException("This table '" + tableName + "' has columns not in database: " + GrouperUtil.toStringForLog(columnsNotInDatabase) + "\n");
      }
      
      Set<String> columnsNotInHibernate = new TreeSet<String>(columnsInDatabase);
      columnsNotInHibernate.removeAll(columnsInHibernate);
      
      if (GrouperUtil.length(columnsNotInHibernate) > 0) {
        throw new RuntimeException("This table '" + tableName + "' has columns not in hibernate: " + GrouperUtil.toStringForLog(columnsNotInHibernate));
      }
      
      if (GrouperUtil.length(columnsInDatabase) != GrouperUtil.length(columnsInHibernate)) {
        throw new RuntimeException("Cannot sync " + tableName + " since the database columns donnt match hibernate: " 
            + GrouperUtil.toStringForLog(columnsNotInHibernate) + " vs: " + GrouperUtil.toStringForLog(columnsNotInDatabase));
      }
    }
    
  }
  
  private Map<String, Set<String>> retrieveDatabaseTablesAndColumns(String connectionName) {
    
    Map<String, Set<String>> result = new TreeMap<String, Set<String>>();
    
    Platform platform = GrouperDdlUtils.retrievePlatform(false, connectionName);
    
    //convenience to get the url, user, etc of the grouper db, helps get db connection
    GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile(connectionName);
    
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

        //this is the version in java
        int javaVersion = GrouperDdlUtils.retrieveDdlJavaVersion(objectName); 
        
        DdlVersionable ddlVersionableJava = GrouperDdlUtils.retieveVersion(objectName, javaVersion);

        DbMetadataBean dbMetadataBean = GrouperDdlUtils.findDbMetadataBean(ddlVersionableJava);
        
        //to be safe lets only deal with tables related to this object
        platform.getModelReader().setDefaultTablePattern(dbMetadataBean.getDefaultTablePattern());
        
        //platform.getModelReader().setDefaultTableTypes(new String[]{"TABLES"});
        platform.getModelReader().setDefaultSchemaPattern(dbMetadataBean.getSchema());

        //it needs a name, just use "grouper"
        Database currentDatabase = platform.readModelFromDatabase(connection, GrouperDdlUtils.PLATFORM_NAME, null, null, null);

        // add in tables from database
        for (Table table : currentDatabase.getTables()) {
          
          Set<String> columns = new TreeSet<String>();
          result.put(table.getName().toLowerCase(), columns);
          for (int i=0;i<table.getColumnCount();i++) {
            columns.add(table.getColumn(i).getName().toLowerCase());
          }
        }

      }            
    } catch (Exception e) {
      throw new RuntimeException("error", e);
    } finally {
      GrouperUtil.closeQuietly(connection);
    }
    return result;
  }
  
  private Map<String, String> tableToEntityName = null;
  // could be array if composite key
  private Map<String, Object> tableToPrimaryKeyProperty = null;

  /**
   *     
   * @return map
   */
  private Map<String, Set<String>> retrieveHibernateTablesAndColumns() {

    tableToEntityName = new TreeMap<String, String>();
    tableToPrimaryKeyProperty = new TreeMap<String, Object>();
    
    Map<String, Set<String>> result = new TreeMap<String, Set<String>>();

    SessionFactoryImpl sessionFactory = (SessionFactoryImpl)Hib3DAO.getSessionFactory("grouper");

    for (String entityName : sessionFactory.getAllClassMetadata().keySet()) {

      AbstractEntityPersister entityPersister = (AbstractEntityPersister)sessionFactory.getEntityPersister(entityName);
      String tableName = entityPersister.getTableName().toLowerCase();
      if (tableName.endsWith("_v")) {
        continue;
      }
      Set<String> columns = new TreeSet<String>();

      for (int i=0;i<entityPersister.getPropertyNames().length;i++) {
        String[] propertyColumnNames = entityPersister.getPropertyColumnNames(i);
        if (propertyColumnNames.length != 1) {
          throw new RuntimeException("Why more than one column? " + entityName 
              + ", " + GrouperUtil.toStringForLog(propertyColumnNames));
        }
        columns.add(propertyColumnNames[0].toLowerCase());
      }
      for (String columnName : entityPersister.getIdentifierColumnNames()) {
        columns.add(columnName.toLowerCase());
      }

      if (GrouperUtil.length(columns) > GrouperUtil.length(result.get(tableName))) {
        this.tableToEntityName.put(tableName, entityName);
        if (entityPersister.getIdentifierColumnNames().length == 1) {
          this.tableToPrimaryKeyProperty.put(tableName,  entityPersister.getIdentifierPropertyName());
        } else {
          this.tableToPrimaryKeyProperty.put(tableName,  new Object[0]);
        }
        result.put(tableName, columns);
      }
    }
    return result;
  }

  public <T> void saveBatch(String connectionName, final List<T> collection, String entityName) {
    
    if (GrouperUtil.length(collection) == 0) {
      return;
    }
    
    int batchSize = 1000;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(collection, batchSize);
    for (int i=0;i<numberOfBatches;i++) {
      List<T> subBatch = GrouperUtil.batchList(collection, batchSize, i);
      saveBatchHelper(connectionName, subBatch, entityName); 
    }
    
  }

  private <T> void saveBatchHelper(String connectionName, final Collection<T> collection, String entityName) {

    Session session = null;
    
    try {
      session = Hib3DAO.session(connectionName);
      Transaction transaction = session.beginTransaction();
      int queries = 1+(GrouperUtil.length(collection) / GrouperHibernateConfig.retrieveConfig().propertyValueInt("hibernate.jdbc.batch_size", 200));

      GrouperContext.incrementQueryCount(queries);
      
      for (Object object : collection) {

        if (StringUtils.isBlank(entityName)) {
          session.save(object);
        } else {
          session.save(entityName, object);
        }
           
      }

      //evictCollection(session, collection);

      session.flush();
      session.clear();

      transaction.commit();
    } catch (Exception e) {
      throw new RuntimeException("error", e);
    } finally {
      sessionEnd(session, false);
    }    

    
  }
}
