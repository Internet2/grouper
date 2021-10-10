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
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperDdl;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils.DbMetadataBean;
import edu.internet2.middleware.grouper.exception.SchemaException;
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
    TestRunner.run(new GrouperDdlUtilsTest("testUpgradeFrom2_5_51To2_6_1ddlUtils"));
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
    GrouperHibernateConfig.retrieveConfig().propertiesOverrideMap().put("registry.auto.ddl.upToVersion", "2.6.*");
    GrouperHibernateConfig.retrieveConfig().propertiesOverrideMap().put("registry.auto.ddl.upToVersion.elConfig", "2.6.*");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    //yes print annoying messages to user again
    GrouperDdlUtils.internal_printDdlUpdateMessage = true;
    GrouperDdlUtils.autoDdl2_5orAbove = null;
    
    // drop everything
    new GrouperDdlEngine().assignCallFromCommandLine(false).assignFromUnitTest(true)
      .assignCompareFromDbVersion(false).assignDropBeforeCreate(true).assignWriteAndRunScript(true).assignDropOnly(true)
      .assignInstallDefaultGrouperData(false).assignMaxVersions(null).assignPromptUser(true)
      .assignFromStartup(false).runDdl();
  
    
    GrouperDdlEngine.addDllWorkerTableIfNeeded(null);
    //first make sure the DB ddl is up to date
    new GrouperDdlEngine().updateDdlIfNeededWithStaticSql(null);
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
  
}
