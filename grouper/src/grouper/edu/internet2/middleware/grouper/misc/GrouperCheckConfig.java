/*
 * @author mchyzer
 * $Id: GrouperCheckConfig.java,v 1.35 2009-12-10 08:54:15 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.hooks.CompositeHooks;
import edu.internet2.middleware.grouper.hooks.FieldHooks;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.GroupTypeHooks;
import edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks;
import edu.internet2.middleware.grouper.hooks.GrouperSessionHooks;
import edu.internet2.middleware.grouper.hooks.LifecycleHooks;
import edu.internet2.middleware.grouper.hooks.MemberHooks;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.StemHooks;
import edu.internet2.middleware.grouper.privs.AccessAdapter;
import edu.internet2.middleware.grouper.privs.NamingAdapter;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;
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
      Group group = GroupFinder.findByName(grouperSession, groupName, true);
      if (group != null) {
        if (GrouperUtil.length(groupResult) >= 1) {
          groupResult[0] = group;
        }
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
      Properties properties = GrouperUtil.propertiesFromResourceName(GROUPER_PROPERTIES_NAME);
      autoCreate = GrouperUtil.propertiesValueBoolean(properties, ApiConfig.testConfig, 
          "configuration.autocreate.system.groups", false);
    }
    
    if (autoCreate) {
      try {
        Group group = Group.saveGroup(grouperSession, null, null, groupName, displayExtension, groupDescription, null, true);
        if (GrouperUtil.length(groupResult) >= 1) {
          groupResult[0] = group;
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
      
      if (size == jarFileSize && StringUtils.equals(manifestVersion, jarVersion)
          && StringUtils.equals(name, jarFile.getName())) {
        LOG.debug("Found jarfile: " + jarFileFullName + " with correct size " + size + " and version: " + manifestVersion);
        return;
      }
      
    } catch (Exception e) {
      //LOG.error("Error finding jar: " + name, e);
      //e.printStackTrace();
      //having problems
    }
    
    //dont penalize activation.jar if the class is found...  sometimes its in java
    if (!StringUtils.equals("activation.jar", name)) {
      String error = "jarfile mismatch, expecting name: '" + name + "' size: " + size 
        + " manifest version: " + manifestVersion + ".  However the jar detected is: " 
        + jarFileFullName + ", name: " + jarFileName + " size: " + jarFileSize
        + " manifest version: " + jarVersion;
      System.err.println("Grouper warning: " + error);
      LOG.warn(error);
    }
  }
  
  /**
   * make sure a resource is on the resource path
   * @param resourcePath
   * @return false if problem or if not checking configs
   */
  public static boolean checkResource(String resourcePath) {
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
        Properties properties = GrouperUtil.propertiesFromResourceName(GROUPER_PROPERTIES_NAME);
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

  /**
   * make sure grouper config files exist
   */
  private static void checkGrouperConfigs() {
    
    //make sure config files are there
    checkGrouperConfig();
    checkResource("ehcache.xml");
    checkResource("grouper.ehcache.xml");
    checkResource("grouper.hibernate.properties");
    checkResource("log4j.properties");
    checkResource("morphString.properties");
    checkResource("sources.xml");
    
  }
  
  /**
   * go through each property and check types of values
   */
  private static void checkGrouperConfig() {
    if (!checkResource(GROUPER_PROPERTIES_NAME)) {
      return;
    }

    GrouperUtil.propertyValueClass(GROUPER_PROPERTIES_NAME, "privileges.access.interface", 
        AccessAdapter.class, true);
    
    GrouperUtil.propertyValueClass(GROUPER_PROPERTIES_NAME, "privileges.naming.interface", 
        NamingAdapter.class, true);
    
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "groups.create.grant.all.admin", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "groups.create.grant.all.optin", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "groups.create.grant.all.optout", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "groups.create.grant.all.read", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "groups.create.grant.all.update", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "groups.create.grant.all.view", true);

    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "stems.create.grant.all.create", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "stems.create.grant.all.stem", true);

    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "groups.wheel.use", true);

    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "registry.autoinit", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "configuration.detect.errors", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "configuration.display.startup.message", true);

    GrouperUtil.propertyValueClass(GROUPER_PROPERTIES_NAME, "dao.factory", 
        GrouperDAOFactory.class, true);

    GrouperUtil.propertyValueClass(GROUPER_PROPERTIES_NAME, "hooks.group.class", GroupHooks.class, false);
    GrouperUtil.propertyValueClass(GROUPER_PROPERTIES_NAME, "hooks.lifecycle.class", LifecycleHooks.class, false);
    GrouperUtil.propertyValueClass(GROUPER_PROPERTIES_NAME, "hooks.membership.class", MembershipHooks.class, false);
    GrouperUtil.propertyValueClass(GROUPER_PROPERTIES_NAME, "hooks.member.class", MemberHooks.class, false);
    GrouperUtil.propertyValueClass(GROUPER_PROPERTIES_NAME, "hooks.stem.class", StemHooks.class, false);
    GrouperUtil.propertyValueClass(GROUPER_PROPERTIES_NAME, "hooks.composite.class", CompositeHooks.class, false);
    GrouperUtil.propertyValueClass(GROUPER_PROPERTIES_NAME, "hooks.field.class", FieldHooks.class, false);
    GrouperUtil.propertyValueClass(GROUPER_PROPERTIES_NAME, "hooks.grouperSession.class", GrouperSessionHooks.class, false);
    GrouperUtil.propertyValueClass(GROUPER_PROPERTIES_NAME, "hooks.groupType.class", GroupTypeHooks.class, false);
    GrouperUtil.propertyValueClass(GROUPER_PROPERTIES_NAME, "hooks.groupTypeTuple.class", GroupTypeTupleHooks.class, false);

    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "ddlutils.exclude.subject.tables", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "ddlutils.schemaexport.installGrouperData", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "ddlutils.failIfNotRightVersion", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "ddlutils.dropBackupUuidCols", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "ddlutils.dropBackupFieldNameTypeCols", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "ddlutils.dropAttributeBackupTableFromGroupUpgrade", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "ddlutils.disableComments", true);
    
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "grouperIncludeExclude.use", true);
    GrouperUtil.propertyValueBoolean(GROUPER_PROPERTIES_NAME, "grouperIncludeExclude.requireGroups.use", true);
    
    Properties properties = GrouperUtil.propertiesFromResourceName(GROUPER_PROPERTIES_NAME);
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
      GrouperConfig.getProperty("groups.wheel.group");
      
      checkGrouperConfigs();
      
      checkGrouperJars();
      
      checkGrouperVersion();
      
      checkConfigProperties();
      
      checkGrouperDb();
      
      //might as well try to init data at this point...
      GrouperStartup.initData(false);
      
      checkGroups();
      
      checkAttributes();
      
      //delegate to subject APIconfigs
      SubjectCheckConfig.checkConfig();
    } finally {
      inCheckConfig = false;
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
    
    Properties properties = GrouperUtil.propertiesFromResourceName(GROUPER_PROPERTIES_NAME);

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
          groupName = GrouperUtil.propertiesValue(properties,groupNameKey);
          
          if (StringUtils.isBlank(groupName)) {
            break;
          }
          
          String groupDescription = GrouperUtil.propertiesValue(properties,"configuration.autocreate.group.description." + i);
          String subjectsKey = "configuration.autocreate.group.subjects." + i;
          String subjects = GrouperUtil.propertiesValue(properties,subjectsKey);
    
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
      boolean useWheel = GrouperUtil.booleanValue(properties.getProperty("groups.wheel.use", "false"));
      if (useWheel) {
        String wheelName = GrouperUtil.propertiesValue(properties,"groups.wheel.group");
        if (StringUtils.isBlank(wheelName) && wasInCheckConfig) {
          String error = "grouper.properties property groups.wheel.group should not be blank if groups.wheel.use is true";
          System.err.println("Grouper error: " + error);
          LOG.warn(error);
        } else {
          checkGroup(grouperSession, wheelName, wasInCheckConfig, null, wasInCheckConfig, null, "system administrators with all privileges", 
              "wheel group from grouper.properties key: groups.wheel.group", null);
        }
      }
      
      // security.stem.groupAllowedToMoveStem
      String allowedGroupName = "security.stem.groupAllowedToMoveStem";
      String groupAllowedToMoveStem = GrouperUtil.propertiesValue(properties, allowedGroupName);
      if (StringUtils.isNotBlank(groupAllowedToMoveStem)) {
        checkGroup(grouperSession, groupAllowedToMoveStem, wasInCheckConfig, null, wasInCheckConfig, null, 
            null, "grouper.properties key: " + allowedGroupName, null);        
      }
      
      // security.stem.groupAllowedToRenameStem
      allowedGroupName = "security.stem.groupAllowedToRenameStem";
      String groupAllowedToRenameStem = GrouperUtil.propertiesValue(properties, allowedGroupName);
      if (StringUtils.isNotBlank(groupAllowedToRenameStem)) {
        checkGroup(grouperSession, groupAllowedToRenameStem, wasInCheckConfig, null, wasInCheckConfig, null, 
            null, "grouper.properties key: " + allowedGroupName, null);        
      }
      
      // security.stem.groupAllowedToCopyStem
      allowedGroupName = "security.stem.groupAllowedToCopyStem";
      String groupAllowedToCopyStem = GrouperUtil.propertiesValue(properties, allowedGroupName);
      if (StringUtils.isNotBlank(groupAllowedToCopyStem)) {
        checkGroup(grouperSession, groupAllowedToCopyStem, wasInCheckConfig, null, wasInCheckConfig, null, 
            null, "grouper.properties key: " + allowedGroupName, null);        
      }
      
      //groups in requireGroups
      i=0;
      while(true) {
        String groupName = GrouperUtil.propertiesValue(properties,"grouperIncludeExclude.requireGroup.group." + i);
        
        if (StringUtils.isBlank(groupName)) {
          break;
        }
        
        String key = "grouperIncludeExclude.requireGroup.description." + i;
        String description = GrouperUtil.propertiesValue(properties,key);
        
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
    return retrievePropertiesKeys(GROUPER_PROPERTIES_NAME, typeSecurityPattern);
  }
  
  /**
   * make sure the grouper.hibernate.properties db settings are correct
   */
  public static void checkGrouperDb() {
    Properties grouperHibernateProperties = GrouperUtil.propertiesFromResourceName(
        "grouper.hibernate.properties");

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
    
    String dialect = StringUtils.defaultString(GrouperUtil.propertiesValue(grouperHibernateProperties,"hibernate.dialect"));
    
    boolean isDialectOracle = dialect.toLowerCase().contains("oracle");
    boolean isDialectPostgres = dialect.toLowerCase().contains("postgres");
    boolean isDialectMysql = dialect.toLowerCase().contains("mysql");
    boolean isDialectHsql = dialect.toLowerCase().contains("hsql");
    
    if ((isDriverOracle && !isDialectOracle) || (isDriverPostgres && !isDialectPostgres) 
        || (isDriverMysql && !isDialectMysql) || (isDriverHsql && !isDialectHsql)
        || (!isDriverOracle && isDialectOracle) || (!isDriverPostgres && isDialectPostgres) 
        || (!isDriverMysql && isDialectMysql) || (!isDriverHsql && isDialectHsql)) {
      String error = "Grouper error: detected mismatch in hibernate.connection.driver_class ("
              + realDriverClass + ") and hibernate.dialect (" + dialect 
              + ") in grouper.hibernate.properties" + spySuffix;
      System.err.println(error);
      LOG.error(error);
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
      
      dbPassword = Morph.decryptIfFile(dbPassword);
      Class driverClass = null;
      try {
        driverClass = GrouperUtil.forName(driverClassName);
      } catch (Exception e) {
        String error = "Error finding database driver class from " + databaseDescription + ": " 
          + driverClassName
          + ", perhaps you did not put the database driver jar in the lib/custom dir or lib dir, " +
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
            + "', perhaps you did not put the database driver jar in the lib/custom dir or lib dir, " +
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

    checkConfigProperties(GROUPER_PROPERTIES_NAME, "grouper.example.properties");
    checkConfigProperties("grouper.hibernate.properties", "grouper.hibernate.example.properties");
    checkConfigProperties("morphString.properties", "morphString.example.properties");
    
    checkGrouperConfigDbChange();
    checkGrouperConfigGroupNameValidators();
    checkGrouperConfigIncludeExcludeAndGroups();
    checkGrouperConfigAutocreateGroups();
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
    Map<String, String> dbMap = retrievePropertiesKeys("grouper-loader.properties", 
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
        String error = "cannot find grouper-loader.properties key: " + driverKey; 
        System.out.println("Grouper error: " + error);
        LOG.error(error);
        missingOne = true;
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
   * check the grouper loader consumer configs
   */
  public static void checkGrouperLoaderConsumers() {

    //changeLog.consumer.ldappc.class = 
    //changeLog.consumer.ldappc.quartz.cron
    
    //make sure sequences are ok
    Map<String, String> consumerMap = retrievePropertiesKeys("grouper-loader.properties", 
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
   * check the grouper config group name validators
   */
  private static void checkGrouperConfigGroupNameValidators() {
    //#group.attribute.validator.attributeName.0=extension
    //#group.attribute.validator.regex.0=^[a-zA-Z0-9]+$
    //#group.attribute.validator.vetoMessage.0=Group ID '$attributeValue$' is invalid since it must contain only alpha-numerics
    
    //make sure sequences are ok
    Map<String, String> validatorKeys = retrievePropertiesKeys(GROUPER_PROPERTIES_NAME, groupValidatorPattern);
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
    Map<String, String> validatorKeys = retrievePropertiesKeys(GROUPER_PROPERTIES_NAME, autocreateGroupsPattern);
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
    Map<String, String> validatorKeys = retrievePropertiesKeys(GROUPER_PROPERTIES_NAME, includeExcludeAndGroupPattern);
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
    Map<String, String> dbChangeKeys = retrievePropertiesKeys(GROUPER_PROPERTIES_NAME, dbChangePattern);
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
  
  /**
   * find all keys with a certain pattern in a properties file.
   * return the keys.  if none, will return the empty set, not null set
   * @param resourceName
   * @param pattern
   * @return the keys.  if none, will return the empty set, not null set
   */
  public static Map<String, String> retrievePropertiesKeys(String resourceName, Pattern pattern) {
    Properties properties = GrouperUtil.propertiesFromResourceName(resourceName);
    Map<String, String> result = new LinkedHashMap<String, String>();
    for (String key: (Set<String>)(Object)properties.keySet()) {
      if (pattern.matcher(key).matches()) {
        result.put(key, (String)properties.get(key));
      }
    }
    
    //add in api config overrides for testing
    for (String key: ApiConfig.testConfig.keySet()) {
      if (pattern.matcher(key).matches()) {
        result.put(key, ApiConfig.testConfig.get(key));
      }
    }
    
    return result;
  }
  
  /**
   * make sure grouper versions match up
   */
  private static void checkGrouperVersion() {
    //grouper version must match in grouper.version.properties,
    //the manifest, and GrouperVersion class
    String grouperVersionFromClass = GrouperVersion.GROUPER_VERSION;
    String grouperVersionFromProperties = null;
    String grouperManifestVersion = null;
    try {
      Properties properties = GrouperUtil.propertiesFromResourceName("grouper.version.properties");
      grouperVersionFromProperties = GrouperUtil.propertiesValue(properties, "version");
      grouperManifestVersion = jarVersion(GrouperCheckConfig.class);
      
    } catch (Exception e) {
      
    }
    if (!StringUtils.equals(grouperVersionFromClass, grouperVersionFromProperties)
        || !StringUtils.equals(grouperVersionFromClass, grouperManifestVersion)) {
      if (grouperVersionFromProperties == null || grouperManifestVersion == null) {
        File jarFile = GrouperUtil.jarFile(GrouperCheckConfig.class, true);
        if (jarFile == null || !jarFile.exists() || jarFile.isDirectory()) {
          return;
        }
      }
      String error = "grouper versions do not match, GrouperVersion.class: " + grouperVersionFromClass
        + ", grouper.version.properties: " + grouperVersionFromProperties
        + ", manifest: " + grouperManifestVersion;
      System.out.println("Grouper error: " + error);
      LOG.error(error);
    }
  }
  
  /**
   * make sure grouper jars are ok
   */
  private static void checkGrouperJars() {
    //NOTE, START THIS IS GENERATED BY GrouperCheckConfig.main()
    checkJar("ant.jar", 3323026, "org.apache.tools.ant.AntClassLoader", "1.7.1");
    checkJar("antlr.jar", 443330, "antlr.actions.cpp.ActionLexer", "2.7.6");
    checkJar("asm-attrs.jar", 16777, "org.objectweb.asm.attrs.Annotation", "1.5.3");
    checkJar("asm-util.jar", 32684, "org.objectweb.asm.util.ASMifierClassVisitor", "1.5.3");
    checkJar("asm.jar", 26360, "org.objectweb.asm.Attribute", "1.5.3");
    checkJar("backport-util-concurrent.jar", 328268, "edu.emory.mathcs.backport.java.util.AbstractCollection", "3.0");
    checkJar("bsh.jar", 281694, "bsh.BSHFormalComment", "2.0b4 2005-05-23 11:49:20");
    checkJar("c3p0.jar", 1064264, "com.mchange.lang.ByteUtils", "0.9.1.2");
    checkJar("cglib.jar", 454154, "net.sf.cglib.beans.BeanCopier", "2.1.3");
    checkJar("commons-beanutils.jar", 173783, "org.apache.commons.beanutils.BasicDynaBean", "0.1.0");
    checkJar("commons-betwixt.jar", 242227, "org.apache.commons.betwixt.expression.MethodExpression", "0.8");
    checkJar("commons-cli.jar", 41123, "org.apache.commons.cli.AlreadySelectedException", "1.2");
    checkJar("commons-codec.jar", 46725, "org.apache.commons.codec.BinaryDecoder", "1.3");
    checkJar("commons-collections.jar", 570463, "org.apache.commons.collections.ArrayStack", "0.1.0");
    checkJar("commons-digester.jar", 136649, "org.apache.commons.digester.AbstractObjectCreationFactory", "0.1.0");
    checkJar("commons-discovery.jar", 76685, "org.apache.commons.discovery.ant.ServiceDiscoveryTask", "0.4");
    checkJar("commons-httpclient.jar", 279383, "org.apache.commons.httpclient.auth.AuthChallengeException", "3.0");
    checkJar("commons-io.jar", 263589, "org.apache.commons.io.comparator.DefaultFileComparator", "1.4");
    checkJar("commons-jexl.jar", 388170, "org.apache.commons.jexl2.DebugInfo", "2.0.1");
    checkJar("commons-lang.jar", 468109, "org.apache.commons.lang.ArrayUtils", "2.1");
    checkJar("commons-logging.jar", 131078, "org.apache.commons.logging.impl.AvalonLogger", "1.1.1");
    checkJar("commons-math.jar", 174535, "org.apache.commons.math.distribution.ExponentialDistributionImpl", "1.1");
    checkJar("ddlUtils.jar", 713153, "org.apache.ddlutils.alteration.AddColumnChange", "1.0");
    checkJar("dom4j.jar", 730604, "org.dom4j.Attribute", "1.6.1");
    checkJar("ehcache.jar", 527332, "net.sf.ehcache.bootstrap.BootstrapCacheLoader", "1.4.0");
    checkJar("ezmorph.jar", 86542, "net.sf.ezmorph.array.AbstractArrayMorpher", "1.0.6");
    checkJar("hibernate.jar", 3770617, "org.hibernate.action.BulkOperationCleanupAction", "3.2.6.ga");
    checkJar("invoker.jar", 27767, "com.dawidweiss.invoker.Invoker", "1.0");
    checkJar("jakarta-oro.jar", 65261, "org.apache.oro.io.AwkFilenameFilter", "2.0.8 2003-12-28 11:00:13");
    checkJar("jamon.jar", 280580, "com.jamonapi.aop.JAMonEJBInterceptor", "JAMon 2.7");
    checkJar("json-lib.jar", 255813, "net.sf.json.filters.AndPropertyFilter", "2.3");
    checkJar("jsr107cache.jar", 8302, "net.sf.jsr107cache.Cache", "1.0");
    checkJar("jta.jar", 8374, "javax.transaction.HeuristicCommitException", "1.0.1B");
    checkJar("jug.jar", 19091, "com.ccg.net.ethernet.BadAddressException", "1.1.1");
    checkJar("log4j.jar", 391834, "org.apache.log4j.xml.XMLLayout", "1.2.15");
    checkJar("mailapi.jar", 178533, "javax.mail.Address", "1.3.2");
    checkJar("morphString.jar", 153260, "edu.internet2.middleware.morphString.Crypto", "1.1");
    checkJar("odmg.jar", 42111, "org.odmg.ClassNotPersistenceCapableException", "0.1.0");
    checkJar("p6spy.jar", 389539, "com.p6spy.engine.common.FastExternalUtils", "1.1");
    checkJar("quartz.jar", 792769, "org.quartz.Calendar", "1.6.0");
    checkJar("smack.jar", 1381464, "com.jcraft.jzlib.Deflate", "3.1.0");
    checkJar("smtp.jar", 23567, "com.sun.mail.smtp.DigestMD5", "1.3.2");
    checkJar("subject.jar", 118749, "edu.internet2.middleware.subject.InvalidQueryException", "1.6.0");
    // can't find class in jar: xpp3_min.jar
    checkJar("xstream.jar", 692061, "com.thoughtworks.xstream.alias.CannotResolveClassException", "1.3");
    //NOTE, END THIS IS GENERATED BY GrouperCheckConfig.main()

  }
  
  /**
   * generate the jars to find
   * @param args 
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    generateCheckJars();
  }

  /**
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws Exception
   */
  private static void generateCheckJars() throws IOException, ClassNotFoundException,
      Exception {
    //find resources dir
    File log4jFile = GrouperUtil.fileFromResourceName("log4j.properties");
    File confDir = log4jFile.getParentFile();
    File grouperDir = confDir.getParentFile();
    File libDir = new File(GrouperUtil.fileCanonicalPath(grouperDir) + File.separator + "lib");
    if (!libDir.exists()) {
      throw new RuntimeException("Cant find lib dir: " + libDir.getCanonicalPath());
    }
    File grouperLibDir = new File(libDir.getCanonicalPath() + File.separator + "grouper");
    if (!grouperLibDir.exists()) {
      throw new RuntimeException("Cant find grouper lib dir: " + grouperLibDir.getCanonicalPath());
    }
    for (File file : grouperLibDir.listFiles()) {
      if (file.getCanonicalPath().endsWith(".jar")) {
        //it was reading from JRE... thats not good...
        if (file.getCanonicalPath().contains("activation.jar")) {
          continue;
        }
        JarFile jarFile = new JarFile(file);
        Class sampleClass = null;
        Enumeration<JarEntry> enumeration = jarFile.entries();
        while (enumeration.hasMoreElements()) {
          JarEntry jarEntry = enumeration.nextElement();
          String jarEntryName = jarEntry.getName();
          if (jarEntryName.endsWith(".class") && !jarEntryName.contains("$")) {
            String className = jarEntryName.substring(0, jarEntryName.length()-6);
            className = className.replace('/', '.');
            className = className.replace('\\', '.');
            //these dont work
            if ("javax.activation.SecuritySupport12".equals(className)) {
              continue;
            }
            Class tempClass = null;
            try {
              tempClass = Class.forName(className);
            } catch  (Throwable t) {
              LOG.warn("Problem with class: " + className + ", in jar: " + file.getCanonicalPath(), t);
              continue;
            }
            if (Modifier.isPublic(tempClass.getModifiers())) {
              sampleClass = tempClass;
              break;
            }
          }
        }
        if (sampleClass == null) {
          System.out.println("    // can't find class in jar: " + file.getName());
          continue;
        }
        String version = jarVersion(sampleClass);
        System.out.println("    checkJar(\"" + file.getName() + "\", " + file.length() 
            + ", \"" + sampleClass.getName() + "\", \"" + version + "\");");
      }
    }
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
   * <pre>
   * match type security
   * match: security.typeName.wheelOnly
   * match: security.typeName.allowOnlyGroup
   * </pre>
   */
  public static final Pattern typeSecurityPattern = Pattern.compile(
      "^security\\.types\\.(.*)\\.(wheelOnly|allowOnlyGroup)$");
  
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
    String attributeDefNameName = stem.getName() + ":" + extension;
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, false);
    if (attributeDefName == null) {
      attributeDefName = stem.addChildAttributeDefName(attributeDef, extension, extension);
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
    String rootStemName = GrouperConfig.getProperty("grouper.attribute.rootStem");
    if (StringUtils.isBlank(rootStemName)) {
      throw new RuntimeException("If autoconfiguring attributes, you need to configure a root stem");
    }
    return rootStemName;
  }
  
  /**
   * make sure configured attributes are there 
   */
  public static void checkAttributes() {
    
    boolean autoconfigure = GrouperConfig.getPropertyBoolean("grouper.attribute.loader.autoconfigure", false);
    if (!autoconfigure) {
      return;
    }

    boolean wasInCheckConfig = inCheckConfig;
    if (!wasInCheckConfig) {
      inCheckConfig = true;
    }

    String rulesRootStemName = RuleUtils.attributeRuleStemName();
    
    String loaderRootStemName = attributeLoaderStemName();
    
    GrouperSession grouperSession = null;
    boolean startedGrouperSession = false;
    try {
      grouperSession = GrouperSession.staticGrouperSession(false);

      if (grouperSession == null) {
        grouperSession = GrouperSession.startRootSession();
        startedGrouperSession = true;
      }
      
      Stem rulesStem = StemFinder.findByName(grouperSession, rulesRootStemName, false);
      if (rulesStem == null) {
        rulesStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignDescription("folder for built in Grouper rules attributes").assignName(rulesRootStemName)
          .save();
      }

      Stem loaderStem = StemFinder.findByName(grouperSession, loaderRootStemName, false);
      if (loaderStem == null) {
        loaderStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignDescription("folder for built in Grouper loader attributes").assignName(loaderRootStemName)
          .save();
      }

      {
        //see if attributeDef is there
        String ruleTypeDefName = rulesRootStemName + ":rulesTypeDef";
        AttributeDef ruleType = AttributeDefFinder.findByName(ruleTypeDefName, false);
        if (ruleType == null) {
          ruleType = rulesStem.addChildAttributeDef("rulesTypeDef", AttributeDefType.type);
          ruleType.setAssignToGroup(true);
          ruleType.setAssignToStem(true);
          ruleType.setAssignToAttributeDef(true);
          ruleType.setMultiAssignable(true);
          ruleType.store();
        }
        
        //add a name
        AttributeDefName rule = checkAttribute(rulesStem, ruleType, "rule", "is a rule", wasInCheckConfig);
        
        //lets add some rule attributes
        String ruleAttrDefName = rulesRootStemName + ":rulesAttrDef";
        AttributeDef ruleAttrType = AttributeDefFinder.findByName(ruleAttrDefName, false);
        if (ruleAttrType == null) {
          ruleAttrType = rulesStem.addChildAttributeDef("rulesAttrDef", AttributeDefType.attr);
          ruleAttrType.setAssignToGroupAssn(true);
          ruleAttrType.setAssignToStemAssn(true);
          ruleAttrType.setValueType(AttributeDefValueType.string);
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
            "RuleConditionEnum that sees if rule should fire, or blank if should run always", wasInCheckConfig);
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
      
      AttributeDefName attributeLoaderTypeName = null;
      
      {
        //see if attributeDef is there
        String attributeDefLoaderTypeDefName = loaderRootStemName + ":attributeDefLoaderTypeDef";
        AttributeDef attributeDefType = AttributeDefFinder.findByName(attributeDefLoaderTypeDefName, false);
        if (attributeDefType == null) {
          attributeDefType = loaderStem.addChildAttributeDef("attributeDefLoaderTypeDef", AttributeDefType.type);
          attributeDefType.setAssignToAttributeDef(true);
          attributeDefType.store();
        }
        
        //add a name
        attributeLoaderTypeName = checkAttribute(loaderStem, attributeDefType, "attributeLoader", 
            "is a loader based attribute def, the loader attributes will be available to be assigned", wasInCheckConfig);
        
      }      
      
      {
        //see if attributeDef is there
        String attributeDefLoaderDefName = loaderRootStemName + ":attributeDefLoaderDef";
        AttributeDef attributeDef = AttributeDefFinder.findByName(attributeDefLoaderDefName, false);
        if (attributeDef == null) {
          attributeDef = loaderStem.addChildAttributeDef("attributeDefLoaderDef", AttributeDefType.attr);
          attributeDef.setAssignToAttributeDef(true);
          attributeDef.setValueType(AttributeDefValueType.string);
          attributeDef.store();
        }
        
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
  
}
