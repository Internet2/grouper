/*
 * @author mchyzer
 * $Id: GrouperDdlUtilsTest.java,v 1.1 2008-07-27 07:37:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;


/**
 * tests
 */
public class GrouperDdlUtilsTest extends GrouperTest {

  /**
   * @param name
   */
  public GrouperDdlUtilsTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(GrouperDdlUtilsTest.class);
    TestRunner.run(new GrouperDdlUtilsTest("testBootstrapHelper"));
  }

  /**
   * see if tables are there (at least the grouper groups one)
   * @param expectRecords 
   * @param expectTrue pritn exception if expecting true
   * @return true if everything ok, false if not
   */
  private static boolean tablesThere(boolean expectRecords, boolean expectTrue) {
    try {
      //first, see if tables are there
      int count = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_stems");
      if (!expectRecords) {
        return true;
      }
      return count > 0;
    } catch (RuntimeException e) {
      if (expectTrue) {
        throw e;
      }
      return false;
    }

  }

  /**
   * 
   */
  public void testBootstrapHelper() {
    
    assertTrue("Starting out, tables should be there", tablesThere(false, true));
    
    //now lets remove all tables and object
    GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, true, false);
    
    assertFalse("Just removed tables, shouldnt be there", tablesThere(false, false));

    //lets add all tables and object
    GrouperDdlUtils.bootstrapHelper(false, true, false, false, true, false, true);
    
    //if we init data, the root stem should be there...
    assertTrue("Just added all tables, and registry init, it should be there", tablesThere(true, true));

    //should also have at least two rows in ddl
    int count = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_ddl");
    assertTrue("Count should be more than 1 since Grouper and Subject should be there " + count, count > 1);
    
    //try again, everything should be there (even not from junit)
    GrouperDdlUtils.bootstrapHelper(false, false, true, false, false, false, false);
    
    assertTrue("Should not change anything", tablesThere(true, true));

    //at this point, hibernate should not be shut off
    assertTrue("at this point, hibernate should not be shut off", GrouperDdlUtils.okToUseHibernate());
  }

}
