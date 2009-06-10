/*
 * @author mchyzer
 * $Id: ChangeLogTest.java,v 1.5 2009-06-10 05:31:35 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import java.util.List;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
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
    TestRunner.run(new ChangeLogTest("testGroups"));
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
  
    assertEquals(groupType.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_ADD.name));
    assertEquals(groupType.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_ADD.id));
  
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
    
    assertEquals(groupType.getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_UPDATE.name));
    assertEquals(groupType.getUuid(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_UPDATE.id));
    assertEquals("name", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_UPDATE.propertyChanged));
    assertEquals("test1", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_UPDATE.propertyOldValue));
    
    
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
        
    assertEquals(groupType.getName(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_DELETE.name));
    assertEquals(groupType.getUuid(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_DELETE.id));
  }

  /**
   * @throws Exception 
   * 
   */
  public void testGroups() throws Exception {

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    
    int changeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_change_log_entry_temp");
    int changeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    //add a type
    @SuppressWarnings("unused")
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Group group = this.edu.addChildGroup("test1", "test1");
    
    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");

    assertTrue("Should have added more than one change log temp", changeLogTempCount+1 <= newChangeLogTempCount);
    
    assertEquals("Should be the same", changeLogCount, newChangeLogCount);

    //get one entry in there
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);

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

    assertEquals("Context id's should match", changeLogEntry.getContextId(), group.getContextId());

    assertEquals(group.getName(), changeLogEntry.retrieveValueForLabel("name"));
    assertEquals(group.getUuid(), changeLogEntry.retrieveValueForLabel("id"));

    //move the temp objects to the regular change log table
    ChangeLogTempToEntity.convertRecords();
    
    //#########################
    // Check the change log table, and temp table, see the record moved over
    
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have nothing in temp table", changeLogTempCount, newChangeLogTempCount);
    assertTrue("Should have more than one record in the change log table: " + changeLogCount + ", " + newChangeLogCount, 
        changeLogCount+1 <= newChangeLogCount);
  
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity").uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), group.getContextId());
    
    //##################################
    // try an update

    //try an update of two field
    group.setDisplayExtension("test2");
    group.setDescription("test2");
    group.store();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have two in temp table", changeLogTempCount+2, newChangeLogTempCount);
    assertTrue("Should have more than one record in the change log table: " + changeLogCount + ", " + newChangeLogCount, 
        changeLogCount+1 <= newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    String propertyChangedFieldName = ChangeLogTypeBuiltin.GROUP_UPDATE.getChangeLogType().retrieveChangeLogEntryFieldForLabel(
        ChangeLogLabels.GROUP_UPDATE.propertyChanged.name());
    
    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by " + propertyChangedFieldName)
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_UPDATE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);

    {
      ChangeLogEntry descriptionEntry = changeLogEntries.get(0);
    
      assertTrue("contextId should exist", StringUtils.isNotBlank(descriptionEntry.getContextId()));
      
      assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
          descriptionEntry.getContextId()));
      
      assertEquals(group.getName(), descriptionEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.name));
      assertEquals(group.getUuid(), descriptionEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.id));
      assertEquals(ChangeLogLabels.GROUP_UPDATE.description.name(), 
          descriptionEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged));
      assertNull(descriptionEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyOldValue));
    }
    
    {
      ChangeLogEntry displayExtensionEntry = changeLogEntries.get(1);
      assertTrue("contextId should exist", StringUtils.isNotBlank(displayExtensionEntry.getContextId()));
      
      assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
          displayExtensionEntry.getContextId()));
      
      assertEquals(group.getName(), displayExtensionEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.name));
      assertEquals(group.getUuid(), displayExtensionEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.id));
      assertEquals(ChangeLogLabels.GROUP_UPDATE.displayExtension.name(), 
          displayExtensionEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged));
      assertEquals("test1", displayExtensionEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyOldValue));
    }
    
    //##################################
    // try a delete
    
    group.delete();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertTrue("Should have more than one in temp table", changeLogTempCount+1 <= newChangeLogTempCount);
    assertTrue("Should have more than two record in the change log table", changeLogCount+2 <= newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry2 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);

    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    assertEquals("Context id's should match", changeLogEntry2.getContextId(), group.getContextId());
        
    assertEquals(group.getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_DELETE.name));
    assertEquals(group.getUuid(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_DELETE.id));
  }

  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession = SessionHelper.getRootSession();
  
  /** root stem */
  private Stem root;

}
