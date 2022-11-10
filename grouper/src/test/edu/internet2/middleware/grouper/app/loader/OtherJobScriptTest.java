/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import java.io.File;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.MembershipSave;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.rules.RuleCheckType;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.rules.RuleThenEnum;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.rules.RuleVeto;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;


/**
 *
 */
public class OtherJobScriptTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new OtherJobScriptTest("testGshScriptMaxExpire"));
  }
  
  public OtherJobScriptTest(String name) {
    super(name);
  }

  /**
   * Test method for {@link edu.internet2.middleware.grouper.app.loader.OtherJobScript#main(java.lang.String[])}.
   */
  public void testMain() {
    GrouperGroovysh.GrouperGroovyResult grouperGroovyResult = GrouperGroovysh.runScript(
        "GrouperSession grouperSession = GrouperSession.startRootSession(); \n new GroupSave(grouperSession).assignName(\"stem1:a\").assignCreateParentStemsIfNotExist(true).save();");
  }
  
  public void testSqlScriptSource() {
    
    GrouperLoader.scheduleJobs();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSql.class", "edu.internet2.middleware.grouper.app.loader.OtherJobScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSql.quartzCron", "0 0 0 * * ?");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSql.scriptType", "sql");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSql.scriptSource", "update grouper_groups set description = 'whatever';$newline$update grouper_stems set description = 'whatever';$newline$commit;");

    assertEquals(1, GrouperLoader.scheduleJobs());
    assertEquals(0, GrouperLoader.scheduleJobs());
    
    OtherJobScript otherJobScript = new OtherJobScript();
    otherJobScript.execute("OTHER_JOB_scriptSql", null);
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem root = StemFinder.findRootStem(grouperSession);
    assertEquals("whatever", root.getDescription());
    
  }

  public void testGshScriptMaxExpire() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    long date366 = System.currentTimeMillis()+ 366*24*60*60*1000L;
    long date364 = System.currentTimeMillis()+ 364*24*60*60*1000L;

    GrouperLoader.scheduleJobs();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.testGroupMaxMembership.class", "edu.internet2.middleware.grouper.app.loader.OtherJobScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.testGroupMaxMembership.quartzCron", "0 0 0 * * ?");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.testGroupMaxMembership.scriptType", "gsh");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.testGroupMaxMembership.scriptSource", 
        GrouperUtil.readResourceIntoString("/edu/internet2/middleware/grouper/app/loader/maxMembershipDeleteDate.txt", false));

    assertEquals(1, GrouperLoader.scheduleJobs());
    assertEquals(0, GrouperLoader.scheduleJobs());
    
    new MembershipSave().assignGroup(testGroup).assignSubjectId(SubjectTestHelper.SUBJ0_ID)
      .assignImmediateMshipDisabledTime(date364).save();
    new MembershipSave().assignGroup(testGroup).assignSubjectId(SubjectTestHelper.SUBJ1_ID)
      .assignImmediateMshipDisabledTime(date366).save();
    new MembershipSave().assignGroup(testGroup).assignSubjectId(SubjectTestHelper.SUBJ2_ID)
      .assignImmediateMshipDisabledTime(date366).save();
    new MembershipSave().assignGroup(testGroup).assignSubjectId(SubjectTestHelper.SUBJ3_ID).save();

    OtherJobScript otherJobScript = new OtherJobScript();
    otherJobScript.execute("OTHER_JOB_testGroupMaxMembership", null);

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog("OTHER_JOB_testGroupMaxMembership");
    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
    assertEquals(3, hib3GrouperLoaderLog.getUpdateCount().intValue());
    
    Membership membership = MembershipFinder.findImmediateMembership(grouperSession, testGroup, SubjectTestHelper.SUBJ0, true);
    assertEquals(date364, membership.getDisabledTimeDb().longValue());
    
    membership = MembershipFinder.findImmediateMembership(grouperSession, testGroup, SubjectTestHelper.SUBJ1, true);
    assertTrue(date364 < membership.getDisabledTimeDb().longValue() && membership.getDisabledTimeDb().longValue() < date366 );

    membership = MembershipFinder.findImmediateMembership(grouperSession, testGroup, SubjectTestHelper.SUBJ2, true);
    assertTrue(date364 < membership.getDisabledTimeDb().longValue() && membership.getDisabledTimeDb().longValue() < date366 );

    membership = MembershipFinder.findImmediateMembership(grouperSession, testGroup, SubjectTestHelper.SUBJ3, true);
    assertTrue(date364 < membership.getDisabledTimeDb().longValue() && membership.getDisabledTimeDb().longValue() < date366 );
    
  }
  
  public void testGshScriptSource() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptGsh.class", "edu.internet2.middleware.grouper.app.loader.OtherJobScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptGsh.quartzCron", "0 0 0 * * ?");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptGsh.scriptType", "gsh");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptGsh.scriptSource", "GrouperSession grouperSession = GrouperSession.startRootSession();$newline$new GroupSave(grouperSession).assignName(\"stem1:a\").assignCreateParentStemsIfNotExist(true).save();");

    OtherJobScript otherJobScript = new OtherJobScript();
    otherJobScript.execute("OTHER_JOB_scriptGsh", null);
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = GroupFinder.findByName(grouperSession, "stem1:a", true);
    
    assertNotNull(group);
    
  }

  public void testGshScriptFile() {
    
    File scriptFile = new File(GrouperUtil.tmpDir(true) + "someFile.gsh");
    GrouperUtil.deleteFile(scriptFile);
    
    GrouperUtil.saveStringIntoFile(scriptFile, "GrouperSession grouperSession = GrouperSession.startRootSession();\nnew GroupSave(grouperSession).assignName(\"stem1:a\").assignCreateParentStemsIfNotExist(true).save();");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptGshFile.class", "edu.internet2.middleware.grouper.app.loader.OtherJobScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptGshFile.quartzCron", "0 0 0 * * ?");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptGshFile.scriptType", "gsh");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptGshFile.fileName", scriptFile.getAbsolutePath());

    OtherJobScript otherJobScript = new OtherJobScript();
    otherJobScript.execute("OTHER_JOB_scriptGshFile", null);
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = GroupFinder.findByName(grouperSession, "stem1:a", true);
    
    assertNotNull(group);
    
  }


  public void testSqlScriptFile() {
    
    File scriptFile = new File(GrouperUtil.tmpDir(true) + "someFile.sql");
    GrouperUtil.deleteFile(scriptFile);
    
    GrouperUtil.saveStringIntoFile(scriptFile, "update grouper_groups set description = 'whatever2';\nupdate grouper_stems set description = 'whatever2';\ncommit;");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSqlFile.class", "edu.internet2.middleware.grouper.app.loader.OtherJobScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSqlFile.quartzCron", "0 0 0 * * ?");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSqlFile.scriptType", "sql");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSqlFile.fileName", scriptFile.getAbsolutePath());

    OtherJobScript otherJobScript = new OtherJobScript();
    otherJobScript.execute("OTHER_JOB_scriptSqlFile", null);
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem root = StemFinder.findRootStem(grouperSession);
    assertEquals("whatever2", root.getDescription());
    
  }



}
