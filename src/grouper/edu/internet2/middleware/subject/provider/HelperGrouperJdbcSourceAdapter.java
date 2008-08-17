/*
 * @author mchyzer
 * $Id: HelperGrouperJdbcSourceAdapter.java,v 1.1 2008-08-17 15:33:02 mchyzer Exp $
 */
package edu.internet2.middleware.subject.provider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPoolFactory;

import edu.internet2.middleware.grouper.subj.GrouperJdbcSubject;
import edu.internet2.middleware.subject.InvalidQueryException;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.JDBCSourceAdapter;
import edu.internet2.middleware.subject.provider.Search;


/**
 * Some methods in subject api have package security, so this class is a temporary
 * measure to get access to them
 */
public class HelperGrouperJdbcSourceAdapter extends BaseSourceAdapter {

  private static Log log = LogFactory.getLog(JDBCSourceAdapter.class);
  
  protected String nameAttributeName;
  protected String subjectIDAttributeName;
  protected String descriptionAttributeName;
  protected String subjectTypeString;

  protected DataSource dataSource;

    /**
     * Allocates new JDBCSourceAdapter;
     */
    public HelperGrouperJdbcSourceAdapter() {
        super();
    }
    
    /**
     * Allocates new JDBCSourceAdapter;
     * 
     * @param id
     * @param name
     */
    public HelperGrouperJdbcSourceAdapter(String id, String name) {
        super(id, name);
    }
    
    /**
     * {@inheritDoc}
     */
    public Subject getSubject(String id)
    throws SubjectNotFoundException,SubjectNotUniqueException {
        return uniqueSearch(id, "searchSubject");
    }
    
    /**
     * {@inheritDoc}
     */
    public Subject getSubjectByIdentifier(String id)
    throws SubjectNotFoundException, SubjectNotUniqueException  {
        return uniqueSearch(id, "searchSubjectByIdentifier");
    }
    
    /**
     * Perform a search for a unique subject.
     * 
     * @param id
     * @param searchType
     * @return
     * @throws SubjectNotFoundException
     * @throws SubjectNotUniqueException
     */
    private Subject uniqueSearch(String id, String searchType)
    throws SubjectNotFoundException, SubjectNotUniqueException  {
        Subject subject = null;
        Search search = getSearch(searchType);
        if (search == null) {
            log.error("searchType: \"" + searchType + "\" not defined.");
            return subject;
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.dataSource.getConnection();
            stmt = prepareStatement(search, conn);
            ResultSet rs = getSqlResults(id, stmt, search);
            subject = createUniqueSubject(rs, search,  id, search.getParam("sql"));
        } catch (SubjectNotFoundException snfe) {
          throw snfe;
        } catch (SubjectNotUniqueException snue) {
          throw snue;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() + ", source: " + this.getId() + ", sql: " + search.getParam("sql"), e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
        if (subject == null) {
            throw new SubjectNotFoundException("Subject " + id + " not found.");
        }
        return subject;
    }

    /**
     * {@inheritDoc}
     */
    public Set search(String searchValue) {
        Set result = new HashSet();
        Search search = getSearch("search");
        if (search == null) {
            log.error("searchType: \"search\" not defined.");
            return result;
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.dataSource.getConnection();
            stmt = prepareStatement(search, conn);
            ResultSet rs = getSqlResults(searchValue, stmt, search);
            if (rs==null) {
                return result;
            }
            while (rs.next()) {
                Subject subject = createSubject(rs, search.getParam("sql"));
                result.add(subject);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage() + ", source: " + this.getId() + ", sql: " + search.getParam("sql"), ex);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
        return result;
    }
    
    /**
     * Create a subject from the current row in the resultSet
     * 
     * @param rs
     * @return
     */
    private Subject createSubject(ResultSet rs, String sql) throws SQLException {
        String name = "";
        String subjectID = "";
        String description = "";
        GrouperJdbcSubject subject = null;
        subjectID = retrieveString(rs, subjectIDAttributeName, "SubjectID_AttributeType", sql);
        name = retrieveString(rs, nameAttributeName, "Name_AttributeType", sql);
        if (!descriptionAttributeName.equals("")) {
            description = retrieveString(rs, descriptionAttributeName, "Description_AttributeType", sql);
        }
        Map attributes = loadAttributes(rs);
        subject = new GrouperJdbcSubject(subjectID,name, description, this.getSubjectType(), this, attributes);
        return subject;
    }
    
    /**
     * retrieve a param from resultset
     * @param rs
     * @param name
     * @param sql
     * @return the value
     */
    private String retrieveString(ResultSet rs, String name, String varName, String sql) throws SQLException {
      try {
        String result = rs.getString(name);
        return result;
      } catch (SQLException se) {
        //TODO inject error message
        log.error("Error retrieving column name: '" + name + "' in source: " + this.getId() 
            + ", in query: " + sql + ", " + se.getMessage() 
            + ", maybe the column configured in " + varName + " does not exist as a query column");
        throw se;

      }
    }
    
    /**
     * Create a unique subject from the resultSet.
     * 
     * @param rs
     * @param search
     * @param searchValue
     * @return
     * @throws SubjectNotFoundException
     * @throws SubjectNotUniqueException
     */
    private Subject createUniqueSubject(ResultSet rs, Search search, String searchValue, String sql)
    throws SubjectNotFoundException,SubjectNotUniqueException, SQLException {
        Subject subject =null;
        if (rs == null || !rs.next()) {
            String errMsg = "No results: " + search.getSearchType() +
                    " searchValue: " + searchValue;
            throw new SubjectNotFoundException( errMsg);
        }
        subject = createSubject(rs, sql);
        if (rs.next()) {
            String errMsg ="Search is not unique:" + rs.getString(subjectIDAttributeName) + "\n";
            throw new SubjectNotUniqueException( errMsg );
        }
        return subject;
        
    }
    
    /**
     * Prepare a statement handle from the search object.
     * 
     * @param search
     * @param conn
     * @return
     * @throws InvalidQueryException
     * @throws SQLException
     */
    protected PreparedStatement prepareStatement(Search search, Connection conn)
    throws InvalidQueryException, SQLException {
        String sql = search.getParam("sql");
        if (sql == null) {
          throw new InvalidQueryException("No sql parameter for search type " + search.getSearchType() + ", source: " + this.getId());
        }
        if (sql.contains("%TERM%")) {
          throw new InvalidQueryException("%TERM%. Possibly old style SQL query, source: " + this.getId() + ", sql: " + sql);
        }
        if (search.getParam("numParameters") == null) {
            throw new InvalidQueryException("No numParameters parameter specified, source: " + this.getId() + ", sql: " + sql);
        }
        try {
            Integer.parseInt(search.getParam("numParameters"));
        } catch (NumberFormatException e) {
            throw new InvalidQueryException("Non-numeric numParameters parameter specified, source: " + this.getId() + ", sql: " + sql);
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
    protected ResultSet getSqlResults(String searchValue, PreparedStatement stmt, Search search) 
           throws SQLException {
        ResultSet rs = null;
        for (int i = 1; i <= Integer.parseInt(search.getParam("numParameters")); i++) {
          try {
            stmt.setString(i, searchValue);
          } catch (SQLException e) {
            //TODO inject error message, remove dupe if caught in parent... (dupe query)
            log.error("Error setting param: " + i + " in source: " + this.getId() + ", in query: " + search.getParam("sql") + ", " + e.getMessage() 
                + ", maybe not enough question marks (bind variables) are in query, or the param 'numParameters' in sources.xml for that query is incorrect");
            throw e;
          }
        }
        rs = stmt.executeQuery();
        return rs;
    }

    /**
     * Loads attributes for the argument subject.
     */
    protected Map loadAttributes(ResultSet rs) throws SQLException {
        Map attributes = new HashMap();
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCount = rsmd.getColumnCount();
        String[] colNames = new String[colCount];
        for (int i=0; i < colCount; i++) {
            colNames[i] = rsmd.getColumnName(i+1);
        }
        for (int i=0; i < colCount; i++ ) {
            String name = colNames[i];
            if (name.toLowerCase().equals(subjectIDAttributeName.toLowerCase())) {
                continue;
            }
            if (name.toLowerCase().equals(nameAttributeName.toLowerCase())) {
                continue;
            }
            if (name.toLowerCase().equals(descriptionAttributeName.toLowerCase())){
                continue;
            }
            String value = rs.getString(i+1);
            Set values = (Set)attributes.get(name);
            if (values == null) {
                values = new HashSet();
                attributes.put(name, values);
            }
            values.add(value);
        }

        return attributes;
    }
    
    /**
     * {@inheritDoc}
     */
    public void init()
    throws SourceUnavailableException {
        try {
            Properties props = getInitParams();
            String driver = props.getProperty("dbDriver");
            loadDriver(driver);
            setupDataSource(props);
        } catch (Exception ex) {
            throw new SourceUnavailableException(
                    "Unable to init JDBC source, source: " + this.getId(), ex);
        }
    }
    
    /**
     * Loads the the JDBC driver.
     */
    protected void loadDriver(String driver)
    throws SourceUnavailableException {
        try {
            Class.forName(driver).newInstance();
            log.debug("Loading JDBC driver: " + driver);
        } catch (Exception ex) {
            throw new SourceUnavailableException(
                    "Error loading JDBC driver: " + driver + ", source: " + this.getId(), ex);
        }
        log.info("JDBC driver loaded.");
    }
    
    /**
     * DataSource connection pool setup.
     * @throws SourceUnavailableException
     */
    protected void setupDataSource(Properties props)
    throws SourceUnavailableException {
        
        GenericObjectPool objectPool = new GenericObjectPool(null);
        int maxActive = Integer.parseInt(props.getProperty("maxActive", "2"));
        objectPool.setMaxActive(maxActive);
        int maxIdle = Integer.parseInt(props.getProperty("maxIdle", "2"));
        objectPool.setMaxIdle(maxIdle);
        int maxWait = 1000 * Integer.parseInt(props.getProperty("maxWait", "5"));
        objectPool.setMaxWait(maxWait);
        objectPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
        
        ConnectionFactory connFactory = null;
        PoolableConnectionFactory poolConnFactory = null;
        String dbUrl = null;
        try {
            log.debug("Initializing connection factory.");
            dbUrl = props.getProperty("dbUrl");
            String dbUser = props.getProperty("dbUser");
            String dbPwd = props.getProperty("dbPwd");
            connFactory = new DriverManagerConnectionFactory(dbUrl, dbUser, dbPwd);
            log.debug("Connection factory initialized.");
        } catch (Exception ex) {
            throw new SourceUnavailableException(
                    "Error initializing connection factory: " + dbUrl + ", source: " + this.getId(), ex);
        }
        
        try {
            boolean readOnly =
                    "true".equals(props.getProperty("readOnly", "true"));
            // StackKeyedObjectPoolFactory supports PreparedStatement pooling.
            poolConnFactory = new PoolableConnectionFactory(
                    connFactory,
                    objectPool,
                    new StackKeyedObjectPoolFactory(),
                    null,
                    readOnly,
                    true);
        } catch (Exception ex) {
            throw new SourceUnavailableException(
                    "Error initializing poolable connection factory, source: " + this.getId(), ex);
        }
        this.dataSource = new PoolingDataSource(objectPool);
        log.info("Data Source initialized.");
        nameAttributeName = props.getProperty("Name_AttributeType");
        if (nameAttributeName==null) {
            throw new SourceUnavailableException("Name_AttributeType not defined, source: " + this.getId());
        }
        subjectIDAttributeName = props.getProperty("SubjectID_AttributeType");
        if (subjectIDAttributeName==null) {
          throw new SourceUnavailableException("SubjectID_AttributeType not defined, source: " + this.getId());
        }
        descriptionAttributeName = props.getProperty("Description_AttributeType");
        if (descriptionAttributeName==null) {
          throw new SourceUnavailableException("Description_AttributeType not defined, source: " + this.getId());
        }
    }
    
    protected void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                log.info("Error while closing JDBC Connection.");
            }
        }
    }
    
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
    return descriptionAttributeName;
  }

  /**
   * @return the nameAttributeName
   */
  public String getNameAttributeName() {
    return nameAttributeName;
  }

  /**
   * @return the subjectIDAttributeName
   */
  public String getSubjectIDAttributeName() {
    return subjectIDAttributeName;
  }

  /**
   * @return the subjectTypeString
   */
  public String getSubjectTypeString() {
    return subjectTypeString;
  }

  
  
}
