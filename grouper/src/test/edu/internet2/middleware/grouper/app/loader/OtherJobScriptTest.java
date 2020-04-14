/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import java.io.File;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class OtherJobScriptTest extends GrouperTest {

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
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSql.class", "edu.internet2.middleware.grouper.app.loader.OtherJobScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSql.quartzCron", "0 0 0 * * ?");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSql.scriptType", "sql");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.scriptSql.scriptSource", "update grouper_groups set description = 'whatever';$newline$update grouper_stems set description = 'whatever';$newline$commit;");

    OtherJobScript otherJobScript = new OtherJobScript();
    otherJobScript.execute("OTHER_JOB_scriptSql", null);
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem root = StemFinder.findRootStem(grouperSession);
    assertEquals("whatever", root.getDescription());
    
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
