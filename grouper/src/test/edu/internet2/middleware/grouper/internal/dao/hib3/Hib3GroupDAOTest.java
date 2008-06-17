/*
 * @author mchyzer
 * $Id: Hib3GroupDAOTest.java,v 1.1.2.1 2008-06-17 17:00:23 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao.hib3;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.R;
import edu.internet2.middleware.grouper.RegistryReset;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;


/**
 *
 */
public class Hib3GroupDAOTest extends GrouperTest {
  
  /**
   * main
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new Hib3GroupDAOTest("testDbRetrieve"));
    //TestRunner.run(Hib3GroupDAOTest.class);
  }
  
  /**
   * @param name
   */
  public Hib3GroupDAOTest(String name) {
    super(name);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    RegistryReset.reset();
  }

  /**
   * Test method for {@link edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GroupDAO#dbVersionDifferent()}.
   */
  public void testDbVersionDifferent() {
    Hib3GroupDAO group1 = new Hib3GroupDAO();
    group1.dbVersionReset();
    assertFalse("Nothing should not be different", group1.dbVersionDifferent());
    group1.setCreateSource("a");
    assertTrue(group1.dbVersionDifferent());
    assertEquals("Only one field changed", 1, group1.dbVersionDifferentFields().size());
    assertEquals("Only one field changed", Hib3GroupDAO.FIELD_CREATE_SOURCE, (String)group1.dbVersionDifferentFields().toArray()[0]);
    group1.setCreateSource(null);
    assertFalse("Nothing should not be different", group1.dbVersionDifferent());
    assertEquals("No fields changed", 0, group1.dbVersionDifferentFields().size());
    
    group1.setCreateSource("");
    assertEquals("No fields changed", 0, group1.dbVersionDifferentFields().size());
    assertFalse("empty is same as null", group1.dbVersionDifferent());
    assertEquals("No fields changed", 0, group1.dbVersionDifferentFields().size());
    
  }

  /**
   * test
   * @throws Exception 
   */
  //TODO
//  public void testDbRetrieve() throws Exception {
//    R r = R.populateRegistry(1, 2, 0);
//    Group a = r.getGroup("a", "a");
//    Hib3GroupDAO aDao = (Hib3GroupDAO)a._getDTO().getDAO();
//    assertFalse("Nothing should not be different", aDao.dbVersionDifferent());
//    aDao.getAttributes().put("description", "abc");
//    assertEquals("Only one field changed", 1, aDao.dbVersionDifferentFields().size());
//    assertEquals("Only one field changed", Hib3GroupDAO.ATTRIBUTE_PREFIX + "description", (String)aDao.dbVersionDifferentFields().toArray()[0]);
//
//    //this persists, and takes a new snapshot
//    HibernateSession.byObjectStatic().update(aDao);
//    
//    assertFalse("Nothing should not be different", aDao.dbVersionDifferent());
//    
//    aDao.setCreateTime(123);
//    assertEquals("Only one field changed", 1, aDao.dbVersionDifferentFields().size());
//    assertEquals("Only one field changed", Hib3GroupDAO.FIELD_CREATE_TIME, (String)aDao.dbVersionDifferentFields().toArray()[0]);
//  }
  

}
