/*
 * @author mchyzer
 * $Id: ChangeLogTest.java,v 1.4 2009-06-09 04:19:23 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import java.util.List;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;


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

    int changeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_change_log_entry_temp");
    int changeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    //add a type
    GrouperSession grouperSession = SessionHelper.getRootSession();
    GroupType groupType = GroupType.createType(grouperSession, "test1");
    
    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");

    assertEquals("Should have added exactly one change log temp", changeLogTempCount+1, newChangeLogTempCount);
    assertEquals("Should be the same", changeLogCount, newChangeLogCount);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp").uniqueResult(ChangeLogEntry.class);

    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));

    //make sure some time has passed
    GrouperUtil.sleep(100);

    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());

    assertTrue("This should have happened in the last 5 seconds", 
        System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime()  < 5000);
    assertTrue("This should have happened in the last 5 seconds: + " + changeLogEntry.getCreatedOn(), 
        System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime() > 0);

    assertTrue("This should have happened in the last 5 seconds", 
        System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) < 5000);
    assertTrue("This should have happened in the last 5 seconds", 
        System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) > 0);

    assertNull(changeLogEntry.getSequenceNumber());

    assertEquals("Context id's should match", changeLogEntry.getContextId(), groupType.getContextId());

    assertEquals(groupType.getName(), changeLogEntry.retrieveValueForLabel("name"));
    assertEquals(groupType.getUuid(), changeLogEntry.retrieveValueForLabel("id"));

    //move the temp objects to the regular change log table
    ChangeLogTempToEntity.convertRecords();
    
    //#########################
    // Check the change log table, and temp table, see the record moved over
    
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have nothing in temp table", changeLogTempCount, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", changeLogCount+1, newChangeLogCount);
  
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity").uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), groupType.getContextId());
    
    //##################################
    // try an update

    //try an update of one field
    groupType.setName("test3");

    final GroupType GROUP_TYPE = groupType;

    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {

        hibernateHandlerBean.getHibernateSession().byObject().update(GROUP_TYPE);
        return null;
      }
    });

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", changeLogTempCount+1, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", changeLogCount+1, newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity order by createdOnDb").list(ChangeLogEntry.class);
    ChangeLogEntry changeLogEntry2 = changeLogEntries.get(1);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    assertEquals(groupType.getName(), changeLogEntry2.retrieveValueForLabel("name"));
    assertEquals(groupType.getUuid(), changeLogEntry2.retrieveValueForLabel("id"));
    assertEquals("name", changeLogEntry2.retrieveValueForLabel("propertyChanged"));
    assertEquals("test1", changeLogEntry2.retrieveValueForLabel("propertyOldValue"));
    
    
    //##################################
    // try a delete
    
    groupType.delete(grouperSession);

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", changeLogTempCount+1, newChangeLogTempCount);
    assertEquals("Should have two record in the change log table", changeLogCount+2, newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity order by createdOnDb").list(ChangeLogEntry.class);
    ChangeLogEntry changeLogEntry3 = changeLogEntries.get(2);

    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry3.getContextId()));
    
    assertEquals("Context id's should match", changeLogEntry3.getContextId(), groupType.getContextId());
        
    assertEquals(groupType.getName(), changeLogEntry3.retrieveValueForLabel("name"));
    assertEquals(groupType.getUuid(), changeLogEntry3.retrieveValueForLabel("id"));
  }

  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession = SessionHelper.getRootSession();
  
  /** root stem */
  private Stem root;

}
