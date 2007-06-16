/*--
	$Header $
	 
	Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
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
 * the SubjectAPI's JDBCSourceAdapter.loadAttributes().
 */
public class SignetJDBCSourceAdapter extends JDBCSourceAdapter implements Serializable
{
	protected Log	log	= LogFactory.getLog(SignetJDBCSourceAdapter.class);

	/** default constructor */
	public SignetJDBCSourceAdapter()
	{
		super();
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
	 */
	protected Map loadAttributes(ResultSet rs)
	{
		Map attributes = new HashMap();
		Connection conn = null;
		PreparedStatement stmt = null;
		try
		{
			conn = dataSource.getConnection();
			String sql = "SELECT sa.name, sa.value " +
						"FROM SubjectAttribute sa " +
						"WHERE sa.subjectID = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, rs.getString(getSubjectIDAttributeName()));

			stmt.execute();

			ResultSet attr_rs = stmt.getResultSet();
			while (attr_rs.next())
			{
				String attr_name = attr_rs.getString("name");
				String attr_value = attr_rs.getString("value");
				Set values = (Set)attributes.get(attr_name);
				if (values == null)
				{
					values = new HashSet();
					attributes.put(attr_name, values);
				}
				values.add(attr_value);
			}
		}
		catch (SQLException ex)
		{
			log.debug("SignetJDBCSourceAdapter: SQLException occurred: " + ex.getMessage(), ex);
		}
		finally
		{
			closeStatement(stmt);
			closeConnection(conn);
		}
		return attributes;
	}

}
