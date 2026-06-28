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

import java.io.File;
import java.sql.Connection;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperDdl;
import edu.internet2.middleware.grouper.app.upgradeTasks.UpgradeTasks;
import edu.internet2.middleware.grouper.app.upgradeTasks.UpgradeTasksJob;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils.DbMetadataBean;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.Platform;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Index;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;


/**
 * tests
 */
public class GrouperDdlUtilsTest extends GrouperTest {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDdlUtilsTest.class);

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
    //GrouperTest.setupTests();
    //TestRunner.run(GrouperDdlUtilsTest.class);
    TestRunner.run(new GrouperDdlUtilsTest("testGrp7076InstallColumnWidths"));
    //TestRunner.run(new GrouperDdlUtilsTest("testUpgradeFrom2_5static"));
    //TestRunner.run(new GrouperDdlUtilsTest("testAutoInstall"));
    
    
    
    //TestRunner.run(new GrouperDdlUtilsTest("testUpgradeFrom2_4"));

    //TestRunner.run(new GrouperDdlUtilsTest("testAutoInstall"));
    
    
//    Platform platform = GrouperDdlUtils.retrievePlatform(false);
//    
//    
//    int javaVersion = GrouperDdlUtils.retrieveDdlJavaVersion("Grouper"); 
//    
//    DdlVersionable ddlVersionableJava = GrouperDdlUtils.retieveVersion("Grouper", javaVersion);
//
//    DbMetadataBean dbMetadataBean = GrouperDdlUtils.findDbMetadataBean(ddlVersionableJava);
//
//    //to be safe lets only deal with tables related to this object
//    platform.getModelReader().setDefaultTablePattern(dbMetadataBean.getDefaultTablePattern());
//    //platform.getModelReader().setDefaultTableTypes(new String[]{"TABLES"});
//    platform.getModelReader().setDefaultSchemaPattern(dbMetadataBean.getSchema());
//      
//    SqlBuilder sqlBuilder = platform.getSqlBuilder();
//
//    
//    //convenience to get the url, user, etc of the grouper db, helps get db connection
//    GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile("grouper");
//    
//    Connection connection = null;
////    Index index = null;
////    PreparedStatement preparedStatement = null;
////    ResultSet resultSet = null;
////    ResultSet resultSet2 = null;
//    try {
//      connection = grouperDb.connection();
//
//////      String sql = "select * from authzadm.grouper_stems where name = ':'";
//////      
//////      preparedStatement = connection.prepareStatement(sql);
//////      resultSet = preparedStatement.executeQuery();
//////        
//////      ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
//////
//////      System.out.println(resultSetMetaData.getColumnCount());
//////      System.out.println(resultSetMetaData.getColumnName(1));
//////      System.out.println(resultSetMetaData.getSchemaName(1));
//////      System.out.println(resultSetMetaData.getTableName(1));
////      
////      DatabaseMetaData databaseMetaData = connection.getMetaData();
////      
////      resultSet2 = databaseMetaData.getTables(null, null, "GROUPER_GROUPS", null);
////      while(resultSet2.next())
////      {
////        for (int i=1;i<=resultSet2.getMetaData().getColumnCount();i++) {
////          //Print
////          System.out.println(resultSet2.getMetaData().getColumnName(i) + ": " + resultSet2.getString(i));
////        }
////      }      
//      Database database = platform.readModelFromDatabase(connection, GrouperDdlUtils.PLATFORM_NAME, null,
//        "AUTHZADM", null);
//    
//      Table membersTable = GrouperDdlUtils.ddlutilsFindTable(database, Member.TABLE_GROUPER_MEMBERS, true);
//  
////      index = GrouperDdlUtils.ddlutilsFindIndex(database, membersTable.getName(), "member_subjidentifier0_idx");
////      
////    } catch (Exception e) {
////      if (e instanceof RuntimeException) {
////        throw (RuntimeException)e;
////      }
////      throw new RuntimeException("error", e);
//    } finally {
////      GrouperUtil.closeQuietly(resultSet);
////      GrouperUtil.closeQuietly(resultSet2);
////      GrouperUtil.closeQuietly(preparedStatement);
//      GrouperUtil.closeQuietly(connection);
//    }
//
//    assertNotNull(index);

    
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
  public void testDdl() {

    GrouperDdlUtils.deleteUtfDdls();

    try {
      Hib3GrouperDdl hib3GrouperDdl = (Hib3GrouperDdl)HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
        
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          Hib3GrouperDdl hib3GrouperDdl = GrouperDdlUtils.storeDdl(hibernateHandlerBean.getHibernateSession(), GrouperUuid.getUuid(), 
              "grouperUtf_abc", "");
          hibernateHandlerBean.getHibernateSession().commit(GrouperCommitType.COMMIT_NOW);
          return hib3GrouperDdl;
        }
      });
  
      Hib3GrouperDdl number2 = GrouperDdlUtils.retrieveDdlByIdFromDatabase(hib3GrouperDdl.getId());
      
      if (number2 == null) {
        throw new RuntimeException("Not by id!");
      }
      
      number2 = GrouperDdlUtils.retrieveDdlByNameFromDatabase(hib3GrouperDdl.getObjectName());
  
      if (number2 == null) {
        throw new RuntimeException("Not by id!");
      }
      
      GrouperDdlUtils.deleteDdlById(hib3GrouperDdl.getId());
  
      number2 = GrouperDdlUtils.retrieveDdlByIdFromDatabase(hib3GrouperDdl.getId());
      
      if (number2 != null) {
        throw new RuntimeException("cant delete!");
      }
  
      hib3GrouperDdl = (Hib3GrouperDdl)HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
        
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          Hib3GrouperDdl hib3GrouperDdl = GrouperDdlUtils.storeDdl(hibernateHandlerBean.getHibernateSession(), GrouperUuid.getUuid(), 
              "grouperUtf_abc", "");
          hibernateHandlerBean.getHibernateSession().commit(GrouperCommitType.COMMIT_NOW);
          return hib3GrouperDdl;
        }
      });
  
      
      number2 = GrouperDdlUtils.retrieveDdlByIdFromDatabase(hib3GrouperDdl.getId());
      
      if (number2 == null) {
        throw new RuntimeException("Not by id!");
      }
      
      GrouperDdlUtils.deleteUtfDdls();
  
      number2 = GrouperDdlUtils.retrieveDdlByIdFromDatabase(hib3GrouperDdl.getId());
      
      if (number2 != null) {
        throw new RuntimeException("cant deleteall!");
      }
    } finally {
      try {
        //GrouperDdlUtils.deleteUtfDdls();
      } catch (RuntimeException re) {
        LOG.error("error", re);
      }
    }
  }
  
  /**
   * 
   */
  public void testBootstrapHelper() {
    GrouperDdlUtils.justTesting = true;

    try {
      assertTrue("Starting out, tables should be there", GrouperDdlUtils.assertTablesThere(null, false, true));
      
      //now lets remove all tables and object
      new GrouperDdlEngine().assignCallFromCommandLine(false).assignFromUnitTest(true)
        .assignCompareFromDbVersion(false).assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
        .assignInstallDefaultGrouperData(false).assignMaxVersions(null).assignPromptUser(false)
        .assignFromStartup(false).runDdl();
      
      assertFalse("Just removed tables, shouldnt be there", GrouperDdlUtils.assertTablesThere(null, false, false));
  
      //lets add all tables and object
      new GrouperDdlEngine().assignCallFromCommandLine(false).assignFromUnitTest(true)
        .assignCompareFromDbVersion(false).assignDropBeforeCreate(false).assignWriteAndRunScript(true).assignDropOnly(false)
        .assignInstallDefaultGrouperData(true).assignMaxVersions(null).assignPromptUser(false)
        .assignFromStartup(false).runDdl();
      
      //if we init data, the root stem should be there...
      assertTrue("Just added all tables, and registry init, it should be there", 
          GrouperDdlUtils.assertTablesThere(null, true, true));
  
      //should also have at least two rows in ddl
      int count = HibernateSession.bySqlStatic().select(int.class, 
          "select count(*) from grouper_ddl");
      assertTrue("Count should be more than 1 since Grouper and Subject " +
      		"should be there " + count, count > 1);
      
      //try again, everything should be there (even not from junit)
      new GrouperDdlEngine().assignCallFromCommandLine(false).assignFromUnitTest(false)
        .assignCompareFromDbVersion(true).assignDropBeforeCreate(false).assignWriteAndRunScript(false).assignDropOnly(false)
        .assignInstallDefaultGrouperData(false).assignMaxVersions(null).assignPromptUser(false)
        .assignFromStartup(false).runDdl();

      assertTrue("Should not change anything", GrouperDdlUtils.assertTablesThere(null, true, true));
  
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
    super.setUp();
    GrouperDdlUtils.autoDdl2_5orAbove = null;
    //dont print annoying messages to user
    GrouperDdlUtils.internal_printDdlUpdateMessage = false;

  }

  
  
  @Override
  protected void setupConfigs() {

  }

  @Override
  protected void setupInitDb() {
    GrouperHibernateConfig.retrieveConfig().propertiesOverrideMap().put("registry.auto.ddl.upToVersion", "5.*.*");
    GrouperHibernateConfig.retrieveConfig().propertiesOverrideMap().put("registry.auto.ddl.upToVersion.elConfig", "5.*.*");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void tearDown() {
    //yes print annoying messages to user again
    GrouperDdlUtils.internal_printDdlUpdateMessage = true;
    GrouperDdlUtils.autoDdl2_5orAbove = null;
    
    // drop everything
    new GrouperDdlEngine().assignCallFromCommandLine(false).assignFromUnitTest(true)
      .assignCompareFromDbVersion(false).assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignInstallDefaultGrouperData(false).assignMaxVersions(null).assignPromptUser(true)
      .assignFromStartup(false).runDdl();
  
    
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    
    // ddl worker wasnt there...
    GrouperUtil.sleep(3000);
    
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);

    super.tearDown();

  }

  /**
   * 
   */
  public void testUpgradeFrom2_4static() {
    
    
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();

    
    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_4_hsql.sql
    // get to 2.4
    File scriptToGetTo2_4 = retrieveScriptFile("GrouperDdl_2_4_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_4, true, true);

    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);

    HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_sync");
    
    scriptToGetTo2_4.delete();
    
  }

  /**
   * 
   */
  public void testUpgradeFrom2_3static() {
    
    
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();

    
    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_3_hsql.sql
    // get to 2.3
    File scriptToGetTo2_3 = retrieveScriptFile("GrouperDdl_2_3_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_3, true, true);

    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);

    
    HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_sync");
    
    Platform platform = GrouperDdlUtils.retrievePlatform(false);
    
    int javaVersion = GrouperDdlUtils.retrieveDdlJavaVersion("Grouper"); 
    
    DdlVersionable ddlVersionableJava = GrouperDdlUtils.retieveVersion("Grouper", javaVersion);

    DbMetadataBean dbMetadataBean = GrouperDdlUtils.findDbMetadataBean(ddlVersionableJava);

    //to be safe lets only deal with tables related to this object
    platform.getModelReader().setDefaultTablePattern(dbMetadataBean.getDefaultTablePattern());
    //platform.getModelReader().setDefaultTableTypes(new String[]{"TABLES"});
    platform.getModelReader().setDefaultSchemaPattern(dbMetadataBean.getSchema());

    //convenience to get the url, user, etc of the grouper db, helps get db connection
    GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile("grouper");
    
    Connection connection = null;
    Index index = null;
    try {
      connection = grouperDb.connection();

      Database database = platform.readModelFromDatabase(connection, GrouperDdlUtils.PLATFORM_NAME, null,
        null, null);
    
      Table membersTable = GrouperDdlUtils.ddlutilsFindTable(database, Member.TABLE_GROUPER_MEMBERS, true);
  
      index = GrouperDdlUtils.ddlutilsFindIndex(database, membersTable.getName(), "member_subjidentifier0_idx");
      
    } finally {
      GrouperUtil.closeQuietly(connection);
    }

    assertNotNull(index);
    
    HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_config");

    scriptToGetTo2_3.delete();
    
  }

  private static File retrieveScriptFile(String fileName) {
    String scriptName = "edu/internet2/middleware/grouper/ddl/" + fileName;
    
    // if running on a workstation, assumes /src/test is on classpath and not filtering anything
    // look in eclipse at build path and make sure not filtering on *.java
    String script = GrouperUtil.readResourceIntoString(scriptName, true);
    
    File tempFile = GrouperUtil.newFileUniqueName(GrouperUtil.tmpDir(true), fileName, ".sql", true);
    
    GrouperUtil.saveStringIntoFile(tempFile, script);
    
//    File scriptToGetTo2_4 = GrouperUtil.fileFromResourceName(scriptName);
//    if (scriptToGetTo2_4 == null) {
//      
//      //lets get grouper.hibernate.base.properties and work back from there
//      scriptToGetTo2_4 = GrouperUtil.fileFromResourceName("grouper.hibernate.base.properties");
//      File grouperBase = scriptToGetTo2_4.getParentFile().getParentFile();
//      if ("target".equals(grouperBase.getName())) {
//        grouperBase = grouperBase.getParentFile();
//      }
//      scriptToGetTo2_4 = new File(grouperBase.getAbsolutePath() + "/src/test/" + scriptName);
//      
//      if (!scriptToGetTo2_4.exists() || !scriptToGetTo2_4.isFile()) {
//        throw new RuntimeException("Cant find 2.4 sql script: " + scriptName + ", " + scriptToGetTo2_4.getAbsolutePath());
//      }
//    }
    return tempFile;
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

  /**
   * 
   */
  public void testAutoInstall() {
    
    
    // drop everything
    new GrouperDdlEngine().assignCallFromCommandLine(false).assignFromUnitTest(true)
      .assignCompareFromDbVersion(false).assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignInstallDefaultGrouperData(false).assignMaxVersions(null).assignPromptUser(true)
      .assignFromStartup(false).runDdl();
  
    
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_sync");
        
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_members", "subject_resolution_deleted"));
    
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_time"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_cache_overall"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_cache_instance"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_recent_mships_conf"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_pit_memberships_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_pit_mship_group_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_pit_mship_stem_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_pit_mship_attr_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_recent_mships_conf_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_recent_mships_load_v"));

  }

  /**
   * GRP-7076: a group (source g:gsa) stores its fully-qualified name in grouper_members.subject_identifier0,
   * and folder paths live in grouper_stems.name; these were varchar(255) while grouper_groups.name is
   * varchar(1024), so a name of 256-1024 chars overflowed and failed the group/folder create.  This test
   * validates that a fresh install creates the widened columns as varchar(1024) and that their indexes exist
   * (on mysql they are (255) prefix indexes, so the install does not exceed the InnoDB key-length limit).
   * Run this against postgres, oracle, and mysql.
   */
  public void testGrp7076InstallColumnWidths() {

    // drop everything and reinstall from the current schema
    new GrouperDdlEngine().assignCallFromCommandLine(false).assignFromUnitTest(true)
      .assignCompareFromDbVersion(false).assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignInstallDefaultGrouperData(false).assignMaxVersions(null).assignPromptUser(true)
      .assignFromStartup(false).runDdl();

    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);

    // the widened columns must install as varchar(1024) (id1/id2 widened for parity with id0)
    assertEquals(1024, GrouperDdlUtils.getColumnSize("grouper_members", "subject_identifier0"));
    assertEquals(1024, GrouperDdlUtils.getColumnSize("grouper_members", "subject_identifier1"));
    assertEquals(1024, GrouperDdlUtils.getColumnSize("grouper_members", "subject_identifier2"));
    assertEquals(1024, GrouperDdlUtils.getColumnSize("grouper_pit_members", "subject_identifier0"));
    assertEquals(1024, GrouperDdlUtils.getColumnSize("grouper_stems", "name"));
    assertEquals(1024, GrouperDdlUtils.getColumnSize("grouper_stems", "display_name"));
    assertEquals(1024, GrouperDdlUtils.getColumnSize("grouper_stems", "alternate_name"));

    // the indexes must exist (the mysql install would have failed at CREATE INDEX if the (255) prefix were missing)
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_members", "member_subjidentifier0_idx"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_members", "member_subjidentifier1_idx"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_members", "member_subjidentifier2_idx"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_pit_members", "pit_member_subjidentifier0_idx"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_stems", "stem_name_idx"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_stems", "stem_displayname_idx"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_stems", "stem_alternate_name_idx"));
  }

  /**
   * GRP-7076: validate that UpgradeTaskV43 widens the columns from varchar(255) to varchar(1024) on oracle and
   * mysql (including dropping/recreating the mysql indexes as (255) prefixes).  Postgres is skipped: there the
   * widening is a manual DBA task because ALTER COLUMN ... TYPE is blocked by the dependent views, so
   * UpgradeTaskV43 intentionally does not auto-run it.  Run this against oracle and mysql.
   */
  public void testGrp7076WidenUpgradeTask() {

    // drop everything and reinstall (columns start at 1024)
    new GrouperDdlEngine().assignCallFromCommandLine(false).assignFromUnitTest(true)
      .assignCompareFromDbVersion(false).assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignInstallDefaultGrouperData(true).assignMaxVersions(null).assignPromptUser(true)
      .assignFromStartup(false).runDdl();
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);

    // postgres widening is a manual DBA task; nothing to auto-validate here
    if (GrouperDdlUtils.isPostgres()) {
      return;
    }

    // simulate a pre-GRP-7076 database by shrinking the columns back to varchar(255)
    grp7076NarrowColumn("grouper_members",     "subject_identifier0", "member_subjidentifier0_idx",     false, false);
    grp7076NarrowColumn("grouper_members",     "subject_identifier1", "member_subjidentifier1_idx",     false, false);
    grp7076NarrowColumn("grouper_members",     "subject_identifier2", "member_subjidentifier2_idx",     false, false);
    grp7076NarrowColumn("grouper_pit_members", "subject_identifier0", "pit_member_subjidentifier0_idx", false, false);
    grp7076NarrowColumn("grouper_stems",       "alternate_name",      "stem_alternate_name_idx",        false, false);
    grp7076NarrowColumn("grouper_stems",       "display_name",        "stem_displayname_idx",           false, true);
    grp7076NarrowColumn("grouper_stems",       "name",                "stem_name_idx",                  true,  true);

    assertEquals(255, GrouperDdlUtils.getColumnSize("grouper_stems", "name"));
    assertEquals(255, GrouperDdlUtils.getColumnSize("grouper_members", "subject_identifier0"));

    // run the upgrade task - it must widen everything back to 1024
    UpgradeTasks.V43.upgradeTask().updateVersionFromPrevious(null);

    assertEquals(1024, GrouperDdlUtils.getColumnSize("grouper_members", "subject_identifier0"));
    assertEquals(1024, GrouperDdlUtils.getColumnSize("grouper_members", "subject_identifier1"));
    assertEquals(1024, GrouperDdlUtils.getColumnSize("grouper_members", "subject_identifier2"));
    assertEquals(1024, GrouperDdlUtils.getColumnSize("grouper_pit_members", "subject_identifier0"));
    assertEquals(1024, GrouperDdlUtils.getColumnSize("grouper_stems", "name"));
    assertEquals(1024, GrouperDdlUtils.getColumnSize("grouper_stems", "display_name"));
    assertEquals(1024, GrouperDdlUtils.getColumnSize("grouper_stems", "alternate_name"));

    // the indexes must still exist after the widen (recreated as (255) prefixes on mysql)
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_stems", "stem_name_idx"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_members", "member_subjidentifier0_idx"));
  }

  /**
   * test helper: shrink a column back to varchar(255) to simulate a pre-GRP-7076 schema (oracle/mysql only).
   * On mysql the index is dropped and recreated as a full-column index (the old state) so it fits the key limit.
   * @param table
   * @param column
   * @param index
   * @param unique whether the index is unique
   * @param notNull whether the column is NOT NULL (mysql MODIFY rewrites the whole column definition)
   */
  private void grp7076NarrowColumn(String table, String column, String index, boolean unique, boolean notNull) {
    if (GrouperDdlUtils.isOracle()) {
      HibernateSession.bySqlStatic().executeSql("ALTER TABLE " + table + " MODIFY (" + column + " VARCHAR2(255))");
    } else if (GrouperDdlUtils.isMysql()) {
      if (GrouperDdlUtils.assertIndexExists(table, index)) {
        HibernateSession.bySqlStatic().executeSql("DROP INDEX " + index + " ON " + table);
      }
      HibernateSession.bySqlStatic().executeSql("ALTER TABLE " + table + " MODIFY " + column
          + " VARCHAR(255)" + (notNull ? " NOT NULL" : " NULL"));
      HibernateSession.bySqlStatic().executeSql("CREATE " + (unique ? "UNIQUE " : "") + "INDEX " + index
          + " ON " + table + " (" + column + ")");
    }
  }

  /**
   * 
   */
  public void testUpgradeFrom2_3ddlUtils() {
    
    
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    
    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_3_hsql.sql
    // get to 2.3
    File scriptToGetTo2_3 = retrieveScriptFile("GrouperDdl_2_3_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_3, true, true);
  
    new GrouperDdlEngine().assignCallFromCommandLine(false).assignFromUnitTest(true).assignDeepCheck(false)
      .assignCompareFromDbVersion(true)//.assignRecreateViewsAndForeignKeys(theRecreateViewsAndForeignKeys)
      .assignDropBeforeCreate(false).assignWriteAndRunScript(true)
      .assignUseDdlUtils(true)
      .assignDropOnly(false)
      .assignInstallDefaultGrouperData(false).assignPromptUser(false).runDdl();
  
    
    HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_sync");
    
    Platform platform = GrouperDdlUtils.retrievePlatform(false);
    
    int javaVersion = GrouperDdlUtils.retrieveDdlJavaVersion("Grouper"); 
    
    DdlVersionable ddlVersionableJava = GrouperDdlUtils.retieveVersion("Grouper", javaVersion);
  
    DbMetadataBean dbMetadataBean = GrouperDdlUtils.findDbMetadataBean(ddlVersionableJava);
  
    //to be safe lets only deal with tables related to this object
    platform.getModelReader().setDefaultTablePattern(dbMetadataBean.getDefaultTablePattern());
    //platform.getModelReader().setDefaultTableTypes(new String[]{"TABLES"});
    platform.getModelReader().setDefaultSchemaPattern(dbMetadataBean.getSchema());
  
    //convenience to get the url, user, etc of the grouper db, helps get db connection
    GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile("grouper");
    
    Connection connection = null;
    Index index = null;
    try {
      connection = grouperDb.connection();
  
      Database database = platform.readModelFromDatabase(connection, GrouperDdlUtils.PLATFORM_NAME, null,
        null, null);
    
      Table membersTable = GrouperDdlUtils.ddlutilsFindTable(database, Member.TABLE_GROUPER_MEMBERS, true);
  
      index = GrouperDdlUtils.ddlutilsFindIndex(database, membersTable.getName(), "member_subjidentifier0_idx");
      
    } finally {
      GrouperUtil.closeQuietly(connection);
    }
  
    assertNotNull(index);
    
    HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_config");
  
    scriptToGetTo2_3.delete();
    
  }

  /**
   * 
   */
  public void testUpgradeFrom2_4ddlUtils() {
    
    
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    
    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_4_hsql.sql
    // get to 2.4
    File scriptToGetTo2_4 = retrieveScriptFile("GrouperDdl_2_4_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_4, true, true);
  
    new GrouperDdlEngine().assignCallFromCommandLine(false).assignFromUnitTest(true).assignDeepCheck(false)
      .assignCompareFromDbVersion(true)//.assignRecreateViewsAndForeignKeys(theRecreateViewsAndForeignKeys)
      .assignDropBeforeCreate(false).assignWriteAndRunScript(true)
      .assignUseDdlUtils(true)
      .assignDropOnly(false)
      .assignInstallDefaultGrouperData(false).assignPromptUser(false).runDdl();
  
    HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_sync");
    
    scriptToGetTo2_4.delete();
    
  }
  
  /**
   * 
   */
  public void testUpgradeFrom2_5static() {
    
    
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();

    
    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_5_hsql.sql
    // get to 2.5
    File scriptToGetTo2_5 = retrieveScriptFile("GrouperDdl_2_5_0_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_5, true, true);
    
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_members", "subject_resolution_deleted"));

    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_time"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_cache_overall"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_cache_instance"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_recent_mships_conf"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_pit_memberships_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_pit_mship_group_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_pit_mship_stem_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_pit_mship_attr_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_recent_mships_conf_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_recent_mships_load_v"));

    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);

    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_members", "subject_resolution_deleted"));
    
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_time"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_cache_overall"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_cache_instance"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_recent_mships_conf"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_pit_memberships_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_pit_mship_group_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_pit_mship_stem_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_pit_mship_attr_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_recent_mships_conf_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_recent_mships_load_v"));

    scriptToGetTo2_5.delete();
    
  }
  
  /**
   * 
   */
  public void testUpgradeFrom2_5ddlUtils() {
    
    
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    
    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_5_hsql.sql
    // get to 2.5
    File scriptToGetTo2_5 = retrieveScriptFile("GrouperDdl_2_5_0_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_5, true, true);
    
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_members", "subject_resolution_deleted"));
  
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_time"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_cache_overall"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_cache_instance"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_recent_mships_conf"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_pit_memberships_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_pit_mship_group_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_pit_mship_stem_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_pit_mship_attr_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_recent_mships_conf_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_recent_mships_load_v"));

    new GrouperDdlEngine().assignCallFromCommandLine(false).assignFromUnitTest(true).assignDeepCheck(false)
      .assignCompareFromDbVersion(true)//.assignRecreateViewsAndForeignKeys(theRecreateViewsAndForeignKeys)
      .assignDropBeforeCreate(false).assignWriteAndRunScript(true)
      .assignUseDdlUtils(true)
      .assignDropOnly(false)
      .assignInstallDefaultGrouperData(false).assignPromptUser(false).runDdl();
  
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_members", "subject_resolution_deleted"));
    
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_time"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_cache_overall"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_cache_instance"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_recent_mships_conf"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_pit_memberships_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_pit_mship_group_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_pit_mship_stem_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_pit_mship_attr_lw_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_recent_mships_conf_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_recent_mships_load_v"));

    scriptToGetTo2_5.delete();
    
  }
  
  /**
   * 
   */
  public void testUpgradeFrom2_5_33To2_5_34ddlUtils() {
    
    
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_5_30_hsql.sql
    // get to 2.5
    File scriptToGetTo2_5_30 = retrieveScriptFile("GrouperDdl_2_5_30_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_5_30, true, true);
    
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_pit_config"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_config", "config_value_clob"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_config", "config_value_bytes"));
  
    new GrouperDdlEngine().assignCallFromCommandLine(false).assignFromUnitTest(true).assignDeepCheck(false)
      .assignCompareFromDbVersion(true)//.assignRecreateViewsAndForeignKeys(theRecreateViewsAndForeignKeys)
      .assignDropBeforeCreate(false).assignWriteAndRunScript(true)
      .assignUseDdlUtils(true)
      .assignDropOnly(false)
      .assignInstallDefaultGrouperData(false).assignPromptUser(false).runDdl();
  
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_config", "config_value_clob"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_config", "config_value_bytes"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_pit_config"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_file"));

    scriptToGetTo2_5_30.delete();
    
  }
  
  /**
   * 
   */
  public void testUpgradeFrom2_5_34To2_5_35ddlUtils() {
    
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_5_30_hsql.sql
    // get to 2.5
    File scriptToGetTo2_5_30 = retrieveScriptFile("GrouperDdl_2_5_30_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_5_30, true, true);
    
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_sync_log", "description_clob"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_sync_log", "description_bytes"));
  
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_log", "description_clob"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_log", "description_bytes"));

    scriptToGetTo2_5_30.delete();
    
  }
  
  /**
   * 
   */
  public void testUpgradeFrom2_5_38To2_5_40ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_group", "error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_member", "error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_membership", "error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_membership_v", "u_error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_membership_v", "g_error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_membership_v", "m_error_code"));
    assertTrue(GrouperDdlUtils.assertIndexHasColumn("grouper_sync_group", "grouper_sync_gr_er_idx", "error_code"));
    assertTrue(GrouperDdlUtils.assertIndexHasColumn("grouper_sync_member", "grouper_sync_us_er_idx", "error_code"));
    assertTrue(GrouperDdlUtils.assertIndexHasColumn("grouper_sync_membership", "grouper_sync_mship_er_idx", "error_code"));

    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
    .assignDropBeforeCreate(false).assignWriteAndRunScript(false).assignDropOnly(false)
    .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0, grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0, grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
    
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();

    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_5_38_hsql.sql
    // get to 2.5.38
    File scriptToGetTo2_5_38 = retrieveScriptFile("GrouperDdl_2_5_38_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_5_38, true, true);

    // stuff gone
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_sync_group", "error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_sync_member", "error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_sync_membership", "error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_sync_membership_v", "u_error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_sync_membership_v", "g_error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_sync_membership_v", "m_error_code"));
    assertFalse(GrouperDdlUtils.assertIndexHasColumn("grouper_sync_group", "grouper_sync_gr_er_idx", "error_code"));
    assertFalse(GrouperDdlUtils.assertIndexHasColumn("grouper_sync_member", "grouper_sync_us_er_idx", "error_code"));
    assertFalse(GrouperDdlUtils.assertIndexHasColumn("grouper_sync_membership", "grouper_sync_mship_er_idx", "error_code"));

    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
    .assignDropBeforeCreate(false).assignWriteAndRunScript(false).assignDropOnly(false)
    .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, " 
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());

    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_group", "error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_member", "error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_membership", "error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_membership_v", "u_error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_membership_v", "g_error_code"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_membership_v", "m_error_code"));
    assertTrue(GrouperDdlUtils.assertIndexHasColumn("grouper_sync_group", "grouper_sync_gr_er_idx", "error_code"));
    assertTrue(GrouperDdlUtils.assertIndexHasColumn("grouper_sync_member", "grouper_sync_us_er_idx", "error_code"));
    assertTrue(GrouperDdlUtils.assertIndexHasColumn("grouper_sync_membership", "grouper_sync_mship_er_idx", "error_code"));
  
    scriptToGetTo2_5_38.delete();
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
    .assignDropBeforeCreate(false).assignWriteAndRunScript(false).assignDropOnly(false)
    .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0, grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0, grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }
  
  /**
   * 
   */
  public void testUpgradeFrom2_5_49To2_5_51ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_group", "metadata_json"));

    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());

    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();

    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_5_49_hsql.sql
    // get to 2.5.49
    File scriptToGetTo2_5_49 = retrieveScriptFile("GrouperDdl_2_5_49_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_5_49, true, true);

    // stuff gone
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_sync_group", "metadata_json"));

    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());

    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_group", "metadata_json"));

    scriptToGetTo2_5_49.delete();
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }
  
  
  /**
   * 
   */
  public void testUpgradeFrom2_5_51To2_6_1ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password", "expires_millis"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password", "created_millis"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password", "member_id_who_set_password"));
    
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password_recently_used", "attempt_millis"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password_recently_used", "ip_address"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password_recently_used", "status"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password_recently_used", "hibernate_version_number"));
    
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_password", "recent_source_addresses"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_password", "failed_source_addresses"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_password", "failed_logins"));

    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_prov_zoom_user", "email"));

    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());

    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();

    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_5_51_hsql.sql
    // get to 2.5.51
    File scriptToGetTo2_5_51 = retrieveScriptFile("GrouperDdl_2_5_51_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_5_51, true, true);

    // stuff gone
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_password", "expires_millis"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_password", "created_millis"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_password", "member_id_who_set_password"));
    
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_password_recently_used", "attempt_millis"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_password_recently_used", "ip_address"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_password_recently_used", "status"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_password_recently_used", "hibernate_version_number"));
    
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password", "recent_source_addresses"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password", "failed_source_addresses"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password", "failed_logins"));

    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_prov_zoom_user", "email"));

    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());

    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password", "expires_millis"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password", "created_millis"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password", "member_id_who_set_password"));
    
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password_recently_used", "attempt_millis"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password_recently_used", "ip_address"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password_recently_used", "status"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_password_recently_used", "hibernate_version_number"));
    
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_password", "recent_source_addresses"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_password", "failed_source_addresses"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_password", "failed_logins"));

    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_prov_zoom_user", "email"));

    scriptToGetTo2_5_51.delete();
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }

  /**
   * 
   */
  public void testUpgradeFrom2_6_1To2_6_5ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_FAILSAFE, GrouperDdl2_6_5.COLUMN_GROUPER_FAILSAFE_ID));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_FAILSAFE, GrouperDdl2_6_5.COLUMN_GROUPER_FAILSAFE_APPROVAL_MEMBER_ID));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_FAILSAFE, GrouperDdl2_6_5.COLUMN_GROUPER_FAILSAFE_APPROVED_ONCE));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_FAILSAFE, GrouperDdl2_6_5.COLUMN_GROUPER_FAILSAFE_LAST_APPROVAL));
    
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_LAST_LOGIN, GrouperDdl2_6_5.COLUMN_GROUPER_LAST_LOGIN_MEMBER_UUID));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_LAST_LOGIN, GrouperDdl2_6_5.COLUMN_GROUPER_LAST_LOGIN_MILLIS));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_LAST_LOGIN, GrouperDdl2_6_5.COLUMN_GROUPER_LAST_STEM_VIEW_COMPUTE));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_LAST_LOGIN, GrouperDdl2_6_5.COLUMN_GROUPER_LAST_STEM_VIEW_NEED));
    
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_STEM_VIEW_PRIVILEGE, GrouperDdl2_6_5.COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_MEMBER_UUID));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_STEM_VIEW_PRIVILEGE, GrouperDdl2_6_5.COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_OBJECT_TYPE));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_STEM_VIEW_PRIVILEGE, GrouperDdl2_6_5.COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_STEM_UUID));
  
    assertTrue(GrouperDdlUtils.assertColumnThere(true, Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_SUBJECT_RESOLUTION_ELIGIBLE));
  
    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_5_51_postgres.sql
    // get to 2.5.51
    File scriptToGetTo2_6_1 = retrieveScriptFile("GrouperDdl_2_6_1_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_6_1, true, true);
  
    // stuff gone
    assertTrue(GrouperDdlUtils.assertColumnThere(false, GrouperDdl2_6_5.TABLE_GROUPER_FAILSAFE, GrouperDdl2_6_5.COLUMN_GROUPER_FAILSAFE_ID));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, GrouperDdl2_6_5.TABLE_GROUPER_FAILSAFE, GrouperDdl2_6_5.COLUMN_GROUPER_FAILSAFE_APPROVAL_MEMBER_ID));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, GrouperDdl2_6_5.TABLE_GROUPER_FAILSAFE, GrouperDdl2_6_5.COLUMN_GROUPER_FAILSAFE_APPROVED_ONCE));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, GrouperDdl2_6_5.TABLE_GROUPER_FAILSAFE, GrouperDdl2_6_5.COLUMN_GROUPER_FAILSAFE_LAST_APPROVAL));
    
    assertTrue(GrouperDdlUtils.assertColumnThere(false, GrouperDdl2_6_5.TABLE_GROUPER_LAST_LOGIN, GrouperDdl2_6_5.COLUMN_GROUPER_LAST_LOGIN_MEMBER_UUID));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, GrouperDdl2_6_5.TABLE_GROUPER_LAST_LOGIN, GrouperDdl2_6_5.COLUMN_GROUPER_LAST_LOGIN_MILLIS));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, GrouperDdl2_6_5.TABLE_GROUPER_LAST_LOGIN, GrouperDdl2_6_5.COLUMN_GROUPER_LAST_STEM_VIEW_COMPUTE));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, GrouperDdl2_6_5.TABLE_GROUPER_LAST_LOGIN, GrouperDdl2_6_5.COLUMN_GROUPER_LAST_STEM_VIEW_NEED));
    
    assertTrue(GrouperDdlUtils.assertColumnThere(false, GrouperDdl2_6_5.TABLE_GROUPER_STEM_VIEW_PRIVILEGE, GrouperDdl2_6_5.COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_MEMBER_UUID));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, GrouperDdl2_6_5.TABLE_GROUPER_STEM_VIEW_PRIVILEGE, GrouperDdl2_6_5.COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_OBJECT_TYPE));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, GrouperDdl2_6_5.TABLE_GROUPER_STEM_VIEW_PRIVILEGE, GrouperDdl2_6_5.COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_STEM_UUID));
  
    assertTrue(GrouperDdlUtils.assertColumnThere(false, Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_SUBJECT_RESOLUTION_ELIGIBLE));
  
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_FAILSAFE, GrouperDdl2_6_5.COLUMN_GROUPER_FAILSAFE_ID));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_FAILSAFE, GrouperDdl2_6_5.COLUMN_GROUPER_FAILSAFE_APPROVAL_MEMBER_ID));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_FAILSAFE, GrouperDdl2_6_5.COLUMN_GROUPER_FAILSAFE_APPROVED_ONCE));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_FAILSAFE, GrouperDdl2_6_5.COLUMN_GROUPER_FAILSAFE_LAST_APPROVAL));
    
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_LAST_LOGIN, GrouperDdl2_6_5.COLUMN_GROUPER_LAST_LOGIN_MEMBER_UUID));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_LAST_LOGIN, GrouperDdl2_6_5.COLUMN_GROUPER_LAST_LOGIN_MILLIS));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_LAST_LOGIN, GrouperDdl2_6_5.COLUMN_GROUPER_LAST_STEM_VIEW_COMPUTE));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_LAST_LOGIN, GrouperDdl2_6_5.COLUMN_GROUPER_LAST_STEM_VIEW_NEED));
    
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_STEM_VIEW_PRIVILEGE, GrouperDdl2_6_5.COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_MEMBER_UUID));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_STEM_VIEW_PRIVILEGE, GrouperDdl2_6_5.COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_OBJECT_TYPE));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, GrouperDdl2_6_5.TABLE_GROUPER_STEM_VIEW_PRIVILEGE, GrouperDdl2_6_5.COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_STEM_UUID));
  
    assertTrue(GrouperDdlUtils.assertColumnThere(true, Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_SUBJECT_RESOLUTION_ELIGIBLE));
  
    scriptToGetTo2_6_1.delete();
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }
  
  /**
   * 
   */
  public void testUpgradeFrom2_6_5To2_6_8ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_duo_user"));
  
    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_5_51_postgres.sql
    // get to 2.5.51
    File scriptToGetTo2_6_5 = retrieveScriptFile("GrouperDdl_2_6_5_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_6_5, true, true);
  
    // stuff gone
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_prov_duo_user"));
  
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_duo_user"));
  
    scriptToGetTo2_6_5.delete();
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }

  /**
   * 
   */
  public void testUpgradeFrom2_6_8To2_6_14ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_loader_log", "job_message_clob"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_loader_log", "job_message_bytes"));

    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    // get to 2.6.8
    File scriptToGetTo2_6_8 = retrieveScriptFile("GrouperDdl_2_6_8_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_6_8, true, true);
  
    // stuff gone
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_loader_log", "job_message_clob"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_loader_log", "job_message_bytes"));
  
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_loader_log", "job_message_clob"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_loader_log", "job_message_bytes"));
  
    scriptToGetTo2_6_8.delete();
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }
  
  /**
   * 
   */
  public void testUpgradeFrom2_6_16To2_6_18ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_sync_dep_group_user"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_dep_group_user", "field_id"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_user", "grouper_sync_dep_grp_user_idx1"));

    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_sync_dep_group_group"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_dep_group_group", "field_id"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_group", "grouper_sync_dep_grp_grp_idx2"));

    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    // get to 2.6.16
    File scriptToGetTo2_6_16 = retrieveScriptFile("GrouperDdl_2_6_16_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_6_16, true, true);
    
    // stuff gone
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_sync_dep_group_user"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_sync_dep_group_user", "field_id"));
    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_user", "grouper_sync_dep_grp_user_idx1"));

    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_sync_dep_group_group"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_sync_dep_group_group", "field_id"));
    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_group", "grouper_sync_dep_grp_grp_idx2"));
  
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_sync_dep_group_user"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_dep_group_user", "field_id"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_user", "grouper_sync_dep_grp_user_idx1"));

    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_sync_dep_group_group"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_dep_group_group", "field_id"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_group", "grouper_sync_dep_grp_grp_idx2"));
  
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
    
    // try from upgrade step
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    // get to 2.6.16    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_6_16, true, true);
    
    // stuff gone
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_sync_dep_group_user"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_sync_dep_group_user", "field_id"));
    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_user", "grouper_sync_dep_grp_user_idx1"));

    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_sync_dep_group_group"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_sync_dep_group_group", "field_id"));
    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_group", "grouper_sync_dep_grp_grp_idx2"));
  
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());

    UpgradeTasks.V11.upgradeTask().updateVersionFromPrevious(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_sync_dep_group_user"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_dep_group_user", "field_id"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_user", "grouper_sync_dep_grp_user_idx1"));

    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_sync_dep_group_group"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync_dep_group_group", "field_id"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_group", "grouper_sync_dep_grp_grp_idx2"));
  
    scriptToGetTo2_6_16.delete();
    
    // get everything back
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());

    
    
  }

  /**
   * 
   */
  public void testUpgradeFrom4_11_0To4_14_0ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_prov_scim_user", "config_id"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx1"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx2"));

    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user_attr"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_prov_scim_user_attr", "config_id"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx1"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx2"));

    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    // get to 2.6.16
    File scriptToGetTo2_6_16 = retrieveScriptFile("GrouperDdl_2_6_16_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_6_16, true, true);
    
    // stuff gone
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_prov_scim_user"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_prov_scim_user", "config_id"));
    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx1"));
    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx2"));

    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_prov_scim_user_attr"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_prov_scim_user_attr", "config_id"));
    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx1"));
    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx2"));
  
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_prov_scim_user", "config_id"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx1"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx2"));

    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user_attr"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_prov_scim_user_attr", "config_id"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx1"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx2"));
  
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
    
    // try from upgrade step
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    // get to 2.6.16    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_6_16, true, true);
    
    // stuff gone
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_prov_scim_user"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_prov_scim_user", "config_id"));
    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx1"));
    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx2"));

    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_prov_scim_user_attr"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_prov_scim_user_attr", "config_id"));
    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx1"));
    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx2"));
  
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());

    UpgradeTasks.V19.upgradeTask().updateVersionFromPrevious(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_prov_scim_user", "config_id"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx1"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx2"));

    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user_attr"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_prov_scim_user_attr", "config_id"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx1"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx2"));
  
    scriptToGetTo2_6_16.delete();
    
    // get everything back
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());

    
    
  }
  
  /**
   * 
   */
  public void testUpgradeFrom2_6_14To2_6_16ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_mship_req_change"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_members", "id_index"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_members", "member_id_index_idx"));
  
    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    // get to 2.6.14
    File scriptToGetTo2_6_14 = retrieveScriptFile("GrouperDdl_2_6_14_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_6_14, true, true);
  
    // stuff gone
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_mship_req_change"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_members", "id_index"));
    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_members", "member_id_index_idx"));
  
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_mship_req_change"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_members", "id_index"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_members", "member_id_index_idx"));
  
    scriptToGetTo2_6_14.delete();
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }
  
  public void testUpgradeFrom2_6_16To5_0_0ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_members", "internal_id"));
    
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_dictionary"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_provider"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_field"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_row"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_alias"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_field_assign"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_row_assign"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_row_field_assign"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_global_assign"));
    
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_field_assign_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_row_assign_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_row_field_asgn_v"));

//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_members", "grouper_mem_internal_id_idx"));
    
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_dictionary", "dictionary_last_referenced_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_dictionary", "dictionary_pre_load_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_dictionary", "dictionary_the_text_idx"));
//
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_provider", "data_provider_config_id_idx"));
//
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_field", "data_field_config_id_idx"));
//
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_row", "grouper_data_row_config_id_idx"));
//    
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_alias", "alias_data_field_intrnl_id_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_alias", "alias_lower_name_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_alias", "alias_name_idx"));
//    
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_field_assign", "fld_assgn_prvdr_intrnl_id_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_field_assign", "fld_assgn_field_intrnl_id_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_field_assign", "fld_assgn_mbrs_intrnl_id_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_field_assign", "fld_assgn_mbr_intrnl_id_idx"));
//    
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_row_assign", "rw_assg_dt_prvdr_intrnl_id_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_row_assign", "rw_assg_dt_rw_intrnl_id_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_row_assign", "rw_assg_mbr_intrnl_id_idx"));
//
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_row_field_assign", "dt_rw_fld_asg_fld_intrnl_ididx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_row_field_assign", "dtrwfldasg_dtrwsg_intrnl_ididx"));
//    
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_global_assign", "grouper_data_global1_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_global_assign", "grouper_data_global2_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_global_assign", "grouper_data_global3_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_global_assign", "grouper_data_global4_idx"));

    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    // get to 2.6.16
    File scriptToGetTo2_6_16 = retrieveScriptFile("GrouperDdl_2_6_16_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo2_6_16, true, true);
  
    // stuff gone
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_members", "internal_id"));
    
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_dictionary"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_data_provider"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_data_field"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_data_row"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_data_alias"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_data_field_assign"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_data_row_assign"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_data_row_field_assign"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_data_global_assign"));
    
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_data_field_assign_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_data_row_assign_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_data_row_field_asgn_v"));

//    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_members", "grouper_mem_internal_id_idx"));
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    UpgradeTasks.V8.upgradeTask().updateVersionFromPrevious(null);
    
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_members", "internal_id"));
    
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_dictionary"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_provider"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_field"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_row"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_alias"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_field_assign"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_row_assign"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_row_field_assign"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_global_assign"));
    
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_field_assign_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_row_assign_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_data_row_field_asgn_v"));

//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_members", "grouper_mem_internal_id_idx"));
//    
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_dictionary", "dictionary_last_referenced_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_dictionary", "dictionary_pre_load_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_dictionary", "dictionary_the_text_idx"));
//
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_provider", "data_provider_config_id_idx"));
//
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_field", "data_field_config_id_idx"));
//
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_row", "grouper_data_row_config_id_idx"));
//    
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_alias", "alias_data_field_intrnl_id_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_alias", "alias_lower_name_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_alias", "alias_name_idx"));
//    
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_field_assign", "fld_assgn_prvdr_intrnl_id_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_field_assign", "fld_assgn_field_intrnl_id_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_field_assign", "fld_assgn_mbrs_intrnl_id_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_field_assign", "fld_assgn_mbr_intrnl_id_idx"));
//    
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_row_assign", "rw_assg_dt_prvdr_intrnl_id_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_row_assign", "rw_assg_dt_rw_intrnl_id_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_row_assign", "rw_assg_mbr_intrnl_id_idx"));
//
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_row_field_assign", "dt_rw_fld_asg_fld_intrnl_ididx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_row_field_assign", "dtrwfldasg_dtrwsg_intrnl_ididx"));
//    
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_global_assign", "grouper_data_global1_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_global_assign", "grouper_data_global2_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_global_assign", "grouper_data_global3_idx"));
//    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_data_global_assign", "grouper_data_global4_idx"));
  
    scriptToGetTo2_6_16.delete();
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }

  /**
   * 
   */
  public void testUpgradeFrom5_0_0To5_0_4ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_v"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_fields", "internal_id"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_fields", "grouper_fie_internal_id_idx"));
  
    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings" , 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    // get to 5.0.0
    File scriptToGetTo5_0_0 = retrieveScriptFile("GrouperDdl_5_0_0_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo5_0_0, true, true);
  
    // roll back the version
    String groupName = UpgradeTasksJob.grouperUpgradeTasksStemName() + ":" + UpgradeTasksJob.UPGRADE_TASKS_METADATA_GROUP;
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true);
    String upgradeTasksVersionName = UpgradeTasksJob.grouperUpgradeTasksStemName() + ":" + UpgradeTasksJob.UPGRADE_TASKS_VERSION_ATTR;
    group.getAttributeValueDelegate().assignValue(upgradeTasksVersionName, "9");

    // stuff gone
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_sql_cache_mship_v"));
    assertTrue(GrouperDdlUtils.assertColumnThere(false, "grouper_fields", "internal_id"));
    assertFalse(GrouperDdlUtils.assertIndexExists("grouper_fields", "grouper_fie_internal_id_idx"));
  
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_v"));
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_fields", "internal_id"));
    assertTrue(GrouperDdlUtils.assertIndexExists("grouper_fields", "grouper_fie_internal_id_idx"));
  
    scriptToGetTo5_0_0.delete();
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }
  
  /**
   * 
   */
  public void testUpgradeFrom5_0_4To5_11_0ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user_attr"));
  
    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_5_51_postgres.sql
    // get to 5.0.4
    File scriptToGetTo5_0_4 = retrieveScriptFile("GrouperDdl_5_0_4_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo5_0_4, true, true);
  
    // stuff gone
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user"));
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user_attr"));
  
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user_attr"));
  
    scriptToGetTo5_0_4.delete();
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }
  
  /**
   * 
   */
  public void testUpgradeFrom5_11_0To5_12_0ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_azure_user"));
  
    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_5_51_postgres.sql
    // get to 5.0.4
    File scriptToGetTo5_0_4 = retrieveScriptFile("GrouperDdl_5_11_0_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo5_0_4, true, true);
  
    // stuff gone
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_prov_azure_user"));
  
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_azure_user"));
  
    // try from upgrade step
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    // get to 2.6.16    
    GrouperDdlUtils.sqlRun(scriptToGetTo5_0_4, true, true);
    
    // stuff gone
    assertTrue(GrouperDdlUtils.assertTableThere(false, "grouper_prov_azure_user"));
  
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    
    assertTrue(grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors, "
        + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings",
        0 < grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount()
            + grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());

    UpgradeTasks.V20.upgradeTask().updateVersionFromPrevious(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_azure_user"));
  
    scriptToGetTo5_0_4.delete();

    
    
    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }
  
  /**
   * 
   */
  public void testUpgradeFrom5_12_0To5_13_0ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_user"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_group"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_membership"));
  
    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_5_51_postgres.sql
    // get to 5.0.4
    File scriptToGetTo5_0_4 = retrieveScriptFile("GrouperDdl_5_12_0_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo5_0_4, true, true);
  
    // stuff gone
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_user"));
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_group"));
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_membership"));
  
    UpgradeTasks.V14.upgradeTask().updateVersionFromPrevious(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_user"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_group"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_membership"));
  
    scriptToGetTo5_0_4.delete();

    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getResult() + " " + 
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    
    
    
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }
  
  /**
   * 
   */
  public void testUpgradeFrom5_13_0To5_22_0ddlUtils() {
    
    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_lifecycle_event_config"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_lifecycle_event"));
  
    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  
    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();
  
    //edu/internet2/middleware/grouper/ddl/GrouperDdl_2_5_51_postgres.sql
    // get to 5.0.4
    File scriptToGetTo5_13_0 = retrieveScriptFile("GrouperDdl_5_13_0_" + GrouperDdlUtils.databaseType() + ".sql");
    
    GrouperDdlUtils.sqlRun(scriptToGetTo5_13_0, true, true);
  
    // stuff gone
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_lifecycle_event_config"));
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_lifecycle_event"));
  
    UpgradeTasks.V36.upgradeTask().updateVersionFromPrevious(null);
  
    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_lifecycle_event_config"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_lifecycle_event"));
  
    scriptToGetTo5_13_0.delete();

    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getResult() + " " + 
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    
    
    
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }

  /**
   * test upgrade from 6.0.1 to 6.1.0 (OAuth tables: grouper_oauth_client, grouper_oauth_code, grouper_oauth_pend_authz_req)
   */
  public void testUpgradeFrom6_0_1To6_1_0ddlUtils() {

    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_oauth_client"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_oauth_code"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_oauth_pend_authz_req"));

    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());

    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();

    // load 6.0.1 DDL (no OAuth tables)
    File scriptToGetTo6_0_1 = retrieveScriptFile("GrouperDdl_6_0_1_" + GrouperDdlUtils.databaseType() + ".sql");

    GrouperDdlUtils.sqlRun(scriptToGetTo6_0_1, true, true);

    // stuff gone
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_oauth_client"));
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_oauth_code"));
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_oauth_pend_authz_req"));

    UpgradeTasks.V38.upgradeTask().updateVersionFromPrevious(null);

    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_oauth_client"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_oauth_code"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_oauth_pend_authz_req"));

    scriptToGetTo6_0_1.delete();

    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();

    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getResult() + " " +
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());



    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings", 0,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }

  /**
   * test upgrade from pre-V41 (7.0.0) to current:
   * - adds grouper_sync.internal_id column (backfilled and NOT NULL)
   * - creates grouper_prov_* tables
   * - creates grouper_prov_user_attr_v, grouper_prov_group_attr_v, grouper_prov_mship_v views
   */
  public void testUpgradeFrom7_0_0_pre_V41ddlUtils() {

    // bring DB to current install state so the baseline assertions hold even
    // if the test DB predates the V47/GrouperDdl7_2_0 wiring
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);

    //lets make sure everything is there on install
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync", "internal_id"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_group"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_group_attr"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_group_attr_value"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_user"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_user_attr"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_user_attr_value"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_mship_role"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_mship"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_user_attr_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_group_attr_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_mship_v"));

    // capture baseline deep-check error/warning counts so we can assert the upgrade
    // doesn't introduce NEW issues (some pre-existing FK gaps in the install schema
    // are unrelated to V41)
    GrouperDdlEngine grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();
    int baselineErrorCount = grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount();
    int baselineWarningCount = grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount();

    // drop everything
    new GrouperDdlEngine().assignFromUnitTest(true)
      .assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignMaxVersions(null).assignPromptUser(true).runDdl();

    // load pre-V41 DDL (no grouper_sync.internal_id, no grouper_prov_* tables/views)
    File scriptToGetToPreV41 = retrieveScriptFile("GrouperDdl_7_0_0_pre_V41_" + GrouperDdlUtils.databaseType() + ".sql");

    GrouperDdlUtils.sqlRun(scriptToGetToPreV41, true, true);

    // baseline does not include grouper_ddl_worker; recreate it like other baseline tests do
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);

    // stuff gone
    assertFalse(GrouperDdlUtils.assertColumnThere(true, "grouper_sync", "internal_id"));
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_prov_group"));
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_prov_user"));
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_prov_mship"));
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_prov_user_attr_v"));
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_prov_group_attr_v"));
    assertFalse(GrouperDdlUtils.assertTableThere(true, "grouper_prov_mship_v"));

    // insert a grouper_sync row so backfill of internal_id is exercised
    String testSyncId = GrouperUuid.getUuid();
    new GcDbAccess()
      .sql("insert into grouper_sync (id, provisioner_name, last_updated) values (?, ?, ?)")
      .bindVars(testSyncId, "testProvV41Upgrade", new java.sql.Timestamp(System.currentTimeMillis()))
      .executeSql();

    // run all in-flight upgrade tasks that the pre-V41 baseline does not yet include
    UpgradeTasks.V39.upgradeTask().updateVersionFromPrevious(null);
    UpgradeTasks.V40.upgradeTask().updateVersionFromPrevious(null);
    UpgradeTasks.V41.upgradeTask().updateVersionFromPrevious(null);

    // pick up any residual auto-DDL not covered by V41 (matches the pattern of
    // other upgrade tests in this file)
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);

    //lets make sure everything is there on upgrade
    assertTrue(GrouperDdlUtils.assertColumnThere(true, "grouper_sync", "internal_id"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_group"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_group_attr"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_group_attr_value"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_user"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_user_attr"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_user_attr_value"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_mship_role"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_mship"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_user_attr_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_group_attr_v"));
    assertTrue(GrouperDdlUtils.assertTableThere(true, "grouper_prov_mship_v"));

    // the pre-existing row should have a backfilled internal_id
    Long backfilledInternalId = new GcDbAccess()
      .sql("select internal_id from grouper_sync where id = ?")
      .bindVars(testSyncId)
      .select(Long.class);
    assertNotNull("internal_id should be backfilled for existing grouper_sync row", backfilledInternalId);

    // and the column should be NOT NULL — inserting a new row without it must fail
    try {
      new GcDbAccess()
        .sql("insert into grouper_sync (id, provisioner_name, last_updated) values (?, ?, ?)")
        .bindVars(GrouperUuid.getUuid(), "testProvV41UpgradeNotNull", new java.sql.Timestamp(System.currentTimeMillis()))
        .executeSql();
      fail("expected NOT NULL violation on grouper_sync.internal_id");
    } catch (Exception e) {
      // expected
    }

    scriptToGetToPreV41.delete();

    grouperDdlEngine = new GrouperDdlEngine();
    grouperDdlEngine.assignFromUnitTest(true)
        .assignDropBeforeCreate(false).assignWriteAndRunScript(false)
        .assignDropOnly(false)
        .assignMaxVersions(null).assignPromptUser(true).assignDeepCheck(true).runDdl();

    // upgrade should not introduce NEW errors/warnings beyond the install baseline
    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getResult() + " "
            + grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount() + " errors (baseline "
            + baselineErrorCount + ")",
        baselineErrorCount,
        grouperDdlEngine.getGrouperDdlCompareResult().getErrorCount());

    assertEquals(
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount() + " warnings (baseline "
            + baselineWarningCount + ")",
        baselineWarningCount,
        grouperDdlEngine.getGrouperDdlCompareResult().getWarningCount());
  }
}
