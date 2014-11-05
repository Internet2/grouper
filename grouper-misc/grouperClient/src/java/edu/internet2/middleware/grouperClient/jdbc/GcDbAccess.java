package edu.internet2.middleware.grouperClient.jdbc;


import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;



/** 
 * <pre>Get access to the global database connections, create a new connection, 
 * and execute sql against them.</pre>
 * @author harveycg
 */
public class GcDbAccess {

  /**
   * A map to cache result bean data in based on a key, and host it for a particular amount of time.
   */
  private static Map<MultiKey, GcDbQueryCache> dbQueryCacheMap = new GcDbQueryCacheMap();


  /**
   * How long the currently selected objects will be stored in the cache.
   */
  private Integer cacheMinutes;

  /**
   * If true, the map queryAndTime will be populated with date regarding the time spent in each unique query (unique by query string, not considering bind variable values).
   */
  private static boolean accumulateQueryMillis;

  /**
   * A map of the time spent in each unique query (unique by query string, not considering bind variable values).
   */
  private static Map<String, GcQueryReport> queriesAndMillis;


  /**
   * The amount of seconds that the query can run before being rolled back.
   */
  private Integer queryTimeoutSeconds;

  /**
   * The list of bind variable objects.
   */
  private List<Object> bindVars;

  /**
   * If you are executing a statement as a batch, this is the list of lists of bind variables to set.
   */
  private List<List<Object>> batchBindVars;


  /**
   * The sql to execute.s
   */
  private String sql;

  
  /**
   * If selecting something by primary key, this is one or many keys.
   */
  private List<Object> primaryKeys;


  /**
   * connection name from the config file, or null for default
   */
  private String connectionName;
 
  /**
   * connection name from the config file, or null for default
   * @param theConnectionName
   * @return this for chaining
   */
  public GcDbAccess connectionName(String theConnectionName) {
    this.connectionName = theConnectionName;
    return this;
  }
  
  /**
   * end a transaction
   * @param transactionEnd
   * @param endOnlyIfStarted
   */
  public static void transactionEnd(GcTransactionEnd transactionEnd, boolean endOnlyIfStarted) {
    transactionEnd(transactionEnd, endOnlyIfStarted, null);
  }
  
  /**
   * end a transaction
   * @param transactionEnd
   * @param endOnlyIfStarted
   * @param connectionName 
   */
  public static void transactionEnd(GcTransactionEnd transactionEnd, boolean endOnlyIfStarted, String connectionName) {
    
    ConnectionBean connectionBean = connection(false, false, connectionName);
    
    Connection connection = connectionBean.getConnection();
    
    if (connection == null) {
      throw new RuntimeException("There is no connection!");
    }

    ConnectionBean.transactionEnd(connectionBean, transactionEnd, endOnlyIfStarted, true, false);
  }
  

  /**
   * The connection that we are using.
   */
  private Connection connection;

  /**
   * If selecting by example, set this and all column values will be used to create a where clause.
   */
  private Object example;


  /**
   * If selecting by example, set this and all column values of the given example object except null values will be used to create a where clause.
   */
  private boolean omitNullValuesForExample;


  /**
   * The number of rows touched if an update, delete, etc was executed.
   */
  private int numberOfRowsAffected;


  /**
   * The number of batch rows touched if an update, delete, etc was executed.
   */
  private int numberOfBatchRowsAffected[];


  /**
   * <pre>This is our helper to convert data to and from Oracle. It is externalized because it will likely be 
   * common that editing will need to be done on a per project basis.</pre>
   */
  private static GcBoundDataConversion boundDataConversion = new GcBoundDataConversionImpl();


  /**
   * Whether we registered all of the dbconnection classes yet or not.
   */
  private static boolean dbConnectionClassesRegistered = false;

  /**
   * This is the helper to convert data to and from Oracle, which has a default of BoundDataConversionImpl. 
   * If you encounter errors getting and setting data from oracle to java, you may need to override the default
   * and set your version here. Otherwise, nothing is needed.
   * @param _boundDataConversion the boundDataConversion to set.
   */
  public static void loadBoundDataConversion(GcBoundDataConversion _boundDataConversion) {
    boundDataConversion = _boundDataConversion;
  }



  /**
   * Create an in statement with the given number of bind variables: createInString(2) returns " (?,?) "
   * @param numberOfBindVariables is the number of bind variables to use.
   * @return the string.
   */
  public static String createInString(int numberOfBindVariables){
    StringBuilder results = new StringBuilder(" (");
    for (int i=0; i<numberOfBindVariables; i++){
      results.append("?,");
    }
    GrouperClientUtils.removeEnd(results, ",");
    results.append(") ");
    return results.toString();
  }


  /**
    /**
   * Set the list of bind variable objects, always replacing any that exist.
   * @param _bindVars are the variables to add to the list.
   * @return this.
   */
  public GcDbAccess bindVars(Object... _bindVars){
    this.bindVars = new ArrayList<Object>();

    for (Object bindVar : _bindVars){
      if (bindVar instanceof List){
        List<?> arrayData = (List<?>)bindVar;
        for (Object value : arrayData){
          this.bindVars.add(value);
        }             
      } else {
        this.bindVars.add(bindVar);
      }   
    }

    return this;
  }



  /**
   * Add to the list of bind variable objects, leaving any that exist there - if you use this in a transaction callback
   * you will have to clear bindvars between calls or they will accumulate.
   * @param _bindVar is the variable to add to the list.
   * @return this.
   */
  public GcDbAccess addBindVar(Object _bindVar){

    if (this.bindVars == null){
      this.bindVars = new ArrayList<Object>();
    }

    this.bindVars.add(_bindVar);

    return this;
  }


  /**
   * If you are executing sql as a batch statement, set the batch bind variables here.
   * @param _batchBindVars are the variables to set.
   * @return this.
   */
  public GcDbAccess batchBindVars(List<List<Object>> _batchBindVars){
    this.batchBindVars = _batchBindVars;
    return this;
  }


  /**
   * <pre>Cache the results of a SELECT query for the allotted minutes.
   * Note that cached objects are not immutable; if you modify them you are modifying them in the cache as well.</pre>
   * @param _cacheMinutes is how long to persist the object(s) in cache for after the initial selection.
   * @return this.
   */
  public GcDbAccess cacheMinutes(Integer _cacheMinutes){
    this.cacheMinutes = _cacheMinutes;
    return this;
  }


  /**
   * Set the sql to use.
   * @param _sql is the sql to use.
   * @return this.
   */
  public GcDbAccess sql(String _sql){
    this.sql = _sql;
    return this;
  }


  /**
   * If selecting by example, set this and all column values of the given example object except null values will be used to create a where clause.
   * @return this.
   */
  public GcDbAccess omitNullValuesForExample(){
    this.omitNullValuesForExample = true;
    return this;
  }




  /**
   * If selecting by example, set this and all column values will be used to create a where clause.
   * @param _example is the example to use.
   * @return this.
   */
  public GcDbAccess example(Object _example){
    this.example = _example;
    return this;
  }


  /**
   * The amount of seconds that the query can run before being rolled back.
   * @param _queryTimeoutSeconds is the amount of seconds to set.
   * @return this.
   */
  public GcDbAccess queryTimeoutSeconds(Integer _queryTimeoutSeconds){
    this.queryTimeoutSeconds = _queryTimeoutSeconds;
    return this;
  }



  /**
   * <pre>If true, the map queryAndTime will be populated with the time spent in each unique query (unique by query string, not considering bind variable values) 
   * - BE SURE TO TURN THIS OFF when done debugging, this is ONLY for debugging on the desktop!Turning it off CLEARS the stats, so write it off first!
   * Example:
   * 1. DbAccess.accumulateQueryMillis(true);
   * 2. use application normally
   * 3. Get the results: Map<String, Long> timeSpentInQueries = 
   * </pre>
   * @param _accumulateQueryMillis is whether to accumulate them or not.
   */
  public static void accumulateQueryMillis(boolean _accumulateQueryMillis){
    if (_accumulateQueryMillis){
      queriesAndMillis = new LinkedHashMap<String, GcQueryReport>();
    } else {
      queriesAndMillis.clear();
    }
    accumulateQueryMillis = _accumulateQueryMillis;
  }


  /**
   * <pre>Write the stats of queries and time spent in them to a file at the given location, then stop collection stats. accumulateQueryMillis(true)
   * must be called first to turn on debugging.</pre>
   * @param fileLocation is the location of the file to write.
   */
  public static void reportQueriesAndMillisAndTurnOffAccumulation(String fileLocation){
    if (!accumulateQueryMillis){
      throw new RuntimeException("accumulateQueryMillis must be set to true first!");
    }
    GcQueryReport.reportToFile(fileLocation, queriesAndMillis);
  }

  /**
   * Set the primary key to select by.
   * @param _primaryKey is the _primaryKey to use.
   * @return this.
   */
  public GcDbAccess primaryKey(Object... _primaryKey){
    this.primaryKeys = new ArrayList<Object>();

    if (_primaryKey != null && _primaryKey.length == 1 && _primaryKey[0] instanceof List){
      List<?> arrayData = (List<?>)_primaryKey[0];
      for (Object value : arrayData){
        this.primaryKeys.add(value);
      }
    } else if (_primaryKey != null){
      for (Object primaryKey : _primaryKey){
        this.primaryKeys.add(primaryKey);
      }
    }
    return this;
  }


  /**
   * Add information about the query that was just executed.
   * @param query is the query.
   * @param nanoTimeStarted is how many millis were spent executing the query.
   */
  private void addQueryToQueriesAndMillis(String query, Long nanoTimeStarted){
    if (!accumulateQueryMillis){
      return;
    }
    GcQueryReport queryReport = queriesAndMillis.get(query);
    if (queryReport == null){
      queryReport = new GcQueryReport();
      queryReport.setQuery(query);

      queriesAndMillis.put(query, queryReport);
    }

    queryReport.addExecutionTime((System.nanoTime() - nanoTimeStarted) / 1000000);
  }


  
  
  /**
   * <pre>Whether this class has already been saved to the database, looks for a field(s) with annotation @Persistable(primaryKeyField=true),
   * assumes that it is a number, and returns true if it is null or larger than 0.</pre>
   *  @param o is the object to store to the database.
   * @return true if so.
   */
  public boolean isPreviouslyPersisted(Object o){
    Field field = GcPersistableHelper.primaryKeyField(o.getClass());

    List<Field> compoundPrimaryKeys =  GcPersistableHelper.compoundPrimaryKeyFields(o.getClass());


    // Objects with no PK are never considered previously persisted.
    if (field == null && compoundPrimaryKeys.size() == 0){
      return false;
    }


    // We have a single primary key.
    if (field != null){
      Object fieldValue = null;
      try {
        fieldValue = field.get(o);
      } catch (Exception e) {
        throw new RuntimeException(e);
      } 

      if (fieldValue == null){
        return false;
      }

      // If it was manually assigned, we have to check the database.
      if (GcPersistableHelper.primaryKeyManuallyAssigned(field)){

        Long startTime = System.nanoTime();

        String query = "select count(*) from " + GcPersistableHelper.tableName(o.getClass()) + " where " +  GcPersistableHelper.columnName(field) + " =  ?";

        int count = new GcDbAccess()
            .sql(query)
            .bindVars(fieldValue)
            .select(int.class);

        addQueryToQueriesAndMillis(query, startTime);

        return count > 0;
      }

      // If field is numeric, it must be > 0.
      try{
        Long theId = new Long(String.valueOf(fieldValue));
        return theId > 0;
      } catch (Exception e){
        throw new RuntimeException("Expected primary key field of numeric type but got " + field.getName() + " of type " + field.getClass() + ". You need to override isPreviouslyPersisted() or provide a Persistable annotation for your primary key!", e);
      }
    }




    // We have multiple primary keys.
    if (compoundPrimaryKeys.size() > 0){

      List<Object> theBindVariables = new ArrayList<Object>();

      String theSql = "select count(*) from " + GcPersistableHelper.tableName(o.getClass()) + " where ";

      // Get all of the fields that are involved in the compound keys and build the sql and get bind variables.
      for (Field compoundPrimaryKey : compoundPrimaryKeys){
        Object fieldValue = null;
        try {
          fieldValue = compoundPrimaryKey.get(o);
        } catch (Exception e) {
          throw new RuntimeException(e);
        } 
        theSql += GcPersistableHelper.columnName(compoundPrimaryKey) + " = ? and ";
        theBindVariables.add(fieldValue);
      }
      theSql = theSql.substring(0, theSql.length() - 4);

      Long startTime = System.nanoTime();

      int count = new GcDbAccess()
          .sql(theSql)
          .bindVars(theBindVariables)
          .select(int.class);

      this.addQueryToQueriesAndMillis(theSql, startTime);

      return count > 0;
    }


    throw new RuntimeException("No primary key or compound primary keys specified!");

  }



  /**
   * Delete the object from  the database if it has already been stored - the object should have appropriate annotations from the PersistableX annotations.
   * @see GcPersistableClass - this annotation must be placed at the class level.
   * @see GcPersistableField these annotations may be placed at the method level depending on your needs.
   *  @param o is the object to delete from the database.
   */
  public  void deleteFromDatabase(Object o){
    if (!isPreviouslyPersisted(o)){
      return;
    }

    Field primaryKeyField = GcPersistableHelper.primaryKeyField(o.getClass());

    List<Field> compoundPrimaryKeys =  GcPersistableHelper.compoundPrimaryKeyFields(o.getClass());



    if (primaryKeyField == null && compoundPrimaryKeys.size() == 0){
      throw new RuntimeException("Cannot delete a row with no primary key or compound primary keys - use sql to delete the row instead of the method deleteFromDatabase().");
    }


    // Single primary key.
    if (primaryKeyField != null){
      String primaryKeyColumnName = GcPersistableHelper.columnName(primaryKeyField);

      Object primaryKey = null;

      try {
        primaryKey = primaryKeyField.get(o);
      } catch (Exception e) {
        throw new RuntimeException(e);
      } 

      String tableName = GcPersistableHelper.tableName(o.getClass());

      String sqlToUse = "delete from " + tableName + " where " + primaryKeyColumnName + " = ? ";

      this.sql(sqlToUse);
      this.bindVars(primaryKey);
      this.executeSql();

      return;
    }



    // Multiple column primary key.
    if (compoundPrimaryKeys.size() > 0){
      List<Object> theBindVariables = new ArrayList<Object>();

      String theSql = "delete from " + GcPersistableHelper.tableName(o.getClass()) + " where ";

      // Get all of the fields that are involved in the compound keys and build the sql and get bind variables.
      for (Field compoundPrimaryKey : compoundPrimaryKeys){
        Object fieldValue = null;
        try {
          fieldValue = compoundPrimaryKey.get(o);
        } catch (Exception e) {
          throw new RuntimeException(e);
        } 
        theSql += GcPersistableHelper.columnName(compoundPrimaryKey) + " = ? and ";
        theBindVariables.add(fieldValue);
      }
      theSql = theSql.substring(0, theSql.length() - 4);

      this.sql(theSql);
      this.bindVars(theBindVariables);
      this.executeSql();
    }

  }


  /**
   * Store the given objects to the database in one transaction - the object should have appropriate annotations from the PersistableX annotations.
   * @see GcPersistableClass - this annotation must be placed at the class level.
   * @see GcPersistableField these annotations may be placed at the method level depending on your needs.
   * @param <T> is the type to store.
   * @param objects are the object to store to the database.
   */
  public <T> void storeListToDatabase(final List<T> objects){
    storeBatchToDatabase(objects, 200);
  }


  /**
   * Store the given object to the database - the object should have appropriate annotations from the PersistableX annotations.
   * @see GcPersistableClass - this annotation must be placed at the class level.
   * @see GcPersistableField these annotations may be placed at the method level depending on your needs.
   * @param <T> is the type to store.
   * @param t is the object to store to the database.
   */
  public <T> void storeToDatabase(T t){

    if (!GrouperClientUtils.isBlank(this.sql)){
      throw new RuntimeException("Cannot use both sql and set an object to store.");
    }

    Map<String, Object> columnNamesAndValues = new HashMap<String, Object>();
    Object primaryKeyValue = null;

    // Get the primary key.
    Field primaryKey = GcPersistableHelper.primaryKeyField(t.getClass());

    List<Field> compoundPrimaryKeys =  GcPersistableHelper.compoundPrimaryKeyFields(t.getClass());

    try{
      
      boolean previouslyPersisted = isPreviouslyPersisted(t);

      boolean keepPrimaryKeyColumns = t instanceof GcSqlAssignPrimaryKey && !previouslyPersisted;
      
      if (keepPrimaryKeyColumns) {
        ((GcSqlAssignPrimaryKey)t).gcSqlAssignNewPrimaryKeyForInsert();
      }
      
      // Get column names and values
      for (Field field: GcPersistableHelper.heirarchicalFields(t.getClass())){
        field.setAccessible(true);
        // We are putting everything in here except the primary key because we may have to go out and get a primary key shortly
        // unless the primary key is manually assigned, in which case it can go in here.
        if ((primaryKey == null && GcPersistableHelper.isPersist(field, t.getClass())) || ( GcPersistableHelper.isPersist(field, t.getClass()) && (keepPrimaryKeyColumns || GcPersistableHelper.primaryKeyManuallyAssigned(primaryKey) || !GcPersistableHelper.isPrimaryKey(field)))){
          columnNamesAndValues.put(GcPersistableHelper.columnName(field), field.get(t));
        }
      }

      String sqlToUse = "";
      List<Object> bindVarstoUse = new ArrayList<Object>();

      // Update if we are already saved.

      if (previouslyPersisted){
        sqlToUse =  " update " + GcPersistableHelper.tableName(t.getClass()) + " set ";
        for (String columnName : columnNamesAndValues.keySet()){
          sqlToUse += " " + columnName + " = ?, " ;
          bindVarstoUse.add(columnNamesAndValues.get(columnName));
        }
        sqlToUse = GrouperClientUtils.removeEnd(sqlToUse, ", ");


        if (primaryKey != null){
          sqlToUse += " where " + GcPersistableHelper.columnName(primaryKey) + " = ? ";
          bindVarstoUse.add(primaryKey.get(t));
        } else if (compoundPrimaryKeys.size() > 0){

          sqlToUse += " where ";

          // Get all of the fields that are involved in the compound keys and build the sql and get bind variables.
          for (Field compoundPrimaryKey : compoundPrimaryKeys){
            Object fieldValue = null;
            try {
              fieldValue = compoundPrimaryKey.get(t);
            } catch (Exception e) {
              throw new RuntimeException(e);
            } 
            sqlToUse += GcPersistableHelper.columnName(compoundPrimaryKey) + " = ? and ";
            bindVarstoUse.add(fieldValue);
          }
          sqlToUse = sqlToUse.substring(0, sqlToUse.length() - 4);
        }
      } else {
        
        // Else insert.
        sqlToUse =  " insert into " + GcPersistableHelper.tableName(t.getClass()) + " ( ";
        String bindVarString = "values (";
        for (String columnName : columnNamesAndValues.keySet()){
          sqlToUse +=  columnName + "," ;
          bindVarString += "?,";
          bindVarstoUse.add(columnNamesAndValues.get(columnName));
        }

        // Get a primary key from the sequence if it is not manually assigned.
        if (primaryKey != null && !GcPersistableHelper.primaryKeyManuallyAssigned(primaryKey) && !GcPersistableHelper.findPersistableClassAnnotation(t.getClass()).hasNoPrimaryKey()){

          sqlToUse += GcPersistableHelper.columnName(primaryKey);
          sqlToUse += ") ";

          primaryKeyValue = new GcDbAccess()
              .sql(" select " + GcPersistableHelper.primaryKeySequenceName(primaryKey) + ".nextval from dual")
              .select(primaryKey.getType());

          bindVarstoUse.add(primaryKeyValue);
          bindVarString += "?) ";

        } else {
          sqlToUse = GrouperClientUtils.removeEnd(sqlToUse, ",") + ") ";
          bindVarString = GrouperClientUtils.removeEnd(bindVarString, ",") + ") ";
        }


        sqlToUse += bindVarString;
      }

      // Execute the insert or update.
      this.sql(sqlToUse);
      this.bindVars(bindVarstoUse);
      this.executeSql();

      // Set the primary key if it was an insert and we grabbed a new one.
      if (primaryKeyValue != null){
        boundDataConversion.setFieldValue(t, primaryKey, primaryKeyValue);
      }

    } catch (Exception e){
      throw new RuntimeException(e);
    }
  }


  /**
   * <pre>Store the given objects to the database in a batch - 
   * the objects should have appropriate annotations from the PersistableX annotations.
   * You cannot have both inserts and updates in the list of objects to store; they MUST all have the 
   * same action (insert or update) being taken against them as jdbc statements supoprt mutliple
   * sqls in a batch but do not support bind variables when using this capability.</pre>
   * @param <T> is the type to store.
   * @see GcPersistableClass - this annotation must be placed at the class level.
   * @see GcPersistableField these annotations may be placed at the method level depending on your needs.
   * @param objects is the list of objects to store to the database.
   * @param batchSize is the size of the batch to insert or update in.
   */
  public <T> void storeBatchToDatabase(final List<T> objects, final int batchSize){
    storeBatchToDatabase(objects, batchSize, false);
  }

  /**
   * <pre>Store the given objects to the database in a batch - 
   * the objects should have appropriate annotations from the PersistableX annotations.
   * You cannot have both inserts and updates in the list of objects to store; they MUST all have the 
   * same action (insert or update) being taken against them as jdbc statements supoprt mutliple
   * sqls in a batch but do not support bind variables when using this capability.</pre>
   * @see GcPersistableClass - this annotation must be placed at the class level.
   * @see GcPersistableField these annotations may be placed at the method level depending on your needs.
   * @param <T> is the type being stored.
   * @param objects is the list of objects to store to the database.
   * @param batchSize is the size of the batch to insert or update in.
   * @param omitPrimaryKeyPopulation if you DON'T need primary keys populated into your objects, you can set this and save some query time since
   * we will just set the primary key population as "some_sequence.nextval" instead of selecting it manually before storing the object.
   */
  public <T> void storeBatchToDatabase(final List<T> objects, final int batchSize, final boolean omitPrimaryKeyPopulation){

    if (objects == null || objects.size() == 0){
      return;
    }

    final List<T> objectsToStore = new ArrayList<T>();
    final List<T> objectsToReturn = new ArrayList<T>();

    this.callbackTransaction(new GcTransactionCallback<Boolean>() {

      @Override
      public Boolean callback(GcDbAccess dbAccessForStorage) {

        for (int i=0; i < objects.size(); i++){

          // Add it.
          objectsToStore.add(objects.get(i));

          // If we have one batch or are at the end, store it.
          if (objectsToStore.size() >= batchSize || i == objects.size() -1){
            dbAccessForStorage.storeBatchToDatabase(objectsToStore, omitPrimaryKeyPopulation);
            objectsToReturn.addAll(objectsToStore);
            objectsToStore.clear();
          }
        }

        return null;
      }
    });

    int existingLength = objects.size();
    objects.clear();
    objects.addAll(objectsToReturn);
    if (objects.size() != existingLength){
      throw new RuntimeException("There should have been " + existingLength + " objects returned but there are only " + objects.size() + "!");
    }
  }


  /**
   * <pre>Store the given objects to the database in a batch - 
   * the objects should have appropriate annotations from the PersistableX annotations.
   * You cannot have both inserts and updates in the list of objects to store; they MUST all have the 
   * same action (insert or update) being taken against them as jdbc statements supoprt mutliple
   * sqls in a batch but do not support bind variables when using this capability.</pre>
   * @param <T> is the type being stored.
   * @see GcPersistableClass - this annotation must be placed at the class level.
   * @see GcPersistableField these annotations may be placed at the method level depending on your needs.
   * @param objects is the list of objects to store to the database.
   */
  public <T> void storeBatchToDatabase(List<T> objects){
    storeBatchToDatabase(objects, false);
  }


  /**
   * <pre>Store the given objects to the database in a batch - 
   * the objects should have appropriate annotations from the PersistableX annotations.
   * You cannot have both inserts and updates in the list of objects to store; they MUST all have the 
   * same action (insert or update) being taken against them as jdbc statements supoprt mutliple
   * sqls in a batch but do not support bind variables when using this capability.</pre>
   * @param <T> is the type being stored.
   * @see GcPersistableClass - this annotation must be placed at the class level.
   * @see GcPersistableField these annotations may be placed at the method level depending on your needs.
   * @param objects is the list of objects to store to the database.
   * @param omitPrimaryKeyPopulation if you DON'T need primary keys populated into your objects, you can set this and save some query time since
   * we will just set the primary key population as "some_sequence.nextval" instead of selecting it manually before storing the object.
   */
  public <T> void storeBatchToDatabase(List<T> objects, boolean omitPrimaryKeyPopulation){

    // No data nothing to do.
    if (objects == null || objects.size() == 0){
      return;
    }

    // We only want to formulate insert or update sql one time.
    String insertSql = null;
    String updateSql = null;
    boolean updateSqlInitialized = false;
    boolean insertSqlInitialized = false;

    if (!GrouperClientUtils.isBlank(this.sql)){
      throw new RuntimeException("Cannot use both sql and set objects to store.");
    }

    // Get the primary key or primary keys.
    Field primaryKey = GcPersistableHelper.primaryKeyField(objects.get(0).getClass());

    // Get any compound primary keys
    List<Field> compoundPrimaryKeys =  GcPersistableHelper.compoundPrimaryKeyFields(objects.get(0).getClass());

    // Get a list of all fields in the class.
    List<Field> allFields = GcPersistableHelper.heirarchicalFields(objects.get(0).getClass());
    
    // Create a map indicating which fields are to be included as bind variables.
    Map<Field, Boolean> fieldAndIncludeStatuses = new HashMap<Field, Boolean>();
    
    // Get field and the status of inclusion.
    for (Field field : allFields){
      field.setAccessible(true);
      // We are putting everything in here except the primary key because we may have to go out and get a primary key shortly
      // unless the primary key is manually assigned, in which case it can go in here.
      if ((primaryKey == null && GcPersistableHelper.isPersist(field, objects.get(0).getClass())) || ( GcPersistableHelper.isPersist(field, objects.get(0).getClass()) && (GcPersistableHelper.primaryKeyManuallyAssigned(primaryKey) || !GcPersistableHelper.isPrimaryKey(field)))){
        fieldAndIncludeStatuses.put(field, true);
      } else {
        fieldAndIncludeStatuses.put(field, false);
      }
    }
    
    try{


      // List of lists of bind variables.
      List<List<Object>> listsOfBindVars = new ArrayList<List<Object>>();

      // Store the primary keys back to the object after we save successfully.
      Map<Integer, Object> indexOfObjectAndPrimaryKeyToSet = new HashMap<Integer, Object>();
      int objectIndex = 0;

      for (Object object : objects){
        
        Map<String, Object> columnNamesAndValues = new HashMap<String, Object>();
                
        // Get column names and values
        for (Field field : allFields){
          if (fieldAndIncludeStatuses.get(field)){
            columnNamesAndValues.put(GcPersistableHelper.columnName(field), field.get(object));
          }
        }
        
        // The bind vars for the given object.
        List<Object> bindVarstoUse = new ArrayList<Object>();

        // Update if we are already saved.
        if (isPreviouslyPersisted(object)){

          // Create the sql.
          if (!updateSqlInitialized){
            updateSql =  " update " + GcPersistableHelper.tableName(object.getClass()) + " set ";
            for (String columnName : columnNamesAndValues.keySet()){
              updateSql += " " + columnName + " = ?, " ;
            }
            updateSql = GrouperClientUtils.removeEnd(updateSql, ", ");
          }

          // Populate the bind vars.
          for (String columnName : columnNamesAndValues.keySet()){
            bindVarstoUse.add(columnNamesAndValues.get(columnName));
          }

          // If there is a primary key add that statement.
          if (primaryKey != null){
            if (!updateSqlInitialized){
              updateSql += " where " + GcPersistableHelper.columnName(primaryKey) + " = ? ";
            }
            bindVarstoUse.add(primaryKey.get(object));
          } else if (compoundPrimaryKeys.size() > 0){

            // There are multiple primary keys.
            if (!updateSqlInitialized){
              updateSql += " where ";
            }

            // Get all of the fields that are involved in the compound keys and build the sql and get bind variables.
            for (Field compoundPrimaryKey : compoundPrimaryKeys){
              Object fieldValue = null;
              try {
                fieldValue = compoundPrimaryKey.get(object);
                bindVarstoUse.add(fieldValue);
              } catch (Exception e) {
                throw new RuntimeException(e);
              } 
              if (!updateSqlInitialized){
                updateSql += GcPersistableHelper.columnName(compoundPrimaryKey) + " = ? and ";
              }
            }
            if (!updateSqlInitialized){
              updateSql = updateSql.substring(0, updateSql.length() - 4);
            }
          }

          // Store the fact that we made the sql and store the bind vars and sql.
          updateSqlInitialized = true;
          listsOfBindVars.add(bindVarstoUse);

        } else {

          // Else insert.
          String bindVarString = "";
          if (!insertSqlInitialized){
            insertSql =  " insert into " + GcPersistableHelper.tableName(object.getClass()) + " ( ";
            bindVarString = "values (";
            for (String columnName : columnNamesAndValues.keySet()){
              insertSql +=  columnName + "," ;
              bindVarString += "?,";
            }
          }

          for (String columnName : columnNamesAndValues.keySet()){
            bindVarstoUse.add(columnNamesAndValues.get(columnName));
          }

          // Get a primary key from the sequence if it is not manually assigned.
          if (primaryKey != null && !GcPersistableHelper.primaryKeyManuallyAssigned(primaryKey) && !GcPersistableHelper.findPersistableClassAnnotation(object.getClass()).hasNoPrimaryKey()){

            // Make the sql.
            if (!insertSqlInitialized){
              insertSql += GcPersistableHelper.columnName(primaryKey);
              insertSql += ") ";
              if (!omitPrimaryKeyPopulation){
                bindVarString += "?) ";
              } else {
                bindVarString += GcPersistableHelper.primaryKeySequenceName(primaryKey) + ".nextval) ";
              }
            }

            // Get the primary key.
            if (!omitPrimaryKeyPopulation){
              Object primaryKeyValue = new GcDbAccess()
                  .sql(" select " + GcPersistableHelper.primaryKeySequenceName(primaryKey) + ".nextval from dual")
                  .select(primaryKey.getType().getClass());
              bindVarstoUse.add(primaryKeyValue);
              indexOfObjectAndPrimaryKeyToSet.put(objectIndex, primaryKeyValue);
            }

          } else {
            if (!insertSqlInitialized){
              insertSql = GrouperClientUtils.removeEnd(insertSql, ",") + ") ";
              bindVarString = GrouperClientUtils.removeEnd(bindVarString, ",") + ") ";
            }
          }

          if (!insertSqlInitialized){
            insertSql += bindVarString;
          }

          // Store the fact that we made the sql and store the bind vars and sql.
          insertSqlInitialized = true;
          listsOfBindVars.add(bindVarstoUse);
        }

        objectIndex++;
      }


      // See which sql we need to send it.
      if (updateSql != null && insertSql != null){
        throw new RuntimeException("It is not possible to mix updates and inserts in one batch; Statement supports it but not with bind variables so we do not support it.");
      } else if (updateSql == null && insertSql == null){
        throw new RuntimeException("No sql was created!");
      } 


      // Execute the sql.
      this.batchBindVars(listsOfBindVars);
      this.sql(updateSql != null ? updateSql : insertSql);
      this.executeBatchSql();
      this.sql(null);
      this.batchBindVars(null);


      // Set the primary keys if there were inserts and we got new ones.
      for (Integer objectIndexInList : indexOfObjectAndPrimaryKeyToSet.keySet()){
        boundDataConversion.setFieldValue(objects.get(objectIndexInList), primaryKey, indexOfObjectAndPrimaryKeyToSet.get(objectIndexInList));
      }

    } catch (Exception e){
      throw new RuntimeException(e);
    }
  }


  /**
   * <pre>For each row of a given resultset, hydrate an object and pass it to the callback.</pre>
   * @param <T>
   * @param clazz is the type of thing passed to the entity callback.
   * @param entityCallback is the callback object that receives this dbAccess with a session set up. 
   */
  public <T> void callbackEntity(Class<T> clazz, GcEntityCallback<T> entityCallback){
    selectList(clazz, entityCallback);
  }




  /**
   * <pre>Use a transaction for all calls that happen within this callback. Upon success with no exceptions thrown,
   * commit is called automatically. Upon failure, rollback it called. You may also call dbAccess.setTransactionEnd()
   * within the callback block.</pre>
   * @param <T>  is the type of thing being returned.
   * @param transactionCallback is the callback object that receives this dbAccess with a session set up. 
   * @return the thing that you want to return.
   */
  public <T> T callbackTransaction(GcTransactionCallback<T> transactionCallback){
    
    ConnectionBean connectionBean = null;
    
    try{

      connectionBean = connection(true, true, this.connectionName);
      
      // Make a new connection.
      this.connection = connectionBean.getConnection();

      // Execute sub logic.
      T t = transactionCallback.callback(this);

      ConnectionBean.transactionEnd(connectionBean, GcTransactionEnd.commit, true, true, true);
      
      return t;

    } catch (Exception e){
      ConnectionBean.transactionEnd(connectionBean, GcTransactionEnd.rollback, true, true, true);
      throw new RuntimeException(e);
    } finally {
      ConnectionBean.closeIfStarted(connectionBean);
    }
    
  }

  /**
   * Select a map of something from the database - set sql() before calling - this will return a map with column name and column value - this should only select one row from the database.
   * @param keyClass is the class of the key.
   * @param valueClass is the class of the value.
   * @param <K> 
   * @param <V> 
   * @return the map or null if nothing is found..
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <K, V> Map<K,V>  selectMap(Class<K> keyClass, Class<V> valueClass){

    List<Map> list = selectList(Map.class);
    if (list.size() == 0){
      return null;
    }

    if (list.size() > 1){
      throw new RuntimeException("Only one object expected but " + list.size() + " were returned for sql " + this.sql);
    }

    // If it is  a map with a string key, we'll be nice and put it into a case ignore map to make it easier on people.
    if (keyClass.equals(String.class)){
      Map mapToReturn = new GcCaseIgnoreHashMap();
      for (Object key : list.get(0).keySet()){
        mapToReturn.put(String.valueOf(key), boundDataConversion.getFieldValue(valueClass, list.get(0).get(key)));
      }
      return mapToReturn;
    }

    // Else just make sure that the data conversion from the database happens.
    Map mapToReturn = new HashMap<K, V>();
    for (Object key : list.get(0).keySet()){
      mapToReturn.put(key, boundDataConversion.getFieldValue(valueClass, list.get(0).get(key)));
    }
    return mapToReturn;
  }




  /**
   * Select a map of two column values from the database - set sql() before calling - the first column in the sql will be used for  the map keys and the second will be used for the map values.
   * @param keyClass is the class of the key.
   * @param valueClass is the class of the value.
   * @param <K> 
   * @param <V> 
   * @return the map or null if nothing is found..
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <K, V> Map<K,V>  selectMapMultipleRows(Class<K> keyClass, Class<V> valueClass){

    List<Map> list = selectList(Map.class);
    if (list.size() == 0){
      return null;
    }

    Iterator columnNames = list.get(0).keySet().iterator();
    Object keyName = columnNames.next();
    Object valueName = columnNames.next();


    // Else just make sure that the data conversion from the database happens.
    Map<K, V> mapToReturn = new HashMap<K, V>();
    for (Map theMap : list){
      mapToReturn.put((K)theMap.get(keyName), boundDataConversion.getFieldValue(valueClass, theMap.get(valueName)));
    }
    return mapToReturn;
  }




  /**
   * Select a map of rows from the database with column name as key and valueClass as value (should be Object if types differ)  from the database - set sql() before calling
   * Example: select first_name, last_name, middle_name from person where rownum < 3:
   * 
   * List(0)
   * Map key      Map value
   * first_name   Fred
   * last_name    Jones
   * middle_name  Percival
   * List(1)
   * Map key      Map value
   * first_name   Jeanette
   * last_name    Shawna
   * middle_name  Percival
   * </pre>
   * 
   * @return the map or null if nothing is found..
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<GcCaseIgnoreHashMap>  selectListMap(){

    List<Map> list = selectList(Map.class);

    // Be nice and put it into a case ignore map to make it easier on people.
    List<GcCaseIgnoreHashMap> newList = new ArrayList<GcCaseIgnoreHashMap>();
    for (Map map : list){
      GcCaseIgnoreHashMap mapToReturn= new GcCaseIgnoreHashMap();
      mapToReturn.putAll(map);
      newList.add(mapToReturn);
    }

    return newList;
  }


  /**
   * <pre>Select a map of key : column name and value : column value from the database - set sql() before calling.
   * Example: select first_name, last_name, middle_name from person:
   * Map key      Map value
   * first_name   Fred
   * last_name    Jones
   * middle_name  Percival
   * </pre>
   * @return the map or null if nothing is found..
   */
  public GcCaseIgnoreHashMap selectMapMultipleColumnsOneRow(){

    List<GcCaseIgnoreHashMap> caseIgnoreHashMaps = selectListMap();

    if (caseIgnoreHashMaps.size() > 1){
      throw new RuntimeException("More than one row was returned for query " + this.sql);
    }
    if (caseIgnoreHashMaps.size() == 1){
      return caseIgnoreHashMaps.get(0);
    }
    return null;
  }



  /**
   * Select something from the database - either set sql() before calling or primaryKey() 
   * @param <T> is the type of object that will be returned.
   * @param clazz  is the type of object that will be returned.
   * @return anything.
   */
  @SuppressWarnings("unchecked")
  public <T>T  select (Class<T> clazz){

    // See if we are caching and we have it in cache.
    if (this.cacheMinutes != null){
      Object cachedObject = this.selectFromQueryCache(false, clazz);
      if (cachedObject != null){
        return (T)cachedObject;
      }
    }

    List<T> list = selectList(clazz, true);
    if (list.size() == 0){
      return null;
    }

    if (list.size() > 1){
      throw new RuntimeException("Only one object expected but " + list.size() + " were returned for sql " + this.sql);
    }

    // See if we are caching and store it in cache.
    if (this.cacheMinutes != null){
      this.populateQueryCache(clazz, list.get(0), false);
    }

    return list.get(0);
  }


  /**
   * Select something from the database - either set sql() before calling or primaryKey(...) 
   * @param <T> is the type of object that will be returned.
   * @param clazz  is the type of object that will be returned.
   * @return anything.
   */
  public <T> List<T> selectList (final Class<T> clazz){
    return selectList(clazz, false);
  }
  
  

  /**
   * Select something from the database - either set sql() before calling or primaryKey(...) 
   * @param <T> is the type of object that will be returned.
   * @param clazz  is the type of object that will be returned.
   * @param calledFromSelect is whether the calling method is select, just for caching purposes.
   * @return anything.
   */
  @SuppressWarnings("unchecked")
  private <T> List<T> selectList (final Class<T> clazz, boolean calledFromSelect){
    // See if we are caching and we have it in cache if we are not being called from select.
    if (!calledFromSelect){
      if (this.cacheMinutes != null){
        Object cachedObject = this.selectFromQueryCache(true, clazz);
        if (cachedObject != null){
          return (List<T>)cachedObject;
        }
      }
    }


    List<T> resultList = selectList(clazz, null);

    if (!calledFromSelect){
      // See if we are caching and store it in cache.
      if (this.cacheMinutes != null){
        this.populateQueryCache(clazz, resultList, true);
      }
    }

    return resultList;
  }


  /**
   * Select something from the database - either set sql() before calling or primaryKey(...) 
   * @param <T> is the type of object that will be returned.
   * @param clazz  is the type of object that will be returned.
   * @param entityCallback is a callback made for each row of data hydrated to an entity, may be null if actually returning the list.
   * @return anything.
   */
  private  <T> List<T> selectList (final Class<T> clazz, final GcEntityCallback<T> entityCallback){

    // Can't select by primary key and sql at the same time.
    if ((this.primaryKeys != null && (this.sql != null || this.example != null))
        || (this.sql != null && (this.primaryKeys != null || this.example != null)) 
        || (this.example != null && (this.primaryKeys != null || this.sql != null))){
      throw new RuntimeException("Set sql(), primaryKey(), or example() but not more than one! primaryKey() will formulate sql.");
    }


    // Get a list of the columns that we are selecting.
    List<String> columnNamesList = new ArrayList<String>();
    for (Field field : GcPersistableHelper.heirarchicalFields(clazz)){
      if (GcPersistableHelper.isSelect(field, clazz)){
        String columnName = GcPersistableHelper.columnName(field);
        columnNamesList.add(columnName);
      }
    }
    String columnNames = GrouperClientUtils.join(columnNamesList.iterator(), ",");


    // If we have a primary key or a list of primary keys, select by them.
    if (this.primaryKeys != null){
      if (this.bindVars != null){
        throw new RuntimeException("Set bindVars() or primaryKey() but not both! primaryKey() will formulate sql.");
      }

      // Get the primary key field.
      Field primaryKeyField = GcPersistableHelper.primaryKeyField(clazz);

      String theSql = " select " + columnNames + " from " + GcPersistableHelper.tableName(clazz) + " where " +   GcPersistableHelper.columnName(primaryKeyField);
      if (this.primaryKeys.size() == 1){
        theSql += " = ? ";
        this.bindVars(this.primaryKeys.get(0));
      } else if (this.primaryKeys.size() > 1) {
        theSql += " in (";
        for (int i = 0; i < this.primaryKeys.size(); i++){
          theSql += "?,";
        }
        theSql = GrouperClientUtils.removeEnd(theSql, ",") + ")";
        this.bindVars(this.primaryKeys);
      } 
      this.sql(theSql);

      // They used no sql and no primary key - that means that we are selecting everything from the table.
    } else if (this.sql == null && this.example == null){
      this.sql(" select " + columnNames + " from " + GcPersistableHelper.tableName(clazz));
    } else if (this.example != null){

      // Make the sql and bind variables.
      String theSql = " select * from " + GcPersistableHelper.tableName(clazz) + " where ";
      String whereClauseToUse = "";
      List<Object> bindVarstoUse = new ArrayList<Object>();

      // We are selecting by example, get all values from the example object and put them into the where clause.
      for (Field field: GcPersistableHelper.heirarchicalFields(clazz)){
        field.setAccessible(true);
        try{
          if (GcPersistableHelper.isSelect(field, clazz) && !GcPersistableHelper.isPrimaryKey(field)){

            // Get the value of the field.
            Object fieldValue = field.get(this.example);

            // See if we are omitting null values from the comparison.
            if (this.omitNullValuesForExample && fieldValue == null){
              continue;
            }

            // All strings get wrapped in to_char for comparison by example - this allows us to compare on clobs.
            String columnName = "";
            if (field.getType().equals(String.class)){
              columnName = "to_char(" + GcPersistableHelper.columnName(field) + ")";
              if (fieldValue != null){
                bindVarstoUse.add(fieldValue);
              }
            } else if (field.getType().equals(Date.class)){
              columnName = "to_char(" + GcPersistableHelper.columnName(field) + ", 'MM/DD/YYYY HH24:MI:SS')";
              if (fieldValue != null){
                bindVarstoUse.add(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format((Date)fieldValue));
              }
            } else {
              columnName =  GcPersistableHelper.columnName(field);
              if (fieldValue != null){
                bindVarstoUse.add(fieldValue);
              }
            }
            String bindOrEquals = (fieldValue == null ? " is null and " : " = ? and ");
            whereClauseToUse += columnName + bindOrEquals;

          }
        } catch (Exception e){
          throw new RuntimeException("Issues encountered trying to read field " + field.getName() + " in class " + this.example.getClass(), e);
        }
      }
      whereClauseToUse = GrouperClientUtils.removeEnd(whereClauseToUse, "and ");

      theSql += whereClauseToUse;
      this.sql(theSql);
      this.bindVars(bindVarstoUse);
    } 


    // Callback on the resultset and create the list of objects, attempting
    // to assign to class fields if T is a PersistableBase, else assign directly to T.
    Long startTime = System.nanoTime();
    String sqlToRecord = this.sql;
    List<T> list = this.callbackResultSet(new GcResultSetCallback<List<T>>() {

      @Override
      public List<T> callback(ResultSet resultSet) throws Exception {
        List<T> theList = new ArrayList<T>();

        // If we are selecting abig list we don't want to have to check every field on every object
        // to see if we have a value in the resultSet, we just want to do it once, so store that here.
        Map<String, Boolean> fieldIsIncludedInResults = new HashMap<String, Boolean>();

        while (resultSet.next()){

          // This is either an entity callback or we are adding stuff to a list.
          if (entityCallback != null){
            T t = addObjectToList(clazz, fieldIsIncludedInResults, resultSet, null);
            boolean keepScrolling = entityCallback.callback(t);
            if (!keepScrolling){
              break;
            }
          } else {
            addObjectToList(clazz, fieldIsIncludedInResults, resultSet, theList);
          }
        }

        return theList;
      }
    });
    this.addQueryToQueriesAndMillis(sqlToRecord, startTime);

    return list;
  }

  /**
   * returned from connection call
   */
  public static class ConnectionBean {
    
    /**
     * if we are in a transaction
     */
    private boolean inTransaction;
    
    /**
     * if we are in a transaction
     * @return the inTransaction
     */
    public boolean isInTransaction() {
      return this.inTransaction;
    }
    
    /**
     * if we are in a transaction
     * @param inTransaction1 the inTransaction to set
     */
    public void setInTransaction(boolean inTransaction1) {
      this.inTransaction = inTransaction1;
    }

    /**
     * connection
     */
    private Connection connection;
    
    /**
     * @return the connection
     */
    public Connection getConnection() {
      return this.connection;
    }

    /**
     * @param connection1 the connection to set
     */
    public void setConnection(Connection connection1) {
      this.connection = connection1;
    }

    /**
     * if a transaction was started
     */
    private boolean transactionStarted;
    
    /**
     * if a transaction was started
     * @return the transactionStarted
     */
    public boolean isTransactionStarted() {
      return this.transactionStarted;
    }
    
    /**
     * if a transaction was started
     * @param transactionStarted1 the transactionStarted to set
     */
    public void setTransactionStarted(boolean transactionStarted1) {
      this.transactionStarted = transactionStarted1;
    }

    /**
     * if the connection was started or reused from threadlocal
     */
    private boolean connectionStarted;
    
    /**
     * if the connection was started or reused from threadlocal
     * @return the connectionStarted
     */
    public boolean isConnectionStarted() {
      return this.connectionStarted;
    }
    
    /**
     * @param connectionStarted1 the connectionStarted to set
     */
    public void setConnectionStarted(boolean connectionStarted1) {
      this.connectionStarted = connectionStarted1;
    }

    /**
     * end a transaction
     * @param connectionBean
     * @param transactionEnd
     * @param endOnlyIfStarted 
     * only a connection and just end it with commit...
     * @param errorIfNoTransaction 
     * @param endTransaction 
     */
    public static void transactionEnd(ConnectionBean connectionBean, 
        GcTransactionEnd transactionEnd, boolean endOnlyIfStarted, 
        boolean errorIfNoTransaction, boolean endTransaction) {
      
      if (connectionBean == null) {
        return;
      }

      if (!connectionBean.isInTransaction()) {
        if (errorIfNoTransaction) {
          throw new RuntimeException("Cannot end a transaction when not in a transaction!");
        }
        return;
      }
      
      if (endOnlyIfStarted && !connectionBean.isTransactionStarted()) {
        return;
      }
      if (endTransaction) {
        transactionThreadLocal.remove();
      }
      try {
        switch (transactionEnd) {
          case commit:
            connectionBean.connection.commit();
            break;
  
          case rollback:
            connectionBean.connection.rollback();
            
            break;
          default:
            throw new RuntimeException("Not expecting: " + transactionEnd);
        }
      } catch (SQLException sqle) {
        throw new RuntimeException("Error: " + transactionEnd + ", " + endOnlyIfStarted);
      }

      if (endTransaction) {
        try {
          connectionBean.connection.setAutoCommit(true);
        } catch (SQLException sqle) {
          throw new RuntimeException(sqle);
        }
      }

    }
    
    
    /**
     * close the connection if started
     * @param connectionBean 
     */
    public static void closeIfStarted(ConnectionBean connectionBean) {
      
      ConnectionBean.transactionEnd(connectionBean, GcTransactionEnd.rollback, true, false, true);

      if (connectionBean != null && connectionBean.isConnectionStarted()) {
        
        connectionThreadLocal.remove();
        GrouperClientUtils.closeQuietly(connectionBean.getConnection());
      }
    }
    
  }

  
  
  /**
   * keep connection in thread local, based on connection name
   */
  private static ThreadLocal<Map<String,Connection>> connectionThreadLocal = new ThreadLocal<Map<String,Connection>>();

  /**
   * if in transaction, true means readwrite, false means readonly
   */
  private static ThreadLocal<Boolean> transactionThreadLocal = new ThreadLocal<Boolean>();
  
  /**
   * get a connection to the oracle DB
   * @param needsTransaction 
   * @param startIfNotStarted generally you start if not started
   * @param connectionName name of connection in properties file
   * @return a connectionbean which wraps the connection never return null
   */
  private static ConnectionBean connection(boolean needsTransaction, boolean startIfNotStarted, String connectionName) {

    if (GrouperClientUtils.isBlank(connectionName)) {
      connectionName = GrouperClientUtils.defaultIfBlank(connectionName, GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.jdbc.defaultName"));
    }
    
    ConnectionBean connectionBean = new ConnectionBean();
    
    Map<String,Connection> connectionMapByName = connectionThreadLocal.get();

    //make sure this gets set once and correctly
    if (connectionMapByName == null) {
      synchronized (GcDbAccess.class) {
        connectionMapByName = connectionThreadLocal.get();
        if (connectionMapByName == null) {
          connectionMapByName = new HashMap<String, Connection>();
          connectionThreadLocal.set(connectionMapByName);
        }
      }
    }
    
    Connection connection = connectionMapByName.get(connectionName);

    //if no connection there and not starting if not started, just return
    if (connection == null && !startIfNotStarted) {
      return connectionBean;
    }

    //try to get it from threadlocal
    Boolean transaction = transactionThreadLocal.get();

    connectionBean.setInTransaction(needsTransaction || transaction != null);
    
    if (needsTransaction) {
      if (transaction != null) {
        connectionBean.setTransactionStarted(false);
      } else {

        transactionThreadLocal.set(true);
        
        connectionBean.setTransactionStarted(true);
      }
    }
    
    if (connection != null) {
      connectionBean.setConnectionStarted(false);
      connectionBean.setConnection(connection);

      //init to this
      try {
        if (connectionBean.isTransactionStarted()) {
          connection.setAutoCommit(false);
        }
      } catch (SQLException sqle) {
        throw new RuntimeException(sqle);
      }
      
      return connectionBean;
    }

    if (transaction != null) {
      throw new RuntimeException("How can you have a transaction without a connection???");
    }
    connectionBean.setConnectionStarted(true);
    
    String url = null;
    
    try {
      String defaultName = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.jdbc.defaultName");
      String driver = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.jdbc." + defaultName + ".driver");
      Class.forName(driver);
  
      url = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.jdbc." + defaultName + ".url");
      String user = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.jdbc." + defaultName + ".user");
      String pass = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.jdbc." + defaultName + ".pass");
      
      connection = DriverManager.getConnection(url, user, pass);

      connectionMapByName.put(connectionName, connection);
      connectionBean.setConnection(connection);

      if (connectionBean.isTransactionStarted()) {
        connection.setAutoCommit(false);
      } else {
        //init to this
        connection.setAutoCommit(true);
      }

      return connectionBean;

    } catch (Exception e) {
      connectionThreadLocal.remove();
      transactionThreadLocal.remove();
      throw new RuntimeException("Error connecting to: " + url, e);
    }
  }





  /**
   * Callback to get a callableStatement - commit is called if there is no exception thrown, otherwise rollback is called.
   * @param <T> is what you are returning, must be a type but you can return null.
   * @param callableStatementCallback is the callback object.
   * @return whatever you return from the connection callback.
   */
  public  <T> T callbackCallableStatement (GcCallableStatementCallback<T> callableStatementCallback){


    CallableStatement callableStatement = null;

    ConnectionBean connectionBean = null;
    
    try{

      connectionBean = connection(false, true, this.connectionName);
      
      // Make a new connection.
      this.connection = connectionBean.getConnection();
      
      // Create the callable statement.
      callableStatement = this.connection.prepareCall(callableStatementCallback.getQuery());

      // Execute sub logic.
      Long startTime = System.nanoTime();
      T t = callableStatementCallback.callback(callableStatement);

      this.addQueryToQueriesAndMillis(callableStatementCallback.getQuery(), startTime);

      return t;

    } catch (Exception e){
      throw new RuntimeException(e);
    } finally{
      try{
        if (callableStatement != null){
          callableStatement.close();
        }
      } catch (Exception e){
        // Nothing to do here.
      }
      ConnectionBean.closeIfStarted(connectionBean);
    }

  }



  /**
   * Callback to get a preparedStatement - commit is called if there is no exception thrown, otherwise rollback is called.
   * @param <T> is what you are returning, must be a type but you can return null.
   * @param preparedStatementCallback is the callback object.
   * @return whatever you return from the connection callback.
   */
  public  <T> T callbackPreparedStatement (GcPreparedStatementCallback<T> preparedStatementCallback){


    PreparedStatement callableStatement = null;

    ConnectionBean connectionBean = null;
    
    try{

      connectionBean = connection(false, true, this.connectionName);
      
      // Make a new connection.
      this.connection = connectionBean.getConnection();

      // Create the callable statement.
      callableStatement = this.connection.prepareStatement(preparedStatementCallback.getQuery());

      // Execute sub logic.
      Long startTime = System.nanoTime();
      T t = preparedStatementCallback.callback(callableStatement);
      this.addQueryToQueriesAndMillis(preparedStatementCallback.getQuery(), startTime);

      return t;

    } catch (Exception e){
      throw new RuntimeException(e);
    } finally{
      try{
        if (callableStatement != null){
          callableStatement.close();
        }
      } catch (Exception e){
        // Nothing to do here.
      }
      ConnectionBean.closeIfStarted(connectionBean);
    }

  }




  /**
   * Callback to get a connection - commit is called if there is no exception thrown, otherwise rollback is called.
   * @param <T> is what you are returning, must be a type but you can return null.
   * @param connectionCallback is the callback object.
   * @return whatever you return from the connection callback.
   */
  public  <T> T callbackConnection (GcConnectionCallback<T> connectionCallback){

    ConnectionBean connectionBean = null;
    
    try{

      connectionBean = connection(false, true, this.connectionName);
      
      // Make a new connection.
      this.connection = connectionBean.getConnection();

      //dont worry about autocommit

      // Execute sub logic.
      Long startTime = System.nanoTime();
      T t = connectionCallback.callback(this.connection);
      this.addQueryToQueriesAndMillis("Connection callback, SQL unknown", startTime);

      return t;

    } catch (Exception e){
      throw new RuntimeException(e);
    } finally {
      ConnectionBean.closeIfStarted(connectionBean);
    }

  }



  /**
   * Callback a resultSet.
   * @param <T> is the type of object that will be returned.
   * @param resultSetCallback is the object to callback.
   * @return anything return from the callback object.
   */
  public  <T> T callbackResultSet (GcResultSetCallback<T> resultSetCallback){

    // At very least, we have to have sql and a connection.
    if (this.sql == null){
      throw new RuntimeException("You must set sql!");
    }

    PreparedStatement preparedStatement = null;

    ConnectionBean connectionBean = null;
    
    try{

      connectionBean = connection(false, true, this.connectionName);
      
      // Make a new connection.
      this.connection = connectionBean.getConnection();

      // Get the statement object that we are going to use.
      preparedStatement = this.connection.prepareStatement(this.sql);
      String sqltoRecord = this.sql;


      // Set the query timeout if there is one.
      if (this.queryTimeoutSeconds != null){
        preparedStatement.setQueryTimeout(this.queryTimeoutSeconds);
      }

      // Add bind variables if we have them.
      if (this.bindVars != null){
        int i = 1;
        for (Object bindVar : this.bindVars){
          boundDataConversion.addBindVariableToStatement(preparedStatement, bindVar, i);
          i++;
        }
      }

      // Add batch bind variables if we have them.
      if(this.batchBindVars != null){
        for (List<Object> theBindVars : this.batchBindVars){
          int i = 1;
          for (Object bindVar : theBindVars){
            boundDataConversion.addBindVariableToStatement(preparedStatement, bindVar, i);
            i++;
          }
          preparedStatement.addBatch();
        }
      }

      // Internally, we use this without a resultset callback.
      if (resultSetCallback == null){

        // Add batch bind variables if we have them.
        if(this.batchBindVars != null){
          Long startTime = System.nanoTime();
          this.numberOfBatchRowsAffected = preparedStatement.executeBatch();
          this.addQueryToQueriesAndMillis(sqltoRecord, startTime);
          return null;
        } 

        Long startTime = System.nanoTime();
        this.numberOfRowsAffected = preparedStatement.executeUpdate();
        this.addQueryToQueriesAndMillis(sqltoRecord, startTime);
        return null; 
      }

      // Externally, it is used as a callback.
      ResultSet rs = preparedStatement.executeQuery();
      return resultSetCallback.callback(rs);
      
    } catch (Exception e){
      GrouperClientUtils.injectInException(e, "sql: " + this.sql);
      throw new RuntimeException(e);
    } finally {
      if (preparedStatement != null){
        try {
          preparedStatement.close();
        } catch (Exception e){
          throw new RuntimeException(e);
        }
      }
      ConnectionBean.closeIfStarted(connectionBean);
    }
  }


  /**
   * Execute some sql.
   * @return anything return from the callback object.
   */
  public int executeSql(){
    if (this.batchBindVars != null){
      throw new RuntimeException("Use executeBatchSql() with batchBindVars!");
    }

    callbackResultSet(null);

    return this.numberOfRowsAffected;
  }


  /**
   * Execute some sql as a batch.
   * @return anything return from the callback object.
   */
  public int[] executeBatchSql(){
    if (this.bindVars != null){
      throw new RuntimeException("Use batchBindVars with executeBatchSql(), not bindVars!");
    }
    callbackResultSet(null);
    return this.numberOfBatchRowsAffected;
  }


  /**
   * Create the object of type T from the resultSet and add it to the list if the list is not null.
   * @param clazz is the class type to return.
   * @param fieldIsIncludedInResults is a map that allows us to check if the resultset field maps to the object only once for each query.
   * @param resultSet is the row of data.
   * @param theList is the list to add to if not null.
   * @param <T> is the class type to return.
   * @return the object.
   * @throws Exception 
   */
  private <T> T addObjectToList(Class<T> clazz, Map<String, Boolean> fieldIsIncludedInResults, ResultSet resultSet, List<T> theList) throws Exception{
    // We are either setting fields of a class that has at least one persistable annotation.
    if (GcPersistableHelper.hasPersistableAnnotation(clazz)){

      // Make a new instance to assign properties to.
      T t = clazz.newInstance();

      // Check each field of the class for persistability and try to assign if if possible.
      for (Field field : GcPersistableHelper.heirarchicalFields(clazz)){
        if (GcPersistableHelper.isSelect(field, clazz)){
          String columnName = GcPersistableHelper.columnName(field);

          // Make sure that we have the column data.
          Boolean columnInQueryResults = fieldIsIncludedInResults.get(columnName);
          if (columnInQueryResults == null){
            try {
              resultSet.findColumn(columnName);
              fieldIsIncludedInResults.put(columnName, new Boolean(true));
              columnInQueryResults = new Boolean(true);
            } catch (SQLException e){
              fieldIsIncludedInResults.put(columnName, new Boolean(false));
              columnInQueryResults = new Boolean(false);
            }
          }
          if (columnInQueryResults){
            Object value = resultSet.getObject(columnName);
            boundDataConversion.setFieldValue(t, field, value);
          }
        }
      }
      // Add the hydrated object to the list.
      if (theList != null){
        theList.add(t);
      }
      return t;
    } 


    // If someone is selecting a list of Map then we are just going to put the object and column name in the map.
    if (clazz.isAssignableFrom(Map.class)){
      int columnCount = resultSet.getMetaData().getColumnCount();
      Map<Object, Object> results = new LinkedHashMap<Object, Object>();
      for (int columnNumber = 1; columnNumber <= columnCount; columnNumber++){
        results.put(resultSet.getMetaData().getColumnName(columnNumber), resultSet.getObject(columnNumber));
      }
      @SuppressWarnings("unchecked")
      T t = (T)results;
      if (theList != null){
        theList.add(t);
      }
      return t;
    }


    // Or we are just returning a primitive or single object such as Long, etc.
    T t = boundDataConversion.getFieldValue(clazz, resultSet.getObject(1));
    if (theList != null){
      theList.add(t);
    }
    return t;


  }

  /**
   * Cached queries, exposed mostly for testing, you should not need direct access to this.
   * @return the dbQueryCacheMap
   */
  public static Map<MultiKey, GcDbQueryCache> getGcDbQueryCacheMap() {
    return dbQueryCacheMap;
  }


  /**
   * The map containing reports if they have been turned on.
   * @return the queriesAndMillis
   */
  public static Map<String, GcQueryReport> getQueriesAndMillis() {
    return queriesAndMillis;
  }



  /**
   * Select the objects from the query cache.
   * @param isList is whether a list is being selected or not.
   * @param clazz is the type of thing being selected.
   * @return the cached object if it exists or null.
   */
  private Object selectFromQueryCache(boolean isList, Class<?> clazz){
    if (this.cacheMinutes == null){
      return null;
    }
    MultiKey queryKey = queryCacheKey(isList, clazz);
    GcDbQueryCache dbQueryCache = dbQueryCacheMap.get(queryKey);
    if (dbQueryCache == null){
      return null;
    }
    return dbQueryCache.getThingBeingCached();
  }


  /**
   * Set the object(s) to the query cache.
   * @param isList is whether a list is being selected or not.
   * @param clazz is the type of thing being selected.
   * @param thingBeingCached is the object(s) being cached.
   */
  private void populateQueryCache(Class<?> clazz, Object thingBeingCached, boolean isList){
    if (this.cacheMinutes == null){
      return;
    }
    MultiKey queryKey = this.queryCacheKey(isList, clazz);
    dbQueryCacheMap.put(queryKey, new GcDbQueryCache(this.cacheMinutes, thingBeingCached));
  }


  /**
   * A key unique to the current state of this dbaccess.
   * @param isList is whether a list is being selected or not.
   * @param clazz is the type of thing being selected.
   * @return the key.
   */
  private MultiKey queryCacheKey(boolean isList, Class<?> clazz){
    
    List<Object> key = new ArrayList<Object>();
    key.add(this.sql);
    key.add(clazz.getName());
    key.add(isList);
    //why is this in here?
    //key.add(queryTimeoutSeconds);

    if (this.bindVars != null && this.bindVars.size() > 0){
      for (Object bindVar : this.bindVars){
        key.add(bindVar);
      }
    }
    if (this.batchBindVars != null && this.batchBindVars.size() > 0){
      for (List<Object> bindVarList : this.batchBindVars){
        for (Object bindVar : bindVarList){
          key.add(bindVar);
        }
      }
    }
    if (this.primaryKeys != null){
      for (Object primaryKey : this.primaryKeys){
        key.add(primaryKey);
      }
    }
    return new MultiKey(key.toArray());
  }


  /**
   * Clone the existing dbAccess.
   * @return the cloned baccess.
   */
  private GcDbAccess cloneDbAccess(){
    GcDbAccess dbAccess = new GcDbAccess();
    for (Field field : GcDbAccess.class.getDeclaredFields()){
      try {
        field.setAccessible(true);
        field.set(dbAccess, field.get(this));
      } catch (Exception e) {
        throw new RuntimeException("Cannot clone value of field " + field.getName());
      } 
    }
    return dbAccess;
  }

}
