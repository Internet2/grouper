package edu.internet2.middleware.grouper.app.ldapToSql;

import java.sql.Types;
import java.util.List;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;


public class LdapToSqlSyncDaemonTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LdapToSqlSyncDaemonTest("testExecuteJobExecutionContextMultivalued"));
//    LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
//    LdapProvisionerTestUtils.startLdapContainer();
  }

  public LdapToSqlSyncDaemonTest(String name) {
    super(name);
  }

  public void testExecuteJobExecutionContextMultivalued() {

    createTablesIfNotThere();
    
    // create a table
//    final String tableName = "testgrouper_ldapsync";
//
//
//    try {
//      // if you cant connrc to it, its not there
//      HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
//      try {
//        HibernateSession.bySqlStatic().executeSql("drop table " + tableName);
//      } catch (Exception e) {
//      }
//      try {
//        // if you cant connrc to it, its not there
//        HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
//        throw new RuntimeException("Cant drop table: '" + tableName + "'");
//      } catch (Exception e) {
//        return;
//      }
//    } catch (Exception e) {
//    }
//
//    try {
//      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
//    } catch (Exception e) {
//
//      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
//        public void changeDatabase(DdlVersionBean ddlVersionBean) {
//
//          Database database = ddlVersionBean.getDatabase();
//
//          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
//          
//          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "the_dn", Types.VARCHAR, "200", true, true);
//          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "cn", Types.VARCHAR, "200", false, false);
//          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "the_date", Types.DATE, null, false, false);
//        }
//      });
//    }
//
//    
//
//    GrouperSession grouperSession = GrouperSession.startRootSession();
//
//    LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
//    LdapProvisionerTestUtils.startLdapContainer();
    
    //TODO
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.class", "edu.internet2.middleware.grouper.app.ldapToSql.LdapToSqlSyncDaemon");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.0.ldapName", "dn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.0.sqlColumn", "the_dn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.0.uniqueKey", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.1.ldapName", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.1.sqlColumn", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.2.sqlColumn", "the_date");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.2.translation", "${'2015-04-22 00:00:00.0'}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlDbConnection", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlFilter", "(uid=aanderson*)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlLdapConnection", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlNumberOfAttributes", "3");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlSearchScope", "SUBTREE_SCOPE");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlTableName", "testgrouper_ldapsync");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.quartzCron", "0 03 5 * * ?");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlHasMultiValuedTable", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlMultiValuedTableName", "testgrouper_ldapsync_attr");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlIdColumn", "the_dn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlMultiValuedAttributes", "objectClass, uid");
    
    HibernateSession.bySqlStatic().executeSql("delete from testgrouper_ldapsync_attr");
    HibernateSession.bySqlStatic().executeSql("delete from testgrouper_ldapsync");
    
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_ldapToSqlTest");

    assertEquals(14, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("insertsCount")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("deletesCount")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("updatesCount")));
    assertEquals(2, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("ldapRecords")));
    assertEquals(12, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("ldapMultiValuedAttributes")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("dbUniqueKeys")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("dbMultiValuedAttributes")));
    
    // check database
    List<Object[]> databaseRecords = new GcDbAccess().sql("select the_dn, cn, the_date from testgrouper_ldapsync order by 1").selectList(Object[].class);
    assertEquals(2, databaseRecords.size());
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseRecords.get(0)[0]);
    assertEquals("aanderson", databaseRecords.get(0)[1]);
    assertEquals(GrouperUtil.dateValue("20150422"), databaseRecords.get(0)[2]);
    assertEquals("uid=aanderson727,ou=People,dc=example,dc=edu", databaseRecords.get(1)[0]);
    assertEquals("aanderson727", databaseRecords.get(1)[1]);
    assertEquals(GrouperUtil.dateValue("20150422"), databaseRecords.get(1)[2]);

    List<Object[]> databaseAttributes = new GcDbAccess().sql("select ldap_id, attribute_name, attribute_value from testgrouper_ldapsync_attr order by 1, 2, 3").selectList(Object[].class);
    assertEquals(12, databaseAttributes.size());
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseAttributes.get(0)[0]);
    assertEquals("objectClass", databaseAttributes.get(0)[1]);
    assertEquals("eduPerson", databaseAttributes.get(0)[2]);
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseAttributes.get(1)[0]);
    assertEquals("objectClass", databaseAttributes.get(1)[1]);
    assertEquals("inetOrgPerson", databaseAttributes.get(1)[2]);
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseAttributes.get(2)[0]);
    assertEquals("objectClass", databaseAttributes.get(2)[1]);
    assertEquals("organizationalPerson", databaseAttributes.get(2)[2]);
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseAttributes.get(3)[0]);
    assertEquals("objectClass", databaseAttributes.get(3)[1]);
    assertEquals("person", databaseAttributes.get(3)[2]);
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseAttributes.get(4)[0]);
    assertEquals("objectClass", databaseAttributes.get(4)[1]);
    assertEquals("top", databaseAttributes.get(4)[2]);
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseAttributes.get(5)[0]);
    assertEquals("uid", databaseAttributes.get(5)[1]);
    assertEquals("aanderson", databaseAttributes.get(5)[2]);

    assertEquals("uid=aanderson727,ou=People,dc=example,dc=edu", databaseAttributes.get(6)[0]);
    assertEquals("objectClass", databaseAttributes.get(6)[1]);
    assertEquals("eduPerson", databaseAttributes.get(6)[2]);
    assertEquals("uid=aanderson727,ou=People,dc=example,dc=edu", databaseAttributes.get(7)[0]);
    assertEquals("objectClass", databaseAttributes.get(7)[1]);
    assertEquals("inetOrgPerson", databaseAttributes.get(7)[2]);
    assertEquals("uid=aanderson727,ou=People,dc=example,dc=edu", databaseAttributes.get(8)[0]);
    assertEquals("objectClass", databaseAttributes.get(8)[1]);
    assertEquals("organizationalPerson", databaseAttributes.get(8)[2]);
    assertEquals("uid=aanderson727,ou=People,dc=example,dc=edu", databaseAttributes.get(9)[0]);
    assertEquals("objectClass", databaseAttributes.get(9)[1]);
    assertEquals("person", databaseAttributes.get(9)[2]);
    assertEquals("uid=aanderson727,ou=People,dc=example,dc=edu", databaseAttributes.get(10)[0]);
    assertEquals("objectClass", databaseAttributes.get(10)[1]);
    assertEquals("top", databaseAttributes.get(10)[2]);
    assertEquals("uid=aanderson727,ou=People,dc=example,dc=edu", databaseAttributes.get(11)[0]);
    assertEquals("uid", databaseAttributes.get(11)[1]);
    assertEquals("aanderson727", databaseAttributes.get(11)[2]);

    
    // run again with no changes
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_ldapToSqlTest");

    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("insertsCount")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("deletesCount")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("updatesCount")));
    assertEquals(2, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("ldapRecords")));
    assertEquals(2, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("dbUniqueKeys")));
    assertEquals(12, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("dbMultiValuedAttributes")));
    
    // check database
    databaseRecords = new GcDbAccess().sql("select the_dn, cn, the_date from testgrouper_ldapsync order by 1").selectList(Object[].class);
    assertEquals(2, databaseRecords.size());
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseRecords.get(0)[0]);
    assertEquals("aanderson", databaseRecords.get(0)[1]);
    assertEquals(GrouperUtil.dateValue("20150422"), databaseRecords.get(0)[2]);
    assertEquals("uid=aanderson727,ou=People,dc=example,dc=edu", databaseRecords.get(1)[0]);
    assertEquals("aanderson727", databaseRecords.get(1)[1]);
    assertEquals(GrouperUtil.dateValue("20150422"), databaseRecords.get(1)[2]);

    //update something
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlFilter", "(uid=aanderson)");

    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_ldapToSqlTest");

    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("insertsCount")));
    assertEquals(7, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("deletesCount")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("updatesCount")));
    assertEquals(1, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("ldapRecords")));
    assertEquals(2, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("dbUniqueKeys")));
    assertEquals(12, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("dbMultiValuedAttributes")));
    
    // check database
    databaseRecords = new GcDbAccess().sql("select the_dn, cn, the_date from testgrouper_ldapsync order by 1").selectList(Object[].class);
    assertEquals(1, databaseRecords.size());
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseRecords.get(0)[0]);
    assertEquals("aanderson", databaseRecords.get(0)[1]);
    assertEquals(GrouperUtil.dateValue("20150422"), databaseRecords.get(0)[2]);

    databaseAttributes = new GcDbAccess().sql("select ldap_id, attribute_name, attribute_value from testgrouper_ldapsync_attr order by 1, 2, 3").selectList(Object[].class);
    assertEquals(6, databaseAttributes.size());
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseAttributes.get(0)[0]);
    assertEquals("objectClass", databaseAttributes.get(0)[1]);
    assertEquals("eduPerson", databaseAttributes.get(0)[2]);
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseAttributes.get(1)[0]);
    assertEquals("objectClass", databaseAttributes.get(1)[1]);
    assertEquals("inetOrgPerson", databaseAttributes.get(1)[2]);
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseAttributes.get(2)[0]);
    assertEquals("objectClass", databaseAttributes.get(2)[1]);
    assertEquals("organizationalPerson", databaseAttributes.get(2)[2]);
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseAttributes.get(3)[0]);
    assertEquals("objectClass", databaseAttributes.get(3)[1]);
    assertEquals("person", databaseAttributes.get(3)[2]);
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseAttributes.get(4)[0]);
    assertEquals("objectClass", databaseAttributes.get(4)[1]);
    assertEquals("top", databaseAttributes.get(4)[2]);
    assertEquals("uid=aanderson,ou=People,dc=example,dc=edu", databaseAttributes.get(5)[0]);
    assertEquals("uid", databaseAttributes.get(5)[1]);
    assertEquals("aanderson", databaseAttributes.get(5)[2]);


    // delete
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlFilter", "(objectClass=groupOfUniqueNames2)");

    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_ldapToSqlTest");

    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("insertsCount")));
    assertEquals(7, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("deletesCount")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("updatesCount")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("ldapRecords")));
    assertEquals(1, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("dbUniqueKeys")));
    assertEquals(6, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("dbMultiValuedAttributes")));
    
    // check database
    databaseRecords = new GcDbAccess().sql("select the_dn, cn, the_date from testgrouper_ldapsync").selectList(Object[].class);
    assertEquals(0, databaseRecords.size());

    databaseAttributes = new GcDbAccess().sql("select ldap_id, attribute_name, attribute_value from testgrouper_ldapsync_attr order by 1, 2, 3").selectList(Object[].class);
    assertEquals(0, databaseAttributes.size());

  }

  public void testExecuteJobExecutionContext() {

    createTablesIfNotThere();
    GrouperSession grouperSession = GrouperSession.startRootSession();

    LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
    LdapProvisionerTestUtils.startLdapContainer();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.class", "edu.internet2.middleware.grouper.app.ldapToSql.LdapToSqlSyncDaemon");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.0.ldapName", "dn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.0.sqlColumn", "the_dn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.0.uniqueKey", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.1.ldapName", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.1.sqlColumn", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.2.sqlColumn", "the_date");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.2.translation", "${'2015-04-22 00:00:00.0'}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlDbConnection", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlFilter", "(objectClass=groupOfUniqueNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlLdapConnection", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlNumberOfAttributes", "3");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlSearchScope", "SUBTREE_SCOPE");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlTableName", "testgrouper_ldapsync");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.quartzCron", "0 03 5 * * ?");

    HibernateSession.bySqlStatic().executeSql("delete from testgrouper_ldapsync");
    
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_ldapToSqlTest");

    assertEquals(1, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("insertsCount")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("deletesCount")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("updatesCount")));
    assertEquals(1, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("ldapRecords")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("dbUniqueKeys")));
    
    // check database
    List<Object[]> databaseRecords = new GcDbAccess().sql("select the_dn, cn, the_date from testgrouper_ldapsync").selectList(Object[].class);
    assertEquals(1, databaseRecords.size());
    assertEquals("cn=users,ou=Groups,dc=example,dc=edu", databaseRecords.get(0)[0]);
    assertEquals("users", databaseRecords.get(0)[1]);
    assertEquals(GrouperUtil.dateValue("20150422"), databaseRecords.get(0)[2]);

    // run again with no changes
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_ldapToSqlTest");

    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("insertsCount")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("deletesCount")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("updatesCount")));
    assertEquals(1, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("ldapRecords")));
    assertEquals(1, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("dbUniqueKeys")));
    
    // check database
    databaseRecords = new GcDbAccess().sql("select the_dn, cn, the_date from testgrouper_ldapsync").selectList(Object[].class);
    assertEquals(1, databaseRecords.size());
    assertEquals("cn=users,ou=Groups,dc=example,dc=edu", databaseRecords.get(0)[0]);
    assertEquals("users", databaseRecords.get(0)[1]);
    assertEquals(GrouperUtil.dateValue("20150422"), databaseRecords.get(0)[2]);

    //update something
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlAttribute.2.translation", "${'2015-04-23 00:00:00.0'}");

    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_ldapToSqlTest");

    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("insertsCount")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("deletesCount")));
    assertEquals(1, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("updatesCount")));
    assertEquals(1, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("ldapRecords")));
    assertEquals(1, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("dbUniqueKeys")));
    
    // check database
    databaseRecords = new GcDbAccess().sql("select the_dn, cn, the_date from testgrouper_ldapsync").selectList(Object[].class);
    assertEquals(1, databaseRecords.size());
    assertEquals("cn=users,ou=Groups,dc=example,dc=edu", databaseRecords.get(0)[0]);
    assertEquals("users", databaseRecords.get(0)[1]);
    assertEquals(GrouperUtil.dateValue("20150423"), databaseRecords.get(0)[2]);

    // delete
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlTest.ldapSqlFilter", "(objectClass=groupOfUniqueNames2)");

    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_ldapToSqlTest");

    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("insertsCount")));
    assertEquals(1, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("deletesCount")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("updatesCount")));
    assertEquals(0, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("ldapRecords")));
    assertEquals(1, GrouperUtil.intValue(LdapToSqlSyncDaemon.internalTestLastDebugMap.get("dbUniqueKeys")));
    
    // check database
    databaseRecords = new GcDbAccess().sql("select the_dn, cn, the_date from testgrouper_ldapsync").selectList(Object[].class);
    assertEquals(0, databaseRecords.size());
  }

  private void createTablesIfNotThere() {
    // create a table
    final String tableName = "testgrouper_ldapsync";


//    try {
//      // if you cant connrc to it, its not there
//      HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
//      try {
//        HibernateSession.bySqlStatic().executeSql("drop table " + tableName);
//      } catch (Exception e) {
//      }
//      try {
//        // if you cant connrc to it, its not there
//        HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
//        throw new RuntimeException("Cant drop table: '" + tableName + "'");
//      } catch (Exception e) {
//        return;
//      }
//    } catch (Exception e) {
//    }

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {

      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();

          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "the_dn", Types.VARCHAR, "200", true, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "cn", Types.VARCHAR, "200", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "the_date", Types.DATE, null, false, false);
        }
      });
    }
    
    // create a table
    final String tableName2 = "testgrouper_ldapsync_attr";


//    try {
//      // if you cant connrc to it, its not there
//      HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName2);
//      try {
//        HibernateSession.bySqlStatic().executeSql("drop table " + tableName2);
//      } catch (Exception e) {
//      }
//      try {
//        // if you cant connrc to it, its not there
//        HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName2);
//        throw new RuntimeException("Cant drop table: '" + tableName2 + "'");
//      } catch (Exception e) {
//        return;
//      }
//    } catch (Exception e) {
//    }

    try {
      new GcDbAccess().sql("select count(*) from " + tableName2).select(int.class);
    } catch (Exception e) {

      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();

          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName2);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "ldap_id", Types.VARCHAR, "200", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "attribute_name", Types.VARCHAR, "200", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "attribute_value", Types.VARCHAR, "200", false, false);
        }
      });
    }

  }

}
