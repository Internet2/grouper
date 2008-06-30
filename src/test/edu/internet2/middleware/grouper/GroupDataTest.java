/*
 * @author mchyzer
 * $Id: GroupDataTest.java,v 1.2 2008-06-30 04:31:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;


/**
 *
 */
public class GroupDataTest extends GrouperTest {
  
  /**
   * main
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new GroupDataTest("testDbRetrieve"));
    TestRunner.run(GroupDataTest.class);
  }
  
  /**
   * @param name
   */
  public GroupDataTest(String name) {
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
   * Test method for {@link edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GroupDAO#dbVersionIsDifferent()}.
   */
  public void testDbVersionDifferent() {
    Group group1 = new Group();
    group1.dbVersionReset();
    assertFalse("Nothing should not be different", group1.dbVersionIsDifferent());
    group1.setCreateSourceDb("a");
    assertTrue(group1.dbVersionIsDifferent());
    assertEquals("Only one field changed", 1, group1.dbVersionDifferentFields().size());
    assertEquals("Only one field changed", Group.FIELD_CREATE_SOURCE, (String)group1.dbVersionDifferentFields().toArray()[0]);
    group1.setCreateSourceDb(null);
    assertFalse("Nothing should not be different", group1.dbVersionIsDifferent());
    assertEquals("No fields changed", 0, group1.dbVersionDifferentFields().size());
    
    group1.setCreateSourceDb("");
    assertEquals("No fields changed", 0, group1.dbVersionDifferentFields().size());
    assertFalse("empty is same as null", group1.dbVersionIsDifferent());
    assertEquals("No fields changed", 0, group1.dbVersionDifferentFields().size());
    
  }

  /**
   * test
   * @throws Exception 
   */
  public void testDbRetrieve() throws Exception {
    R r = R.populateRegistry(1, 2, 0);
    Group a = r.getGroup("a", "a");
    assertFalse("Nothing should not be different", a.dbVersionIsDifferent());
    a.getAttributesDb().put("description", "abc");
    Set<String> dbVersionDifferentFields = a.dbVersionDifferentFields();
    assertEquals("Only one field changed", 1, dbVersionDifferentFields.size());
    String differentField = (String)dbVersionDifferentFields.toArray()[0];
    assertEquals("Only one field changed", "description", differentField);

    assertEquals("abc", a.fieldValue(differentField));
    
    //this persists, and takes a new snapshot
    HibernateSession.byObjectStatic().update(a);
    
    assertFalse("Nothing should not be different", a.dbVersionIsDifferent());
    
    a.setCreateTimeLong(123);
    dbVersionDifferentFields = a.dbVersionDifferentFields();
    differentField = (String)dbVersionDifferentFields.toArray()[0];
    assertEquals("Only one field changed", 1, dbVersionDifferentFields.size());
    assertEquals("Only one field changed", Group.FIELD_CREATE_TIME, differentField);
  
    //delete and db version should be null
    a.delete();
    
    assertNull(a.dbVersion());
  }
  

}
