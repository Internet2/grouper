/*
 * $Id: GrouperTest.java,v 1.11 2004-07-15 01:48:20 blair Exp $
 */

package test.edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.io.BufferedReader;
import  java.io.File;
import  java.io.FileReader;
import  java.io.IOException;
import  java.lang.reflect.*;
import  java.sql.*;
import  junit.framework.*;

public class GrouperTest extends TestCase {

  public Grouper G;

  public GrouperTest(String name) {
    super(name);
  }

  protected void setUp () {
    Connection conn   = null;
    ResultSet  rs     = null;
    Statement  st     = null;
    String sqlFile    = "sql/hsqldb.sql";
    String sqlStr     = null;
    try {
      BufferedReader  br  = new BufferedReader(new FileReader(sqlFile));
      String          l   = null;
      while ((l=br.readLine()) != null){
        if (sqlStr != null) {
          sqlStr = sqlStr + l + "\n";
        } else {
          sqlStr = l + "\n";
        }
      }
      br.close();
      if (sqlStr != null) {
        try {
          Class.forName("org.hsqldb.jdbcDriver");
          conn = DriverManager.getConnection("jdbc:hsqldb:build/grouper", "sa", "");
          try {
            st = conn.createStatement();
            // XXX Ugh
            rs = st.executeQuery("DROP TABLE grouper_members IF EXISTS");
            rs = st.executeQuery("DROP TABLE grouper_session IF EXISTS");
            rs = st.executeQuery(sqlStr);
            st.close();
            conn.close();
          } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
          }
        } catch (Exception e) {
          System.err.println(e);
          System.exit(1);
        }
      } else {
        System.err.println("Unable to load SQL from '" + sqlFile + "'");
        System.exit(1);
      }
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
  }

  protected void tearDown () {
    // Destroy our Grouper instance
    G.destroy();
  }

  /* Instantiate a Grouper instance */
  public void testGrouperInstantiate() {
    // Establish a new Grouper instance
    G = new Grouper();

    Class  klass    = G.getClass();
    String expKlass = "edu.internet2.middleware.directory.grouper.Grouper";

    Assert.assertNotNull(G);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /* Initialize Grouper environment */
  public void testGrouperInitialize() {
    G = new Grouper();

    try {
      G.initialize();
    } catch(Exception e) {
      Assert.fail("Exception thrown when initializing Grouper");
    }
  }

  /* Get a runtime configuration setting */
  public void testGetRuntimeConfigSetting() {
    G = new Grouper();
    G.initialize();
    
    String expVal = "GrouperSystem";
    Assert.assertTrue( expVal.equals( G.config("member.system") ) );
  }

  /* Instantiate a Grouper session */
  public void testSessionInstantiate() {
    // Establish a new Grouper instance
    G = new Grouper();
    G.initialize();
    GrouperSession s = new GrouperSession();

    Class klass     = s.getClass();
    String expKlass = "edu.internet2.middleware.directory.grouper.GrouperSession";

    Assert.assertNotNull(s);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /* Start a session as SubjectID "member.system", 1 argument method */
  public void testSessionStartAsMemberSystemOneArgMethod() {
    G = new Grouper();
    G.initialize();
    GrouperSession s = new GrouperSession();
    try {
      s.start( G, G.config("member.system") );
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
  }
  
  /* Start a session as SubjectID "member.system", 2 argument method */
  public void testSessionStartAsMemberSystemTwoArgMethod() {
    G = new Grouper();
    G.initialize();
    GrouperSession s= new GrouperSession();
    try {
      s.start( G, G.config("member.system"), true );
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
  }
  
  /* Start and end a session as SubjectID "member.system", 1 argument method */
  public void testSessionStartEndAsMemberSystemOneArgMethod() {
    G = new Grouper();
    G.initialize();
    GrouperSession s= new GrouperSession();
    try {
      s.start( G, G.config("member.system") );
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
    try {
      s.end();
    } catch(Exception e) {
      Assert.fail("Exception thrown when ending session");
    }
  }
  
  /* Start and end a session as SubjectID "member.system", 2 argument method */
  public void testSessionStartEndAsMemberSystemTwoArgMethod() {
    G = new Grouper();
    G.initialize();
    GrouperSession s= new GrouperSession();
    try {
      s.start( G, G.config("member.system"), true );
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
    try {
      s.end();
    } catch(Exception e) {
      Assert.fail("Exception thrown when ending session");
    }
  }

  /* Attempt to end a session that hasn't been started */
  public void testSessionEndWithoutStart() {
    G = new Grouper();
    G.initialize();
    GrouperSession s = new GrouperSession();
    try {
      // XXX This may fail if we start throwing exceptions. 
      s.end();
    } catch(Exception e) {
      Assert.fail("Exception thrown when ending unstarted session");
    }
  }

}

