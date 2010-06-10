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
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.flat.FlatAttributeDef;
import edu.internet2.middleware.grouper.flat.FlatGroup;
import edu.internet2.middleware.grouper.flat.FlatMembership;
import edu.internet2.middleware.grouper.flat.FlatStem;
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
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
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
    TestRunner.run(new ChangeLogTest("testAttributeDefs"));
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
   */
  public void testAttributeDefs() throws Exception {
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    //add attr def
    SessionHelper.getRootSession();
    AttributeDef resourcesDef = edu.addChildAttributeDef("attrdef", AttributeDefType.perm);
    
    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have added exactly two change log temp", 2, newChangeLogTempCount);
    assertEquals("Should be the same", 0, newChangeLogCount);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_DEF_ADD.getChangeLogType().getId())
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

    assertEquals("Context id's should match", changeLogEntry.getContextId(), resourcesDef.getContextId());
  
    assertEquals(resourcesDef.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_ADD.name));
    assertEquals(resourcesDef.getStemId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_ADD.stemId));
    assertEquals(resourcesDef.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_ADD.id));
  
    //move the temp objects to the regular change log table
    ChangeLogTempToEntity.convertRecords();
    
    //#########################
    // Check the change log table, and temp table, see the record moved over.
    
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have two records in the change log table", 2, newChangeLogCount);
  
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_DEF_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), resourcesDef.getContextId());
    
    // check flat table
    FlatAttributeDef flatAttrDef = GrouperDAOFactory.getFactory().getFlatAttributeDef().findById(resourcesDef.getId());
    assertNotNull(flatAttrDef);
    assertEquals("Verify attribute def id", flatAttrDef.getId(), flatAttrDef.getAttributeDefId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatAttrDef.getContextId());
    
    //##################################
    // try an update
  
    //try an update of one field
    resourcesDef.setDescription("test description");
  
    final AttributeDef ATTRIBUTE_DEF = resourcesDef;
  
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().update(ATTRIBUTE_DEF);
        return null;
      }
    });
  
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have two records in the change log table", 2, newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry2 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_DEF_UPDATE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));

    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    assertEquals(resourcesDef.getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.name));
    assertEquals(resourcesDef.getDescription(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.description));
    assertEquals(resourcesDef.getUuid(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.id));
    assertEquals("description", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.propertyChanged));
    assertEquals(null, changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.propertyOldValue));
    assertEquals("test description", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.propertyNewValue));
    
    
    //##################################
    // try a delete
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        ATTRIBUTE_DEF.delete();
        return null;
      }
    });
  
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have two in temp table", 2, newChangeLogTempCount);
    assertEquals("Should have three records in the change log table", 3, newChangeLogCount);

    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry3 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_DEF_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertEquals(resourcesDef.getName(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_DELETE.name));
    assertEquals(resourcesDef.getDescription(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_DELETE.description));
    assertEquals(resourcesDef.getStemId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_DELETE.stemId));
    assertEquals(resourcesDef.getUuid(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_DELETE.id));

    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry3.getContextId()));
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry3.getContextId()));
    assertEquals("Context id's should match", changeLogEntry3.getContextId(), resourcesDef.getContextId());
    
    // check flat table
    flatAttrDef = GrouperDAOFactory.getFactory().getFlatAttributeDef().findById(resourcesDef.getId());
    assertNull(flatAttrDef);
    
    //##################################
    // try a add and delete and verify there are no errors with the flat tables

    int flatCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_attribute_def");
    
    resourcesDef = edu.addChildAttributeDef("attrdef2", AttributeDefType.perm);
    resourcesDef.delete();
    
    ChangeLogTempToEntity.convertRecords();

    int newFlatCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_attribute_def");
   
    assertEquals("Should have no changes to flat table", flatCount, newFlatCount);
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
      .createQuery("from ChangeLogEntryEntity order by sequenceNumber").list(ChangeLogEntry.class);
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
      .createQuery("from ChangeLogEntryEntity order by sequenceNumber").list(ChangeLogEntry.class);
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
  
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
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
    
    // check flat table
    FlatGroup flatGroup = GrouperDAOFactory.getFactory().getFlatGroup().findById(group.getUuid());
    assertNotNull(flatGroup);
    assertEquals("Verify group id", flatGroup.getId(), flatGroup.getGroupId());
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
    
    // check flat table
    flatGroup = GrouperDAOFactory.getFactory().getFlatGroup().findById(group.getUuid());
    assertNull(flatGroup);
    
    //##################################
    // try a add and delete and verify there are no errors with the flat tables

    int flatCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_groups");
    int flatCount2 = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    group = this.edu.addChildGroup("test2", "test2");
    group.delete();
    
    ChangeLogTempToEntity.convertRecords();

    int newFlatCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_groups");
    int newFlatCount2 = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
   
    assertEquals("Should have no changes to flat table", flatCount, newFlatCount);
    assertEquals("Should have no changes to flat table", flatCount2, newFlatCount2);
    
    // now try having an addMember and deleteGroup in the changelog after the addGroup is processed
    group = this.edu.addChildGroup("test3", "test3");
    ChangeLogTempToEntity.convertRecords();
    group.addMember(newMember1.getSubject());
    group.delete();
    ChangeLogTempToEntity.convertRecords();

    newFlatCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_groups");
    newFlatCount2 = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
   
    assertEquals("Should have no changes to flat table", flatCount, newFlatCount);
    assertEquals("Should have no changes to flat table", flatCount2, newFlatCount2);
      
    
    // try adding a member to a group where the group is a member of another group that's just been deleted
    group = this.edu.addChildGroup("test3", "test3");
    Group group2 = this.edu.addChildGroup("test4", "test4");
    group2.addMember(group.toSubject());
    ChangeLogTempToEntity.convertRecords();
    group.addMember(newMember1.getSubject());
    group2.delete();
    ChangeLogTempToEntity.convertRecords();

    newFlatCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_groups");
    newFlatCount2 = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
   
    assertEquals("Should have one change in the flat table", flatCount + 1, newFlatCount);
    assertEquals("Should have four changes in the flat table (3 are privs)", flatCount2 + 4, newFlatCount2);
      
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
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.STEM_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), stem.getContextId());
    
    // check flat table
    FlatStem flatStem = GrouperDAOFactory.getFactory().getFlatStem().findById(stem.getUuid());
    assertNotNull(flatStem);
    assertEquals("Verify stem id", flatStem.getId(), flatStem.getStemId());
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
    
    // check flat table
    flatStem = GrouperDAOFactory.getFactory().getFlatStem().findById(stem.getUuid());
    assertNull(flatStem);
    
    //##################################
    // try a add and delete and verify there are no errors with the flat tables

    int flatCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_stems");
    
    stem = this.edu.addChildStem("test1", "test1");
    stem.delete();
    
    ChangeLogTempToEntity.convertRecords();

    int newFlatCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_stems");
   
    assertEquals("Should have no changes to flat table", flatCount, newFlatCount);
  }

  /**
   * @throws Exception 
   * 
   */
  public void testMemberships() throws Exception {

    GrouperSession grouperSession = SessionHelper.getRootSession();
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    Member newMember3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, true);
    
    Group group = this.edu.addChildGroup("test1", "test1");

    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic()
      .createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
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
    
    // check flat table
    FlatMembership flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(membership.getOwnerGroupId(), membership.getMemberUuid(), membership.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner group id", flatMship.getOwnerId(), flatMship.getOwnerGroupId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());
    
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
    
    {
      List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subj")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
        .setString("subj", rootMember.getSubjectId())
        .list(ChangeLogEntry.class);

      ChangeLogEntry deleteEntry = changeLogEntries.get(0);
    
      assertTrue("contextId should exist", StringUtils.isNotBlank(deleteEntry.getContextId()));
      
      assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
          deleteEntry.getContextId()));

      assertEquals(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId(), deleteEntry.getChangeLogTypeId());
      assertEquals("members", deleteEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
      assertEquals(rootMember.getSubjectId(), deleteEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
      assertEquals(rootMember.getSubjectSourceId(), deleteEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
      assertEquals(membership.getGroup().getUuid(), deleteEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
      assertEquals(membership.getGroup().getName(), deleteEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    }
    
    {
      List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subj")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
        .setString("subj", newMember.getSubjectId())
        .list(ChangeLogEntry.class);

      ChangeLogEntry addEntry = changeLogEntries.get(0);
      assertTrue("contextId should exist", StringUtils.isNotBlank(addEntry.getContextId()));
      
      assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
          addEntry.getContextId()));
      
      assertEquals(ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId(), addEntry.getChangeLogTypeId());
      assertEquals("members", addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
      assertEquals(newMember.getSubjectId(), addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
      assertEquals(newMember.getSubjectSourceId(), addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
      assertEquals(membership.getGroup().getUuid(), addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
      assertEquals(membership.getGroup().getName(), addEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    }
    
    // check flat table
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(membership.getOwnerGroupId(), rootMember.getUuid(), membership.getFieldId());
    assertNull(flatMship);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(membership.getOwnerGroupId(), newMember.getUuid(), membership.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner group id", flatMship.getOwnerId(), flatMship.getOwnerGroupId());
    assertEquals("Context id's should match", membership.getContextId(), flatMship.getContextId());
    
    //##################################
    // try a delete
    
    group.addMember(SubjectTestHelper.SUBJ1);
    
    ChangeLogTempToEntity.convertRecords();
    
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
    
    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subj")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .setString("subj", newMember1.getSubjectId())
      .list(ChangeLogEntry.class);
    
    ChangeLogEntry changeLogEntry2 = changeLogEntries.get(0);

    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));

    assertEquals(membership.getMemberSubjectId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals("members", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(membership.getMemberSourceId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals(membership.getGroup().getUuid(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(membership.getGroup().getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    
    // check flat table
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(membership.getOwnerGroupId(), newMember1.getUuid(), membership.getFieldId());
    assertNull(flatMship);
    
    
    //##################################
    // try multiple immediate memberships

    Group group2 = this.edu.addChildGroup("test2", "test2");

    ChangeLogTempToEntity.convertRecords();
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    int flatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    group2.addMember(newMember1.getSubject());
    group2.addMember(newMember2.getSubject());
    Group group3 = this.edu.addChildGroup("test3", "test3");
    group2.addMember(group3.toSubject());
    Group group4 = this.edu.addChildGroup("test4", "test4");
    group4.addMember(group2.toSubject());
    group2.addMember(newMember3.getSubject());
    ChangeLogTempToEntity.convertRecords();

    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    int newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");

    assertEquals("Should have 9 change log entries", 9, changeLogEntries.size());
    assertEquals("Should have 15 new flat memberships (6 are privs)", flatMembershipCount + 15, newFlatMembershipCount);

    group2.deleteMember(newMember1.getSubject());
    group2.deleteMember(newMember2.getSubject());
    group3.delete();
    group4.delete();
    group2.deleteMember(newMember3.getSubject());
    ChangeLogTempToEntity.convertRecords();

    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 9 change log entries", 9, changeLogEntries.size());
    assertEquals("Should have 15 fewer flat memberships (6 are privs)", flatMembershipCount, newFlatMembershipCount);
    
    //##################################
    // try a membership add that causes effective memberships
    
    Group g0 = this.edu.addChildGroup("group0", "group0");
    Group g1 = this.edu.addChildGroup("group1", "group1");
    Group g2 = this.edu.addChildGroup("group2", "group2");
    Group g3 = this.edu.addChildGroup("group3", "group3");
    Group g4 = this.edu.addChildGroup("group4", "group4");
    Group g5 = this.edu.addChildGroup("group5", "group5");
    Stem s0 = this.edu.addChildStem("stem0", "stem0");
    Member subj2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    AttributeDef a0 = this.edu.addChildAttributeDef("attributeDef0", AttributeDefType.perm);
        
    g0.addMember(g1.toSubject());
    g1.grantPriv(g3.toSubject(), AccessPrivilege.UPDATE);
    g2.addMember(g3.toSubject());
    g4.addMember(g5.toSubject());
    s0.grantPriv(g3.toSubject(), NamingPrivilege.CREATE);
    a0.getPrivilegeDelegate().grantPriv(g3.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    
    g5.addMember(subj2.getSubject());
    g3.addMember(subj2.getSubject());
    
    ChangeLogTempToEntity.convertRecords();
    
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
    Membership s0g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s0.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("creators", true), "effective", true, true);
    Membership s0g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s0.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("creators", true), "effective", true, true);
    Membership a0g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a0.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("attrUpdaters", true), "effective", true, true);
    Membership a0g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a0.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("attrUpdaters", true), "effective", true, true);

    flatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    ChangeLogTempToEntity.convertRecords();
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");

    // note that subj2 already had the memberships and privileges...
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 10 new change log entries", 10, newChangeLogCount);
    assertEquals("Shoud have 10 new flat memberships", flatMembershipCount + 10, newFlatMembershipCount);
    
    verifyEffectiveMembershipsAdd(g3g4Mship, g3g5Mship, g2g4Mship, g2g5Mship, g1g4Priv, g1g5Priv, 
        s0g4Priv, s0g5Priv, a0g4Priv, a0g5Priv, g3g4Mship.getContextId());
    
    //##################################
    // try disabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3g4Mship.setEnabled(false);
    g3g4Mship.setEnabledTime(new Timestamp(new Date().getTime() + 100000));

    final Membership MEMBERSHIP2 = g3g4Mship;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP2);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();

    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 10 new change log entries", 10, newChangeLogCount);
    assertEquals("Shoud have 10 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);

    verifyEffectiveMembershipsDelete(g3g4Mship, g3g5Mship, g2g4Mship, g2g5Mship, g1g4Priv, g1g5Priv, 
        s0g4Priv, s0g5Priv, a0g4Priv, a0g5Priv, g3g4Mship.getContextId());
    
    
    //##################################
    // try changing member when disabled

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g3g4Mship.setMember(rootMember);

    final Membership MEMBERSHIP3 = g3g4Mship;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP3);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    assertEquals("Shoud have no changes in flat memberships", flatMembershipCount, newFlatMembershipCount);

    g3g4Mship.setMember(g4.toMember());

    final Membership MEMBERSHIP4 = g3g4Mship;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP4);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    assertEquals("Shoud have no changes in flat memberships", flatMembershipCount, newFlatMembershipCount);

    
    //##################################
    // try enabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3g4Mship.setEnabled(true);
    g3g4Mship.setEnabledTime(new Timestamp(new Date().getTime() - 100000));

    final Membership MEMBERSHIP5 = g3g4Mship;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP5);

        return null;
      }
    });
    
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
    s0g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s0.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("creators", true), "effective", true, true);
    s0g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s0.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("creators", true), "effective", true, true);
    a0g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a0.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("attrUpdaters", true), "effective", true, true);
    a0g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a0.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("attrUpdaters", true), "effective", true, true);
  
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 10 new change log entries", 10, newChangeLogCount);
    assertEquals("Shoud have 10 new flat memberships", flatMembershipCount + 10, newFlatMembershipCount);

    verifyEffectiveMembershipsAdd(g3g4Mship, g3g5Mship, g2g4Mship, g2g5Mship, g1g4Priv, g1g5Priv, 
        s0g4Priv, s0g5Priv, a0g4Priv, a0g5Priv, g3g4Mship.getContextId());
    
    
    //##################################
    // try changing member and make sure change log has correct deletes
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    Membership immediate = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          Group.getDefaultList(), "immediate", true, true);
    immediate.setMember(rootMember);

    final Membership MEMBERSHIP6 = immediate;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP6);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 15 new change log entries", 15, newChangeLogCount);
    assertEquals("Shoud have 5 fewer flat memberships", flatMembershipCount + 5, newFlatMembershipCount);

    verifyEffectiveMembershipsDelete(g3g4Mship, g3g5Mship, g2g4Mship, g2g5Mship, g1g4Priv, g1g5Priv, 
        s0g4Priv, s0g5Priv, a0g4Priv, a0g5Priv, immediate.getContextId());

    
    //##################################
    // try changing member and make sure change log has correct adds
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    immediate.setMember(g4.toMember());

    final Membership MEMBERSHIP7 = immediate;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP7);

        return null;
      }
    });
    
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
    s0g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s0.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("creators", true), "effective", true, true);
    s0g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s0.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("creators", true), "effective", true, true);
    a0g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a0.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("attrUpdaters", true), "effective", true, true);
    a0g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a0.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("attrUpdaters", true), "effective", true, true);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 15 new change log entries", 15, newChangeLogCount);
    assertEquals("Shoud have 5 more flat memberships", flatMembershipCount + 10, newFlatMembershipCount);

    verifyEffectiveMembershipsAdd(g3g4Mship, g3g5Mship, g2g4Mship, g2g5Mship, g1g4Priv, g1g5Priv, 
        s0g4Priv, s0g5Priv, a0g4Priv, a0g5Priv, g3g4Mship.getContextId());

    
    //##################################
    // try deleting membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3.deleteMember(g4.toSubject());

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp")
      .uniqueResult(ChangeLogEntry.class);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 10 new change log entries", 10, newChangeLogCount);
    assertEquals("Shoud have 10 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);

    verifyEffectiveMembershipsDelete(g3g4Mship, g3g5Mship, g2g4Mship, g2g5Mship, g1g4Priv, g1g5Priv, 
        s0g4Priv, s0g5Priv, a0g4Priv, a0g5Priv, changeLogEntry.getContextId());
    
  }


  /**
   * @throws Exception 
   * 
   */
  public void testAccessPrivileges() throws Exception {

    Subject rootSubject = SubjectFinder.findRootSubject();
    Member rootMember = MemberFinder.findBySubject(grouperSession, rootSubject, true);

    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    Member newMember3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, true);
    
    //##################################
    // try a membership add that causes effective memberships
    
    Group g2 = this.edu.addChildGroup("group2", "group2");
    Group g3 = this.edu.addChildGroup("group3", "group3");
    Group g4 = this.edu.addChildGroup("group4", "group4");
    Group g5 = this.edu.addChildGroup("group5", "group5");
    
    g2.addMember(g3.toSubject());
    g4.addMember(g5.toSubject());
    
    g4.addMember(newMember2.getSubject());
    g3.grantPriv(newMember2.getSubject(), AccessPrivilege.OPTIN);
    
    ChangeLogTempToEntity.convertRecords();

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

    int flatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");

    ChangeLogTempToEntity.convertRecords();

    // note that subj2 already had the privilege...
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    int newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 new flat memberships", flatMembershipCount + 2, newFlatMembershipCount);
    
    verifyEffectiveAccessPrivilegesAdd(g3g4Priv, g3g5Priv, g3g4Priv.getContextId());
    
    //##################################
    // try disabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3g4Priv.setEnabled(false);
    g3g4Priv.setEnabledTime(new Timestamp(new Date().getTime() + 100000));

    final Membership MEMBERSHIP = g3g4Priv;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);

    verifyEffectiveAccessPrivilegesDelete(g3g4Priv, g3g5Priv, g3g4Priv.getContextId());
    
    
    //##################################
    // try changing member when disabled

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g3g4Priv.setMember(rootMember);

    final Membership MEMBERSHIP2 = g3g4Priv;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP2);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    assertEquals("Shoud have no changes flat memberships", flatMembershipCount, newFlatMembershipCount);

    g3g4Priv.setMember(g4.toMember());

    final Membership MEMBERSHIP3 = g3g4Priv;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP3);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    assertEquals("Shoud have no changes flat memberships", flatMembershipCount, newFlatMembershipCount);

    
    //##################################
    // try enabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3g4Priv.setEnabled(true);
    g3g4Priv.setEnabledTime(new Timestamp(new Date().getTime() - 100000));

    final Membership MEMBERSHIP4 = g3g4Priv;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP4);

        return null;
      }
    });

    g3g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("optins", true), "immediate", true, true);
    g3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("optins", true), "effective", true, true);
  
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 new flat memberships", flatMembershipCount + 2, newFlatMembershipCount);

    verifyEffectiveAccessPrivilegesAdd(g3g4Priv, g3g5Priv, g3g4Priv.getContextId());
    
    
    //##################################
    // try changing member and make sure change log has correct deletes
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    Membership immediate = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("optins", true), "immediate", true, true);
    immediate.setMember(rootMember);

    final Membership MEMBERSHIP5 = immediate;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP5);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);
    assertEquals("Shoud have 1 fewer flat memberships", flatMembershipCount + 1, newFlatMembershipCount);

    verifyEffectiveAccessPrivilegesDelete(g3g4Priv, g3g5Priv, immediate.getContextId());

    
    //##################################
    // try changing member and make sure change log has correct adds
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    immediate.setMember(g4.toMember());

    final Membership MEMBERSHIP6 = immediate;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP6);

        return null;
      }
    });
    
    g3g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("optins", true), "immediate", true, true);
    g3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("optins", true), "effective", true, true);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);
    assertEquals("Shoud have 1 more flat memberships", flatMembershipCount + 2, newFlatMembershipCount);

    verifyEffectiveAccessPrivilegesAdd(g3g4Priv, g3g5Priv, g3g4Priv.getContextId());

    
    //##################################
    // try deleting membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3.revokePriv(g4.toSubject(), AccessPrivilege.OPTIN);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp")
      .uniqueResult(ChangeLogEntry.class);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);

    verifyEffectiveAccessPrivilegesDelete(g3g4Priv, g3g5Priv, changeLogEntry.getContextId());
    
    //##################################
    // try multiple immediate memberships
    
    Group group = this.edu.addChildGroup("test1", "test1");

    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    flatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    group.grantPriv(newMember1.getSubject(), AccessPrivilege.UPDATE);
    group.grantPriv(newMember2.getSubject(), AccessPrivilege.UPDATE);
    Group group2 = this.edu.addChildGroup("test2", "test2");
    group.grantPriv(group2.toSubject(), AccessPrivilege.UPDATE);
    group.grantPriv(newMember3.getSubject(), AccessPrivilege.UPDATE);
    ChangeLogTempToEntity.convertRecords();

    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 7 change log entries", 7, changeLogEntries.size());
    assertEquals("Should have 7 new flat memberships", flatMembershipCount + 7, newFlatMembershipCount);
    
    group.revokePriv(newMember1.getSubject(), AccessPrivilege.UPDATE);
    group.revokePriv(newMember2.getSubject(), AccessPrivilege.UPDATE);
    group2.delete();
    group.revokePriv(newMember3.getSubject(), AccessPrivilege.UPDATE);
    ChangeLogTempToEntity.convertRecords();

    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 7 change log entries", 7, changeLogEntries.size());
    assertEquals("Should have 7 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);
  }
  
  /**
   * @throws Exception 
   * 
   */
  public void testNamingPrivileges() throws Exception {

    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    Member newMember3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, true);
    
    //##################################
    // try a membership add that causes effective memberships
    
    Stem s3 = this.edu.addChildStem("stem3", "stem3");
    Group g4 = this.edu.addChildGroup("group4", "group4");
    Group g5 = this.edu.addChildGroup("group5", "group5");
    
    g4.addMember(g5.toSubject());
    
    g4.addMember(newMember2.getSubject());
    s3.grantPriv(newMember2.getSubject(), NamingPrivilege.STEM);
    
    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    s3.grantPriv(g4.toSubject(), NamingPrivilege.STEM);

    Membership s3g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("stemmers", true), "immediate", true, true);
    Membership s3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("stemmers", true), "effective", true, true);

    int flatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");

    ChangeLogTempToEntity.convertRecords();

    // note that subj2 already had the privilege...
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    int newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 new flat memberships", flatMembershipCount + 2, newFlatMembershipCount);

    verifyEffectiveNamingPrivilegesAdd(s3g4Priv, s3g5Priv, s3g4Priv.getContextId());
    
    //##################################
    // try disabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    s3g4Priv.setEnabled(false);
    s3g4Priv.setEnabledTime(new Timestamp(new Date().getTime() + 100000));

    final Membership MEMBERSHIP = s3g4Priv;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);

    verifyEffectiveNamingPrivilegesDelete(s3g4Priv, s3g5Priv, s3g4Priv.getContextId());
    
    
    //##################################
    // try changing member when disabled

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    s3g4Priv.setMember(newMember1);

    final Membership MEMBERSHIP2 = s3g4Priv;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP2);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    assertEquals("Shoud have no changes to flat memberships", flatMembershipCount, newFlatMembershipCount);

    s3g4Priv.setMember(g4.toMember());

    final Membership MEMBERSHIP3 = s3g4Priv;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP3);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    assertEquals("Shoud have no changes to flat memberships", flatMembershipCount, newFlatMembershipCount);

    
    //##################################
    // try enabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    s3g4Priv.setEnabled(true);
    s3g4Priv.setEnabledTime(new Timestamp(new Date().getTime() - 100000));

    final Membership MEMBERSHIP4 = s3g4Priv;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP4);

        return null;
      }
    });

    s3g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("stemmers", true), "immediate", true, true);
    s3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("stemmers", true), "effective", true, true);
  
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 new flat memberships", flatMembershipCount + 2, newFlatMembershipCount);

    verifyEffectiveNamingPrivilegesAdd(s3g4Priv, s3g5Priv, s3g4Priv.getContextId());
    
    
    //##################################
    // try changing member and make sure change log has correct deletes
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    Membership immediate = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("stemmers", true), "immediate", true, true);
    immediate.setMember(newMember1);

    final Membership MEMBERSHIP5 = immediate;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP5);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);
    assertEquals("Shoud have 1 fewer flat memberships", flatMembershipCount + 1, newFlatMembershipCount);

    verifyEffectiveNamingPrivilegesDelete(s3g4Priv, s3g5Priv, immediate.getContextId());

    
    //##################################
    // try changing member and make sure change log has correct adds
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    immediate.setMember(g4.toMember());

    final Membership MEMBERSHIP6 = immediate;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP6);

        return null;
      }
    });
    
    s3g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("stemmers", true), "immediate", true, true);
    s3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("stemmers", true), "effective", true, true);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);
    assertEquals("Shoud have 1 more flat memberships", flatMembershipCount + 2, newFlatMembershipCount);

    verifyEffectiveNamingPrivilegesAdd(s3g4Priv, s3g5Priv, s3g4Priv.getContextId());

    
    //##################################
    // try deleting membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    s3.revokePriv(g4.toSubject(), NamingPrivilege.STEM);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp")
      .uniqueResult(ChangeLogEntry.class);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);

    verifyEffectiveNamingPrivilegesDelete(s3g4Priv, s3g5Priv, changeLogEntry.getContextId());
    
    
    //##################################
    // try multiple immediate memberships
    
    Stem stem = this.edu.addChildStem("test1", "test1");

    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    flatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    stem.grantPriv(newMember1.getSubject(), NamingPrivilege.STEM);
    stem.grantPriv(newMember2.getSubject(), NamingPrivilege.STEM);
    Group group = this.edu.addChildGroup("test", "test");
    stem.grantPriv(group.toSubject(), NamingPrivilege.STEM);
    stem.grantPriv(newMember3.getSubject(), NamingPrivilege.STEM);
    ChangeLogTempToEntity.convertRecords();

    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 7 change log entries", 7, changeLogEntries.size());
    assertEquals("Should have 7 new flat memberships", flatMembershipCount + 7, newFlatMembershipCount);
    
    stem.revokePriv(newMember1.getSubject(), NamingPrivilege.STEM);
    stem.revokePriv(newMember2.getSubject(), NamingPrivilege.STEM);
    group.delete();
    stem.revokePriv(newMember3.getSubject(), NamingPrivilege.STEM);
    ChangeLogTempToEntity.convertRecords();

    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 7 change log entries", 7, changeLogEntries.size());
    assertEquals("Should have 7 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);
  }


  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefPrivileges() throws Exception {

    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    Member newMember3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, true);

    //##################################
    // try a membership add that causes effective memberships
    
    AttributeDef a3 = this.edu.addChildAttributeDef("attributeDef3", AttributeDefType.perm);
    Group g4 = this.edu.addChildGroup("group4", "group4");
    Group g5 = this.edu.addChildGroup("group5", "group5");
    
    g4.addMember(g5.toSubject());
    
    g4.addMember(newMember2.getSubject());
    a3.getPrivilegeDelegate().grantPriv(newMember2.getSubject(), AttributeDefPrivilege.ATTR_OPTIN, true);
    
    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    final AttributeDef ATTRIBUTE_DEF = a3;
    final Subject SUBJECT = g4.toSubject();
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {

        ATTRIBUTE_DEF.getPrivilegeDelegate().grantPriv(SUBJECT, AttributeDefPrivilege.ATTR_OPTIN, true);

        return null;
      }
    });

    Membership a3g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("attrOptins", true), "immediate", true, true);
    Membership a3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("attrOptins", true), "effective", true, true);

    int flatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    ChangeLogTempToEntity.convertRecords();

    // note that subj2 already had the privilege...
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    int newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 new flat memberships", flatMembershipCount + 2, newFlatMembershipCount);

    verifyEffectiveAttributeDefPrivilegesAdd(a3g4Priv, a3g5Priv, a3g4Priv.getContextId());
    
    //##################################
    // try disabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    a3g4Priv.setEnabled(false);
    a3g4Priv.setEnabledTime(new Timestamp(new Date().getTime() + 100000));

    final Membership MEMBERSHIP = a3g4Priv;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {

        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);

    verifyEffectiveAttributeDefPrivilegesDelete(a3g4Priv, a3g5Priv, a3g4Priv.getContextId());
    
    
    //##################################
    // try changing member when disabled

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    a3g4Priv.setMember(newMember1);

    final Membership MEMBERSHIP2 = a3g4Priv;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP2);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    assertEquals("Shoud have no changes to flat memberships", flatMembershipCount, newFlatMembershipCount);

    a3g4Priv.setMember(g4.toMember());

    final Membership MEMBERSHIP3 = a3g4Priv;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP3);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    assertEquals("Shoud have no changes to flat memberships", flatMembershipCount, newFlatMembershipCount);

    
    //##################################
    // try enabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    a3g4Priv.setEnabled(true);
    a3g4Priv.setEnabledTime(new Timestamp(new Date().getTime() - 100000));

    final Membership MEMBERSHIP4 = a3g4Priv;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP4);

        return null;
      }
    });

    a3g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("attrOptins", true), "immediate", true, true);
    a3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("attrOptins", true), "effective", true, true);
  
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 new flat memberships", flatMembershipCount + 2, newFlatMembershipCount);

    verifyEffectiveAttributeDefPrivilegesAdd(a3g4Priv, a3g5Priv, a3g4Priv.getContextId());
    
    
    //##################################
    // try changing member and make sure change log has correct deletes
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    Membership immediate = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("attrOptins", true), "immediate", true, true);
    immediate.setMember(newMember1);

    final Membership MEMBERSHIP5 = immediate;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP5);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);
    assertEquals("Shoud have 1 fewer flat memberships", flatMembershipCount + 1, newFlatMembershipCount);

    verifyEffectiveAttributeDefPrivilegesDelete(a3g4Priv, a3g5Priv, immediate.getContextId());

    
    //##################################
    // try changing member and make sure change log has correct adds
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    immediate.setMember(g4.toMember());

    final Membership MEMBERSHIP6 = immediate;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP6);

        return null;
      }
    });
    
    a3g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("attrOptins", true), "immediate", true, true);
    a3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("attrOptins", true), "effective", true, true);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);
    assertEquals("Shoud have 1 more flat memberships", flatMembershipCount + 2, newFlatMembershipCount);

    verifyEffectiveAttributeDefPrivilegesAdd(a3g4Priv, a3g5Priv, a3g4Priv.getContextId());

    
    //##################################
    // try deleting membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {

        ATTRIBUTE_DEF.getPrivilegeDelegate().revokePriv(SUBJECT, AttributeDefPrivilege.ATTR_OPTIN, true);

        return null;
      }
    });
    
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp")
      .uniqueResult(ChangeLogEntry.class);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);

    verifyEffectiveAttributeDefPrivilegesDelete(a3g4Priv, a3g5Priv, changeLogEntry.getContextId());
   
    //##################################
    // try multiple immediate memberships
    
    AttributeDef attrDef = this.edu.addChildAttributeDef("test1", AttributeDefType.perm);

    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    flatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    attrDef.getPrivilegeDelegate().grantPriv(newMember1.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    attrDef.getPrivilegeDelegate().grantPriv(newMember2.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    Group group = this.edu.addChildGroup("test", "test");
    attrDef.getPrivilegeDelegate().grantPriv(group.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    attrDef.getPrivilegeDelegate().grantPriv(newMember3.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    ChangeLogTempToEntity.convertRecords();

    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 7 change log entries", 7, changeLogEntries.size());
    assertEquals("Should have 7 new flat memberships", flatMembershipCount + 7, newFlatMembershipCount);
    
    attrDef.getPrivilegeDelegate().revokePriv(newMember1.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    attrDef.getPrivilegeDelegate().revokePriv(newMember2.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    group.delete();
    attrDef.getPrivilegeDelegate().revokePriv(newMember3.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    ChangeLogTempToEntity.convertRecords();

    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 7 change log entries", 7, changeLogEntries.size());
    assertEquals("Should have 7 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);
  }

  /**
   * @throws Exception 
   * 
   */
  public void testCustomListMemberships() throws Exception {

    Subject rootSubject = SubjectFinder.findRootSubject();
    Member rootMember = MemberFinder.findBySubject(grouperSession, rootSubject, true);
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    Member newMember3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, true);

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
    
    ChangeLogTempToEntity.convertRecords();

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

    int flatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");

    ChangeLogTempToEntity.convertRecords();

    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    int newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 more flat memberships", flatMembershipCount + 2, newFlatMembershipCount);

    verifyEffectiveCustomListMembershipsAdd(g3g4Mship, g3g5Mship, g3g4Mship.getContextId());
    
    //##################################
    // try disabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3g4Mship.setEnabled(false);
    g3g4Mship.setEnabledTime(new Timestamp(new Date().getTime() + 100000));

    final Membership MEMBERSHIP = g3g4Mship;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);

    verifyEffectiveCustomListMembershipsDelete(g3g4Mship, g3g5Mship, g3g4Mship.getContextId());
    
    
    //##################################
    // try changing member when disabled

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g3g4Mship.setMember(rootMember);

    final Membership MEMBERSHIP2 = g3g4Mship;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP2);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    assertEquals("Shoud have no changes to flat memberships", flatMembershipCount, newFlatMembershipCount);

    g3g4Mship.setMember(g4.toMember());

    final Membership MEMBERSHIP3 = g3g4Mship;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP3);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    assertEquals("Shoud have no changes to flat memberships", flatMembershipCount, newFlatMembershipCount);

    
    //##################################
    // try enabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3g4Mship.setEnabled(true);
    g3g4Mship.setEnabledTime(new Timestamp(new Date().getTime() - 100000));

    final Membership MEMBERSHIP4 = g3g4Mship;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP4);

        return null;
      }
    });
    
    g3g4Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("customList", true), "immediate", true, true);
    g3g5Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("customList", true), "effective", true, true);
  
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 new flat memberships", flatMembershipCount + 2, newFlatMembershipCount);

    verifyEffectiveCustomListMembershipsAdd(g3g4Mship, g3g5Mship, g3g4Mship.getContextId());
    
    
    //##################################
    // try changing member and make sure change log has correct deletes
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    Membership immediate = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("customList", true), "immediate", true, true);
    immediate.setMember(rootMember);

    final Membership MEMBERSHIP5 = immediate;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP5);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);
    assertEquals("Shoud have 1 fewer flat memberships", flatMembershipCount + 1, newFlatMembershipCount);

    verifyEffectiveCustomListMembershipsDelete(g3g4Mship, g3g5Mship, immediate.getContextId());

    
    //##################################
    // try changing member and make sure change log has correct adds
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    immediate.setMember(g4.toMember());

    final Membership MEMBERSHIP6 = immediate;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP6);

        return null;
      }
    });
    
    g3g4Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("customList", true), "immediate", true, true);
    g3g5Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("customList", true), "effective", true, true);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);
    assertEquals("Shoud have 1 more flat memberships", flatMembershipCount + 2, newFlatMembershipCount);

    verifyEffectiveCustomListMembershipsAdd(g3g4Mship, g3g5Mship, immediate.getContextId());

    
    //##################################
    // try deleting membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g3.deleteMember(g4.toSubject(), customList);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp")
      .uniqueResult(ChangeLogEntry.class);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    assertEquals("Shoud have 2 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);

    verifyEffectiveCustomListMembershipsDelete(g3g4Mship, g3g5Mship, changeLogEntry.getContextId());
    
    //##################################
    // try multiple immediate memberships
    
    Group group = this.edu.addChildGroup("test1", "test1");
    group.addType(groupType);

    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    flatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    group.addMember(newMember1.getSubject(), customList);
    group.addMember(newMember2.getSubject(), customList);
    Group group2 = this.edu.addChildGroup("test", "test");
    group2.addType(groupType);
    group.addMember(group2.toSubject(), customList);
    group.addMember(newMember3.getSubject(), customList);
    ChangeLogTempToEntity.convertRecords();

    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 4 change log entries", 4, changeLogEntries.size());
    assertEquals("Should have 7 new flat memberships", flatMembershipCount + 7, newFlatMembershipCount);
    
    group.deleteMember(newMember1.getSubject(), customList);
    group.deleteMember(newMember2.getSubject(), customList);
    group2.delete();
    group.deleteMember(newMember3.getSubject(), customList);
    ChangeLogTempToEntity.convertRecords();

    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 4 change log entries", 4, changeLogEntries.size());
    assertEquals("Should have 7 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);
  }
  

  /**
   * @throws Exception 
   * 
   */
  public void testCompositeMemberships() throws Exception {

    Subject rootSubject = SubjectFinder.findRootSubject();
    Member rootMember = MemberFinder.findBySubject(grouperSession, rootSubject, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);

    
    //##################################
    // add composite
    
    Group g1 = this.edu.addChildGroup("group1", "group1");
    Group g2 = this.edu.addChildGroup("group2", "group2");
    Group g3 = this.edu.addChildGroup("group3", "group3");
    Group g5 = this.edu.addChildGroup("group5", "group5");
    Group g6 = this.edu.addChildGroup("group6", "group6");
    
    g5.grantPriv(g1.toSubject(), AccessPrivilege.UPDATE);
    g6.addMember(g1.toSubject());
    g2.addMember(member1.getSubject());
    
    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g1.addCompositeMember(CompositeType.UNION, g2, g3);

    int flatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    ChangeLogTempToEntity.convertRecords();

    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    int newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 3 new change log entries - 1 composite, 2 effective", 3, newChangeLogCount);
    assertEquals("Shoud have 3 new flat memberships", flatMembershipCount + 3, newFlatMembershipCount);


    //##################################
    // delete composite
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g1.deleteCompositeMember();

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 3 new change log entries - 1 composite, 2 effective", 3, newChangeLogCount);
    assertEquals("Shoud have 3 fewer flat memberships", flatMembershipCount, newFlatMembershipCount);


    //##################################
    // add immediate that causes composite
    
    g1.addCompositeMember(CompositeType.UNION, g2, g3);
    g2.deleteMember(member1.getSubject());

    ChangeLogTempToEntity.convertRecords();
    
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    assertEquals("Shoud have 1 fewer to flat memberships", flatMembershipCount - 1, newFlatMembershipCount);

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g2.addMember(member1.getSubject());

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 4 new change log entries - 1 composite, 1 immediate, 2 effective", 4, newChangeLogCount);
    assertEquals("Shoud have 4 new flat memberships", flatMembershipCount + 3, newFlatMembershipCount);
    
    
    //##################################
    // try disabling membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    Membership immediate = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g2.getUuid(), member1.getUuid(), 
          Group.getDefaultList(), "immediate", true, true);
    
    immediate.setEnabled(false);
    immediate.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    GrouperDAOFactory.getFactory().getMembership().update(immediate);

    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 4 new change log entries - 1 composite, 1 immediate, 2 effective", 4, newChangeLogCount);
    assertEquals("Shoud have 4 fewer flat memberships", flatMembershipCount - 1, newFlatMembershipCount);

    
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
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    assertEquals("Shoud have no changes to flat memberships", flatMembershipCount - 1, newFlatMembershipCount);

    immediate.setMember(member1);
    GrouperDAOFactory.getFactory().getMembership().update(immediate);

    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);
    assertEquals("Shoud have no changes to flat memberships", flatMembershipCount - 1, newFlatMembershipCount);
    
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
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 4 new change log entries - 1 composite, 1 immediate, 2 effective", 4, newChangeLogCount);
    assertEquals("Shoud have 4 new flat memberships", flatMembershipCount + 3, newFlatMembershipCount);

    
    //##################################
    // try changing member
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    immediate = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g2.getUuid(), member1.getUuid(), 
          Group.getDefaultList(), "immediate", true, true);
    immediate.setMember(rootMember);
    GrouperDAOFactory.getFactory().getMembership().update(immediate);

    ChangeLogTempToEntity.convertRecords();


    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 8 new change log entries - 2 composite, 2 immediate, 4 effective", 8, newChangeLogCount);
    assertEquals("Shoud have 4 new flat memberships and 4 deleted flat memberships", flatMembershipCount + 3, newFlatMembershipCount);

    
    //##################################
    // try deleting membership now
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g2.deleteMember(rootMember);

    ChangeLogTempToEntity.convertRecords();


    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    newFlatMembershipCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_flat_memberships");
    
    assertEquals("Should have 4 new change log entries - 1 composite, 1 immediate, 2 effective", 4, newChangeLogCount);
    assertEquals("Shoud have 4 fewer flat memberships", flatMembershipCount - 1, newFlatMembershipCount);

  }
  
  /**
   * @throws Exception
   */
  public void testNonFlattenedMemberships() throws Exception {

    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    
    Group g1 = this.edu.addChildGroup("group1", "group1");
    Group g2 = this.edu.addChildGroup("group2", "group2");
    Group g3 = this.edu.addChildGroup("group3", "group3");
    Group g4 = this.edu.addChildGroup("group5", "group5");
    
    g1.addCompositeMember(CompositeType.UNION, g2, g3);
    g2.addMember(g4.toSubject());
    g2.addMember(member1.getSubject());
    
    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g4.addMember(member1.getSubject());
    
    GrouperLoaderConfig.testConfig.put("changeLog.includeNonFlattenedMemberships", "true");
    
    ChangeLogTempToEntity.convertRecords();

    // should have only 1 immediate and only 1 flattened membership
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 change log entries", 2, newChangeLogCount);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(Group.getDefaultList().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldId));
    assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.memberId));
    assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(g4.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g4.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));

    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g4.deleteMember(member1.getSubject());

    ChangeLogTempToEntity.convertRecords();

    // should have only 1 immediate and only 1 flattened membership
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 change log entries", 2, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(Group.getDefaultList().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldId));
    assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.memberId));
    assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals(g4.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g4.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));


    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g4.addMember(member2.getSubject());

    ChangeLogTempToEntity.convertRecords();

    // should have only 1 immediate, 1 composite and 3 flattened memberships
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 5 change log entries", 5, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(Group.getDefaultList().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldId));
    assertEquals(member2.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.memberId));
    assertEquals(member2.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(member2.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(g4.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g4.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = 'composite'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(Group.getDefaultList().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldId));
    assertEquals(member2.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.memberId));
    assertEquals(member2.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(member2.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(g1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g4.deleteMember(member2.getSubject());

    ChangeLogTempToEntity.convertRecords();

    // should have only 1 immediate, 1 composite and 3 flattened memberships
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 5 change log entries", 5, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(Group.getDefaultList().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldId));
    assertEquals(member2.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.memberId));
    assertEquals(member2.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(member2.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals(g4.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g4.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = 'composite'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(Group.getDefaultList().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldId));
    assertEquals(member2.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.memberId));
    assertEquals(member2.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(member2.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals(g1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    
    // now try adding a member that will only cause an immediate membership change log entry and no flattened change log entries
    g4.addMember(member1.getSubject());
    g2.deleteMember(member1.getSubject());

    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g2.addMember(member1.getSubject());
    
    ChangeLogTempToEntity.convertRecords();

    // should have only 1 immediate
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 1 change log entry", 1, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(Group.getDefaultList().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldId));
    assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.memberId));
    assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(g2.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g2.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
  }
  

  /**
   * @throws Exception
   */
  public void testNonFlattenedAccessPrivileges() throws Exception {

    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    
    Group g1 = this.edu.addChildGroup("group1", "group1");
    Group g2 = this.edu.addChildGroup("group2", "group2");
    
    g1.grantPriv(g2.toSubject(), AccessPrivilege.UPDATE);
    
    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g2.addMember(member1.getSubject());
    
    GrouperLoaderConfig.testConfig.put("changeLog.includeNonFlattenedPrivileges", "true");
    
    ChangeLogTempToEntity.convertRecords();

    // should have only 2 flattened (non-flattened memberships are disabled)
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 change log entries", 2, newChangeLogCount);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertNull(changeLogEntry);

    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g1.grantPriv(member1.getSubject(), AccessPrivilege.UPDATE);
        
    ChangeLogTempToEntity.convertRecords();

    // should have only 1 immediate only
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 1 change log entry", 1, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string11 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("update", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals("access", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(FieldFinder.find("updaters", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId));
    assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.memberId));
    assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals(g1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(g1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g1.grantPriv(member2.getSubject(), AccessPrivilege.UPDATE);
        
    ChangeLogTempToEntity.convertRecords();

    // should have 1 immediate and 1 flattened
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 change log entry", 2, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string11 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("update", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals("access", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(FieldFinder.find("updaters", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId));
    assertEquals(member2.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.memberId));
    assertEquals(member2.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(member2.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals(g1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(g1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
   
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g1.revokePriv(member2.getSubject(), AccessPrivilege.UPDATE);
        
    ChangeLogTempToEntity.convertRecords();

    // should have 1 immediate and 1 flattened
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 change log entry", 2, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string11 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("update", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals("access", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(FieldFinder.find("updaters", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.fieldId));
    assertEquals(member2.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.memberId));
    assertEquals(member2.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(member2.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals(g1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(g1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g1.revokePriv(member1.getSubject(), AccessPrivilege.UPDATE);
        
    ChangeLogTempToEntity.convertRecords();

    // should have only 1 immediate only
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 1 change log entry", 1, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string11 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("update", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals("access", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(FieldFinder.find("updaters", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.fieldId));
    assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.memberId));
    assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals(g1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(g1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g2.deleteMember(member1.getSubject());

    ChangeLogTempToEntity.convertRecords();

    // should have only 2 flattened (non-flattened memberships are disabled)
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 change log entries", 2, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertNull(changeLogEntry);
  }
  
  /**
   * @throws Exception
   */
  public void testNonFlattenedAttributeDefPrivileges() throws Exception {

    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    
    AttributeDef a1 = this.edu.addChildAttributeDef("attributeDef1", AttributeDefType.perm);
    Group g2 = this.edu.addChildGroup("group2", "group2");
    
    a1.getPrivilegeDelegate().grantPriv(g2.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    
    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g2.addMember(member1.getSubject());
    
    GrouperLoaderConfig.testConfig.put("changeLog.includeNonFlattenedPrivileges", "true");
    
    ChangeLogTempToEntity.convertRecords();

    // should have only 2 flattened (non-flattened memberships are disabled)
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 change log entries", 2, newChangeLogCount);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertNull(changeLogEntry);

    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    a1.getPrivilegeDelegate().grantPriv(member1.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
        
    ChangeLogTempToEntity.convertRecords();

    // should have only 1 immediate only
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 1 change log entry", 1, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string11 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("attrUpdate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(FieldFinder.find("attrUpdaters", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId));
    assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.memberId));
    assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals(a1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(a1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    a1.getPrivilegeDelegate().grantPriv(member2.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
        
    ChangeLogTempToEntity.convertRecords();

    // should have 1 immediate and 1 flattened
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 change log entry", 2, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string11 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("attrUpdate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(FieldFinder.find("attrUpdaters", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId));
    assertEquals(member2.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.memberId));
    assertEquals(member2.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(member2.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals(a1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(a1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
   
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    a1.getPrivilegeDelegate().revokePriv(member2.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    
    ChangeLogTempToEntity.convertRecords();

    // should have 1 immediate and 1 flattened
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 change log entry", 2, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string11 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("attrUpdate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(FieldFinder.find("attrUpdaters", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.fieldId));
    assertEquals(member2.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.memberId));
    assertEquals(member2.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(member2.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals(a1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(a1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    a1.getPrivilegeDelegate().revokePriv(member1.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);

    ChangeLogTempToEntity.convertRecords();

    // should have only 1 immediate only
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 1 change log entry", 1, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string11 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("attrUpdate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(FieldFinder.find("attrUpdaters", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.fieldId));
    assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.memberId));
    assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals(a1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(a1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g2.deleteMember(member1.getSubject());

    ChangeLogTempToEntity.convertRecords();

    // should have only 2 flattened (non-flattened memberships are disabled)
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 change log entries", 2, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertNull(changeLogEntry);
  }
  

  /**
   * @throws Exception
   */
  public void testNonFlattenedNamingPrivileges() throws Exception {

    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    
    Stem s1 = this.edu.addChildStem("stem1", "stem1");
    Group g2 = this.edu.addChildGroup("group2", "group2");
    
    s1.grantPriv(g2.toSubject(), NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g2.addMember(member1.getSubject());
    
    GrouperLoaderConfig.testConfig.put("changeLog.includeNonFlattenedPrivileges", "true");
    
    ChangeLogTempToEntity.convertRecords();

    // should have only 2 flattened (non-flattened memberships are disabled)
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 change log entries", 2, newChangeLogCount);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertNull(changeLogEntry);

    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    s1.grantPriv(member1.getSubject(), NamingPrivilege.CREATE);
        
    ChangeLogTempToEntity.convertRecords();

    // should have only 1 immediate only
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 1 change log entry", 1, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string11 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("create", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals("naming", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(FieldFinder.find("creators", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId));
    assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.memberId));
    assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals(s1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(s1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    s1.grantPriv(member2.getSubject(), NamingPrivilege.CREATE);
        
    ChangeLogTempToEntity.convertRecords();

    // should have 1 immediate and 1 flattened
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 change log entry", 2, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string11 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("create", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals("naming", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(FieldFinder.find("creators", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId));
    assertEquals(member2.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.memberId));
    assertEquals(member2.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(member2.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals(s1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(s1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
   
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    s1.revokePriv(member2.getSubject(), NamingPrivilege.CREATE);
        
    ChangeLogTempToEntity.convertRecords();

    // should have 1 immediate and 1 flattened
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 change log entry", 2, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string11 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("create", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals("naming", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(FieldFinder.find("creators", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.fieldId));
    assertEquals(member2.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.memberId));
    assertEquals(member2.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(member2.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals(s1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(s1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    s1.revokePriv(member1.getSubject(), NamingPrivilege.CREATE);
        
    ChangeLogTempToEntity.convertRecords();

    // should have only 1 immediate only
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 1 change log entry", 1, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string11 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("create", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals("naming", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(FieldFinder.find("creators", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.fieldId));
    assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.memberId));
    assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals(s1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(s1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    g2.deleteMember(member1.getSubject());

    ChangeLogTempToEntity.convertRecords();

    // should have only 2 flattened (non-flattened memberships are disabled)
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 2 change log entries", 2, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string05 = 'immediate'")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertNull(changeLogEntry);
  }

  private void verifyEffectiveCustomListMembershipsAdd(Membership g3g4Mship, Membership g3g5Mship, String contextId) {

    assertTrue(!StringUtils.isBlank(contextId));

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .setString("subjectId", g3g4Mship.getMemberSubjectId())
      .setString("groupId", g3g4Mship.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("customList", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(g3g4Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(g3g4Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(g3g4Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g3g4Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    FlatMembership flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g3g4Mship.getOwnerGroupId(), g3g4Mship.getMemberUuid(), g3g4Mship.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerGroupId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .setString("subjectId", g3g5Mship.getMemberSubjectId())
      .setString("groupId", g3g5Mship.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("customList", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(g3g5Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(g3g5Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(g3g5Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g3g5Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g3g5Mship.getOwnerGroupId(), g3g5Mship.getMemberUuid(), g3g5Mship.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerGroupId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());
  }
  
  private void verifyEffectiveCustomListMembershipsDelete(Membership g3g4Mship, Membership g3g5Mship, String contextId) {

    assertTrue(!StringUtils.isBlank(contextId));

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .setString("subjectId", g3g4Mship.getMemberSubjectId())
      .setString("groupId", g3g4Mship.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("customList", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(g3g4Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(g3g4Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals(g3g4Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g3g4Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);

    FlatMembership flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g3g4Mship.getOwnerGroupId(), g3g4Mship.getMemberUuid(), g3g4Mship.getFieldId());
    assertNull(flatMship);
  
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .setString("subjectId", g3g5Mship.getMemberSubjectId())
      .setString("groupId", g3g5Mship.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("customList", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(g3g5Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(g3g5Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals(g3g5Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g3g5Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g3g5Mship.getOwnerGroupId(), g3g5Mship.getMemberUuid(), g3g5Mship.getFieldId());
    assertNull(flatMship);
  }
  
  
  private void verifyEffectiveAccessPrivilegesAdd(Membership g3g4Priv, Membership g3g5Priv, String contextId) {

    assertTrue(!StringUtils.isBlank(contextId));

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", g3g4Priv.getMemberSubjectId())
      .setString("groupId", g3g4Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("optin", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(g3g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(g3g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("access", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(g3g4Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(g3g4Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);

    FlatMembership flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g3g4Priv.getOwnerGroupId(), g3g4Priv.getMemberUuid(), g3g4Priv.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerGroupId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", g3g5Priv.getMemberSubjectId())
      .setString("groupId", g3g5Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("optin", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(g3g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(g3g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("access", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(g3g5Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(g3g5Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g3g5Priv.getOwnerGroupId(), g3g5Priv.getMemberUuid(), g3g5Priv.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerGroupId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());
  }
  
  private void verifyEffectiveAccessPrivilegesDelete(Membership g3g4Priv, Membership g3g5Priv, String contextId) {
    
    assertTrue(!StringUtils.isBlank(contextId));

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", g3g4Priv.getMemberSubjectId())
      .setString("groupId", g3g4Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("optin", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(g3g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(g3g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("access", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(g3g4Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(g3g4Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    FlatMembership flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g3g4Priv.getOwnerGroupId(), g3g4Priv.getMemberUuid(), g3g4Priv.getFieldId());
    assertNull(flatMship);
  
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", g3g5Priv.getMemberSubjectId())
      .setString("groupId", g3g5Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("optin", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(g3g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(g3g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("access", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(g3g5Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(g3g5Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g3g5Priv.getOwnerGroupId(), g3g5Priv.getMemberUuid(), g3g5Priv.getFieldId());
    assertNull(flatMship);
  }
  
  private void verifyEffectiveAttributeDefPrivilegesAdd(Membership a3g4Priv, Membership a3g5Priv, String contextId) {

    assertTrue(!StringUtils.isBlank(contextId));

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :attrDefId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", a3g4Priv.getMemberSubjectId())
      .setString("attrDefId", a3g4Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("attrOptin", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(a3g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(a3g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(a3g4Priv.getAttributeDef().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(a3g4Priv.getAttributeDef().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);

    FlatMembership flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(a3g4Priv.getOwnerAttrDefId(), a3g4Priv.getMemberUuid(), a3g4Priv.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerAttrDefId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());
    

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :attrDefId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", a3g5Priv.getMemberSubjectId())
      .setString("attrDefId", a3g5Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("attrOptin", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(a3g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(a3g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(a3g5Priv.getAttributeDef().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(a3g5Priv.getAttributeDef().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(a3g5Priv.getOwnerAttrDefId(), a3g5Priv.getMemberUuid(), a3g5Priv.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerAttrDefId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());
  }
  
  private void verifyEffectiveAttributeDefPrivilegesDelete(Membership a3g4Priv, Membership a3g5Priv, String contextId) {
    
    assertTrue(!StringUtils.isBlank(contextId));

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :attrDefId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", a3g4Priv.getMemberSubjectId())
      .setString("attrDefId", a3g4Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("attrOptin", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(a3g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(a3g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(a3g4Priv.getAttributeDef().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(a3g4Priv.getAttributeDef().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
  
    FlatMembership flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(a3g4Priv.getOwnerAttrDefId(), a3g4Priv.getMemberUuid(), a3g4Priv.getFieldId());
    assertNull(flatMship);
  
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :attrDefId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", a3g5Priv.getMemberSubjectId())
      .setString("attrDefId", a3g5Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("attrOptin", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(a3g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(a3g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(a3g5Priv.getAttributeDef().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(a3g5Priv.getAttributeDef().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(a3g5Priv.getOwnerAttrDefId(), a3g5Priv.getMemberUuid(), a3g5Priv.getFieldId());
    assertNull(flatMship);
  }
  

  private void verifyEffectiveNamingPrivilegesAdd(Membership s3g4Priv, Membership s3g5Priv, String contextId) {

    assertTrue(!StringUtils.isBlank(contextId));

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :stemId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", s3g4Priv.getMemberSubjectId())
      .setString("stemId", s3g4Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(s3g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(s3g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("naming", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(s3g4Priv.getStem().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(s3g4Priv.getStem().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);

    FlatMembership flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(s3g4Priv.getOwnerStemId(), s3g4Priv.getMemberUuid(), s3g4Priv.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerStemId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :stemId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", s3g5Priv.getMemberSubjectId())
      .setString("stemId", s3g5Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(s3g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(s3g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("naming", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(s3g5Priv.getStem().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(s3g5Priv.getStem().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(s3g5Priv.getOwnerStemId(), s3g5Priv.getMemberUuid(), s3g5Priv.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerStemId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());
  }
  
  private void verifyEffectiveNamingPrivilegesDelete(Membership s3g4Priv, Membership s3g5Priv, String contextId) {
    
    assertTrue(!StringUtils.isBlank(contextId));

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :stemId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", s3g4Priv.getMemberSubjectId())
      .setString("stemId", s3g4Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(s3g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(s3g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("naming", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(s3g4Priv.getStem().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(s3g4Priv.getStem().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
  
    FlatMembership flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(s3g4Priv.getOwnerStemId(), s3g4Priv.getMemberUuid(), s3g4Priv.getFieldId());
    assertNull(flatMship);
  
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :stemId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", s3g5Priv.getMemberSubjectId())
      .setString("stemId", s3g5Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(s3g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(s3g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("naming", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(s3g5Priv.getStem().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(s3g5Priv.getStem().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(s3g5Priv.getOwnerStemId(), s3g5Priv.getMemberUuid(), s3g5Priv.getFieldId());
    assertNull(flatMship);
  }
  
  
  private void verifyEffectiveMembershipsAdd(Membership g3g4Mship, Membership g3g5Mship, 
      Membership g2g4Mship, Membership g2g5Mship, Membership g1g4Priv, Membership g1g5Priv, 
      Membership s0g4Priv, Membership s0g5Priv, Membership a0g4Priv, Membership a0g5Priv,
      String contextId) {

    assertTrue(!StringUtils.isBlank(contextId));
    
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .setString("subjectId", g3g4Mship.getMemberSubjectId())
      .setString("groupId", g3g4Mship.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(g3g4Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(g3g4Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(g3g4Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g3g4Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    FlatMembership flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g3g4Mship.getOwnerGroupId(), g3g4Mship.getMemberUuid(), g3g4Mship.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerGroupId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .setString("subjectId", g3g5Mship.getMemberSubjectId())
      .setString("groupId", g3g5Mship.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(g3g5Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(g3g5Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(g3g5Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g3g5Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g3g5Mship.getOwnerGroupId(), g3g5Mship.getMemberUuid(), g3g5Mship.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerGroupId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .setString("subjectId", g2g4Mship.getMemberSubjectId())
      .setString("groupId", g2g4Mship.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(g2g4Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(g2g4Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(g2g4Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g2g4Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g2g4Mship.getOwnerGroupId(), g2g4Mship.getMemberUuid(), g2g4Mship.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerGroupId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .setString("subjectId", g2g5Mship.getMemberSubjectId())
      .setString("groupId", g2g5Mship.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(g2g5Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(g2g5Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(g2g5Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g2g5Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g2g5Mship.getOwnerGroupId(), g2g5Mship.getMemberUuid(), g2g5Mship.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerGroupId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", g1g4Priv.getMemberSubjectId())
      .setString("groupId", g1g4Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("update", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(g1g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(g1g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("access", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(g1g4Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(g1g4Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g1g4Priv.getOwnerGroupId(), g1g4Priv.getMemberUuid(), g1g4Priv.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerGroupId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", g1g5Priv.getMemberSubjectId())
      .setString("groupId", g1g5Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("update", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(g1g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(g1g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("access", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(g1g5Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(g1g5Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g1g5Priv.getOwnerGroupId(), g1g5Priv.getMemberUuid(), g1g5Priv.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerGroupId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :stemId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", s0g4Priv.getMemberSubjectId())
      .setString("stemId", s0g4Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("create", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(s0g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(s0g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("naming", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(s0g4Priv.getStem().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(s0g4Priv.getStem().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);

    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(s0g4Priv.getOwnerStemId(), s0g4Priv.getMemberUuid(), s0g4Priv.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerStemId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :stemId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", s0g5Priv.getMemberSubjectId())
      .setString("stemId", s0g5Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("create", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(s0g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(s0g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("naming", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(s0g5Priv.getStem().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(s0g5Priv.getStem().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(s0g5Priv.getOwnerStemId(), s0g5Priv.getMemberUuid(), s0g5Priv.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerStemId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :attrDefId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", a0g4Priv.getMemberSubjectId())
      .setString("attrDefId", a0g4Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("attrUpdate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(a0g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(a0g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(a0g4Priv.getAttributeDef().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(a0g4Priv.getAttributeDef().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(a0g4Priv.getOwnerAttrDefId(), a0g4Priv.getMemberUuid(), a0g4Priv.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerAttrDefId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :attrDefId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", a0g5Priv.getMemberSubjectId())
      .setString("attrDefId", a0g5Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("attrUpdate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals(a0g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(a0g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(a0g5Priv.getAttributeDef().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(a0g5Priv.getAttributeDef().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(a0g5Priv.getOwnerAttrDefId(), a0g5Priv.getMemberUuid(), a0g5Priv.getFieldId());
    assertNotNull(flatMship);
    assertEquals("Verify owner id", flatMship.getOwnerId(), flatMship.getOwnerAttrDefId());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), flatMship.getContextId());
  }
  

  private void verifyEffectiveMembershipsDelete(Membership g3g4Mship, Membership g3g5Mship, 
      Membership g2g4Mship, Membership g2g5Mship, Membership g1g4Priv, Membership g1g5Priv,
      Membership s0g4Priv, Membership s0g5Priv, Membership a0g4Priv, Membership a0g5Priv,
      String contextId) {

    assertTrue(!StringUtils.isBlank(contextId));

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .setString("subjectId", g3g4Mship.getMemberSubjectId())
      .setString("groupId", g3g4Mship.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(g3g4Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(g3g4Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals(g3g4Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g3g4Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    FlatMembership flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g3g4Mship.getOwnerGroupId(), g3g4Mship.getMemberUuid(), g3g4Mship.getFieldId());
    assertNull(flatMship);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .setString("subjectId", g3g5Mship.getMemberSubjectId())
      .setString("groupId", g3g5Mship.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(g3g5Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(g3g5Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals(g3g5Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g3g5Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);

    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g3g5Mship.getOwnerGroupId(), g3g5Mship.getMemberUuid(), g3g5Mship.getFieldId());
    assertNull(flatMship);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .setString("subjectId", g2g4Mship.getMemberSubjectId())
      .setString("groupId", g2g4Mship.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(g2g4Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(g2g4Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals(g2g4Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g2g4Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g2g4Mship.getOwnerGroupId(), g2g4Mship.getMemberUuid(), g2g4Mship.getFieldId());
    assertNull(flatMship);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .setString("subjectId", g2g5Mship.getMemberSubjectId())
      .setString("groupId", g2g5Mship.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(g2g5Mship.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(g2g5Mship.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals(g2g5Mship.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g2g5Mship.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);

    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g2g5Mship.getOwnerGroupId(), g2g5Mship.getMemberUuid(), g2g5Mship.getFieldId());
    assertNull(flatMship);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", g1g4Priv.getMemberSubjectId())
      .setString("groupId", g1g4Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("update", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(g1g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(g1g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("access", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(g1g4Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(g1g4Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);

    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g1g4Priv.getOwnerGroupId(), g1g4Priv.getMemberUuid(), g1g4Priv.getFieldId());
    assertNull(flatMship);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", g1g5Priv.getMemberSubjectId())
      .setString("groupId", g1g5Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("update", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(g1g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(g1g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("access", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(g1g5Priv.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(g1g5Priv.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(g1g5Priv.getOwnerGroupId(), g1g5Priv.getMemberUuid(), g1g5Priv.getFieldId());
    assertNull(flatMship);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :stemId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", s0g4Priv.getMemberSubjectId())
      .setString("stemId", s0g4Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("create", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(s0g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(s0g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("naming", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(s0g4Priv.getStem().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(s0g4Priv.getStem().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(s0g4Priv.getOwnerStemId(), s0g4Priv.getMemberUuid(), s0g4Priv.getFieldId());
    assertNull(flatMship);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :stemId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", s0g5Priv.getMemberSubjectId())
      .setString("stemId", s0g5Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("create", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(s0g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(s0g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("naming", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(s0g5Priv.getStem().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(s0g5Priv.getStem().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(s0g5Priv.getOwnerStemId(), s0g5Priv.getMemberUuid(), s0g5Priv.getFieldId());
    assertNull(flatMship);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :attrDefId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", a0g4Priv.getMemberSubjectId())
      .setString("attrDefId", a0g4Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("attrUpdate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(a0g4Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(a0g4Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(a0g4Priv.getAttributeDef().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(a0g4Priv.getAttributeDef().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(a0g4Priv.getOwnerAttrDefId(), a0g4Priv.getMemberUuid(), a0g4Priv.getFieldId());
    assertNull(flatMship);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :attrDefId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", a0g5Priv.getMemberSubjectId())
      .setString("attrDefId", a0g5Priv.getOwnerId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("attrUpdate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals(a0g5Priv.getMemberSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(a0g5Priv.getMemberSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(a0g5Priv.getAttributeDef().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(a0g5Priv.getAttributeDef().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    assertEquals("flattened", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.membershipType));
    assertEquals(changeLogEntry.getContextId(), contextId);
    
    flatMship = GrouperDAOFactory.getFactory().getFlatMembership()
      .findByOwnerAndMemberAndField(a0g5Priv.getOwnerAttrDefId(), a0g5Priv.getMemberUuid(), a0g5Priv.getFieldId());
    assertNull(flatMship);
  }
  
  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession = SessionHelper.getRootSession();
  
  /** root stem */
  private Stem root;

}
