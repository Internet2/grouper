/*
 * @author mchyzer
 * $Id: ChangeLogTest.java,v 1.10 2009-10-31 17:46:47 shilen Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.entity.Entity;
import edu.internet2.middleware.grouper.entity.EntitySave;
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
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITGroupSet;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.pit.PITStem;
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
    TestRunner.run(new ChangeLogTest("testMemberships"));
    //TestRunner.run(ChangeLogTest.class);
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    grouperSession = SessionHelper.getRootSession();
    ApiConfig.testConfig.put("grouper.env.name", "testEnv");
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    
    GrouperLoaderConfig.testConfig.put("changeLog.includeRolesWithPermissionChanges", "true");
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
   * 
   */
  public void testStemRenameOrder() {

    // initialize some data
    Stem one = root.addChildStem("one", "ONE");
    Stem two = one.addChildStem("two", "TWO");
    Stem three = two.addChildStem("three", "THREE");
    three.addChildStem("four", "FOUR");
    
    two.addChildGroup("two-group", "TWO-GROUP");
    three.addChildGroup("three-group", "THREE-GROUP");
    
    AttributeDef attributeDef = three.addChildAttributeDef("three-attrDef", AttributeDefType.attr);    
    three.addChildAttributeDefName(attributeDef, "three-attr", "THREE-ATTR");
    
    //move the temp objects to the regular change log table and delete them
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // now rename
    one.setExtension("oneNew");
    one.store();
    ChangeLogTempToEntity.convertRecords();

    // order should be ..
    //
    // one
    // one:two
    // one:two:two-group
    // one:two:three
    // one:two:three:three-attr
    // one:two:three:three-attrDef
    // one:two:three:three-group
    // one:two:three:four
    
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have added 8 change log entries", 8, newChangeLogCount);

    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity order by sequenceNumber")
      .list(ChangeLogEntry.class);

    {
      ChangeLogEntry entry = changeLogEntries.get(0);
      assertTrue("contextId should exist", StringUtils.isNotBlank(entry.getContextId()));
      assertEquals("verify contextId", one.getContextId(), entry.getContextId());
      assertEquals(ChangeLogTypeBuiltin.STEM_UPDATE.getChangeLogType(), entry.getChangeLogType());
      assertEquals(ChangeLogLabels.STEM_UPDATE.name.name(), entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyChanged));
      assertEquals("oneNew", entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.name));
      assertEquals("one", entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyOldValue));
      assertEquals("oneNew", entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyNewValue));
    }
    
    {
      ChangeLogEntry entry = changeLogEntries.get(1);
      assertTrue("contextId should exist", StringUtils.isNotBlank(entry.getContextId()));
      assertEquals("verify contextId", one.getContextId(), entry.getContextId());
      assertEquals(ChangeLogTypeBuiltin.STEM_UPDATE.getChangeLogType(), entry.getChangeLogType());
      assertEquals(ChangeLogLabels.STEM_UPDATE.name.name(), entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyChanged));
      assertEquals("oneNew:two", entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.name));
      assertEquals("one:two", entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyOldValue));
      assertEquals("oneNew:two", entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyNewValue));
    }
    
    {
      ChangeLogEntry entry = changeLogEntries.get(2);
      assertTrue("contextId should exist", StringUtils.isNotBlank(entry.getContextId()));
      assertEquals("verify contextId", one.getContextId(), entry.getContextId());
      assertEquals(ChangeLogTypeBuiltin.GROUP_UPDATE.getChangeLogType(), entry.getChangeLogType());
      assertEquals(ChangeLogLabels.GROUP_UPDATE.name.name(), entry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged));
      assertEquals("oneNew:two:two-group", entry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.name));
      assertEquals("one:two:two-group", entry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyOldValue));
      assertEquals("oneNew:two:two-group", entry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyNewValue));
    }
    
    {
      ChangeLogEntry entry = changeLogEntries.get(3);
      assertTrue("contextId should exist", StringUtils.isNotBlank(entry.getContextId()));
      assertEquals("verify contextId", one.getContextId(), entry.getContextId());
      assertEquals(ChangeLogTypeBuiltin.STEM_UPDATE.getChangeLogType(), entry.getChangeLogType());
      assertEquals(ChangeLogLabels.STEM_UPDATE.name.name(), entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyChanged));
      assertEquals("oneNew:two:three", entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.name));
      assertEquals("one:two:three", entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyOldValue));
      assertEquals("oneNew:two:three", entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyNewValue));
    }
    
    {
      ChangeLogEntry entry = changeLogEntries.get(4);
      assertTrue("contextId should exist", StringUtils.isNotBlank(entry.getContextId()));
      assertEquals("verify contextId", one.getContextId(), entry.getContextId());
      assertEquals(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_UPDATE.getChangeLogType(), entry.getChangeLogType());
      assertEquals(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.name.name(), entry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.propertyChanged));
      assertEquals("oneNew:two:three:three-attr", entry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.name));
      assertEquals("one:two:three:three-attr", entry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.propertyOldValue));
      assertEquals("oneNew:two:three:three-attr", entry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.propertyNewValue));
    }
    
    {
      ChangeLogEntry entry = changeLogEntries.get(5);
      assertTrue("contextId should exist", StringUtils.isNotBlank(entry.getContextId()));
      assertEquals("verify contextId", one.getContextId(), entry.getContextId());
      assertEquals(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_UPDATE.getChangeLogType(), entry.getChangeLogType());
      assertEquals(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.name.name(), entry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.propertyChanged));
      assertEquals("oneNew:two:three:three-attrDef", entry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.name));
      assertEquals("one:two:three:three-attrDef", entry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.propertyOldValue));
      assertEquals("oneNew:two:three:three-attrDef", entry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.propertyNewValue));
    }
    
    {
      ChangeLogEntry entry = changeLogEntries.get(6);
      assertTrue("contextId should exist", StringUtils.isNotBlank(entry.getContextId()));
      assertEquals("verify contextId", one.getContextId(), entry.getContextId());
      assertEquals(ChangeLogTypeBuiltin.GROUP_UPDATE.getChangeLogType(), entry.getChangeLogType());
      assertEquals(ChangeLogLabels.GROUP_UPDATE.name.name(), entry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged));
      assertEquals("oneNew:two:three:three-group", entry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.name));
      assertEquals("one:two:three:three-group", entry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyOldValue));
      assertEquals("oneNew:two:three:three-group", entry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyNewValue));
    }
    
    {
      ChangeLogEntry entry = changeLogEntries.get(7);
      assertTrue("contextId should exist", StringUtils.isNotBlank(entry.getContextId()));
      assertEquals("verify contextId", one.getContextId(), entry.getContextId());
      assertEquals(ChangeLogTypeBuiltin.STEM_UPDATE.getChangeLogType(), entry.getChangeLogType());
      assertEquals(ChangeLogLabels.STEM_UPDATE.name.name(), entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyChanged));
      assertEquals("oneNew:two:three:four", entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.name));
      assertEquals("one:two:three:four", entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyOldValue));
      assertEquals("oneNew:two:three:four", entry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyNewValue));
    }
  }
  
  /**
   * @throws Exception
   */
  public void testAttributeAssignValue() throws Exception {
    
    //##################################
    // try a string value
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName = edu.addChildAttributeDefName(attributeDef, "testAttribute", "testAttribute");
    AttributeAssign attributeAssign = edu.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // add value
    AttributeAssignValue attributeAssignValue = attributeAssign.getValueDelegate().assignValueString("test").getAttributeAssignValue();
    
    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have added exactly one change log temp", 1, newChangeLogTempCount);
    assertEquals("Should be the same", 0, newChangeLogCount);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD.getChangeLogType().getId())
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
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), attributeAssignValue.getContextId());
  
    assertEquals(attributeAssignValue.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.id));
    assertEquals(attributeAssign.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeAssignId));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameName));
    assertEquals("test", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.value));
    assertEquals(AttributeDefValueType.string.name(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.valueType));

    //move the temp objects to the regular change log table
    ChangeLogTempToEntity.convertRecords();

    // Check the change log table, and temp table, see the record moved over. 
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);
  
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), attributeAssignValue.getContextId());
    
    // now delete the attribute value
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    attributeAssign.getValueDelegate().deleteValue("test");
    ChangeLogTempToEntity.convertRecords();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals(attributeAssignValue.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.id));
    assertEquals(attributeAssign.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeAssignId));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameName));
    assertEquals("test", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.value));
    assertEquals(AttributeDefValueType.string.name(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.valueType));

    
    //##################################
    // try an integer now (this one is multi-valued)
    
    attributeDef = edu.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.setMultiValued(true);
    attributeDef.setValueType(AttributeDefValueType.integer);
    attributeDef.store();
    attributeDefName = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    attributeAssign = edu.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    attributeAssign.getValueDelegate().assignValueInteger(5L).getAttributeAssignValue();
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    attributeAssignValue = attributeAssign.getValueDelegate().addValueInteger(10L).getAttributeAssignValue();
    ChangeLogTempToEntity.convertRecords();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
        
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals(attributeAssignValue.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.id));
    assertEquals(attributeAssign.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeAssignId));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameName));
    assertEquals("10", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.value));
    assertEquals(AttributeDefValueType.integer.name(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.valueType));

    // now delete the attribute value (just one -- this is multi-valued)
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    attributeAssign.getValueDelegate().deleteValue("10");
    ChangeLogTempToEntity.convertRecords();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals(attributeAssignValue.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.id));
    assertEquals(attributeAssign.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeAssignId));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameName));
    assertEquals("10", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.value));
    assertEquals(AttributeDefValueType.integer.name(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.valueType));

    
    //##################################
    // try a floating point number now
    
    attributeDef = edu.addChildAttributeDef("attributeDef3", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.setValueType(AttributeDefValueType.floating);
    attributeDef.store();
    attributeDefName = edu.addChildAttributeDefName(attributeDef, "testAttribute3", "testAttribute3");
    attributeAssign = edu.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    attributeAssignValue = attributeAssign.getValueDelegate().assignValueFloating(5.2).getAttributeAssignValue();
    ChangeLogTempToEntity.convertRecords();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals(attributeAssignValue.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.id));
    assertEquals(attributeAssign.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeAssignId));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameName));
    assertEquals("5.2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.value));
    assertEquals(AttributeDefValueType.floating.name(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.valueType));

    // now delete the attribute value
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    attributeAssign.getValueDelegate().deleteValue("5.2");
    ChangeLogTempToEntity.convertRecords();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals(attributeAssignValue.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.id));
    assertEquals(attributeAssign.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeAssignId));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameName));
    assertEquals("5.2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.value));
    assertEquals(AttributeDefValueType.floating.name(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.valueType));

    
    //##################################
    // try a timestamp now
    
    attributeDef = edu.addChildAttributeDef("attributeDef4", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.setValueType(AttributeDefValueType.timestamp);
    attributeDef.store();
    attributeDefName = edu.addChildAttributeDefName(attributeDef, "testAttribute4", "testAttribute4");
    attributeAssign = edu.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();
    Long time = System.currentTimeMillis();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    attributeAssignValue = attributeAssign.getValueDelegate().assignValueTimestamp(new Timestamp(time)).getAttributeAssignValue();
    ChangeLogTempToEntity.convertRecords();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals(attributeAssignValue.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.id));
    assertEquals(attributeAssign.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeAssignId));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameName));
    assertEquals(time.toString(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.value));
    assertEquals(AttributeDefValueType.timestamp.name(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.valueType));

    // now delete the attribute value
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    attributeAssign.getValueDelegate().deleteValueTimestamp(new Timestamp(time));
    ChangeLogTempToEntity.convertRecords();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals(attributeAssignValue.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.id));
    assertEquals(attributeAssign.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeAssignId));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameName));
    assertEquals(time.toString(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.value));
    assertEquals(AttributeDefValueType.timestamp.name(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.valueType));

    
    //##################################
    // try a memberId now
    
    attributeDef = edu.addChildAttributeDef("attributeDef5", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.setValueType(AttributeDefValueType.memberId);
    attributeDef.store();
    attributeDefName = edu.addChildAttributeDefName(attributeDef, "testAttribute5", "testAttribute5");
    attributeAssign = edu.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    Member member = MemberFinder.findBySubject(grouperSession, SubjectFinder.findRootSubject(), false);
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    attributeAssignValue = attributeAssign.getValueDelegate().assignValueMember(member).getAttributeAssignValue();
    ChangeLogTempToEntity.convertRecords();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals(attributeAssignValue.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.id));
    assertEquals(attributeAssign.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeAssignId));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameName));
    assertEquals(member.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.value));
    assertEquals(AttributeDefValueType.memberId.name(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.valueType));

    // now delete the attribute value
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    attributeAssign.getValueDelegate().deleteValueMember(member);
    ChangeLogTempToEntity.convertRecords();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals(attributeAssignValue.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.id));
    assertEquals(attributeAssign.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeAssignId));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameName));
    assertEquals(member.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.value));
    assertEquals(AttributeDefValueType.memberId.name(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.valueType));

    
    //##################################
    // try updating an attribute value

    attributeDef = edu.addChildAttributeDef("attributeDef6", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.setValueType(AttributeDefValueType.integer);
    attributeDef.store();
    attributeDefName = edu.addChildAttributeDefName(attributeDef, "testAttribute6", "testAttribute6");
    attributeAssign = edu.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    
    attributeAssignValue = attributeAssign.getValueDelegate().assignValueInteger(10L).getAttributeAssignValue();
    String oldId = attributeAssignValue.getId();
    ChangeLogTempToEntity.convertRecords();
    
    // now do the update
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    attributeAssignValue.setValueInteger(11L);
    attributeAssignValue.saveOrUpdate();
    ChangeLogTempToEntity.convertRecords();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");

    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have two records in the change log table", 2, newChangeLogCount);
    
    // the id should have been updated
    assertFalse(oldId.equals(attributeAssignValue.getId()));
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals(attributeAssignValue.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.id));
    assertEquals(attributeAssign.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeAssignId));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameName));
    assertEquals("11", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.value));
    assertEquals(AttributeDefValueType.integer.name(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.valueType));

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals(oldId, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.id));
    assertEquals(attributeAssign.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeAssignId));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameName));
    assertEquals("10", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.value));
    assertEquals(AttributeDefValueType.integer.name(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.valueType));
  }
  
  
  /**
   * @throws Exception
   */
  public void testAttributeAssign() throws Exception {
    
    // initialize some data
    Group group = edu.addChildGroup("testGroup", "testGroup");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group.addMember(newMember1.getSubject());
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.setAssignToImmMembership(true);
    attributeDef.setAssignToImmMembershipAssn(true);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    AttributeDefName attributeDefName = edu.addChildAttributeDefName(attributeDef, "testAttribute", "testAttribute");
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // assign attribute to stem
    AttributeAssign attributeAssign = edu.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();

    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have added exactly one change log temp", 1, newChangeLogTempCount);
    assertEquals("Should be the same", 0, newChangeLogCount);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD.getChangeLogType().getId())
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
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), attributeAssign.getContextId());
  
    assertEquals(attributeAssign.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.id));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeAssignActionId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeAssignActionId));
    assertEquals(attributeAssign.getAttributeAssignTypeDb(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.assignType));
    assertEquals(edu.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId1));
    assertTrue(StringUtils.isBlank(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId2)));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameName));
    assertEquals(attributeAssign.getAttributeAssignAction().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.action));
    assertEquals(attributeAssign.getDisallowedDb(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.disallowed));

    //move the temp objects to the regular change log table
    ChangeLogTempToEntity.convertRecords();

    //#########################
    // Check the change log table, and temp table, see the record moved over.
 
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);
  
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), attributeAssign.getContextId());
    
    //##################################
    // try disabling

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    attributeAssign.setEnabled(false);
    attributeAssign.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    attributeAssign.saveOrUpdate();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have zero records in the change log table", 0, newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry2 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
  
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    assertEquals(attributeAssign.getId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.id));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeAssignActionId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeAssignActionId));
    assertEquals(attributeAssign.getAttributeAssignTypeDb(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.assignType));
    assertEquals(edu.getUuid(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.ownerId1));
    assertEquals(null, changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.ownerId2));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameName));
    assertEquals(attributeAssign.getAttributeAssignAction().getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.action));

    //##################################
    // try enabling

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    attributeAssign.setEnabled(true);
    attributeAssign.setEnabledTime(new Timestamp(new Date().getTime() - 100000));
    attributeAssign.saveOrUpdate();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have zero records in the change log table", 0, newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry3 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry3.getContextId()));
  
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry2.getContextId(), 
        changeLogEntry3.getContextId()));
    
    assertEquals(attributeAssign.getId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.id));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeAssignActionId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeAssignActionId));
    assertEquals(attributeAssign.getAttributeAssignTypeDb(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.assignType));
    assertEquals(edu.getUuid(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId1));
    assertEquals(null, changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId2));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameName));
    assertEquals(attributeAssign.getAttributeAssignAction().getName(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.action));
    
    //##################################
    // try deleting now
    
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    attributeAssign.delete();
    
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have zero records in the change log table", 0, newChangeLogCount);

    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry4 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    

    assertEquals(attributeAssign.getId(), changeLogEntry4.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.id));
    assertEquals(attributeAssign.getAttributeDefNameId(), changeLogEntry4.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameId));
    assertEquals(attributeAssign.getAttributeAssignActionId(), changeLogEntry4.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeAssignActionId));
    assertEquals(attributeAssign.getAttributeAssignTypeDb(), changeLogEntry4.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.assignType));
    assertEquals(edu.getUuid(), changeLogEntry4.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.ownerId1));
    assertEquals(null, changeLogEntry4.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.ownerId2));
    assertEquals(attributeAssign.getAttributeDefName().getName(), changeLogEntry4.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameName));
    assertEquals(attributeAssign.getAttributeAssignAction().getName(), changeLogEntry4.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.action));
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry4.getContextId()));
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry3.getContextId(), 
        changeLogEntry4.getContextId()));
    
    //##################################
    // try adding an attribute to an immediate membership now...

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // assign attribute immediate membership
    Membership immediateMembership = MembershipFinder.findImmediateMembership(grouperSession, group, newMember1.getSubject(), Group.getDefaultList(), true);
    AttributeAssign attributeAssign2 = immediateMembership.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have zero records in the change log table", 0, newChangeLogCount);

    ChangeLogTempToEntity.convertRecords();
  
    ChangeLogEntry changeLogEntry5 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals(attributeAssign2.getId(), changeLogEntry5.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.id));
    assertEquals(attributeAssign2.getAttributeDefNameId(), changeLogEntry5.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameId));
    assertEquals(attributeAssign2.getAttributeAssignActionId(), changeLogEntry5.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeAssignActionId));
    assertEquals(attributeAssign2.getAttributeAssignTypeDb(), changeLogEntry5.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.assignType));
    assertEquals(immediateMembership.getImmediateMembershipId(), changeLogEntry5.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId1));
    assertEquals(null, changeLogEntry5.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId2));
    assertEquals(attributeAssign2.getAttributeDefName().getName(), changeLogEntry5.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameName));
    assertEquals(attributeAssign2.getAttributeAssignAction().getName(), changeLogEntry5.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.action));

    //##################################
    // try adding an attribute to an immediate membership assignment now

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // assign attribute immediate membership assign
    AttributeAssign attributeAssign3 = attributeAssign2.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have zero records in the change log table", 0, newChangeLogCount);

    ChangeLogTempToEntity.convertRecords();
  
    ChangeLogEntry changeLogEntry6 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals(attributeAssign3.getId(), changeLogEntry6.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.id));
    assertEquals(attributeAssign3.getAttributeDefNameId(), changeLogEntry6.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameId));
    assertEquals(attributeAssign3.getAttributeAssignActionId(), changeLogEntry6.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeAssignActionId));
    assertEquals(attributeAssign3.getAttributeAssignTypeDb(), changeLogEntry6.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.assignType));
    assertEquals(attributeAssign2.getId(), changeLogEntry6.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId1));
    assertEquals(null, changeLogEntry6.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId2));
    assertEquals(attributeAssign3.getAttributeDefName().getName(), changeLogEntry6.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameName));
    assertEquals(attributeAssign3.getAttributeAssignAction().getName(), changeLogEntry6.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.action));
    
    //##################################
    // try adding an attribute to an effective membership now

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // assign attribute immediate membership assign
    AttributeAssign attributeAssign4 = immediateMembership.getAttributeDelegateEffMship().assignAttribute(attributeDefName).getAttributeAssign();

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have zero records in the change log table", 0, newChangeLogCount);

    ChangeLogTempToEntity.convertRecords();
  
    ChangeLogEntry changeLogEntry7 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals(attributeAssign4.getId(), changeLogEntry7.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.id));
    assertEquals(attributeAssign4.getAttributeDefNameId(), changeLogEntry7.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameId));
    assertEquals(attributeAssign4.getAttributeAssignActionId(), changeLogEntry7.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeAssignActionId));
    assertEquals(attributeAssign4.getAttributeAssignTypeDb(), changeLogEntry7.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.assignType));
    assertEquals(group.getId(), changeLogEntry7.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId1));
    assertEquals(newMember1.getUuid(), changeLogEntry7.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId2));
    assertEquals(attributeAssign4.getAttributeDefName().getName(), changeLogEntry7.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameName));
    assertEquals(attributeAssign4.getAttributeAssignAction().getName(), changeLogEntry7.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.action));
  }
  
  /**
   * @throws Exception
   */
  public void testAttributeDefName() throws Exception {
    
    // initialize some data
    AttributeDef permissionDef = edu.addChildAttributeDef("permissionDef", AttributeDefType.perm);
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // add attribute
    AttributeDefName attributeDefName = edu.addChildAttributeDefName(permissionDef, "testAttribute", "testAttribute");
    
    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have added exactly two change log temp", 2, newChangeLogTempCount);
    assertEquals("Should be the same", 0, newChangeLogCount);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_ADD.getChangeLogType().getId())
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

    assertEquals("Context id's should match", changeLogEntry.getContextId(), attributeDefName.getContextId());
  
    assertEquals(attributeDefName.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.id));
    assertEquals(attributeDefName.getStemId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.stemId));
    assertTrue(StringUtils.isBlank(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.description)));
    assertEquals(attributeDefName.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.name));
    assertEquals(attributeDefName.getAttributeDefId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.attributeDefId));

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
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), attributeDefName.getContextId());
    
    //##################################
    // try an update
    

    attributeDefName.setDescription("test description");
    attributeDefName.store();
    
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have two records in the change log table", 2, newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry2 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_UPDATE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));

    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    assertEquals(attributeDefName.getId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.id));
    assertEquals(attributeDefName.getStemId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.stemId));
    assertEquals(attributeDefName.getDescription(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.description));
    assertEquals(attributeDefName.getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.name));
    assertEquals(attributeDefName.getAttributeDefId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.attributeDefId));
    assertEquals("description", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.propertyChanged));
    assertEquals(null, changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.propertyOldValue));
    assertEquals("test description", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.propertyNewValue));
    
    
    //##################################
    // try a delete
    
    attributeDefName.delete();
    
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have two in temp table", 2, newChangeLogTempCount);
    assertEquals("Should have three records in the change log table", 3, newChangeLogCount);

    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry3 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertEquals(attributeDefName.getId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.id));
    assertEquals(attributeDefName.getStemId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.stemId));
    assertEquals(attributeDefName.getDescription(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.description));
    assertEquals(attributeDefName.getName(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.name));
    assertEquals(attributeDefName.getAttributeDefId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.attributeDefId));

    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry3.getContextId()));
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry2.getContextId(), 
        changeLogEntry3.getContextId()));
  }

  /**
   * @throws Exception
   */
  public void testAttributeDefNameSet() throws Exception {
    
    // initialize some data
    AttributeDef permissionDef = edu.addChildAttributeDef("permissionDef", AttributeDefType.perm);
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(permissionDef, "testAttribute1", "testAttribute1");
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // add another attribute def name
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(permissionDef, "testAttribute2", "testAttribute2");

    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have added exactly two change log temp", 2, newChangeLogTempCount);
    assertEquals("Should be the same", 0, newChangeLogCount);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_SET_ADD.getChangeLogType().getId())
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
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), attributeDefName2.getContextId());
  
    assertTrue(StringUtils.isNotBlank(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.id)));
    assertEquals(attributeDefName2.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.ifHasAttributeDefNameId));
    assertEquals(attributeDefName2.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.thenHasAttributeDefNameId));
    assertEquals("self", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.type));

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
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_SET_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), attributeDefName2.getContextId());
    
    //##################################
    // try adding attribute def name set

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    final AttributeDefName ATTRIBUTEDEFNAME1a = attributeDefName1;
    final AttributeDefName ATTRIBUTEDEFNAME2a = attributeDefName2;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        ATTRIBUTEDEFNAME1a.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(ATTRIBUTEDEFNAME2a);
        return null;
      }
    });

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have zero records in the change log table", 0, newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry2 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_SET_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
  
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    assertTrue(StringUtils.isNotBlank(changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.id)));
    assertEquals(attributeDefName1.getId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.ifHasAttributeDefNameId));
    assertEquals(attributeDefName2.getId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.thenHasAttributeDefNameId));
    assertEquals("immediate", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.type));


    //##################################
    // try deleting attribute def name set

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    final AttributeDefName ATTRIBUTEDEFNAME1b = attributeDefName1;
    final AttributeDefName ATTRIBUTEDEFNAME2b = attributeDefName2;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        ATTRIBUTEDEFNAME1b.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(ATTRIBUTEDEFNAME2b);
        return null;
      }
    });

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have zero records in the change log table", 0, newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry3 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_SET_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry3.getContextId()));
  
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry2.getContextId(), 
        changeLogEntry3.getContextId()));
    
    assertTrue(StringUtils.isNotBlank(changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.id)));
    assertEquals(attributeDefName1.getId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.ifHasAttributeDefNameId));
    assertEquals(attributeDefName2.getId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.thenHasAttributeDefNameId));
    assertEquals("immediate", changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.type));

    
    //##################################
    // try deleting attribute def name now
    
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    attributeDefName2.delete();
    
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have two in temp table", 2, newChangeLogTempCount);
    assertEquals("Should have zero records in the change log table", 0, newChangeLogCount);

    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry4 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_SET_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    

    assertTrue(StringUtils.isNotBlank(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.id)));
    assertEquals(attributeDefName2.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.ifHasAttributeDefNameId));
    assertEquals(attributeDefName2.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.thenHasAttributeDefNameId));
    assertEquals("self", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.type));

    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry4.getContextId()));
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry3.getContextId(), 
        changeLogEntry4.getContextId()));

  }
  
  /**
   * @throws Exception
   */
  public void testRoleSet() throws Exception {
    
    // initialize some data
    final Role role1 = edu.addChildRole("testRole1", "testRole1");
    final Role role2 = edu.addChildRole("testRole2", "testRole2");
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    
    //##################################
    // try adding role set
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        role1.getRoleInheritanceDelegate().addRoleToInheritFromThis(role2);
        return null;
      }
    });

    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have zero records in the change log table", 0, newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ROLE_SET_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry.getContextId()));
    
    assertTrue(StringUtils.isNotBlank(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_ADD.id)));
    assertEquals(role1.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_ADD.ifHasRoleId));
    assertEquals(role2.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_ADD.thenHasRoleId));
    assertEquals("immediate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_ADD.type));


    //##################################
    // try deleting role set

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        role1.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(role2);
        return null;
      }
    });

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have zero records in the change log table", 0, newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry2 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ROLE_SET_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
  
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    assertTrue(StringUtils.isNotBlank(changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_DELETE.id)));
    assertEquals(role1.getId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_DELETE.ifHasRoleId));
    assertEquals(role2.getId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_DELETE.thenHasRoleId));
    assertEquals("immediate", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_DELETE.type));

  }
  
  /**
   * @throws Exception
   */
  public void testAttributeAssignActionSet() throws Exception {
    
    // initialize some data
    final AttributeDef permissionDef = edu.addChildAttributeDef("permissionDef", AttributeDefType.perm);
    AttributeAssignAction action2 = permissionDef.getAttributeDefActionDelegate().addAction("testAction2");
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // add action
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        permissionDef.getAttributeDefActionDelegate().addAction("testAction");
        return null;
      }
    });
    
    AttributeAssignAction action = permissionDef.getAttributeDefActionDelegate().findAction("testAction", true);

    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have added exactly two change log temp", 2, newChangeLogTempCount);
    assertEquals("Should be the same", 0, newChangeLogCount);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.getChangeLogType().getId())
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

    assertEquals("Context id's should match", changeLogEntry.getContextId(), action.getContextId());
  
    assertTrue(StringUtils.isNotBlank(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.id)));
    assertEquals(action.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.ifHasAttrAssnActionId));
    assertEquals(action.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.thenHasAttrAssnActionId));
    assertEquals("self", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.type));

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
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), action.getContextId());
    
    //##################################
    // try adding action set

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    final AttributeAssignAction ACTION1a = action;
    final AttributeAssignAction ACTION2a = action2;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        ACTION1a.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(ACTION2a);
        return null;
      }
    });

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have zero records in the change log table", 0, newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry2 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
  
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    assertTrue(StringUtils.isNotBlank(changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.id)));
    assertEquals(action.getId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.ifHasAttrAssnActionId));
    assertEquals(action2.getId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.thenHasAttrAssnActionId));
    assertEquals("immediate", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.type));


    //##################################
    // try deleting action set

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    final AttributeAssignAction ACTION1b = action;
    final AttributeAssignAction ACTION2b = action2;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        ACTION1b.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(ACTION2b);
        return null;
      }
    });

    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have zero records in the change log table", 0, newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry3 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry3.getContextId()));
  
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry2.getContextId(), 
        changeLogEntry3.getContextId()));
    
    assertTrue(StringUtils.isNotBlank(changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.id)));
    assertEquals(action.getId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.ifHasAttrAssnActionId));
    assertEquals(action2.getId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.thenHasAttrAssnActionId));
    assertEquals("immediate", changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.type));

    
    //##################################
    // try deleting action now
    
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    final AttributeAssignAction ACTION1c = action;
  
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        ACTION1c.delete();
        return null;
      }
    });
    
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have two in temp table", 2, newChangeLogTempCount);
    assertEquals("Should have zero records in the change log table", 0, newChangeLogCount);

    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry4 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    

    assertTrue(StringUtils.isNotBlank(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.id)));
    assertEquals(action.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.ifHasAttrAssnActionId));
    assertEquals(action.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.thenHasAttrAssnActionId));
    assertEquals("self", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.type));

    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry4.getContextId()));
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry3.getContextId(), 
        changeLogEntry4.getContextId()));

  }
  
  /**
   * @throws Exception
   */
  public void testAttributeAssignAction() throws Exception {
    
    // initialize some data
    final AttributeDef permissionDef = edu.addChildAttributeDef("permissionDef", AttributeDefType.perm);
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // add action
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        permissionDef.getAttributeDefActionDelegate().addAction("testAction");
        return null;
      }
    });
    
    AttributeAssignAction action = permissionDef.getAttributeDefActionDelegate().findAction("testAction", true);

    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have added exactly two change log temp", 2, newChangeLogTempCount);
    assertEquals("Should be the same", 0, newChangeLogCount);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_ADD.getChangeLogType().getId())
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

    assertEquals("Context id's should match", changeLogEntry.getContextId(), action.getContextId());
  
    assertEquals(action.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.id));
    assertEquals(action.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.name));
    assertEquals(action.getAttributeDefId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.attributeDefId));

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
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), action.getContextId());
    
    //##################################
    // try an update
    

    action.setNameDb("testAction2");

    final AttributeAssignAction ACTION = action;
  
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        ACTION.update();
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
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_UPDATE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));

    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    assertEquals(action.getId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.id));
    assertEquals(action.getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.name));
    assertEquals(action.getAttributeDefId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.attributeDefId));
    assertEquals("name", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.propertyChanged));
    assertEquals("testAction", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.propertyOldValue));
    assertEquals("testAction2", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.propertyNewValue));
    
    
    //##################################
    // try a delete
    
    final AttributeAssignAction ACTION2 = action;
  
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        ACTION2.delete();
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
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertEquals(action.getId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_DELETE.id));
    assertEquals(action.getName(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_DELETE.name));
    assertEquals(action.getAttributeDefId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_DELETE.attributeDefId));

    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry3.getContextId()));
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry2.getContextId(), 
        changeLogEntry3.getContextId()));
  }
  
  /**
   * @throws Exception
   */
  public void testRolePermissionEnableDisable() throws Exception {

    // initialize some data
    Role group = edu.addChildRole("testGroup", "testGroup");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group.addMember(newMember1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName = edu.addChildAttributeDefName(attributeDef, "testAttribute", "testAttribute");
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // assign permission
    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED).getAttributeAssign();
    
    //move the temp objects to the regular change log table
    ChangeLogTempToEntity.convertRecords();
    
    
    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have two records in the change log table", 2, newChangeLogCount);
  
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    //make sure some time has passed
    GrouperUtil.sleep(100);
  
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime()  < 5000);
    assertTrue("This should have happened in the last 5 seconds: + " + changeLogEntry.getCreatedOn(), System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime() > 0);
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) < 5000);
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) > 0);
  
    assertNotNull(changeLogEntry.getSequenceNumber());
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    assertEquals("Context id's should match", changeLogEntry.getContextId(), attributeAssign.getContextId());
  
    assertEquals(group.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));

    
    //##################################
    // try disabling

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    attributeAssign.setEnabled(false);
    attributeAssign.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    attributeAssign.saveOrUpdate();
  
    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have two records in the change log table", 2, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry.getContextId()));
    
    assertEquals(group.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));

    //##################################
    // try enabling

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    attributeAssign.setEnabled(true);
    attributeAssign.setEnabledTime(new Timestamp(new Date().getTime() - 100000));
    attributeAssign.saveOrUpdate();

    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have two records in the change log table", 2, newChangeLogCount);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry.getContextId()));
    
    assertEquals(group.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));

    //##################################
    // try deleting now
    
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    attributeAssign.delete();

    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have two records in the change log table", 2, newChangeLogCount);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry.getContextId()));
    
    assertEquals(group.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
  }
  

  /**
   * @throws Exception
   */
  public void testSubjectRolePermissionEnableDisable() throws Exception {

    // initialize some data
    Role group = edu.addChildRole("testGroup", "testGroup");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group.addMember(newMember1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    AttributeDefName attributeDefName = edu.addChildAttributeDefName(attributeDef, "testAttribute", "testAttribute");
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // assign permission
    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignSubjectRolePermission(attributeDefName, newMember1, PermissionAllowed.ALLOWED).getAttributeAssign();
    
    //move the temp objects to the regular change log table
    ChangeLogTempToEntity.convertRecords();
    
    
    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have two records in the change log table", 2, newChangeLogCount);
  
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    //make sure some time has passed
    GrouperUtil.sleep(100);
  
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime()  < 5000);
    assertTrue("This should have happened in the last 5 seconds: + " + changeLogEntry.getCreatedOn(), System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime() > 0);
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) < 5000);
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) > 0);
  
    assertNotNull(changeLogEntry.getSequenceNumber());
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    assertEquals("Context id's should match", changeLogEntry.getContextId(), attributeAssign.getContextId());
  
    assertEquals(group.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    
    //##################################
    // try disabling

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    attributeAssign.setEnabled(false);
    attributeAssign.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    attributeAssign.saveOrUpdate();
  
    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have two records in the change log table", 2, newChangeLogCount);

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry.getContextId()));
    
    assertEquals(group.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));

    //##################################
    // try enabling

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    attributeAssign.setEnabled(true);
    attributeAssign.setEnabledTime(new Timestamp(new Date().getTime() - 100000));
    attributeAssign.saveOrUpdate();

    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have two records in the change log table", 2, newChangeLogCount);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry.getContextId()));
    
    assertEquals(group.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));

    //##################################
    // try deleting now
    
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    attributeAssign.delete();

    ChangeLogTempToEntity.convertRecords();
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
    assertEquals("Should have two records in the change log table", 2, newChangeLogCount);
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry.getContextId()));
    
    assertEquals(group.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
  }

  /**
   * @throws Exception
   */
  public void testPermissionsByMembership() throws Exception {

    // initialize some data
    Role group1 = edu.addChildRole("testGroup1", "testGroup1");
    Role group1y = edu.addChildRole("testGroup1y", "testGroup1y");
    Role group1ysub = edu.addChildRole("testGroup1ysub", "testGroup1ysub");
    Role group1z = edu.addChildRole("testGroup1z", "testGroup1z");
    Role group1a = edu.addChildRole("testGroup1a", "testGroup1a");
    Role group1b = edu.addChildRole("testGroup1b", "testGroup1b");
    Role group2 = edu.addChildRole("testGroup2", "testGroup2");
    Role group3 = edu.addChildRole("testGroup3", "testGroup3");
    Role group3z = edu.addChildRole("testGroup3z", "testGroup3z");
    Role group3a = edu.addChildRole("testGroup3a", "testGroup3a");
    group1.addMember(((Group)group2).toSubject(), true);
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    Member newMember3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    group3.addMember(newMember3.getSubject(), true);
    AttributeDefName attributeDefName = edu.addChildAttributeDefName(attributeDef, "testAttribute", "testAttribute");
    AttributeAssign assign1 = group1.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.DISALLOWED).getAttributeAssign();
    group2.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED).getAttributeAssign();
    group3.getPermissionRoleDelegate().assignSubjectRolePermission(attributeDefName, newMember3.getSubject(), PermissionAllowed.ALLOWED).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // add subj1 to group1, should have notifications for group1 only
    group1.addMember(newMember1.getSubject(), true);    
    ChangeLogTempToEntity.convertRecords();
   
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertNotNull(changeLogEntry);
    
    //make sure some time has passed
    GrouperUtil.sleep(100);
  
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime()  < 5000);
    assertTrue("This should have happened in the last 5 seconds: + " + changeLogEntry.getCreatedOn(), System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime() > 0);
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) < 5000);
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) > 0);
  
    assertNotNull(changeLogEntry.getSequenceNumber());
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
  
    assertEquals(group1.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // add subj1 to group2, should have notifications for group1 and group2
    group2.addMember(newMember1.getSubject(), true);
    ChangeLogTempToEntity.convertRecords();

    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    assertEquals(2, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group2.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // privileges should not notify
    ((Group)group1).grantPriv(newMember1.getSubject(), AccessPrivilege.READ);
    ((Group)group2).grantPriv(newMember2.getSubject(), AccessPrivilege.READ);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // add role sets
    group1y.getRoleInheritanceDelegate().addRoleToInheritFromThis(group1z);
    group1z.getRoleInheritanceDelegate().addRoleToInheritFromThis(group1);
    group1.getRoleInheritanceDelegate().addRoleToInheritFromThis(group1a);
    group1a.getRoleInheritanceDelegate().addRoleToInheritFromThis(group1b);
    group1y.addMember(((Group)group1ysub).toSubject(), true);
    group3z.getRoleInheritanceDelegate().addRoleToInheritFromThis(group3);
    group3.getRoleInheritanceDelegate().addRoleToInheritFromThis(group3a);
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // no new permissions here
    group1a.addMember(newMember1.getSubject(), true);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // notify group1z only
    group1z.addMember(newMember1.getSubject(), true);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNotNull(changeLogEntry);
    assertEquals(group1z.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1z.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // notify group1y only
    group1ysub.addMember(newMember1.getSubject(), true);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNotNull(changeLogEntry);
    assertEquals(group1y.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1y.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // no new permissions here since group3 only has subject role permissions
    group3a.addMember(newMember1.getSubject(), true);
    group3z.addMember(newMember1.getSubject(), true);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // membership delete - notify group1y only
    group1ysub.deleteMember(newMember1.getSubject(), true);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNotNull(changeLogEntry);
    assertEquals(group1y.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1y.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // membership add again but without group set so there's no notification
    group1y.deleteMember(((Group)group1ysub).toSubject(), true);
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    group1ysub.addMember(newMember1.getSubject(), true);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // membership delete - notify group1z only
    group1z.deleteMember(newMember1.getSubject(), true);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNotNull(changeLogEntry);
    assertEquals(group1z.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1z.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // membership add again but without role set so there's no notification
    group1z.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(group1);
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    group1z.addMember(newMember1.getSubject(), true);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // membership delete - notify group1 only
    group1.deleteMember(newMember1.getSubject(), true);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNotNull(changeLogEntry);
    assertEquals(group1.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // membership add again but without assignment so there's no notification
    assign1.delete();
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    group1.addMember(newMember1.getSubject(), true);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
  }
  
  /**
   * @throws Exception
   */
  public void testPermissionsByAction() throws Exception {

    // initialize some data
    Role group1 = edu.addChildRole("testGroup1", "testGroup1");
    Role group2 = edu.addChildRole("testGroup2", "testGroup2");
    Role group3 = edu.addChildRole("testGroup3", "testGroup3");
    Role group4 = edu.addChildRole("testGroup4", "testGroup4");
    Role group5 = edu.addChildRole("testGroup5", "testGroup5");
    group1.getRoleInheritanceDelegate().addRoleToInheritFromThis(group2);
    group2.getRoleInheritanceDelegate().addRoleToInheritFromThis(group3);
    group3.getRoleInheritanceDelegate().addRoleToInheritFromThis(group4);
    group4.getRoleInheritanceDelegate().addRoleToInheritFromThis(group5);
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);

    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    AttributeDefName attributeDefName = edu.addChildAttributeDefName(attributeDef, "testAttribute", "testAttribute");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");
    AttributeAssignAction action3 = attributeDef.getAttributeDefActionDelegate().addAction("testAction3");
    AttributeAssignAction action4 = attributeDef.getAttributeDefActionDelegate().addAction("testAction4");
    AttributeAssignAction action5 = attributeDef.getAttributeDefActionDelegate().addAction("testAction5");
    action1.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2);
    action2.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action3);
    action3.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action4);
    action4.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action5);
    group3.getPermissionRoleDelegate().assignRolePermission("testAction3", attributeDefName, PermissionAllowed.ALLOWED).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // delete action1->action2, should not have notifications
    action1.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(action2);
    ChangeLogTempToEntity.convertRecords();
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // add action1->action2, should not have notifications
    action1.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // delete action2->action3, should not have notifications
    action2.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(action3);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // add action2->action3, should not have notifications
    action2.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action3);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // delete action3->action4, should notify group1, group2, group3
    action3.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(action4);
    ChangeLogTempToEntity.convertRecords();
    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(3, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group2.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group3.getId(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // add action3->action4, should notify group1, group2, group3
    action3.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action4);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(3, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group2.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group3.getId(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // delete action4->action5, should notify group1, group2, group3
    action4.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(action5);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(3, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group2.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group3.getId(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // add action4->action5, should notify group1, group2, group3
    action4.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action5);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(3, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group2.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group3.getId(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // try subject role permissions instead.....
    group3.getPermissionRoleDelegate().removeRolePermission("testAction3", attributeDefName).getAttributeAssign();
    group3.addMember(newMember1.getSubject(), true);
    group3.getPermissionRoleDelegate().assignSubjectRolePermission("testAction3", attributeDefName, newMember1.getSubject(), PermissionAllowed.DISALLOWED).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // delete action2->action3, should not have notifications
    action2.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(action3);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // delete action3->action4, should notify group1, group2, group3
    action3.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(action4);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(1, changeLogEntries.size());
    assertEquals(group3.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // add action3->action4, should notify group1, group2, group3
    action3.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action4);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(1, changeLogEntries.size());
    assertEquals(group3.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // delete action4->action5, should notify group1, group2, group3
    action4.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(action5);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(1, changeLogEntries.size());
    assertEquals(group3.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // add action4->action5, should notify group1, group2, group3
    action4.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action5);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(1, changeLogEntries.size());
    assertEquals(group3.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
  }
  
  /**
   * @throws Exception
   */
  public void testPermissionsByAttributeDefName() throws Exception {

    // initialize some data
    Role group1 = edu.addChildRole("testGroup1", "testGroup1");
    Role group2 = edu.addChildRole("testGroup2", "testGroup2");
    Role group3 = edu.addChildRole("testGroup3", "testGroup3");
    Role group4 = edu.addChildRole("testGroup4", "testGroup4");
    Role group5 = edu.addChildRole("testGroup5", "testGroup5");
    group1.getRoleInheritanceDelegate().addRoleToInheritFromThis(group2);
    group2.getRoleInheritanceDelegate().addRoleToInheritFromThis(group3);
    group3.getRoleInheritanceDelegate().addRoleToInheritFromThis(group4);
    group4.getRoleInheritanceDelegate().addRoleToInheritFromThis(group5);
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);

    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    AttributeDefName attr1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attr2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeDefName attr3 = edu.addChildAttributeDefName(attributeDef, "testAttribute3", "testAttribute3");
    AttributeDefName attr4 = edu.addChildAttributeDefName(attributeDef, "testAttribute4", "testAttribute4");
    AttributeDefName attr5 = edu.addChildAttributeDefName(attributeDef, "testAttribute5", "testAttribute5");
    attr1.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attr2);
    attr2.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attr3);
    attr3.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attr4);
    attr4.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attr5);
    
    group3.getPermissionRoleDelegate().assignRolePermission(attr3, PermissionAllowed.DISALLOWED).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // delete attr1->attr2, should not have notifications
    attr1.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(attr2);
    ChangeLogTempToEntity.convertRecords();
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // add attr1->attr2, should not have notifications
    attr1.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attr2);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // delete attr2->attr3, should not have notifications
    attr2.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(attr3);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // add attr2->attr3, should not have notifications
    attr2.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attr3);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // delete attr3->attr4, should notify group1, group2, group3
    attr3.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(attr4);
    ChangeLogTempToEntity.convertRecords();
    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(3, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group2.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group3.getId(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // add attr3->attr4, should notify group1, group2, group3
    attr3.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attr4);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(3, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group2.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group3.getId(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // delete attr4->attr5, should notify group1, group2, group3
    attr4.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(attr5);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(3, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group2.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group3.getId(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // add attr4->attr5, should notify group1, group2, group3
    attr4.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attr5);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(3, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group2.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group3.getId(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // try subject role permissions instead.....
    group3.getPermissionRoleDelegate().removeRolePermission(attr3).getAttributeAssign();
    group3.addMember(newMember1.getSubject(), true);
    group3.getPermissionRoleDelegate().assignSubjectRolePermission(attr3, newMember1.getSubject(), PermissionAllowed.ALLOWED).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // delete attr2->attr3, should not have notifications
    attr2.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(attr3);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertNull(changeLogEntry);
    
    // delete attr3->attr4, should notify group1, group2, group3
    attr3.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(attr4);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(1, changeLogEntries.size());
    assertEquals(group3.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // add attr3->attr4, should notify group1, group2, group3
    attr3.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attr4);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(1, changeLogEntries.size());
    assertEquals(group3.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // delete attr4->attr5, should notify group1, group2, group3
    attr4.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(attr5);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(1, changeLogEntries.size());
    assertEquals(group3.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // add attr4->attr5, should notify group1, group2, group3
    attr4.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attr5);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(1, changeLogEntries.size());
    assertEquals(group3.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
  }
  
  /**
   * @throws Exception
   */
  public void testPermissionsByRoleSets() throws Exception {

    // initialize some data
    Role group1 = edu.addChildRole("testGroup1", "testGroup1");
    Role group2 = edu.addChildRole("testGroup2", "testGroup2");
    Role group3 = edu.addChildRole("testGroup3", "testGroup3");
    Role group4 = edu.addChildRole("testGroup4", "testGroup4");
    Role group5 = edu.addChildRole("testGroup5", "testGroup5");
    Role group6 = edu.addChildRole("testGroup6", "testGroup6");
    Role group7 = edu.addChildRole("testGroup7", "testGroup7");
    group1.getRoleInheritanceDelegate().addRoleToInheritFromThis(group2);
    group2.getRoleInheritanceDelegate().addRoleToInheritFromThis(group3);
    group3.getRoleInheritanceDelegate().addRoleToInheritFromThis(group4);
    group4.getRoleInheritanceDelegate().addRoleToInheritFromThis(group5);
    group5.getRoleInheritanceDelegate().addRoleToInheritFromThis(group6);
    group6.getRoleInheritanceDelegate().addRoleToInheritFromThis(group7);

    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attr1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    
    group3.getPermissionRoleDelegate().assignRolePermission(attr1, PermissionAllowed.ALLOWED).getAttributeAssign();
    group5.getPermissionRoleDelegate().assignRolePermission(attr1, PermissionAllowed.DISALLOWED).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // delete group1->group2, should notify group1
    group1.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(group2);
    ChangeLogTempToEntity.convertRecords();
    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(1, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // add group1->group2, should notify group1
    group1.getRoleInheritanceDelegate().addRoleToInheritFromThis(group2);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(1, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // delete group4->group5, should notify group1, group2, group3, group4
    group4.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(group5);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(4, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group2.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group3.getId(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group4.getId(), changeLogEntries.get(3).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group4.getName(), changeLogEntries.get(3).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // add group4->group5, should notify group1, group2, group3, group4
    group4.getRoleInheritanceDelegate().addRoleToInheritFromThis(group5);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(4, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group2.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group3.getId(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group4.getId(), changeLogEntries.get(3).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group4.getName(), changeLogEntries.get(3).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // delete group5->group6, no notifications
    group5.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(group6);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(0, changeLogEntries.size());
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
      
    // add group5->group6, no notifications
    group5.getRoleInheritanceDelegate().addRoleToInheritFromThis(group6);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(0, changeLogEntries.size());
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // delete group6->group7, no notifications
    group6.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(group7);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(0, changeLogEntries.size());
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
      
    // add group6->group7, no notifications
    group6.getRoleInheritanceDelegate().addRoleToInheritFromThis(group7);
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(0, changeLogEntries.size());
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
  }

  /**
   * @throws Exception
   */
  public void testPermissionsByRoleAssignment() throws Exception {

    // initialize some data
    Role group1 = edu.addChildRole("testGroup1", "testGroup1");
    Role group2 = edu.addChildRole("testGroup2", "testGroup2");
    Role group3 = edu.addChildRole("testGroup3", "testGroup3");
    Role group4 = edu.addChildRole("testGroup4", "testGroup4");
    Role group5 = edu.addChildRole("testGroup5", "testGroup5");
    group1.getRoleInheritanceDelegate().addRoleToInheritFromThis(group2);
    group2.getRoleInheritanceDelegate().addRoleToInheritFromThis(group3);
    group3.getRoleInheritanceDelegate().addRoleToInheritFromThis(group4);
    group4.getRoleInheritanceDelegate().addRoleToInheritFromThis(group5);

    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attr1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // add assignment for group3, should notify group1, group2, and group3
    group3.getPermissionRoleDelegate().assignRolePermission(attr1, PermissionAllowed.ALLOWED).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();
    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(3, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group2.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group3.getId(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // delete assignment for group3, should notify group1, group2, and group3
    group3.getPermissionRoleDelegate().removeRolePermission(attr1).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(3, changeLogEntries.size());
    assertEquals(group1.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group1.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group2.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group3.getId(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(2).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
 
    // delete role set for group1->group2 and try adding the assignment again, should notify group2, group3
    group1.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(group2);
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    group3.getPermissionRoleDelegate().assignRolePermission(attr1, PermissionAllowed.DISALLOWED).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(2, changeLogEntries.size());
    assertEquals(group2.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group3.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // delete assignment for group3, should notify group2, and group3
    group3.getPermissionRoleDelegate().removeRolePermission(attr1).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(2, changeLogEntries.size());
    assertEquals(group2.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group2.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    assertEquals(group3.getId(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(1).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
  }
  
  /**
   * @throws Exception
   */
  public void testPermissionsBySubjectRoleAssignment() throws Exception {

    // initialize some data
    Role group1 = edu.addChildRole("testGroup1", "testGroup1");
    Role group2 = edu.addChildRole("testGroup2", "testGroup2");
    Role group3 = edu.addChildRole("testGroup3", "testGroup3");
    Role group4 = edu.addChildRole("testGroup4", "testGroup4");
    Role group5 = edu.addChildRole("testGroup5", "testGroup5");
    group1.getRoleInheritanceDelegate().addRoleToInheritFromThis(group2);
    group2.getRoleInheritanceDelegate().addRoleToInheritFromThis(group3);
    group3.getRoleInheritanceDelegate().addRoleToInheritFromThis(group4);
    group4.getRoleInheritanceDelegate().addRoleToInheritFromThis(group5);
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group3.addMember(newMember1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    AttributeDefName attr1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // add assignment for group3, should notify group3 only
    group3.getPermissionRoleDelegate().assignSubjectRolePermission(attr1, newMember1.getSubject(), PermissionAllowed.ALLOWED).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();
    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(1, changeLogEntries.size());
    assertEquals(group3.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // delete assignment for group3, should notify group3 only
    group3.getPermissionRoleDelegate().removeSubjectRolePermission(attr1, newMember1.getSubject()).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();
    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType order by string02")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    assertEquals(1, changeLogEntries.size());
    assertEquals(group3.getId(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group3.getName(), changeLogEntries.get(0).retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
  }
  
  /**
   * @throws Exception
   */
  public void testTypeAssignment() throws Exception {
    
    // initialize some data
    GroupType groupType = GroupType.createType(grouperSession, "testType");
    groupType.addAttribute(grouperSession, "attr1", AccessPrivilege.READ, AccessPrivilege.UPDATE, false);
    groupType.addList(grouperSession, "list1", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    groupType.addList(grouperSession, "list2", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    Group group = edu.addChildGroup("test1", "test1");
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    

    // add assignment
    group.addType(groupType);
    GroupTypeTuple gtt = GrouperDAOFactory.getFactory().getGroupTypeTuple().findByUuidOrKey(
        null, group.getId(), groupType.getUuid(), true);

    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have added exactly one change log temp", 1, newChangeLogTempCount);
    assertEquals("Should be the same", 0, newChangeLogCount);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_TYPE_ASSIGN.getChangeLogType().getId())
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

    assertEquals("Context id's should match", changeLogEntry.getContextId(), gtt.getContextId());
  
    assertEquals(gtt.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_ASSIGN.id));
    assertEquals(gtt.getGroupUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_ASSIGN.groupId));
    assertEquals(gtt.getTypeUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_ASSIGN.typeId));
    assertEquals(group.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_ASSIGN.groupName));
    assertEquals(groupType.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_ASSIGN.typeName));


    //move the temp objects to the regular change log table
    ChangeLogTempToEntity.convertRecords();

    //#########################
    // Check the change log table, and temp table, see the record moved over.
    
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);
  
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_TYPE_ASSIGN.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), gtt.getContextId());
    
    PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(group.getId(), true);
    PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(FieldFinder.find("list1", true).getUuid(), true);
    PITField pitField2 = GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(FieldFinder.find("list2", true).getUuid(), true);
    PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitGroup.getId(), pitField.getId(), false);
    assertNotNull(pitGroupSet);
    assertEquals(gtt.getContextId(), pitGroupSet.getContextId());
    assertTrue(pitGroupSet.isActive());
    assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
    assertNull(pitGroupSet.getEndTimeDb());
    
    pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitGroup.getId(), pitField2.getId(), false);
    assertNotNull(pitGroupSet);
    assertEquals(gtt.getContextId(), pitGroupSet.getContextId());
    assertTrue(pitGroupSet.isActive());
    assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
    assertNull(pitGroupSet.getEndTimeDb());
    
    //##################################
    // try a delete
  
    group.deleteType(groupType);
  
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);

    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry2 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_TYPE_UNASSIGN.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertEquals(gtt.getId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_UNASSIGN.id));
    assertEquals(gtt.getGroupUuid(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_UNASSIGN.groupId));
    assertEquals(gtt.getTypeUuid(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_UNASSIGN.typeId));
    assertEquals(group.getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_UNASSIGN.groupName));
    assertEquals(groupType.getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_UNASSIGN.typeName));

    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitGroup.getId(), pitField.getId(), false);
    assertNotNull(pitGroupSet);
    assertEquals(changeLogEntry2.getContextId(), pitGroupSet.getContextId());
    assertFalse(pitGroupSet.isActive());
    assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
    assertEquals(changeLogEntry2.getCreatedOnDb(), pitGroupSet.getEndTimeDb());
    
    pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitGroup.getId(), pitField2.getId(), false);
    assertNotNull(pitGroupSet);
    assertEquals(changeLogEntry2.getContextId(), pitGroupSet.getContextId());
    assertFalse(pitGroupSet.isActive());
    assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
    assertEquals(changeLogEntry2.getCreatedOnDb(), pitGroupSet.getEndTimeDb());
    
  }
  
  /**
   * @throws Exception
   */
  public void testMembers() throws Exception {
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    

    // add member
    SessionHelper.getRootSession();
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
        return null;
      }
    });
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);

    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have added exactly one change log temp", 1, newChangeLogTempCount);
    assertEquals("Should be the same", 0, newChangeLogCount);

    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBER_ADD.getChangeLogType().getId())
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

    assertEquals("Context id's should match", changeLogEntry.getContextId(), member.getContextId());
  
    assertEquals(member.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_ADD.id));
    assertEquals(member.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_ADD.subjectId));
    assertEquals(member.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_ADD.subjectSourceId));
    assertEquals(member.getSubjectTypeId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_ADD.subjectTypeId));

    //move the temp objects to the regular change log table
    ChangeLogTempToEntity.convertRecords();
    
    //#########################
    // Check the change log table, and temp table, see the record moved over.
    
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);
  
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBER_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), member.getContextId());
    
    // check PIT table
    PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdUnique(member.getUuid(), false);
    assertNotNull(pitMember);
    assertEquals(member.getSubjectId(), pitMember.getSubjectId());
    assertEquals(member.getSubjectSourceId(), pitMember.getSubjectSourceId());
    assertEquals(member.getSubjectTypeId(), pitMember.getSubjectTypeId());
    assertEquals(member.getContextId(), pitMember.getContextId());
    
    //##################################
    // try an update
  
    //try an update of one field
    member.setSubjectId("test.subject.0a");
  
    final Member MEMBER = member;
  
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().update(MEMBER);
        return null;
      }
    });
  
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have one in temp table", 1, newChangeLogTempCount);
    assertEquals("Should have one record in the change log table", 1, newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry2 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBER_UPDATE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));

    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    assertEquals(member.getUuid(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.id));
    assertEquals(member.getSubjectId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectId));
    assertEquals(member.getSubjectSourceId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectSourceId));
    assertEquals(member.getSubjectTypeId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectTypeId));
    assertEquals("subjectId", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged));
    assertEquals("test.subject.0", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyOldValue));
    assertEquals("test.subject.0a", changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyNewValue));
    
    // Check PIT table
    pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdUnique(member.getUuid(), false);
    assertNotNull(pitMember);
    assertEquals(member.getSubjectId(), pitMember.getSubjectId());
    assertEquals(member.getSubjectSourceId(), pitMember.getSubjectSourceId());
    assertEquals(member.getSubjectTypeId(), pitMember.getSubjectTypeId());
    assertEquals(member.getContextId(), pitMember.getContextId());
    
    //##################################
    // try a delete
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().delete(MEMBER);
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
    
    ChangeLogEntry changeLogEntry3 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBER_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertEquals(member.getUuid(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.MEMBER_DELETE.id));
    assertEquals(member.getSubjectId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.MEMBER_DELETE.subjectId));
    assertEquals(member.getSubjectSourceId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.MEMBER_DELETE.subjectSourceId));
    assertEquals(member.getSubjectTypeId(), changeLogEntry3.retrieveValueForLabel(ChangeLogLabels.MEMBER_DELETE.subjectTypeId));

    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry3.getContextId()));
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry3.getContextId()));
    assertEquals("Context id's should match", changeLogEntry3.getContextId(), member.getContextId());
  }

  
  /**
   * @throws Exception
   */
  public void testAttributeDefs() throws Exception {
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    //add attr def
    SessionHelper.getRootSession();
    AttributeDef resourcesDef = edu.addChildAttributeDef("attrdef", AttributeDefType.perm);
    
    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    // 4 temp change log changes - attributeDef, action, actionSet, privilege
    assertEquals("Should have added exactly four change log temp", 4, newChangeLogTempCount);
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
    assertEquals("Should have four records in the change log table", 4, newChangeLogCount);
  
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ATTRIBUTE_DEF_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), resourcesDef.getContextId());
    
    // check PIT table
    PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(resourcesDef.getId(), false);
    assertNotNull(pitAttributeDef);
    assertEquals(resourcesDef.getName(), pitAttributeDef.getName());
    assertEquals(resourcesDef.getStemId(), pitAttributeDef.getPITStem().getSourceId());
    assertEquals(resourcesDef.getContextId(), pitAttributeDef.getContextId());
    
    Iterator<Field> allFields = FieldFinder.findAll().iterator();
    while (allFields.hasNext()) {
      Field currField = allFields.next();
      PITField pitCurrField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(currField.getUuid(), true);
      PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitAttributeDef.getId(), pitCurrField.getId(), false);
      if (currField.isAttributeDefListField()) {
        assertNotNull(pitGroupSet);
        assertEquals(changeLogEntry.getContextId(), pitGroupSet.getContextId());
        assertTrue(pitGroupSet.isActive());
        assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
        assertNull(pitGroupSet.getEndTimeDb()); 
      } else {
        assertNull(pitGroupSet);
      }
    }
    
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
    assertEquals("Should have four records in the change log table", 4, newChangeLogCount);
  
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
  
    assertEquals("Should have four in temp table", 4, newChangeLogTempCount);
    assertEquals("Should have five records in the change log table", 5, newChangeLogCount);

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
    
    // check PIT table
    pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(resourcesDef.getId(), false);
    allFields = FieldFinder.findAll().iterator();
    while (allFields.hasNext()) {
      Field currField = allFields.next();
      PITField pitCurrField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(currField.getUuid(), true);
      PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitAttributeDef.getId(), pitCurrField.getId(), false);
      if (currField.isAttributeDefListField()) {
        assertNotNull(pitGroupSet);
        assertEquals(changeLogEntry3.getContextId(), pitGroupSet.getContextId());
        assertFalse(pitGroupSet.isActive());
        assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
        assertEquals(changeLogEntry3.getCreatedOnDb(), pitGroupSet.getEndTimeDb());
      } else {
        assertNull(pitGroupSet);
      }
    }
    
    //##################################
    // try updating the name
    
    AttributeDef resourcesDef2 = edu.addChildAttributeDef("attrdef2", AttributeDefType.perm);
    ChangeLogTempToEntity.convertRecords();
    PITAttributeDef pitAttributeDef2 = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(resourcesDef2.getId(), false);
    assertNotNull(pitAttributeDef2);
    assertEquals(resourcesDef2.getName(), pitAttributeDef2.getName());
    assertEquals(resourcesDef2.getContextId(), pitAttributeDef2.getContextId());
    
    resourcesDef2.setExtension("attrdef2a");
    resourcesDef2.store();
    ChangeLogTempToEntity.convertRecords();
    pitAttributeDef2 = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(resourcesDef2.getId(), false);
    assertNotNull(pitAttributeDef2);
    assertEquals(resourcesDef2.getName(), pitAttributeDef2.getName());
    assertEquals(resourcesDef2.getContextId(), pitAttributeDef2.getContextId());

    //##################################
    // try adding and deleting before change log daemon runs
    
    AttributeDef resourcesDef3 = edu.addChildAttributeDef("attrdef3", AttributeDefType.perm);
    resourcesDef3.setExtension("attrdef3a");
    resourcesDef3.store();
    resourcesDef3.delete();
    ChangeLogTempToEntity.convertRecords();
    PITAttributeDef pitAttributeDef3 = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(resourcesDef3.getId(), false);
    assertNotNull(pitAttributeDef3);
    assertEquals("edu:attrdef3a", pitAttributeDef3.getName());
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
    Group group1 = edu.addChildGroup("group1", "group1");
    Group group2 = edu.addChildGroup("group2", "group2");
    GroupType groupType = GroupType.createType(grouperSession, "test1");
    group1.addType(groupType);
    group2.addType(groupType);
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    int changeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_change_log_entry_temp");
    int changeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    Field field = groupType.addList(grouperSession, "testList", AccessPrivilege.READ, AccessPrivilege.ADMIN);
    String fieldId = field.getUuid();
    
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
    
    // check PIT table
    PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(field.getUuid(), false);
    assertNotNull(pitField);
    assertEquals(field.getName(), pitField.getName());
    assertEquals(field.getContextId(), pitField.getContextId());

    PITGroup pitGroup1 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(group1.getId(), true);
    PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitGroup1.getId(), pitField.getId(), false);
    assertNotNull(pitGroupSet);
    assertEquals(changeLogEntry.getContextId(), pitGroupSet.getContextId());
    assertTrue(pitGroupSet.isActive());
    assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
    assertNull(pitGroupSet.getEndTimeDb());
    
    PITGroup pitGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(group2.getId(), true);
    pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitGroup2.getId(), pitField.getId(), false);
    assertNotNull(pitGroupSet);
    assertEquals(changeLogEntry.getContextId(), pitGroupSet.getContextId());
    assertTrue(pitGroupSet.isActive());
    assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
    assertNull(pitGroupSet.getEndTimeDb());
    
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
   
    // check PIT table
    pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitGroup1.getId(), pitField.getId(), false);
    assertNotNull(pitGroupSet);
    assertEquals(changeLogEntry.getContextId(), pitGroupSet.getContextId());
    assertTrue(pitGroupSet.isActive());
    assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
    assertNull(pitGroupSet.getEndTimeDb());
    
    pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitGroup2.getId(), pitField.getId(), false);
    assertNotNull(pitGroupSet);
    assertEquals(changeLogEntry.getContextId(), pitGroupSet.getContextId());
    assertTrue(pitGroupSet.isActive());
    assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
    assertNull(pitGroupSet.getEndTimeDb());
    
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
    
    // check PIT table
    pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitGroup1.getId(), pitField.getId(), false);
    assertNotNull(pitGroupSet);
    assertEquals(changeLogEntry2.getContextId(), pitGroupSet.getContextId());
    assertFalse(pitGroupSet.isActive());
    assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
    assertEquals(changeLogEntry2.getCreatedOnDb(), pitGroupSet.getEndTimeDb());
    
    pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitGroup2.getId(), pitField.getId(), false);
    assertNotNull(pitGroupSet);
    assertEquals(changeLogEntry2.getContextId(), pitGroupSet.getContextId());
    assertFalse(pitGroupSet.isActive());
    assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
    assertEquals(changeLogEntry2.getCreatedOnDb(), pitGroupSet.getEndTimeDb());
    
    //##################################
    // try updating the name
    
    Field field2 = groupType.addList(grouperSession, "testList2", AccessPrivilege.READ, AccessPrivilege.ADMIN);
    ChangeLogTempToEntity.convertRecords();
    PITField pitField2 = GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(field2.getUuid(), false);
    assertNotNull(pitField2);
    assertEquals(field2.getName(), pitField2.getName());
    assertEquals(field2.getContextId(), pitField2.getContextId());
    
    field2.setName("testList2a");
    field2.store();
    ChangeLogTempToEntity.convertRecords();
    pitField2 = GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(field2.getUuid(), false);
    assertNotNull(pitField2);
    assertEquals(field2.getName(), pitField2.getName());
    assertEquals(field2.getContextId(), pitField2.getContextId());

    //##################################
    // try adding and deleting before change log daemon runs
    
    Field field3 = groupType.addList(grouperSession, "testList3", AccessPrivilege.READ, AccessPrivilege.ADMIN);
    groupType.deleteField(grouperSession, field3.getName());
    ChangeLogTempToEntity.convertRecords();
    PITField pitField3 = GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(field3.getUuid(), false);
    assertNotNull(pitField3);
    assertEquals("testList3", pitField3.getName());
  }

  /**
   * @throws Exception 
   * 
   */
  public void testGroups() throws Exception {
  
    //get things moved over:
    ChangeLogTempToEntity.convertRecords();
    
    GrouperUtil.sleep(1000);
    long testStart = System.currentTimeMillis() * 1000;
    GrouperUtil.sleep(1000);
    
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
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and createdOnDb > :theCreatedOnDb")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ADD.getChangeLogType().getId())
      .setLong("theCreatedOnDb", testStart)
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), group.getContextId());
    
    // check PIT table
    PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(group.getId(), false);
    assertNotNull(pitGroup);
    assertEquals(group.getName(), pitGroup.getName());
    assertEquals(group.getParentUuid(), pitGroup.getPITStem().getSourceId());
    assertEquals(group.getContextId(), pitGroup.getContextId());
    
    Iterator<Field> allFields = FieldFinder.findAll().iterator();
    while (allFields.hasNext()) {
      Field currField = allFields.next();
      PITField pitCurrField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(currField.getUuid(), true);
      PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitGroup.getId(), pitCurrField.getId(), false);
      if (currField.isGroupListField()) {
        assertNotNull(pitGroupSet);
        assertEquals(changeLogEntry.getContextId(), pitGroupSet.getContextId());
        assertTrue(pitGroupSet.isActive());
        assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
        assertNull(pitGroupSet.getEndTimeDb()); 
      } else {
        assertNull(pitGroupSet);
      }
    }
    
    //##################################
    // try an update
  
    //try an update of two field
    group.setDisplayExtension("test1a");
    group.setDescription("test1a");
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
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and createdOnDb > :theCreatedOnDb order by " + propertyChangedFieldName)
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_UPDATE.getChangeLogType().getId())
      .setLong("theCreatedOnDb", testStart)
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
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and createdOnDb > :theCreatedOnDb")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DELETE.getChangeLogType().getId())
      .setLong("theCreatedOnDb", testStart)
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    assertEquals("Context id's should match", changeLogEntry2.getContextId(), group.getContextId());
        
    assertEquals(group.getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_DELETE.name));
    assertEquals(group.getUuid(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.GROUP_DELETE.id));
      
    // check PIT table
    allFields = FieldFinder.findAll().iterator();
    while (allFields.hasNext()) {
      Field currField = allFields.next();
      PITField pitCurrField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(currField.getUuid(), true);
      PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitGroup.getId(), pitCurrField.getId(), false);
      if (currField.isGroupListField()) {
        assertNotNull(pitGroupSet);
        assertEquals(changeLogEntry2.getContextId(), pitGroupSet.getContextId());
        assertFalse(pitGroupSet.isActive());
        assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
        assertEquals(changeLogEntry2.getCreatedOnDb(), pitGroupSet.getEndTimeDb());
      } else {
        assertNull(pitGroupSet);
      }
    }
    
    //##################################
    // try updating the name
    
    Group group2 = edu.addChildGroup("group2", "group2");
    ChangeLogTempToEntity.convertRecords();
    PITGroup pitGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(group2.getId(), false);
    assertNotNull(pitGroup2);
    assertEquals(group2.getName(), pitGroup2.getName());
    assertEquals(group2.getContextId(), pitGroup2.getContextId());
    
    group2.setExtension("group2a");
    group2.store();
    ChangeLogTempToEntity.convertRecords();
    pitGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(group2.getId(), false);
    assertNotNull(pitGroup2);
    assertEquals(group2.getName(), pitGroup2.getName());
    assertEquals(group2.getContextId(), pitGroup2.getContextId());
    
    
    //##################################
    // try updating the stem id
    
    Stem stem1 = edu.addChildStem("stem1", "stem1");
    Stem stem2 = edu.addChildStem("stem2", "stem2");
    Group groupToMove = stem1.addChildGroup("groupToMove", "groupToMove");
    ChangeLogTempToEntity.convertRecords();

    groupToMove.move(stem2);
    ChangeLogTempToEntity.convertRecords();
    
    PITGroup pitGroupToMove = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(groupToMove.getId(), false);
    assertNotNull(pitGroupToMove);
    assertEquals(groupToMove.getParentUuid(), pitGroupToMove.getPITStem().getSourceId());
    assertEquals(groupToMove.getContextId(), pitGroupToMove.getContextId());


    //##################################
    // try adding and deleting before change log daemon runs
    
    Group group3 = edu.addChildGroup("group3","group3");
    group3.setExtension("group3a");
    group3.store();
    group3.delete();
    ChangeLogTempToEntity.convertRecords();
    PITGroup pitGroup3 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(group3.getId(), false);
    assertNotNull(pitGroup3);
    assertEquals("edu:group3a", pitGroup3.getName());
  }

  /**
   * @throws Exception 
   * 
   */
  public void testEntities() throws Exception {
  
    //get things moved over:
    ChangeLogTempToEntity.convertRecords();
    
    GrouperUtil.sleep(1000);
    long testStart = System.currentTimeMillis() * 1000;
    GrouperUtil.sleep(1000);
    
    int changeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_change_log_entry_temp");
    int changeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    //add a group
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Entity entity = new EntitySave(grouperSession).assignName("edu:test1").save();
    
    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertTrue("Should have added more than one change log temp", changeLogTempCount+1 <= newChangeLogTempCount);
    
    assertEquals("Should be the same", changeLogCount, newChangeLogCount);
  
    //get one entry in there
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryTemp where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ENTITY_ADD.getChangeLogType().getId())
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
  
    assertEquals("Context id's should match", changeLogEntry.getContextId(), entity.getContextId());
  
    assertEquals(entity.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ENTITY_ADD.name));
    assertEquals(entity.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ENTITY_ADD.id));
  
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
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and createdOnDb > :theCreatedOnDb")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ENTITY_ADD.getChangeLogType().getId())
      .setLong("theCreatedOnDb", testStart)
      .uniqueResult(ChangeLogEntry.class);
    
    assertTrue(!StringUtils.isBlank(changeLogEntry.getContextId()));
    
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertNotNull(changeLogEntry.getSequenceNumber());
    
    assertEquals("Context id's should match", changeLogEntry.getContextId(), entity.getContextId());
    
    // check PIT table
    PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(entity.getId(), false);
    assertNotNull(pitGroup);
    assertEquals(entity.getName(), pitGroup.getName());
    assertEquals(entity.getStemId(), pitGroup.getPITStem().getSourceId());
    assertEquals(entity.getContextId(), pitGroup.getContextId());
    
    Iterator<Field> allFields = FieldFinder.findAll().iterator();
    while (allFields.hasNext()) {
      Field currField = allFields.next();
      PITField pitCurrField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(currField.getUuid(), true);
      PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitGroup.getId(), pitCurrField.getId(), false);
      if (currField.isEntityListField()) {
        assertNotNull(currField.getName(), pitGroupSet);
        assertEquals(changeLogEntry.getContextId(), pitGroupSet.getContextId());
        assertTrue(pitGroupSet.isActive());
        assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
        assertNull(pitGroupSet.getEndTimeDb()); 
      } else {
        assertNull(pitGroupSet);
      }
    }
    
    //##################################
    // try an update
  
    //try an update of two field
    entity.setDisplayExtension("test1a");
    entity.setDescription("test1a");
    entity.store();
  
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have two in temp table", changeLogTempCount+2, newChangeLogTempCount);
    assertTrue("Should have more than one record in the change log table: " + changeLogCount + ", " + newChangeLogCount, 
        changeLogCount+1 <= newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    String propertyChangedFieldName = ChangeLogTypeBuiltin.ENTITY_UPDATE.getChangeLogType().retrieveChangeLogEntryFieldForLabel(
        ChangeLogLabels.ENTITY_UPDATE.propertyChanged.name());
    
    List<ChangeLogEntry> changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and createdOnDb > :theCreatedOnDb order by " + propertyChangedFieldName)
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ENTITY_UPDATE.getChangeLogType().getId())
      .setLong("theCreatedOnDb", testStart)
      .list(ChangeLogEntry.class);
  
    {
      ChangeLogEntry descriptionEntry = changeLogEntries.get(0);
    
      assertTrue("contextId should exist", StringUtils.isNotBlank(descriptionEntry.getContextId()));
      
      assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
          descriptionEntry.getContextId()));
      
      assertEquals(entity.getName(), descriptionEntry.retrieveValueForLabel(ChangeLogLabels.ENTITY_UPDATE.name));
      assertEquals(entity.getId(), descriptionEntry.retrieveValueForLabel(ChangeLogLabels.ENTITY_UPDATE.id));
      assertEquals(ChangeLogLabels.ENTITY_UPDATE.description.name(), 
          descriptionEntry.retrieveValueForLabel(ChangeLogLabels.ENTITY_UPDATE.propertyChanged));
      assertNull(descriptionEntry.retrieveValueForLabel(ChangeLogLabels.ENTITY_UPDATE.propertyOldValue));
    }
    
    {
      ChangeLogEntry displayExtensionEntry = changeLogEntries.get(1);
      assertTrue("contextId should exist", StringUtils.isNotBlank(displayExtensionEntry.getContextId()));
      
      assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
          displayExtensionEntry.getContextId()));
      
      assertEquals(entity.getName(), displayExtensionEntry.retrieveValueForLabel(ChangeLogLabels.ENTITY_UPDATE.name));
      assertEquals(entity.getId(), displayExtensionEntry.retrieveValueForLabel(ChangeLogLabels.ENTITY_UPDATE.id));
      assertEquals(ChangeLogLabels.ENTITY_UPDATE.displayExtension.name(), 
          displayExtensionEntry.retrieveValueForLabel(ChangeLogLabels.ENTITY_UPDATE.propertyChanged));
      assertEquals("test1", displayExtensionEntry.retrieveValueForLabel(ChangeLogLabels.ENTITY_UPDATE.propertyOldValue));
    }
    
    //##################################
    // try a delete
    
    entity.delete();
  
    newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry_temp");
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
  
    assertTrue("Should have more than one in temp table", changeLogTempCount+1 <= newChangeLogTempCount);
    assertTrue("Should have more than two record in the change log table", changeLogCount+2 <= newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    ChangeLogEntry changeLogEntry2 = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and createdOnDb > :theCreatedOnDb")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.ENTITY_DELETE.getChangeLogType().getId())
      .setLong("theCreatedOnDb", testStart)
      .uniqueResult(ChangeLogEntry.class);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(changeLogEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(changeLogEntry.getContextId(), 
        changeLogEntry2.getContextId()));
    
    assertEquals("Context id's should match", changeLogEntry2.getContextId(), entity.getContextId());
        
    assertEquals(entity.getName(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ENTITY_DELETE.name));
    assertEquals(entity.getId(), changeLogEntry2.retrieveValueForLabel(ChangeLogLabels.ENTITY_DELETE.id));
      
    // check PIT table
    allFields = FieldFinder.findAll().iterator();
    while (allFields.hasNext()) {
      Field currField = allFields.next();
      PITField pitCurrField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(currField.getUuid(), true);
      PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitGroup.getId(), pitCurrField.getId(), false);
      if (currField.isEntityListField() ) {
        assertNotNull(pitGroupSet);
        assertEquals(changeLogEntry2.getContextId(), pitGroupSet.getContextId());
        assertFalse(pitGroupSet.isActive());
        assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
        assertEquals(changeLogEntry2.getCreatedOnDb(), pitGroupSet.getEndTimeDb());
      } else {
        assertNull(pitGroupSet);
      }
    }
    
    //##################################
    // try updating the name
    
    Entity entity2 = new EntitySave(grouperSession).assignName("edu:entity2").save();
    ChangeLogTempToEntity.convertRecords();
    PITGroup pitGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(entity2.getId(), false);
    assertNotNull(pitGroup2);
    assertEquals(entity2.getName(), pitGroup2.getName());
    assertEquals(entity2.getContextId(), pitGroup2.getContextId());
    
    entity2.setExtension("entity2a");
    entity2.store();
    ChangeLogTempToEntity.convertRecords();
    pitGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(entity2.getId(), false);
    assertNotNull(pitGroup2);
    assertEquals(entity2.getName(), pitGroup2.getName());
    assertEquals(entity2.getContextId(), pitGroup2.getContextId());
    
    
    //##################################
    // try updating the stem id
    
    edu.addChildStem("stem1", "stem1");
    Stem stem2 = edu.addChildStem("stem2", "stem2");
    Entity entityToMove = new EntitySave(grouperSession).assignName("edu:stem1:groupToMove").save();
    ChangeLogTempToEntity.convertRecords();

    entityToMove.move(stem2);
    ChangeLogTempToEntity.convertRecords();
    
    PITGroup pitGroupToMove = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(entityToMove.getId(), false);
    assertNotNull(pitGroupToMove);
    assertEquals(entityToMove.getStemId(), pitGroupToMove.getPITStem().getSourceId());
    assertEquals(entityToMove.getContextId(), pitGroupToMove.getContextId());


    //##################################
    // try adding and deleting before change log daemon runs
    
    Entity entity3 = new EntitySave(grouperSession).assignName("edu:entity3").save();
    entity3.setExtension("entity3a");
    entity3.store();
    entity3.delete();
    ChangeLogTempToEntity.convertRecords();
    PITGroup pitGroup3 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(entity3.getId(), false);
    assertNotNull(pitGroup3);
    assertEquals("edu:entity3a", pitGroup3.getName());
  }

  /**
   * @throws Exception 
   * 
   */
  public void testStems() throws Exception {
  
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    int changeLogTempCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_change_log_entry_temp");
    int changeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    //add a stem
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
    
    // check PIT table
    PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem.getUuid(), false);
    assertNotNull(pitStem);
    assertEquals(stem.getName(), pitStem.getName());
    assertEquals(stem.getParentUuid(), pitStem.getParentPITStem().getSourceId());
    assertEquals(stem.getContextId(), pitStem.getContextId());
    
    Iterator<Field> allFields = FieldFinder.findAll().iterator();
    while (allFields.hasNext()) {
      Field currField = allFields.next();
      PITField pitCurrField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(currField.getUuid(), true);
      PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitStem.getId(), pitCurrField.getId(), false);
      if (currField.isStemListField()) {
        assertNotNull(pitGroupSet);
        assertEquals(changeLogEntry.getContextId(), pitGroupSet.getContextId());
        assertTrue(pitGroupSet.isActive());
        assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
        assertNull(pitGroupSet.getEndTimeDb()); 
      } else {
        assertNull(pitGroupSet);
      }
    }
    
    //##################################
    // try an update
  
    //try an update of two field
    stem.setDisplayExtension("test1a");
    stem.setDescription("test1a");
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
    
    // check PIT table
    allFields = FieldFinder.findAll().iterator();
    while (allFields.hasNext()) {
      Field currField = allFields.next();
      PITField pitCurrField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(currField.getUuid(), true);
      PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findSelfPITGroupSet(pitStem.getId(), pitCurrField.getId(), false);
      if (currField.isStemListField()) {
        assertNotNull(pitGroupSet);
        assertEquals(changeLogEntry2.getContextId(), pitGroupSet.getContextId());
        assertFalse(pitGroupSet.isActive());
        assertEquals(changeLogEntry.getCreatedOnDb(), pitGroupSet.getStartTimeDb());
        assertEquals(changeLogEntry2.getCreatedOnDb(), pitGroupSet.getEndTimeDb());
      } else {
        assertNull(pitGroupSet);
      }
    }
    
    //##################################
    // try updating the name
    
    Stem stem2 = edu.addChildStem("stem2", "stem2");
    ChangeLogTempToEntity.convertRecords();
    PITStem pitStem2 = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem2.getUuid(), false);
    assertNotNull(pitStem2);
    assertEquals(stem2.getName(), pitStem2.getName());
    assertEquals(stem2.getContextId(), pitStem2.getContextId());
    
    stem2.setExtension("stem2a");
    stem2.store();
    ChangeLogTempToEntity.convertRecords();
    pitStem2 = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem2.getUuid(), false);
    assertNotNull(pitStem2);
    assertEquals(stem2.getName(), pitStem2.getName());
    assertEquals(stem2.getContextId(), pitStem2.getContextId());
    
    //##################################
    // try adding and deleting before change log daemon runs
    
    Stem stem3 = edu.addChildStem("stem3","stem3");
    stem3.setExtension("stem3a");
    stem3.store();
    stem3.delete();
    ChangeLogTempToEntity.convertRecords();
    PITStem pitStem3 = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem3.getUuid(), false);
    assertNotNull(pitStem3);
    assertEquals("edu:stem3a", pitStem3.getName());
    
    //##################################
    // try updating the parent stem id
    
    Stem stem4 = edu.addChildStem("stem4", "stem4");
    Stem stem5 = edu.addChildStem("stem5", "stem5");
    Group group1 = stem4.addChildGroup("group1", "group1");
    ChangeLogTempToEntity.convertRecords();

    stem4.move(stem5);
    ChangeLogTempToEntity.convertRecords();
    
    PITGroup pitGroup1 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(group1.getId(), false);
    PITStem pitStem4 = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem4.getUuid(), false);
    PITStem pitStem5 = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem5.getUuid(), false);
    
    stem4 = StemFinder.findByName(grouperSession, stem4.getName(), true);
    stem5 = StemFinder.findByName(grouperSession, stem5.getName(), true);
    group1 = GroupFinder.findByName(grouperSession, group1.getName(), true);
    
    assertNotNull(pitGroup1);
    assertEquals(group1.getParentUuid(), pitGroup1.getPITStem().getSourceId());
    assertEquals(group1.getContextId(), pitGroup1.getContextId());
    
    assertNotNull(pitStem4);
    assertEquals(stem4.getParentUuid(), pitStem4.getParentPITStem().getSourceId());
    assertEquals(stem4.getContextId(), pitStem4.getContextId());
    
    assertNotNull(pitStem5);
    assertEquals(stem5.getParentUuid(), pitStem5.getParentPITStem().getSourceId());
    assertEquals(stem5.getContextId(), pitStem5.getContextId());
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
  
    assertEquals("Should have three in temp table", changeLogTempCount+3, newChangeLogTempCount);
    assertTrue("Should have more than one record in the change log table: " + changeLogCount + ", " + newChangeLogCount, 
        changeLogCount+1 <= newChangeLogCount);
  
    ChangeLogTempToEntity.convertRecords();
    
    int newerChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertTrue("Should have three more records in the change log table: " + newChangeLogCount + ", " + newerChangeLogCount, 
        newChangeLogCount+3 == newerChangeLogCount);
    
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
    
    
    //##################################
    // try multiple immediate memberships

    Group group2 = this.edu.addChildGroup("test2", "test2");

    ChangeLogTempToEntity.convertRecords();
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
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

    assertEquals("Should have 9 change log entries", 9, changeLogEntries.size());

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
    
    assertEquals("Should have 9 change log entries", 9, changeLogEntries.size());
    
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
          FieldFinder.find(Field.FIELD_NAME_UPDATERS, true), "effective", true, true);
    Membership g1g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g1.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_UPDATERS, true), "effective", true, true);
    Membership s0g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s0.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_CREATORS, true), "effective", true, true);
    Membership s0g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s0.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_CREATORS, true), "effective", true, true);
    Membership a0g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a0.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("attrUpdaters", true), "effective", true, true);
    Membership a0g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a0.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("attrUpdaters", true), "effective", true, true);
    
    ChangeLogTempToEntity.convertRecords();
    

    // note that subj2 already had the memberships and privileges...
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 10 new change log entries", 10, newChangeLogCount);
    
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
    
    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 10 new change log entries", 10, newChangeLogCount);

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

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);

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
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);

    
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
          FieldFinder.find(Field.FIELD_NAME_UPDATERS, true), "effective", true, true);
    g1g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g1.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_UPDATERS, true), "effective", true, true);
    s0g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s0.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_CREATORS, true), "effective", true, true);
    s0g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s0.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_CREATORS, true), "effective", true, true);
    a0g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a0.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("attrUpdaters", true), "effective", true, true);
    a0g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a0.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("attrUpdaters", true), "effective", true, true);
  
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    
    assertEquals("Should have 10 new change log entries", 10, newChangeLogCount);

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
    
    
    assertEquals("Should have 15 new change log entries", 15, newChangeLogCount);

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
          FieldFinder.find(Field.FIELD_NAME_UPDATERS, true), "effective", true, true);
    g1g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g1.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_UPDATERS, true), "effective", true, true);
    s0g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s0.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_CREATORS, true), "effective", true, true);
    s0g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s0.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_CREATORS, true), "effective", true, true);
    a0g4Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a0.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find("attrUpdaters", true), "effective", true, true);
    a0g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByAttrDefOwnerAndMemberAndFieldAndType(a0.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find("attrUpdaters", true), "effective", true, true);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    
    assertEquals("Should have 15 new change log entries", 15, newChangeLogCount);

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
    
    
    assertEquals("Should have 10 new change log entries", 10, newChangeLogCount);

    verifyEffectiveMembershipsDelete(g3g4Mship, g3g5Mship, g2g4Mship, g2g5Mship, g1g4Priv, g1g5Priv, 
        s0g4Priv, s0g5Priv, a0g4Priv, a0g5Priv, changeLogEntry.getContextId());
    
    
    //##################################
    // try changing group's name and member's subject id after adding membership and verify change log
    
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    Member newMember4 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ4, true);
    Group g6 = this.edu.addChildGroup("group6", "group6");
    g6.addMember(newMember4.getSubject());
    g6.setExtension("group6b");
    g6.store();
    String oldSubjectId = newMember4.getSubjectId();
    String newSubjectId = oldSubjectId + "new";
    newMember4.setSubjectId(newSubjectId);
    newMember4.store();
    ChangeLogTempToEntity.convertRecords();
    g6.deleteMember(newMember4.getSubject());
    ChangeLogTempToEntity.convertRecords();

    ChangeLogEntry membershipAddChangeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    assertNotNull(membershipAddChangeLogEntry);
    assertEquals("edu:group6", membershipAddChangeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    assertEquals(oldSubjectId, membershipAddChangeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    
    ChangeLogEntry membershipDeleteChangeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);

    assertNotNull(membershipDeleteChangeLogEntry);
    assertEquals("edu:group6b", membershipDeleteChangeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    assertEquals(newSubjectId, membershipDeleteChangeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
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
          FieldFinder.find(Field.FIELD_NAME_OPTINS, true), "immediate", true, true);
    Membership g3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_OPTINS, true), "effective", true, true);


    ChangeLogTempToEntity.convertRecords();

    // note that subj2 already had the privilege...
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);
    
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
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

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
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);

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
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);

    
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
          FieldFinder.find(Field.FIELD_NAME_OPTINS, true), "immediate", true, true);
    g3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_OPTINS, true), "effective", true, true);
  
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

    verifyEffectiveAccessPrivilegesAdd(g3g4Priv, g3g5Priv, g3g4Priv.getContextId());
    
    
    //##################################
    // try changing member and make sure change log has correct deletes
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    Membership immediate = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_OPTINS, true), "immediate", true, true);
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
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);

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
          FieldFinder.find(Field.FIELD_NAME_OPTINS, true), "immediate", true, true);
    g3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_OPTINS, true), "effective", true, true);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);

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
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

    verifyEffectiveAccessPrivilegesDelete(g3g4Priv, g3g5Priv, changeLogEntry.getContextId());
    
    //##################################
    // try multiple immediate memberships
    
    Group group = this.edu.addChildGroup("test1", "test1");

    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    
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
    
    
    assertEquals("Should have 7 change log entries", 7, changeLogEntries.size());
    
    group.revokePriv(newMember1.getSubject(), AccessPrivilege.UPDATE);
    group.revokePriv(newMember2.getSubject(), AccessPrivilege.UPDATE);
    group2.delete();
    group.revokePriv(newMember3.getSubject(), AccessPrivilege.UPDATE);
    ChangeLogTempToEntity.convertRecords();

    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    
    assertEquals("Should have 7 change log entries", 7, changeLogEntries.size());
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
          FieldFinder.find(Field.FIELD_NAME_STEMMERS, true), "immediate", true, true);
    Membership s3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_STEMMERS, true), "effective", true, true);


    ChangeLogTempToEntity.convertRecords();

    // note that subj2 already had the privilege...
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

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
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

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
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);

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
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);

    
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
          FieldFinder.find(Field.FIELD_NAME_STEMMERS, true), "immediate", true, true);
    s3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_STEMMERS, true), "effective", true, true);
  
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

    verifyEffectiveNamingPrivilegesAdd(s3g4Priv, s3g5Priv, s3g4Priv.getContextId());
    
    
    //##################################
    // try changing member and make sure change log has correct deletes
    
    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    Membership immediate = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s3.getUuid(), g4.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_STEMMERS, true), "immediate", true, true);
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
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);

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
          FieldFinder.find(Field.FIELD_NAME_STEMMERS, true), "immediate", true, true);
    s3g5Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByStemOwnerAndMemberAndFieldAndType(s3.getUuid(), g5.toMember().getUuid(), 
          FieldFinder.find(Field.FIELD_NAME_STEMMERS, true), "effective", true, true);
    
    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);

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
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

    verifyEffectiveNamingPrivilegesDelete(s3g4Priv, s3g5Priv, changeLogEntry.getContextId());
    
    
    //##################################
    // try multiple immediate memberships
    
    Stem stem = this.edu.addChildStem("test1", "test1");

    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    
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
    
    
    assertEquals("Should have 7 change log entries", 7, changeLogEntries.size());
    
    stem.revokePriv(newMember1.getSubject(), NamingPrivilege.STEM);
    stem.revokePriv(newMember2.getSubject(), NamingPrivilege.STEM);
    group.delete();
    stem.revokePriv(newMember3.getSubject(), NamingPrivilege.STEM);
    ChangeLogTempToEntity.convertRecords();

    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    
    assertEquals("Should have 7 change log entries", 7, changeLogEntries.size());
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

    
    ChangeLogTempToEntity.convertRecords();

    // note that subj2 already had the privilege...
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

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
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

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
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);

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
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);

    
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
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

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
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);

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
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);

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
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

    verifyEffectiveAttributeDefPrivilegesDelete(a3g4Priv, a3g5Priv, changeLogEntry.getContextId());
   
    //##################################
    // try multiple immediate memberships
    
    AttributeDef attrDef = this.edu.addChildAttributeDef("test1", AttributeDefType.perm);

    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
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
    
    
    assertEquals("Should have 7 change log entries", 7, changeLogEntries.size());
    
    attrDef.getPrivilegeDelegate().revokePriv(newMember1.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    attrDef.getPrivilegeDelegate().revokePriv(newMember2.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    group.delete();
    attrDef.getPrivilegeDelegate().revokePriv(newMember3.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    ChangeLogTempToEntity.convertRecords();

    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    
    assertEquals("Should have 7 change log entries", 7, changeLogEntries.size());
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


    ChangeLogTempToEntity.convertRecords();

    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

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
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

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
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);

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
    
    assertEquals("Should have 0 new change log entries", 0, newChangeLogCount);

    
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
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

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
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);

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
    
    assertEquals("Should have 3 new change log entries", 3, newChangeLogCount);

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
    
    assertEquals("Should have 2 new change log entries", 2, newChangeLogCount);

    verifyEffectiveCustomListMembershipsDelete(g3g4Mship, g3g5Mship, changeLogEntry.getContextId());
    
    //##################################
    // try multiple immediate memberships
    
    Group group = this.edu.addChildGroup("test1", "test1");
    group.addType(groupType);

    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
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
    
    
    assertEquals("Should have 4 change log entries", 4, changeLogEntries.size());
    
    group.deleteMember(newMember1.getSubject(), customList);
    group.deleteMember(newMember2.getSubject(), customList);
    group2.delete();
    group.deleteMember(newMember3.getSubject(), customList);
    ChangeLogTempToEntity.convertRecords();

    changeLogEntries = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .list(ChangeLogEntry.class);
    
    
    assertEquals("Should have 4 change log entries", 4, changeLogEntries.size());
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

    
    ChangeLogTempToEntity.convertRecords();

    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have 3 new change log entries - 1 composite, 2 effective", 3, newChangeLogCount);


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


    //##################################
    // add immediate that causes composite
    
    g1.addCompositeMember(CompositeType.UNION, g2, g3);
    g2.deleteMember(member1.getSubject());

    ChangeLogTempToEntity.convertRecords();

    // clear changelog
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    g2.addMember(member1.getSubject());

    ChangeLogTempToEntity.convertRecords();

    newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_change_log_entry");
    
    assertEquals("Should have 4 new change log entries - 1 composite, 1 immediate, 2 effective", 4, newChangeLogCount);
    
    
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
    
    assertEquals("Should have 4 new change log entries - 1 composite, 1 immediate, 2 effective", 4, newChangeLogCount);

    
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

    immediate.setMember(member1);
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
    
    assertEquals("Should have 8 new change log entries - 2 composite, 2 immediate, 4 effective", 8, newChangeLogCount);

    
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
    assertEquals(FieldFinder.find(Field.FIELD_NAME_UPDATERS, true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId));
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
    assertEquals(FieldFinder.find(Field.FIELD_NAME_UPDATERS, true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId));
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
    assertEquals(FieldFinder.find(Field.FIELD_NAME_UPDATERS, true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.fieldId));
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
    assertEquals(FieldFinder.find(Field.FIELD_NAME_UPDATERS, true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.fieldId));
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
    assertEquals(FieldFinder.find(Field.FIELD_NAME_CREATORS, true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId));
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
    assertEquals(FieldFinder.find(Field.FIELD_NAME_CREATORS, true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId));
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
    assertEquals(FieldFinder.find(Field.FIELD_NAME_CREATORS, true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.fieldId));
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
    assertEquals(FieldFinder.find(Field.FIELD_NAME_CREATORS, true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.fieldId));
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
    
  }
  
  /**
   * @throws Exception
   */
  public void testRolePermissionAddAfterMembershipEnable() throws Exception {

    // initialize some data
    Role group = edu.addChildRole("testGroup", "testGroup");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group.addMember(newMember1.getSubject(), true);
    Membership membership = MembershipFinder.findImmediateMembership(grouperSession, (Group)group, newMember1.getSubject(), Group.getDefaultList(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName = edu.addChildAttributeDefName(attributeDef, "testAttribute", "testAttribute");
    ChangeLogTempToEntity.convertRecords();

    // assign permission
    group.getPermissionRoleDelegate().assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED).getAttributeAssign();
    
    //move the temp objects to the regular change log table
    ChangeLogTempToEntity.convertRecords();
    
    // now disable the membership
    membership.setEnabled(false);
    membership.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    membership.update();
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // now enable the membership and verify permission
    membership.setEnabled(true);
    membership.setEnabledTime(null);
    final Membership MEMBERSHIP = membership;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();
    
    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have two records in the change log table", 2, newChangeLogCount);
  
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    //make sure some time has passed
    GrouperUtil.sleep(100);
  
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime()  < 5000);
    assertTrue("This should have happened in the last 5 seconds: + " + changeLogEntry.getCreatedOn(), System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime() > 0);
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) < 5000);
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) > 0);
  
    assertNotNull(changeLogEntry.getSequenceNumber());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), membership.getContextId());
  
    assertEquals(group.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    //make sure some time has passed
    GrouperUtil.sleep(100);
  
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime()  < 5000);
    assertTrue("This should have happened in the last 5 seconds: + " + changeLogEntry.getCreatedOn(), System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime() > 0);
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) < 5000);
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) > 0);
  
    assertNotNull(changeLogEntry.getSequenceNumber());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), membership.getContextId());
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(newMember1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(newMember1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(membership.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(membership.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
  }
  
  /**
   * @throws Exception
   */
  public void testSubjectRolePermissionAddAfterMembershipEnable() throws Exception {

    // initialize some data
    Role group = edu.addChildRole("testGroup", "testGroup");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group.addMember(newMember1.getSubject(), true);
    Membership membership = MembershipFinder.findImmediateMembership(grouperSession, (Group)group, newMember1.getSubject(), Group.getDefaultList(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    AttributeDefName attributeDefName = edu.addChildAttributeDefName(attributeDef, "testAttribute", "testAttribute");
    ChangeLogTempToEntity.convertRecords();

    // assign permission
    group.getPermissionRoleDelegate().assignSubjectRolePermission(attributeDefName, newMember1, PermissionAllowed.ALLOWED).getAttributeAssign();

    //move the temp objects to the regular change log table
    ChangeLogTempToEntity.convertRecords();
    
    // now disable the membership
    membership.setEnabled(false);
    membership.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    membership.update();
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // now enable the membership and verify flattened membership and permission
    membership.setEnabled(true);
    membership.setEnabledTime(null);
    final Membership MEMBERSHIP = membership;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP);

        return null;
      }
    });
    
    ChangeLogTempToEntity.convertRecords();
    
    int newChangeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    int newChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
  
    assertEquals("Should have nothing in temp table", 0, newChangeLogTempCount);
    assertEquals("Should have two records in the change log table", 2, newChangeLogCount);
  
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
  
    //make sure some time has passed
    GrouperUtil.sleep(100);
  
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime()  < 5000);
    assertTrue("This should have happened in the last 5 seconds: + " + changeLogEntry.getCreatedOn(), System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime() > 0);
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) < 5000);
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) > 0);
  
    assertNotNull(changeLogEntry.getSequenceNumber());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), membership.getContextId());
      
    assertEquals(group.getId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId));
    assertEquals(group.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName));

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    
    //make sure some time has passed
    GrouperUtil.sleep(100);
  
    assertNotNull("createdOn should exist", changeLogEntry.getCreatedOn());
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime()  < 5000);
    assertTrue("This should have happened in the last 5 seconds: + " + changeLogEntry.getCreatedOn(), System.currentTimeMillis() - changeLogEntry.getCreatedOn().getTime() > 0);
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) < 5000);
    assertTrue("This should have happened in the last 5 seconds", System.currentTimeMillis() - (changeLogEntry.getCreatedOnDb() / 1000) > 0);
  
    assertNotNull(changeLogEntry.getSequenceNumber());
    assertEquals("Context id's should match", changeLogEntry.getContextId(), membership.getContextId());
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(newMember1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(newMember1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(membership.getGroup().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(membership.getGroup().getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
  }
  
  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession = null;
  
  /** root stem */
  private Stem root;

}
