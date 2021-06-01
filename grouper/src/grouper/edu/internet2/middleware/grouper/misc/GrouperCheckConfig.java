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
 * $Id: GrouperCheckConfig.java,v 1.35 2009-12-10 08:54:15 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_APPROVALS;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_ATTRIBUTE_NAME;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_DEF;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_DESCRIPTION;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_ENABLED;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_FORM;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_ID;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_NAME;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_PARAMS;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_SEND_EMAIL;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_TYPE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_VALUE_DEF;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_VIEWERS_GROUP_ID;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.Job;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAffiliation;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAttributeNames;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAttributeValue;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningConfiguration;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningJob;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningOverallConfiguration;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningSettings;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
//import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningJob;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.NotificationDaemon;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames;
import edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames;
import edu.internet2.middleware.grouper.app.reports.GrouperReportSettings;
import edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperRecentMemberships;
import edu.internet2.middleware.grouper.app.upgradeTasks.UpgradeTasksJob;
import edu.internet2.middleware.grouper.app.usdu.UsduAttributeNames;
import edu.internet2.middleware.grouper.app.usdu.UsduSettings;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceAttributeNames;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowSettings;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.entity.EntityUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttrFramework;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hooks.AttributeAssignHooks;
import edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks;
import edu.internet2.middleware.grouper.hooks.AttributeDefHooks;
import edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks;
import edu.internet2.middleware.grouper.hooks.AttributeHooks;
import edu.internet2.middleware.grouper.hooks.CompositeHooks;
import edu.internet2.middleware.grouper.hooks.ExternalSubjectHooks;
import edu.internet2.middleware.grouper.hooks.FieldHooks;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.GroupTypeHooks;
import edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks;
import edu.internet2.middleware.grouper.hooks.GrouperSessionHooks;
import edu.internet2.middleware.grouper.hooks.LifecycleHooks;
import edu.internet2.middleware.grouper.hooks.LoaderHooks;
import edu.internet2.middleware.grouper.hooks.MemberHooks;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.StemHooks;
import edu.internet2.middleware.grouper.hooks.examples.AttributeAutoCreateHook;
import edu.internet2.middleware.grouper.hooks.examples.AttributeDefNameUniqueNameCaseInsensitiveHook;
import edu.internet2.middleware.grouper.hooks.examples.AttributeDefUniqueNameCaseInsensitiveHook;
import edu.internet2.middleware.grouper.hooks.examples.GroupUniqueNameCaseInsensitiveHook;
import edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook;
import edu.internet2.middleware.grouper.hooks.examples.MembershipOneInFolderMaxHook;
import edu.internet2.middleware.grouper.hooks.examples.StemUniqueNameCaseInsensitiveHook;
import edu.internet2.middleware.grouper.instrumentation.InstrumentationDataUtils;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefDAO;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO;
import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.ui.customUi.CustomUiAttributeNames;
import edu.internet2.middleware.grouper.userData.GrouperUserDataUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.morphString.MorphStringConfig;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectCheckConfig;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 * check the configuration of grouper to make sure things are configured right, and
 * to give descriptive errors of the problems
 */
public class GrouperCheckConfig {

  /**
   * we can delay some config until after started, but maybe some things need to wait for it
   */
  private static boolean doneWithExtraConfig = false;
  
  /**
   * 
   * @return true when done with extra config
   */
  public static boolean isDoneWithExtraconfig() {
    return doneWithExtraConfig;
  }

  /**
   * 
   */
  public static void waitUntilDoneWithExtraConfig() {
    while (!doneWithExtraConfig) {
      GrouperUtil.sleep(1000);
    }
  }
  
  /**
   * 
   */
  public static final String GROUPER_PROPERTIES_NAME = "grouper.properties";

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperCheckConfig.class);
  
  /** result from check group */
  public static enum CheckGroupResult{ 
    /** group didnt exist, and was created */
    DIDNT_CHECK, 

    /** group didnt exist, and was created */
    CREATED, 
    
    /** group doesnt exist */
    DOESNT_EXIST, 
    
    /** group created */
    ERROR_CREATING, 
    
    /** group exists */
    EXISTS };
  
  /**
   * verify that a group exists by name (dont throw exceptions)
   * @param grouperSession (probably should be root session)
   * @param groupName
   * @param logError 
   * @param autoCreate if auto create, or null, for grouper.properties setting
   * @param logAutocreate 
   * @param displayExtension optional, dislpay extension if creating
   * @param groupDescription group description if auto create
   * @param propertyDescription for logging explaning to the user how to fix the problem
   * @param groupResult put in an array of size one to get the group back
   * @return if group exists or not or was created
   */
  public static CheckGroupResult checkGroup(GrouperSession grouperSession, String groupName, 
      boolean logError, Boolean autoCreate, 
      boolean logAutocreate, String displayExtension, String groupDescription, String propertyDescription,
      Group[] groupResult) {

    if (configCheckDisabled()) {
      return CheckGroupResult.DIDNT_CHECK;
    }
    try {
      Group group = GroupFinder.findByName(grouperSession, groupName, true, new QueryOptions().secondLevelCache(false));
      if (group != null) {
        if (GrouperUtil.length(groupResult) >= 1) {
          groupResult[0] = group;
        }
        GroupFinder.groupCacheAsRootAddSystemGroup(group);
        return CheckGroupResult.EXISTS;
      }
    } catch (Exception e) {
      
    }
    
    if (logError) {
      String error = "cannot find group from config: " + propertyDescription + ": " + groupName;
      System.err.println("Grouper warning: " + error);
      LOG.warn(error);
    }
    
    //get auto create from config
    if (autoCreate == null) {
      Properties properties = GrouperConfig.retrieveConfig().properties();
      autoCreate = GrouperUtil.propertiesValueBoolean(properties, GrouperConfig.retrieveConfig().propertiesOverrideMap(), 
          "configuration.autocreate.system.groups", false);
    }
    
    if (autoCreate) {
      try {
        Group group = Group.saveGroup(grouperSession, null, null, groupName, displayExtension, groupDescription, null, true);
        if (GrouperUtil.length(groupResult) >= 1) {
          groupResult[0] = group;
          GroupFinder.groupCacheAsRootAddSystemGroup(group);
        }
        if (logAutocreate) {
          String error = "auto-created " + propertyDescription + ": " + groupName;
          System.err.println("Grouper note: " + error);
          LOG.warn(error);
        }
        return CheckGroupResult.CREATED;
      } catch (Exception e) {
        System.err.println("Grouper error: " + groupName + ", " + ExceptionUtils.getFullStackTrace(e));
        LOG.error("Problem with group: " + groupName, e);
        return CheckGroupResult.ERROR_CREATING;
      }
    }
    
    return CheckGroupResult.DOESNT_EXIST;
  }

  /**
   * check a jar
   * @param name name of the jar from grouper
   * @param size that the jar should be
   * @param sampleClassName inside the jar
   * @param manifestVersion in the manifest file, which version we are expecting
   */
  public static void checkJar(String name, long size, String sampleClassName, String manifestVersion) {
    checkJar(name, GrouperUtil.toSet(size), sampleClassName, manifestVersion);
  }

  /**
   * check a jar
   * @param name name of the jar from grouper
   * @param sizes that the jar should be
   * @param sampleClassName inside the jar
   * @param manifestVersion in the manifest file, which version we are expecting
   */
  public static void checkJar(String name, Set<Long> sizes, String sampleClassName, String manifestVersion) {
    
    if (configCheckDisabled()) {
      return;
    }
    
    Class sampleClass = null;
    try {
      sampleClass = Class.forName(sampleClassName);
    } catch (ClassNotFoundException cnfe) {
      String error = "cannot find class " + sampleClassName + ", perhaps you are missing jar: " + name;
      System.err.println("Grouper warning: " + error);
      LOG.warn(error);
      return;
    }
    String jarFileFullName = null;
    String jarFileName = null;
    String jarVersion = null;
    long jarFileSize = -1;
    try {
      
      File jarFile = GrouperUtil.jarFile(sampleClass, true);
      jarFileFullName = jarFile.getCanonicalPath();
      jarFileName = jarFile.getName();
      jarFileSize = jarFile.length();
      //in case null
      jarVersion = jarVersion(sampleClass) + "";
      
      if (sizes.contains(jarFileSize) && StringUtils.equals(manifestVersion, jarVersion)
          && StringUtils.equals(name, jarFile.getName())) {
        LOG.debug("Found jarfile: " + jarFileFullName + " with correct size " 
          + GrouperUtil.toStringForLog(sizes) + " and version: " + manifestVersion);
        return;
      }
      
    } catch (Exception e) {
      //LOG.error("Error finding jar: " + name, e);
      //e.printStackTrace();
      //having problems
    }
    
    String error = "jarfile mismatch, expecting name: '" + name + "' size: " + GrouperUtil.toStringForLog(sizes)
      + " manifest version: " + manifestVersion + ".  However the jar detected is: "
      + jarFileFullName + ", name: " + jarFileName + " size: " + jarFileSize
      + " manifest version: " + jarVersion;
    System.err.println("Grouper warning: " + error);
    LOG.warn(error);
  }

  /**
   * make sure a resource is on the resource path
   * @param resourcePath
   * @return false if problem or if not checking configs
   */
  public static boolean checkResource(String resourcePath) {
    return checkResource(resourcePath, true);
  }

  /**
   * make sure a resource is on the resource path
   * @param resourcePath
   * @param required
   * @return false if problem or if not checking configs
   */
  public static boolean checkResource(String resourcePath, boolean required) {
    if (configCheckDisabled()) {
      return false;
    }
    try {
      URL url = GrouperUtil.computeUrl(resourcePath, false);
      if (url != null) {
        LOG.debug("Found resource: " + url);
        return true;
      }
    } catch (Exception e) {
      //this means it cant be found
    }
    if (!required) {
      return false;
    }
    String error = "Cant find required resource on classpath: " + resourcePath;
    //this is serious, lets go out and error
    System.err.println("Grouper error: " + error);
    LOG.error(error);
    return false;
  }

  /** cache if we are disabling config check */
  private static Boolean disableConfigCheck = null;
  
  /**
   * if the config check is disabled
   * @return if the config check is disabled
   */
  public static boolean configCheckDisabled() {
    if (disableConfigCheck == null) {
      //see if we shouldnt do this (but dont use ApiConfig API)
      try {
        Properties properties = GrouperConfig.retrieveConfig().properties();
        String detectErrorsKey = "configuration.detect.errors";
        String detectErrors = GrouperUtil.propertiesValue(properties, detectErrorsKey);
        if (!GrouperUtil.booleanValue(detectErrors, true)) {
          String warning = "Not checking configuration integrity due to grouper.properties: " 
              + detectErrorsKey;
          System.err.println("Grouper warning: " + warning);
          LOG.warn(warning);
          disableConfigCheck = true;
        }
      } catch (Exception e) {
        //cant read grouper properties
      }
      if (disableConfigCheck == null) {
        disableConfigCheck = false;
      }
    }
    return disableConfigCheck;
  }

  private static void verifyMailConfigsMigrated() {
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    String[] oldConfigs = new String[] {"mail.transport.protocol", "mail.use.protocol.in.property.names", "mail.from.address", "mail.subject.prefix", "mail.sendAllMessagesHere", "mail.debug", "grouperEmailContentType"};
    String[] newConfigs = new String[] {"mail.smtp.transport.protocol", "mail.smtp.use.protocol.in.property.names", "mail.smtp.from.address", "mail.smtp.subject.prefix", "mail.smtp.sendAllMessagesHere", "mail.smtp.debug", "mail.smtp.grouperEmailContentType"};
    
    for (int i=0;i<oldConfigs.length;i++) {
      if (!StringUtils.isBlank(grouperConfig.propertyValueString(oldConfigs[i]))) {
        LOG.error("Error: please change your grouper.properties config key: " + oldConfigs[i] + " to be " + newConfigs[i]);
      }
    }

  }
  
  /**
   * make sure grouper config files exist
   */
  private static void checkGrouperConfigs() {
    
    //make sure config files are there
    checkGrouperConfig();
    checkResource("grouper.cache.properties");
    checkResource("grouper.hibernate.properties");
    checkResource("log4j.properties");
    checkResource("morphString.properties");
    checkResource("subject.properties");
    
    verifyMailConfigsMigrated();
    
    for (ConfigFileName configFileName : ConfigFileName.values()) {
      if (!configFileName.isUseBaseForConfigFileMetadata()) {
        continue;
      }
      ConfigFileMetadata configFileMetadata = configFileName.configFileMetadata();
      // i.e. the ui and ws will be null if running gsh alone
      if (configFileMetadata != null) {
        if (!configFileMetadata.isValidConfig()) {
          LOG.error("Config " + configFileName.getClasspath() + " is not valid, see logs");
        }
      }
    }
    
  }
  
  /**
   * go through each property and check types of values
   */
  private static void checkGrouperConfig() {
    //if (!checkResource(GROUPER_PROPERTIES_NAME)) {
    //  return;
    //}

    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("groups.create.grant.all.optin", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("groups.create.grant.all.optout", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("groups.create.grant.all.read", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("groups.create.grant.all.view", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("groups.create.grant.all.groupAttrRead", true);

    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("stems.create.grant.all.create", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("stems.create.grant.all.stemAdmin", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("stems.create.grant.all.stemAttrRead", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("stems.create.grant.all.stemAttrUpdate", true);

    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("groups.wheel.use", true);

    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("registry.autoinit", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("configuration.detect.errors", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("configuration.display.startup.message", true);

    GrouperConfig.retrieveConfig().assertPropertyValueClass("dao.factory", 
        GrouperDAOFactory.class, true);

    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.attribute.class", AttributeHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.attributeDef.class", AttributeDefHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.attributeDefName.class", AttributeDefNameHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.attributeAssign.class", AttributeAssignHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.attributeAssignValue.class", AttributeAssignValueHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.composite.class", CompositeHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.externalSubject.class", ExternalSubjectHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.field.class", FieldHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.group.class", GroupHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.grouperSession.class", GrouperSessionHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.groupType.class", GroupTypeHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.groupTypeTuple.class", GroupTypeTupleHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.lifecycle.class", LifecycleHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.loader.class", LoaderHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.membership.class", MembershipHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.member.class", MemberHooks.class, false);
    GrouperConfig.retrieveConfig().assertPropertyValueClass("hooks.stem.class", StemHooks.class, false);
    
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("ddlutils.exclude.subject.tables", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("ddlutils.schemaexport.installGrouperData", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("ddlutils.failIfNotRightVersion", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("ddlutils.dropBackupUuidCols", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("ddlutils.dropBackupFieldNameTypeCols", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("ddlutils.dropAttributeBackupTableFromGroupUpgrade", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("ddlutils.disableComments", true);
    
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("grouperIncludeExclude.use", true);
    GrouperConfig.retrieveConfig().assertPropertyValueBoolean("grouperIncludeExclude.requireGroups.use", true);
    
    Properties properties = GrouperConfig.retrieveConfig().properties();
    String value = GrouperUtil.propertiesValue(properties, "grouperIncludeExclude.requireGroups.extension.suffix");

    if (value != null && !value.contains("${i}")) {
      String error = "Property grouperIncludeExclude.requireGroups.extension.suffix in grouper.properties must contain ${i}";
      System.err.println("Grouper error: " + error);
      LOG.error(error);
    }

    int i=0;
    while (true) {
      String key = "grouperIncludeExclude.requireGroup.attributeOrType." + i;
      String attributeOrType = GrouperUtil.propertiesValue(properties, key);
      if (StringUtils.isBlank(attributeOrType)) {
        break;
      }
      if (!StringUtils.equals(attributeOrType, "type") && !StringUtils.equals(attributeOrType, "attribute")) {
        String error = "Property " + key + " in grouper.properties must be either 'type' or 'attribute'";
        System.err.println("Grouper error: " + error);
        LOG.error(error);
      }
      i++;
    }
  }

  /** if in check config */
  public static boolean inCheckConfig = false;
  
  /**
   * check the grouper config safely, log errors
   */
  public static void checkConfig() {
    
    inCheckConfig = true;

    try {
      if (configCheckDisabled()) {
        return;
      }
      
      //first try to get in the GrouperConfig, just get a property to init stuff
      GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group");
      
      checkGrouperConfigs();
      
      checkConfigProperties();
      
      checkGrouperDb();
      
      //might as well try to init data at this point...
      GrouperStartup.initData(false);
      
      checkGroups();
      
      checkAttributes();
      
      postSteps();
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          //delegate to subject APIconfigs
          SubjectCheckConfig.checkConfig();
          return null;
        }
      });
    } finally {
      inCheckConfig = false;
    }
  }
  
  public static void checkConfig2() {
    
    boolean autoconfigure = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.attribute.loader.autoconfigure", true);
    if (!autoconfigure) {
      return;
    }

    final boolean wasInCheckConfig = inCheckConfig;

    inCheckConfig = true;

    try {
      if (configCheckDisabled()) {
        return;
      }
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          {
            
            String recentMembershipsRootStemName = GrouperRecentMemberships.recentMembershipsStemName();
            
            boolean assignAutoCreate = false;
            
            Stem recentMembershipsStem = StemFinder.findByName(grouperSession, recentMembershipsRootStemName, false);
            if (recentMembershipsStem == null) {
              recentMembershipsStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
                .assignDescription("folder for built in Grouper recent memberships objects").assignName(recentMembershipsRootStemName)
                .save();
            }

            //see if attributeDef is there
            String recentMembershipsMarkerDefName = recentMembershipsRootStemName + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER_DEF;
            AttributeDef recentMembershipsMarkerDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
                recentMembershipsMarkerDefName, false, new QueryOptions().secondLevelCache(false));
            if (recentMembershipsMarkerDef == null) {
              recentMembershipsMarkerDef = recentMembershipsStem.addChildAttributeDef(GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER_DEF, AttributeDefType.attr);
              recentMembershipsMarkerDef.setAssignToGroup(true);
              recentMembershipsMarkerDef.setMultiAssignable(false);
              recentMembershipsMarkerDef.store();
              assignAutoCreate = true;
            }
            
            Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(recentMembershipsMarkerDef);
            

            //add a name
            AttributeDefName recentMembershipsMarker = checkAttribute(recentMembershipsStem, recentMembershipsMarkerDef, GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, 
                "has recent memberships settings", wasInCheckConfig);
            
            //lets add some rule attributes
            String grouperRecentMembershipsValueDefName = recentMembershipsRootStemName + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_VALUE_DEF;
            AttributeDef grouperRecentMembershipsValueDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
                grouperRecentMembershipsValueDefName, false, new QueryOptions().secondLevelCache(false));
            if (grouperRecentMembershipsValueDef == null) {
              grouperRecentMembershipsValueDef = recentMembershipsStem.addChildAttributeDef(GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_VALUE_DEF, AttributeDefType.attr);
              grouperRecentMembershipsValueDef.setAssignToGroupAssn(true);
              grouperRecentMembershipsValueDef.setValueType(AttributeDefValueType.string);
              grouperRecentMembershipsValueDef.store();
            }

            Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(grouperRecentMembershipsValueDef);

            //the attributes can only be assigned to the type def
            // try an attribute def dependent on an attribute def name
            grouperRecentMembershipsValueDef.getAttributeDefScopeDelegate().assignOwnerNameEquals(recentMembershipsMarker.getName());

            //add some names
            AttributeDefName groupUuidAttributeDefName = checkAttribute(recentMembershipsStem, grouperRecentMembershipsValueDef, GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM, 
                "", wasInCheckConfig);
            AttributeDefName includeEligibleAttributeDefName = checkAttribute(recentMembershipsStem, grouperRecentMembershipsValueDef, GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT,
                "true or false if the eligible population should be included in the recent memberships group to reduce provisioning flicker", wasInCheckConfig);

            //lets add some rule attributes
            String grouperRecentMembershipsIntValueDefName = recentMembershipsRootStemName + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_INT_VALUE_DEF;
            AttributeDef grouperRecentMembershipsIntValueDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
                grouperRecentMembershipsIntValueDefName, false, new QueryOptions().secondLevelCache(false));
            if (grouperRecentMembershipsIntValueDef == null) {
              grouperRecentMembershipsIntValueDef = recentMembershipsStem.addChildAttributeDef(GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_INT_VALUE_DEF, AttributeDefType.attr);
              grouperRecentMembershipsIntValueDef.setAssignToGroupAssn(true);
              grouperRecentMembershipsIntValueDef.setValueType(AttributeDefValueType.integer);
              grouperRecentMembershipsIntValueDef.store();
            }

            //the attributes can only be assigned to the type def
            // try an attribute def dependent on an attribute def name
            grouperRecentMembershipsIntValueDef.getAttributeDefScopeDelegate().assignOwnerNameEquals(recentMembershipsMarker.getName());

            Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(grouperRecentMembershipsIntValueDef);

            AttributeDefName microsAttributeDefName = checkAttribute(recentMembershipsStem, grouperRecentMembershipsIntValueDef, GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS, 
                "Number of micros that the recent memberships last", wasInCheckConfig);

            
            String groupName = recentMembershipsRootStemName + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_LOADER_GROUP_NAME;
            Group group = GrouperDAOFactory.getFactory().getGroup().findByNameSecure(
                groupName, false, new QueryOptions().secondLevelCache(false), GrouperUtil.toSet(TypeOfGroup.group));
            
            String descriptionIfEnabled = "Holds the loader configuration of the recent memberships job that populates the recent memberships groups configured by attributes.  This is enabled in grouper.properties";
            String descriptionIfDisabled = "Holds the loader configuration of the recent memberships job that populates the recent memberships groups configured by attributes.  This is not enabled in grouper.properties";

            boolean recentMembershipsEnabled = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.recentMemberships.loaderJob.enable", true);
            String descriptionShouldBe = recentMembershipsEnabled ? descriptionIfEnabled : descriptionIfDisabled;

            Boolean changeLoader = null;

            if (group != null) {
              changeLoader = !StringUtils.equals(descriptionShouldBe, group.getDescription());
            }
            
            if (group == null) {
              changeLoader = (changeLoader != null && changeLoader) || recentMembershipsEnabled;
            }
            
            if (group == null) {
              group = new GroupSave(grouperSession).assignName(groupName)
                .assignDescription(descriptionShouldBe).save();
            }
            
            // if its new or the state has changed
            if (changeLoader != null && changeLoader) {
              GrouperRecentMemberships.setupRecentMembershipsLoaderJob(group);
            }
            
            // these attribute tell a grouper rule to auto assign the three name value pair attributes to the assignment when the marker is assigned
            if (assignAutoCreate) {
              AttributeDefName autoCreateMarker = AttributeDefNameFinder.findByName(AttributeAutoCreateHook.attributeAutoCreateStemName() + ":" + AttributeAutoCreateHook.GROUPER_ATTRIBUTE_AUTO_CREATE_MARKER, true);
              AttributeDefName ifName = AttributeDefNameFinder.findByName(AttributeAutoCreateHook.attributeAutoCreateStemName() + ":" + AttributeAutoCreateHook.GROUPER_ATTRIBUTE_AUTO_CREATE_ATTR_IF_NAME, true);
              AttributeDefName thenNames = AttributeDefNameFinder.findByName(AttributeAutoCreateHook.attributeAutoCreateStemName() + ":" + AttributeAutoCreateHook.GROUPER_ATTRIBUTE_AUTO_CREATE_ATTR_THEN_NAMES_ON_ASSIGN, true);
              
              AttributeAssignResult attributeAssignResult = recentMembershipsMarkerDef.getAttributeDelegate().assignAttribute(autoCreateMarker);
              attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(ifName.getName(), recentMembershipsMarker.getName());
              attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(thenNames.getName(), microsAttributeDefName.getName() 
                  + ", " + groupUuidAttributeDefName.getName() + ", " + includeEligibleAttributeDefName.getName());
            }
            
          }
          return null;
        }
      });
      
    } finally {
      inCheckConfig = false;
    }

  }
  
  /**
   * 
   */
  public static void postSteps() {

    boolean theTesting = false;
    long now = System.currentTimeMillis();
    try {
      String grouperTestClassName = "edu.internet2.middleware.grouper.helper.GrouperTest";
      Class grouperTestClass = GrouperUtil.forName(grouperTestClassName);
      theTesting = (Boolean)GrouperUtil.callMethod(grouperTestClass, "isTesting");
    } catch (Exception e) {
      //ignore
      LOG.debug("Likely non-fatal error seeing if testing, took (ms): " + (System.currentTimeMillis() - now), e);
    }
    final boolean testing = theTesting;
    
    // do this in a thread so we dont delay startup
    Thread thread = new Thread(new Runnable() {

      public void run() {
        
        doneWithExtraConfig = false;
        try {

          for (int i=0;i<200;i++) {
            if (GrouperStartup.isFinishedStartupSuccessfully()) {
              break;
            }
            //wait a sec for other things to get all initted
            GrouperUtil.sleep(100);
          }
          if (!testing) {
            //wait a sec for other things to get all initted
            GrouperUtil.sleep(5000);
          }
          
          GrouperContext grouperContext = GrouperContext.retrieveDefaultContext();
          if (grouperContext != null && grouperContext.getGrouperEngine() == GrouperEngineBuiltin.JUNIT) {
            return;
          }
          
          if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperDeprovisioningCheckSettingsOnDeprovisionedGroups", true)) {
          
            try {
              GrouperSession grouperSession = GrouperSession.startRootSession();
              
              // group that users who are allowed to deprovision other users are in
              for (String affiliation : GrouperDeprovisioningAffiliation.retrieveDeprovisioningAffiliations()) {

                // group that deprovisioned users go in (temporarily, but history will always be there)
                String deprovisioningGroupWhichHasBeenDeprovisionedName = GrouperDeprovisioningJob.retrieveGroupNameWhichHasBeenDeprovisioned(affiliation);
                
                Group group = GroupFinder.findByName(grouperSession, deprovisioningGroupWhichHasBeenDeprovisionedName, false);
                if (group != null) {
                  GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(group, false);
                  
                  // group that users who are allowed to deprovision other users are in
                  for (String affiliationToConfigure : GrouperDeprovisioningAffiliation.retrieveDeprovisioningAffiliations()) {
                    
                    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get(affiliationToConfigure);
                    
                    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
                    
                    boolean hasChange = false;
                    // if theres no configuration, or if the configuration is inherited, then clear it out
                    if (grouperDeprovisioningAttributeValue == null) {
                      grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
                      grouperDeprovisioningAttributeValue.setAffiliationString(affiliationToConfigure);
                      grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);
                      grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);
                      hasChange = true;
                    }
                    if (StringUtils.isBlank(grouperDeprovisioningAttributeValue.getDirectAssignmentString()) || !grouperDeprovisioningAttributeValue.isDirectAssignment()) {
                      grouperDeprovisioningAttributeValue.setDirectAssignment(true);
                      hasChange = true;
                    }
                    
                    if (StringUtils.isBlank(grouperDeprovisioningAttributeValue.getDeprovisionString()) || grouperDeprovisioningAttributeValue.isDeprovision()) {
                      grouperDeprovisioningAttributeValue.setDeprovision(false);
                      hasChange = true;
                    }
                    
                    if (StringUtils.isBlank(grouperDeprovisioningAttributeValue.getAutoselectForRemovalString()) || grouperDeprovisioningAttributeValue.isAutoselectForRemoval()) {
                      grouperDeprovisioningAttributeValue.setAutoselectForRemoval(false);
                      hasChange = true;
                    }
                    
                    if (StringUtils.isBlank(grouperDeprovisioningAttributeValue.getAutoChangeLoaderString()) || grouperDeprovisioningAttributeValue.isAutoChangeLoader()) {
                      grouperDeprovisioningAttributeValue.setAutoChangeLoader(false);
                      hasChange = true;
                    }
                    
                    if (StringUtils.isBlank(grouperDeprovisioningAttributeValue.getShowForRemovalString()) || grouperDeprovisioningAttributeValue.isShowForRemoval()) {
                      grouperDeprovisioningAttributeValue.setShowForRemoval(false);
                      hasChange = true;
                    }
                    
                    if (hasChange) {
                      grouperDeprovisioningConfiguration.storeConfiguration();
                    }
                  }
                  
                }
              }
                
            } catch (RuntimeException re) {
              //log incase thread didnt finish when screen was drawing
              LOG.error("Error with additional config", re);
            }
          }
        } finally {
          doneWithExtraConfig = true;
        }
      }
    });

    thread.setDaemon(true);
    thread.start();
    if (testing) {
      GrouperUtil.threadJoin(thread);
    }

  }

  /**
   * make sure configured groups are there 
   */
  public static void checkGroups() {
    
    boolean wasInCheckConfig = inCheckConfig;
    if (!wasInCheckConfig) {
      inCheckConfig = true;
    }
    
    //groups auto-create
    //#configuration.autocreate.group.name.0 = etc:uiUsers
    //#configuration.autocreate.group.description.0 = users allowed to log in to the UI
    //#configuration.autocreate.group.subjects.0 = johnsmith
    int i=0;
    
    GrouperSession grouperSession = null;
    boolean startedGrouperSession = false;
    try {
      grouperSession = GrouperSession.staticGrouperSession(false);

      if (grouperSession == null) {
        grouperSession = GrouperSession.startRootSession();
        startedGrouperSession = true;
      }
      
      while(true) {
        String groupName = null;
        try {
          String groupNameKey = "configuration.autocreate.group.name." + i;
          groupName = GrouperConfig.retrieveConfig().propertyValueString(groupNameKey);
          
          if (StringUtils.isBlank(groupName)) {
            break;
          }
          
          String groupDescription = GrouperConfig.retrieveConfig().propertyValueString("configuration.autocreate.group.description." + i);
          String subjectsKey = "configuration.autocreate.group.subjects." + i;
          String subjects = GrouperConfig.retrieveConfig().propertyValueString(subjectsKey);
    
          Group[] theGroup = new Group[1];
          //first the group
          checkGroup(grouperSession, groupName, wasInCheckConfig, true, wasInCheckConfig, null, groupDescription, "grouper.properties key " + groupNameKey, theGroup);
          //now the subjects
          if (!StringUtils.isBlank(subjects)) {
            String[] subjectArray = GrouperUtil.splitTrim(subjects, ",");
            for (String subjectId : subjectArray) {
              
              try {
                Subject subject = SubjectFinder.findByIdOrIdentifier(subjectId, false);
                boolean added = theGroup[0].addMember(subject, false);
                if (added && wasInCheckConfig) {
                  String error = "auto-added subject " + subjectId + " to group: " + theGroup[0].getName();
                  System.err.println("Grouper warning: " + error);
                  LOG.warn(error);
                }
              } catch (MemberAddException mae) {
                throw new RuntimeException("this should never happen", mae);
              } catch (InsufficientPrivilegeException snfe) {
                throw new RuntimeException("this should never happen", snfe);
              } catch (SubjectNotFoundException snfe) {
                throw new RuntimeException("this should never happen", snfe);
              } catch (SubjectNotUniqueException snue) {
                String error = "subject not unique from grouper.properties key: " + subjectsKey + ", " + subjectId;
                System.err.println("Grouper error: " + error);
                LOG.error(error, snue);
              }
            }
              
          }
        } catch (RuntimeException re) {
          GrouperUtil.injectInException(re, ", problem with auto-create group: " + groupName);
        }
        i++;
      }
      {
        boolean useWheel = GrouperConfig.retrieveConfig().propertyValueBoolean("groups.wheel.use", false);
        if (useWheel) {
          String wheelName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group");
          if (StringUtils.isBlank(wheelName) && wasInCheckConfig) {
            String error = "grouper.properties property groups.wheel.group should not be blank if groups.wheel.use is true";
            System.err.println("Grouper error: " + error);
            LOG.warn(error);
          } else {
            checkGroup(grouperSession, wheelName, wasInCheckConfig, null, wasInCheckConfig, null, "system administrators with all privileges", 
                "wheel group from grouper.properties key: groups.wheel.group", null);
          }
        }
      }      
      {
        boolean useViewonlyWheel = GrouperConfig.retrieveConfig().propertyValueBoolean("groups.wheel.viewonly.use", false);
        if (useViewonlyWheel) {
          String wheelViewonlyName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.viewonly.group");
          if (StringUtils.isBlank(wheelViewonlyName) && wasInCheckConfig) {
            String error = "grouper.properties property groups.wheel.viewonly.group should not be blank if groups.wheel.viewonly.use is true";
            System.err.println("Grouper error: " + error);
            LOG.warn(error);
          } else {
            checkGroup(grouperSession, wheelViewonlyName, wasInCheckConfig, null, wasInCheckConfig, null, "system administrators with view privileges", 
                "viewonly wheel group from grouper.properties key: groups.wheel.viewonly.group", null);
          }
        }
      }      
      {
        boolean useReadonlyWheel = GrouperConfig.retrieveConfig().propertyValueBoolean("groups.wheel.readonly.use", false);
        if (useReadonlyWheel) {
          String wheelReadonlyName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.readonly.group");
          if (StringUtils.isBlank(wheelReadonlyName) && wasInCheckConfig) {
            String error = "grouper.properties property groups.wheel.readonly.group should not be blank if groups.wheel.readonly.use is true";
            System.err.println("Grouper error: " + error);
            LOG.warn(error);
          } else {
            checkGroup(grouperSession, wheelReadonlyName, wasInCheckConfig, null, wasInCheckConfig, null, "system administrators with read privileges", 
                "readonly wheel group from grouper.properties key: groups.wheel.readonly.group", null);
          }
        }
      }      
      // security.stem.groupAllowedToMoveStem
      String allowedGroupName = "security.stem.groupAllowedToMoveStem";
      String groupAllowedToMoveStem = GrouperConfig.retrieveConfig().propertyValueString(allowedGroupName);
      if (StringUtils.isNotBlank(groupAllowedToMoveStem)) {
        checkGroup(grouperSession, groupAllowedToMoveStem, wasInCheckConfig, null, wasInCheckConfig, null, 
            null, "grouper.properties key: " + allowedGroupName, null);        
      }
      
      // security.stem.groupAllowedToRenameStem
      allowedGroupName = "security.stem.groupAllowedToRenameStem";
      String groupAllowedToRenameStem = GrouperConfig.retrieveConfig().propertyValueString(allowedGroupName);
      if (StringUtils.isNotBlank(groupAllowedToRenameStem)) {
        checkGroup(grouperSession, groupAllowedToRenameStem, wasInCheckConfig, null, wasInCheckConfig, null, 
            null, "grouper.properties key: " + allowedGroupName, null);        
      }
      
      // security.stem.groupAllowedToCopyStem
      allowedGroupName = "security.stem.groupAllowedToCopyStem";
      String groupAllowedToCopyStem = GrouperConfig.retrieveConfig().propertyValueString(allowedGroupName);
      if (StringUtils.isNotBlank(groupAllowedToCopyStem)) {
        checkGroup(grouperSession, groupAllowedToCopyStem, wasInCheckConfig, null, wasInCheckConfig, null, 
            null, "grouper.properties key: " + allowedGroupName, null);        
      }
      
      //groups in requireGroups
      i=0;
      while(true) {
        String groupName = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroup.group." + i);
        
        if (StringUtils.isBlank(groupName)) {
          break;
        }
        
        String key = "grouperIncludeExclude.requireGroup.description." + i;
        String description = GrouperConfig.retrieveConfig().propertyValueString(key);
        
        checkGroup(grouperSession, groupName, wasInCheckConfig, null, wasInCheckConfig, null, description, 
          "requireGroup from grouper.properties key: " + key, null);
        
        i++;
      }
      
      //groups that manage types
      Map<String, String> typePatterns = typeSecuritySettings();
      for (String key: typePatterns.keySet()) {
        
        Matcher matcher = typeSecurityPattern.matcher(key);
        
        matcher.matches();
        String typeName = matcher.group(1);
        String settingType = matcher.group(2);
        if (!StringUtils.equalsIgnoreCase("allowOnlyGroup", settingType)) {
          continue;
        }
        //this is a group
        String groupName = typePatterns.get(key);
        String description = "Group whose members are allowed to edit type (and related attributes): " + typeName;
        checkGroup(grouperSession, groupName, wasInCheckConfig, null, wasInCheckConfig, null, description, 
            "type security from grouper.properties key: " + key, null);
        
      }
      
      //groups that manage access to sort and search strings
      Map<String, String> memberSortSearchPatterns = memberSortSearchSecuritySettings();
      for (String key: memberSortSearchPatterns.keySet()) {
        
        Matcher matcher = memberSortSearchSecurityPattern.matcher(key);
        
        matcher.matches();
        String name = matcher.group(1) + matcher.group(2);
        String settingType = matcher.group(3);
        if (!StringUtils.equalsIgnoreCase("allowOnlyGroup", settingType)) {
          continue;
        }
        //this is a group
        String groupName = memberSortSearchPatterns.get(key);
        String description = "Group whose members are allowed to access: " + name;
        checkGroup(grouperSession, groupName, wasInCheckConfig, null, wasInCheckConfig, null, description, 
            "member sort/search security from grouper.properties key: " + key, null);
        
      }
      
      if (MembershipCannotAddSelfToGroupHook.cannotAddSelfEnabled()){
        String cannotAddSelfRootStemName = MembershipCannotAddSelfToGroupHook.cannotAddSelfStemName();
        
        Stem cannotAddSelfRootStem = StemFinder.findByName(grouperSession, cannotAddSelfRootStemName, false);
        if (cannotAddSelfRootStem == null) {
          cannotAddSelfRootStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for objects related to cannot add self to group").assignName(cannotAddSelfRootStemName)
            .save();
        }

        {
          // users who can assign "cannot add self as member of group"
          String cannotAddSelfAssignGroupName = MembershipCannotAddSelfToGroupHook.cannotAddSelfAssignGroupName();

          checkGroup(grouperSession, cannotAddSelfAssignGroupName, wasInCheckConfig, true, 
              wasInCheckConfig, null, 
              "users who can assign \"cannot add self as member of group\"", 
              "users who can assign \"cannot add self as member of group\"",
              null);
        }        
        
        {
          // users who can revoke "cannot add self as member of group"
          String cannotAddSelfRevokeGroupName = MembershipCannotAddSelfToGroupHook.cannotAddSelfRevokeGroupName();

          checkGroup(grouperSession, cannotAddSelfRevokeGroupName, wasInCheckConfig, true, 
              wasInCheckConfig, null, 
              "users who can revoke \"cannot add self as member of group\"", 
              "users who can revoke \"cannot add self as member of group\"",
              null);
        }        
        
        //see if attributeDef is there
        String cannotAddSelfTypeDefName = MembershipCannotAddSelfToGroupHook.cannotAddSelfNameOfAttributeDef();
        AttributeDef cannotAddSelfType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            cannotAddSelfTypeDefName, false, new QueryOptions().secondLevelCache(false));
        if (cannotAddSelfType == null) {
          cannotAddSelfType = cannotAddSelfRootStem.addChildAttributeDef(GrouperUtil.extensionFromName(cannotAddSelfTypeDefName), AttributeDefType.type);
          //assign once
          cannotAddSelfType.setMultiAssignable(false);
          cannotAddSelfType.setAssignToGroup(true);
          cannotAddSelfType.store();
        }
        
        //add a name
        checkAttribute(cannotAddSelfRootStem, cannotAddSelfType, GrouperUtil.extensionFromName(MembershipCannotAddSelfToGroupHook.cannotAddSelfNameOfAttributeDefName()), 
            "Assign this attribute to a group and users will not be able to add themself to the group for separation of duties", wasInCheckConfig);
        
        MembershipCannotAddSelfToGroupHook.registerHookIfNecessary();
      }
      
      //if (GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      // always add these objects
      {
        String deprovisioningRootStemName = GrouperDeprovisioningSettings.deprovisioningStemName();
        
        Stem deprovisioningStem = StemFinder.findByName(grouperSession, deprovisioningRootStemName, false);
        if (deprovisioningStem == null) {
          deprovisioningStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in Grouper deprovisioning objects").assignName(deprovisioningRootStemName)
            .save();
        }

        boolean autocreate = GrouperConfig.retrieveConfig().propertyValueBoolean("deprovisioning.autocreate.groups", true);
        
        {
          // # users in this group who are admins of an affiliation but who are not Grouper SysAdmins, will be 
          // # able to deprovision from all grouper groups/objects, not just groups they have access to UPDATE/ADMIN
          // deprovisioning.admin.group = $$deprovisioning.systemFolder$$:deprovisioningAdmins
          String deprovisioningAdminGroupName = GrouperDeprovisioningSettings.retrieveDeprovisioningAdminGroupName();

          checkGroup(grouperSession, deprovisioningAdminGroupName, wasInCheckConfig, autocreate, 
              wasInCheckConfig, null, 
              "deprovisioning admin group can deprovision from all groups/objects in Grouper even if the user is not a Grouper overall SysAdmin", 
              "deprovisioning admin group can deprovision from all groups/objects in Grouper even if the user is not a Grouper overall SysAdmin",
              null);
        }        
        
        // group that users who are allowed to deprovision other users are in
        for (String affiliation : GrouperDeprovisioningAffiliation.retrieveDeprovisioningAffiliations()) {

          String deprovisioningManagersMustBeInGroupName = GrouperDeprovisioningJob.retrieveDeprovisioningManagersMustBeInGroupName(affiliation);

          checkGroup(grouperSession, deprovisioningManagersMustBeInGroupName, wasInCheckConfig, autocreate, 
              wasInCheckConfig, null, "deprovisioning: " + affiliation + ", group that users who are allowed to deprovision other users are in", 
              "deprovisioning: " + affiliation + ", group that users who are allowed to deprovision other users are in", null);

          // group that deprovisioned users go in (temporarily, but history will always be there)
          String deprovisioningGroupWhichHasBeenDeprovisionedName = GrouperDeprovisioningJob.retrieveGroupNameWhichHasBeenDeprovisioned(affiliation);
          
          checkGroup(grouperSession, deprovisioningGroupWhichHasBeenDeprovisionedName, wasInCheckConfig, autocreate, 
              wasInCheckConfig, null, "deprovisioning: " + affiliation + ", group that deprovisioned users go in (temporarily, but history will always be there)", 
              "deprovisioning: " + affiliation + ", group that deprovisioned users go in (temporarily, but history will always be there)", null);

          
        }
        
        //see if attributeDef is there
        String deprovisioningTypeDefName = deprovisioningRootStemName + ":" + GrouperDeprovisioningAttributeNames.DEPROVISIONING_DEF;
        AttributeDef deprovisioningType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            deprovisioningTypeDefName, false, new QueryOptions().secondLevelCache(false));
        if (deprovisioningType == null) {
          deprovisioningType = deprovisioningStem.addChildAttributeDef(GrouperDeprovisioningAttributeNames.DEPROVISIONING_DEF, AttributeDefType.type);
          //assign once for each affiliation
          deprovisioningType.setMultiAssignable(true);
          deprovisioningType.setAssignToGroup(true);
          deprovisioningType.setAssignToAttributeDef(true);
          deprovisioningType.setAssignToStem(true);
          deprovisioningType.store();
        }
        
        //add a name
        AttributeDefName attribute = checkAttribute(deprovisioningStem, deprovisioningType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_BASE, "has deprovisioning attributes", wasInCheckConfig);
        
        //lets add some rule attributes
        String deprovisioningAttrDefName = deprovisioningRootStemName + ":" + GrouperDeprovisioningAttributeNames.DEPROVISIONING_VALUE_DEF;
        AttributeDef deprovisioningAttrType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
            deprovisioningAttrDefName, false, new QueryOptions().secondLevelCache(false));
        if (deprovisioningAttrType == null) {
          deprovisioningAttrType = deprovisioningStem.addChildAttributeDef(GrouperDeprovisioningAttributeNames.DEPROVISIONING_VALUE_DEF, AttributeDefType.attr);
          deprovisioningAttrType.setAssignToGroupAssn(true);
          deprovisioningAttrType.setAssignToStemAssn(true);
          deprovisioningAttrType.setAssignToAttributeDefAssn(true);
          deprovisioningAttrType.setValueType(AttributeDefValueType.string);
          deprovisioningAttrType.store();
        }

        //the attributes can only be assigned to the type def
        // try an attribute def dependent on an attribute def name
        deprovisioningAttrType.getAttributeDefScopeDelegate().assignOwnerNameEquals(attribute.getName());
        
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_INHERITED_FROM_FOLDER_ID,
            "Stem ID of the folder where the configuration is inherited from.  This is blank if this is a direct assignment and not inherited", wasInCheckConfig);
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_AFFILIATION, 
            "Affiliation configured in the grouper.properties.  e.g. employee, student, etc", wasInCheckConfig);
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_ALLOW_ADDS_WHILE_DEPROVISIONED, 
            "If allows adds to group of people who are deprovisioned.  can be: blank, true, or false.  "
            + "If blank, then will not allow adds unless auto change loader is false", wasInCheckConfig);
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_AUTO_CHANGE_LOADER, 
            "If this is a loader job, if being in a deprovisioned group means the user "
            + "should not be in the loaded group. can be: blank (true), or false (false)", wasInCheckConfig);
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_AUTOSELECT_FOR_REMOVAL, 
            "If the deprovisioning screen should autoselect this object as an object to deprovision can be: blank, true, or false.  "
            + "If blank, then will autoselect unless deprovisioningAutoChangeLoader is false", wasInCheckConfig);
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_DIRECT_ASSIGNMENT, 
            "if deprovisioning configuration is directly assigned to the group or folder or inherited from parent", wasInCheckConfig);
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_EMAIL_ADDRESSES, 
            "Email addresses to send deprovisioning messages.  If blank, then send to group managers, or comma separated email addresses (mutually exclusive with deprovisioningMailToGroup)", wasInCheckConfig);
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_MAIL_TO_GROUP, 
            "Group ID which holds people to email members of that group to send deprovisioning messages (mutually exclusive with deprovisioningEmailAddresses)", wasInCheckConfig);
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_SEND_EMAIL, 
            "If this is true, then send an email about the deprovisioning event.  If the assignments were removed, then give a "
            + "description of the action.  If assignments were not removed, then remind the managers to unassign.  Can be <blank>, true, or false.  "
            + "Defaults to false unless the assignments were not removed.", wasInCheckConfig);
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_SHOW_FOR_REMOVAL, 
            "If the deprovisioning screen should show this object if the user as an assignment.  "
            + "Can be: blank, true, or false.  If blank, will default to true unless auto change loader is false.", wasInCheckConfig);
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_DEPROVISION, 
            "if this object should be in consideration for the deprovisioning system.  Can be: blank, true, or false.  Defaults to true", wasInCheckConfig);
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_STEM_SCOPE,
            "If configuration is assigned to a folder, then this is 'one' or 'sub'.  'one' means only applicable to objects"
            + " directly in this folder.  'sub' (default) means applicable to all objects in this folder and "
            + "subfolders.  Note, the inheritance stops when a sub folder or object has configuration assigned.", wasInCheckConfig);
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_EMAIL_BODY, 
            "custom email body for emails, if blank use the default configured body.  "
            + "Note there are template variables $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$", wasInCheckConfig);
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_LAST_EMAILED_DATE, 
            "yyyy/mm/dd date that this was last emailed so multiple emails dont go out on same day", wasInCheckConfig);
        checkAttribute(deprovisioningStem, deprovisioningAttrType, GrouperDeprovisioningAttributeNames.DEPROVISIONING_CERTIFIED_MILLIS, 
            "(String) number of millis since 1970 that this group was certified for deprovisioning. i.e. the group managers"
            + " indicate that the deprovisioned users are ok being in the group and do not send email reminders about it" 
            + " anymore until there are newly deprovisioned entities", wasInCheckConfig);

      }
      
      {
        // add workflow admin group
        String workflowEditorsGroup = GrouperWorkflowSettings.workflowEditorsGroup();

        checkGroup(grouperSession, workflowEditorsGroup, wasInCheckConfig, true, 
            wasInCheckConfig, null,
            "Workflow editors group",
            "Workflow editors group",
            null);
      }
      
      // add attribute defs for provisioning 
      // (https://spaces.at.internet2.edu/display/Grouper/Grouper+provisioning+in+UI)
      {
        String grouperProvisioningUiRootStemName = GrouperProvisioningSettings.provisioningConfigStemName();
        
        Stem grouperProvisioningStemName = StemFinder.findByName(grouperSession, grouperProvisioningUiRootStemName, false);
        if (grouperProvisioningStemName == null) {
          grouperProvisioningStemName = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder to store attribute defs and names for provisioning in ui").assignName(grouperProvisioningUiRootStemName)
            .save();
        }

        //see if attributeDef is there
        String provisioningDefName = grouperProvisioningUiRootStemName + ":" + GrouperProvisioningAttributeNames.PROVISIONING_DEF;
        AttributeDef provisioningDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            provisioningDefName, false, new QueryOptions().secondLevelCache(false));
        if (provisioningDef == null) {
          provisioningDef = grouperProvisioningStemName.addChildAttributeDef(GrouperProvisioningAttributeNames.PROVISIONING_DEF, AttributeDefType.type);
          //assign once for each target
          provisioningDef.setMultiAssignable(true);
          provisioningDef.setAssignToGroup(true);
          provisioningDef.setAssignToMember(true);
          provisioningDef.setAssignToEffMembership(true);
          provisioningDef.setAssignToStem(true);
          
          provisioningDef.store();
        }
        
        if (provisioningDef.isAssignToEffMembership() == false) {
          provisioningDef.setAssignToEffMembership(true);
          provisioningDef.setAssignToMember(true);
          
          provisioningDef.store();
        }
        
        
        //add a name
        AttributeDefName attribute = checkAttribute(grouperProvisioningStemName, provisioningDef, GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME, "has provisioning attributes", wasInCheckConfig);
        
        //lets add some rule attributes
        String provisioningValueAttrDefName = grouperProvisioningUiRootStemName + ":" + GrouperProvisioningAttributeNames.PROVISIONING_VALUE_DEF;
        AttributeDef provisioningAttrValueDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
            provisioningValueAttrDefName, false, new QueryOptions().secondLevelCache(false));
        if (provisioningAttrValueDef == null) {
          provisioningAttrValueDef = grouperProvisioningStemName.addChildAttributeDef(GrouperProvisioningAttributeNames.PROVISIONING_VALUE_DEF, AttributeDefType.attr);
          
          provisioningAttrValueDef.setAssignToGroupAssn(true);
          provisioningAttrValueDef.setAssignToStemAssn(true);
          provisioningAttrValueDef.setAssignToMemberAssn(true);
          provisioningAttrValueDef.setAssignToEffMembershipAssn(true);
          provisioningAttrValueDef.setAssignToAttributeDefAssn(true);
          provisioningAttrValueDef.setValueType(AttributeDefValueType.string);
          provisioningAttrValueDef.store();
        }
        
        if (provisioningAttrValueDef.isAssignToEffMembershipAssn() == false) {
          provisioningAttrValueDef.setAssignToMemberAssn(true);
          provisioningAttrValueDef.setAssignToEffMembershipAssn(true);
          provisioningAttrValueDef.store();
        }

        //the attributes can only be assigned to the value def
        // try an attribute def dependent on an attribute def name
        provisioningAttrValueDef.getAttributeDefScopeDelegate().assignOwnerNameEquals(attribute.getName());
        
        checkAttribute(grouperProvisioningStemName, provisioningAttrValueDef, GrouperProvisioningAttributeNames.PROVISIONING_TARGET,
            "pspngLdap|box1|etc", wasInCheckConfig);
        
        checkAttribute(grouperProvisioningStemName, provisioningAttrValueDef, GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT, 
            "If this is directly assigned or inherited from a parent folder", wasInCheckConfig);
        
        checkAttribute(grouperProvisioningStemName, provisioningAttrValueDef, GrouperProvisioningAttributeNames.PROVISIONING_STEM_SCOPE, 
            "If folder provisioning applies to only this folder or this folder and subfolders", wasInCheckConfig);
        
        checkAttribute(grouperProvisioningStemName, provisioningAttrValueDef, GrouperProvisioningAttributeNames.PROVISIONING_OWNER_STEM_ID, 
            "Stem ID of the folder where the configuration is inherited from.  This is blank if this is a direct assignment", wasInCheckConfig);
        
        checkAttribute(grouperProvisioningStemName, provisioningAttrValueDef, GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION, 
            "If you should provision (default to true)", wasInCheckConfig);
        
        checkAttribute(grouperProvisioningStemName, provisioningAttrValueDef, GrouperProvisioningAttributeNames.PROVISIONING_METADATA_JSON,
            "generated json from the UI", wasInCheckConfig);
        
      }
      
      // https://spaces.at.internet2.edu/display/Grouper/USDU+delete+subjects+after+unresolvable+for+X+days
      // add usdu attributes
      {
        String usduRootStemName = UsduSettings.usduStemName();
        
        Stem usduStem = StemFinder.findByName(grouperSession, usduRootStemName, false);
        if (usduStem == null) {
          usduStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in Grouper usdu objects").assignName(usduRootStemName)
            .save();
        }

        //see if attributeDef is there
        String subjectResolutionTypeDefName = usduRootStemName + ":" + UsduAttributeNames.SUBJECT_RESOLUTION_DEF;
        AttributeDef subjectResolutionType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            subjectResolutionTypeDefName, false, new QueryOptions().secondLevelCache(false));
        if (subjectResolutionType == null) {
          subjectResolutionType = usduStem.addChildAttributeDef(UsduAttributeNames.SUBJECT_RESOLUTION_DEF, AttributeDefType.type);
          subjectResolutionType.setAssignToMember(true);
          subjectResolutionType.store();
        }
        
        //add a name
        AttributeDefName attribute = checkAttribute(usduStem, subjectResolutionType, UsduAttributeNames.SUBJECT_RESOLUTION_NAME, "has subject resolution attributes", wasInCheckConfig);
        
        //lets add some rule attributes
        String subjectResolutionAttrDefName = usduRootStemName + ":" + UsduAttributeNames.SUBJECT_RESOLUTION_VALUE_DEF;
        AttributeDef subjectResolutionAttrType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
            subjectResolutionAttrDefName, false, new QueryOptions().secondLevelCache(false));
        if (subjectResolutionAttrType == null) {
          subjectResolutionAttrType = usduStem.addChildAttributeDef(UsduAttributeNames.SUBJECT_RESOLUTION_VALUE_DEF, AttributeDefType.attr);
          subjectResolutionAttrType.setAssignToMemberAssn(true);
          subjectResolutionAttrType.setValueType(AttributeDefValueType.string);
          subjectResolutionAttrType.store();
        }
        
        //the attributes can only be assigned to the type def
        // try an attribute def dependent on an attribute def name
        subjectResolutionAttrType.getAttributeDefScopeDelegate().assignOwnerNameEquals(attribute.getName());
        
        checkAttribute(usduStem, subjectResolutionAttrType, UsduAttributeNames.SUBJECT_RESOLUTION_DATE_LAST_RESOLVED, 
            "yyyy/mm/dd If this subject has a date and is unresolveable, leave it. if this subject doesnt have a date, and is unresolvable, then set to currentDate.", wasInCheckConfig);
        
        checkAttribute(usduStem, subjectResolutionAttrType, UsduAttributeNames.SUBJECT_RESOLUTION_DAYS_UNRESOLVED, 
            "the number of days from current date minus dateLastResolved.", wasInCheckConfig);
        
        checkAttribute(usduStem, subjectResolutionAttrType, UsduAttributeNames.SUBJECT_RESOLUTION_LAST_CHECKED, 
            "yyyy/mm/dd the date this subject was last checked. When the USDU runs, if this subject is current unresolvable, then set to currentDate", wasInCheckConfig);
        
        checkAttribute(usduStem, subjectResolutionAttrType, UsduAttributeNames.SUBJECT_RESOLUTION_DELETE_DATE,
            "yyyy/mm/dd when all the memberships are removed", wasInCheckConfig);
      }
      
      
    } catch (SessionException se) {
      throw new RuntimeException(se);
    } finally {
      if (startedGrouperSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
      if (!wasInCheckConfig) {
        inCheckConfig = false;
      }
    }
    
  }

  /**
   * @return the map of settings from grouper.properties
   */
  public static Map<String, String> typeSecuritySettings() {
    return GrouperConfig.retrieveConfig().propertiesMap(typeSecurityPattern);
  }
  
  /**
   * @return the map of settings from grouper.properties
   */
  public static Map<String, String> memberSortSearchSecuritySettings() {
    return GrouperConfig.retrieveConfig().propertiesMap(memberSortSearchSecurityPattern);
  }
  
  /**
   * make sure the grouper.hibernate.properties db settings are correct
   */
  public static void checkGrouperDb() {
    Properties grouperHibernateProperties = GrouperHibernateConfig.retrieveConfig().properties();

    //#com.p6spy.engine.spy.P6SpyDriver, oracle.jdbc.driver.OracleDriver
    String driverClassName = GrouperUtil.propertiesValue(
        grouperHibernateProperties, "hibernate.connection.driver_class");
    String connectionUrl = GrouperUtil.propertiesValue(
        grouperHibernateProperties, "hibernate.connection.url");
    String dbUser = GrouperUtil.propertiesValue(
        grouperHibernateProperties, "hibernate.connection.username");
    String dbPassword = GrouperUtil.propertiesValue(
        grouperHibernateProperties, "hibernate.connection.password");
    
    if (!checkDatabase(driverClassName, connectionUrl, dbUser, dbPassword, "grouper.hibernate.properties")) {
      return;
    }
    
    driverClassName = GrouperDdlUtils.convertUrlToDriverClassIfNeeded(connectionUrl, driverClassName);
    
    String realDriverClass = driverClassName;

    String spySuffix = "";

    //dont load class here
    if (driverClassName.equals("com.p6spy.engine.spy.P6SpyDriver")) {
      Properties spyProperties = GrouperUtil.propertiesFromResourceName("spy.properties");
      realDriverClass = StringUtils.defaultString(GrouperUtil.propertiesValue(spyProperties, "realdriver"));
      spySuffix = ", and spy.properties";
    }
    
    //try to check the hibernate dialect
    boolean isDriverOracle = realDriverClass.toLowerCase().contains("oracle");
    boolean isDriverPostgres = realDriverClass.toLowerCase().contains("postgres");
    boolean isDriverMysql = realDriverClass.toLowerCase().contains("mysql");
    boolean isDriverHsql = realDriverClass.toLowerCase().contains("hsql");
    boolean isDriverSqlServer = realDriverClass.toLowerCase().contains("sqlserver") 
      || realDriverClass.toLowerCase().contains("jtds");
    
    String dialect = StringUtils.defaultString(GrouperUtil.propertiesValue(grouperHibernateProperties,"hibernate.dialect"));
    
    dialect = GrouperDdlUtils.convertUrlToHibernateDialectIfNeeded(connectionUrl, dialect);
    
    boolean isDialectOracle = dialect.toLowerCase().contains("oracle");
    boolean isDialectPostgres = dialect.toLowerCase().contains("postgres");
    boolean isDialectMysql = dialect.toLowerCase().contains("mysql");
    boolean isDialectHsql = dialect.toLowerCase().contains("hsql");
    boolean isDialectSqlServer = dialect.toLowerCase().contains("sqlserver");
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("db.log.driver.mismatch", true)) {
      if ((isDriverOracle && !isDialectOracle) || (isDriverPostgres && !isDialectPostgres) 
          || (isDriverMysql && !isDialectMysql) || (isDriverHsql && !isDialectHsql)
          || (!isDriverOracle && isDialectOracle) || (!isDriverPostgres && isDialectPostgres) 
          || (!isDriverMysql && isDialectMysql) || (!isDriverHsql && isDialectHsql)
          || (!isDriverSqlServer && isDialectSqlServer) || (isDriverSqlServer && !isDialectSqlServer)) {
        String error = "Grouper error: detected mismatch in hibernate.connection.driver_class ("
                + realDriverClass + ") and hibernate.dialect (" + dialect 
                + ") in grouper.hibernate.properties" + spySuffix;
        System.err.println(error);
        LOG.error(error);
      }
    }    
  }

  /**
   * test a database connection
   * @param driverClassName
   * @param connectionUrl
   * @param dbUser
   * @param dbPassword
   * @param databaseDescription friendly error description when there is a problem
   * @return true if it is ok, false if there is a problem
   */
  public static boolean checkDatabase(String driverClassName, String connectionUrl, String dbUser, String dbPassword,
      String databaseDescription) {
    try {
      
      if (StringUtils.isBlank(connectionUrl)) {
        String error = "Error finding connection url from " + databaseDescription;
        System.err.println("Grouper error: " + error);
        LOG.error(error);
        return false;
        
      }
      
      dbPassword = Morph.decryptIfFile(dbPassword);
      
      driverClassName = GrouperDdlUtils.convertUrlToDriverClassIfNeeded(connectionUrl, driverClassName);
      
      Class driverClass = null;
      try {
        driverClass = GrouperUtil.forName(driverClassName);
      } catch (Exception e) {
        String error = "Error finding database driver class from " + databaseDescription + ": " 
          + driverClassName
          + ", perhaps you did not put the database driver jar in the /opt/grouper/grouperWebapp/WEB-INF/lib dir or lib dir, " +
              "or you have the wrong driver listed";
        System.err.println("Grouper error: " + error + ": " + ExceptionUtils.getFullStackTrace(e));
        LOG.error(error, e);
        return false;
      }
      
      //check out P6Spy
      String spyInsert = "";
      //dont load class here
      if (driverClass.getName().equals("com.p6spy.engine.spy.P6SpyDriver")) {
        spyInsert = " and spy.properties, ";
        checkResource("spy.properties");
        Properties spyProperties = GrouperUtil.propertiesFromResourceName("spy.properties");
        driverClassName = StringUtils.defaultString(GrouperUtil.propertiesValue(spyProperties,"realdriver"));
        try {
          driverClass = GrouperUtil.forName(driverClassName);
        } catch (Exception e) {
          String error = "Error finding database driver class from spy.properties: '" 
            + driverClassName
            + "', perhaps you did not put the database driver jar in the /opt/grouper/grouperWebapp/WEB-INF/lib dir or lib dir, " +
                "or you have the wrong driver listed";
          System.err.println("Grouper error: " + error + ": " + ExceptionUtils.getFullStackTrace(e));
          LOG.error(error, e);
          return false;
        }
      }
      
      //lets make a db connection
      Connection dbConnection = null;
      try {
        dbConnection = DriverManager.getConnection(connectionUrl, dbUser, dbPassword);
        @SuppressWarnings("unused")
        String version = dbConnection.getMetaData().getDatabaseProductVersion();
        return true;
      } catch( SQLException sqlException) {
        String error = "Error connecting to the database with credentials from " + databaseDescription + ", "
          + spyInsert + "url: " + connectionUrl + ", driver: " + driverClassName + ", user: " + dbUser;
        System.out.println("Grouper error: " + error + ", " + ExceptionUtils.getFullStackTrace(sqlException));
        LOG.error(error, sqlException);
      } finally {
        GrouperUtil.closeQuietly(dbConnection);
      }
      
    } catch (Exception e) {
      String error = "Error verifying " + databaseDescription + " database configuration: ";
      System.err.println("Grouper error: " + error + ExceptionUtils.getFullStackTrace(e));
      LOG.error(error, e);
    }
    return false;
  }
  
    
  /**
   * make sure properties file properties match up
   */
  private static void checkConfigProperties() {

    //checkConfigProperties(GROUPER_PROPERTIES_NAME, "grouper.example.properties");
    //checkConfigProperties("grouper.hibernate.properties", "grouper.hibernate.example.properties");
    //checkConfigProperties("morphString.properties", "morphString.example.properties");
    if (GrouperUtil.isBlank(MorphStringConfig.retrieveConfig().propertyValueString("encrypt.key"))) {
      String error = "Error: Grouper expects an encrpyt key (generally a long random alphanumeric string) \"encrypt.key\" in properties file morphString.properties";
      System.err.println(error);
      LOG.error(error);
    }
    checkGrouperConfigDbChange();
    checkGrouperConfigGroupNameValidators();
    checkGrouperConfigIncludeExcludeAndGroups();
    checkGrouperConfigAutocreateGroups();
    checkGrouperConfigCustomComposites();
  }

  /**
   * check the grouper loader db configs
   */
  public static void checkGrouperLoaderConfigDbs() {

    //db.warehouse.user = mylogin
    //db.warehouse.pass = secret
    //db.warehouse.url = jdbc:mysql://localhost:3306/grouper
    //db.warehouse.driver = com.mysql.jdbc.Driver
    //make sure sequences are ok
    Map<String, String> dbMap = GrouperLoaderConfig.retrieveConfig().propertiesMap( 
        grouperLoaderDbPattern);
    while (dbMap.size() > 0) {
      //get one
      String dbKey = dbMap.keySet().iterator().next();
      //get the database name
      Matcher matcher = grouperLoaderDbPattern.matcher(dbKey);
      matcher.matches();
      String dbName = matcher.group(1);
      boolean missingOne = false;
      //now find all 4 required keys
      String userKey = "db." + dbName + ".user";
      if (!dbMap.containsKey(userKey)) {
        String error = "cannot find grouper-loader.properties key: " + userKey; 
        System.out.println("Grouper error: " + error);
        LOG.error(error);
        missingOne = true;
      }
      String passKey = "db." + dbName + ".pass";
      if (!dbMap.containsKey(passKey)) {
        String error = "cannot find grouper-loader.properties key: " + passKey; 
        System.out.println("Grouper error: " + error);
        LOG.error(error);
        missingOne = true;
      }
      String urlKey = "db." + dbName + ".url";
      if (!dbMap.containsKey(urlKey)) {
        String error = "cannot find grouper-loader.properties key: " + urlKey; 
        System.out.println("Grouper error: " + error);
        LOG.error(error);
        missingOne = true;
      }
      String driverKey = "db." + dbName + ".driver";
      if (!dbMap.containsKey(driverKey)) {
        
        //its ok unless we cant convert from url...
        if (!StringUtils.isBlank(urlKey) && StringUtils.isBlank(GrouperDdlUtils.convertUrlToDriverClassIfNeeded(dbMap.get(urlKey), null))) {
        
        String error = "cannot find grouper-loader.properties key: " + driverKey; 
        System.out.println("Grouper error: " + error);
        LOG.error(error);
        missingOne = true;
      }
      }
      if (missingOne) {
        return;
      }
      String user = dbMap.get(userKey);
      String password = dbMap.get(passKey);
      String url = dbMap.get(urlKey);
      String driver = dbMap.get(driverKey);

      //try to connect to database
      checkDatabase(driver, url, user, password, "grouper-loader.properties database name '" + dbName + "'");
      
      dbMap.remove(userKey);
      dbMap.remove(passKey);
      dbMap.remove(urlKey);
      dbMap.remove(driverKey);

    }
    
  }
  
  /**
   * check the grouper loader other jobs configs
   */
  public static void checkGrouperLoaderOtherJobs() {

    //otherJob.duo.class = 
    //otherJob.duo.quartzCron = 
    //otherJob.duo.priority = 
    
    //make sure sequences are ok
    Map<String, String> otherJobMap = GrouperLoaderConfig.retrieveConfig().propertiesMap(
        grouperLoaderOtherJobPattern);
    while (otherJobMap.size() > 0) {
      //get one
      String otherJobKey = otherJobMap.keySet().iterator().next();
      //get the database name
      Matcher matcher = grouperLoaderOtherJobPattern.matcher(otherJobKey);
      matcher.matches();
      String otherJobName = matcher.group(1);
      boolean missingOne = false;
      //now find all required keys
      String classKey = "otherJob." + otherJobName + ".class";
      if (!otherJobMap.containsKey(classKey)) {
        String error = "cannot find grouper-loader.properties key: " + classKey; 
        System.out.println("Grouper error: " + error);
        LOG.error(error);
        missingOne = true;
      }
      String cronKey = "otherJob." + otherJobName + ".quartzCron";
      if (!otherJobMap.containsKey(cronKey)) {
        String error = "cannot find grouper-loader.properties key: " + cronKey; 
        System.out.println("Grouper error: " + error);
        LOG.error(error);
        missingOne = true;
      }
      
      String priorityKey = "otherJob." + otherJobName + ".priority";

      if (missingOne) {
        return;
      }
      String className = otherJobMap.get(classKey);
      @SuppressWarnings("unused")
      String cronName = otherJobMap.get(cronKey);
      
      //check the classname
      try {
        
        @SuppressWarnings("unused")
        Class<? extends Job> theClass = GrouperUtil.forName(className);
        
      } catch (Exception e) {
        String error = "problem finding class: " + classKey + " from grouper-loader.properties: " + className 
          + ", " + ExceptionUtils.getFullStackTrace(e);
        System.out.println("Grouper error: " + error);
        LOG.error(error);
        
      }
      
      otherJobMap.remove(classKey);
      otherJobMap.remove(cronKey);
      otherJobMap.remove(priorityKey);

    }
    
  }
  
  /**
   * check the grouper loader consumer configs
   */
  public static void checkGrouperLoaderConsumers() {

    //changeLog.consumer.ldappc.class = 
    //changeLog.consumer.ldappc.quartz.cron
    
    //make sure sequences are ok
    Map<String, String> consumerMap = GrouperLoaderConfig.retrieveConfig().propertiesMap(
        grouperLoaderConsumerPattern);
    while (consumerMap.size() > 0) {
      //get one
      String consumerKey = consumerMap.keySet().iterator().next();
      //get the database name
      Matcher matcher = grouperLoaderConsumerPattern.matcher(consumerKey);
      matcher.matches();
      String consumerName = matcher.group(1);
      boolean missingOne = false;
      //now find all 4 required keys
      String classKey = "changeLog.consumer." + consumerName + ".class";
      if (!consumerMap.containsKey(classKey)) {
        String error = "cannot find grouper-loader.properties key: " + classKey; 
        System.out.println("Grouper error: " + error);
        LOG.error(error);
        missingOne = true;
      }
      String cronKey = "changeLog.consumer." + consumerName + ".quartzCron";
      if (!consumerMap.containsKey(cronKey)) {
        String error = "cannot find grouper-loader.properties key: " + cronKey; 
        System.out.println("Grouper error: " + error);
        LOG.error(error);
        missingOne = true;
      }
      if (missingOne) {
        return;
      }
      String className = consumerMap.get(classKey);
      @SuppressWarnings("unused")
      String cronName = consumerMap.get(cronKey);
      
      //check the classname
      try {
        
        Class<?> theClass = GrouperUtil.forName(className);
        if (!ChangeLogConsumerBase.class.isAssignableFrom(theClass)) {
          String error = "class in grouper-loader.properties: " + classKey + " must extend : " 
            + ChangeLogConsumerBase.class.getName() + " : offendingClass: " + className; 
          System.out.println("Grouper error: " + error);
          LOG.error(error);
        }
        
      } catch (Exception e) {
        String error = "problem finding class: " + classKey + " from grouper-loader.properties: " + className 
          + ", " + ExceptionUtils.getFullStackTrace(e);
        System.out.println("Grouper error: " + error);
        LOG.error(error);
        
      }
      
      consumerMap.remove(classKey);
      consumerMap.remove(cronKey);

    }
    
  }

  /**
   * check custom composites
   */
  private static void checkGrouperConfigCustomComposites() {
    //#grouper.membership.customComposite.uiKey.0 = customCompositeMinusEmployees
    //#grouper.membership.customComposite.compositeType.0 = complement
    //#grouper.membership.customComposite.groupName.0 = ref:activeEmployees

    //make sure sequences are ok
    Map<String, String> keys = GrouperConfig.retrieveConfig().propertiesMap(customCompositePattern);
    int i=0;
    while (true) {
      boolean foundOne = false;
      String uiKeyKey = "grouper.membership.customComposite.uiKey." + i;
      String compositeTypeKey = "grouper.membership.customComposite.compositeType." + i;
      String groupNameKey = "grouper.membership.customComposite.groupName." + i;

      foundOne = assertAndRemove(GROUPER_PROPERTIES_NAME, keys,
          new String[]{uiKeyKey, compositeTypeKey, groupNameKey});
      if (!foundOne) {
        break;
      }
      i++;
    }
    if (keys.size() > 0) {
      String error = "in property file: grouper.properties, these properties " +
          "are misspelled or non-sequential: " + GrouperUtil.setToString(keys.keySet());
      System.err.println("Grouper error: " + error);
      LOG.error(error);
    }

  }
  
  /**
   * check the grouper config group name validators
   */
  private static void checkGrouperConfigGroupNameValidators() {
    //#group.attribute.validator.attributeName.0=extension
    //#group.attribute.validator.regex.0=^[a-zA-Z0-9]+$
    //#group.attribute.validator.vetoMessage.0=Group ID '$attributeValue$' is invalid since it must contain only alpha-numerics
    
    //make sure sequences are ok
    Map<String, String> validatorKeys = GrouperConfig.retrieveConfig().propertiesMap(groupValidatorPattern);
    int i=0;
    while (true) {
      boolean foundOne = false;
      String attributeNameKey = "group.attribute.validator.attributeName." + i;
      String regexKey = "group.attribute.validator.regex." + i;
      String vetoMessageKey = "group.attribute.validator.vetoMessage." + i;

      foundOne = assertAndRemove(GROUPER_PROPERTIES_NAME, validatorKeys, 
          new String[]{attributeNameKey, regexKey, vetoMessageKey});
      if (!foundOne) {
        break;
      }
      i++;
    }
    if (validatorKeys.size() > 0) {
      String error = "in property file: grouper.properties, these properties " +
          "are misspelled or non-sequential: " + GrouperUtil.setToString(validatorKeys.keySet());
      System.err.println("Grouper error: " + error);
      LOG.error(error);
    }

  }

  /**
   * check the grouper config group name validators
   */
  private static void checkGrouperConfigAutocreateGroups() {
    //#configuration.autocreate.group.name.0 = etc:uiUsers
    //#configuration.autocreate.group.description.0 = users allowed to log in to the UI
    //#configuration.autocreate.group.subjects.0 = johnsmith
    
    //make sure sequences are ok
    Map<String, String> validatorKeys = GrouperConfig.retrieveConfig().propertiesMap(autocreateGroupsPattern);
    int i=0;
    while (true) {
      boolean foundOne = false;
      String nameKey = "configuration.autocreate.group.name." + i;
      String descriptionKey = "configuration.autocreate.group.description." + i;
      String subjectsKey = "configuration.autocreate.group.subjects." + i;

      foundOne = assertAndRemove(GROUPER_PROPERTIES_NAME, validatorKeys, 
          new String[]{nameKey, descriptionKey, subjectsKey});

      if (!foundOne) {
        break;
      }
      i++;
    }
    if (validatorKeys.size() > 0) {
      String error = "in property file: grouper.properties, these properties " +
          "are misspelled or non-sequential: " + GrouperUtil.setToString(validatorKeys.keySet());
      System.err.println("Grouper error: " + error);
      LOG.error(error);
    }

  }

  /**
   * check the grouper config group name validators
   */
  private static void checkGrouperConfigIncludeExcludeAndGroups() {
    //#grouperIncludeExclude.requireGroup.name.0 = activeEmployee
    //#grouperIncludeExclude.requireGroup.group.0 = school:community:activeEmployee
    //#grouperIncludeExclude.requireGroup.description.0 = If value is true, members of the overall group must be an active employee.  Otherwise, leave this value not filled in.
    
    //make sure sequences are ok
    Map<String, String> validatorKeys = GrouperConfig.retrieveConfig().propertiesMap(includeExcludeAndGroupPattern);
    int i=0;
    while (true) {
      boolean foundOne = false;
      String nameKey = "grouperIncludeExclude.requireGroup.name." + i;
      String attributeOrTypeKey = "grouperIncludeExclude.requireGroup.attributeOrType." + i;
      String regexKey = "grouperIncludeExclude.requireGroup.group." + i;
      String vetoMessageKey = "grouperIncludeExclude.requireGroup.description." + i;

      foundOne = assertAndRemove(GROUPER_PROPERTIES_NAME, validatorKeys, 
          new String[]{nameKey, attributeOrTypeKey, regexKey, vetoMessageKey});
      if (!foundOne) {
        break;
      }
      i++;
    }
    if (validatorKeys.size() > 0) {
      String error = "in property file: grouper.properties, these properties " +
          "are misspelled or non-sequential: " + GrouperUtil.setToString(validatorKeys.keySet());
      System.err.println("Grouper error: " + error);
      LOG.error(error);
    }

  }

  /**
   * check db change properties in the grouper config
   */
  private static void checkGrouperConfigDbChange() {
    //make sure sequences are ok
    Map<String, String> dbChangeKeys = GrouperConfig.retrieveConfig().propertiesMap(dbChangePattern);
    int i=0;
    //db.change.allow.user.0=grouper3
    //db.change.allow.url.0=jdbc:mysql://localhost:3306/grouper3
    while (true) {
      boolean foundOne = false;
      String allowUserKey = "db.change.allow.user." + i;
      String allowUrlKey = "db.change.allow.url." + i;
      String denyUserKey = "db.change.deny.user." + i;
      String denyUrlKey = "db.change.deny.url." + i;
      //note, not short circuit OR since needs to evaluate both
      foundOne = assertAndRemove(GROUPER_PROPERTIES_NAME, dbChangeKeys, new String[]{allowUserKey, allowUrlKey})
        | assertAndRemove(GROUPER_PROPERTIES_NAME, dbChangeKeys, new String[]{denyUserKey, denyUrlKey});
      if (!foundOne) {
        break;
      }
      i++;
    }
    if (dbChangeKeys.size() > 0) {
      String error = "in property file: grouper.properties, these properties " +
          "are misspelled or non-sequential: " + GrouperUtil.setToString(dbChangeKeys.keySet());
      System.err.println("Grouper error: " + error);
      LOG.error(error);
    }
  }

  /**
   * if one there, then they all must be there, and remove, return true if found one
   * @param resourceName
   * @param set of properties that match this pattern
   * @param propertiesNames
   * @return true if found one
   */
  public static boolean assertAndRemove(String resourceName, 
      Map<String, String> set, String[] propertiesNames) {
    boolean foundOne = false;
    for (String propertyName : propertiesNames) {
      if (set.containsKey(propertyName)) {
        foundOne = true;
        break;
      }
    }
    if (foundOne) {
      for (String propertyName : propertiesNames) {
        if (set.containsKey(propertyName)) {
          set.remove(propertyName);
        } else {
          String error = "expecting property " + propertyName 
            + " in config file: " + resourceName + " since related properties exist";
          System.err.println("Grouper error: " + error);
          LOG.error(error);
        }
      }
    }
    return foundOne;
  }
  
  /** properties in manifest for version */
  private static final String[] versionProperties = new String[]{
    "Implementation-Version","Version"};
  
  /**
   * get the version from the manifest of a jar
   * @param sampleClass
   * @return the version
   * @throws Exception
   */
  public static String jarVersion(Class sampleClass) throws Exception {
    return manifestProperty(sampleClass, versionProperties);
  }

  /**
   * get the version from the manifest of a jar
   * @param sampleClass
   * @param propertyNames that we are looking for (usually just one)
   * @return the version
   * @throws Exception
   */
  public static String manifestProperty(Class sampleClass, String[] propertyNames) throws Exception {
    File jarFile = GrouperUtil.jarFile(sampleClass, true);
    URL manifestUrl = new URL("jar:file:" + jarFile.getCanonicalPath() + "!/META-INF/MANIFEST.MF");
    Manifest manifest = new Manifest(manifestUrl.openStream());
    Map<String, Attributes> attributeMap = manifest.getEntries();
    String value = null;
    for (String propertyName : propertyNames) {
      value = manifest.getMainAttributes().getValue(propertyName);
      if (!StringUtils.isBlank(value)) {
        break;
      }
    }
    if (value == null) {
      OUTER:
      for (Attributes attributes: attributeMap.values()) {
        for (String propertyName : propertyNames) {
          value = attributes.getValue(propertyName);
          if (!StringUtils.isBlank(value)) {
            break OUTER;
          }
        }
      }
    }
    if (value == null) {
      
      for (Attributes attributes: attributeMap.values()) {
        for (Object key : attributes.keySet()) {
          LOG.info(jarFile.getName() + ", " + key + ": " + attributes.getValue((Name)key));
        }
      }
      Attributes attributes = manifest.getMainAttributes();
      for (Object key : attributes.keySet()) {
        LOG.info(jarFile.getName() + ", " + key + ": " + attributes.getValue((Name)key));
      }
    }
    return value;
  }

  /** match something like this: db.change.allow.url.1 */
  private static Pattern dbChangePattern = Pattern.compile(
      "^db\\.change\\.(deny|allow)\\.(user|url).\\d+$");
  
  /** match something like this: group.attribute.validator.attributeName.0 */
  private static Pattern groupValidatorPattern = Pattern.compile(
      "^group\\.attribute\\.validator\\.(attributeName|regex|vetoMessage)\\.\\d+$");

  /** match something like this: grouper.membership.customComposite.uiKey.0 */
  private static Pattern customCompositePattern = Pattern.compile(
      "^grouper\\.membership\\.customComposite\\.(uiKey|compositeType|groupName)\\.\\d+$");
  
  /** match something like this: grouperIncludeExclude.requireGroup.name.0 */
  private static Pattern includeExcludeAndGroupPattern = Pattern.compile(
      "^grouperIncludeExclude\\.requireGroup\\.(name|attributeOrType|group|description)\\.\\d+$");
  
  /** match something like this: configuration.autocreate.group.name.0 */
  private static Pattern autocreateGroupsPattern = Pattern.compile(
      "^configuration\\.autoCreate\\.(name|description|subjects)\\.\\d+$");
  
  /**
   * match something like this: db.warehouse.pass
   */
  private static Pattern grouperLoaderDbPattern = Pattern.compile(
      "^db\\.(\\w+)\\.(pass|url|driver|user)$");
  
  /**
   * match something like this: changeLog.consumer.ldappc.class, changeLog.consumer.ldappc.quartzCron
   */
  public static Pattern grouperLoaderConsumerPattern = Pattern.compile(
      "^changeLog\\.consumer\\.(\\w+)\\.(class|quartzCron)$");
  
  /**
   * match something like this: changeLog.consumer.ldappc.class, changeLog.consumer.ldappc.quartzCron
   */
  public static Pattern messagingListenerConsumerPattern = Pattern.compile(
      "^messaging\\.listener\\.(\\w+)\\.(.*)$");
  
  /**
   * match something like this: otherJob.duo.class, otherJob.duo.quartzCron, otherJob.duo.priority
   */
  public static Pattern grouperLoaderOtherJobPattern = Pattern.compile(
      "^otherJob\\.(\\w+)\\.(class|quartzCron|priority)$");
  
  /**
   * <pre>
   * match type security
   * match: security.typeName.wheelOnly
   * match: security.typeName.allowOnlyGroup
   * </pre>
   */
  public static final Pattern typeSecurityPattern = Pattern.compile(
      "^security\\.types\\.(.*)\\.(wheelOnly|allowOnlyGroup)$");
  
  /**
   * <pre>
   * match security for search and sort strings
   * match: security.member.sort.string[0-4].allowOnlyGroup
   * match: security.member.sort.string[0-4].wheelOnly
   * match: security.member.search.string[0-4].allowOnlyGroup
   * match: security.member.search.string[0-4].wheelOnly
   * </pre>
   */
  public static final Pattern memberSortSearchSecurityPattern = Pattern.compile(
      "^security\\.member\\.(sort|search)\\.(string[0-4])\\.(wheelOnly|allowOnlyGroup)$");
  
  /**
   * return true if this is an exception case, dont worry about it
   * @param resourceName
   * @param propertyName
   * @param missingPropertyInFile true if missing property in file, false if
   * extra property in file
   * @return true if exception case
   */
  public static boolean nonStandardProperty(String resourceName, String propertyName,
      boolean missingPropertyInFile) {
    if (StringUtils.equals(resourceName, GROUPER_PROPERTIES_NAME)) {
      if (dbChangePattern.matcher(propertyName).matches()) {
        return true;
      }
      if (groupValidatorPattern.matcher(propertyName).matches()) {
        return true;
      }
      if (includeExcludeAndGroupPattern.matcher(propertyName).matches()) {
        return true;
      }
      if (autocreateGroupsPattern.matcher(propertyName).matches()) {
        return true;
      }
      if (typeSecurityPattern.matcher(propertyName).matches()) {
        return true;
      }
      if (memberSortSearchSecurityPattern.matcher(propertyName).matches()) {
        return true;
      }
      if (customCompositePattern.matcher(propertyName).matches()) {
        return true;
      }
    }
    if (StringUtils.equals(resourceName, "grouper.hibernate.properties")
      || !missingPropertyInFile) {
      return true;
    }
    if (StringUtils.equals(resourceName, "grouper-loader.properties")) {
      if (grouperLoaderDbPattern.matcher(propertyName).matches()) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * return if in check config
   * @return if in check config
   */
  public static boolean isInCheckConfig() {
    return inCheckConfig;
  }
  
  /**
   * compare a properties file with an example file, compare all the properties
   * @param resourceName
   * @param resourceExampleName
   */
  public static void checkConfigProperties(String resourceName, 
      String resourceExampleName) {
    
    Properties propertiesFromFile = GrouperUtil.propertiesFromResourceName(resourceName);
    Properties propertiesFromExample = GrouperUtil.propertiesFromResourceName(resourceExampleName);
    String exampleFileContents = GrouperUtil.readResourceIntoString(resourceExampleName, false);
    
    //find properties missing from file:
    Set<String> missingProps = new HashSet<String>();
    for (String key: (Set<String>)(Object)propertiesFromExample.keySet()) {
      if (!propertiesFromFile.containsKey(key)) {
        if (!nonStandardProperty(resourceName, key, true)) {
          missingProps.add(key);
        }
      }
    }
    if (missingProps.size() > 0) {
      String error = "missing from file: " + resourceName + ", the following " +
          "properties (which are in the example file: " + resourceExampleName
          + "): " + GrouperUtil.setToString(missingProps);
      System.err.println("Grouper warning: " + error);
      LOG.warn(error);
    }
    
    //find extra properties in file:
    missingProps.clear();
    for (String key: (Set<String>)(Object)propertiesFromFile.keySet()) {
      //dont look in properties, look in file, since could be commented out
      if (!exampleFileContents.contains(key)) {
        if (!nonStandardProperty(resourceName, key, false)) {
          missingProps.add(key);
        }
      }
    }
    if (missingProps.size() > 0) {
      String error = "properties are in file: " + resourceName + " (but not in " +
          "the example file: " + resourceExampleName
          + "): " + GrouperUtil.setToString(missingProps);
      System.err.println("Grouper warning: " + error);
      LOG.warn(error);
    }
  }

  /**
   * make sure an attribute is there or add it if not
   * @param stem
   * @param attributeDef 
   * @param extension
   * @param description
   * @param logAutocreate 
   * @return the attribute def name
   */
  private static AttributeDefName checkAttribute(Stem stem, AttributeDef attributeDef, String extension, String description, boolean logAutocreate) {
    return checkAttribute(stem, attributeDef, extension, extension, description, logAutocreate);
  }
  
  /**
   * make sure an attribute is there or add it if not
   * @param stem
   * @param attributeDef 
   * @param extension
   * @param displayExtension
   * @param description
   * @param logAutocreate 
   * @return the attribute def name
   */
  public static AttributeDefName checkAttribute(Stem stem, AttributeDef attributeDef, String extension, String displayExtension, String description, boolean logAutocreate) {
    String attributeDefNameName = stem.getName() + ":" + extension;
    
    //dont cache since if not there, that not there will be cached
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(attributeDefNameName, false, new QueryOptions().secondLevelCache(false));

    if (attributeDefName == null) {
      try {
        attributeDefName = stem.addChildAttributeDefName(attributeDef, extension, displayExtension);
      } catch (RuntimeException theException) {
        GrouperUtil.sleep(3000);
        attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(attributeDefNameName, false, new QueryOptions().secondLevelCache(false));
        if (attributeDefName == null) {
          throw theException;
        }
        return attributeDefName;
      }
      attributeDefName.setDescription(description);
      attributeDefName.store();
      
      if (logAutocreate) {
        String error = "auto-created attributeDefName: " + attributeDefNameName;
        System.err.println("Grouper note: " + error);
        LOG.warn(error);
      }
    }
    return attributeDefName;
  }

  /**
   * return the stem name where the attribute loader attributes go, without colon on end
   * @return stem name
   */
  public static String attributeLoaderStemName() {
    String rootStemName = attributeRootStemName();
    
    //namespace this separate from other builtins
    rootStemName += ":attrLoader";
    return rootStemName;
  }

  /**
   * root stem where attributes live
   * @return attribute built in stem name
   */
  public static String attributeRootStemName() {
    String rootStemName = GrouperConfig.retrieveConfig().propertyValueString("grouper.attribute.rootStem");
    if (StringUtils.isBlank(rootStemName)) {
      throw new RuntimeException("If autoconfiguring attributes, you need to configure a root stem");
    }
    return rootStemName;
  }
  
  /**
   * 
   * @return the stem name
   */
  public static String loaderMetadataStemName() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":attribute:loaderMetadata";
  }

  /**
   * call this to init data in grouper
   */
  public static void checkObjects() {
    checkGroups();
    checkAttributes();
    GrouperStartup.initLoaderType();
    checkConfig2();
  }
  
  /**
   * make sure configured attributes are there 
   */
  private static void checkAttributes() {
    
    boolean autoconfigure = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.attribute.loader.autoconfigure", true);
    if (!autoconfigure) {
      return;
    }

    boolean wasInCheckConfig = inCheckConfig;
    if (!wasInCheckConfig) {
      inCheckConfig = true;
    }

    GrouperSession grouperSession = null;
    boolean startedGrouperSession = false;
    try {
      grouperSession = GrouperSession.staticGrouperSession(false);

      if (grouperSession == null) {
        grouperSession = GrouperSession.startRootSession();
        startedGrouperSession = true;
      }
      
      //clear this for tests
      ExpirableCache.clearAll();
        
      legacyAttributeBaseStem(grouperSession);
      
      {
        StemUniqueNameCaseInsensitiveHook.registerHookIfNecessary();
        GroupUniqueNameCaseInsensitiveHook.registerHookIfNecessary();
        AttributeDefUniqueNameCaseInsensitiveHook.registerHookIfNecessary();
        AttributeDefNameUniqueNameCaseInsensitiveHook.registerHookIfNecessary();
      }
      
      boolean autoAssignTheAutoAssignAttributes = false;
      AttributeDefName attributeAutoCreateMarker = null;
      AttributeDef attributeAutoCreateDef = null;
      AttributeDefName autoAssignIfName = null;
      AttributeDefName autoAssignThenNames = null;
      {
        
        String attributeAutoCreateStemName = AttributeAutoCreateHook.attributeAutoCreateStemName();
        
        Stem attributeAutoCreateStem = StemFinder.findByName(grouperSession, attributeAutoCreateStemName, false);
        if (attributeAutoCreateStem == null) {
          attributeAutoCreateStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for attribute autocreate objects").assignName(attributeAutoCreateStemName)
            .save();
        }

        //see if attributeDef is there
        String attributeAutoCreateDefName = attributeAutoCreateStemName + ":" + AttributeAutoCreateHook.GROUPER_ATTRIBUTE_AUTO_CREATE_MARKER_DEF;
        attributeAutoCreateDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            attributeAutoCreateDefName, false, new QueryOptions().secondLevelCache(false));
        if (attributeAutoCreateDef == null) {
          attributeAutoCreateDef = attributeAutoCreateStem.addChildAttributeDef(AttributeAutoCreateHook.GROUPER_ATTRIBUTE_AUTO_CREATE_MARKER_DEF, 
              AttributeDefType.attr);
          attributeAutoCreateDef.setMultiAssignable(true);
          attributeAutoCreateDef.setAssignToAttributeDef(true);
          attributeAutoCreateDef.store();
        }
        
        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(attributeAutoCreateDef);
        

        //add a name
        attributeAutoCreateMarker = checkAttribute(attributeAutoCreateStem, attributeAutoCreateDef, 
            AttributeAutoCreateHook.GROUPER_ATTRIBUTE_AUTO_CREATE_MARKER, 
            "has autocreate settings settings", wasInCheckConfig);
        
        //lets add some rule attributes
        String attributeAutoCreateValueDefName = attributeAutoCreateStemName + ":" + AttributeAutoCreateHook.GROUPER_ATTRIBUTE_AUTO_CREATE_VALUE_DEF;
        AttributeDef attributeAutoCreateValueDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
            attributeAutoCreateValueDefName, false, new QueryOptions().secondLevelCache(false));
        
        if (attributeAutoCreateValueDef == null) {
          attributeAutoCreateValueDef = attributeAutoCreateStem.addChildAttributeDef(
              AttributeAutoCreateHook.GROUPER_ATTRIBUTE_AUTO_CREATE_VALUE_DEF, AttributeDefType.attr);
          attributeAutoCreateValueDef.setAssignToAttributeDefAssn(true);
          attributeAutoCreateValueDef.setValueType(AttributeDefValueType.string);
          attributeAutoCreateValueDef.store();
          autoAssignTheAutoAssignAttributes = true;
        }

        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(attributeAutoCreateValueDef);

        //the attributes can only be assigned to the type def
        // try an attribute def dependent on an attribute def name
        attributeAutoCreateValueDef.getAttributeDefScopeDelegate().assignOwnerNameEquals(attributeAutoCreateMarker.getName());

        //add some names
        autoAssignIfName = checkAttribute(attributeAutoCreateStem, attributeAutoCreateValueDef, AttributeAutoCreateHook.GROUPER_ATTRIBUTE_AUTO_CREATE_ATTR_IF_NAME, 
            "If an attribute is assigned with this name of attribute def name", wasInCheckConfig);
        autoAssignThenNames = checkAttribute(attributeAutoCreateStem, attributeAutoCreateValueDef, AttributeAutoCreateHook.GROUPER_ATTRIBUTE_AUTO_CREATE_ATTR_THEN_NAMES_ON_ASSIGN, 
            "Then assign these comma separated names of attribute def names to the assignment of the first name that was assigned", wasInCheckConfig);
        
        AttributeAutoCreateHook.registerHookIfNecessary();

      }

      {
        String notificationLastSentStemName = NotificationDaemon.attributeAutoCreateStemName();

        Stem notificationLastSentStem = StemFinder.findByName(grouperSession, notificationLastSentStemName, false, new QueryOptions().secondLevelCache(false));
        if (notificationLastSentStem == null) {
          notificationLastSentStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in external subject invite attributes, and holds the data via attributes for invites.  Dont delete this folder")
            .assignName(notificationLastSentStemName).save();
        }

        //see if attributeDef is there
        String notificationLastSentDefName = notificationLastSentStemName + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT_DEF;

        AttributeDef notificationLastSentDef = new AttributeDefSave(grouperSession).assignName(notificationLastSentDefName)
          .assignToImmMembership(true).assignMultiAssignable(false).assignMultiValued(false).assignValueType(AttributeDefValueType.string)
          .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).save();

        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(notificationLastSentDef);

        //add a name
        checkAttribute(notificationLastSentStem, notificationLastSentDef, 
            NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT, "yyyy/mm/dd.  Represents last date notification was sent", wasInCheckConfig);
      }

      {
        String externalSubjectStemName = ExternalSubjectAttrFramework.attributeExternalSubjectInviteStemName();
        
        Stem externalSubjectStem = StemFinder.findByName(grouperSession, externalSubjectStemName, false, new QueryOptions().secondLevelCache(false));
        if (externalSubjectStem == null) {
          externalSubjectStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in external subject invite attributes, and holds the data via attributes for invites.  Dont delete this folder")
            .assignName(externalSubjectStemName).save();
        }


        //see if attributeDef is there
        String externalSubjectInviteDefName = externalSubjectStemName + ":externalSubjectInviteDef";
        
        AttributeDef externalSubjectInviteType = new AttributeDefSave(grouperSession).assignName(externalSubjectInviteDefName)
          .assignToStem(true).assignMultiAssignable(true).assignAttributeDefType(AttributeDefType.type).assignCreateParentStemsIfNotExist(true).save();
          
        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(externalSubjectInviteType);
        
        //add a name
        AttributeDefName externalSubjectInvite = checkAttribute(externalSubjectStem, externalSubjectInviteType, "externalSubjectInvite", "is an invite", wasInCheckConfig);
        
        //lets add some rule attributes
        String externalSubjectInviteAttrDefName = externalSubjectStemName + ":externalSubjectInviteAttrDef";
        AttributeDef externalSubjectInviteAttrType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            externalSubjectInviteAttrDefName, false, new QueryOptions().secondLevelCache(false));

        if (externalSubjectInviteAttrType == null) {
          externalSubjectInviteAttrType = externalSubjectStem.addChildAttributeDef("externalSubjectInviteAttrDef", AttributeDefType.attr);
          externalSubjectInviteAttrType.setAssignToStemAssn(true);
          externalSubjectInviteAttrType.setValueType(AttributeDefValueType.string);
          externalSubjectInviteAttrType.store();
        }

        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(externalSubjectInviteAttrType);
        

        //the attributes can only be assigned to the type def
        // try an attribute def dependent on an attribute def name
        externalSubjectInviteAttrType.getAttributeDefScopeDelegate().assignOwnerNameEquals(externalSubjectInvite.getName());

        //add some names
        checkAttribute(externalSubjectStem, externalSubjectInviteAttrType, ExternalSubjectAttrFramework.EXTERNAL_SUBJECT_INVITE_EXPIRE_DATE, 
            "number of millis since 1970 when this invite expires", wasInCheckConfig);
        checkAttribute(externalSubjectStem, externalSubjectInviteAttrType, ExternalSubjectAttrFramework.EXTERNAL_SUBJECT_INVITE_DATE, 
            "number of millis since 1970 that this invite was issued", wasInCheckConfig);
        checkAttribute(externalSubjectStem, externalSubjectInviteAttrType, ExternalSubjectAttrFramework.EXTERNAL_SUBJECT_EMAIL_ADDRESS, 
            "email address this invite was sent to", wasInCheckConfig);
        checkAttribute(externalSubjectStem, externalSubjectInviteAttrType, ExternalSubjectAttrFramework.EXTERNAL_SUBJECT_INVITE_GROUP_UUIDS, 
            "comma separated group ids to assign this user to", wasInCheckConfig);
        checkAttribute(externalSubjectStem, externalSubjectInviteAttrType, ExternalSubjectAttrFramework.EXTERNAL_SUBJECT_INVITE_MEMBER_ID, 
            "member id who invited this user", wasInCheckConfig);
        checkAttribute(externalSubjectStem, externalSubjectInviteAttrType, ExternalSubjectAttrFramework.EXTERNAL_SUBJECT_INVITE_UUID, 
            "unique id in the email sent to the user", wasInCheckConfig);
        checkAttribute(externalSubjectStem, externalSubjectInviteAttrType, ExternalSubjectAttrFramework.EXTERNAL_SUBJECT_INVITE_EMAIL_WHEN_REGISTERED, 
            "email addresses to notify when the user registers", wasInCheckConfig);
        checkAttribute(externalSubjectStem, externalSubjectInviteAttrType, ExternalSubjectAttrFramework.EXTERNAL_SUBJECT_INVITE_EMAIL,
            "email sent to user as invite", wasInCheckConfig);      
      
      }      

      {
        String messagesRootStemName = GrouperBuiltinMessagingSystem.messageRootStemName();

        Stem messagesStem = StemFinder.findByName(grouperSession, messagesRootStemName, false, new QueryOptions().secondLevelCache(false));
        if (messagesStem == null) {
          messagesStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for message queues and topics, topic to queue relationships and permissions")
            .assignName(messagesRootStemName)
            .save();
          if (wasInCheckConfig) {
            String error = "auto-created stem: " + messagesRootStemName;
            System.err.println("Grouper note: " + error);
            LOG.warn(error);
          }
        }
        
        {
          //see if role for permissions is there
          String grouperMessageNameOfRole = GrouperBuiltinMessagingSystem.grouperMessageNameOfRole();
          Group groupMessagingRoleGroup = GrouperDAOFactory.getFactory().getGroup().findByNameSecure(
              grouperMessageNameOfRole, false, new QueryOptions().secondLevelCache(false), GrouperUtil.toSet(TypeOfGroup.role));
          if (groupMessagingRoleGroup == null) {
            groupMessagingRoleGroup = (Group)messagesStem.addChildRole(GrouperUtil.extensionFromName(grouperMessageNameOfRole), 
                GrouperUtil.extensionFromName(grouperMessageNameOfRole));
            if (wasInCheckConfig) {
              String error = "auto-created role: " + groupMessagingRoleGroup.getName();
              System.err.println("Grouper note: " + error);
              LOG.warn(error);
            }
          }
          GroupFinder.groupCacheAsRootAddSystemGroup(groupMessagingRoleGroup);
        }

        {
          //see if attributeDef for topics is there
          String grouperMessageTopicNameOfDef = GrouperBuiltinMessagingSystem.grouperMessageTopicNameOfDef();
          AttributeDef grouperMessageTopicDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
              grouperMessageTopicNameOfDef, false, new QueryOptions().secondLevelCache(false));
          if (grouperMessageTopicDef == null) {
            grouperMessageTopicDef = messagesStem.addChildAttributeDef(GrouperUtil.extensionFromName(grouperMessageTopicNameOfDef), AttributeDefType.perm);
            grouperMessageTopicDef.setAssignToGroup(true);
            grouperMessageTopicDef.setAssignToEffMembership(true);
            grouperMessageTopicDef.store();
            if (wasInCheckConfig) {
              String error = "auto-created attributeDef: " + grouperMessageTopicNameOfDef;
              System.err.println("Grouper note: " + error);
              LOG.warn(error);
            }
            
          }
          
          Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(grouperMessageTopicDef);
          

          grouperMessageTopicDef.getAttributeDefActionDelegate().configureActionList(GrouperBuiltinMessagingSystem.actionSendToTopic);
        }

        {
          //see if attributeDef for queues is there
          String grouperMessageQueueNameOfDef = GrouperBuiltinMessagingSystem.grouperMessageQueueNameOfDef();
          AttributeDef grouperMessageQueueDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
              grouperMessageQueueNameOfDef, false, new QueryOptions().secondLevelCache(false));
          if (grouperMessageQueueDef == null) {
            grouperMessageQueueDef = messagesStem.addChildAttributeDef(GrouperUtil.extensionFromName(grouperMessageQueueNameOfDef), AttributeDefType.perm);
            grouperMessageQueueDef.setAssignToGroup(true);
            grouperMessageQueueDef.setAssignToEffMembership(true);
            grouperMessageQueueDef.store();
            if (wasInCheckConfig) {
              String error = "auto-created attributeDef: " + grouperMessageQueueNameOfDef;
              System.err.println("Grouper note: " + error);
              LOG.warn(error);
            }
          }
          Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(grouperMessageQueueDef);

          grouperMessageQueueDef.getAttributeDefActionDelegate().configureActionList(
              GrouperBuiltinMessagingSystem.actionSendToQueue + "," + GrouperBuiltinMessagingSystem.actionReceive);
        }

        {
          String topicStemName = GrouperBuiltinMessagingSystem.topicStemName();
          Stem topicStem = StemFinder.findByName(grouperSession, topicStemName, false, new QueryOptions().secondLevelCache(false));
          if (topicStem == null) {
            topicStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
              .assignDescription("folder for message topics, add a permission here for a topic, imply queues by the topic")
              .assignName(topicStemName)
              .save();
            if (wasInCheckConfig) {
              String error = "auto-created stem: " + topicStemName;
              System.err.println("Grouper note: " + error);
              LOG.warn(error);
            }
          }
        }        

        {
          String queueStemName = GrouperBuiltinMessagingSystem.queueStemName();
          Stem queueStem = StemFinder.findByName(grouperSession, queueStemName, false);
          if (queueStem == null) {
            queueStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
              .assignDescription("folder for message queues, add a permission here for a queue, implied queues by the topic")
              .assignName(queueStemName)
              .save();
            if (wasInCheckConfig) {
              String error = "auto-created stem: " + queueStemName;
              System.err.println("Grouper note: " + error);
              LOG.warn(error);
            }
          }
        }        

      }
      {
        
        String attestationRootStemName = GrouperAttestationJob.attestationStemName();
        
        Stem attestationStem = StemFinder.findByName(grouperSession, attestationRootStemName, false);
        if (attestationStem == null) {
          attestationStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in Grouper attestation attributes").assignName(attestationRootStemName)
            .save();
        }

        //see if attributeDef is there
        String attestationTypeDefName = attestationRootStemName + ":attestationDef";
        AttributeDef attestationType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            attestationTypeDefName, false, new QueryOptions().secondLevelCache(false));
        if (attestationType == null) {
          attestationType = attestationStem.addChildAttributeDef("attestationDef", AttributeDefType.type);
          attestationType.setAssignToGroup(true);
          attestationType.setAssignToStem(true);
          attestationType.store();
        }
        
        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(attestationType);
        

        //add a name
        AttributeDefName attribute = checkAttribute(attestationStem, attestationType, "attestation", "has attestation attributes", wasInCheckConfig);
        
        //lets add some rule attributes
        String attestationAttrDefName = attestationRootStemName + ":attestationValueDef";
        AttributeDef attestationAttrType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
            attestationAttrDefName, false, new QueryOptions().secondLevelCache(false));
        if (attestationAttrType == null) {
          attestationAttrType = attestationStem.addChildAttributeDef("attestationValueDef", AttributeDefType.attr);
          attestationAttrType.setAssignToGroupAssn(true);
          attestationAttrType.setAssignToStemAssn(true);
          attestationAttrType.setValueType(AttributeDefValueType.string);
          attestationAttrType.store();
        }

        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(attestationAttrType);

        //the attributes can only be assigned to the type def
        // try an attribute def dependent on an attribute def name
        attestationAttrType.getAttributeDefScopeDelegate().assignOwnerNameEquals(attribute.getName());

        //add some names
        checkAttribute(attestationStem, attestationAttrType, GrouperAttestationJob.ATTESTATION_DATE_CERTIFIED, 
            "Last certified date for this group", wasInCheckConfig);
        checkAttribute(attestationStem, attestationAttrType, GrouperAttestationJob.ATTESTATION_DAYS_BEFORE_TO_REMIND,
            "Number of days before attestation deadline to start sending emails about it to owners", wasInCheckConfig);
        checkAttribute(attestationStem, attestationAttrType, GrouperAttestationJob.ATTESTATION_DAYS_UNTIL_RECERTIFY,
            "Number of days until need to recertify from last certification", wasInCheckConfig);
        checkAttribute(attestationStem, attestationAttrType, GrouperAttestationJob.ATTESTATION_DIRECT_ASSIGNMENT,
            "If this group has attestation settings and not inheriting from ancestor folders (group only)", wasInCheckConfig);
        checkAttribute(attestationStem, attestationAttrType, GrouperAttestationJob.ATTESTATION_EMAIL_ADDRESSES,
            "Comma separated email addresses to send reminders to, if blank then send to group admins", wasInCheckConfig);
        checkAttribute(attestationStem, attestationAttrType, GrouperAttestationJob.ATTESTATION_LAST_EMAILED_DATE,
            "yyyy/mm/dd date that this was last emailed so multiple emails don't go out on same day (group only)", wasInCheckConfig);
        checkAttribute(attestationStem, attestationAttrType, GrouperAttestationJob.ATTESTATION_MIN_CERTIFIED_DATE,
            "yyyy/mm/dd date that folder set certification now. Any groups in this folder will have this date at a minimum of last certified date.", wasInCheckConfig);
        checkAttribute(attestationStem, attestationAttrType, GrouperAttestationJob.ATTESTATION_CALCULATED_DAYS_LEFT,
            "In order to search for attestations, this is the calculated days left before needs attestation", wasInCheckConfig);
        checkAttribute(attestationStem, attestationAttrType, GrouperAttestationJob.ATTESTATION_SEND_EMAIL,
            "true or false if emails should be sent", wasInCheckConfig);
        checkAttribute(attestationStem, attestationAttrType, GrouperAttestationJob.ATTESTATION_STEM_SCOPE,
            "one or sub for if attestation settings inherit to just this folder or also to subfolders (folder only)", wasInCheckConfig);
        checkAttribute(attestationStem, attestationAttrType, GrouperAttestationJob.ATTESTATION_HAS_ATTESTATION,
            "If this folder has attestation directly assigned or if this group has attestation either directly or indirectly assigned", wasInCheckConfig);
        checkAttribute(attestationStem, attestationAttrType, GrouperAttestationJob.ATTESTATION_TYPE,
            "Type of attestation.  Either based on groups or a report.", wasInCheckConfig);
        checkAttribute(attestationStem, attestationAttrType, GrouperAttestationJob.ATTESTATION_REPORT_CONFIGURATION_ID,
            "The report configuration associated with this attestation if any", wasInCheckConfig);
        checkAttribute(attestationStem, attestationAttrType, GrouperAttestationJob.ATTESTATION_AUTHORIZED_GROUP_ID,
            "The authorized group associated with this attestation if any", wasInCheckConfig);
      }

      {
        
        String customUiRootStemName = CustomUiAttributeNames.customUiStemName();
        
        Stem customUiStem = StemFinder.findByName(grouperSession, customUiRootStemName, false);
        if (customUiStem == null) {
          customUiStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for Grouper custom UI attributes").assignName(customUiRootStemName)
            .save();
        }

        //see if attributeDef is there
        String customUiTypeDefName = customUiRootStemName + ":" + CustomUiAttributeNames.CUSTOM_UI_DEF;

        AttributeDef customUiType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            customUiTypeDefName, false, new QueryOptions().secondLevelCache(false));
        if (customUiType == null) {
          customUiType = customUiStem.addChildAttributeDef(CustomUiAttributeNames.CUSTOM_UI_DEF, AttributeDefType.type);
          customUiType.setAssignToGroup(true);
          customUiType.store();
        }
        
        //add a name
        AttributeDefName attribute = checkAttribute(customUiStem, customUiType, CustomUiAttributeNames.CUSTOM_UI_MARKER, "has custom UI attributes", wasInCheckConfig);
        
        //lets add some rule attributes
        String customUiAttrDefName = customUiRootStemName + ":" + CustomUiAttributeNames.CUSTOM_UI_VALUE_DEF;
        AttributeDef customUiAttrType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
            customUiAttrDefName, false, new QueryOptions().secondLevelCache(false));
        if (customUiAttrType == null) {
          customUiAttrType = customUiStem.addChildAttributeDef(CustomUiAttributeNames.CUSTOM_UI_VALUE_DEF, AttributeDefType.attr);
          customUiAttrType.setAssignToGroupAssn(true);
          customUiAttrType.setMultiValued(true);
          customUiAttrType.setValueType(AttributeDefValueType.string);
          customUiAttrType.store();
        }

        //the attributes can only be assigned to the type def
        // try an attribute def dependent on an attribute def name
        customUiAttrType.getAttributeDefScopeDelegate().assignOwnerNameEquals(attribute.getName());

        //add some names
        checkAttribute(customUiStem, customUiAttrType, CustomUiAttributeNames.CUSTOM_UI_TEXT_CONFIG_BEANS, 
            "JSONs of CustomUiTextConfigBeans.  Add a json with multiple values to configure text for this custom UI", wasInCheckConfig);
        checkAttribute(customUiStem, customUiAttrType, CustomUiAttributeNames.CUSTOM_UI_USER_QUERY_CONFIG_BEANS, 
            "JSONs of CustomUiUserQueryConfigBeans.  Add a json with multiple values to configure variables and queries for this custom UI", wasInCheckConfig);
      }

      // add attribute defs for grouper types
      {
        String grouperObjectTypesRootStemName = GrouperObjectTypesSettings.objectTypesStemName();
        
        Stem grouperTypesStemName = StemFinder.findByName(grouperSession, grouperObjectTypesRootStemName, false);
        if (grouperTypesStemName == null) {
          grouperTypesStemName = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in Grouper types objects").assignName(grouperObjectTypesRootStemName)
            .save();
        }

        //see if attributeDef is there
        String grouperObjectTypeDefName = grouperObjectTypesRootStemName + ":" + GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DEF;
        AttributeDef grouperObjectType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            grouperObjectTypeDefName, false, new QueryOptions().secondLevelCache(false));
        if (grouperObjectType == null) {
          grouperObjectType = grouperTypesStemName.addChildAttributeDef(GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DEF, AttributeDefType.type);
          //assign once for each affiliation
          grouperObjectType.setMultiAssignable(true);
          grouperObjectType.setAssignToGroup(true);
          grouperObjectType.setAssignToStem(true);
          grouperObjectType.store();
        }
        
        //add a name
        AttributeDefName attribute = checkAttribute(grouperTypesStemName, grouperObjectType, GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_ATTRIBUTE_NAME, "has grouper object type attributes", wasInCheckConfig);
        
        //lets add some rule attributes
        String grouperObjectTypeAttrDefName = grouperObjectTypesRootStemName + ":" + GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_VALUE_DEF;
        AttributeDef grouperObjectTypeAttrType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
            grouperObjectTypeAttrDefName, false, new QueryOptions().secondLevelCache(false));
        if (grouperObjectTypeAttrType == null) {
          grouperObjectTypeAttrType = grouperTypesStemName.addChildAttributeDef(GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_VALUE_DEF, AttributeDefType.attr);
          grouperObjectTypeAttrType.setAssignToGroupAssn(true);
          grouperObjectTypeAttrType.setAssignToStemAssn(true);
          grouperObjectTypeAttrType.setAssignToAttributeDefAssn(true);
          grouperObjectTypeAttrType.setValueType(AttributeDefValueType.string);
          grouperObjectTypeAttrType.store();
        }

        //the attributes can only be assigned to the type def
        // try an attribute def dependent on an attribute def name
        grouperObjectTypeAttrType.getAttributeDefScopeDelegate().assignOwnerNameEquals(attribute.getName());
        
        checkAttribute(grouperTypesStemName, grouperObjectTypeAttrType, GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME,
            "ref, basis, policy,etc, bundle, org, test, service, app, readOnly, grouperSecurity", wasInCheckConfig);
        
        checkAttribute(grouperTypesStemName, grouperObjectTypeAttrType, GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DATA_OWNER, 
            "e.g. Registrar's office owns this data", wasInCheckConfig);
        
        checkAttribute(grouperTypesStemName, grouperObjectTypeAttrType, GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_MEMBERS_DESCRIPTION, 
            "Human readable description of the members of this group", wasInCheckConfig);
        
        checkAttribute(grouperTypesStemName, grouperObjectTypeAttrType, GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, 
            "if configuration is directly assigned to the group or folder or inherited from parent", wasInCheckConfig);
        
        checkAttribute(grouperTypesStemName, grouperObjectTypeAttrType, GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_SERVICE_NAME, 
            "name of the service that this app falls under", wasInCheckConfig);
        
        checkAttribute(grouperTypesStemName, grouperObjectTypeAttrType, GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_OWNER_STEM_ID, 
            "Stem ID of the folder where the configuration is inherited from.  This is blank if this is a direct assignment and not inherited", wasInCheckConfig);

      }
      
      {
        // add workflow config attributes
        String workflowRootStemName = GrouperWorkflowSettings.workflowStemName();
        
        Stem workflowStem = StemFinder.findByName(grouperSession, workflowRootStemName, false);
        if (workflowStem == null) {
          workflowStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in Grouper workflow attributes").assignName(workflowRootStemName)
            .save();
        }
          //see if attributeDef is there

          String workflowTypeDefName = workflowRootStemName + ":" + GROUPER_WORKFLOW_CONFIG_DEF;
          AttributeDef workflowType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
              workflowTypeDefName, false, new QueryOptions().secondLevelCache(false));
          if (workflowType == null) {
            workflowType = workflowStem.addChildAttributeDef(GROUPER_WORKFLOW_CONFIG_DEF, AttributeDefType.type);
            workflowType.setMultiAssignable(true);
            workflowType.setAssignToGroup(true);
            workflowType.store();
          }
          
          Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(workflowType);
          

          //add a name
          AttributeDefName attribute = checkAttribute(workflowStem, workflowType, GROUPER_WORKFLOW_CONFIG_ATTRIBUTE_NAME, "has workflow approval attributes", wasInCheckConfig);
          
          //add attributes
          String workflowAttrDefName = workflowRootStemName + ":" + GROUPER_WORKFLOW_CONFIG_VALUE_DEF;
          AttributeDef workflowAttrType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
              workflowAttrDefName, false, new QueryOptions().secondLevelCache(false));
          if (workflowAttrType == null) {
            workflowAttrType = workflowStem.addChildAttributeDef(GROUPER_WORKFLOW_CONFIG_VALUE_DEF, AttributeDefType.attr);
            workflowAttrType.setAssignToGroupAssn(true);
            workflowAttrType.setValueType(AttributeDefValueType.string);
            workflowAttrType.store();
          }

          Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(workflowAttrType);

          //the attributes can only be assigned to the type def
          // try an attribute def dependent on an attribute def name
          workflowAttrType.getAttributeDefScopeDelegate().assignOwnerNameEquals(attribute.getName());

          //add some names
          checkAttribute(workflowStem, workflowAttrType, GROUPER_WORKFLOW_CONFIG_TYPE, 
              "workflow implementation type. default is grouper", wasInCheckConfig);
          checkAttribute(workflowStem, workflowAttrType, GROUPER_WORKFLOW_CONFIG_APPROVALS,
              "JSON config of the workflow approvals", wasInCheckConfig);
          checkAttribute(workflowStem, workflowAttrType, GROUPER_WORKFLOW_CONFIG_NAME,
              "Name of workflow.", wasInCheckConfig);
          checkAttribute(workflowStem, workflowAttrType, GROUPER_WORKFLOW_CONFIG_ID,
              "Camel-case alphanumeric id of workflow", wasInCheckConfig);
          checkAttribute(workflowStem, workflowAttrType, GROUPER_WORKFLOW_CONFIG_DESCRIPTION,
              "workflow config description", wasInCheckConfig);
          checkAttribute(workflowStem, workflowAttrType, GROUPER_WORKFLOW_CONFIG_PARAMS,
              "workflow config params", wasInCheckConfig);
          checkAttribute(workflowStem, workflowAttrType, GROUPER_WORKFLOW_CONFIG_FORM,
              "workflow form with html, javascript", wasInCheckConfig);
          checkAttribute(workflowStem, workflowAttrType, GROUPER_WORKFLOW_CONFIG_VIEWERS_GROUP_ID,
              "GroupId of people who can view this workflow and instances of this workflow.", wasInCheckConfig);
          checkAttribute(workflowStem, workflowAttrType, GROUPER_WORKFLOW_CONFIG_SEND_EMAIL,
              "true/false if email should be sent", wasInCheckConfig);
          checkAttribute(workflowStem, workflowAttrType, GROUPER_WORKFLOW_CONFIG_ENABLED,
              "Could by true, false, or noNewSubmissions", wasInCheckConfig);
          
          // add workflow instance attributes
          String grouperWorkflowInstanceDefName = workflowRootStemName + ":" + GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_DEF;
          AttributeDef grouperWorkflowInstance = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
              grouperWorkflowInstanceDefName, false, new QueryOptions().secondLevelCache(false));
          if (grouperWorkflowInstance == null) {
            grouperWorkflowInstance = workflowStem.addChildAttributeDef(GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_DEF, AttributeDefType.type);
            grouperWorkflowInstance.setMultiAssignable(true);
            grouperWorkflowInstance.setAssignToGroup(true);
            grouperWorkflowInstance.store();
          }
          
          //add a name
          AttributeDefName instanceAttribute = checkAttribute(workflowStem, grouperWorkflowInstance, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_ATTRIBUTE_NAME, "has grouper workflow instance attributes", wasInCheckConfig);
          
          //lets add some attributes names
          String grouperWorkflowInstanceAttrDefName = workflowRootStemName + ":" + GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_VALUE_DEF;
          AttributeDef grouperWorkflowInstanceAttrType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
              grouperWorkflowInstanceAttrDefName, false, new QueryOptions().secondLevelCache(false));
          if (grouperWorkflowInstanceAttrType == null) {
            grouperWorkflowInstanceAttrType = workflowStem.addChildAttributeDef(GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_VALUE_DEF, AttributeDefType.attr);
            grouperWorkflowInstanceAttrType.setAssignToGroupAssn(true);
            grouperWorkflowInstanceAttrType.setValueType(AttributeDefValueType.string);
            grouperWorkflowInstanceAttrType.store();
          }

          //the attributes can only be assigned to the type def
          // try an attribute def dependent on an attribute def name
          grouperWorkflowInstanceAttrType.getAttributeDefScopeDelegate().assignOwnerNameEquals(instanceAttribute.getName());
          
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_STATE,
              "Any of the states, plus exception", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_LAST_UPDATED_MILLIS_SINCE_1970,
              "number of millis since 1970 when this instance was last updated", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID,
              "Attribute assign ID of the marker attribute of the config", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_INITIATED_MILLIS_SINCE_1970,
              "millis since 1970 that this workflow was submitted", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_UUID,
              "uuid assigned to this workflow instance", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_FILE_INFO,
              "workflow instance file info", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_ENCRYPTION_KEY,
              "randomly generated 16 char alphanumeric encryption key", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_LAST_EMAILED_DATE,
              "yyyy/mm/dd date that this was last emailed", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_LAST_EMAILED_STATE,
              "the state of the workflow instance when it was last emailed", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_LOG,
              "has brief info about who did what when on this instance", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_ERROR,
              "error message including stack of why this instance is in exception state", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_0,
              "param value 0", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_1,
              "param value 1", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_2,
              "param value 2", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_3,
              "param value 3", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_4,
              "param value 4", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_5,
              "param value 5", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_6,
              "param value 6", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_7,
              "param value 7", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_8,
              "param value 8", wasInCheckConfig);
          checkAttribute(workflowStem, grouperWorkflowInstanceAttrType, GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_9,
              "param value 9", wasInCheckConfig);
            
        }
      

      {
        // add attribute defs for grouper report config and grouper report instance
        String reportConfigStemName = GrouperReportSettings.reportConfigStemName();
        
        Stem reportConfigStem = StemFinder.findByName(grouperSession, reportConfigStemName, false);
        if (reportConfigStem == null) {
          reportConfigStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for Grouper report config").assignName(reportConfigStemName)
            .save();
        }

        String grouperReportConfigDefName = reportConfigStemName + ":" + GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_DEF;
        AttributeDef grouperReportConfig = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            grouperReportConfigDefName, false, new QueryOptions().secondLevelCache(false));
        if (grouperReportConfig == null) {
          grouperReportConfig = reportConfigStem.addChildAttributeDef(GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_DEF, AttributeDefType.type);
          //assign once for each affiliation
          grouperReportConfig.setMultiAssignable(true);
          grouperReportConfig.setAssignToGroup(true);
          grouperReportConfig.setAssignToStem(true);
          grouperReportConfig.store();
        }
        
        //add a name
        AttributeDefName attribute = checkAttribute(reportConfigStem, grouperReportConfig, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_ATTRIBUTE_NAME, "has grouper report config attributes", wasInCheckConfig);
        
        //lets add some attributes names
        String grouperReportConfigAttrDefName = reportConfigStemName + ":" + GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_VALUE_DEF;
        AttributeDef grouperReportConfigAttrType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
            grouperReportConfigAttrDefName, false, new QueryOptions().secondLevelCache(false));
        if (grouperReportConfigAttrType == null) {
          grouperReportConfigAttrType = reportConfigStem.addChildAttributeDef(GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_VALUE_DEF, AttributeDefType.attr);
          grouperReportConfigAttrType.setAssignToGroupAssn(true);
          grouperReportConfigAttrType.setAssignToStemAssn(true);
          grouperReportConfigAttrType.setValueType(AttributeDefValueType.string);
          grouperReportConfigAttrType.store();
        }

        //the attributes can only be assigned to the type def
        // try an attribute def dependent on an attribute def name
        grouperReportConfigAttrType.getAttributeDefScopeDelegate().assignOwnerNameEquals(attribute.getName());
        
        checkAttribute(reportConfigStem, grouperReportConfigAttrType, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_TYPE,
            "report config type. Currently only SQL is available", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportConfigAttrType, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_FORMAT, 
            "report config format. Currently only CSV is available", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportConfigAttrType, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_NAME, 
            "Name of report. No two reports in the same owner should have the same name", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportConfigAttrType, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_FILE_NAME, 
            "file name in which report contents will be saved", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportConfigAttrType, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_DESCRIPTION, 
            "Textarea which describes the information in the report. Must be less than 4k", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportConfigAttrType, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_VIEWERS_GROUP_ID, 
            "GroupId of people who can view this report. Grouper admins can view any report", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportConfigAttrType, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_QUARTZ_CRON, 
            "Quartz cron-like schedule", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportConfigAttrType, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SEND_EMAIL, 
            "true/false if email should be sent", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportConfigAttrType, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_EMAIL_SUBJECT, 
            "subject for email (optional, will be generated from report name if blank)", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportConfigAttrType, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_EMAIL_BODY, 
            "email body", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportConfigAttrType, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SEND_EMAIL_TO_VIEWERS, 
            "true/false if report viewers should get email (if reportSendEmail is true)", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportConfigAttrType, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_SEND_EMAIL_TO_GROUP_ID, 
            "this is the groupId where members are retrieved from, and the subject email attribute, if not null then send", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportConfigAttrType, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_QUERY, 
            "SQL for the report. The columns must be named in the SQL (e.g. not select *) and generally this comes from a view", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportConfigAttrType, GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_ENABLED, 
            "logic from loader enabled, either enable or disabled this job", wasInCheckConfig);
        

        //see if attributeDef is there
        String grouperReportInstanceDefName = reportConfigStemName + ":" + GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_DEF;
        AttributeDef grouperReportInstance = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            grouperReportInstanceDefName, false, new QueryOptions().secondLevelCache(false));
        if (grouperReportInstance == null) {
          grouperReportInstance = reportConfigStem.addChildAttributeDef(GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_DEF, AttributeDefType.type);
          //assign once for each affiliation
          grouperReportInstance.setMultiAssignable(true);
          grouperReportInstance.setAssignToGroup(true);
          grouperReportInstance.setAssignToStem(true);
          grouperReportInstance.store();
        }
        
        //add a name
        AttributeDefName instanceAttribute = checkAttribute(reportConfigStem, grouperReportInstance, GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_ATTRIBUTE_NAME, "has grouper report instance attributes", wasInCheckConfig);
        
        //lets add some attributes names
        String grouperReportInstanceAttrDefName = reportConfigStemName + ":" + GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_VALUE_DEF;
        AttributeDef grouperReportInstanceAttrType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
            grouperReportInstanceAttrDefName, false, new QueryOptions().secondLevelCache(false));
        if (grouperReportInstanceAttrType == null) {
          grouperReportInstanceAttrType = reportConfigStem.addChildAttributeDef(GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_VALUE_DEF, AttributeDefType.attr);
          grouperReportInstanceAttrType.setAssignToGroupAssn(true);
          grouperReportInstanceAttrType.setAssignToStemAssn(true);
          grouperReportInstanceAttrType.setValueType(AttributeDefValueType.string);
          grouperReportInstanceAttrType.store();
        }

        //the attributes can only be assigned to the type def
        // try an attribute def dependent on an attribute def name
        grouperReportInstanceAttrType.getAttributeDefScopeDelegate().assignOwnerNameEquals(instanceAttribute.getName());
        
        checkAttribute(reportConfigStem, grouperReportInstanceAttrType, GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_STATUS,
            "SUCCESS means link to the report from screen, ERROR means didnt execute successfully", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportInstanceAttrType, GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_MILLIS_ELAPSED, 
            "number of millis it took to generate this report", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportInstanceAttrType, GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID, 
            "Attribute assign ID of the marker attribute of the config (same owner as this attribute, but there could be many reports configured on one owner)", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportInstanceAttrType, GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_MILLIS_SINCE_1970, 
            "millis since 1970 that this report was run. This must match the timestamp in the report name and storage", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportInstanceAttrType, GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_SIZE_BYTES, 
            "number of bytes of the unencrypted report", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportInstanceAttrType, GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_FILE_NAME, 
            "filename of report", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportInstanceAttrType, GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_FILE_POINTER, 
            "depending on storage type, this is a pointer to the report in storage, e.g. the S3 address. note the S3 address is .csv suffix, but change to __metadata.json for instance metadata", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportInstanceAttrType, GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_DOWNLOAD_COUNT, 
            "number of times this report was downloaded (note update this in try/catch and a for loop so concurrency doesnt cause problems)", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportInstanceAttrType, GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_ENCRYPTION_KEY, 
            "randomly generated 16 char alphanumeric encryption key (never allow display or edit of this)", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportInstanceAttrType, GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_ROWS, 
            "number of rows returned in report", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportInstanceAttrType, GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_EMAIL_TO_SUBJECTS, 
            "source::::subjectId1, source2::::subjectId2 list for subjects who were were emailed successfully (cant be more than 4k chars)", wasInCheckConfig);
        
        checkAttribute(reportConfigStem, grouperReportInstanceAttrType, GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_EMAIL_TO_SUBJECTS_ERROR, 
            "source::::subjectId1, source2::::subjectId2 list for subjects who were were NOT emailed successfully, dont include g:gsa groups (cant be more than 4k chars)", wasInCheckConfig);
      }
      
      {
        
        Stem loaderMetadataStem = StemFinder.findByName(grouperSession, loaderMetadataStemName(), false, new QueryOptions().secondLevelCache(false));
        if (loaderMetadataStem == null) {
          loaderMetadataStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in Grouper Loader Metadata attributes").assignName(loaderMetadataStemName())
            .save();
        }

        //see if attributeDef is there
        String loaderMetadataTypeDefName = loaderMetadataStemName() + ":loaderMetadataDef";
        AttributeDef loaderMetadataType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            loaderMetadataTypeDefName, false, new QueryOptions().secondLevelCache(false));
        if (loaderMetadataType == null) {
          loaderMetadataType = loaderMetadataStem.addChildAttributeDef("loaderMetadataDef", AttributeDefType.type);
          loaderMetadataType.setAssignToGroup(true);
          loaderMetadataType.store();
        }
        
        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(loaderMetadataType);

        //add a name
        AttributeDefName attribute = checkAttribute(loaderMetadataStem, loaderMetadataType, "loaderMetadata", "has metadata attributes", wasInCheckConfig);
        
        //lets add some rule attributes
        String loaderMetadataAttrDefName = loaderMetadataStemName() + ":loaderMetadataValueDef";
        AttributeDef loaderMetadataAttrType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
            loaderMetadataAttrDefName, false, new QueryOptions().secondLevelCache(false));
        if (loaderMetadataAttrType == null) {
          loaderMetadataAttrType = loaderMetadataStem.addChildAttributeDef("loaderMetadataValueDef", AttributeDefType.attr);
          loaderMetadataAttrType.setAssignToGroupAssn(true);
          loaderMetadataAttrType.setValueType(AttributeDefValueType.string);
          loaderMetadataAttrType.store();
        }

        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(loaderMetadataAttrType);

        //the attributes can only be assigned to the type def
        // try an attribute def dependent on an attribute def name
        loaderMetadataAttrType.getAttributeDefScopeDelegate().assignOwnerNameEquals(attribute.getName());

        //add some names
        checkAttribute(loaderMetadataStem, loaderMetadataAttrType, GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_LOADED, 
            "True means the group was loaded from loader", wasInCheckConfig);
        checkAttribute(loaderMetadataStem, loaderMetadataAttrType, GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_GROUP_ID,
            "Group id which is being populated from the loader", wasInCheckConfig);
        checkAttribute(loaderMetadataStem, loaderMetadataAttrType, GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_LAST_FULL_MILLIS,
            "Millis since 1970 that this group was fully processed", wasInCheckConfig);
        checkAttribute(loaderMetadataStem, loaderMetadataAttrType, GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_LAST_INCREMENTAL_MILLIS,
            "Millis since 1970 that this group was incrementally processed", wasInCheckConfig);
        checkAttribute(loaderMetadataStem, loaderMetadataAttrType, GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_LAST_SUMMARY,
            "Summary of loader job", wasInCheckConfig);
      }
      
      {
        String rulesRootStemName = RuleUtils.attributeRuleStemName();
        
        Stem rulesStem = StemFinder.findByName(grouperSession, rulesRootStemName, false, new QueryOptions().secondLevelCache(false));
        if (rulesStem == null) {
          rulesStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in Grouper rules attributes").assignName(rulesRootStemName)
            .save();
        }

        //see if attributeDef is there
        String ruleTypeDefName = rulesRootStemName + ":rulesTypeDef";
        AttributeDef ruleType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            ruleTypeDefName, false, new QueryOptions().secondLevelCache(false));
        if (ruleType == null) {
          ruleType = rulesStem.addChildAttributeDef("rulesTypeDef", AttributeDefType.type);
          ruleType.setAssignToGroup(true);
          ruleType.setAssignToStem(true);
          ruleType.setAssignToAttributeDef(true);
          ruleType.setMultiAssignable(true);
          ruleType.store();
        }
        
        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(ruleType);

        //add a name
        AttributeDefName rule = checkAttribute(rulesStem, ruleType, "rule", "is a rule", wasInCheckConfig);
        
        //lets add some rule attributes
        String ruleAttrDefName = rulesRootStemName + ":rulesAttrDef";
        AttributeDef ruleAttrType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
            ruleAttrDefName, false, new QueryOptions().secondLevelCache(false));
        if (ruleAttrType == null) {
          ruleAttrType = rulesStem.addChildAttributeDef("rulesAttrDef", AttributeDefType.attr);
          ruleAttrType.setAssignToGroupAssn(true);
          ruleAttrType.setAssignToAttributeDefAssn(true);
          ruleAttrType.setAssignToStemAssn(true);
          ruleAttrType.setValueType(AttributeDefValueType.string);
          ruleAttrType.store();
        }

        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(ruleAttrType);

        //if not configured properly, configure it properly
        if (!ruleAttrType.isAssignToAttributeDefAssn()) {
          ruleAttrType.setAssignToAttributeDefAssn(true);
          ruleAttrType.store();
        }
        
        //the attributes can only be assigned to the type def
        // try an attribute def dependent on an attribute def name
        ruleAttrType.getAttributeDefScopeDelegate().assignOwnerNameEquals(rule.getName());

        //add some names
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_ACT_AS_SUBJECT_ID, 
            "subject id to act as, mutually exclusive with identifier", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_ACT_AS_SUBJECT_IDENTIFIER, 
            "subject identifier to act as, mutually exclusive with id", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_ACT_AS_SUBJECT_SOURCE_ID, 
            "subject source id to act as", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_CHECK_TYPE, 
            "when the check should be to see if rule should fire, enum: RuleCheckType", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_CHECK_OWNER_ID, 
            "when the check should be to see if rule should fire, this is owner of type, mutually exclusive with name", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_CHECK_OWNER_NAME, 
            "when the check should be to see if rule should fire, this is owner of type, mutually exclusice with id", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_CHECK_STEM_SCOPE, 
            "when the check is a stem type, this is Stem.Scope ALL or SUB", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_CHECK_ARG0, 
            "when the check needs an arg, this is the arg0", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_CHECK_ARG1, 
            "when the check needs an arg, this is the arg1", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_IF_OWNER_ID, 
            "when the if part has an arg, this is owner of if, mutually exclusive with name", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_IF_OWNER_NAME, 
            "when the if part has an arg, this is owner of if, mutually exclusive with id", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_IF_CONDITION_EL, 
            "expression language to run to see if the rule should run, or blank if should run always", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_IF_CONDITION_ENUM, 
            "RuleIfConditionEnum that sees if rule should fire, or exclude if should run always", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_IF_CONDITION_ENUM_ARG0, 
            "RuleIfConditionEnumArg0 if the if condition takes an argument, this is the first one", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_IF_CONDITION_ENUM_ARG1, 
            "RuleIfConditionEnumArg1 if the if condition takes an argument, this is the second param", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_IF_STEM_SCOPE, 
            "when the if part is a stem, this is the scope of SUB or ONE", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_THEN_EL, 
            "expression language to run when the rule fires", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_THEN_ENUM, 
            "RuleThenEnum to run when the rule fires", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_THEN_ENUM_ARG0, 
            "RuleThenEnum argument 0 to run when the rule fires (enum might need args)", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_THEN_ENUM_ARG1, 
            "RuleThenEnum argument 1 to run when the rule fires (enum might need args)", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_THEN_ENUM_ARG2, 
            "RuleThenEnum argument 2 to run when the rule fires (enum might need args)", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_VALID, 
            "T|F for if this rule is valid, or the reason, managed by hook automatically", wasInCheckConfig);
        checkAttribute(rulesStem, ruleAttrType, RuleUtils.RULE_RUN_DAEMON, 
            "T|F for if this rule daemon should run.  Default to true if blank and check and if are enums, false if not", wasInCheckConfig);
        
      }      

      boolean permissionsLimitsPublic = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.permissions.limits.builtin.createAs.public", true);
      
      {
        String limitsRootStemName = PermissionLimitUtils.attributeLimitStemName();
        
        Stem limitsStem = StemFinder.findByName(grouperSession, limitsRootStemName, false, new QueryOptions().secondLevelCache(false));
        if (limitsStem == null) {
          limitsStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in Grouper permission limits").assignName(limitsRootStemName)
            .save();
        }

        //see if attributeDef is there
        String limitDefName = limitsRootStemName + ":" + PermissionLimitUtils.LIMIT_DEF;
        AttributeDef limitDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            limitDefName, false, new QueryOptions().secondLevelCache(false));
        if (limitDef == null) {
          limitDef = limitsStem.addChildAttributeDef(PermissionLimitUtils.LIMIT_DEF, AttributeDefType.limit);
          limitDef.setAssignToGroup(true);
          limitDef.setAssignToAttributeDef(true);
          limitDef.setAssignToGroupAssn(true);
          limitDef.setAssignToEffMembership(true);
          limitDef.setAssignToEffMembershipAssn(true);
          limitDef.setValueType(AttributeDefValueType.string);
          limitDef.setMultiAssignable(true);
          limitDef.store();
          
          if (permissionsLimitsPublic) {
            limitDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
            limitDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
          }
          
        }
        
        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(limitDef);

        //add an el
        {
          String elDisplayExtension = StringUtils.defaultIfEmpty(GrouperConfig.retrieveConfig().propertyValueString("grouper.permissions.limits.builtin.displayExtension.limitExpression"), "Expression");
          checkAttribute(limitsStem, limitDef, PermissionLimitUtils.LIMIT_EL, elDisplayExtension, 
              "An expression language limit has a value of an EL which evaluates to true or false", wasInCheckConfig);
        }
        {
          String ipOnNetworksDisplayExtension = StringUtils.defaultIfEmpty(GrouperConfig.retrieveConfig().propertyValueString("grouper.permissions.limits.builtin.displayExtension.limitIpOnNetworks"), "ipAddress on networks");
          checkAttribute(limitsStem, limitDef, PermissionLimitUtils.LIMIT_IP_ON_NETWORKS, ipOnNetworksDisplayExtension,
              "If the user is on an IP address on the following networks", wasInCheckConfig);
        }
        {
          String ipOnNetworkRealmDisplayEntension = StringUtils.defaultIfEmpty(GrouperConfig.retrieveConfig().propertyValueString("grouper.permissions.limits.builtin.displayExtension.limitIpOnNetworkRealm"), "ipAddress on network realm");
          checkAttribute(limitsStem, limitDef, PermissionLimitUtils.LIMIT_IP_ON_NETWORK_REALM, ipOnNetworkRealmDisplayEntension,
              "If the user is on an IP address on a centrally configured list of addresses", wasInCheckConfig);
        }
        {
          String labelsContainDisplayExtension = StringUtils.defaultIfEmpty(GrouperConfig.retrieveConfig().propertyValueString("grouper.permissions.limits.builtin.displayExtension.limitLabelsContain"), "labels contains");
          checkAttribute(limitsStem, limitDef, PermissionLimitUtils.LIMIT_LABELS_CONTAIN, labelsContainDisplayExtension,
              "Configure a set of comma separated labels.  The env variable 'labels' should be passed with comma separated " +
              "labels.  If one is there, its ok, if not, then disallowed", wasInCheckConfig);
        }
      }
      
      {
        String limitsRootStemName = PermissionLimitUtils.attributeLimitStemName();
        Stem limitsStem = StemFinder.findByName(grouperSession, limitsRootStemName, true, new QueryOptions().secondLevelCache(false));

        //see if attributeDef is there
        String limitDefIntName = limitsRootStemName + ":" + PermissionLimitUtils.LIMIT_DEF_INT;
        AttributeDef limitDefInt = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            limitDefIntName, false, new QueryOptions().secondLevelCache(false));
        if (limitDefInt == null) {
          limitDefInt = limitsStem.addChildAttributeDef(PermissionLimitUtils.LIMIT_DEF_INT, AttributeDefType.limit);
          limitDefInt.setAssignToGroup(true);
          limitDefInt.setAssignToAttributeDef(true);
          limitDefInt.setAssignToGroupAssn(true);
          limitDefInt.setAssignToEffMembership(true);
          limitDefInt.setAssignToEffMembershipAssn(true);
          limitDefInt.setMultiAssignable(true);
          limitDefInt.setValueType(AttributeDefValueType.integer);
          limitDefInt.store();

          if (permissionsLimitsPublic) {
            limitDefInt.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
            limitDefInt.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
          }
        }
        
        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(limitDefInt);

        {
          String limitAmountLessThanDisplayExtension = StringUtils.defaultIfEmpty(GrouperConfig.retrieveConfig().propertyValueString("grouper.permissions.limits.builtin.displayExtension.limitAmountLessThan"), "amount less than");
          checkAttribute(limitsStem, limitDefInt, PermissionLimitUtils.LIMIT_AMOUNT_LESS_THAN, limitAmountLessThanDisplayExtension, 
              "Make sure the amount is less than the configured value", wasInCheckConfig);
        }
        {
          String limitAmountLessThanOrEqualToDisplayExtension = StringUtils.defaultIfEmpty(GrouperConfig.retrieveConfig().propertyValueString("grouper.permissions.limits.builtin.displayExtension.limitAmountLessThanOrEqual"), "amount less than or equal to");
          checkAttribute(limitsStem, limitDefInt, PermissionLimitUtils.LIMIT_AMOUNT_LESS_THAN_OR_EQUAL, limitAmountLessThanOrEqualToDisplayExtension,
              "Make sure the amount is less or equal to the configured value", wasInCheckConfig);
        }
        
      }

      {
        String limitsRootStemName = PermissionLimitUtils.attributeLimitStemName();
        Stem limitsStem = StemFinder.findByName(grouperSession, limitsRootStemName, true, new QueryOptions().secondLevelCache(false));

        //see if attributeDef is there
        String limitDefMarkerName = limitsRootStemName + ":" + PermissionLimitUtils.LIMIT_DEF_MARKER;
        AttributeDef limitDefMarker = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            limitDefMarkerName, false, new QueryOptions().secondLevelCache(false));
        if (limitDefMarker == null) {
          limitDefMarker = limitsStem.addChildAttributeDef(PermissionLimitUtils.LIMIT_DEF_MARKER, AttributeDefType.limit);
          limitDefMarker.setAssignToGroup(true);
          limitDefMarker.setAssignToAttributeDef(true);
          limitDefMarker.setAssignToGroupAssn(true);
          limitDefMarker.setAssignToEffMembershipAssn(true);
          limitDefMarker.setAssignToEffMembership(true);
          limitDefMarker.setMultiAssignable(true);
          limitDefMarker.setValueType(AttributeDefValueType.marker);
          limitDefMarker.store();

          if (permissionsLimitsPublic) {
            limitDefMarker.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
            limitDefMarker.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
          }
        }

        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(limitDefMarker);
        
        {
          String limitAmountLessThanDisplayExtension = StringUtils.defaultIfEmpty(GrouperConfig.retrieveConfig().propertyValueString("grouper.permissions.limits.builtin.displayExtension.limitWeekday9to5"), "Weekday 9 to 5");
          //add an weekday 9 to 5
          checkAttribute(limitsStem, limitDefMarker, PermissionLimitUtils.LIMIT_WEEKDAY_9_TO_5, limitAmountLessThanDisplayExtension,
              "Make sure the check for the permission happens between 9am to 5pm on Monday through Friday", wasInCheckConfig);
        }
      }


      AttributeDefName attributeLoaderTypeName = null;
      
      {
        String loaderRootStemName = attributeLoaderStemName();
        
        Stem loaderStem = StemFinder.findByName(grouperSession, loaderRootStemName, false, new QueryOptions().secondLevelCache(false));
        if (loaderStem == null) {
          loaderStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in Grouper loader attributes").assignName(loaderRootStemName)
            .save();
        }

        //see if attributeDef is there
        String attributeDefLoaderTypeDefName = loaderRootStemName + ":attributeDefLoaderTypeDef";
        AttributeDef attributeDefType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            attributeDefLoaderTypeDefName, false, new QueryOptions().secondLevelCache(false));
        if (attributeDefType == null) {
          attributeDefType = loaderStem.addChildAttributeDef("attributeDefLoaderTypeDef", AttributeDefType.type);
          attributeDefType.setAssignToAttributeDef(true);
          attributeDefType.store();
        }
        
        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(attributeDefType);
        
        //add a name
        attributeLoaderTypeName = checkAttribute(loaderStem, attributeDefType, "attributeLoader", 
            "is a loader based attribute def, the loader attributes will be available to be assigned", wasInCheckConfig);
        
        //see if attributeDef is there
        String attributeDefLoaderDefName = loaderRootStemName + ":attributeDefLoaderDef";
        AttributeDef attributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
          attributeDefLoaderDefName, false, new QueryOptions().secondLevelCache(false));
        if (attributeDef == null) {
          attributeDef = loaderStem.addChildAttributeDef("attributeDefLoaderDef", AttributeDefType.attr);
          attributeDef.setAssignToAttributeDef(true);
          attributeDef.setValueType(AttributeDefValueType.string);
          attributeDef.store();
        }
        
        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(attributeDef);
        
        //make sure the other def means this one is allowed
        attributeDef.getAttributeDefScopeDelegate().assignTypeDependence(attributeLoaderTypeName);
        
        //add some names
        checkAttribute(loaderStem, attributeDef, "attributeLoaderType", "Type of loader, e.g. ATTR_SQL_SIMPLE", wasInCheckConfig);
        checkAttribute(loaderStem, attributeDef, "attributeLoaderDbName", 
          "DB name in grouper-loader.properties or default grouper db if blank", wasInCheckConfig);
        checkAttribute(loaderStem, attributeDef, "attributeLoaderScheduleType", 
          "Type of schedule.  Defaults to CRON if a cron schedule is entered, or START_TO_START_INTERVAL if an interval is entered", wasInCheckConfig);
        checkAttribute(loaderStem, attributeDef, "attributeLoaderQuartzCron", 
          "If a CRON schedule type, this is the cron setting string from the quartz product to run a job daily, hourly, weekly, etc.  e.g. daily at 7am: 0 0 7 * * ?", wasInCheckConfig);
        checkAttribute(loaderStem, attributeDef, "attributeLoaderIntervalSeconds", 
          "If a START_TO_START_INTERVAL schedule type, this is the number of seconds between runs", wasInCheckConfig);
        checkAttribute(loaderStem, attributeDef, "attributeLoaderPriority", 
          "Quartz has a fixed threadpool (max configured in the grouper-loader.properties), and when the max is reached, then jobs are prioritized by this integer.  The higher the better, and the default if not set is 5.", wasInCheckConfig);
        checkAttribute(loaderStem, attributeDef, "attributeLoaderAttrsLike", 
          "If empty, then orphans will be left alone (for attributeDefName and attributeDefNameSets).  If %, then all orphans deleted.  If a SQL like string, then only ones in that like string not in loader will be deleted", wasInCheckConfig);
        checkAttribute(loaderStem, attributeDef, "attributeLoaderAttrQuery", 
          "SQL query with at least some of the following columns: attr_name, attr_display_name, attr_description", wasInCheckConfig);
        checkAttribute(loaderStem, attributeDef, "attributeLoaderAttrSetQuery", 
          "SQL query with at least the following columns: if_has_attr_name, then_has_attr_name", wasInCheckConfig);
        checkAttribute(loaderStem, attributeDef, "attributeLoaderActionQuery", 
            "SQL query with at least the following column: action_name", wasInCheckConfig);
        checkAttribute(loaderStem, attributeDef, "attributeLoaderActionSetQuery", 
            "SQL query with at least the following columns: if_has_action_name, then_has_action_name", wasInCheckConfig);
                
      }

      {
        String loaderLdapRootStemName = LoaderLdapUtils.attributeLoaderLdapStemName();
        
        Stem loaderLdapStem = StemFinder.findByName(grouperSession, loaderLdapRootStemName, false, new QueryOptions().secondLevelCache(false));
        if (loaderLdapStem == null) {
          loaderLdapStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in Grouper loader ldap attributes").assignName(loaderLdapRootStemName)
            .save();
        }

        {
          //see if attributeDef is there
          String loaderLdapDefName = loaderLdapRootStemName + ":" + LoaderLdapUtils.LOADER_LDAP_DEF;
          AttributeDef loaderLdapDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
              loaderLdapDefName, false, new QueryOptions().secondLevelCache(false));
          if (loaderLdapDef == null) {
            loaderLdapDef = loaderLdapStem.addChildAttributeDef(LoaderLdapUtils.LOADER_LDAP_DEF, AttributeDefType.attr);
            loaderLdapDef.setAssignToGroup(true);
            loaderLdapDef.setValueType(AttributeDefValueType.marker);
            loaderLdapDef.store();
          }
          
          Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(loaderLdapDef);
          
          //add an attribute for the loader ldap marker
          {
            checkAttribute(loaderLdapStem, loaderLdapDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_MARKER, "Grouper loader LDAP", 
                "Marks a group to be processed by the Grouper loader as an LDAP synced job", wasInCheckConfig);
          }
        }
        {
          //see if attributeDef is there
          String loaderLdapValueDefName = loaderLdapRootStemName + ":" + LoaderLdapUtils.LOADER_LDAP_VALUE_DEF;
          AttributeDef loaderLdapValueDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
              loaderLdapValueDefName, false, new QueryOptions().secondLevelCache(false));
          if (loaderLdapValueDef == null) {
            loaderLdapValueDef = loaderLdapStem.addChildAttributeDef(LoaderLdapUtils.LOADER_LDAP_VALUE_DEF, AttributeDefType.attr);
            loaderLdapValueDef.setAssignToGroupAssn(true);
            loaderLdapValueDef.setValueType(AttributeDefValueType.string);
            loaderLdapValueDef.store();
          }
          
          Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(loaderLdapValueDef);
          
          //add an attribute for the loader ldap marker
          {
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_TYPE, "Grouper loader LDAP type", 
                "This holds the type of job from the GrouperLoaderType enum, currently the only valid values are " +
                "LDAP_SIMPLE, LDAP_GROUP_LIST, LDAP_GROUPS_FROM_ATTRIBUTES. Simple is a group loaded from LDAP " +
                "filter which returns subject ids or identifiers.  Group list is an LDAP filter which returns " +
                "group objects, and the group objects have a list of subjects.  Groups from attributes is an LDAP " +
                "filter that returns subjects which have a multi-valued attribute e.g. affiliations where groups " +
                "will be created based on subject who have each attribute value  ", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_SERVER_ID, "Grouper loader LDAP server ID", 
                "Server ID that is configured in the grouper-loader.properties that identifies the connection information to the LDAP server", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_FILTER, "Grouper loader LDAP filter", 
                "LDAP filter returns objects that have subjectIds or subjectIdentifiers and group name (if LDAP_GROUP_LIST)", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_QUARTZ_CRON, 
                "Grouper loader LDAP quartz cron", 
                "Quartz cron config string, e.g. every day at 8am is: 0 0 8 * * ?", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_SEARCH_DN, "Grouper loader LDAP search base DN", 
                "Location that constrains the subtree where the filter is applicable", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_SUBJECT_ATTRIBUTE, 
                "Grouper loader LDAP subject attribute name", 
                "Attribute name of the filter object result that holds the subject id.  Note, if you use 'dn', and " +
                "dn is not an attribute of the object, then the fully qualified object name will be used", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_SOURCE_ID, 
                "Grouper loader LDAP source ID", 
                "Source ID from the subject.properties that narrows the search for subjects.  This is optional though makes the loader job more efficient", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_SUBJECT_ID_TYPE, 
                "Grouper loader LDAP subject ID type", 
                "The type of subject ID.  This can be either: subjectId (most efficient), subjectIdentifier (2nd most efficient), or subjectIdOrIdentifier", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_AND_GROUPS, 
                "Grouper loader LDAP require in groups", 
                "If you want to restrict membership in the dynamic group based on other group(s), put the list of group names " +
                "here comma-separated.  The require groups means if you put a group names in there (e.g. school:community:employee) " +
                "then it will 'and' that group with the member list from the loader.  So only members of the group from the loader " +
                "query who are also employees will be in the resulting group", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_SEARCH_SCOPE, 
                "Grouper loader LDAP search scope", 
                "How the deep in the subtree the search will take place.  Can be OBJECT_SCOPE, ONELEVEL_SCOPE, or SUBTREE_SCOPE (default)", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_PRIORITY, 
                "Grouper loader LDAP scheduling priority", 
                "Quartz has a fixed threadpool (max configured in the grouper-loader.properties), and when the max is reached, " +
                "then jobs are prioritized by this integer.  The higher the better, and the default if not set is 5.", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_GROUPS_LIKE, 
                "Grouper loader LDAP groups like", 
                "This should be a sql like string (e.g. school:orgs:%org%_systemOfRecord), and the loader should be able to query group names to " +
                "see which names are managed by this loader job.  So if a group falls off the loader resultset (or is moved), this will help the " +
                "loader remove the members from this group.  Note, if the group is used anywhere as a member or composite member, it wont be removed.  " +
                "All include/exclude/requireGroups will be removed.  Though the two groups, include and exclude, will not be removed if they have members.  " +
                "There is a grouper-loader.properties setting to remove loader groups if empty and not used: " +
                "#if using a sql table, and specifying the name like string, then shoudl the group (in addition to memberships)" +
                "# be removed if not used anywhere else?" +
                "loader.sqlTable.likeString.removeGroupIfNotUsed = true", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_RESULTS_TRANSFORMATION_CLASS, 
                "Grouper loader LDAP results transformation class (optional for loader ldap type: LDAP_GROUPS_FROM_ATTRIBUTE)", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_GROUP_ATTRIBUTE, 
                "Grouper loader LDAP group attribute name", 
                "Attribute name of the filter object result that holds the group name (required for " +
                "loader ldap type: LDAP_GROUPS_FROM_ATTRIBUTE)", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_ATTRIBUTE_FILTER_EXPRESSION, 
                "Grouper loader LDAP attribute filter expression", 
                "JEXL expression that returns true or false to signify if an attribute (in GROUPS_FROM_ATTRIBUTES) is ok to use for a group.  " +
                "attributeValue is the variable that is the value of the attribute.", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_EXTRA_ATTRIBUTES, 
                "Grouper loader LDAP extra attributes", 
                "Attribute names (comma separated) to get LDAP data for expressions in group name, displayExtension, description, " +
                "optional, for LDAP_GROUP_LIST", wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_GROUP_NAME_EXPRESSION, 
                "Grouper loader LDAP group name expression", 
                "JEXL expression language fragment that evaluates to the group name (relative in the stem as the " +
                "group which has the loader definition), optional, for LDAP_GROUP_LIST, or LDAP_GROUPS_FROM_ATTRIBUTES", 
                wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_GROUP_DISPLAY_NAME_EXPRESSION, 
                "Grouper loader LDAP group display name expression", 
                "JEXL expression language fragment that evaluates to the group display name, optional for " +
                "LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES", 
                wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_GROUP_DESCRIPTION_EXPRESSION, 
                "Grouper loader LDAP group description expression", 
                "JEXL expression language fragment that evaluates to the group description, " +
                "optional for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES", 
                wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_SUBJECT_EXPRESSION, 
                "Grouper loader LDAP subject expression", 
                "JEXL expression language fragment that processes the subject string before passing it to the subject API (optional)", 
                wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_GROUP_TYPES, 
                "Grouper loader LDAP group types", 
                "Comma separated GroupTypes which will be applied to the loaded groups.  The reason this enhancement " +
                "exists is so we can do a group list filter and attach addIncludeExclude to the groups.  Note, if you " +
                "do this (or use some requireGroups), the group name in the loader query should end in the system of " +
                "record suffix, which by default is _systemOfRecord. optional for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES", 
                wasInCheckConfig);

            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_READERS, 
                "Grouper loader LDAP group readers", 
                "Comma separated subjectIds or subjectIdentifiers who will be allowed to READ the group membership.  " +
                "optional for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES", 
                wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_VIEWERS, 
                "Grouper loader LDAP group viewers", 
                "Comma separated subjectIds or subjectIdentifiers who will be allowed to VIEW the group.  " +
                "optional for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES", 
                wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_ADMINS, 
                "Grouper loader LDAP group admins", 
                "Comma separated subjectIds or subjectIdentifiers who will be allowed to ADMIN the group.  " +
                "optional for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES", 
                wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_UPDATERS, 
                "Grouper loader LDAP group updaters", 
                "Comma separated subjectIds or subjectIdentifiers who will be allowed to UPDATE the group memberships.  " +
                "optional for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES", 
                wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_OPTINS, 
                "Grouper loader LDAP group optins", 
                "Comma separated subjectIds or subjectIdentifiers who will be allowed to OPT IN to the group membership list.  " +
                "optional for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES", 
                wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_OPTOUTS, 
                "Grouper loader LDAP group optouts", 
                "Comma separated subjectIds or subjectIdentifiers who will be allowed to OPT OUT of the group membership list.  " +
                "optional for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES", 
                wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_GROUP_ATTR_READERS, 
                "Grouper loader LDAP group attribute readers", 
                "Comma separated subjectIds or subjectIdentifiers who will be allowed to GROUP_ATTR_READ on the group.  " +
                "optional for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES", 
                wasInCheckConfig);
            checkAttribute(loaderLdapStem, loaderLdapValueDef, LoaderLdapUtils.ATTR_DEF_EXTENSION_LDAP_GROUP_ATTR_UPDATERS, 
                "Grouper loader LDAP group attribute updaters", 
                "Comma separated subjectIds or subjectIdentifiers who will be allowed to GROUP_ATTR_UPDATE on the group.  " +
                "optional for LDAP_GROUP_LIST or LDAP_GROUPS_FROM_ATTRIBUTES", 
                wasInCheckConfig);
          }
        }
      }
      
      {
        String upgradeTasksRootStemName = UpgradeTasksJob.grouperUpgradeTasksStemName();
        
        Stem upgradeTasksRootStem = StemFinder.findByName(grouperSession, upgradeTasksRootStemName, false, new QueryOptions().secondLevelCache(false));
        if (upgradeTasksRootStem == null) {
          upgradeTasksRootStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for upgrade tasks objects").assignName(upgradeTasksRootStemName)
            .save();
        }

        // check attribute def
        String upgradeTasksDefName = upgradeTasksRootStemName + ":" + UpgradeTasksJob.UPGRADE_TASKS_DEF;
        AttributeDef upgradeTasksDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
            upgradeTasksDefName, false, new QueryOptions().secondLevelCache(false));
        if (upgradeTasksDef == null) {
          upgradeTasksDef = upgradeTasksRootStem.addChildAttributeDef(UpgradeTasksJob.UPGRADE_TASKS_DEF, AttributeDefType.attr);
          upgradeTasksDef.setAssignToGroup(true);
          upgradeTasksDef.setValueType(AttributeDefValueType.string);
          upgradeTasksDef.store();
        }
        
        String upgradeTasksVersionName = upgradeTasksRootStemName + ":" + UpgradeTasksJob.UPGRADE_TASKS_VERSION_ATTR;
        
        AttributeDefName upgradeTasksVersion = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
            upgradeTasksVersionName, false, new QueryOptions().secondLevelCache(false));
        
        if (upgradeTasksVersion == null) {
          upgradeTasksVersion = upgradeTasksRootStem.addChildAttributeDefName(upgradeTasksDef, UpgradeTasksJob.UPGRADE_TASKS_VERSION_ATTR, UpgradeTasksJob.UPGRADE_TASKS_VERSION_ATTR);
        }
        
        String groupName = upgradeTasksRootStemName + ":" + UpgradeTasksJob.UPGRADE_TASKS_METADATA_GROUP;
        Group group = GrouperDAOFactory.getFactory().getGroup().findByNameSecure(
            groupName, false, new QueryOptions().secondLevelCache(false), GrouperUtil.toSet(TypeOfGroup.group));
        if (group == null) {
          group = upgradeTasksRootStem.addChildGroup(UpgradeTasksJob.UPGRADE_TASKS_METADATA_GROUP, UpgradeTasksJob.UPGRADE_TASKS_METADATA_GROUP);
        }
        
        if (group.getAttributeValueDelegate().retrieveValueString(upgradeTasksVersionName) == null) {
          group.getAttributeValueDelegate().assignValue(upgradeTasksVersionName, "0");
        }
      }
            
      {
        String instrumentationDataRootStemName = InstrumentationDataUtils.grouperInstrumentationDataStemName();
        
        Stem instrumentationDataRootStem = StemFinder.findByName(grouperSession, instrumentationDataRootStemName, false, new QueryOptions().secondLevelCache(false));
        if (instrumentationDataRootStem == null) {
          instrumentationDataRootStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in Grouper instrumentation data attributes").assignName(instrumentationDataRootStemName)
            .save();
        }
        
        {
          // check instances folder
                    
          String instancesStemName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCES_FOLDER;
          Stem instancesStem = StemFinder.findByName(grouperSession, instancesStemName, false, new QueryOptions().secondLevelCache(false));
          if (instancesStem == null) {
            new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
              .assignDescription("folder for Grouper instances").assignName(instancesStemName)
              .save();
          }
        }
        
        {
          // check collectors folder
                    
          String collectorsStemName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTORS_FOLDER;
          Stem collectorsStem = StemFinder.findByName(grouperSession, collectorsStemName, false, new QueryOptions().secondLevelCache(false));
          if (collectorsStem == null) {
            new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
              .assignDescription("folder for Grouper collectors").assignName(collectorsStemName)
              .save();
          }
        }

        {
          // check instances def
          String instancesDefName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCES_DEF;
          AttributeDef instancesDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
              instancesDefName, false, new QueryOptions().secondLevelCache(false));
          if (instancesDef == null) {
            instancesDef = instrumentationDataRootStem.addChildAttributeDef(InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCES_DEF, AttributeDefType.attr);
            instancesDef.setAssignToGroup(true);
            instancesDef.setValueType(AttributeDefValueType.marker);
            instancesDef.store();
          }
          
          Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(instancesDef);
          
        }
        
        {
          // check collectors def
          String collectorsDefName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTORS_DEF;
          AttributeDef collectorsDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
              collectorsDefName, false, new QueryOptions().secondLevelCache(false));
          if (collectorsDef == null) {
            collectorsDef = instrumentationDataRootStem.addChildAttributeDef(InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTORS_DEF, AttributeDefType.attr);
            collectorsDef.setAssignToGroup(true);
            collectorsDef.setValueType(AttributeDefValueType.marker);
            collectorsDef.store();
          }

          Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(collectorsDef);
          
        }
        
        {
          // check counts def and attr
          String countsDefName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_COUNTS_DEF;
          AttributeDef countsDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
              countsDefName, false, new QueryOptions().secondLevelCache(false));
          if (countsDef == null) {
            countsDef = instrumentationDataRootStem.addChildAttributeDef(InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_COUNTS_DEF, AttributeDefType.attr);
            countsDef.setAssignToGroupAssn(true);
            countsDef.setValueType(AttributeDefValueType.string);
            countsDef.setMultiValued(true);
            countsDef.store();
          }
          
          Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(countsDef);
          
          String countsDefNameName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_COUNTS_ATTR;
          
          AttributeDefName countsAttrDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
              countsDefNameName, false, new QueryOptions().secondLevelCache(false));
          
          if (countsAttrDefName == null) {
            countsAttrDefName = instrumentationDataRootStem.addChildAttributeDefName(countsDef, InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_COUNTS_ATTR, InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_COUNTS_ATTR);
          }
        }
        
        {
          // check instance details def and attrs
          String detailsDefName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_DETAILS_DEF;
          AttributeDef detailsDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
              detailsDefName, false, new QueryOptions().secondLevelCache(false));
          if (detailsDef == null) {
            detailsDef = instrumentationDataRootStem.addChildAttributeDef(InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_DETAILS_DEF, AttributeDefType.attr);
            detailsDef.setAssignToGroupAssn(true);
            detailsDef.setValueType(AttributeDefValueType.string);
            detailsDef.store();
          }
          
          Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(detailsDef);
          
          {
            String lastUpdateName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_LAST_UPDATE_ATTR;
            
            AttributeDefName lastUpdate = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
                lastUpdateName, false, new QueryOptions().secondLevelCache(false));
            
            if (lastUpdate == null) {
              lastUpdate = instrumentationDataRootStem.addChildAttributeDefName(detailsDef, InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_LAST_UPDATE_ATTR, InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_LAST_UPDATE_ATTR);
            }
          }
          
          {
            String engineNameName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_ENGINE_NAME_ATTR;
            
            AttributeDefName engineName = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
                engineNameName, false, new QueryOptions().secondLevelCache(false));
            
            if (engineName == null) {
              engineName = instrumentationDataRootStem.addChildAttributeDefName(detailsDef, InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_ENGINE_NAME_ATTR, InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_ENGINE_NAME_ATTR);
            }
          }
          
          {
            String serverLabelName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_SERVER_LABEL_ATTR;
            
            AttributeDefName serverLabel = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
                serverLabelName, false, new QueryOptions().secondLevelCache(false));
            
            if (serverLabel == null) {
              serverLabel = instrumentationDataRootStem.addChildAttributeDefName(detailsDef, InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_SERVER_LABEL_ATTR, InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_SERVER_LABEL_ATTR);
            }
          }
        }
        
        {
          // check collector details def and attrs
          String detailsDefName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTOR_DETAILS_DEF;
          AttributeDef detailsDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
              detailsDefName, false, new QueryOptions().secondLevelCache(false));
          if (detailsDef == null) {
            detailsDef = instrumentationDataRootStem.addChildAttributeDef(InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTOR_DETAILS_DEF, AttributeDefType.attr);
            detailsDef.setAssignToGroupAssn(true);
            detailsDef.setValueType(AttributeDefValueType.string);
            detailsDef.store();
          }
          
          Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(detailsDef);
          
          {
            String lastUpdateName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTOR_LAST_UPDATE_ATTR;
            
            AttributeDefName lastUpdate = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
                lastUpdateName, false, new QueryOptions().secondLevelCache(false));
            
            if (lastUpdate == null) {
              lastUpdate = instrumentationDataRootStem.addChildAttributeDefName(detailsDef, InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTOR_LAST_UPDATE_ATTR, InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTOR_LAST_UPDATE_ATTR);
            }
          }
          
          {
            String uuidName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTOR_UUID_ATTR;
            
            AttributeDefName uuid = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
                uuidName, false, new QueryOptions().secondLevelCache(false));
            
            if (uuid == null) {
              uuid = instrumentationDataRootStem.addChildAttributeDefName(detailsDef, InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTOR_UUID_ATTR, InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTOR_UUID_ATTR);
            }
          }
        }
        
        {
          // check instances group
          String groupName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCES_GROUP;
          Group group = GrouperDAOFactory.getFactory().getGroup().findByNameSecure(
              groupName, false, new QueryOptions().secondLevelCache(false), GrouperUtil.toSet(TypeOfGroup.group));
          if (group == null) {
            group = instrumentationDataRootStem.addChildGroup(InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCES_GROUP, InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCES_GROUP);
            GroupFinder.groupCacheAsRootAddSystemGroup(group);
          }
        }
        
        {
          // check collectors group
          String groupName = instrumentationDataRootStemName + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTORS_GROUP;
          Group group = GrouperDAOFactory.getFactory().getGroup().findByNameSecure(
              groupName, false, new QueryOptions().secondLevelCache(false), GrouperUtil.toSet(TypeOfGroup.group));
          if (group == null) {
            group = instrumentationDataRootStem.addChildGroup(InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTORS_GROUP, InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTORS_GROUP);
            GroupFinder.groupCacheAsRootAddSystemGroup(group);
          }
        }
      }
      
      {
        String userDataRootStemName = GrouperUserDataUtils.grouperUserDataStemName();
        
        Stem userDataStem = StemFinder.findByName(grouperSession, userDataRootStemName, false, new QueryOptions().secondLevelCache(false));
        if (userDataStem == null) {
          userDataStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in Grouper user data attributes").assignName(userDataRootStemName)
            .save();
        }

        {
          //see if attributeDef is there
          String userDataDefName = userDataRootStemName + ":" + GrouperUserDataUtils.USER_DATA_DEF;
          AttributeDef userDataDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
              userDataDefName, false, new QueryOptions().secondLevelCache(false));
          if (userDataDef == null) {
            userDataDef = userDataStem.addChildAttributeDef(GrouperUserDataUtils.USER_DATA_DEF, AttributeDefType.attr);
            userDataDef.setAssignToImmMembership(true);
            userDataDef.setValueType(AttributeDefValueType.marker);
            userDataDef.store();
          }
          
          Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(userDataDef);
          
          //add an attribute for the loader ldap marker
          {
            checkAttribute(userDataStem, userDataDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_MARKER, "Grouper user data", 
                "Marks a group that has memberships which have attributes for user data", wasInCheckConfig);
          }
        }
        {
          //see if attributeDef is there
          String userDataValueDefName = userDataRootStemName + ":" + GrouperUserDataUtils.USER_DATA_VALUE_DEF;
          AttributeDef userDataValueDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
              userDataValueDefName, false, new QueryOptions().secondLevelCache(false));
          if (userDataValueDef == null) {
            userDataValueDef = userDataStem.addChildAttributeDef(GrouperUserDataUtils.USER_DATA_VALUE_DEF, AttributeDefType.attr);
            userDataValueDef.setAssignToImmMembershipAssn(true);
            userDataValueDef.setValueType(AttributeDefValueType.string);
            userDataValueDef.store();
          }
          
          Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(userDataValueDef);
          
          //add an attribute for the loader ldap marker
          {
            checkAttribute(userDataStem, userDataValueDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_FAVORITE_GROUPS, 
                "Grouper user data favorite groups", 
                "A list of group ids and metadata in json format that are the favorites for a user", wasInCheckConfig);
            checkAttribute(userDataStem, userDataValueDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_FAVORITE_SUBJECTS, 
                "Grouper user data favorite subjects", 
                "A list of member ids and metadata in json format that are the favorites for a user", wasInCheckConfig);
            checkAttribute(userDataStem, userDataValueDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_RECENT_GROUPS, 
                "Grouper user data recent groups", 
                "A list of group ids and metadata in json format that are the recently used groups for a user", wasInCheckConfig);
            checkAttribute(userDataStem, userDataValueDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_FAVORITE_STEMS, 
                "Grouper user data favorite folders", 
                "A list of folder ids and metadata in json format that are the favorites for a user", wasInCheckConfig);
            checkAttribute(userDataStem, userDataValueDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_RECENT_STEMS, 
                "Grouper user data recent folders", 
                "A list of folder ids and metadata in json format that are the recently used folders for a user", wasInCheckConfig);
            checkAttribute(userDataStem, userDataValueDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_RECENT_ATTIRBUTE_DEFS, 
                "Grouper user data recent attribute definitions", 
                "A list of attribute definition ids and metadata in json format that are the recently used attribute definitions for a user", wasInCheckConfig);
            checkAttribute(userDataStem, userDataValueDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_RECENT_ATTRIBUTE_DEF_NAMES, 
                "Grouper user data recent attribute definition names", 
                "A list of attribute definition name ids and metadata in json format that are the recently used attribute definition names for a user", wasInCheckConfig);
            checkAttribute(userDataStem, userDataValueDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_RECENT_SUBJECTS, 
                "Grouper user data recent subjects", 
                "A list of attribute member ids and metadata in json format that are the recently used subjects for a user", wasInCheckConfig);
            checkAttribute(userDataStem, userDataValueDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_FAVORITE_ATTIRBUTE_DEFS, 
                "Grouper user data favorite attribute definitions", 
                "A list of attribute definition ids and metadata in json format that are the favorites for a user", wasInCheckConfig);
            checkAttribute(userDataStem, userDataValueDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_RECENT_ATTIRBUTE_DEFS, 
                "Grouper user data recent attribute definitions", 
                "A list of attribute definition ids and metadata in json format that are the recently used attribute definitions for a user", wasInCheckConfig);
            checkAttribute(userDataStem, userDataValueDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_FAVORITE_ATTRIBUTE_DEF_NAMES, 
                "Grouper user data favorite attribute definition names", 
                "A list of attribute definition name ids and metadata in json format that are the favorites for a user", wasInCheckConfig);
            checkAttribute(userDataStem, userDataValueDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_RECENT_ATTRIBUTE_DEF_NAMES, 
                "Grouper user data recent attribute definition names", 
                "A list of attribute definition name ids and metadata in json format that are the recently used attribute definition names for a user", wasInCheckConfig);
            checkAttribute(userDataStem, userDataValueDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_PREFERENCES,
              "Grouper user data preferences",
              "Preferences and metadata in json format for a user", wasInCheckConfig);
            checkAttribute(userDataStem, userDataValueDef, GrouperUserDataUtils.ATTR_DEF_EXTENSION_VISUALIZATION_PREFS,
              "Grouper user data visualization preferences",
              "Recent options for the visualization form for a user in json format", wasInCheckConfig);
          }
          
        }
      }
      {
        String entitiesRootStemName = EntityUtils.attributeEntityStemName();
        
        Stem entitiesStem = StemFinder.findByName(grouperSession, entitiesRootStemName, false, new QueryOptions().secondLevelCache(false));
        if (entitiesStem == null) {
          entitiesStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
            .assignDescription("folder for built in Grouper entities attributes").assignName(entitiesRootStemName)
            .save();
        }

        //see if attributeDef is there
        String entityIdDefName = entitiesRootStemName + ":entitySubjectIdentifierDef";
        AttributeDef entityIdDef = new AttributeDefSave(grouperSession).assignName(entityIdDefName)
          .assignAttributeDefPublic(true).assignAttributeDefType(AttributeDefType.attr)
          .assignMultiAssignable(false).assignMultiValued(false).assignToGroup(true).assignValueType(AttributeDefValueType.string).save();
        
        Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(entityIdDef);
        
        if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.attribute.allow.everyEntity.privileges", true)) {
          
          //this is publicly assignable and readable
          entityIdDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
          entityIdDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);

        }
        
        //add the only name
        checkAttribute(entitiesStem, entityIdDef, EntityUtils.ATTR_DEF_EXTENSION_ENTITY_SUBJECT_IDENTIFIER, "This overrides the subjectId of the entity", wasInCheckConfig);
        
      }

      {
        //are we using this hook?
        if (GrouperUtil.trimToEmpty(GrouperConfig.retrieveConfig().propertyValueString("hooks.membership.class")).contains(MembershipOneInFolderMaxHook.class.getName())) {
          MembershipOneInFolderMaxHook.initObjectsOnce(wasInCheckConfig);
        }
        
      }
      {
        // if ignored from change log then it should be cached if not already
        for (String attributeDefId : GrouperUtil.nonNull(GrouperConfig.retrieveConfig().attributeDefIdsToIgnoreChangeLogAndAudit())) {
          AttributeDef attributeDef = AttributeDefFinder.findByIdAsRoot(attributeDefId, false);
          Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(attributeDef);
        }
        // if ignored from change log then it should be cached if not already
        for (String attributeDefNameId : GrouperUtil.nonNull(GrouperConfig.retrieveConfig().attributeDefNameIdsToIgnoreChangeLogAndAudit())) {
          AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, false);
          Hib3AttributeDefNameDAO.attributeDefNameCacheAsRootIdsAndNamesAdd(attributeDefName);
        }
      }
      
      if (autoAssignTheAutoAssignAttributes) {
        // these need to be at the end so everything else is initted
        AttributeAssignResult attributeAssignResult = attributeAutoCreateDef.getAttributeDelegate().assignAttribute(attributeAutoCreateMarker);
        attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(autoAssignIfName.getName(), attributeAutoCreateMarker.getName());
        attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(autoAssignThenNames.getName(), autoAssignIfName.getName() 
            + ", " + autoAssignThenNames.getName());
      }

      
    } catch (SessionException se) {
      throw new RuntimeException(se);
    } finally {
      if (startedGrouperSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
      if (!wasInCheckConfig) {
        inCheckConfig = false;
      }
    }
    
  }

  /**
   * get or create the legacy attribute base stem
   * @param grouperSession
   * @return the stem
   */
  public static Stem legacyAttributeBaseStem(GrouperSession grouperSession) {
    String legacyAttributesStemName =  GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
    Stem legacyAttributesStem = StemFinder.findByName(grouperSession, legacyAttributesStemName, false, new QueryOptions().secondLevelCache(false));
    if (legacyAttributesStem == null) {
      legacyAttributesStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignDescription("Folder for legacy attributes.  Do not delete.")
        .assignName(legacyAttributesStemName).save();
    }
    return legacyAttributesStem;
  }
  
}
