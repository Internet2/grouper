/*
 * @author mchyzer
 * $Id: ChangeLogTest.java,v 1.10 2009-10-31 17:46:47 shilen Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
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
import edu.internet2.middleware.grouper.misc.CompositeType;
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
  
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();

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
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
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
    assertEquals(rootMember.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(rootMember.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(membership.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(membership.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));

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
    
    int newerChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertTrue("Should have two more records in the change log table: " + newChangeLogCount + ", " + newerChangeLogCount, 
        newChangeLogCount+2 == newerChangeLogCount);
    
    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity order by createdOnDb")
      .list(ChangeLogEntry.class);

    {
      ChangeLogEntry deleteEntry = changeLogEntries.get(changeLogEntries.size() - 2);
    
      assertTrue("contextId should exist", StringUtils.isNotBlank(deleteEntry.getContextId()));
      
      assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
          deleteEntry.getContextId()));
      
      assertEquals(membership.getUuid(), deleteEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.id));
      assertEquals(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId(), deleteEntry.getChangeLogTypeId());
      assertEquals("members", deleteEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
      assertEquals(rootMember.getSubjectId(), deleteEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
      assertEquals(rootMember.getSubjectSourceId(), deleteEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
      assertEquals("immediate", deleteEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
      assertEquals(membership.getGroup().getUuid(), deleteEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
      assertEquals(membership.getGroup().getName(), deleteEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    }
    
    {
      ChangeLogEntry addEntry = changeLogEntries.get(changeLogEntries.size() - 1);
      assertTrue("contextId should exist", StringUtils.isNotBlank(addEntry.getContextId()));
      
      assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
          addEntry.getContextId()));
      
      assertEquals(membership.getUuid(), addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.id));
      assertEquals(ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId(), addEntry.getChangeLogTypeId());
      assertEquals("members", addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
      assertEquals(newMember.getSubjectId(), addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
      assertEquals(newMember.getSubjectSourceId(), addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
      assertEquals("immediate", addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
      assertEquals(membership.getGroup().getUuid(), addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
      assertEquals(membership.getGroup().getName(), addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
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
    
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by createdOnDb")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    ChangeLogEntry changeLogEntry2 = changeLogEntries.get(changeLogEntries.size() - 1);

    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
        
    assertEquals(membership.getUuid(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.id));
    assertEquals(membership.getMemberSubjectId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals("members", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(membership.getMemberSourceId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals("immediate", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
    assertEquals(membership.getGroup().getUuid(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(membership.getGroup().getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    
    //##################################
    // try a membership add that causes effective memberships
    
    Group g0 = this.edu.addChildGroup("group0", "group0");
    Group g1 = this.edu.addChildGroup("group1", "group1");
    Group g2 = this.edu.addChildGroup("group2", "group2");
    Group g3 = this.edu.addChildGroup("group3", "group3");
    Group g4 = this.edu.addChildGroup("group4", "group4");
    Group g5 = this.edu.addChildGroup("group5", "group5");
    
    g0.addMember(g1.toSubject());
    g1.grantPriv(g3.toSubject(), AccessPrivilege.UPDATE);
    g2.addMember(g3.toSubject());
    g4.addMember(g5.toSubject());
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3.addMember(g4.toSubject());

    Membership g3g4Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          Group.getDefaultList(), "immediate", true, true);
    Membership g3g5Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          Group.getDefaultList(), "effective", true, true);
    Membership g2g4Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g2.getUuid(), g4.toMember().getUuid(), 
          Group.getDefaultList(), "effective", true, true);
    Membership g2g5Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g2.getUuid(), g5.toMember().getUuid(), 
          Group.getDefaultList(), "effective", true, true);
    Membership g1g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g1.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("updaters", true), "effective", true, true);
    Membership g1g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g1.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("updaters", true), "effective", true, true);


    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 6 new change log entries", 6, newChangeLogCount);
    
    verifyEffectiveMembershipsAdd(g3g4Mship, g3g5Mship, g2g4Mship, g2g5Mship, g1g4Priv, g1g5Priv);
    
    //##################################
    // try disabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3g4Mship.setEnabled(false);
    g3g4Mship.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    GrouperDAOFactory.getFactory().getMembership().update(g3g4Mship);

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 6 new change log entries", 6, newChangeLogCount);
    
    verifyEffectiveMembershipsDelete(g3g4Mship, g3g5Mship, g2g4Mship, g2g5Mship, g1g4Priv, g1g5Priv);
    
    
    //##################################
    // try changing member when disabled

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g3g4Mship.setMember(rootMember);
    GrouperDAOFactory.getFactory().getMembership().update(g3g4Mship);

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    
    g3g4Mship.setMember(g4.toMember());
    GrouperDAOFactory.getFactory().getMembership().update(g3g4Mship);

    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    
    
    //##################################
    // try enabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3g4Mship.setEnabled(true);
    g3g4Mship.setEnabledTime(new Timestamp(new Date().getTime() - 100000));
    GrouperDAOFactory.getFactory().getMembership().update(g3g4Mship);

    g3g4Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          Group.getDefaultList(), "immediate", true, true);
    g3g5Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          Group.getDefaultList(), "effective", true, true);
    g2g4Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g2.getUuid(), g4.toMember().getUuid(), 
          Group.getDefaultList(), "effective", true, true);
    g2g5Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g2.getUuid(), g5.toMember().getUuid(), 
          Group.getDefaultList(), "effective", true, true);
    g1g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g1.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("updaters", true), "effective", true, true);
    g1g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g1.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("updaters", true), "effective", true, true);
  
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 6 new change log entries", 6, newChangeLogCount);
    
    verifyEffectiveMembershipsAdd(g3g4Mship, g3g5Mship, g2g4Mship, g2g5Mship, g1g4Priv, g1g5Priv);
    
    
    //##################################
    // try changing member and make sure change log has correct deletes
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    Membership immediate = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          Group.getDefaultList(), "immediate", true, true);
    immediate.setMember(rootMember);
    GrouperDAOFactory.getFactory().getMembership().update(immediate);

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 9 new change log entries", 9, newChangeLogCount);

    verifyEffectiveMembershipsDelete(g3g4Mship, g3g5Mship, g2g4Mship, g2g5Mship, g1g4Priv, g1g5Priv);

    
    //##################################
    // try changing member and make sure change log has correct adds
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    immediate.setMember(g4.toMember());
    GrouperDAOFactory.getFactory().getMembership().update(immediate);
    
    g3g4Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          Group.getDefaultList(), "immediate", true, true);
    g3g5Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          Group.getDefaultList(), "effective", true, true);
    g2g4Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g2.getUuid(), g4.toMember().getUuid(), 
          Group.getDefaultList(), "effective", true, true);
    g2g5Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g2.getUuid(), g5.toMember().getUuid(), 
          Group.getDefaultList(), "effective", true, true);
    g1g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g1.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("updaters", true), "effective", true, true);
    g1g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g1.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("updaters", true), "effective", true, true);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 9 new change log entries", 9, newChangeLogCount);
    
    verifyEffectiveMembershipsAdd(g3g4Mship, g3g5Mship, g2g4Mship, g2g5Mship, g1g4Priv, g1g5Priv);

    
    //##################################
    // try deleting membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3.deleteMember(g4.toSubject());

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 6 new change log entries", 6, newChangeLogCount);
    
    verifyEffectiveMembershipsDelete(g3g4Mship, g3g5Mship, g2g4Mship, g2g5Mship, g1g4Priv, g1g5Priv);
    
  }


  /**
   * @throws Exception 
   * 
   */
  public void testAccessPrivileges() throws Exception {

    Subject rootSubject = SubjectFinder.findRootSubject();
    Member rootMember = MemberFinder.findBySubject(grouperSession, rootSubject, true);

    
    //##################################
    // try a membership add that causes effective memberships
    
    Group g2 = this.edu.addChildGroup("group2", "group2");
    Group g3 = this.edu.addChildGroup("group3", "group3");
    Group g4 = this.edu.addChildGroup("group4", "group4");
    Group g5 = this.edu.addChildGroup("group5", "group5");
    
    g2.addMember(g3.toSubject());
    g4.addMember(g5.toSubject());
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3.grantPriv(g4.toSubject(), AccessPrivilege.OPTIN);

    Membership g3g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("optins", true), "immediate", true, true);
    Membership g3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("optins", true), "effective", true, true);


    ChangeLogTempToEntity.convertRecords();

    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    
    verifyEffectiveAccessPrivilegesAdd(g3g4Priv, g3g5Priv);
    
    //##################################
    // try disabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3g4Priv.setEnabled(false);
    g3g4Priv.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    GrouperDAOFactory.getFactory().getMembership().update(g3g4Priv);

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    
    verifyEffectiveAccessPrivilegesDelete(g3g4Priv, g3g5Priv);
    
    
    //##################################
    // try changing member when disabled

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g3g4Priv.setMember(rootMember);
    GrouperDAOFactory.getFactory().getMembership().update(g3g4Priv);

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    
    g3g4Priv.setMember(g4.toMember());
    GrouperDAOFactory.getFactory().getMembership().update(g3g4Priv);

    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    
    
    //##################################
    // try enabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3g4Priv.setEnabled(true);
    g3g4Priv.setEnabledTime(new Timestamp(new Date().getTime() - 100000));
    GrouperDAOFactory.getFactory().getMembership().update(g3g4Priv);

    g3g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("optins", true), "immediate", true, true);
    g3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("optins", true), "effective", true, true);
  
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    
    verifyEffectiveAccessPrivilegesAdd(g3g4Priv, g3g5Priv);
    
    
    //##################################
    // try changing member and make sure change log has correct deletes
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    Membership immediate = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("optins", true), "immediate", true, true);
    immediate.setMember(rootMember);
    GrouperDAOFactory.getFactory().getMembership().update(immediate);

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);

    verifyEffectiveAccessPrivilegesDelete(g3g4Priv, g3g5Priv);

    
    //##################################
    // try changing member and make sure change log has correct adds
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    immediate.setMember(g4.toMember());
    GrouperDAOFactory.getFactory().getMembership().update(immediate);
    
    g3g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("optins", true), "immediate", true, true);
    g3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("optins", true), "effective", true, true);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);
    
    verifyEffectiveAccessPrivilegesAdd(g3g4Priv, g3g5Priv);

    
    //##################################
    // try deleting membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3.revokePriv(g4.toSubject(), AccessPrivilege.OPTIN);

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    
    verifyEffectiveAccessPrivilegesDelete(g3g4Priv, g3g5Priv);
    
  }


  /**
   * @throws Exception 
   * 
   */
  public void testCustomListMemberships() throws Exception {

    Subject rootSubject = SubjectFinder.findRootSubject();
    Member rootMember = MemberFinder.findBySubject(grouperSession, rootSubject, true);

    //add a type
    GrouperSession grouperSession = SessionHelper.getRootSession();
    GroupType groupType = GroupType.createType(grouperSession, "customType");
    Field customList = groupType.addList(grouperSession, "customList", AccessPrivilege.READ, AccessPrivilege.ADMIN);
    
    //##################################
    // try a membership add that causes effective memberships
    
    Group g2 = this.edu.addChildGroup("group2", "group2");
    Group g3 = this.edu.addChildGroup("group3", "group3");
    Group g4 = this.edu.addChildGroup("group4", "group4");
    Group g5 = this.edu.addChildGroup("group5", "group5");
    
    g3.addType(groupType);
    
    g2.addMember(g3.toSubject());
    g4.addMember(g5.toSubject());
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3.addMember(g4.toSubject(), customList);

    Membership g3g4Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("customList", true), "immediate", true, true);
    Membership g3g5Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("customList", true), "effective", true, true);


    ChangeLogTempToEntity.convertRecords();

    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    
    verifyEffectiveCustomListMembershipsAdd(g3g4Mship, g3g5Mship);
    
    //##################################
    // try disabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3g4Mship.setEnabled(false);
    g3g4Mship.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    GrouperDAOFactory.getFactory().getMembership().update(g3g4Mship);

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    
    verifyEffectiveCustomListMembershipsDelete(g3g4Mship, g3g5Mship);
    
    
    //##################################
    // try changing member when disabled

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g3g4Mship.setMember(rootMember);
    GrouperDAOFactory.getFactory().getMembership().update(g3g4Mship);

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    
    g3g4Mship.setMember(g4.toMember());
    GrouperDAOFactory.getFactory().getMembership().update(g3g4Mship);

    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    
    
    //##################################
    // try enabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3g4Mship.setEnabled(true);
    g3g4Mship.setEnabledTime(new Timestamp(new Date().getTime() - 100000));
    GrouperDAOFactory.getFactory().getMembership().update(g3g4Mship);

    g3g4Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("customList", true), "immediate", true, true);
    g3g5Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("customList", true), "effective", true, true);
  
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    
    verifyEffectiveCustomListMembershipsAdd(g3g4Mship, g3g5Mship);
    
    
    //##################################
    // try changing member and make sure change log has correct deletes
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    Membership immediate = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("customList", true), "immediate", true, true);
    immediate.setMember(rootMember);
    GrouperDAOFactory.getFactory().getMembership().update(immediate);

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);

    verifyEffectiveCustomListMembershipsDelete(g3g4Mship, g3g5Mship);

    
    //##################################
    // try changing member and make sure change log has correct adds
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    immediate.setMember(g4.toMember());
    GrouperDAOFactory.getFactory().getMembership().update(immediate);
    
    g3g4Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("customList", true), "immediate", true, true);
    g3g5Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("customList", true), "effective", true, true);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);
    
    verifyEffectiveCustomListMembershipsAdd(g3g4Mship, g3g5Mship);

    
    //##################################
    // try deleting membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3.deleteMember(g4.toSubject(), customList);

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    
    verifyEffectiveCustomListMembershipsDelete(g3g4Mship, g3g5Mship);
    
  }
  

  /**
   * @throws Exception 
   * 
   */
  public void testCompositeMemberships() throws Exception {

    Subject rootSubject = SubjectFinder.findRootSubject();
    Member rootMember = MemberFinder.findBySubject(grouperSession, rootSubject, true);

    
    //##################################
    // add composite
    
    Group g1 = this.edu.addChildGroup("group1", "group1");
    Group g2 = this.edu.addChildGroup("group2", "group2");
    Group g3 = this.edu.addChildGroup("group3", "group3");
    Group g4 = this.edu.addChildGroup("group4", "group4");
    Group g5 = this.edu.addChildGroup("group5", "group5");
    Group g6 = this.edu.addChildGroup("group6", "group6");
    
    g5.grantPriv(g1.toSubject(), AccessPrivilege.UPDATE);
    g6.addMember(g1.toSubject());
    g2.addMember(g4.toSubject());
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g1.addCompositeMember(CompositeType.UNION, g2, g3);

    ChangeLogTempToEntity.convertRecords();

    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 3 new change log entries - 1 composite, 2 effective", 3, newChangeLogCount);

    int newCompositeChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry where string05='composite'");
    assertEquals("Should have 1 new composite membership change log entries", 1, newCompositeChangeLogCount);
    

    //##################################
    // delete composite
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g1.deleteCompositeMember();

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 3 new change log entries - 1 composite, 2 effective", 3, newChangeLogCount);

    newCompositeChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry where string05='composite'");
    assertEquals("Should have 1 new composite membership change log entries", 1, newCompositeChangeLogCount);
    

    //##################################
    // add immediate that causes composite
    
    g1.addCompositeMember(CompositeType.UNION, g2, g3);
    g2.deleteMember(g4.toSubject());

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g2.addMember(g4.toSubject());

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 4 new change log entries - 1 composite, 1 immediate, 2 effective", 4, newChangeLogCount);

    newCompositeChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry where string05='composite'");
    assertEquals("Should have 1 new composite membership change log entries", 1, newCompositeChangeLogCount);
    
    
    
    //##################################
    // try disabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    Membership immediate = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g2.getUuid(), g4.toMember().getUuid(), 
          Group.getDefaultList(), "immediate", true, true);
    
    immediate.setEnabled(false);
    immediate.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    GrouperDAOFactory.getFactory().getMembership().update(immediate);

    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 4 new change log entries - 1 composite, 1 immediate, 2 effective", 4, newChangeLogCount);
  
    newCompositeChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry where string05='composite'");
    assertEquals("Should have 1 new composite membership change log entries", 1, newCompositeChangeLogCount);
    
    
    //##################################
    // try changing member when disabled

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    immediate.setMember(rootMember);
    GrouperDAOFactory.getFactory().getMembership().update(immediate);

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    
    immediate.setMember(g4.toMember());
    GrouperDAOFactory.getFactory().getMembership().update(immediate);

    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    
    
    //##################################
    // try enabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    immediate.setEnabled(true);
    immediate.setEnabledTime(new Timestamp(new Date().getTime() - 100000));
    GrouperDAOFactory.getFactory().getMembership().update(immediate);

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 4 new change log entries - 1 composite, 1 immediate, 2 effective", 4, newChangeLogCount);
  
    newCompositeChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry where string05='composite'");
    assertEquals("Should have 1 new composite membership change log entries", 1, newCompositeChangeLogCount);
    
    
    //##################################
    // try changing member
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    immediate = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g2.getUuid(), g4.toMember().getUuid(), 
          Group.getDefaultList(), "immediate", true, true);
    immediate.setMember(rootMember);
    GrouperDAOFactory.getFactory().getMembership().update(immediate);

    ChangeLogTempToEntity.convertRecords();


    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 8 new change log entries - 2 composite, 2 immediate, 4 effective", 8, newChangeLogCount);
  
    newCompositeChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry where string05='composite'");
    assertEquals("Should have 2 new composite membership change log entries", 2, newCompositeChangeLogCount);
    
    
    //##################################
    // try deleting membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g2.deleteMember(rootMember);

    ChangeLogTempToEntity.convertRecords();


    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 4 new change log entries - 1 composite, 1 immediate, 2 effective", 4, newChangeLogCount);
  
    newCompositeChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry where string05='composite'");
    assertEquals("Should have 1 new composite membership change log entries", 1, newCompositeChangeLogCount);
    
  }

  private void verifyEffectiveCustomListMembershipsAdd(Membership g3g4Mship, Membership g3g5Mship) {

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = :type")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .setString("type", "immediate")
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("customList", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(g3g4Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(g3g4Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(g3g4Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g3g4Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .setString("id", g3g5Mship.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("customList", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(g3g5Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(g3g5Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals("effective", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
    assertEquals(g3g5Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g3g5Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
  }
  
  private void verifyEffectiveCustomListMembershipsDelete(Membership g3g4Mship, Membership g3g5Mship) {
    
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .setString("id", g3g4Mship.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("customList", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(g3g4Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(g3g4Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals("immediate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
    assertEquals(g3g4Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g3g4Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    
  
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .setString("id", g3g5Mship.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("customList", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(g3g5Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(g3g5Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals("effective", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
    assertEquals(g3g5Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g3g5Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
  }
  
  
  private void verifyEffectiveAccessPrivilegesAdd(Membership g3g4Priv, Membership g3g5Priv) {

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = :type")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("type", "immediate")
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("optins", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(g3g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(g3g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(g3g4Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(g3g4Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("id", g3g5Priv.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("optins", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(g3g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(g3g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("effective", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(g3g5Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(g3g5Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
  }
  
  private void verifyEffectiveAccessPrivilegesDelete(Membership g3g4Priv, Membership g3g5Priv) {
    
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("id", g3g4Priv.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("optins", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(g3g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(g3g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("immediate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(g3g4Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(g3g4Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    
  
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("id", g3g5Priv.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("optins", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(g3g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(g3g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("effective", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(g3g5Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(g3g5Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
  }
  
  private void verifyEffectiveMembershipsAdd(Membership g3g4Mship, Membership g3g5Mship, 
      Membership g2g4Mship, Membership g2g5Mship, Membership g1g4Priv, Membership g1g5Priv) {

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = :type")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .setString("type", "immediate")
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(g3g4Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(g3g4Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals("immediate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
    assertEquals(g3g4Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g3g4Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .setString("id", g3g5Mship.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(g3g5Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(g3g5Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals("effective", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
    assertEquals(g3g5Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g3g5Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .setString("id", g2g4Mship.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(g2g4Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(g2g4Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals("effective", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
    assertEquals(g2g4Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g2g4Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .setString("id", g2g5Mship.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(g2g5Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(g2g5Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals("effective", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
    assertEquals(g2g5Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g2g5Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("id", g1g4Priv.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("updaters", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(g1g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(g1g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("effective", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(g1g4Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(g1g4Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("id", g1g5Priv.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("updaters", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(g1g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(g1g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("effective", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(g1g5Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(g1g5Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
  }
  

  private void verifyEffectiveMembershipsDelete(Membership g3g4Mship, Membership g3g5Mship, 
      Membership g2g4Mship, Membership g2g5Mship, Membership g1g4Priv, Membership g1g5Priv) {

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .setString("id", g3g4Mship.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(g3g4Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(g3g4Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals("immediate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
    assertEquals(g3g4Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g3g4Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .setString("id", g3g5Mship.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(g3g5Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(g3g5Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals("effective", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
    assertEquals(g3g5Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g3g5Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .setString("id", g2g4Mship.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(g2g4Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(g2g4Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals("effective", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
    assertEquals(g2g4Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g2g4Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .setString("id", g2g5Mship.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(g2g5Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(g2g5Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals("effective", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
    assertEquals(g2g5Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g2g5Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("id", g1g4Priv.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("updaters", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(g1g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(g1g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("effective", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(g1g4Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(g1g4Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string01 = :id")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("id", g1g5Priv.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("updaters", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(g1g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(g1g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("effective", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(g1g5Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(g1g5Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
  }
  
  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession = SessionHelper.getRootSession();
  
  /** root stem */
  private Stem root;

}
