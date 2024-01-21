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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.helper;
import static org.junit.Assert.fail;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.syncToGrouper.SyncToGrouper;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperShutdown;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.plugins.GrouperPluginManager;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.registry.RegistryInitializeSchema;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.testing.GrouperTestBase;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfigInApi;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * Grouper-specific JUnit assertions.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperTest.java,v 1.3 2009-12-10 08:54:15 mchyzer Exp $
 * @since   1.1.0
 */
public class GrouperTest extends GrouperTestBase {

  public static boolean isTesting() {
    return testing;
  }

  // PRIVATE CLASS CONSTANTS //
  private static final Log    LOG = GrouperUtil.getLog(GrouperTest.class);

  /**
   * make sure enough memory to run tests
   */
  public static void assertEnoughMemory() {
    if (Runtime.getRuntime().maxMemory() < 400000000) {
      throw new RuntimeException("Not enough memory, you should have at least 500 megs, e.g. -Xms80m -Xmx640m, but this much was detected: " + Runtime.getRuntime().maxMemory());
    }
  }
  
  /**
   * @since   1.2.0
   */
  public GrouperTest() {
    super();
    
    testing = true;
    
    //I believe this needs to be here before Grouper starts up
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.autoCreateSource", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubject.sourceId", "grouperExternal");

    //let the database release...
    GrouperStartup.startup();
  } // public GrouperTest()

  /** 
   * @since   1.1.0
   */
  public GrouperTest(String name) {
    super(name);
    testing = true;

    //let the database release...
    GrouperStartup.startup();
  } // public GrouperTest()


  /**
   * Return consistent test initialization error message.
   * @since   1.2.0
   */
  protected void errorInitializingTest(Exception e) {
    fail( "ERROR INITIALIZING TEST: " + e.getMessage() );
  }

  /**
   * override this method to configure the configs...
   */
  protected void setupConfigs() {
    
  }
  
  /**
   * 
   */
  private static Thread shutdownDelayThread = new Thread(new Runnable() {

    @Override
    public void run() {

      // dont call GrouperUtil.sleep since it will print stack on interruption
      // delay in case another test started up and we dont want Grouper to shot down
      try {
        Thread.sleep(5000);
        
      } catch (InterruptedException ie) {
        // this means another test started
        return;
      }
      
      GrouperShutdown.shutdown();
      
    }
    
  });
  
  /**
   * if prompted user to see if db ok to make changes
   */
  private static boolean promptedUserToSeeIfOk = false;
  
  public static void shutdownDelayThreadInterrupt() {
    
    try {
      if (shutdownDelayThread.isAlive()) {
        shutdownDelayThread.interrupt();
      }
    } catch (Throwable e) {
      //ignore
    }
  }
  
  // @since   1.2.0
  protected void setUp () {
    
    LOG.debug("setUp");
    
    shutdownDelayThreadInterrupt();

    GrouperCacheUtils.clearAllCaches();
    SourceManager.clearAllSources();
    
    GrouperSession.stopQuietly(GrouperSession.staticGrouperSession(false));
    GrouperSession.clearGrouperSessions();
    
    //set this and leave it...
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.JUNIT, false, true);
    
    GrouperProvisioner.setTest_saveLastProvisionerInStaticVariable(true);
    
    if (!promptedUserToSeeIfOk) {
      GrouperUtil.promptUserAboutDbChanges("delete all data in the database to run junit test(s)", true);
    }
    
    //remove any settings in testconfig
    GrouperConfig.retrieveConfig().propertiesOverrideMap().clear();
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().clear();
    GrouperHibernateConfig.retrieveConfig().propertiesOverrideMap().clear();
    GrouperWsConfigInApi.retrieveConfig().propertiesOverrideMap().clear();
    GrouperUiConfigInApi.retrieveConfig().propertiesOverrideMap().clear();
    SubjectConfig.retrieveConfig().propertiesOverrideMap().clear();
    ConfigPropertiesCascadeBase.clearCache();

    SourceManager.getInstance().reloadSource("personLdapSource");

    SubjectFinder.internalClearSubjectCustomizerCache();

    for (int i=0;i<20;i++) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.group.name." + i, null);
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.group.description." + i, null);
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.group.subjects." + i, null);
    }

    //set grouper.example.properties stuff...
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.admin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.optin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.optout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.update", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "true");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.viewonly.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.viewonly.group", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.readonly.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.readonly.group", "false");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.create.grant.all.create", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.create.grant.all.stem", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrAdmin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrUpdate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("entities.create.grant.all.view", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", "etc:sysadmingroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subject.internal.grouperall.name", "EveryEntity");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subject.internal.groupersystem.name", "GrouperSysAdmin");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.searchAttribute0.el", "${subject.name},${subject.id}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.sortAttribute0.el", "${subject.name}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.types.grouperLoader.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.types.grouperGroupMembershipSettings.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.stem.groupAllowedToMoveStem", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.stem.groupAllowedToRenameStem", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.stem.groupAllowedToCopyStem", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("member.search.defaultIndexOrder", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("member.sort.defaultIndexOrder", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subjects.allPage.useThreadForkJoin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subjects.idOrIdentifier.useThreadForkJoin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subjects.group.useCreatorAndModifierAsSubjectAttributes", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subjects.customizer.className", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subjects.startRootSessionIfOneIsntStarted", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.attribute.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.attributeDef.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.attributeDefName.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.attributeAssign.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.attributeAssignValue.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.group.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.lifecycle.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.membership.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.member.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.stem.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.composite.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.field.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.grouperSession.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.groupType.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.groupTypeTuple.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.loader.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.externalSubject.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.act.as.group", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.accessToApiInEl.group", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.act.as.cache.minutes", "30");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.customElClasses", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.allowActAsGrouperSystemForInheritedStemPrivileges", null); 
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.emailTemplatesFolder", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("group.attribute.validator.attributeName.0", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("group.attribute.validator.regex.0", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("group.attribute.validator.vetoMessage.0", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.api.readonly", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("membershipUpdateLiteTypeAutoCreate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.attribute.rootStem", "etc:attribute");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.attribute.loader.autoconfigure", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.permissions.limits.builtin.createAs.public", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ddlutils.use.nestedTransactions", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.use.builtin.messaging", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.default.name.of.messaging.system", "grouperBuiltinMessaging");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.autoadd.typesAttributes", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.sqlTable.likeString.removeGroupIfMemberOfAnotherGroup", "false");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.changeLogTempToChangeLog.longRunning", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.compositeMemberships.longRunning", "false");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("default.subject.source.id", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.system.groups", "true");
    
    //dont send emails
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("mail.smtp.server", "testing");

    setupInitDb();

    GrouperEmail.testingEmails().clear();
    GrouperEmail.testingEmailCount = 0;
    GrouperCacheUtils.clearAllCaches();
    
    
    SyncToGrouper.reclaimMemory = false;


  }

  protected void setupInitDb() {
    setupConfigs();
    
    GrouperHooksUtils.reloadHooks();
    
    RegistryReset.internal_resetRegistryAndAddTestSubjects();

    // clear config cache
    GrouperCacheUtils.clearAllCaches();

    setupConfigsPostClearCache();


    GrouperTest.initGroupsAndAttributes();
    
  }
  
  protected void setupConfigsPostClearCache() {
    
    
  }


  /**
   * if printed warning
   */
  private static boolean tomcatPrintedWarning = false;
  
  /**
   * see if running tomcat test
   * @return if continue
   */
  public static boolean tomcatRunTests() {
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat", false)) {
      if (!tomcatPrintedWarning) {
        System.out.println("Not running tomcat tests since grouper.properties: junit.test.tomcat=false");
        tomcatPrintedWarning = true;
      }
      return false;
    }

    return true;
  }
  
  /**
   * start tomcat and wait for port listening
   */
  public static CommandLineExec tomcatStart() {
    
    String startCommand = GrouperConfig.retrieveConfig().propertyValueStringRequired("junit.test.tomcat.startCommand");
    if (StringUtils.equals(startCommand, "none")) {
      System.out.println("Start tomcat...");
      GrouperUtil.sleep(10000);
      return null;
    }
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    String ipAddress = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.ipAddress", "0.0.0.0");
    Boolean waitForProcessReturn = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.waitForProcessReturn");
    
    if (waitForProcessReturn == null) {
      waitForProcessReturn = !GrouperUtil.isWindows();
    }
    
    // if its listening on port now, shouldnt be
    if (!GrouperUtil.portAvailable(port, ipAddress)) {
      tomcatStop();
    }

    CommandLineExec commandLineExec = new CommandLineExec().assignCommand(startCommand).assignErrorOnNonZero(true);
    commandLineExec.assignWaitForCompletion(waitForProcessReturn);
    commandLineExec.execute();
    boolean success = GrouperUtil.portAvailableWait(port, ipAddress, 180, true);
    GrouperUtil.assertion(success, "Cannot stop tomcat on ip address: " + ipAddress + ", port: " + port);
    return commandLineExec;
  }
  
  /**
   * stop tomcat and wait for port not listening
   */
  public static void tomcatStop() {
    
    String stopCommand = GrouperConfig.retrieveConfig().propertyValueStringRequired("junit.test.tomcat.stopCommand");
    if (StringUtils.equals(stopCommand, "none")) {
      System.out.println("Stop tomcat...");
      GrouperUtil.sleep(10000);
      return;
    }

    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    String ipAddress = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.ipAddress", "0.0.0.0");
    
    // if its listening on port now, shouldnt be
    if (!GrouperUtil.portAvailable(port, ipAddress)) {
      CommandLineExec commandLineExec = new CommandLineExec().assignCommand(stopCommand).assignErrorOnNonZero(true);
      commandLineExec.execute();
      System.out.println(commandLineExec.getStdout().getAllLines());
      boolean success = GrouperUtil.portAvailableWait(port, ipAddress, 180, false);
      GrouperUtil.assertion(success, "Cannot stop tomcat on ip address: " + ipAddress + ", port: " + port);
    }

  }
  
  
  /**
   * init groups and attributes after reset
   */
  public static void initGroupsAndAttributes() {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
      @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
  
          GrouperCheckConfig.checkObjects();
          return null;
        }
      });
    }

  // @since   1.2.0
  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperLoader.shutdownIfStarted();
    GrouperPluginManager.shutdownIfStarted();
    if (!shutdownDelayThread.isAlive()) {
      try {
        shutdownDelayThread.start();
      } catch (Throwable t) {
        
      }
    }
  } 



  /**
   * 
   */
  public void setupTestConfigForIncludeExclude() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.use", "true");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.type.name", "addIncludeExclude");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.type.name", "requireInGroups");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.tooltip", "Select this type to auto-create other groups which facilitate having include and exclude list, and setting up group math so that other groups can be required (e.g. activeEmployee)");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.attributeName", "requireAlsoInGroups");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.tooltip", "Enter in comma separated group path(s).  An entity must be in these groups for it to be in the overall group.  e.g. stem1:stem2:group1, stem1:stem3:group2");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.systemOfRecord.extension.suffix", "_systemOfRecord");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.include.extension.suffix", "_includes");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.exclude.extension.suffix", "_excludes");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.systemOfRecordAndIncludes.extension.suffix", "_systemOfRecordAndIncludes");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.includesMinusExcludes.extension.suffix", "_includesMinusExcludes");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.extension.suffix", "_requireGroups${i}");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.systemOfRecord.displayExtension.suffix", "${space}system of record");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.include.displayExtension.suffix", "${space}includes");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.exclude.displayExtension.suffix", "${space}excludes");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.systemOfRecordAndIncludes.displayExtension.suffix", "${space}system of record and includes");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.includesMinusExcludes.displayExtension.suffix", "${space}includes minus excludes");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.displayExtension.suffix", "${space}includes minus exludes minus andGroup${i}");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.overall.description", "Group containing list of ${displayExtension} after adding the includes and subtracting the excludes");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.systemOfRecord.description", "Group containing list of ${displayExtension} (generally straight from the system of record) without yet considering manual include or exclude lists");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.include.description", "Group containing manual list of includes for group ${displayExtension} which will be added to the system of record list (unless the subject is also in the excludes group)");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.exclude.description", "Group containing manual list of excludes for group ${displayExtension} which will not be in the overall group");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.systemOfRecordAndIncludes.description", "Internal utility group for group ${displayExtension} which facilitates the group math for the include and exclude lists");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.includesMinusExclude.description", "Internal utility group for group ${displayExtension} which facilitates includes, excludes, and required groups (e.g. activeEmployee)");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.description", "Internal utility group for group ${displayExtension} which facilitates includes, excludes, and required groups (e.g. activeEmployee)");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.name.0", "requireActiveEmployee");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.attributeOrType.0", "attribute");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.group.0", "aStem:activeEmployee");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.description.0", "If value is true, members of the overall group must be an active employee (in the aStem:activeEmployee group).  Otherwise, leave this value not filled in.");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.name.1", "requireActiveStudent");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.attributeOrType.1", "type");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.group.1", "aStem:activeStudent");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.description.1", "If value is true, members of the overall group must be an active student (in the aStem:activeStudent group).  Otherwise, leave this value not filled in.");
  }

  /** see if we are testing */
  public static boolean testing = false;

  /**
   * 
   */
  public static void setupTests() {
    //dont keep prompting user about DB
    GrouperUtil.stopPromptingUser = true;
    GrouperDdlUtils.internal_printDdlUpdateMessage = false;
    RegistryInitializeSchema.initializeSchemaForTests();
    GrouperDdlUtils.internal_printDdlUpdateMessage = true;
  }

  /**
   * concat to stem name full
   * @param names
   * @param length
   * @return stem name based on array and length
   */
  public static String stemName(String[] names, int length) {
    StringBuilder result = new StringBuilder();
    for (int i=0;i<length;i++) {
      result.append(names[i]);
      if (i<length-1) {
        result.append(":");
      }
    }
    return result.toString();
  }

  /**
   * helper method to delete stems if exist
   * @param grouperSession
   * @param name
   * @throws Exception 
   */
  public static void deleteAllStemsIfExists(GrouperSession grouperSession, String name) throws Exception {
    //this isnt good, it exists
    String[] stems = StringUtils.split(name, ':');
    Stem currentStem = null;
    for (int i=stems.length-1;i>-0;i--) {
      String currentName = GrouperTest.stemName(stems, i+1);
      try {
        currentStem = StemFinder.findByName(grouperSession, currentName, true);
      } catch (StemNotFoundException snfe1) {
        continue;
      }
      currentStem.delete();
    }
    
  }

  public static void runCompositeMembershipChangeLogConsumer() {
    int count = 0;
    while (true) {
      int numberOfChanges = ChangeLogTempToEntity.convertRecords();
      
      if (numberOfChanges == 0 && count > 0) {
        break;
      }
      
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
      count++;
    }
  }
}

