/*
 * @author mchyzer
 * $Id: GrouperDdlUtilsTest.java,v 1.2 2008-07-28 20:12:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SessionHelper;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemHelper;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;


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
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(GrouperDdlUtilsTest.class);
    TestRunner.run(new GrouperDdlUtilsTest("testIdUpgrade"));
  }

  /**
   * see if tables are there (at least the grouper groups one)
   * @param expectRecords 
   * @param expectTrue pritn exception if expecting true
   * @return true if everything ok, false if not
   */
  private static boolean assertTablesThere(boolean expectRecords, boolean expectTrue) {
    return assertTablesThere(expectRecords, expectTrue, "grouper_stems");
  }
  
  /**
   * see if tables are there (at least the grouper groups one)
   * @param expectRecords 
   * @param expectTrue pritn exception if expecting true
   * @param tableName 
   * @return true if everything ok, false if not
   */
  private static boolean assertTablesThere(boolean expectRecords, boolean expectTrue, String tableName) {
    try {
      //first, see if tables are there
      int count = HibernateSession.bySqlStatic().select(int.class, 
          "select count(*) from " + tableName);
      if (!expectRecords) {
        return true;
      }
      return count > 0;
    } catch (RuntimeException e) {
      if (expectTrue) {
        throw e;
      }
      return false;
    }

  }

  /**
   * 
   */
  public void testBootstrapHelper() {
    
    assertTrue("Starting out, tables should be there", assertTablesThere(false, true));
    
    //now lets remove all tables and object
    GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, true, false, null);
    
    assertFalse("Just removed tables, shouldnt be there", assertTablesThere(false, false));

    //lets add all tables and object
    GrouperDdlUtils.bootstrapHelper(false, true, false, false, true, false, true, null);
    
    //if we init data, the root stem should be there...
    assertTrue("Just added all tables, and registry init, it should be there", 
        assertTablesThere(true, true));

    //should also have at least two rows in ddl
    int count = HibernateSession.bySqlStatic().select(int.class, 
        "select count(*) from grouper_ddl");
    assertTrue("Count should be more than 1 since Grouper and Subject " +
    		"should be there " + count, count > 1);
    
    //try again, everything should be there (even not from junit)
    GrouperDdlUtils.bootstrapHelper(false, false, true, false, false, false, false, null);
    
    assertTrue("Should not change anything", assertTablesThere(true, true));

    //at this point, hibernate should not be shut off
    assertTrue("at this point, hibernate should not be shut off", 
        GrouperDdlUtils.okToUseHibernate());
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
  }

  /**
   * @throws Exception 
   * @throws SchemaException 
   */
  public void testIdUpgrade() throws Exception {
    
    //lets get the first version
    @SuppressWarnings("unused")
    String script = GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, false, false, 
        GrouperDdlUtils.maxVersionMap(GrouperDdl.V1));

    GrouperDdlUtils.justTesting = true;
    
    //now we should have the ddl table...
    assertTablesThere(true, true, "grouper_ddl");
    //but no other tables
    assertTablesThere(false, false);

    //get up to v4...  note if cols are added, they should be added pre-v4 also...
    GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, false, true, 
        GrouperDdlUtils.maxVersionMap(GrouperDdl.V4));
    
    //make sure uuid is there...
    HibernateSession.bySqlStatic().select(int.class, 
      "select count(*) from grouper_groups where uuid is not null");
    
    //now we should have the ddl table of course...
    assertTablesThere(true, true, "grouper_ddl");
    //but no other tables
    assertTablesThere(false, true);

    //add a group, type, stem, member, etc.
    super.setUp();
    
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Stem root = StemHelper.findRootStem(grouperSession);
    Stem edu = StemHelper.addChildStem(root, "edu", "education");
    Group groupq = StemHelper.addChildGroup(edu, "testq", "the testq");
    Group groupr = StemHelper.addChildGroup(edu, "testr", "the testr");
    Group groups = StemHelper.addChildGroup(edu, "tests", "the tests");
    Privilege read = AccessPrivilege.READ;
    Privilege write = AccessPrivilege.UPDATE;
    GroupType groupType = GroupType.createType(grouperSession, "testType");    
    Field field = groupType.addAttribute(grouperSession, "test1", read, write, true);
    groups.addType(groupType);
    groups.setAttribute(field.getName(), "whatever");
    groups.addMember(SubjectTestHelper.SUBJ0);
    groupq.addCompositeMember(CompositeType.UNION, groupr, groups);
    
    //hibernate is set to the new way, so the uuid cols will be blank... copy them over
    HibernateSession.bySqlStatic().executeSql("update grouper_composites set uuid = id");
    HibernateSession.bySqlStatic().executeSql("update grouper_fields set field_uuid = id");
    HibernateSession.bySqlStatic().executeSql("update grouper_groups set uuid = id");
    HibernateSession.bySqlStatic().executeSql("update grouper_members set member_uuid = id");
    HibernateSession.bySqlStatic().executeSql("update grouper_sessions set session_uuid = id");
    HibernateSession.bySqlStatic().executeSql("update grouper_stems set uuid = id");
    HibernateSession.bySqlStatic().executeSql("update grouper_types set type_uuid = id");
    
    //now convert the data
    ApiConfig.testConfig.put("ddlutils.dropUuidCols", "false");
    GrouperDdlUtils.bootstrapHelper(false, true, false, false, true, false, false, null);
    
    //that should have created backup cols
    int count = HibernateSession.bySqlStatic().select(int.class, 
        "select count(*) from grouper_groups where old_uuid is not null");
    assertTrue("should have data: " + count, count > 0);
    
    //should not have deleted existing cols
    count = HibernateSession.bySqlStatic().select(int.class, 
      "select count(*) from grouper_groups where uuid is not null");
    assertTrue("should have data: " + count, count > 0);
    
    StemFinder.findByName(grouperSession, "edu");
    groupq = GroupFinder.findByName(grouperSession, "edu:testq");
    groupq.hasMember(SubjectTestHelper.SUBJ0);
    assertEquals("edu:testr", groupq.getComposite().getLeftGroup().getName());
    groups = GroupFinder.findByName(grouperSession, "edu:tests");
    assertEquals("whatever", groups.getAttribute("test1"));
    
    //now delete the uuid cols
    ApiConfig.testConfig.put("ddlutils.dropUuidCols", "true");
    GrouperDdlUtils.bootstrapHelper(false, true, false, false, true, false, false, null);
    
    try {
      count = HibernateSession.bySqlStatic().select(int.class, 
        "select count(*) from grouper_groups where uuid is not null");
      fail("this col shouldnt be there anymore");
    } catch (Exception e) {
      //this is good
    }
    
    //make sure data is still there
    StemFinder.findByName(grouperSession, "edu");
    groupq = GroupFinder.findByName(grouperSession, "edu:testq");
    groupq.hasMember(SubjectTestHelper.SUBJ0);
    assertEquals("edu:testr", groupq.getComposite().getLeftGroup().getName());
    groups = GroupFinder.findByName(grouperSession, "edu:tests");
    assertEquals("whatever", groups.getAttribute("test1"));
    
    //get ready for final test from scratch...
    ApiConfig.testConfig.remove("ddlutils.dropUuidCols");
    GrouperDdlUtils.everythingRightVersion = true;
    GrouperDdlUtils.justTesting = false;

    GrouperDdlUtils.bootstrapHelper(false, true, false, true, true, false, true, null);
    
    //at this point, hibernate should not be shut off
    assertTrue("at this point, hibernate should not be shut off", GrouperDdlUtils.okToUseHibernate());
    
  }
  
}
