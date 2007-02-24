/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/SubsystemDestroyer.java,v 1.9 2007-02-24 02:11:32 ddonn Exp $
Created on Feb 22, 2005

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package edu.internet2.middleware.signet.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;

/**
 * @version $Revision: 1.9 $
 * @author lmcrae
 *
 */
public class SubsystemDestroyer
{
    private static final String[] statements = new String[] {
        "delete from signet_assignmentLimit_history  where assignment_historyID in (select signet_assignment_history.historyID from signet_assignment_history, signet_assignment, signet_function where signet_assignment_history.assignmentID = signet_assignment.assignmentID and signet_assignment.functionKey = signet_function.functionKey and signet_function.subsystemID=?)",
        "delete from signet_assignmentLimit     where assignmentID in (select signet_assignment.assignmentID from signet_assignment, signet_function where signet_assignment.functionKey=signet_function.functionKey and signet_function.subsystemID=?)",
        "delete from signet_assignment_history  where assignmentID in (select signet_assignment.assignmentID from signet_assignment, signet_function where signet_assignment.functionKey = signet_function.functionKey and signet_function.subsystemID=?)",
        "delete from signet_assignment          where functionKey in (select signet_function.functionKey from signet_function where signet_function.subsystemID = ?)",
        "delete from signet_proxy_history       where proxyID in (select signet_proxy.proxyID from signet_proxy where signet_proxy.subsystemID=?)",
        "delete from signet_proxy               where subsystemID=?",
    	"delete from signet_choice              where signet_choice.choiceSetKey in (select signet_choiceSet.choiceSetKey from signet_choiceSet where signet_choiceSet.subsystemID = ?)",
		"delete from signet_choiceSet           where subsystemID = ?",
		"delete from signet_function_permission where signet_function_permission.functionKey in (select signet_function.functionKey from signet_function where signet_function.subsystemID=?)",
		"delete from signet_permission_limit    where signet_permission_limit.permissionKey in (select signet_permission.permissionKey from signet_permission where subsystemID = ?)",
		"delete from signet_permission          where subsystemID = ?",
        "delete from signet_assignmentLimit_history  where assignment_historyID in (select signet_assignment_history.historyID from signet_assignment_history, signet_assignment, signet_function where signet_assignment_history.assignmentID = signet_assignment.assignmentID and signet_assignment.functionKey = signet_function.functionKey and signet_function.subsystemID=?)",
        "delete from signet_assignmentLimit     where assignmentID in (select signet_assignment.assignmentID from signet_assignment, signet_function where signet_assignment.functionKey=signet_function.functionKey and signet_function.subsystemID=?)",
		"delete from signet_limit               where subsystemID = ?",
		"delete from signet_function            where subsystemID = ?",
		"delete from signet_category            where subsystemID = ?",
		"delete from signet_subsystem           where subsystemID = ?"
	};
    
    private static final String[] tables = new String[] {
        "signet_assignmentLimit_history",
        "signet_assignmentLimit",
        "signet_assignment_history",
        "signet_assignment",
        "signet_proxy_history",
        "signet_proxy",
    	"signet_choice",
		"signet_choiceSet",
		"signet_function_permission",
		"signet_permission_limit",
		"signet_permission",
        "signet_assignmentLimit_history",
        "signet_assignmentLimit",
		"signet_limit",
		"signet_function",
		"signet_category",
		"signet_subsystem"
	};
	

    /** default constructor */
	public SubsystemDestroyer()
	{
	}


	/**
	 * Deletes all Subsystem metadata and associated assignments.
	 * @param hibr
	 * @param subsystemId
	 * @throws HibernateException
	 * @throws SQLException
	 */
	public void execute(HibernateDB hibr, String subsystemId)
	    throws HibernateException, SQLException {

		Session hs = hibr.openSession();
		Connection conn = hs.connection();
	    try {
	        conn.setAutoCommit(true);
	        for (int i = 0; i < statements.length; i++) {
	        	execute(conn, statements[i], tables[i], subsystemId);
	    	}
	    	conn.commit();
	    }
	    catch (SQLException ex) {
	        conn.rollback();
	    	System.out.println("SQL error occurred: " + ex.getMessage());
	    }
	    finally
	    {
	    	conn.close();
	    	hibr.closeSession(hs);
	    }
	}
	
	/**
	 * @param conn
	 * @param sql
	 * @param table
	 * @param subsystemId
	 * @throws SQLException
	 */
	private void execute(Connection conn, String sql, String table, String subsystemId)
		throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, subsystemId);
		int rows = ps.executeUpdate();
	    System.out.println("Delete from " + table + " -- " + rows + " rows affected");
	}


    /**
     * note: this is untested as of 01/30/07
     * @param args
     */
    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.err.println("Usage: SubsystemDestroyer <subsystem id>");
                return;
            }
			String subsystemId = args[0];
            HibernateDB hibr = new HibernateDB(null); // untested, don't know whether passing null will work (DMD)
            SubsystemDestroyer processor = new SubsystemDestroyer();
            processor.execute(hibr, subsystemId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
