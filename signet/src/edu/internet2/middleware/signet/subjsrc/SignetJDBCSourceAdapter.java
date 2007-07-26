/*--
	$Header $

	Copyright 2007 Internet2 and Stanford University.  All Rights Reserved.
	See doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet.subjsrc;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.subject.provider.JDBCSourceAdapter;


/**
 * Signet's JDBC Source. The purpose of this class is to override 
 * the SubjectAPI's JDBCSourceAdapter.loadAttributes(). Signet supports
 * multiple values for a given attribute name. Therefore, two queries are
 * needed to fully retrieve a Subject.
 */
public class SignetJDBCSourceAdapter extends JDBCSourceAdapter implements Serializable
{
	protected static final String NAME_FLD = "name";
	protected static final String VALUE_FLD = "value";
	protected static final String SUBJID_FLD = "subjectID";
	protected static final String INSTANCE_FLD = "instance";
	protected static final String ATTR_TABLE = "SubjectAttribute";
	protected static final String SUBJ_ATTR_SQL =
						"SELECT sa." + NAME_FLD + ", " +
						"sa." + VALUE_FLD + " " +
						"FROM " + ATTR_TABLE + " sa " +
						"WHERE sa." + SUBJID_FLD + " = ?" +
						" ORDER BY sa." + NAME_FLD + ", sa." + INSTANCE_FLD;

	protected Log	log;


	/** default constructor */
	public SignetJDBCSourceAdapter()
	{
		super();
		log	= LogFactory.getLog(SignetJDBCSourceAdapter.class);
	}

	///////////////////////////////////////
	// overrides subject.JDBCSourceAdapter
	///////////////////////////////////////

	/**
	 * Overrides JDBCSourceAdapter#search(java.lang.String), but provides the
	 * capability to use wildcards in the search. All instances of '*' are
	 * replaced by '%' and all instances of '.' are replaced by '_'. This
	 * allows a regex-style input to be converted to a SQL-style LIKE expression.
	 * @see edu.internet2.middleware.subject.provider.JDBCSourceAdapter#search(java.lang.String)
	 */
	public Set search(String searchValue)
	{
		if (null != searchValue)
		{
			searchValue = searchValue.replace('*', '%');
			searchValue = searchValue.replace('.', '_');
		}
		return (super.search(searchValue));
	}

	/**
	 * Loads attributes for the argument subject.
	 * @param rs The ResultSet from the previous 'select subject...'
	 * @return A Map of HashSets, each HashSet contains one or more instances of
	 * a given attribute. 
	 * @see edu.internet2.middleware.subject.provider.JDBCSourceAdapter#loadAttributes(java.sql.ResultSet)
	 */
	protected Map<String, HashSet<String>> loadAttributes(ResultSet rs)
	{
		Map<String, HashSet<String>> attributes = new HashMap<String, HashSet<String>>();
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = null;
		try
		{
			conn = dataSource.getConnection();
			sql = SUBJ_ATTR_SQL;
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, rs.getString(getSubjectIDAttributeName()));

			stmt.execute();

			ResultSet attr_rs = stmt.getResultSet();
			while (attr_rs.next())
			{
				String attr_name = attr_rs.getString(NAME_FLD);
				String attr_value = attr_rs.getString(VALUE_FLD);
				HashSet<String> values = attributes.get(attr_name);
				if (null == values)
				{
					values = new HashSet<String>();
					attributes.put(attr_name, values);
				}
				values.add(attr_value);
			}
		}
		catch (SQLException ex)
		{
			log.error("SignetJDBCSourceAdapter: SQLException occurred: " +
					ex.getMessage() + "   SQL String was: \"" + sql + "\"",
					ex);
		}
		finally
		{
			closeStatement(stmt);
			closeConnection(conn);
		}

		return attributes;
	}

}
