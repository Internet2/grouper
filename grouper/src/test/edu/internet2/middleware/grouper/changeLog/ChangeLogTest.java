/*
 * @author mchyzer
 * $Id: ChangeLogTest.java,v 1.2 2009-05-26 06:50:56 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;


/**
 *
 */
public class ChangeLogTest extends GrouperTest {
  /**
   * @param name
   */
  public ChangeLogTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ChangeLogTest("testTypes"));
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    ApiConfig.testConfig.put("grouper.env.name", "testEnv");
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    ApiConfig.testConfig.remove("grouper.env.name");

  }

  /**
   * @throws Exception 
   * 
   */
  public void testTypes() throws Exception {
    //TODO finish this
//    //delete all change log records
//    HibernateSession.bySqlStatic().executeSql("delete from grouper_change_log_entry");
//
//    int changeLogCount = HibernateSession.bySqlStatic().select(int.class, 
//        "select count(1) from grouper_change_log_entry");
//    
//    //add a type
//    GrouperSession grouperSession = SessionHelper.getRootSession();
//    GroupType groupType = GroupType.createType(grouperSession, "test1");
//    
//    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
//      "select count(1) from grouper_change_log_entry");
//    
//    assertEquals("Should have added exactly one change log", changeLogCount+1, newChangeLogCount);
//    
//    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
//      .createQuery("from AuditEntry").uniqueResult(ChangeLogEntry.class);
//    
//    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry.getContextId()));
//
//    assertEquals("Context id's should match", changeLogEntry.getContextId(), groupType.getContextId());
//    
//    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
//
//    GroupType.createType(grouperSession, "test1", false);
//    
//    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
//      "select count(1) from grouper_change_log_entry");
//
//    assertEquals("Shouldnt have changed since type didnt change", changeLogCount+1, newChangeLogCount);
//    
//    //make sure date is different
//    GrouperUtil.sleep(1000);
//    
//    groupType.delete(grouperSession);
//
//    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
//      "select count(1) from grouper_change_log_entry");
//  
//    assertEquals("Should have added exactly two change logs", changeLogCount+2, newChangeLogCount);
//  
//    List<ChangeLogEntry> auditEntries = HibernateSession.byHqlStatic()
//      .createQuery("from AuditEntry order by createdOnDb").list(ChangeLogEntry.class);
//    ChangeLogEntry changeLogEntry2 = auditEntries.get(1);
//
//    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
//    
//    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
//        changeLogEntry2.getContextId()));
//    
//    assertEquals("Context id's should match", changeLogEntry2.getContextId(), groupType.getContextId());
//    
    
  }

  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession = SessionHelper.getRootSession();
  
  /** root stem */
  private Stem root;

}
