/*
 * @author mchyzer $Id: JDBCSourceAdapter2.java,v 1.3 2009-08-12 04:52:21 mchyzer Exp $
 */
package edu.internet2.middleware.subject.provider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.subj.InvalidQueryRuntimeException;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectCheckConfig;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectUtils;

/**
 * jdbc source adapter based on one table with more complex searches
 */
public class JDBCSourceAdapter2 extends JDBCSourceAdapter {

  /** 
   * table or view where each row is a subject
   * <param-name>dbTableOrView</param-name> 
   */
  private String dbTableOrView;

  /** 
   * column which holds the subject id
   * <param-name>subjectIdCol</param-name> 
   */
  private String subjectIdCol;

  /**
   * column which holds the subject name 
   * <param-name>nameCol</param-name> 
   */
  private String nameCol;

  /**
   * col for subject description
   * <param-name>descriptionCol</param-name> 
   */
  private String descriptionCol;

  /**
   * for searches (not by id or identifier), this is the col which holds the search terms, in lower case 
   * <param-name>lowerSearchCol</param-name> 
   */
  private String lowerSearchCol;

  /**
   * search queries will sort by this.  Note it might be overridden by caller, e.g. UI
   * <param-name>defaultSortCol</param-name> 
   */
  private String defaultSortCol;

  /**
   * cols which are used in a findByIdentifier query
   * <param-name>subjectIdentifierCol0</param-name> 
   */
  private Set<String> subjectIdentifierCols = new LinkedHashSet<String>();

  /**
   * cols which are selected in queries
   */
  private Set<String> selectCols = new LinkedHashSet<String>();

  /**
   * map of col to attribute name
   * //<param-name>subjectAttributeCol0</param-name>
   * //<param-name>subjectAttributeName0</param-name>
   */
  private Map<String, String> subjectAttributeColToName = new LinkedHashMap<String, String>();

  /** logger */
  private static Log log = LogFactory.getLog(JDBCSourceAdapter2.class);

  /**
   * 
   */
  public JDBCSourceAdapter2() {
  }

  /**
   * @param id
   * @param name
   */
  public JDBCSourceAdapter2(String id, String name) {
    super(id, name);
  }

  /**
     * @see edu.internet2.middleware.subject.Source#checkConfig()
     */
  public void checkConfig() {

    Properties props = this.getInitParams();
    String error = "problem with sources.xml source id: " + this.getId() + ", ";

    //TODO encapsulate this stuff from the superclass into one method
    //see if has jdbc in provider
    if (this.jdbcConnectionProvider.requiresJdbcConfigInSourcesXml()) {

      String driver = props.getProperty("dbDriver");
      if (StringUtils.isBlank(driver)) {
        System.err.println("Subject API error: " + error + ", driver param is required");
        log.error(error + ", driver param is required");
        return;
      }
      String dbUrl = props.getProperty("dbUrl");
      if (StringUtils.isBlank(dbUrl)) {
        System.err.println("Subject API error: " + error + ", dbUrl param is required");
        log.error(error + ", dbUrl param is required");
        return;
      }
      String dbUser = props.getProperty("dbUser");
      if (StringUtils.isBlank(dbUser)) {
        System.err.println("Subject API error: " + error + ", dbUser param is required");
        log.error(error + ", dbUser param is required");
        return;
      }
      String dbPwd = StringUtils.defaultString(props.getProperty("dbPwd"));
      //      if (StringUtils.isBlank(dbPwd)) {
      //        System.err.println("Subject API error: " + error + ", dbPwd param is required");
      //        log.error(error + ", dbPwd param is required");
      //        return;
      //      }
      dbPwd = Morph.decryptIfFile(dbPwd);

      try {

        Class driverClass = null;
        try {
          driverClass = Class.forName(driver);
        } catch (Exception e) {
          String theError = error
              + "Error finding database driver class: "
              + driver
              + ", perhaps you did not put the database driver jar in the lib/custom dir or lib dir, "
              + "or you have the wrong driver listed";
          System.err.println("Subject API error: " + theError + ": "
              + ExceptionUtils.getFullStackTrace(e));
          log.error(theError, e);
          return;
        }

        //check out P6Spy
        String spyInsert = "";
        //dont load class here
        if (driverClass.getName().equals("com.p6spy.engine.spy.P6SpyDriver")) {
          spyInsert = " and spy.properties";
          if (!SubjectCheckConfig.checkConfig("spy.properties")) {
            return;
          }
          Properties spyProperties = SubjectUtils
              .propertiesFromResourceName("spy.properties");
          driver = spyProperties.getProperty("realdriver");
          try {
            driverClass = SubjectUtils.forName(driver);
          } catch (Exception e) {
            String theError = error
                + "Error finding database driver class from spy.properties: "
                + driver
                + ", perhaps you did not put the database driver jar in the lib/custom dir or lib dir, "
                + "or you have the wrong driver listed";
            System.err.println("Subject API error: " + theError + ": "
                + ExceptionUtils.getFullStackTrace(e));
            log.error(theError, e);
            return;
          }
        }

        //lets make a db connection
        Connection dbConnection = null;
        try {
          dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
          @SuppressWarnings("unused")
          String version = dbConnection.getMetaData().getDatabaseProductVersion();
        } catch (SQLException sqlException) {
          String theError = error
              + "Error connecting to the database with credentials from sources.xml"
              + spyInsert + ", url: " + dbUrl + ", driver: " + driver + ", user: "
              + dbUser;
          System.out.println("Subject API error: " + theError + ", "
              + ExceptionUtils.getFullStackTrace(sqlException));
          log.error(theError, sqlException);
          return;
        } finally {
          SubjectUtils.closeQuietly(dbConnection);
        }

      } catch (Exception e) {
        String theError = error + "Error verifying sources.xml database configuration: ";
        System.err.println("Subject API error: " + theError
            + ExceptionUtils.getFullStackTrace(e));
        log.error(theError, e);
      }

    }

    //TODO encapsulate this in its own method
    {
      //    <param-name>dbTableOrView</param-name>
      String dbTableOrView = props.getProperty("dbTableOrView");
      if (StringUtils.isBlank(dbTableOrView)) {
        String errorDetail = "dbTableOrView is required";
        System.err.println(error + errorDetail);
        log.error(error + errorDetail);
        return;
      }
    }

    {
      //    <param-name>subjectIdCol</param-name>
      String subjectIdCol = props.getProperty("subjectIdCol");
      if (StringUtils.isBlank(subjectIdCol)) {
        String errorDetail = "subjectIdCol is required";
        System.err.println(error + errorDetail);
        log.error(error + errorDetail);
        return;
      }
    }

    {
      //    <param-name>nameCol</param-name>
      String nameCol = props.getProperty("nameCol");
      if (StringUtils.isBlank(nameCol)) {
        String errorDetail = "nameCol is required";
        System.err.println(error + errorDetail);
        log.error(error + errorDetail);
        return;
      }
    }

    {
      //    <param-name>lowerSearchCol</param-name>
      String lowerSearchCol = props.getProperty("lowerSearchCol");
      if (StringUtils.isBlank(lowerSearchCol)) {
        String errorDetail = "lowerSearchCol is required";
        System.err.println(error + errorDetail);
        log.error(error + errorDetail);
        return;
      }
    }

  }

  /**
   * DataSource connection pool setup.
   * @param props 
   * @throws SourceUnavailableException
   */
  @SuppressWarnings("unchecked")
  protected void setupDataSource(Properties props) throws SourceUnavailableException {

    //TODO encapsulate generic connection stuff like this into one method
    String driver = props.getProperty("dbDriver");

    //default is 2
    String maxActiveString = props.getProperty("maxActive");
    Integer maxActive = StringUtils.isBlank(maxActiveString) ? null : Integer
        .parseInt(maxActiveString);

    //default is 2
    String maxIdleString = props.getProperty("maxIdle");
    Integer maxIdle = StringUtils.isBlank(maxIdleString) ? null : Integer
        .parseInt(maxIdleString);

    //default is 5
    String maxWaitString = props.getProperty("maxWait");
    Integer maxWaitSeconds = StringUtils.isBlank(maxWaitString) ? null : Integer
        .parseInt(maxWaitString);

    String dbUrl = null;
    log.debug("Initializing connection factory.");
    dbUrl = props.getProperty("dbUrl");
    String dbUser = props.getProperty("dbUser");
    String dbPwd = props.getProperty("dbPwd");
    dbPwd = Morph.decryptIfFile(dbPwd);

    //defaults to true
    Boolean readOnly = SubjectUtils.booleanObjectValue(props.getProperty("readOnly"));

    String jdbcConnectionProviderString = SubjectUtils.defaultIfBlank(props
        .getProperty("jdbcConnectionProvider"), C3p0JdbcConnectionProvider.class
        .getName());
    Class<JdbcConnectionProvider> jdbcConnectionProviderClass = null;
    try {
      jdbcConnectionProviderClass = SubjectUtils.forName(jdbcConnectionProviderString);
    } catch (RuntimeException re) {
      SubjectUtils
          .injectInException(
              re,
              "Valid built-in options are: "
                  + C3p0JdbcConnectionProvider.class.getName()
                  + " (default) [note: its a zero, not a capital O], "
                  + DbcpJdbcConnectionProvider.class.getName()
                  + ", edu.internet2.middleware.grouper.subj.GrouperJdbcConnectionProvider (if using Grouper).  "
                  + "Note, these are the built-ins for the Subject API or Grouper, there might be other valid choices.");
      throw re;
    }

    this.jdbcConnectionProvider = SubjectUtils.newInstance(jdbcConnectionProviderClass);
    this.jdbcConnectionProvider.init(this.getId(), driver, maxActive, 2, maxIdle, 2,
        maxWaitSeconds, 5, dbUrl, dbUser, dbPwd, readOnly, true);

    log.info("Data Source initialized.");

    //    <param-name>dbTableOrView</param-name>
    this.dbTableOrView = props.getProperty("dbTableOrView");
    if (StringUtils.isBlank(this.dbTableOrView)) {
      throw new SourceUnavailableException("dbTableOrView not defined, source: "
          + this.getId());
    }

    //    <param-name>subjectIdCol</param-name>
    this.subjectIdCol = props.getProperty("subjectIdCol");
    if (StringUtils.isBlank(this.subjectIdCol)) {
      throw new SourceUnavailableException("subjectIdCol not defined, source: "
          + this.getId());
    }
    this.selectCols.add(this.subjectIdCol);

    //    <param-name>nameCol</param-name>
    this.nameCol = props.getProperty("nameCol");
    if (StringUtils.isBlank(this.nameCol)) {
      throw new SourceUnavailableException("nameCol not defined, source: " + this.getId());
    }
    this.selectCols.add(this.nameCol);

    //    <param-name>descriptionCol</param-name>
    this.descriptionCol = props.getProperty("descriptionCol");
    if (!StringUtils.isBlank(this.descriptionCol)) {
      this.selectCols.add(this.descriptionCol);
    }

    //    <param-name>lowerSearchCol</param-name>
    this.lowerSearchCol = props.getProperty("lowerSearchCol");
    if (StringUtils.isBlank(this.lowerSearchCol)) {
      throw new SourceUnavailableException("lowerSearchCol not defined, source: "
          + this.getId());
    }
    this.selectCols.add(this.lowerSearchCol);

    //    <param-name>defaultSortCol</param-name>
    this.defaultSortCol = props.getProperty("defaultSortCol");
    if (!StringUtils.isBlank(this.defaultSortCol)) {
      this.selectCols.add(this.defaultSortCol);
    }

    {
      int index = 0;
      while (true) {
        //    <param-name>subjectIdentifierCol0</param-name>
        String subjectIdentifierCol = props.getProperty("subjectIdentifierCol" + index);
        if (StringUtils.isBlank(subjectIdentifierCol)) {
          break;
        }

        this.subjectIdentifierCols.add(subjectIdentifierCol);

        this.selectCols.add(subjectIdentifierCol);

        index++;
      }
    }

    {
      int index = 0;
      while (true) {
        //    <param-name>subjectAttributeCol0</param-name>
        //    <param-name>subjectAttributeName0</param-name>
        String subjectAttributeCol = props.getProperty("subjectAttributeCol" + index);
        if (StringUtils.isBlank(subjectAttributeCol)) {
          break;
        }
        String subjectAttributeName = props.getProperty("subjectAttributeName" + index);
        if (StringUtils.isBlank(subjectAttributeName)) {
          throw new SourceUnavailableException("subjectAttributeCol" + index
              + " is defined, which requires subjectAttributeName" + index
              + " which cant be found, source: " + this.getId());
        }

        this.subjectAttributeColToName.put(subjectAttributeCol, subjectAttributeName);

        this.selectCols.add(subjectAttributeCol);

        index++;
      }
    }

  }

  /**
   * table or view where each row is a subject
   * <param-name>dbTableOrView</param-name> 
   * @return table or view
   */
  public String getDbTableOrView() {
    return this.dbTableOrView;
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.provider.JDBCSourceAdapter#getSubject(java.lang.String, boolean)
   */
  @Override
  public Subject getSubject(String id, boolean exceptionIfNull) throws SubjectNotFoundException,
      SubjectNotUniqueException {

    String query = "select " + StringUtils.join(this.selectCols.iterator(), ",")
        + " from " + this.dbTableOrView + " where " + this.subjectIdCol + " = ?";

    List<String> args = new ArrayList<String>();
    args.add(id);
    Set<Subject> subject = this.search(query.toString(), args, true, exceptionIfNull);

    //I know there is exactly one at this point
    return subject == null ? null : subject.iterator().next();

  }

  /**
   * 
   * @see edu.internet2.middleware.subject.provider.JDBCSourceAdapter#search(java.lang.String)
   */
  @Override
  public Set<Subject> search(String searchValue) {

    //if there is nothing, what are we searching on?
    if (StringUtils.isBlank(searchValue)) {
      throw new RuntimeException("You need to supply a search value: '" + searchValue
          + "'");
    }

    //lets split by any whitespace space
    String[] terms = searchValue.split("\\s+");

    StringBuilder query = new StringBuilder("select "
        + StringUtils.join(this.selectCols.iterator(), ",") + " from "
        + this.dbTableOrView + " where ");

    List<String> args = new ArrayList<String>();

    for (int i = 0; i < terms.length; i++) {
      query.append(this.lowerSearchCol + " like ?");
      if (i != terms.length - 1) {
        query.append(" and ");
      }
      args.add("%" + terms[i].toLowerCase() + "%");
    }
    
    if (!StringUtils.isBlank(this.defaultSortCol)) {
      query.append(" order by ").append(this.defaultSortCol);
    }
    
    Set<Subject> results = null;
    try {
      results = this.search(query.toString(), args, false, true);
    } catch (SubjectNotUniqueException snue) {
      throw new RuntimeException("This shouldnt happen", snue);
    } catch (SubjectNotFoundException sfue) {
      throw new RuntimeException("This shouldnt happen", sfue);
    }

    return results;

  }

  /**
   * 
   * @see edu.internet2.middleware.subject.provider.JDBCSourceAdapter#getSubjectByIdentifier(java.lang.String, boolean)
   */
  @Override
  public Subject getSubjectByIdentifier(String id, boolean exceptionIfNull) throws SubjectNotFoundException,
      SubjectNotUniqueException {

    //if no identifier col, just get by id
    if (this.subjectIdentifierCols.size() == 0) {
      return this.getSubject(id, exceptionIfNull);
    }

    StringBuilder query = new StringBuilder("select "
        + StringUtils.join(this.selectCols.iterator(), ",") + " from "
        + this.dbTableOrView + " where ");

    List<String> args = new ArrayList<String>();
    int index = 0;
    for (String subjectIdentifierCol : this.subjectIdentifierCols) {
      query.append(subjectIdentifierCol + " = ?");
      if (index != this.subjectIdentifierCols.size() - 1) {
        query.append(" or ");
      }
      args.add(id);
      index++;
    }

    Set<Subject> subject = this.search(query.toString(), args, true, exceptionIfNull);

    //I know there is exactly one at this point
    return subject == null ? null : subject.iterator().next();
  }

  /**
   * Perform a search for subjects
   * 
   * @param query is query to run, prepared statement args should be question marks
   * @param args are the prepared statement args
   * @param expectSingle true if expecting one answer
   * @return subjects or empty set if none or null if expect one and no results and not exception on null
   * @throws SubjectNotFoundException if expecting one and not found
   * @throws SubjectNotUniqueException
   * @throws InvalidQueryRuntimeException 
   */
  private Set<Subject> search(String query, List<String> args, boolean expectSingle, boolean exceptionIfNull)
      throws SubjectNotFoundException, SubjectNotUniqueException,
      InvalidQueryRuntimeException {

    Connection conn = null;
    PreparedStatement stmt = null;
    JdbcConnectionBean jdbcConnectionBean = null;
    Set<Subject> results = new LinkedHashSet<Subject>();

    try {
      jdbcConnectionBean = this.jdbcConnectionProvider.connectionBean();
      conn = jdbcConnectionBean.connection();
      stmt = conn.prepareStatement(query);
      ResultSet rs = null;

      for (int i = 0; i < SubjectUtils.length(args); i++) {

        String arg = args.get(i);

        try {
          stmt.setString(i + 1, arg);
        } catch (SQLException e) {
          SubjectUtils.injectInException(e, "Error setting param: " + i
              + " (zero indexed) in source: " + this.getId() + ", in query: " + query
              + ", " + e.getMessage());
          throw e;
        }
      }

      rs = stmt.executeQuery();

      while (rs.next()) {
        Subject subject = createSubject(rs, query);
        results.add(subject);
      }

      jdbcConnectionBean.doneWithConnection();
    } catch (SQLException ex) {
      String error = "problem in sources.xml source: " + this.getId() + ", sql: " + query;
      try {
        jdbcConnectionBean.doneWithConnectionError(ex);
      } catch (RuntimeException e) {
        log.error(error, e);
      }
      log.error(error, ex);
    } finally {
      closeStatement(stmt);
      if (jdbcConnectionBean != null) {
        jdbcConnectionBean.doneWithConnectionFinally();
      }
    }

    if (expectSingle) {
      if (results.size() > 1) {
        throw new SubjectNotUniqueException("Multiple subjects exist: " + query + ", "
            + StringUtils.join(args.iterator(), ","));
      }
      if (results.size() == 0) {
        if (exceptionIfNull) {
          throw new SubjectNotFoundException("Subject not found: " + query + ", "
              + StringUtils.join(args.iterator(), ","));
        }
        results = null;
      }
    }

    return results;
  }

  /**
   * Create a subject from the current row in the resultSet
   * 
   * @param resultSet
   * @param query 
   * @return subject
   * @throws SQLException 
   */
  private Subject createSubject(ResultSet resultSet, String query) throws SQLException {
    String name = "";
    String subjectID = "";
    String description = "";
    JDBCSubject subject = null;
    //lets do this through metadata so caps dont matter
    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

    subjectID = retrieveString(resultSet, this.subjectIdCol, "subjectIdCol", query,
        resultSetMetaData);
    name = retrieveString(resultSet, this.nameCol, "nameCol", query, resultSetMetaData);
    if (!StringUtils.isBlank(this.descriptionCol)) {
      description = retrieveString(resultSet, this.descriptionCol, "descriptionCol",
          query, resultSetMetaData);
    }
    Map attributes = loadAttributes(resultSet, query, resultSetMetaData);
    subject = new JDBCSubject(subjectID, name, description, this.getSubjectType(), this,
        attributes);
    return subject;
  }

  /**
   * Loads attributes for the argument subject.
   * @param resultSet 
   * @param query for logging
   * @param resultSetMetaData 
   * @return attributes
   * @throws SQLException 
   */
  protected Map<String, Set<String>> loadAttributes(ResultSet resultSet, String query,
      ResultSetMetaData resultSetMetaData) throws SQLException {
    Map<String, Set<String>> attributes = new HashMap<String, Set<String>>();
    if (this.subjectAttributeColToName.size() > 0) {
      int index = 0;
      for (String colName : this.subjectAttributeColToName.keySet()) {
        String value = retrieveString(resultSet, colName, "subjectIdentifierCol" + index,
            query, resultSetMetaData);
        String attributeName = this.subjectAttributeColToName.get(colName);
        attributes.put(attributeName, new JdbcSubjectAttributeSet(value));
        index++;
      }
    }
    //caller should not change this
    return Collections.unmodifiableMap(attributes);
  }

  /**
   * retrieve a param from resultset
   * @param resultSet
   * @param name
   * @param varName 
   * @param query
   * @param resultSetMetaData
   * @return the value
   * @throws SQLException 
   */
  private String retrieveString(ResultSet resultSet, String name, String varName,
      String query, ResultSetMetaData resultSetMetaData) throws SQLException {
    try {
      int columnCount = resultSetMetaData.getColumnCount();

      //this will be case-insensitive
      for (int i = 0; i < columnCount; i++) {
        if (StringUtils.equalsIgnoreCase(name, resultSetMetaData.getColumnName(i + 1))
            || StringUtils
                .equalsIgnoreCase(name, resultSetMetaData.getColumnLabel(i + 1))) {
          return resultSet.getString(i + 1);
        }
      }
      String result = resultSet.getString(name);
      return result;
    } catch (SQLException se) {
      SubjectUtils.injectInException(se, "Error retrieving column name: '" + name
          + "' in source: " + this.getId() + ", in query: " + query + ", "
          + se.getMessage() + ", maybe the column configured in " + varName
          + " does not exist as a query column");
      throw se;

    }
  }

  /**
   * table or view where each row is a subject
   * <param-name>dbTableOrView</param-name> 
   * @param dbTableOrView1
   */
  public void setDbTableOrView(String dbTableOrView1) {
    this.dbTableOrView = dbTableOrView1;
  }

  /**
   * column which holds the subject id
   * <param-name>subjectIdCol</param-name> 
   * @return the subject id col
   */
  public String getSubjectIdCol() {
    return this.subjectIdCol;
  }

  /**
   * column which holds the subject id
   * <param-name>subjectIdCol</param-name> 
   * @param subjectIdCol1
   */
  public void setSubjectIdCol(String subjectIdCol1) {
    this.subjectIdCol = subjectIdCol1;
  }

  /**
   * column which holds the subject name 
   * <param-name>nameCol</param-name> 
   * @return the col for name
   */
  public String getNameCol() {
    return this.nameCol;
  }

  /**
   * column which holds the subject name 
   * <param-name>nameCol</param-name> 
   * @param nameCol1
   */
  public void setNameCol(String nameCol1) {
    this.nameCol = nameCol1;
  }

  /**
   * 
   * @return description col
   */
  public String getDescriptionCol() {
    return this.descriptionCol;
  }

  /**
   * @param descriptionCol1
   */
  public void setDescriptionCol(String descriptionCol1) {
    this.descriptionCol = descriptionCol1;
  }

  /**
   * for searches (not by id or identifier), this is the col which holds the search terms, in lower case 
   * <param-name>lowerSearchCol</param-name> 
   * @return lower search col
   */
  public String getLowerSearchCol() {
    return this.lowerSearchCol;
  }

  /**
   * for searches (not by id or identifier), this is the col which holds the search terms, in lower case 
   * <param-name>lowerSearchCol</param-name> 
   * @param lowerSearchCol1
   */
  public void setLowerSearchCol(String lowerSearchCol1) {
    this.lowerSearchCol = lowerSearchCol1;
  }

  /**
   * search queries will sort by this.  Note it might be overridden by caller, e.g. UI
   * <param-name>defaultSortCol</param-name> 
   * @return sort col
   */
  public String getDefaultSortCol() {
    return this.defaultSortCol;
  }

  /**
   * search queries will sort by this.  Note it might be overridden by caller, e.g. UI
   * <param-name>defaultSortCol</param-name> 
   * @param defaultSortCol1
   */
  public void setDefaultSortCol(String defaultSortCol1) {
    this.defaultSortCol = defaultSortCol1;
  }

  /**
   * cols which are used in a findByIdentifier query
   * <param-name>subjectIdentifierCol0</param-name> 
   * @return subject id cols
   */
  public Set<String> getSubjectIdentifierCols() {
    return this.subjectIdentifierCols;
  }

  /**
   * cols which are used in a findByIdentifier query
   * <param-name>subjectIdentifierCol0</param-name> 
   * @param subjectIdentifierCols1
   */
  public void setSubjectIdentifierCols(Set<String> subjectIdentifierCols1) {
    this.subjectIdentifierCols = subjectIdentifierCols1;
  }

  /**
   * map of col to attribute name
   * //<param-name>subjectAttributeCol0</param-name>
   * //<param-name>subjectAttributeName0</param-name>
   * @return subject attribute col
   */
  public Map<String, String> getSubjectAttributeColToName() {
    return this.subjectAttributeColToName;
  }

  /**
   * map of col to attribute name
   * //<param-name>subjectAttributeCol0</param-name>
   * //<param-name>subjectAttributeName0</param-name>
   * @param subjectAttributeColToName1
   */
  public void setSubjectAttributeColToName(Map<String, String> subjectAttributeColToName1) {
    this.subjectAttributeColToName = subjectAttributeColToName1;
  }

  /**
   * 
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    Subject subject = SubjectFinder.findById("10021368", true);
    
    System.out.println(SubjectHelper.getPrettyComplete(subject));
    
    subject = SubjectFinder.findByIdentifier("mchyzer", true);
    
    System.out.println(SubjectHelper.getPrettyComplete(subject));
    
    System.out.println("\n\n###########################\n\n");
    
    Set<Subject> subjects = SubjectFinder.findAll("BeCk");
    
    for (Subject theSubject : subjects) {
      System.out.println(SubjectHelper.getPrettyComplete(theSubject));
    }
    
    System.out.println("\n\n###########################\n\n");
    
    subjects = SubjectFinder.findAll("ro beck");
    
    for (Subject theSubject : subjects) {
      System.out.println(SubjectHelper.getPrettyComplete(theSubject));
    }
    
  }
  
}
