/*
SubsystemXmlDestroyer.java
Created on Feb 22, 2005

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.internet2.middleware.signet.*;
import edu.internet2.middleware.signet.choice.*;
import edu.internet2.middleware.signet.tree.*;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;

public class SubsystemDestroyer {

	private static SessionFactory sessionFactory;
    private Session session;

    private String subsystemId = null;
    
    private String[] statements = new String[] {
    	"delete Choice from Choice, ChoiceSet where Choice.choiceSetID = ChoiceSet.choiceSetID and ChoiceSet.subsystemID = ?",
		"delete from ChoiceSet           where subsystemID = ?",
		"delete from Function_Permission where subsystemID = ?",
		"delete from Permission_Limit    where subsystemID = ?",
		"delete from Permission          where subsystemID = ?",
		"delete from Limit               where subsystemID = ?",
		"delete from Function            where subsystemID = ?",
		"delete from Category            where subsystemID = ?",
		"delete from Subsystem           where subsystemID = ?",
		"delete from Assignment          where subsystemID = ?"
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
	private void execute()
	    throws HibernateException, SQLException {
	    
	    Connection conn = this.session.connection();
	    
	    try {
	        //conn.setAutoCommit(true);
	        for (int i = 0; i < this.statements.length; i++) {
	        	execute(conn, this.statements[i]);
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
	
	private void execute(Connection conn, String sql)
		throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, this.subsystemId);
		int rows = ps.executeUpdate();
	    System.out.println("Number of rows deleted: " + rows);
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
