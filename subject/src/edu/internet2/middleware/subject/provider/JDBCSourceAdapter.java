/*--
$Id: JDBCSourceAdapter.java,v 1.23 2009-10-30 20:41:41 mchyzer Exp $
$Date: 2009-10-30 20:41:41 $
 
Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */
package edu.internet2.middleware.subject.provider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.InvalidQueryException;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectCaseInsensitiveMapImpl;
import edu.internet2.middleware.subject.SubjectCheckConfig;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectTooManyResults;
import edu.internet2.middleware.subject.SubjectUtils;

/**
 * JDBC Source
 */
public class JDBCSourceAdapter extends BaseSourceAdapter {

  /** logger */
  private static Log log = LogFactory.getLog(JDBCSourceAdapter.class);

  /** name attribute name */
  protected String nameAttributeName;

  /** subject id attribute name */
  protected String subjectIDAttributeName;

  /** decsription attribute name */
  protected String descriptionAttributeName;

  /** subject type string */
  protected String subjectTypeString;

  /** if there is a limit to the number of results */
  protected Integer maxResults;
  
  /** keep a reference to the object which gets our connections for us */
  protected JdbcConnectionProvider jdbcConnectionProvider = null;

  /**
   * Allocates new JDBCSourceAdapter;
   */
  public JDBCSourceAdapter() {
    super();
  }

  /**
   * Allocates new JDBCSourceAdapter;
   * 
   * @param id1
   * @param name1
   */
  public JDBCSourceAdapter(String id1, String name1) {
    super(id1, name1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Subject getSubject(String id1, boolean exceptionIfNull)
      throws SubjectNotFoundException, SubjectNotUniqueException {
    try {
      return uniqueSearch(id1, "searchSubject");
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNull) {
        throw snfe;
      }
      return null;
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubjectByIdentifier(java.lang.String, boolean)
   */
  @Override
  public Subject getSubjectByIdentifier(String id1, boolean exceptionIfNull) throws SubjectNotFoundException,
      SubjectNotUniqueException {
    try {
      return uniqueSearch(id1, "searchSubjectByIdentifier");
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNull) {
        throw snfe;
      }
      return null;
    }
  }

  /**
   * Perform a search for a unique subject.
   * 
   * @param id1
   * @param searchType
   * @return subject
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   * @throws InvalidQueryException 
   */
  private Subject uniqueSearch(String id1, String searchType)
      throws SubjectNotFoundException, SubjectNotUniqueException, InvalidQueryException {
    Subject subject = null;
    Search search = getSearch(searchType);
    if (search == null) {
      log.error("searchType: \"" + searchType + "\" not defined.");
      return subject;
    }
    Connection conn = null;
    PreparedStatement stmt = null;
    JdbcConnectionBean jdbcConnectionBean = null;
    try {
      jdbcConnectionBean = this.jdbcConnectionProvider.connectionBean();
      conn = jdbcConnectionBean.connection();
      stmt = prepareStatement(search, conn);
      ResultSet rs = getSqlResults(id1, stmt, search);
      subject = createUniqueSubject(rs, search, id1, search.getParam("sql"));
      jdbcConnectionBean.doneWithConnection();
    } catch (SQLException ex) {
      String error = "problem in sources.xml source: " + this.getId() + ", sql: "
          + search.getParam("sql") + ", " + id1 + ", " + searchType;
      try {
        jdbcConnectionBean.doneWithConnectionError(ex);
      } catch (RuntimeException e) {
        log.error(error, e);
      }
      throw new SourceUnavailableException(error, ex);
    } finally {
      closeStatement(stmt);
      if (jdbcConnectionBean != null) {
        jdbcConnectionBean.doneWithConnectionFinally();
      }
    }
    if (subject == null) {
      throw new SubjectNotFoundException("Subject " + id1 + " not found.");
    }
    return subject;
  }

  /** for testing if we should fail on testing */
  public static boolean failOnSearchForTesting = false;

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Subject> search(String searchValue) {
    Set<Subject> result = new HashSet<Subject>();
    Search search = getSearch("search");
    if (search == null) {
      log.error("searchType: \"search\" not defined.");
      return result;
    }
    String throwErrorOnFindAllFailureString = this.getInitParam("throwErrorOnFindAllFailure");
    boolean throwErrorOnFindAllFailure = SubjectUtils.booleanValue(throwErrorOnFindAllFailureString, true);

    Connection conn = null;
    PreparedStatement stmt = null;
    JdbcConnectionBean jdbcConnectionBean = null;
    try {
      if (failOnSearchForTesting) {
        throw new RuntimeException("failOnSearchForTesting");
      }
      
      jdbcConnectionBean = this.jdbcConnectionProvider.connectionBean();
      conn = jdbcConnectionBean.connection();
      stmt = prepareStatement(search, conn);
      ResultSet rs = getSqlResults(searchValue, stmt, search);
      if (rs == null) {
        return result;
      }
      while (rs.next()) {
        Subject subject = createSubject(rs, search.getParam("sql"));
        result.add(subject);
        if (this.maxResults != null && result.size() > this.maxResults) {
          throw new SubjectTooManyResults(
              "More results than allowed: " + this.maxResults 
              + " for search '" + search + "'");
        }
      }
      jdbcConnectionBean.doneWithConnection();
    } catch (Exception ex) {
      try {
        jdbcConnectionBean.doneWithConnectionError(ex);
      } catch (RuntimeException re) {
        log.error(re);
      }
      if (!throwErrorOnFindAllFailure) {
        log.error(ex.getMessage() + ", source: " + this.getId() + ", sql: "
          + search.getParam("sql"), ex);
      } else {
        throw new SourceUnavailableException(ex.getMessage() + ", source: " + this.getId() + ", sql: "
            + search.getParam("sql"), ex);
      }
    } finally {
      closeStatement(stmt);
      if (jdbcConnectionBean != null) {
        jdbcConnectionBean.doneWithConnectionFinally();
      }
    }
    return result;
  }

  /**
   * Create a subject from the current row in the resultSet
   * 
   * @param rs
   * @param sql 
   * @return subject
   */
  private Subject createSubject(ResultSet rs, String sql) {
    String name1 = "";
    String subjectID = "";
    String description = "";
    Subject subject = null;
    try {
      subjectID = retrieveString(rs, this.subjectIDAttributeName,
          "SubjectID_AttributeType", sql);
      name1 = retrieveString(rs, this.nameAttributeName, "Name_AttributeType", sql);
      if (!this.descriptionAttributeName.equals("")) {
        description = retrieveString(rs, this.descriptionAttributeName,
            "Description_AttributeType", sql);
      }
      Map<String, Set<String>> attributes1 = loadAttributes(rs);
      subject = new SubjectImpl(subjectID, name1, description, this.getSubjectType().getName(),
          this.getId(), attributes1);
    } catch (SQLException ex) {
      throw new SourceUnavailableException("SQLException occurred: " + ex.getMessage() + ": " + sql, ex);
    }
    return subject;
  }

  /**
   * retrieve a param from resultset
   * @param rs
   * @param name1
   * @param varName 
   * @param sql
   * @return the value
   * @throws SQLException 
   */
  private String retrieveString(ResultSet rs, String name1, String varName, String sql)
      throws SQLException {
    try {
      String result = rs.getString(name1);
      return result;
    } catch (SQLException se) {
      SubjectUtils.injectInException(se, "Error retrieving column name: '" + name1
          + "' in source: " + this.getId() + ", in query: " + sql + ", "
          + se.getMessage() + ", maybe the column configured in " + varName
          + " does not exist as a query column");
      throw se;

    }
  }

  /**
   * Create a unique subject from the resultSet.
   * 
   * @param rs
   * @param search
   * @param searchValue
   * @param sql 
   * @return subject
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  private Subject createUniqueSubject(ResultSet rs, Search search, String searchValue,
      String sql) throws SubjectNotFoundException, SubjectNotUniqueException {
    Subject subject = null;
    try {
      if (rs == null || !rs.next()) {
        String errMsg = "No results: " + search.getSearchType() + " searchValue: "
            + searchValue;
        throw new SubjectNotFoundException(errMsg);
      }
      subject = createSubject(rs, sql);
      if (rs.next()) {
        String errMsg = "Search is not unique:"
            + rs.getString(this.subjectIDAttributeName) + "\n";
        throw new SubjectNotUniqueException(errMsg);
      }
    } catch (SQLException ex) {
      throw new SourceUnavailableException("SQLException occurred: " + ex.getMessage() + ": " + sql, ex);
    }
    return subject;

  }

  /**
   * Prepare a statement handle from the search object.
   * 
   * @param search
   * @param conn
   * @return the prepared statement
   * @throws InvalidQueryException
   * @throws SQLException
   */
  protected PreparedStatement prepareStatement(Search search, Connection conn)
      throws InvalidQueryException, SQLException {
    String sql = search.getParam("sql");
    if (sql == null) {
      throw new InvalidQueryException("No sql parameter for search type "
          + search.getSearchType() + ", source: " + this.getId());
    }
    if (sql.contains("%TERM%")) {
      throw new InvalidQueryException("%TERM%. Possibly old style SQL query, source: "
          + this.getId() + ", sql: " + sql);
    }
    String numParametersString = search.getParam("numParameters");
    //not required since it will just count the number of question marks
    if (!StringUtils.isBlank(numParametersString)) {
      try {
        Integer.parseInt(numParametersString);
      } catch (NumberFormatException e) {
        throw new InvalidQueryException(
            "Non-numeric numParameters parameter specified, source: " + this.getId()
                + ", sql: " + sql);
      }
    }
    PreparedStatement stmt = conn.prepareStatement(sql);
    return stmt;
  }

  /**
   * Set the parameters in the prepared statement and execute the query.
   * 
   * @param searchValue
   * @param stmt
   * @param search
   * @return resultSet
   * @throws SQLException 
   */
  protected ResultSet getSqlResults(String searchValue, PreparedStatement stmt,
      Search search) throws SQLException {
    ResultSet rs = null;
    String sql = search.getParam("sql");
    String numParametersString = search.getParam("numParameters");
    //default to the number of question marks
    int numParameters = StringUtils.isBlank(numParametersString) ? StringUtils
        .countMatches(sql, "?") : Integer.parseInt(numParametersString);
    for (int i = 1; i <= numParameters; i++) {
      try {
        stmt.setString(i, searchValue);
      } catch (SQLException e) {
        SubjectUtils
            .injectInException(
                e,
                "Error setting param: "
                    + i
                    + " in source: "
                    + this.getId()
                    + ", in query: "
                    + sql
                    + ", "
                    + e.getMessage()
                    + ", maybe not enough question marks "
                    + "(bind variables) are in query, or the number of question marks in the query is "
                    + "not the same as the number of parameters (might need to set the optional param numParameters), "
                    + "or the param 'numParameters' in sources.xml for that query is incorrect");
        throw e;
      }
    }
    rs = stmt.executeQuery();
    return rs;
  }

  /**
   * Loads attributes for the argument subject.
   * @param rs 
   * @return attributes
   */
  protected Map<String, Set<String>> loadAttributes(ResultSet rs) {
    Map<String, Set<String>> attributes1 = new SubjectCaseInsensitiveMapImpl<String, Set<String>>();
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int colCount = rsmd.getColumnCount();
      String[] colNames = new String[colCount];
      for (int i = 0; i < colCount; i++) {
        colNames[i] = rsmd.getColumnName(i + 1);
      }
      for (int i = 0; i < colCount; i++) {
        String name1 = colNames[i];
        if (name1.toLowerCase().equals(this.subjectIDAttributeName.toLowerCase())) {
          continue;
        }
        if (name1.toLowerCase().equals(this.nameAttributeName.toLowerCase())) {
          continue;
        }
        if (name1.toLowerCase().equals(this.descriptionAttributeName.toLowerCase())) {
          continue;
        }
        String value = rs.getString(i + 1);
        Set<String> values = attributes1.get(name1);
        if (values == null) {
          values = new HashSet<String>();
          attributes1.put(name1, values);
        }
        values.add(value);
      }
    } catch (SQLException ex) {
      throw new SourceUnavailableException("SQLException occurred: " + ex.getMessage(), ex);
    }

    return attributes1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void init() throws SourceUnavailableException {
    try {
      Properties props = getInitParams();
      //this might not exist if it is Grouper source and no driver...
      setupDataSource(props);
    } catch (Exception ex) {
      throw new SourceUnavailableException(
          "Unable to init sources.xml JDBC source, source: " + this.getId(), ex);
    }
  }

  /**
   * Loads the the JDBC driver.
   * @param sourceId 
   * @param driver 
   * @throws SourceUnavailableException 
   */
  public static void loadDriver(String sourceId, String driver)
      throws SourceUnavailableException {
    try {
      Class.forName(driver).newInstance();
      log.debug("Loading JDBC driver: " + driver);
    } catch (Exception ex) {
      throw new SourceUnavailableException("Error loading sources.xml JDBC driver: "
          + driver + ", source: " + sourceId, ex);
    }
    log.info("JDBC driver loaded.");
  }

  /**
   * DataSource connection pool setup.
   * @param props 
   * @throws SourceUnavailableException
   */
  @SuppressWarnings("unchecked")
  protected void setupDataSource(Properties props) throws SourceUnavailableException {

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
    this.nameAttributeName = props.getProperty("Name_AttributeType");
    if (this.nameAttributeName == null) {
      throw new SourceUnavailableException("Name_AttributeType not defined, source: "
          + this.getId());
    }
    this.subjectIDAttributeName = props.getProperty("SubjectID_AttributeType");
    if (this.subjectIDAttributeName == null) {
      throw new SourceUnavailableException(
          "SubjectID_AttributeType not defined, source: " + this.getId());
    }
    this.descriptionAttributeName = props.getProperty("Description_AttributeType");
    if (this.descriptionAttributeName == null) {
      throw new SourceUnavailableException(
          "Description_AttributeType not defined, source: " + this.getId());
    }
    
    String maxResultsString = props.getProperty("maxResults");
    if (!StringUtils.isBlank(maxResultsString)) {
      try {
        this.maxResults = Integer.parseInt(maxResultsString);
      } catch (NumberFormatException nfe) {
        throw new SourceUnavailableException("Cant parse maxResults: " + maxResultsString, nfe);
      }
    }
  }

  /**
   * 
   * @param stmt
   */
  protected void closeStatement(PreparedStatement stmt) {
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException ex) {
        log.info("Error while closing JDBC Statement.");
      }
    }
  }

  /**
   * @return the descriptionAttributeName
   */
  public String getDescriptionAttributeName() {
    return this.descriptionAttributeName;
  }

  /**
   * @return the nameAttributeName
   */
  public String getNameAttributeName() {
    return this.nameAttributeName;
  }

  /**
   * @return the subjectIDAttributeName
   */
  public String getSubjectIDAttributeName() {
    return this.subjectIDAttributeName;
  }

  /**
   * @return the subjectTypeString
   */
  public String getSubjectTypeString() {
    return this.subjectTypeString;
  }

  /**
   * @see edu.internet2.middleware.subject.Source#checkConfig()
   */
  public void checkConfig() {

    //see if has jdbc in provider
    if (this.jdbcConnectionProvider.requiresJdbcConfigInSourcesXml()) {
      String error = "problem with sources.xml source id: " + this.getId() + ", ";
      Properties props = this.getInitParams();
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
  }

  /**
   * @see edu.internet2.middleware.subject.Source#printConfig()
   */
  public String printConfig() {

    Properties props = this.getInitParams();
    String dbResult = this.jdbcConnectionProvider.getClass().getSimpleName();
    if (this.jdbcConnectionProvider.requiresJdbcConfigInSourcesXml()) {
      String dbUrl = props.getProperty("dbUrl");
      String dbUser = props.getProperty("dbUser");
      dbResult = dbUser + "@" + dbUrl;
    }
    String message = "sources.xml jdbc source id:   " + this.getId() + ": " + dbResult;
    return message;
  }

  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubject(java.lang.String)
   * @deprecated
   */
  @Deprecated
  @Override
  public Subject getSubject(String id1) throws SubjectNotFoundException,
      SubjectNotUniqueException {
    return this.getSubject(id1, true);
  }

  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubjectByIdentifier(java.lang.String)
   * @deprecated
   */
  @Deprecated
  @Override
  public Subject getSubjectByIdentifier(String id1) throws SubjectNotFoundException,
      SubjectNotUniqueException {
    return this.getSubjectByIdentifier(id1, true);
  }
}
