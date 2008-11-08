/*
 * @author mchyzer
 * $Id: GrouperLoaderTest.java,v 1.1 2008-11-08 08:15:34 mchyzer Exp $
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
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;


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
    
    testDataList.add(new TestgrouperLoader("loader:group1_systemOfRecord", SubjectTestHelper.SUBJ0_ID, null));
    testDataList.add(new TestgrouperLoader("loader:group1_systemOfRecord", SubjectTestHelper.SUBJ1_ID, null));
    testDataList.add(new TestgrouperLoader("loader:group2_systemOfRecord", SubjectTestHelper.SUBJ1_ID, null));
    testDataList.add(new TestgrouperLoader("loader:group2_systemOfRecord", SubjectTestHelper.SUBJ2_ID, null));

    HibernateSession.byObjectStatic().saveOrUpdate(testDataList);

    //lets add a group which will load these
    Group loaderGroup = Group.saveGroup(this.grouperSession, null, null, 
        "loader:owner",null, null, null, true);
    loaderGroup.addType(GroupTypeFinder.find("grouperLoader"));
    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, 
        "select col1 group_name, col2 subject_id from testgrouper_loader");
    loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUP_TYPES,
        "addIncludeExclude");
    
    GrouperLoader.runJobOnceForGroup(this.grouperSession, loaderGroup);
    
    Group overallGroup = GroupFinder.findByName(this.grouperSession, "loader:group1");
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
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
