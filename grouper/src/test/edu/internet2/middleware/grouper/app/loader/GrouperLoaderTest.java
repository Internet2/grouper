/*
 * @author mchyzer
 * $Id: GrouperLoaderTest.java,v 1.3 2008-11-13 05:46:23 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import junit.textui.TestRunner;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * 
 */
public class GrouperLoaderTest extends GrouperTest {

  /**
   * @param name
   */
  public GrouperLoaderTest(String name) {
    super(name);
  }

  /**
   * grouper session
   */
  private GrouperSession grouperSession = null;
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperLoaderTest("testLoaderTypes"));
  }

  /**
   * test the loader
   * @throws Exception 
   */
  public void testLoaderTypes() throws Exception {
    
    List<TestgrouperLoader> testDataList = new ArrayList<TestgrouperLoader>();
    
    TestgrouperLoader group1subj0 = new TestgrouperLoader("loader:group1_systemOfRecord", SubjectTestHelper.SUBJ0_ID, null);
    testDataList.add(group1subj0);
    TestgrouperLoader group1subj1 = new TestgrouperLoader("loader:group1_systemOfRecord", SubjectTestHelper.SUBJ1_ID, null);
    testDataList.add(group1subj1);
    TestgrouperLoader group2subj1 = new TestgrouperLoader("loader:group2_systemOfRecord", SubjectTestHelper.SUBJ1_ID, null);
    testDataList.add(group2subj1);
    TestgrouperLoader group2subj2 = new TestgrouperLoader("loader:group2_systemOfRecord", SubjectTestHelper.SUBJ2_ID, null);
    testDataList.add(group2subj2);
    TestgrouperLoader group3subj2 = new TestgrouperLoader("loader:group3_systemOfRecord", SubjectTestHelper.SUBJ2_ID, null);
    testDataList.add(group3subj2);
    TestgrouperLoader group3subj3 = new TestgrouperLoader("loader:group3_systemOfRecord", SubjectTestHelper.SUBJ3_ID, null);
    testDataList.add(group3subj3);
    TestgrouperLoader group4subj3 = new TestgrouperLoader("loader:group4_systemOfRecord", SubjectTestHelper.SUBJ3_ID, null);
    testDataList.add(group4subj3);
    TestgrouperLoader group4subj4 = new TestgrouperLoader("loader:group4_systemOfRecord", SubjectTestHelper.SUBJ4_ID, null);
    testDataList.add(group4subj4);
    TestgrouperLoader group6subj5 = new TestgrouperLoader("loader:group6_systemOfRecord", SubjectTestHelper.SUBJ5_ID, null);
    testDataList.add(group6subj5);
    TestgrouperLoader group6subj6 = new TestgrouperLoader("loader:group6_systemOfRecord", SubjectTestHelper.SUBJ6_ID, null);
    testDataList.add(group6subj6);

    HibernateSession.byObjectStatic().saveOrUpdate(testDataList);

    //lets add a group which will load these
    Group loaderGroup = Group.saveGroup(this.grouperSession, null, null, 
        "loader:owner",null, null, null, true);
    loaderGroup.addType(GroupTypeFinder.find("grouperLoader"));
    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, 
        "select col1 as GROUP_NAME, col2 as SUBJECT_ID from testgrouper_loader");
    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUP_TYPES,
        "addIncludeExclude");
    
    GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup);
    
    Group overallGroup1 = GroupFinder.findByName(this.grouperSession, "loader:group1");
    assertTrue(overallGroup1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup1.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ6));
    
    Group overallGroup2 = GroupFinder.findByName(this.grouperSession, "loader:group2");
    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup2.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup2.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup2.hasMember(SubjectTestHelper.SUBJ6));
    
    Group overallGroup3 = GroupFinder.findByName(this.grouperSession, "loader:group3");
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup3.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup3.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ6));

    Group overallGroup4 = GroupFinder.findByName(this.grouperSession, "loader:group4");
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup4.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup4.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ6));

    Group overallGroup5 = GroupFinder.findByName(this.grouperSession, "loader:group5", false);
    assertNull(overallGroup5);
    
    //lets use the includes/excludes for group6
    Group group6includes = GroupFinder.findByName(this.grouperSession, "loader:group6_includes");
    group6includes.addMember(SubjectTestHelper.SUBJ9);

    Group overallGroup6 = GroupFinder.findByName(this.grouperSession, "loader:group6");
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup6.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(overallGroup6.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(overallGroup6.hasMember(SubjectTestHelper.SUBJ6));
    assertTrue(overallGroup6.hasMember(SubjectTestHelper.SUBJ9));

    
    //#########################################################
    //change around the groups for another run
    
    // now lets use a group in another group
    Group anotherGroup = Group.saveGroup(this.grouperSession, "aStem:anotherGroup", null, 
        "aStem:anotherGroup", null, null, null, true);
    anotherGroup.addMember(SubjectFinder.findById(overallGroup1.getUuid()));
    
    //now lets change around the memberships...
    //delete group1, group2, and group3subj2
    HibernateSession.byObjectStatic().delete(GrouperUtil.toList(group1subj0, 
        group1subj1, group2subj1, group2subj2, group3subj2, group6subj5, group6subj6));
    
    //add group3subj4
    TestgrouperLoader group3subj4 = new TestgrouperLoader(
        "loader:group3_systemOfRecord", SubjectTestHelper.SUBJ4_ID, null);
    
    //add group5subj4, group5subj5
    TestgrouperLoader group5subj4 = new TestgrouperLoader(
        "loader:group5_systemOfRecord", SubjectTestHelper.SUBJ4_ID, null);
    TestgrouperLoader group5subj5 = new TestgrouperLoader(
        "loader:group5_systemOfRecord", SubjectTestHelper.SUBJ5_ID, null);
    
    HibernateSession.byObjectStatic().saveOrUpdate(GrouperUtil.toList(group3subj4, group5subj4, group5subj5));

    GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup);

    //###################################################################
    //we didnt add the attribute yet, so nothing should work yet
    
    //group1 is used in another group, so it should exist, with no members
    overallGroup1 = GroupFinder.findByName(this.grouperSession, "loader:group1");
    assertTrue(overallGroup1.hasMember(SubjectTestHelper.SUBJ0));
    
    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUPS_LIKE, "loader:group%_systemOfRecord");
    
    GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup);
    
    //###################################################################
    //make sure everything worked...
    
    //group1 is used in another group, so it should exist, with no members
    overallGroup1 = GroupFinder.findByName(this.grouperSession, "loader:group1");
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup1.hasMember(SubjectTestHelper.SUBJ6));
    
    //group2 should be removed
    overallGroup2 = GroupFinder.findByName(this.grouperSession, "loader:group2", false);
    assertNull(overallGroup2);
    
    //group3 should remove subj2 and add subj4
    overallGroup3 = GroupFinder.findByName(this.grouperSession, "loader:group3");
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup3.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup3.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup3.hasMember(SubjectTestHelper.SUBJ6));

    //group4 should be unchanged
    overallGroup4 = GroupFinder.findByName(this.grouperSession, "loader:group4");
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup4.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup4.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup4.hasMember(SubjectTestHelper.SUBJ6));
    
    //group5 should be added, with subj4 and subj5
    overallGroup5 = GroupFinder.findByName(this.grouperSession, "loader:group5");
    assertFalse(overallGroup5.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup5.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup5.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup5.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup5.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(overallGroup5.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup5.hasMember(SubjectTestHelper.SUBJ6));

    //group6 should be removed, and the excludes, but not the includes
    group6includes = GroupFinder.findByName(this.grouperSession, "loader:group6_includes");
    assertTrue(group6includes.hasMember(SubjectTestHelper.SUBJ9));

    overallGroup6 = GroupFinder.findByName(this.grouperSession, "loader:group6", false);
    assertNull(overallGroup6);
    
    Group group6excludes = GroupFinder.findByName(this.grouperSession, "loader:group6_excludes", false);
    assertNull(group6excludes);

  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    try {
      this.grouperSession = GrouperSession.startRootSession();
      
      ensureTestgrouperLoaderTable();
  
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperLoader").executeUpdate();
      
      //override whatever is in the config
      ApiConfig.testConfig.put("loader.autoadd.typesAttributes", "true");
      
      GrouperStartup.initLoaderType();
      
      setupTestConfigForIncludeExclude();
      
      GrouperStartup.initIncludeExcludeType();
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 
   */
  public void ensureTestgrouperLoaderTable() {
    //we need to delete the test table if it is there, and create a new one
    //drop field id col, first drop foreign keys
    GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {

      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        Database database = ddlVersionBean.getDatabase();

        Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"testgrouper_loader");
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", 
            Types.VARCHAR, "255", true, true);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "hibernate_version_number", 
            Types.BIGINT, "12", false, true);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "col1", 
            Types.VARCHAR, "255", false, false);
    
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "col2", 
            Types.VARCHAR, "255", false, false);
    
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "col3", 
            Types.VARCHAR, "255", false, false);
    
        GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
            "testgrouper_loader", "sample table that can be used by loader");
    
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_loader", "col1", 
            "col1");
    
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_loader", "col2", 
            "col2");
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_loader", "col3", 
            "col3");
      }
      
    });
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperSession.stopQuietly(this.grouperSession);
  }
  
}
