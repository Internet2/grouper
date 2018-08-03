/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperInstaller.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperInstaller.driverShim.HsqlShim;
import edu.internet2.middleware.grouperInstaller.driverShim.MySqlShim;
import edu.internet2.middleware.grouperInstaller.driverShim.OracleShim;
import edu.internet2.middleware.grouperInstaller.driverShim.PostgresShim;
import edu.internet2.middleware.grouperInstaller.driverShim.SqlServerShim;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.logging.Log;


/**
 * the instance can be reused, it doesnt store state except the connection user/pass etc
 */
public class GiDbUtils {

  /**
   * url of db
   */
  private String url;
  
  /**
   * user of db
   */
  private String user;
  
  /**
   * pass of db
   */
  private String pass;
  
  /**
   * construct
   * @param url
   * @param user
   * @param pass
   */
  public GiDbUtils(String url, String user, String pass) {
    super();
    this.url = url;
    this.user = user;
    this.pass = pass;
  }

  /**
   * strings of driver classes that are registered
   */
  private static Set<String> driversRegistered = new HashSet<String>();
  
  
  /**
   * 
   * @param appDir where we can find drivers
   */
  public void registerDriverOnce(String appDir) {
    String driver = convertUrlToDriverClassIfNeeded(this.url, null);
    if (driversRegistered.contains(driver)) {
      return;
    }
    //see if its on classpath
    try {
      Class.forName(driver);
      //already on classpath?  thats good
    } catch (ClassNotFoundException e) {
      
      //we need to find jar and add
      String prefix = null;
      if (this.isHsql()) {
        prefix = "hsql";
      } else if (this.isMysql()) {
        prefix = "mysql";
      } else if (this.isOracle()) {
        prefix = "ojdbc";
      } else if (this.isPostgres()) {
        prefix = "postgres";
      } else if (this.isSQLServer()) {
        prefix = "sqljdbc";
      } else {
        throw new RuntimeException("What kind of database is this???? " + this.url);
      }

      List<File> allFiles = GrouperInstallerUtils.fileListRecursive(new File(appDir));
      File driverJar = null;
      for (File file : allFiles) {
        if (file.getName().endsWith(".jar") && file.getName().startsWith(prefix)) {
          
          //find the latest file with correct prefix and suffix
          if (driverJar == null || driverJar.lastModified() < file.lastModified()) {
            driverJar = file;
          }
        }
      }
      
      if (driverJar == null) {
        System.out.println("Cant find driver jar that starts with '" + prefix + "' and ends with .jar!!! in directory: " + appDir);
        System.exit(1);
      }
      try {
        if (this.isHsql()) {
          HsqlShim.init(driverJar);
          HsqlShim hsqlShim = new HsqlShim();
          DriverManager.registerDriver(hsqlShim);
        } else if (this.isMysql()) {
          MySqlShim.init(driverJar);
          MySqlShim mysqlShim = new MySqlShim();
          DriverManager.registerDriver(mysqlShim);
        } else if (this.isOracle()) {
          OracleShim.init(driverJar);
          OracleShim oracleShim = new OracleShim();
          DriverManager.registerDriver(oracleShim);
        } else if (this.isPostgres()) {
          PostgresShim.init(driverJar);
          PostgresShim postgresShim = new PostgresShim();
          DriverManager.registerDriver(postgresShim);
        } else if (this.isSQLServer()) {
          SqlServerShim.init(driverJar);
          SqlServerShim sqlServerShim = new SqlServerShim();
          DriverManager.registerDriver(sqlServerShim);
        } else {
          throw new RuntimeException("What kind of database is this???? " + this.url);
        }
      } catch (SQLException sqle) {
        throw new RuntimeException("Problem registering driver: " + this.url, sqle);
      }
    }
    driversRegistered.add(driver);
  }
  
  /** 
   * oracle types
   */
  public static enum DbType {

    /** string type */
    STRING {

      /**
       * 
       * @see edu.internet2.middleware.GiDbUtils.util.GcDbUtils.DbType#processResultSet(java.sql.ResultSet, int)
       */
      @Override
      public Object processResultSet(ResultSet resultSet, int indexZeroIndexed) {
        try {
          return resultSet.getString(indexZeroIndexed+1);
        } catch (SQLException e) {
          throw new RuntimeException("Error reading col (zero indexed) " + indexZeroIndexed, e);
        }
      }

      /**
       * @see edu.internet2.middleware.GiDbUtils.util.GcDbUtils.DbType#attachParam(PreparedStatement, Object, int)
       */
      @Override
      public void attachParam(PreparedStatement preparedStatement, Object arg,
          int indexZeroIndexed) {
        String argString = GrouperInstallerUtils.stringValue(arg);
        try {
          preparedStatement.setString(indexZeroIndexed+1, argString);
        } catch (SQLException e) {
          throw new RuntimeException("Error setting param (zero indexed) " + indexZeroIndexed + ": " + argString, e);
        }
      }
    };

    /**
     * get the data
     * @param resultSet
     * @param indexZeroIndexed
     * @return the object or null
     */
    public abstract Object processResultSet(ResultSet resultSet, int indexZeroIndexed);
    
    /**
     * attach a param to a prepared statement
     * @param preparedStatement
     * @param arg
     * @param indexZeroIndexed
     */
    public abstract void attachParam(PreparedStatement preparedStatement, Object arg, int indexZeroIndexed);
    
    /**
     * convert an object arg to a dbtype
     * @param object
     * @return dbtype
     */
    public static DbType fromObject(Object object) {
      
      if (object == null || object instanceof String) {
        return DbType.STRING;
      }
      throw new RuntimeException("Unsupported type: " + GrouperInstallerUtils.className(object) + ", " + object);
    }
    
    /**
     * convert an object arg to a dbtype
     * @param theClass
     * @return dbtype
     */
    public static DbType fromClass(Class<?> theClass) {
      
      if (theClass instanceof Class<?> && String.class.equals(theClass)) {
        return DbType.STRING;
      }
      
      throw new RuntimeException("Unsupported type: " + theClass);
    }
    
  }
  
  /**
   * see if the config file seems to be hsql
   * @return see if hsql
   */
  public boolean isHsql() {
    return isHsql(this.url);
  }

  /**
   * see if the config file seems to be hsql
   * @param connectionUrl url to check against
   * @return see if hsql
   */
  public static boolean isHsql(String connectionUrl) {
    return GrouperInstallerUtils.defaultString(connectionUrl).toLowerCase().contains(":hsqldb:");
  }
  
  /**
   * see if the config file seems to be postgres
   * @return see if postgres
   */
  public boolean isPostgres() {
    return isPostgres(this.url);
  }
  
  /**
   * see if the config file seems to be postgres
   * @param connectionUrl
   * @return see if postgres
   */
  public static boolean isPostgres(String connectionUrl) {
    return GrouperInstallerUtils.defaultString(connectionUrl).toLowerCase().contains(":postgresql:");
  }
  
  /**
   * see if the config file seems to be oracle
   * @return see if oracle
   */
  public boolean isOracle() {
    return isOracle(this.url);
  }
  
  /**
   * see if the config file seems to be oracle
   * @param connectionUrl
   * @return see if oracle
   */
  public static boolean isOracle(String connectionUrl) {
    return GrouperInstallerUtils.defaultString(connectionUrl).toLowerCase().contains(":oracle:");
  }
  
  /**
   * see if the config file seems to be mysql
   * @return see if mysql
   */
  public boolean isMysql() {
    return isMysql(this.url);
  }
  
  /**
   * see if the config file seems to be mysql
   * @param connectionUrl
   * @return see if mysql
   */
  public static boolean isMysql(String connectionUrl) {
    return GrouperInstallerUtils.defaultString(connectionUrl).toLowerCase().contains(":mysql:");
  }
  
  /**
   * see if the config file seems to be sql server
   * @return see if sql server
   */
  public boolean isSQLServer() {
    return isSQLServer(this.url);
  }
  
  /**
   * see if the config file seems to be sql server
   * @param connectionUrl
   * @return see if sql server
   */
  public static boolean isSQLServer(String connectionUrl) {
    return GrouperInstallerUtils.defaultString(connectionUrl).toLowerCase().contains(":sqlserver:");
  }
  

  /**
   * if there is no driver class specified, then try to derive it from the URL
   * @param connectionUrl
   * @param driverClassName
   * @return the driver class
   */
  public static String convertUrlToDriverClassIfNeeded(String connectionUrl, String driverClassName) {
    //default some of the stuff
    if (GrouperInstallerUtils.isBlank(driverClassName)) {
      
      if (isHsql(connectionUrl)) {
        driverClassName = "org.hsqldb.jdbcDriver";
      } else if (isMysql(connectionUrl)) {
        driverClassName = "com.mysql.jdbc.Driver";
      } else if (isOracle(connectionUrl)) {
        driverClassName = "oracle.jdbc.driver.OracleDriver";
      } else if (isPostgres(connectionUrl)) { 
        driverClassName = "org.postgresql.Driver";
      } else if (isSQLServer(connectionUrl)) {
        driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
      } else {
        
        //if this is blank we will figure it out later
        if (!GrouperInstallerUtils.isBlank(connectionUrl)) {
        
          String error = "Cannot determine the driver class from database URL: " + connectionUrl;
          System.err.println(error);
          LOG.error(error);
          return null;
        }
      }
    }
    return driverClassName;

  }

  /**
   * get a connection to the oracle DB
   * @return a connection
   */
  public Connection connection() {
    
    String oracleUrl = null;
    
    try {

      // this should be done with original driver or shim by now
//      String driverClass = convertUrlToDriverClassIfNeeded(this.url, null);
//  
//      GrouperInstallerUtils.forName(driverClass);
      
      Connection conn = DriverManager.getConnection(this.url, this.user, this.pass);
      conn.setAutoCommit(false);
      return conn;
    } catch (Exception e) {
      throw new RuntimeException("Error connecting to: " + oracleUrl, e);
    }
  }

  /**
   * run a query and get rows back
   * @param rowType type of each row returned, e.g. String.class or Object[]
   * @param <T> generic type
   * @param query
   * @param returnColTypes
   * @return the list of objects
   */
  public <T> List<T> listSelect(Class<T> rowType, String query, List<DbType> returnColTypes) {
    return listSelect(rowType, query, returnColTypes, null);
  }

  /**
   * select a value from the db
   * @param <T>
   * @param colType
   * @param query
   * @return the data
   */
  public <T> T select(Class<T> colType, String query) {
    List<T> rows = listSelect(colType, query, GrouperInstallerUtils.toList(DbType.fromClass(colType)));
    
    T data = GrouperInstallerUtils.listPopOne(rows);
    
    return data;
  }
  
  /**
   * run a query and get rows back
   * @param rowType type of each row returned, e.g. String.class or Object[]
   * @param <T> generic type
   * @param query
   * @param returnColTypes
   * @param args preparedstatement arguments
   * @return the list of objects
   */
  @SuppressWarnings("unchecked")
  public <T> List<T> listSelect(Class<T> rowType, String query, List<DbType> returnColTypes, List<Object> args) {
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    List<T> results = new ArrayList<T>();
    
    try {
      
      int returnColTypesLength = GrouperInstallerUtils.length(returnColTypes);
      if (returnColTypesLength==0) {
        throw new RuntimeException("Why is returnColTypesLength == 0???");
      }
      
      connection = this.connection();
      preparedStatement = connection.prepareStatement(query);
      
      //attach arguments
      int argsLength = GrouperInstallerUtils.length(args);

      for (int i=0; i<argsLength;i++) {
        Object arg = args.get(i);
        DbType dbType = DbType.fromObject(arg);
        dbType.attachParam(preparedStatement, arg, i);
      }
      
      resultSet = preparedStatement.executeQuery();

      if (rowType.isArray() != returnColTypesLength>1) {
        throw new RuntimeException("If returnColTypesLength > 1 (" + returnColTypesLength + ") then you must pass in an array as the return type");
      }
      
      while (resultSet.next()) {
        
        //if array of cols
        if (rowType.isArray()) {
        
          Object[] result = new Object[returnColTypesLength];
          results.add((T)result);
          
          for (int i=0; i<returnColTypesLength;i++) {
            result[i] = returnColTypes.get(i).processResultSet(resultSet, i);
          }
        } else {
          //if one col
          results.add((T)returnColTypes.get(0).processResultSet(resultSet, 0));
        }
        
      }
    } catch (Exception e) {
      throw new RuntimeException("Error with query: " + query, e);
    } 
    finally {
      rollbackQuietly(connection);
      GrouperInstallerUtils.closeQuietly(resultSet);
      GrouperInstallerUtils.closeQuietly(preparedStatement);
      GrouperInstallerUtils.closeQuietly(connection);
    }
    return results;

  }
  
  /**
   * rollback quiently
   * @param connection
   */
  public void rollbackQuietly(Connection connection) {
    try {
      if (connection != null) {
        connection.rollback();
      }
    } catch (Exception e) {
      LOG.error("Problem rolling back", e);
      //probably should rethrow but dont want to mess up the other closes...
    }

  }
  
  /**
   * execute a query (insert/update/delete/etc)
   * @param query query to execute
   * @param arg preparedstatement argument
   * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     *         or (2) 0 for SQL statements that return nothing
   */
  public int executeUpdate(String query, Object arg) {
    
    List<Object> args = GrouperInstallerUtils.toList(arg);
    return executeUpdate(query, args);
    
  }
  
  /**
   * execute a query (insert/update/delete/etc)
   * @param query query to execute
   * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
   *         or (2) 0 for SQL statements that return nothing
   */
  public int executeUpdate(String query) {
    
    return executeUpdate(query, null);
    
  }
  
  /**
   * execute a query (insert/update/delete/etc)
   * @param query query to execute
   * @param args preparedstatement arguments
   * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     *         or (2) 0 for SQL statements that return nothing
   */
  public int executeUpdate(String query, List<Object> args) {
    return executeUpdate(query, args, true);
  }
  
  /**
   * check connection to the db
   * @return exception if there is one
   */
  public Exception checkConnection() {
    
    String query = checkConnectionQuery();
    
    try {
      select(String.class, query);
    } catch (Exception e) {
      return e;
    }
    return null;
  }

  /**
   * check connection to the db
   * @return the query to check connection with
   */
  public String checkConnectionQuery() {
    if (this.isHsql()) {
      return "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
    }
    if (this.isMysql()) {
      return "select 1";
    }
    if (this.isOracle()) {
      return "select 1 from dual";
    }
    if (this.isPostgres()) {
      return "select 1";
    }
    if (this.isSQLServer()) {
      return "select 1";
    }
    throw new RuntimeException("Cant find which database type from URL: " + this.url);

  }

  /**
   * execute a query (insert/update/delete/etc)
   * @param query query to execute
   * @param args preparedstatement arguments
   * @param commit if we should commit
   * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     *         or (2) 0 for SQL statements that return nothing
   */
  public int executeUpdate(String query, List<Object> args, boolean commit) {
    Connection connection = null;
    PreparedStatement preparedStatement = null;

    try {
      connection = this.connection();
      preparedStatement = connection.prepareStatement(query);
      
      //attach arguments
      int argsLength = GrouperInstallerUtils.length(args);

      for (int i=0; i<argsLength;i++) {
        Object arg = args.get(i);
        DbType dbType = DbType.fromObject(arg);
        dbType.attachParam(preparedStatement, arg, i);
      }
      
      int result = preparedStatement.executeUpdate();
      
      if (commit) {
        connection.commit();
      }
      
      return result;
      
    } catch (Exception e) {
      rollbackQuietly(connection);
      throw new RuntimeException("Error with query: " + query, e);
    } 
    finally {
      GrouperInstallerUtils.closeQuietly(preparedStatement);
      GrouperInstallerUtils.closeQuietly(connection);
    }

  }

  /**
   * 
   */
  private static Log LOG = GrouperInstallerUtils.retrieveLog(GiDbUtils.class);
}
