/*
  Copyright 2006-2007 The University Of Chicago
  Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006-2007 EDUCAUSE
  
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

package edu.internet2.middleware.ldappcTest;

import junit.framework.TestCase;

import edu.internet2.middleware.ldappc.util.ResourceBundleUtil;
import edu.internet2.middleware.ldappcTest.AllJUnitTests;
import edu.internet2.middleware.ldappcTest.DisplayTest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class for making sure a connection can be make to the Hsql database.
 * This does not use any of the non-test classes.
 * @author Gil Singer 
 */
public class HsqlConnectionTest extends BaseTestCase
{
    /**
     * database connection
     */
    Connection conn;

    /**
     * Constructor
     */
    public HsqlConnectionTest(String name) 
    {
        super(name);
    }
    
    /**
     * Setup the fixture.
     */
    protected void setUp() 
    {
        DisplayTest.showRunClass(getClass().getName());       
    }

    /**
     * Tear down the fixture.
     */
    protected void tearDown() 
    {
        //shutdown();
    }

    /**
     * The main method for running the test.
     */
    public static void main(String args[]) 
    {
        BaseTestCase.runTestRunner(HsqlConnectionTest.class);
    }
    
    /**
     * Shutdown Hsql DB
     */
    public void shutdown()
    {
        try
        { 
            Statement st = conn.createStatement();
            st.execute("SHUTDOWN");
            conn.close();    
        } 
        catch(SQLException se)
        {
            fail("Connection shutdown failure.  " + se.getMessage());
        } 
    }

    /**
     * A test of LDAP search capability.
     */
    public void testHsqlConnection() 
    {
        DisplayTest.showRunTitle("testHsqlConnection", "Connection to HSQL.");

        //
        // Load the HSQL Database Engine JDBC driver
        // hsqldb.jar should be in the class path or made part of the current jar
        //

        String jdbcDriverClass = null; 
        try
        { 
            jdbcDriverClass = ResourceBundleUtil.getString("jdbcDriver");
            Class.forName(jdbcDriverClass);
        } 
        catch(ClassNotFoundException cnfe)
        {
            fail("Could not find " + jdbcDriverClass + "   " + cnfe.getMessage());
        } 
        String dbUrl = null;
        try
        {
            // Connect to the database.   This will load the db files and start the
            // database if it is not already running.
            // It can contain directory names relative to the
            // current working directory
            //
            // The following gives a socket creation error
            // when netstat shows port 9001 in CLOSE_WAIT state but does not give an 
            // error when it shows in the ESTABLISHED state (i.e. make sure that the
            // Hsql server is running before running this test.)
            //

            dbUrl = ResourceBundleUtil.getString("signetDbUrl");
        
            //                                                         xdb = filename   username  pw 
            //conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:9001/xdb", "sa",    "");
            // or
            //conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:9001/xdb", "sa",    "");

            conn = DriverManager.getConnection(dbUrl, "sa",    "");
              
            //
            // Examples of bad connection parameters and the errors create by them follow.
            // These notes can be used to write unit tests to verify failure messages
            // if that is deemed worth the effort. 
            //
            // The following two conn= lines would not detect any error from test even though 
            // database filename prefix QQQ does not exist.
            // Therefore, without executing a query statement, this is would not be a good test 
            // of anything except finding the driver;
            // it does not determine if the file exists in the database.
            //     conn = DriverManager.getConnection("jdbc:hsqldb:QQQ", "sa",    "");
            //     conn = DriverManager.getConnection("jdbc:hsqldb:file:xdb", "sa",    "");
            //
            // The following gives a "SQLException getting driver.  No suitable driver" error.
            //     conn = DriverManager.getConnection("jdbc:hsqldbXXX:QQQ", "sa",    "");
            //
            //
            // The following line creates an error:
            //    conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:9001/mydb", "sa",    "");
            // The error: "SQLException getting driver.  Database does not exists in statement [mydb]"
            //

            Statement st = conn.createStatement();
            // The following checks that a successful connect really was made.
            //ResultSet rs = st.executeQuery("select * from Subject");
            //
            // Handle different test databases.
            //
            // Windows databases and the built-in test database on biofix have a SUBJECT table
            // while the UC test database on biofix has an INDIVIDUAL table.
            //

            String subjectOrIndividual = null;
            subjectOrIndividual = AllJUnitTests.SUBJECT_QUERY;
            //
            // Removing this logic to simplify the distribution.
            // This means that the built-in test cases will no longer run with the
            // biofix UC data.  Restore this if needed.
            //
            /*
            String testCaseSet = TestOptions.getTestCaseSet();
            // Use subject query for all except biofix UC.
            if (        AllJUnitTests.TEST_CASE_WINDOWS_TEST.equals(testCaseSet)
                    ||  AllJUnitTests.TEST_CASE_WINDOWS_QS.equals(testCaseSet)
                    ||  AllJUnitTests.TEST_CASE_BIOFIX_TEST.equals(testCaseSet) )
            {
                subjectOrIndividual = AllJUnitTests.SUBJECT_QUERY;
            }
            else if (AllJUnitTests.TEST_CASE_BIOFIX_UC.equals(testCaseSet) )
            {
                subjectOrIndividual = AllJUnitTests.INDIVIDUAL_QUERY;
            }
            else
            {
                fail("testCaseSet not recognized: " + testCaseSet);
            }
            */

            //ResultSet rs = st.executeQuery("select * from Subject");
            // or
            //ResultSet rs = st.executeQuery("select * from individual");
              
            ResultSet rs = st.executeQuery("select * from " + subjectOrIndividual);
        }
        catch(SQLException se)
        {
            fail("SQLException getting driver: " + dbUrl + "   " + se.getMessage());
        } 
    }
}
        



