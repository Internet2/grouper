/*
 * $Id: GrouperTest.java,v 1.10 2004-07-14 19:36:31 blair Exp $
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

    // TODO Make this into a test of its own and move out of setUp()
    // Establish a new Grouper instance
    G = new Grouper();
  }

  protected void tearDown () {
    // TODO Make this into a test of its own and move out of tearDown()
    // Destroy our Grouper instance
    G.destroy();
  }

  /* Instantiate a Grouper instance */
  public void testGrouperInstantiate() {
    Class  klass    = G.getClass();
    String expKlass = "edu.internet2.middleware.directory.grouper.Grouper";

    Assert.assertNotNull(G);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /* Initialize Grouper environment */
  public void testGrouperInitialize() {
    try {
      G.initialize();
    } catch(Exception e) {
      Assert.fail("Exception thrown when initializing Grouper");
    }
  }

  /* Get a runtime configuration setting */
  public void testGetRuntimeConfigSetting() {
    G.initialize();
    
    String expVal = "GrouperSystem";
    Assert.assertTrue( expVal.equals( G.config("member.system") ) );
  }

  /* Instantiate a Grouper session */
  public void testSessionInstantiate() {
    G.initialize();
    GrouperSession s = new GrouperSession();

    Class klass     = s.getClass();
    String expKlass = "edu.internet2.middleware.directory.grouper.GrouperSession";

    Assert.assertNotNull(s);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /* Start a session as SubjectID "member.system", 1 argument method */
  public void testSessionStartAsMemberSystemOneArgMethod() {
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
    G.initialize();
    GrouperSession s = new GrouperSession();
    try {
      // XXX This may fail if we start throwing exceptions. 
      s.end();
    } catch(Exception e) {
      Assert.fail("Exception thrown when ending unstarted session");
    }
  }

  /*
   * TODO Tests To Write
   * - Session creation as memberID ???
   * - Session creation as subjectID ???
   * - TODO And all the ones I haven't thought of or written down yet
   */

}

