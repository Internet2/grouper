/*
 * @author mchyzer
 * $Id: ChangeLogTest.java,v 1.9 2009-08-18 23:11:39 shilen Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import java.util.List;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


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
    TestRunner.run(new ChangeLogTest("testMemberships"));
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
  public void testFields() throws Exception {
  
    //add a type
    GrouperSession grouperSession = SessionHelper.getRootSession();
    GroupType groupType = GroupType.createType(grouperSession, "test1");

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();

    int changeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_change_log_entry_temp");
    int changeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    Field field = groupType.addAttribute(grouperSession, "testAttr", AccessPrivilege.READ, AccessPrivilege.ADMIN, false);
    
    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    assertTrue("Should have added more than one change log temp", changeLogTempCount+1 <= newChangeLogTempCount);
    
    assertEquals("Should be the same", changeLogCount, newChangeLogCount);
    
    //get one entry in there
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_FIELD_ADD.getChangeLogType().getId())
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
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), field.getContextId());
    
    assertEquals(field.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_ADD.name));
    assertEquals(field.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_ADD.id));
    
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
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), field.getContextId());
    
    //##################################
    // try an update
    
    //try an update of two field
    field.setReadPrivilege(AccessPrivilege.OPTIN);
    field.setWritePrivilege(AccessPrivilege.OPTOUT);

    final Field FIELD = field;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        GrouperDAOFactory.getFactory().getField().createOrUpdate(FIELD);

        return null;
      }
    });

    
    
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
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_FIELD_UPDATE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    {
      ChangeLogEntry readPrivilege = changeLogEntries.get(0);
      assertTrue("contextId should exist", StringUtils.isNotBlank(readPrivilege.getContextId()));
      
      assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
          readPrivilege.getContextId()));
      
      assertEquals(field.getName(), readPrivilege.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.name));
      assertEquals(field.getUuid(), readPrivilege.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.id));
      assertEquals(ChangeLogLabels.GROUP_FIELD_UPDATE.readPrivilege.name(), 
          readPrivilege.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.propertyChanged));
      assertEquals(AccessPrivilege.READ.getName(), readPrivilege.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.propertyOldValue));
      assertEquals(AccessPrivilege.OPTIN.getName(), readPrivilege.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.propertyNewValue));
    }
    
    {
      ChangeLogEntry writePrivilege = changeLogEntries.get(1);
      assertTrue("contextId should exist", StringUtils.isNotBlank(writePrivilege.getContextId()));
      
      assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
          writePrivilege.getContextId()));
      
      assertEquals(field.getName(), writePrivilege.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.name));
      assertEquals(field.getUuid(), writePrivilege.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.id));
      assertEquals(ChangeLogLabels.GROUP_FIELD_UPDATE.writePrivilege.name(), 
          writePrivilege.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.propertyChanged));
      assertEquals(AccessPrivilege.ADMIN.getName(), writePrivilege.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.propertyOldValue));
      assertEquals(AccessPrivilege.OPTOUT.getName(), writePrivilege.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.propertyNewValue));
    }
    
    //##################################
    // try a delete
    
    groupType.deleteField(grouperSession, field.getName());
    
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    assertTrue("Should have more than one in temp table", changeLogTempCount+1 <= newChangeLogTempCount);
    assertTrue("Should have more than two record in the change log table", changeLogCount+2 <= newChangeLogCount);
    
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry2 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_FIELD_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    assertEquals(field.getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_DELETE.name));
    assertEquals(field.getUuid(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_DELETE.id));
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
    
    //add a group
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
  
    assertEquals(group.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.name));
    assertEquals(group.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.id));
  
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

  /**
   * @throws Exception 
   * 
   */
  public void testStems() throws Exception {
  
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    
    int changeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_change_log_entry_temp");
    int changeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    //add a stem
    @SuppressWarnings("unused")
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Stem stem = this.edu.addChildStem("test1", "test1");
    
    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertTrue("Should have added more than one change log temp", changeLogTempCount+1 <= newChangeLogTempCount);
    
    assertEquals("Should be the same", changeLogCount, newChangeLogCount);
  
    //get one entry in there
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.STEM_ADD.getChangeLogType().getId())
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
  
    assertEquals("Context id's should match", changeLogEntry.getContextId(), stem.getContextId());
  
    assertEquals(stem.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_ADD.name));
    assertEquals(stem.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_ADD.id));
  
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
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), stem.getContextId());
    
    //##################################
    // try an update
  
    //try an update of two field
    stem.setDisplayExtension("test2");
    stem.setDescription("test2");
    stem.store();
  
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have two in temp table", changeLogTempCount+2, newChangeLogTempCount);
    assertTrue("Should have more than one record in the change log table: " + changeLogCount + ", " + newChangeLogCount, 
        changeLogCount+1 <= newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    String propertyChangedFieldName = ChangeLogTypeBuiltin.STEM_UPDATE.getChangeLogType().retrieveChangeLogEntryFieldForLabel(
        ChangeLogLabels.STEM_UPDATE.propertyChanged.name());
    
    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by " + propertyChangedFieldName)
      .setString("theChangeLogType", ChangeLogTypeBuiltin.STEM_UPDATE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
  
    {
      ChangeLogEntry descriptionEntry = changeLogEntries.get(0);
    
      assertTrue("contextId should exist", StringUtils.isNotBlank(descriptionEntry.getContextId()));
      
      assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
          descriptionEntry.getContextId()));
      
      assertEquals(stem.getName(), descriptionEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.name));
      assertEquals(stem.getUuid(), descriptionEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.id));
      assertEquals(ChangeLogLabels.STEM_UPDATE.description.name(), 
          descriptionEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyChanged));
      assertNull(descriptionEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyOldValue));
    }
    
    {
      ChangeLogEntry displayExtensionEntry = changeLogEntries.get(1);
      assertTrue("contextId should exist", StringUtils.isNotBlank(displayExtensionEntry.getContextId()));
      
      assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
          displayExtensionEntry.getContextId()));
      
      assertEquals(stem.getName(), displayExtensionEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.name));
      assertEquals(stem.getUuid(), displayExtensionEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.id));
      assertEquals(ChangeLogLabels.STEM_UPDATE.displayExtension.name(), 
          displayExtensionEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyChanged));
      assertEquals("test1", displayExtensionEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyOldValue));
    }
    
    //##################################
    // try a delete
    
    stem.delete();
  
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertTrue("Should have more than one in temp table", changeLogTempCount+1 <= newChangeLogTempCount);
    assertTrue("Should have more than two record in the change log table", changeLogCount+2 <= newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry2 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.STEM_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    assertEquals("Context id's should match", changeLogEntry2.getContextId(), stem.getContextId());
        
    assertEquals(stem.getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.STEM_DELETE.name));
    assertEquals(stem.getUuid(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.STEM_DELETE.id));
  }

  /**
   * @throws Exception 
   * 
   */
  public void testMemberships() throws Exception {

    @SuppressWarnings("unused")
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Group group = this.edu.addChildGroup("test1", "test1");

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    
    int changeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_change_log_entry_temp");
    int changeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    Subject rootSubject = SubjectFinder.findRootSubject();
    Member rootMember = MemberFinder.findBySubject(grouperSession, rootSubject, true);
    group.addMember(rootSubject);
    Membership membership = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(group.getUuid(), rootMember.getUuid(), 
          Group.getDefaultList(), "immediate", true, true);
    
    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");

    assertTrue("Should have added more than one change log temp", changeLogTempCount+1 <= newChangeLogTempCount);
    
    assertEquals("Should be the same", changeLogCount, newChangeLogCount);

    //get one entry in there
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
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

    assertEquals("Context id's should match", changeLogEntry.getContextId(), membership.getContextId());

    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals("immediate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.membershipType));

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
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), membership.getContextId());
    
    //##################################
    // try an update

    //try an update of two field
    Member newMember = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    membership.setMember(newMember);
    
    final Membership MEMBERSHIP = membership;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP);

        return null;
      }
    });

    
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have two in temp table", changeLogTempCount+2, newChangeLogTempCount);
    assertTrue("Should have more than one record in the change log table: " + changeLogCount + ", " + newChangeLogCount, 
        changeLogCount+1 <= newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    String propertyChangedFieldName = ChangeLogTypeBuiltin.MEMBERSHIP_UPDATE.getChangeLogType().retrieveChangeLogEntryFieldForLabel(
        ChangeLogLabels.MEMBERSHIP_UPDATE.propertyChanged.name());
    
    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by " + propertyChangedFieldName)
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_UPDATE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);

    {
      ChangeLogEntry sourceIdEntry = changeLogEntries.get(0);
    
      assertTrue("contextId should exist", StringUtils.isNotBlank(sourceIdEntry.getContextId()));
      
      assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
          sourceIdEntry.getContextId()));
      
      assertEquals(membership.getUuid(), sourceIdEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_UPDATE.id));
      assertEquals(ChangeLogLabels.MEMBERSHIP_UPDATE.sourceId.name(), 
          sourceIdEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_UPDATE.propertyChanged));
      assertEquals("g:isa", sourceIdEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_UPDATE.propertyOldValue));
    }
    
    {
      ChangeLogEntry subjectEntry = changeLogEntries.get(1);
      assertTrue("contextId should exist", StringUtils.isNotBlank(subjectEntry.getContextId()));
      
      assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
          subjectEntry.getContextId()));
      
      assertEquals(membership.getUuid(), subjectEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_UPDATE.id));
      assertEquals(ChangeLogLabels.MEMBERSHIP_UPDATE.subjectId.name(), 
          subjectEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_UPDATE.propertyChanged));
      assertEquals("GrouperSystem", subjectEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_UPDATE.propertyOldValue));
    }
    
    //##################################
    // try a delete
    
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group.addMember(SubjectTestHelper.SUBJ1);
    
    membership = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(group.getUuid(), newMember1.getUuid(), 
      Group.getDefaultList(), "immediate", true, true);

    group.deleteMember(SubjectTestHelper.SUBJ1);

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertTrue("Should have more than one in temp table", changeLogTempCount+1 <= newChangeLogTempCount);
    assertTrue("Should have more than two record in the change log table", changeLogCount+2 <= newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry2 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);

    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
        
    assertEquals(membership.getUuid(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.id));
  }

  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession = SessionHelper.getRootSession();
  
  /** root stem */
  private Stem root;

}
