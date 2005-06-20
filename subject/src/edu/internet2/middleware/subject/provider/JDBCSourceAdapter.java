/*--
$Id: JDBCSourceAdapter.java,v 1.2 2005-06-20 14:49:52 mnguyen Exp $
$Date: 2005-06-20 14:49:52 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
*/
package edu.internet2.middleware.subject.provider;

import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPoolFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;


/**
 * JDBC Source which uses the Subject and SubjectAttribute database
 * tables for the repository.<p>
 * 
 * This class assumes the following schema.  The column lengths below
 * are only examples. This class is not limited by the listed lengths.<p>
 * 
 * table Subject<br>
 * (<br>
 * subjectID         varchar(64)     NOT NULL,<br>
 * subjectTypeID     varchar(32)     NOT NULL,<br>
 * name              varchar(255)    NOT NULL,<br>
 * primary key (subjectID)<br>
 * )<br>
 * <p>
 * table SubjectAttribute<br>
 * (<br>
 * subjectID         varchar(64)     NOT NULL,<br>
 * name              varchar(255)     NOT NULL,<br>
 * value             varchar(255)    NOT NULL,<br>
 * searchValue       varchar(255)    NULL,<br>
 * primary key (subjectID, name, value),<br>
 * foreign key (subjectID) references Subject (subjectID)<br>
 * )<br>
 * 
 */
public class JDBCSourceAdapter
	extends BaseSourceAdapter {

	private static Log log = LogFactory.getLog(JDBCSourceAdapter.class);
	
	private static final String SEARCH_SUBJ_SQL =
		"SELECT DISTINCT Subject.subjectID, Subject.name, Subject.subjectTypeID" +
			" FROM Subject, SubjectAttribute" +
			" WHERE Subject.subjectID = SubjectAttribute.subjectID" +
			" AND (SubjectAttribute.searchValue = ?" +
			" OR SubjectAttribute.searchValue LIKE ?)";
	
	private static final String SEARCH_SUBJ_BY_ID_SQL =
		"SELECT DISTINCT Subject.subjectID, Subject.name, Subject.subjectTypeID" +
			" FROM Subject, SubjectAttribute" +
			" WHERE Subject.subjectID = SubjectAttribute.subjectID" +
			" AND SubjectAttribute.name = 'loginid'" +
			" AND SubjectAttribute.value = ?";
	
	private static final String LOAD_SUBJECT_SQL =
		"SELECT subjectID, name, subjectTypeID" +
			" FROM Subject" +
			" WHERE subjectID = ?";
	
	private static final String LOAD_ATTRIBUTES_SQL =
		"SELECT DISTINCT name, value" +
			" FROM SubjectAttribute" +
			" WHERE subjectID = ?";

	private DataSource dataSource;
	
	/**
	 * Allocates new JDBCSourceAdapter;
	 */
	public JDBCSourceAdapter() {
		super();
	}
	
	/**
	 * Allocates new JDBCSourceAdapter;
	 * @param id
	 * @param name
	 */
	public JDBCSourceAdapter(String id, String name) {
		super(id, name);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Subject getSubject(String id)
		throws SubjectNotFoundException {
		Subject subject = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.prepareStatement(LOAD_SUBJECT_SQL);
			stmt.setString(1, id);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				SubjectType type = SubjectTypeEnum.valueOf(rs.getString("subjectTypeID"));
				subject = new JDBCSubject(id, name, type, this);
			}
		}
		catch (SQLException ex) {
			log.debug("SQLException occurred: " + ex.getMessage(), ex);
		}
		finally {
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
	public Subject getSubjectByIdentifier(String id)
		throws SubjectNotFoundException {
		Subject subject = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.prepareStatement(SEARCH_SUBJ_BY_ID_SQL);
			stmt.setString(1, id);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String subjectId = rs.getString("subjectID");
				String name = rs.getString("name");
				SubjectType type = SubjectTypeEnum.valueOf(rs.getString("subjectTypeID"));
				subject = new JDBCSubject(subjectId, name, type, this);
			}
		}
		catch (SQLException ex) {
			log.debug("SQLException occurred: " + ex.getMessage(), ex);
		}
		finally {
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
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.prepareStatement(SEARCH_SUBJ_SQL);
			stmt.setString(1, normalizeString(searchValue));
			stmt.setString(2, normalizeName(searchValue));
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String id = rs.getString("subjectID");
				String name = rs.getString("name");
				SubjectType type = SubjectTypeEnum.valueOf(rs.getString("subjectTypeID"));
				Subject subject = new JDBCSubject(id, name, type, this);
				result.add(subject);
			}
		}
		catch (SQLException ex) {
			log.debug("SQLException occurred: " + ex.getMessage(), ex);
		}
		finally {
			closeStatement(stmt);
			closeConnection(conn);
		}
		return result;
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
	    		"Unable to init JDBC source", ex);
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
			log.error("Error loading JDBC driver: " + ex.getMessage(), ex);
			throw new SourceUnavailableException(
				"Error loading JDBC driver", ex);
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
		int maxActive = Integer.parseInt((String)props.getProperty("maxActive", "2"));
		objectPool.setMaxActive(maxActive);
		int maxIdle = Integer.parseInt((String)props.getProperty("maxIdle", "2"));
		objectPool.setMaxIdle(maxIdle);
		int maxWait = 1000 * Integer.parseInt((String)props.getProperty("maxWait", "5"));
		objectPool.setMaxWait(maxWait);
		objectPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);

		ConnectionFactory connFactory = null;
		PoolableConnectionFactory poolConnFactory = null;

		try {
			log.debug("Initializing connection factory.");
			String dbUrl = props.getProperty("dbUrl");
			String dbUser = props.getProperty("dbUser");
			String dbPwd = props.getProperty("dbPwd");
			connFactory = new DriverManagerConnectionFactory(dbUrl, dbUser, dbPwd);
			log.debug("Connection factory initialized.");
		} catch (Exception ex) {
			log.error(
				"Error initializing connection factory: " + ex.getMessage(), ex);
			throw new SourceUnavailableException(
				"Error initializing connection factory", ex);
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
			log.error(
				"Error initializing poolable connection factory: " + ex.getMessage(), ex);
			throw new SourceUnavailableException(
				"Error initializing poolable connection factory", ex);
		}
		this.dataSource = new PoolingDataSource(objectPool);
		log.info("Data Source initialized.");
	}

	
	/**
     * Normalize a value for searching.  All non-alpha-numeric are converted
     * to a space except for apostrophes, which are elided.
     */
	protected static String normalizeString(String value) {
		if (value == null) {
			return null;
		}
		char[] chars = value.trim().toLowerCase().toCharArray();
		StringBuffer buf = new StringBuffer();

		boolean lastCharacterIsSpace = false;
		for (int i = 0; i < chars.length; ++i) {
			if (Character.isLetterOrDigit(chars[i])) {
				buf.append(chars[i]);
				lastCharacterIsSpace = false;
			} else if (chars[i] == '\'') {
				continue; // elide apostrophes
			} else if (! lastCharacterIsSpace) {
				//change any non-alpha, non-numeric to a space.
				buf.append(' ');
				lastCharacterIsSpace = true;
			}
        }
		//trim the leading/trailing whitespace
		return buf.toString().trim();
	}

	/**
     * Normalize a name for searching.  If the value
     * is multi-part (delimited by spaces), the last word
     * is moved to the beginning and the other words are
     * appended with '% '.  All non-alpha-numeric
     * are converted to a space except for apostrophes which
     * are removed.
     */
	protected static String normalizeName(String value) {
		if (value == null) {
            return null;
        }
		StringBuffer buf = new StringBuffer();
		
		String[] words = value.trim().split("\\s");
		if (words.length > 1) {
			// No comma, move last word to first.
			if (value.indexOf(",") == -1) {
				// Get last element.
				String lastElement = words[words.length - 1];
				// Shift elements to the right by one, except for last one.
				System.arraycopy(words, 0, words, 1, words.length - 1);
				// Set last element to beginning.
				words[0] = lastElement;
			}
			buf.append(normalizeString(words[0]));
        	buf.append(" ");
        	for (int i = 1; i < words.length; i++) {
        		String str = words[i];
        		buf.append(normalizeString(str));
        		buf.append("%");
        		if (i < words.length - 1) {
    				buf.append(" ");
        		}
        		log.debug(i + "-" + buf.toString());
        	}        	
        } else {
        	buf.append(normalizeString(words[0]));
        	buf.append(" %");
        }
        if (log.isDebugEnabled()) {
        	log.debug("Normalized name from '" + value +
        			"' to '" + buf.toString() + "'");
        }
        return buf.toString();
	}
	
	/**
	 * Loads attributes for the argument subject.
	 */
	protected Map loadAttributes(JDBCSubject subject) {
		Map attributes = new HashMap();
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.prepareStatement(LOAD_ATTRIBUTES_SQL);
			stmt.setString(1, subject.getId());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				String value = rs.getString("value");
				Set values = (Set)attributes.get(name);
				if (values == null) {
					values = new HashSet();
					attributes.put(name, values);
				}
				values.add(value);
			}
			subject.setAttributes(attributes);
		}
		catch (SQLException ex) {
			log.debug("SQLException occurred: " + ex.getMessage(), ex);
		}
		finally {
			closeStatement(stmt);
			closeConnection(conn);
		}
		return attributes;
	}
	
	private void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			}
			catch (SQLException ex) {
				log.info("Error while closing JDBC Connection.");
			}
		}
	}
	
	private void closeStatement(PreparedStatement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			}
			catch (SQLException ex) {
				log.info("Error while closing JDBC Statement.");
			}
		}
	}
}
