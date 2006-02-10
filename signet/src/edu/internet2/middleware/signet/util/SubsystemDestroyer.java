/*
SubsystemXmlDestroyer.java
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
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

public class SubsystemDestroyer {

	private static SessionFactory sessionFactory;
    private Session session;

    private String subsystemId = null;
    
    private String[] statements = new String[] {
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
    
    private String[] tables = new String[] {
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
	
    static
    /* runs at class load time */
    {
        Configuration cfg = new Configuration();

        try {
            // Read the "hibernate.cfg.xml" file.
            cfg.configure();
            sessionFactory = cfg.buildSessionFactory();
        }
        catch (HibernateException he) {
            throw new RuntimeException(he);
        }

    }
    
    /**
     *
     *
     */
    public SubsystemDestroyer(String subsystemId) {
        try {
		    this.session = sessionFactory.openSession();
		    this.subsystemId = subsystemId;
		}
		catch (HibernateException he) {
		    throw new RuntimeException(he);
        }
    }

	/*
	 * Deletes all Subsystem metadata and associated assignments.
	 *
	 */
	public void execute()
	    throws HibernateException, SQLException {
	    
	    Connection conn = this.session.connection();
	    
	    try {
	        //conn.setAutoCommit(true);
	        for (int i = 0; i < this.statements.length; i++) {
	        	execute(conn, this.statements[i], this.tables[i]);
	    	}
	    	conn.commit();
	    }
	    catch (SQLException ex) {
	        conn.rollback();
	    	System.out.println("SQL error occurred: " + ex.getMessage());
	    }
	    finally {
	    	conn.close();
	    }

	}
	
	private void execute(Connection conn, String sql, String table)
		throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, this.subsystemId);
		int rows = ps.executeUpdate();
	    System.out.println("Delete from " + table + " -- " + rows + " rows affected");
	}
	
    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.err.println("Usage: SubsystemDestroyer <subsystem id>");
                return;
            }
			String subsystemId = args[0];
            SubsystemDestroyer processor = new SubsystemDestroyer(subsystemId);
            processor.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
