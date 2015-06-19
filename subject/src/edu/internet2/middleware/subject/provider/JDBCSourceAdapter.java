/**
 * Copyright 2014 Internet2
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
 */
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.InvalidQueryException;
import edu.internet2.middleware.subject.SearchPageResult;
import edu.internet2.middleware.subject.Source;
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

  /** the database type will try to be detected by URL, though if you want to specify in sources.xml, you can */
  private String databaseType;
  
  /** if there is a limit to the number of results */
  protected Integer maxResults;
  
  /** if there is a limit to the number of results */
  private Integer maxPage;
  
  /** if we should batch up ids and identifiers */
  private boolean useInClauseForIdAndIdentifier = false;
  
  /** keep a reference to the object which gets our connections for us */
  protected JdbcConnectionProvider jdbcConnectionProvider = null;

  /** if we should change the search query for max results */
  private boolean changeSearchQueryForMaxResults = true;

  /** comma separate the identifiers for this row, this is for the findByIdentifiers if using an in clause */
  private List<String> identifierAttributes = new ArrayList<String>();
  
  /**
   * try to change a paging query, note it will add one to the resultSetLimit so that
   * the caller can see if there are too many records
   * @param query
   * @param conn
   * @param resultSetLimit
   * @return the query with paging or original query if cant change it
   */
  public String tryToChangeQuery(String query, Connection conn, int resultSetLimit) {
    
    //lets see if we are restricted
    if (!this.changeSearchQueryForMaxResults || resultSetLimit <= 0) {
      return query;
    }
    
    JdbcDatabaseType jdbcDatabaseType = JdbcDatabaseType.resolveDatabaseType(conn);

    if (jdbcDatabaseType == null) {
      return query;
    }
    
    //we need to add one so we know if there are too many results...
    resultSetLimit++;
    
    String newQuery = jdbcDatabaseType.pageQuery(query, resultSetLimit);
    
    if (StringUtils.isBlank(newQuery)) {
      return query;
    }
    
    return newQuery;
  }
  
  /**
   * @return the databaseType
   */
  public String getDatabaseType() {
    return this.databaseType;
  }

  /**
   * if we should change the query based on max resuuls or page
   * @return the changeSearchQueryForMaxResults
   */
  public boolean isChangeSearchQueryForMaxResults() {
    return this.changeSearchQueryForMaxResults;
  }

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
    
    //do the batched one
    if (this.useInClauseForIdAndIdentifier) {
      
      Map<String, Subject> result = this.getSubjectsByIds(SubjectUtils.toSet(id1));
      
      if (SubjectUtils.length(result) == 1) {
        return result.get(id1);
      }
      
      if (SubjectUtils.length(result) > 1) {
        throw new SubjectNotUniqueException("Not unique by id: " + id1);
      }
      
      if (exceptionIfNull) {
        throw new SubjectNotFoundException("Cant find subject by id: " + id1);
      }
      
      return null;
    }
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
    //do the batched one
    if (this.useInClauseForIdAndIdentifier) {
      
      Map<String, Subject> result = this.getSubjectsByIdentifiers(SubjectUtils.toSet(id1));
      
      if (SubjectUtils.length(result) == 1) {
        return result.get(id1);
      }
      
      if (SubjectUtils.length(result) > 1) {
        throw new SubjectNotUniqueException("Not unique by id: " + id1);
      }

      if (exceptionIfNull) {
        throw new SubjectNotFoundException("Cant find subject by id: " + id1);
      }
      
      return null;
    }
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
      
      queryCountforTesting++;
      
      stmt = prepareStatement(search, conn, false, false);
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
    return searchHelper(searchValue, false).getResults();
  }

  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#searchPage(java.lang.String)
   */
  @Override
  public SearchPageResult searchPage(String searchValue) {
    return searchHelper(searchValue, true);
  }

  /** increment a query count for testing */
  public static int queryCountforTesting = 0;
  
  /**
   * helper for query
   * @param searchValue 
   * @param firstPageOnly 
   * @return the result object, never null
   */
  private SearchPageResult searchHelper(String searchValue, boolean firstPageOnly) {

    //if this is a search and not by id or identifier, strip out the status part
    {
      SubjectStatusResult subjectStatusResult = null;
      
      //see if we are doing status
      SubjectStatusProcessor subjectStatusProcessor = new SubjectStatusProcessor(searchValue, this.getSubjectStatusConfig());
      subjectStatusResult = subjectStatusProcessor.processSearch();

      //strip out status parts
      searchValue = subjectStatusResult.getStrippedQuery();
    }      
    

    Set<Subject> result = new LinkedHashSet<Subject>();
    boolean tooManyResults = false;
    Search search = getSearch("search");
    if (search == null) {
      log.error("searchType: \"search\" not defined.");
      return new SearchPageResult(false, result);
    }
    String throwErrorOnFindAllFailureString = this.getInitParam("throwErrorOnFindAllFailure");
    boolean throwErrorOnFindAllFailure = SubjectUtils.booleanValue(throwErrorOnFindAllFailureString, true);

    Connection conn = null;
    PreparedStatement stmt = null;
    JdbcConnectionBean jdbcConnectionBean = null;
    try {
      jdbcConnectionBean = this.jdbcConnectionProvider.connectionBean();

      if (failOnSearchForTesting) {
        throw new RuntimeException("failOnSearchForTesting");
      }
      
      conn = jdbcConnectionBean.connection();
      
      queryCountforTesting++;
      
      stmt = prepareStatement(search, conn, true, firstPageOnly);
      ResultSet rs = getSqlResults(searchValue, stmt, search);
      if (rs == null) {
        return new SearchPageResult(false, result);
      }
      while (rs.next()) {
        Subject subject = createSubject(rs, search.getParam("sql"));
        result.add(subject);
        //if we are at the end of the page
        if (firstPageOnly && this.maxPage != null && result.size() >= this.maxPage) {
          tooManyResults = true;
          break;
        }
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
        if (!(ex instanceof SubjectTooManyResults)) {
          log.error("Problem with source: " + this.getId() + ", and query: '" + searchValue + "'", re);
        }
      }
      if (ex instanceof SubjectTooManyResults) {
        throw (SubjectTooManyResults)ex;
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
    return new SearchPageResult(tooManyResults, result);
  }

  /**
   * Create a subject from the current row in the resultSet
   * 
   * @param rs
   * @param sql 
   * @return subject
   */
  private Subject createSubject(ResultSet rs, String sql) {
    String subjectID = "";
    Subject subject = null;
    try {
      subjectID = retrieveString(rs, this.subjectIDAttributeName,
          "SubjectID_AttributeType", sql);

      Map<String, Set<String>> attributes1 = loadAttributes(rs);
      subject = new SubjectImpl(subjectID, null, null, this.getSubjectType().getName(),
          this.getId(), attributes1, this.nameAttributeName, this.descriptionAttributeName);
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
   * Create unique subjects from the resultSet.
   * 
   * @param rs
   * @param search
   * @param searchValue
   * @param sql 
   * @return subject
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  private Map<String, Subject> createUniqueSubjects(ResultSet rs, Search search, List<String> searchValues,
      String sql, boolean useIdentifiersInMatch) throws SubjectNotFoundException, SubjectNotUniqueException {

    Map<String, Subject> results = new HashMap<String, Subject>();
    Set<String> searchValuesSet = new HashSet<String>(searchValues);

    if (rs != null) {
      try {
        RESULT_SET_LOOP: while (rs.next()) {
          
          Subject subject = createSubject(rs, sql);

          //maybe id match
          if (searchValues.contains(subject.getId())) {
            results.put(subject.getId(), subject);
          } else {
            
            //find out which id or identifier found this one
            for (String identifierAttribute : this.identifierAttributes) {
              Set<String> values = subject.getAttributeValues(identifierAttribute);
              if (SubjectUtils.length(values) > 0) {
                for (String value: values) {
                  //we found a match
                  if (searchValuesSet.contains(value)) {
                    results.put(value, subject);
                    //dont want dupes
                    searchValuesSet.remove(value);
                    continue RESULT_SET_LOOP;
                  }
                }
              }
              
            }

            //if we made it this far there is a problem
            throw new InvalidQueryException("Why is this subject not able to be " +
                "referenced by id or identifier (do you need to add " +
                "identifierAttributes to your sources.xml???) " + SubjectUtils.subjectToString(subject) );

          }
          
        }
      } catch (SQLException ex) {
        throw new SourceUnavailableException("SQLException occurred: " + ex.getMessage() + ": " + sql, ex);
      }

    }
    return results;

  }

  /**
   * Prepare a statement handle from the search object.
   * 
   * @param search
   * @param conn
   * @param searchAll true if a searchAll method
   * @param firstPageOnly 
   * @return the prepared statement
   * @throws InvalidQueryException
   * @throws SQLException
   */
  protected PreparedStatement prepareStatement(Search search, Connection conn, boolean searchAll, boolean firstPageOnly)
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
    
    if (searchAll) {
      Integer pageSize = resultSetLimit(firstPageOnly, this.getMaxPage(), this.maxResults);
      if (pageSize != null && pageSize > 0) {
        
        sql = this.tryToChangeQuery(sql, conn, pageSize);
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
   * Set the parameters in the prepared statement and execute the query.
   * 
   * @param searchValue
   * @param stmt
   * @param search
   * @return resultSet
   * @throws SQLException 
   */
  protected ResultSet getSqlResults(List<String> batchIdsOrIdentifiers, PreparedStatement stmt, 
      int numParameters, String sql) throws SQLException {

    ResultSet rs = null;
    int paramIndex = 1;
    for (String idOrIdentifier : batchIdsOrIdentifiers) {
      
      //there might be more than one param for the same value
      for (int i = 1; i <= numParameters; i++) {
        try {
          stmt.setString(paramIndex++, idOrIdentifier);
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
                      + "or the param 'numParameters' in sources.xml for that query is incorrect, " + this.getId());
          throw e;
        }
      }
    }
    rs = stmt.executeQuery();
    return rs;
  }

  /**
   * @see Source#getSubjectsByIdentifiers(Collection)
   */
  @Override
  public Map<String, Subject> getSubjectsByIdentifiers(Collection<String> identifiers) {
    //if not the batched one
    if (!this.useInClauseForIdAndIdentifier) {
      return super.getSubjectsByIdentifiers(identifiers);
    }

    return uniqueSearchBatch(identifiers, "searchSubjectByIdentifier", true);
    
  }

  /**
   * @see Source#getSubjectsByIds(Collection)
   */
  @Override
  public Map<String, Subject> getSubjectsByIds(Collection<String> ids) {
    //if not the batched one
    if (!this.useInClauseForIdAndIdentifier) {
      return super.getSubjectsByIds(ids);
    }
    
    return uniqueSearchBatch(ids, "searchSubject", false);
    
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
        /*
        if (name1.toLowerCase().equals(this.subjectIDAttributeName.toLowerCase())) {
          continue;
        }
        if (name1.toLowerCase().equals(this.nameAttributeName.toLowerCase())) {
          continue;
        }
        if (name1.toLowerCase().equals(this.descriptionAttributeName.toLowerCase())) {
          continue;
        }
        */
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
      
      
      {
        String maxResultsString = props.getProperty("maxResults");
        if (!StringUtils.isBlank(maxResultsString)) {
          try {
            this.maxResults = Integer.parseInt(maxResultsString);
          } catch (NumberFormatException nfe) {
            throw new SourceUnavailableException("Cant parse maxResults: " + maxResultsString, nfe);
          }
        }
      }
      
      {
        String maxPageString = props.getProperty("maxPageSize");
        if (!StringUtils.isBlank(maxPageString)) {
          try {
            this.maxPage = Integer.parseInt(maxPageString);
          } catch (NumberFormatException nfe) {
            throw new SourceUnavailableException("Cant parse maxPage: " + maxPageString, nfe);
          }
        }
      }

      {
        String useInClauseForIdAndIdentifierString = props.getProperty("useInClauseForIdAndIdentifier");
        if (!StringUtils.isBlank(useInClauseForIdAndIdentifierString)) {
          try {
            this.useInClauseForIdAndIdentifier = SubjectUtils.booleanValue(useInClauseForIdAndIdentifierString);
          } catch (Exception e) {
            throw new SourceUnavailableException("Cant parse useInClauseForIdAndIdentifier: " + useInClauseForIdAndIdentifierString, e);
          }
        }
      }

      {
        String identifierAttributesString = props.getProperty("identifierAttributes");
        if (!StringUtils.isBlank(identifierAttributesString)) {
          this.identifierAttributes = SubjectUtils.toList(SubjectUtils.splitTrim(identifierAttributesString, ","));
        }
      }

      {
        String changeSearchQueryForMaxResultsString = props.getProperty("changeSearchQueryForMaxResults");
        if (!StringUtils.isBlank(changeSearchQueryForMaxResultsString)) {
          try {
            this.changeSearchQueryForMaxResults = SubjectUtils.booleanValue(changeSearchQueryForMaxResultsString);
          } catch (Exception e) {
            throw new SourceUnavailableException("Cant parse changeSearchQueryForMaxResults: " + changeSearchQueryForMaxResultsString, e);
          }
        }
      }

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
                  //+ DbcpJdbcConnectionProvider.class.getName()
                  + ", edu.internet2.middleware.grouper.subj.GrouperJdbcConnectionProvider (if using Grouper).  "
                  + "Note, these are the built-ins for the Subject API or Grouper, there might be other valid choices.");
      throw re;
    }

    this.jdbcConnectionProvider = SubjectUtils.newInstance(jdbcConnectionProviderClass);
    this.jdbcConnectionProvider.init(props, this.getId(), driver, maxActive, 2, maxIdle, 2,
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
  }

  
  /**
   * max Page size
   * @return the maxPage
   */
  public Integer getMaxPage() {
    return this.maxPage;
  }

  /**
   * if we should use an in clause for id or identifier searches
   * @return true if should
   */
  public boolean isUseInClauseForIdAndIdentifier() {
    return this.useInClauseForIdAndIdentifier;
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
      
      {
        String maxResultsString = props.getProperty("maxResults");
        if (!StringUtils.isBlank(maxResultsString)) {
          try {
            Integer.parseInt(maxResultsString);
          } catch (Exception e) {
            System.err.println("Cant parse maxResults: " + maxResultsString);
            log.error("Cant parse maxResults: " + maxResultsString);
            return;
          }
        }
      }
      
      {
        String maxPageString = props.getProperty("maxPageSize");
        if (!StringUtils.isBlank(maxPageString)) {
          try {
            Integer.parseInt(maxPageString);
          } catch (Exception e) {
            System.err.println("Cant parse maxPageSize: " + maxPageString);
            log.error("Cant parse maxPageSize: " + maxPageString);
            return;
          }
        }
      }

      {
        String useInClauseForIdAndIdentifierString = props.getProperty("useInClauseForIdAndIdentifier");
        if (!StringUtils.isBlank(useInClauseForIdAndIdentifierString)) {
          try {
            Integer.parseInt(useInClauseForIdAndIdentifierString);
          } catch (Exception e) {
            System.err.println("Cant parse useInClauseForIdAndIdentifier: " + useInClauseForIdAndIdentifier);
            log.error("Cant parse useInClauseForIdAndIdentifier: " + useInClauseForIdAndIdentifier);
            return;
          }
        }
      }

      {
        String changeSearchQueryForMaxResultsString = props.getProperty("changeSearchQueryForMaxResults");
        if (!StringUtils.isBlank(changeSearchQueryForMaxResultsString)) {
          try {
            SubjectUtils.booleanValue(changeSearchQueryForMaxResultsString);
          } catch (Exception e) {
            System.err.println("Cant parse changeSearchQueryForMaxResults: " + changeSearchQueryForMaxResultsString);
            log.error("Cant parse changeSearchQueryForMaxResults: " + changeSearchQueryForMaxResultsString);
            return;
          }
        }
      }

      
      
      String dbPwd = StringUtils.defaultString(props.getProperty("dbPwd"));
      //      if (StringUtils.isBlank(dbPwd)) {
      //        System.err.println("Subject API error: " + error + ", dbPwd param is required");
      //        log.error(error + ", dbPwd param is required");
      //        return;
      //      }
      dbPwd = Morph.decryptIfFile(dbPwd);

      try {

        Class<?> driverClass = null;
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

  /**
   * Perform a search for a unique subject.
   * 
   * @param id1
   * @param searchType
   * @return subjects
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   * @throws InvalidQueryException 
   */
  private Map<String, Subject> uniqueSearchBatch(Collection<String> idsOrIdentifiers, String searchType, boolean useIdentifiersInMatch)
      throws SubjectNotFoundException, SubjectNotUniqueException, InvalidQueryException {

    Map<String, Subject> results = new LinkedHashMap<String, Subject>();
    
    if (SubjectUtils.length(idsOrIdentifiers) > 0) {
      
      Search search = getSearch(searchType);

      if (search == null) {
        log.error("searchType: \"" + searchType + "\" not defined.");
        return results;
      }

      String sql = search.getParam("sql");
      
      if (StringUtils.isBlank(sql)) {
        throw new SourceUnavailableException("Why is there no sql for sourceId: " + this.getId());
      }

      if (StringUtils.countMatches(sql, "?") > 0) {
        throw new SourceUnavailableException("Why are there parameters in the sql? " + this.getId());
      }

      if (!StringUtils.contains(sql, "{inclause}")) {
        throw new SourceUnavailableException("Why does the SQL not have an {inclause} param? " + this.getId() + ", " + sql);
      }
      
      if (sql.contains("%TERM%")) {
        throw new InvalidQueryException("%TERM%. Possibly old style SQL query, source: "
            + this.getId() + ", sql: " + sql);
      }

      String inclause = search.getParam("inclause");
      
      if (StringUtils.isBlank(inclause)) {
        throw new SourceUnavailableException("Why is there no inclause? " + this.getId());
      }
      
      String numParametersString = search.getParam("numParameters");

      //default to the number of question marks
      int numParameters = StringUtils.isBlank(numParametersString) ? StringUtils
          .countMatches(inclause, "?") : Integer.parseInt(numParametersString);
          
      if (numParameters == 0) {
        throw new SourceUnavailableException("Why are there no parameters in the inclause? " + this.getId());
      }
          
      //we dont want more than 180 bind variables per batch
      int batchSize = 180 / numParameters;
      
      List<String> idsOrIdentifiersList = SubjectUtils.listFromCollection(idsOrIdentifiers);
      
      int numberOfBatches = SubjectUtils.batchNumberOfBatches(idsOrIdentifiersList, batchSize);
      
      for (int i=0;i<numberOfBatches;i++) {

        List<String> batchIdsOrIdentifiers = SubjectUtils.batchList(idsOrIdentifiersList, batchSize, i);
        
        Connection conn = null;
        PreparedStatement stmt = null;
        JdbcConnectionBean jdbcConnectionBean = null;
        String aggregateSql = sql;
        try {
          jdbcConnectionBean = this.jdbcConnectionProvider.connectionBean();
          conn = jdbcConnectionBean.connection();
          
          queryCountforTesting++;
          aggregateSql = uniqueSearchBatchSql(sql, inclause, batchIdsOrIdentifiers);
          
          stmt = conn.prepareStatement(aggregateSql);
          
          ResultSet rs = getSqlResults(batchIdsOrIdentifiers, stmt, numParameters, aggregateSql);
          Map<String, Subject> resultBatch = createUniqueSubjects(rs, search, batchIdsOrIdentifiers, aggregateSql, useIdentifiersInMatch);
          results.putAll(resultBatch);

          jdbcConnectionBean.doneWithConnection();
        } catch (SQLException ex) {
          String error = "problem in sources.xml source: " + this.getId() + ", sql: "
              + aggregateSql + ", id size: " + SubjectUtils.length(idsOrIdentifiers) + ", " + searchType;
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
        
      }
    }
    return results;
  }

  /**
   * turn a sql, and inclause, and batch, into a sql statement
   * @param sql
   * @param inclause
   * @param numParameters
   * @param batchIdsOrIdentifiers
   * @return unique search sql
   */
  private String uniqueSearchBatchSql(String sql, String inclause, List<String> batchIdsOrIdentifiers) {
    StringBuilder result = new StringBuilder();
    result.append(" ( ");
    for (int i=0;i<SubjectUtils.length(batchIdsOrIdentifiers);i++) {
      if (i != 0) {
        result.append(" or ");
      }

      result.append(" ( ");
      result.append(inclause);
      result.append(" ) ");
    }
    result.append(" ) ");
    String aggregateInclause = result.toString();
    
    String aggregateSql = StringUtils.replace(sql, "{inclause}", aggregateInclause);
    
    return aggregateSql;
  }
  
  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectIdentifierAttributes()
   */
  public Map<Integer, String> getSubjectIdentifierAttributes() {
    
    if (this.subjectIdentifierAttributes == null) {
      synchronized(JDBCSourceAdapter.class) {
        if (this.subjectIdentifierAttributes == null) {
          LinkedHashMap<Integer, String> temp = new LinkedHashMap<Integer, String>();
          
          for (int i = 0; i < 1; i++) {
            String value = getInitParam("subjectIdentifierAttribute" + i);
            if (value != null) {
              temp.put(i, value.toLowerCase());
            }        
          }
          
          // if we still don't have anything..
          if (temp.size() == 0) {
            if (this.identifierAttributes != null && this.identifierAttributes.size() > 0) {
              temp.put(0, this.identifierAttributes.get(0));
            }
          }
          
          this.subjectIdentifierAttributes = temp;
        }
      }
    }
    
    return this.subjectIdentifierAttributes;
  }
}
