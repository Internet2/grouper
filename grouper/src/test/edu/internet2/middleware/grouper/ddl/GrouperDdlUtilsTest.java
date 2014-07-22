/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id: GrouperDdlUtilsTest.java,v 1.22 2009-11-14 16:44:01 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils.DbMetadataBean;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
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
   * @param args
   */
  public static void main(String[] args) {
    GrouperTest.setupTests();
    //TestRunner.run(GrouperDdlUtilsTest.class);
    TestRunner.run(new GrouperDdlUtilsTest("testIdUpgrade"));
  }

  /**
   * test
   */
  public void findDdlMetadataBean() {
    //make sure we can find the ddl metadata bean
    DbMetadataBean dbMetadataBean = GrouperDdlUtils.findDbMetadataBean(GrouperDdl.V1);
    assertNotNull(dbMetadataBean);
    dbMetadataBean = GrouperDdlUtils.findDbMetadataBean(SubjectDdl.V1);
    assertNotNull(dbMetadataBean);
    
  }
  
  /**
   * 
   */
  public void testBootstrapHelper() {
    GrouperDdlUtils.justTesting = true;

    try {
      assertTrue("Starting out, tables should be there", GrouperDdlUtils.assertTablesThere(false, true));
      
      //now lets remove all tables and object
      GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, true, false, null, false);
      
      assertFalse("Just removed tables, shouldnt be there", GrouperDdlUtils.assertTablesThere(false, false));
  
      //lets add all tables and object
      GrouperDdlUtils.bootstrapHelper(false, true, false, false, true, false, true, null, false);
      
      //if we init data, the root stem should be there...
      assertTrue("Just added all tables, and registry init, it should be there", 
          GrouperDdlUtils.assertTablesThere(true, true));
  
      //should also have at least two rows in ddl
      int count = HibernateSession.bySqlStatic().select(int.class, 
          "select count(*) from grouper_ddl");
      assertTrue("Count should be more than 1 since Grouper and Subject " +
      		"should be there " + count, count > 1);
      
      //try again, everything should be there (even not from junit)
      GrouperDdlUtils.bootstrapHelper(false, false, true, false, false, false, false, null, false);
      
      assertTrue("Should not change anything", GrouperDdlUtils.assertTablesThere(true, true));
  
      //at this point, hibernate should not be shut off
      assertTrue("at this point, hibernate should not be shut off", 
          GrouperDdlUtils.okToUseHibernate());
    } finally {
      GrouperDdlUtils.justTesting = false;
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    //dont print annoying messages to user
    GrouperDdlUtils.internal_printDdlUpdateMessage = false;
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void tearDown() {
    //yes print annoying messages to user again
    GrouperDdlUtils.internal_printDdlUpdateMessage = true;
  }

  /**
   * @throws Exception 
   * @throws SchemaException 
   */
  public void testIdUpgrade() throws Exception {
    
    ////doesnt work on this db
    ////TODO MCH 20090202 make this work for postgres... what is the problem?
    //if (GrouperDdlUtils.isHsql()) {
    //  return;
    //}
    //
    ////lets get the first version
    //GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, false, false, 
    //    GrouperDdlUtils.maxVersionMap(GrouperDdl.V1), false);
    //
    //GrouperDdlUtils.justTesting = true;
    //
    ////now we should have the ddl table...
    //GrouperDdlUtils.assertTablesThere(true, true, "grouper_ddl");
    ////but no other tables
    //GrouperDdlUtils.assertTablesThere(false, false);
    //
    ////get up to v4...  note if cols are added, they should be added pre-v4 also...
    //GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, false, true, 
    //    GrouperDdlUtils.maxVersionMap(GrouperDdl.V4), false);
    ////auto-init wheel group
    //GrouperCheckConfig.checkGroups();
    //
    ////make sure uuid is there...
    //HibernateSession.bySqlStatic().select(int.class, 
    //  "select count(*) from grouper_groups where uuid is not null");
    //
    ////now we should have the ddl table of course...
    //GrouperDdlUtils.assertTablesThere(true, true, "grouper_ddl");
    ////and all other tables
    //GrouperDdlUtils.assertTablesThere(false, true);
    //
    ////add a group, type, stem, member, etc.
    //super.setUp();
    //
    //RegistryReset.internal_resetRegistryAndAddTestSubjects();
    //GrouperTest.initGroupsAndAttributes();
    //
    //GrouperSession grouperSession = SessionHelper.getRootSession();
    //Stem root = StemHelper.findRootStem(grouperSession);
    //Stem edu = StemHelper.addChildStem(root, "edu", "education");
    //Group groupq = StemHelper.addChildGroup(edu, "testq", "the testq");
    //Group groupr = StemHelper.addChildGroup(edu, "testr", "the testr");
    //Group groups = StemHelper.addChildGroup(edu, "tests", "the tests");
    //Privilege read = AccessPrivilege.READ;
    //Privilege write = AccessPrivilege.UPDATE;
    //GroupType groupType = GroupType.createType(grouperSession, "testType");    
    //Field field = groupType.addAttribute(grouperSession, "test1", read, write, true);
    //groups.addType(groupType);
    //groups.setAttribute(field.getName(), "whatever");
    //groups.addMember(SubjectTestHelper.SUBJ0);
    //groupq.addCompositeMember(CompositeType.UNION, groupr, groups);
    //
    ////hibernate is set to the new way, so the uuid cols will be blank... copy them over
    //HibernateSession.bySqlStatic().executeSql("update grouper_composites set uuid = id");
    //HibernateSession.bySqlStatic().executeSql("update grouper_fields set field_uuid = id");
    //HibernateSession.bySqlStatic().executeSql("update grouper_groups set uuid = id");
    //HibernateSession.bySqlStatic().executeSql("update grouper_members set member_uuid = id");
    //HibernateSession.bySqlStatic().executeSql("update grouper_stems set uuid = id");
    //HibernateSession.bySqlStatic().executeSql("update grouper_types set type_uuid = id");
    //
    ////now convert the data
    //GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ddlutils.dropBackupUuidCols", "false");
    //GrouperDdlUtils.bootstrapHelper(false, true, false, false, true, false, false, null, false);
    //
    ////that should have created backup cols
    //int count = HibernateSession.bySqlStatic().select(int.class, 
    //    "select count(*) from grouper_groups where old_uuid is not null");
    //assertTrue("should have data: " + count, count > 0);
    //
    ////should have deleted existing cols
    //try {
    //  HibernateSession.bySqlStatic().select(int.class, 
    //  "select count(*) from grouper_groups where uuid is not null");
    //  fail("This column should not be there anymore");
    //} catch (Exception e) {
    //  //good
    //}
    //
    //StemFinder.findByName(grouperSession, "edu", true);
    //groupq = GroupFinder.findByName(grouperSession, "edu:testq", true);
    //groupq.hasMember(SubjectTestHelper.SUBJ0);
    //assertEquals("edu:testr", groupq.getComposite(true).getLeftGroup().getName());
    //groups = GroupFinder.findByName(grouperSession, "edu:tests", true);
    //assertEquals("whatever", groups.getAttributeValue("test1", false, true));
    //
    ////now delete the uuid cols
    //GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ddlutils.dropBackupUuidCols", "true");
    //GrouperDdlUtils.bootstrapHelper(false, true, false, false, true, false, false, null, false);
    //
    //try {
    //  count = HibernateSession.bySqlStatic().select(int.class, 
    //    "select count(*) from grouper_groups where old_uuid is not null");
    //  fail("this col shouldnt be there anymore");
    //} catch (Exception e) {
    //  //this is good
    //}
    //
    ////make sure data is still there
    //StemFinder.findByName(grouperSession, "edu", true);
    //groupq = GroupFinder.findByName(grouperSession, "edu:testq", true);
    //groupq.hasMember(SubjectTestHelper.SUBJ0);
    //assertEquals("edu:testr", groupq.getComposite(true).getLeftGroup().getName());
    //groups = GroupFinder.findByName(grouperSession, "edu:tests", true);
    //assertEquals("whatever", groups.getAttributeValue("test1", false, true));
    //
    ////get ready for final test from scratch...
    //GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ddlutils.dropBackupUuidCols");
    //GrouperDdlUtils.everythingRightVersion = true;
    //GrouperDdlUtils.justTesting = false;
    //
    //GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, false, true, null, false);
    //
    ////at this point, hibernate should not be shut off
    //assertTrue("at this point, hibernate should not be shut off", GrouperDdlUtils.okToUseHibernate());
    
  }

  /**
   * @throws Exception 
   * @throws SchemaException 
   */
  public void testGroupAttributeUpgrade() throws Exception {
    
    //if (GrouperDdlUtils.isHsql()) {
    //  return;
    //}
    //
    //if (GrouperDdlUtils.tableExists(GrouperDdl.BAK_GROUPER_ATTRIBUTES)) {
    //  GrouperDdlUtils.changeDatabase(GrouperDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    //
    //    public void changeDatabase(DdlVersionBean ddlVersionBean) {
    //      GrouperDdlUtils.ddlutilsDropTable(ddlVersionBean, GrouperDdl.BAK_GROUPER_ATTRIBUTES);
    //    }
    //  });
    //}
    //
    //GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ddlutils.dropAttributeBackupTableFromGroupUpgrade", "false");
    //
    ////lets get the first version
    //GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, false, false, 
    //    GrouperDdlUtils.maxVersionMap(GrouperDdl.V1), false);
    //
    //GrouperDdlUtils.justTesting = true;
    //
    ////now we should have the ddl table...
    //GrouperDdlUtils.assertTablesThere(true, true, "grouper_ddl");
    ////but no other tables
    //GrouperDdlUtils.assertTablesThere(false, false);
    //
    ////get up to v12...  note if cols are added, they should be added pre-v12 also...
    //GrouperDdl.addGroupNameColumns = false;
    //
    //try {
    //  GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, false, false, 
    //      GrouperDdlUtils.maxVersionMap(GrouperDdl.V13), false);
    //}finally {
    //  GrouperDdl.addGroupNameColumns = true;
    //}
    //
    ////make sure grouper_groups.name is not there...
    //try {
    //  HibernateSession.bySqlStatic().select(int.class, 
    //    "select count(*) from grouper_groups where name is not null");
    //  fail("name should not be there");
    //} catch (Exception e) {
    //  //good
    //}
    //
    ////now we should have the ddl table of course...
    //GrouperDdlUtils.assertTablesThere(true, true, "grouper_ddl");
    ////and all other tables
    //GrouperDdlUtils.assertTablesThere(false, true);
    //
    //boolean hasBackupTable = GrouperDdlUtils.tableExists(GrouperDdl.BAK_GROUPER_ATTRIBUTES);
    //assertFalse("should have no backup table", hasBackupTable);
    //
    ////do the last step
    //GrouperDdlUtils.bootstrapHelper(false, true, true, false, true, false, true, null, false);
    //
    //hasBackupTable = GrouperDdlUtils.tableExists(GrouperDdl.BAK_GROUPER_ATTRIBUTES);
    //assertTrue("should have backup table", hasBackupTable);
    //
    //
    ////put all data in there
    ////add a group, type, stem, member, etc.
    //super.setUp();
    //
    //RegistryReset.internal_resetRegistryAndAddTestSubjects();
    //GrouperTest.initGroupsAndAttributes();
    //
    //
    //GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ddlutils.dropAttributeBackupTableFromGroupUpgrade", "true");
    //
    //GrouperDdlUtils.bootstrapHelper(false, true, false, false, true, false, true, null, false);
    //
    //hasBackupTable = GrouperDdlUtils.tableExists(GrouperDdl.BAK_GROUPER_ATTRIBUTES);
    //assertFalse("should not have backup table", hasBackupTable);
    //
    //GrouperDdlUtils.everythingRightVersion = true;
    //GrouperDdlUtils.justTesting = false;
    //
    ////at this point, hibernate should not be shut off
    //assertTrue("at this point, hibernate should not be shut off", GrouperDdlUtils.okToUseHibernate());
    //
    ////remove the backup table
    //GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ddlutils.dropAttributeBackupTableFromGroupUpgrade");
  
  }

  /**
   * @throws Exception 
   * @throws SchemaException 
   */
  public void testFieldIdUpgrade() throws Exception {
    
    ////doesnt work on this db
    ////TODO MCH 20090202 make this work for postgres... what is the problem?
    //if (GrouperDdlUtils.isHsql()) {
    //  return;
    //}
    //
    ////lets get the first version
    //GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, false, false, 
    //    GrouperDdlUtils.maxVersionMap(GrouperDdl.V1), false);
    //
    //GrouperDdlUtils.justTesting = true;
    //
    ////now we should have the ddl table...
    //GrouperDdlUtils.assertTablesThere(true, true, "grouper_ddl");
    ////but no other tables
    //GrouperDdlUtils.assertTablesThere(false, false);
    //
    ////get up to v4...  note if cols are added, they should be added pre-v4 also...
    //GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, false, true, 
    //    GrouperDdlUtils.maxVersionMap(GrouperDdl.V4), false);
    //
    ////make sure attribute name, list_type, list_name is there...
    //HibernateSession.bySqlStatic().select(int.class, 
    //  "select count(*) from grouper_attributes where field_name is not null");
    //HibernateSession.bySqlStatic().select(int.class, 
    //  "select count(*) from grouper_memberships where list_name is not null");
    //HibernateSession.bySqlStatic().select(int.class, 
    //  "select count(*) from grouper_memberships where list_type is not null");
    //
    ////backups should not be there
    //try {
    //  HibernateSession.bySqlStatic().select(int.class, 
    //    "select count(*) from grouper_attributes where old_field_name is not null");
    //  fail("backups should not be there");
    //} catch (Exception e) {
    //  //good
    //}
    //try {
    //  HibernateSession.bySqlStatic().select(int.class, 
    //    "select count(*) from grouper_memberships where old_list_name is not null");
    //  fail("backups should not be there");
    //} catch (Exception e) {
    //  //good
    //}
    //try {
    //  HibernateSession.bySqlStatic().select(int.class, 
    //    "select count(*) from grouper_memberships where old_list_type is not null");
    //  fail("backups should not be there");
    //} catch (Exception e) {
    //  //good
    //}
    //
    ////now we should have the ddl table of course...
    //GrouperDdlUtils.assertTablesThere(true, true, "grouper_ddl");
    ////and all other tables
    //GrouperDdlUtils.assertTablesThere(false, true);
    //
    ////add a group, type, stem, member, etc.
    //super.setUp();
    //
    //RegistryReset.internal_resetRegistryAndAddTestSubjects();
    //GrouperTest.initGroupsAndAttributes();
    //
    //GrouperSession grouperSession = SessionHelper.getRootSession();
    //Stem root = StemHelper.findRootStem(grouperSession);
    //Stem edu = StemHelper.addChildStem(root, "edu", "education");
    //Group groupq = StemHelper.addChildGroup(edu, "testq", "the testq");
    //Group groupr = StemHelper.addChildGroup(edu, "testr", "the testr");
    //Group groups = StemHelper.addChildGroup(edu, "tests", "the tests");
    //Privilege read = AccessPrivilege.READ;
    //Privilege write = AccessPrivilege.UPDATE;
    //GroupType groupType = GroupType.createType(grouperSession, "testType");    
    //Field field = groupType.addAttribute(grouperSession, "test1", read, write, true);
    //groups.addType(groupType);
    //groups.setAttribute(field.getName(), "whatever");
    //groups.addMember(SubjectTestHelper.SUBJ0);
    //groupq.addCompositeMember(CompositeType.UNION, groupr, groups);
    //
    ////now we need to move the data from the fieldId to the attribute name etc, and drop the field id cols...
    ////loop through all fields:
    //List<Field> fields = HibernateSession.byCriteriaStatic().list(Field.class, null);
    //
    //for (Field theField : fields) {
    //  
    //  //attributes work on the attributes table, and non-attributes work on the memberships table
    //  if (theField.isAttributeName()) {
    //    
    //    //update records, move the name to the id, commit inline so that the db undo required is not too huge
    //    HibernateSession.bySqlStatic().executeSql("update grouper_attributes set " +
    //    		"field_name = '" + theField.getName() + "' where field_id = '" + theField.getUuid() + "'");
    //
    //  } else {
    //    
    //    //update records, move the name to the id, commit inline so that the db undo required is not too huge
    //    HibernateSession.bySqlStatic().executeSql("update grouper_memberships set " +
    //    		"list_name = '" + theField.getName() + "', list_type = '" + theField.getTypeString() + "'" +
    //    				" where field_id = '" + theField.getUuid() + "'");
    //    
    //  }
    //  
    //}
    //
    ////drop field id col, first drop foreign keys
    //GrouperDdlUtils.changeDatabase(GrouperDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    //
    //  public void changeDatabase(DdlVersionBean ddlVersionBean) {
    //    
    //    Database database = ddlVersionBean.getDatabase();
    //    {
    //      Table attributesTable = database.findTable(Attribute.TABLE_GROUPER_ATTRIBUTES);
    //      GrouperDdlUtils.ddlutilsDropColumn(attributesTable, Attribute.COLUMN_FIELD_ID, ddlVersionBean);
    //    }
    //    
    //    {
    //      Table membershipsTable = database.findTable(Membership.TABLE_GROUPER_MEMBERSHIPS);
    //      GrouperDdlUtils.ddlutilsDropColumn(membershipsTable, Membership.COLUMN_FIELD_ID, ddlVersionBean);
    //    }
    //    //set version back for foreign keys
    //    ddlVersionBean.setBuildingToVersion(GrouperDdl.V3.getVersion());
    //  }
    //  
    //});
    //
    ////now convert the data
    //GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ddlutils.dropBackupFieldNameTypeCols", "false");
    //GrouperDdlUtils.bootstrapHelper(false, true, false, false, true, false, false, null, false);
    //
    ////that should have created backup cols
    //int count = HibernateSession.bySqlStatic().select(int.class, 
    //    "select count(*) from grouper_attributes where old_field_name is not null");
    //assertTrue("should have data: " + count, count > 0);
    //count = HibernateSession.bySqlStatic().select(int.class, 
    //  "select count(*) from grouper_memberships where old_list_type is not null");
    //    assertTrue("should have data: " + count, count > 0);
    //count = HibernateSession.bySqlStatic().select(int.class, 
    //  "select count(*) from grouper_memberships where old_list_name is not null");
    //assertTrue("should have data: " + count, count > 0);
    //
    ////should have deleted existing cols
    //try {
    //  HibernateSession.bySqlStatic().select(int.class, 
    //    "select count(*) from grouper_attributes where field_name is not null");
    //  fail("This column should not be there anymore");
    //} catch (Exception e) {
    //  //good
    //}
    //try {
    //  HibernateSession.bySqlStatic().select(int.class, 
    //    "select count(*) from grouper_memberships where list_name is not null");
    //  fail("This column should not be there anymore");
    //} catch (Exception e) {
    //  //good
    //}
    //try {
    //  HibernateSession.bySqlStatic().select(int.class, 
    //    "select count(*) from grouper_memberships where list_type is not null");
    //  fail("This column should not be there anymore");
    //} catch (Exception e) {
    //  //good
    //}
    //
    //StemFinder.findByName(grouperSession, "edu", true);
    //groupq = GroupFinder.findByName(grouperSession, "edu:testq", true);
    //groupq.hasMember(SubjectTestHelper.SUBJ0);
    //assertEquals("edu:testr", groupq.getComposite(true).getLeftGroup().getName());
    //groups = GroupFinder.findByName(grouperSession, "edu:tests", true);
    //assertEquals("whatever", groups.getAttributeValue("test1", false, true));
    //
    ////now delete the uuid cols
    //GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ddlutils.dropBackupFieldNameTypeCols", "true");
    //GrouperDdlUtils.bootstrapHelper(false, true, false, false, true, false, false, null, false);
    //
    //try {
    //  count = HibernateSession.bySqlStatic().select(int.class, 
    //    "select count(*) from grouper_attributes where old_field_name is not null");
    //  fail("this col shouldnt be there anymore");
    //} catch (Exception e) {
    //  //this is good
    //}
    //
    ////make sure data is still there
    //StemFinder.findByName(grouperSession, "edu", true);
    //groupq = GroupFinder.findByName(grouperSession, "edu:testq", true);
    //groupq.hasMember(SubjectTestHelper.SUBJ0);
    //assertEquals("edu:testr", groupq.getComposite(true).getLeftGroup().getName());
    //groups = GroupFinder.findByName(grouperSession, "edu:tests", true);
    //assertEquals("whatever", groups.getAttributeValue("test1", false, true));
    //
    ////get ready for final test from scratch...
    //GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ddlutils.dropBackupFieldNameTypeCols");
    //GrouperDdlUtils.everythingRightVersion = true;
    //GrouperDdlUtils.justTesting = false;
    //
    //GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, false, true, null, false);
    //
    //try {
    //  count = HibernateSession.bySqlStatic().select(int.class, 
    //    "select count(*) from grouper_attributes where old_field_name is not null");
    //  fail("this col shouldnt be there anymore");
    //} catch (Exception e) {
    //  //this is good
    //}
    //
    ////at this point, hibernate should not be shut off
    //assertTrue("at this point, hibernate should not be shut off", GrouperDdlUtils.okToUseHibernate());
    
  }

  /**
   * @throws Exception 
   * @throws SchemaException 
   */
  public void testGrouperSessionDrop() throws Exception {
    
    ////doesnt work on this db
    //if (GrouperDdlUtils.isHsql()) {
    //  return;
    //}
    //
    ////lets get the first version
    //GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, false, false, 
    //    GrouperDdlUtils.maxVersionMap(GrouperDdl.V1), false);
    //
    //GrouperDdlUtils.justTesting = true;
    //
    ////now we should have the ddl table...
    //GrouperDdlUtils.assertTablesThere(true, true, "grouper_ddl");
    ////but has other tables
    //GrouperDdlUtils.assertTablesThere(false, false);
    //
    ////get up to v4...  note grouper_sessions will be added...
    //GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, false, true, 
    //    GrouperDdlUtils.maxVersionMap(GrouperDdl.V4), false);
    //
    ////now we should have the grouper_sessions table of course...
    //GrouperDdlUtils.assertTablesThere(false, true, "grouper_sessions");
    ////but no other tables
    //GrouperDdlUtils.assertTablesThere(false, true);
    //
    ////add a group, type, stem, member, etc.
    //super.setUp();
    //
    //GrouperDdlUtils.bootstrapHelper(false, true, false, false, true, false, false, null, false);
    //
    ////now we should not have the grouper_sessions table of course...
    //GrouperDdlUtils.assertTablesThere(false, false, "grouper_sessions");
    ////but has other tables
    //GrouperDdlUtils.assertTablesThere(false, true);
    //
    ////that should have dropped grouper_sessions
    //GrouperDdlUtils.everythingRightVersion = true;
    //GrouperDdlUtils.justTesting = false;
    //
    //GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, false, true, null, false);
    //
    ////at this point, hibernate should not be shut off
    //assertTrue("at this point, hibernate should not be shut off", GrouperDdlUtils.okToUseHibernate());
    
  }
  
}
