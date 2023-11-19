/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.abac.GrouperAbac;
import edu.internet2.middleware.grouper.abac.GrouperJexlScriptAnalysis;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperRecentMemberships;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiDaemonJob;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGrouperLoaderJob;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiHib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.misc.GrouperFailsafe;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;
import net.redhogs.cronparser.CronExpressionDescriptor;


/**
 *
 */
public class GrouperLoaderContainer {

  public static void main(String[] args) {
  }
  
  /**
   * recent memberships from group id
   */
  private String editLoaderRecentGroupUuidFrom;
  
  
  
  /**
   * recent memberships from group id
   * @return from group id
   */
  public String getEditLoaderRecentGroupUuidFrom() {
    return this.editLoaderRecentGroupUuidFrom;
  }

  /**
   * recent memberships from group id
   * @param editLoaderRecentFromGroupId1
   */
  public void setEditLoaderRecentGroupUuidFrom(String editLoaderRecentGroupUuidFrom) {
    this.editLoaderRecentGroupUuidFrom = editLoaderRecentGroupUuidFrom;
  }

  /**
   * this is a number, could have two decimal places
   */
  private String editLoaderRecentDays;

  /**
   * this is a number, could have two decimal places
   * @return edit loader recent days
   */
  public String getEditLoaderRecentDays() {
    return this.editLoaderRecentDays;
  }

  /**
   * this is a number, could have two decimal places
   * @param editLoaderRecentDays1
   */
  public void setEditLoaderRecentDays(String editLoaderRecentDays1) {
    this.editLoaderRecentDays = editLoaderRecentDays1;
  }

  /**
   * if should include current members
   */
  private String editLoaderRecentIncludeCurrent = "true";

  /**
   * if should include current members
   * @return if should include current members
   */
  public String getEditLoaderRecentIncludeCurrent() {
    return this.editLoaderRecentIncludeCurrent;
  }

  /**
   * if should include current members
   * @param editLoaderRecentIncludeCurrent1
   */
  public void setEditLoaderRecentIncludeCurrent(String editLoaderRecentIncludeCurrent1) {
    this.editLoaderRecentIncludeCurrent = editLoaderRecentIncludeCurrent1;
  }

  /**
   * script to run
   */
  private String editLoaderJexlScriptJexlScript;

  /**
   * script to run
   * @return script
   */
  public String getEditLoaderJexlScriptJexlScript() {
    return this.editLoaderJexlScriptJexlScript;
  }

  /**
   * script to run
   * @param editLoaderJexlScriptJexlScript1
   */
  public void setEditLoaderJexlScriptJexlScript(String editLoaderJexlScriptJexlScript1) {
    this.editLoaderJexlScriptJexlScript = editLoaderJexlScriptJexlScript1;
  }

  /**
   * T or F to include internal sources
   */
  private Boolean editLoaderJexlScriptIncludeInternalSources;

  /**
   * T or F to include internal sources
   * @return T or F
   */
  public Boolean getEditLoaderJexlScriptIncludeInternalSources() {
    return this.editLoaderJexlScriptIncludeInternalSources;
  }

  /**
   * T or F to include internal sources
   * @param editLoaderJexlScriptIncludeInternalSources1
   */
  public void setEditLoaderJexlScriptIncludeInternalSources(Boolean editLoaderJexlScriptIncludeInternalSources1) {
    this.editLoaderJexlScriptIncludeInternalSources = editLoaderJexlScriptIncludeInternalSources1;
  }

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderContainer.class);

  /**
   * 
   */
  public GrouperLoaderContainer() {
  }

  /**
   * 
   * @return number of rows
   */
  public int getNumberOfRows() {
    return GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.loader.logs.maxSize", 400);
  }

  /**
   * 
   * @return true if this job has subjobs
   */
  public boolean isHasSubjobs() {
    
    GrouperLoaderType grouperLoaderType = this.getGrouperLoaderType();
    
    if (grouperLoaderType != null && 
        (grouperLoaderType == GrouperLoaderType.LDAP_GROUP_LIST 
          || grouperLoaderType == GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES 
          || grouperLoaderType == GrouperLoaderType.SQL_GROUP_LIST)) {
      return true;
    }
    
    return false;
  }
  
  /**
   * 
   */
  private List<GuiHib3GrouperLoaderLog> guiHib3GrouperLoaderLogs;

  //private static Pattern groupIdFromJobNamePattern = Pattern.compile(".*__([^_]+)$");
  /**
   * pattern to get group id from job name
   * SQL_GROUP_LIST__penn:community:emplo__yee:affiliationPrimaryConfig__fa9dca910f9a4accb8529dd040dc1198
   */
  private static Pattern groupNameFromJobNamePattern = Pattern.compile("^.*?__(.*)__.*$");
  
  /**
   * group name from subjob name
   */
  private static Pattern groupNameFromSubjobNamePattern = Pattern.compile("^subjobFor_(.*)$");
  
  private GuiDaemonJob guiDaemonJob = null;
  
  private boolean hasRetrievedDaemonJob = false;
  
  
  
  /**
   * retrieve group name from job name
   * @param jobName
   * @return group id
   */
  public static String retrieveGroupNameFromJobName(String jobName) {
    
    if (StringUtils.isBlank(jobName)) {
      return null;
    }
    
    //try normal job
    Matcher matcher = groupNameFromJobNamePattern.matcher(jobName);
    
    if (matcher.matches()) {
      return matcher.group(1);
    }

    //try subjob
    matcher = groupNameFromSubjobNamePattern.matcher(jobName);
      
    if (matcher.matches()) {
      return matcher.group(1);
    }
    return null;
    
  }
  
  /**
   * hib3 loader logs
   * @return the list of logs
   */
  public List<GuiHib3GrouperLoaderLog> getGuiHib3GrouperLoaderLogs() {

    return this.guiHib3GrouperLoaderLogs;

  }

  /**
   * @param guiHib3GrouperLoaderLogs1 the guiHib3GrouperLoaderLogs to set
   */
  public void setGuiHib3GrouperLoaderLogs(List<GuiHib3GrouperLoaderLog> guiHib3GrouperLoaderLogs1) {
    this.guiHib3GrouperLoaderLogs = guiHib3GrouperLoaderLogs1;
  }

  /**
   * 
   * @return the sql group query
   */
  public String getSqlGroupQuery() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderGroupQuery = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_GROUP_QUERY);
    
    return grouperLoaderGroupQuery;
    
  }
  
  /**
   * 
   * @return sql groups like
   */
  public String getSqlGroupsLike() {

    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderGroupsLike = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_GROUPS_LIKE);
    
    return grouperLoaderGroupsLike;

  }
  
  /**
   * @return sql group types
   */
  public String getSqlGroupTypes() {

    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderGroupsLike = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_GROUP_TYPES);
    
    return grouperLoaderGroupsLike;

  }
  
  /**
   * @return display name sync type
   */
  public String getDisplayNameSyncType() {

    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderDisplayNameSyncType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_DISPLAY_NAME_SYNC_TYPE);
    return grouperLoaderDisplayNameSyncType;

  }

  /**
   * @return display name sync base folder name
   */
  public String getDisplayNameSyncBaseFolderName() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderDisplayNameSyncBaseFolderName = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_DISPLAY_NAME_SYNC_BASE_FOLDER_NAME);
    return grouperLoaderDisplayNameSyncBaseFolderName;
    
  }

  /**
   * @return display name sync levels
   */
  public String getDisplayNameSyncLevels() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderDisplayNameSyncLevels = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_DISPLAY_NAME_SYNC_LEVELS);
    return grouperLoaderDisplayNameSyncLevels;
    
  }
  
  /**
   * 
   * @return database name
   */
  public String getSqlDatabaseName() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderDbName = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_DB_NAME);
    
    return grouperLoaderDbName;
    
  }

  /**
   * config name e.g. for databases
   */
  public static class ConfigName implements Comparable<ConfigName> {
    
    /** id of config name */
    private String id;
    
    /** name of config name */
    private String name;

    /**
     * 
     * @param theId
     * @param theName
     */
    public ConfigName(String theId, String theName) {
      this.id = theId;
      this.name = theName;
    }
    
    /**
     * id of config name
     * @return the id
     */
    public String getId() {
      return this.id;
    }

    
    /**
     * id of config name
     * @param id1 the id to set
     */
    public void setId(String id1) {
      this.id = id1;
    }

    
    /**
     * name of config name
     * @return the name
     */
    public String getName() {
      return this.name;
    }

    
    /**
     * name of config name
     * @param name1 the name to set
     */
    public void setName(String name1) {
      this.name = name1;
    }

    @Override
    public int compareTo(ConfigName o) {
      if (o == null) {
        return -1;
      }
      if (this.name == o.name) {
        return 0;
      }
      if (this.name == null) {
        return 1;
      }
      if (o.name == null) {
        return -1;
      }
      return this.name.compareTo(o.name);
      
    }
    
  }
  
  /**
   * ldap server ids
   * @return ldap server ids
   */
  public List<ConfigName> getLdapServerIds() {
    List<ConfigName> result = new ArrayList<ConfigName>();

    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

    Pattern pattern = Pattern.compile("^ldap.([^.]+).url$");

    for (String propertyName : grouperLoaderConfig.propertyNames()) {
      Matcher matcher = pattern.matcher(propertyName);
      if (!matcher.matches()) {
        continue;
      }
      String configUrlName = matcher.group(1);
      String configUrl = grouperLoaderConfig.propertyValueString(propertyName);
      result.add(new ConfigName(configUrlName, configUrlName + " - " + configUrl));
    }
    return result;
    
  }
  
  /**
   * sql database names
   * @return the database names
   */
  public List<ConfigName> getSqlDatabaseNames() {
    List<ConfigName> result = new ArrayList<ConfigName>();
    result.add(new ConfigName("grouper", "grouper - " + GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.connection.url")));

    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

    Pattern pattern = Pattern.compile("^db.([^.]+).url$");

    for (String propertyName : grouperLoaderConfig.propertyNames()) {
      Matcher matcher = pattern.matcher(propertyName);
      if (!matcher.matches()) {
        continue;
      }
      String configUrlName = matcher.group(1);
      String configUrl = grouperLoaderConfig.propertyValueString(propertyName);
      result.add(new ConfigName(configUrlName, configUrlName + " - " + configUrl));
    }
    return result;
  }
  
  /**
   * subject sources
   * @return the sources
   */
  public List<ConfigName> getSources() {
    Set<ConfigName> result = new TreeSet<ConfigName>();

    Collection<Source> sources = SourceManager.getInstance().getSources();

    for (Source source : sources) {
      result.add(new ConfigName(source.getId(), source.getId() + " - " + source.getName()));
    }
    //turn the sorted set into a list
    return new ArrayList<ConfigName>(result);
  }
  
  /**
   * 
   * @return database name
   */
  public String getSqlDatabaseNameUrl() {
    
    String databaseName = this.getSqlDatabaseName();

    return convertDatabaseNameToUrl(databaseName);

  }

  /**
   * convert a loader database name to a url
   * @param databaseName
   * @return the url
   */
  public static String convertDatabaseNameToUrl(String databaseName) {
    if (StringUtils.isBlank(databaseName)) {
      return null;
    }
    
    if (StringUtils.equals("grouper", databaseName)) {
      
      return GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.connection.url");
      
    }
    
    String databaseUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("db." + databaseName + ".url");
    return databaseUrl;
  }

  /**
   * @return ldap server id url or a message that says not found
   */
  public String getSqlDatabaseNameUrlText() {
    
    String databaseNameUrl = this.getSqlDatabaseNameUrl();
    return convertDatabaseUrlToText(databaseNameUrl);

  }

  /**
   * convert database url to text
   * @param databaseNameUrl
   * @return text
   */
  public static String convertDatabaseUrlToText(String databaseNameUrl) {
    if (!StringUtils.isBlank(databaseNameUrl)) {
      return databaseNameUrl;
    }
    
    return TextContainer.retrieveFromRequest().getText().get("grouperLoaderDatabaseNameNotFound");
  }


  /**
   * 
   * @return scheduling priority
   */
  public String getSqlPriority() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String priority = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_PRIORITY);
    
    return priority;
    
  }
  
  /**
   * 
   * @return database name
   */
  public int getSqlPriorityInt() {
    
    String priority = this.getSqlPriority();
    
    if (!StringUtils.isBlank(priority)) {
      
      try {
        
        return GrouperUtil.intValue(priority);
        
      } catch (Exception e) {
        LOG.error("Cant parse priority: '" + priority + "'", e);
        return -200;
      }
      
    }
    
    return 5;
    
  }
  
  /**
   * 
   * @return scheduling priority
   */
  public String getLdapPriority() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapPriorityName());

  }
  
  /**
   * 
   * @return groups like
   */
  public String getLdapGroupsLike() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupsLikeName());

  }
  
  /**
   * 
   * @return extra attributes
   */
  public String getLdapExtraAttributes() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapExtraAttributesName());

  }

  public Integer getLdapMaxOverallPercentGroupsRemove() {
    return GrouperUtil.intObjectValue(retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapMaxOverallPercentGroupsRemoveName()), true);


  }

  public Integer getLdapMaxOverallPercentMembershipsRemove() {
    return GrouperUtil.intObjectValue(retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapMaxOverallPercentMembershipsRemoveName()), true);
  }

  public Integer getLdapMinManagedGroups() {
    return GrouperUtil.intObjectValue(retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapMinManagedGroupsName()), true);
  }

  public Integer getLdapMinOverallNumberOfMembers() {
    return GrouperUtil.intObjectValue(retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapMinOverallNumberOfMembersName()), true);
  }

  public Integer getLdapMaxGroupPercentRemove() {
    return GrouperUtil.intObjectValue(retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapMaxGroupPercentRemoveName()), true);
  }

  public Integer getLdapMinGroupSize() {
    return GrouperUtil.intObjectValue(retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapMinGroupSizeName()), true);
  }

  public Integer getLdapMinGroupNumberOfMembers() {
    return GrouperUtil.intObjectValue(retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapMinGroupNumberOfMembersName()), true);
  }

  public Boolean getLdapFailsafeUse() {
    return GrouperUtil.booleanObjectValue(retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapFailsafeUseName()));
  }

  public Boolean getLdapFailsafeSendEmail() {
    return GrouperUtil.booleanObjectValue(retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapFailsafeSendEmailName()));
  }

  public String getLdapFailsafeUseOrDefault() {
    Boolean failsafeUse = GrouperUtil.booleanObjectValue(retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapFailsafeUseName()));
    if (failsafeUse == null) {
      return "default";
    }
    return failsafeUse ? "true" : "false";
  }

  public String getLdapFailsafeSendEmailOrDefault() {
    Boolean failsafeSendEmail = GrouperUtil.booleanObjectValue(retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapFailsafeSendEmailName()));
    if (failsafeSendEmail == null) {
      return "default";
    }
    return failsafeSendEmail ? "true" : "false";
  }

  
  /**
   * extra attributes
   */
  private String editLoaderLdapExtraAttributes;

  /**
   * extra attributes
   * @return the editLoaderLdapExtraAttributes
   */
  public String getEditLoaderLdapExtraAttributes() {
    return this.editLoaderLdapExtraAttributes;
  }

  /**
   * @param editLoaderLdapExtraAttributes1 the editLoaderLdapExtraAttributes to set
   */
  public void setEditLoaderLdapExtraAttributes(String editLoaderLdapExtraAttributes1) {
    this.editLoaderLdapExtraAttributes = editLoaderLdapExtraAttributes1;
  }

  /**
   * <pre>
   * attribute filter expression e.g. ${attributeValue == 'a' || attributeValue == 'b'}
   * </pre>
   */
  private String editLoaderLdapAttributeFilterExpression;
  
  /**
   * <pre>
   * attribute filter expression e.g. ${attributeValue == 'a' || attributeValue == 'b'}
   * </pre>
   * @return the editLoaderLdapAttributeFilterExpression
   */
  public String getEditLoaderLdapAttributeFilterExpression() {
    return this.editLoaderLdapAttributeFilterExpression;
  }

  
  /**
   * <pre>
   * attribute filter expression e.g. ${attributeValue == 'a' || attributeValue == 'b'}
   * </pre>
   * @param editLoaderLdapAttributeFilterExpression1 the editLoaderLdapAttributeFilterExpression to set
   */
  public void setEditLoaderLdapAttributeFilterExpression(
      String editLoaderLdapAttributeFilterExpression1) {
    this.editLoaderLdapAttributeFilterExpression = editLoaderLdapAttributeFilterExpression1;
  }
  
  /**
   * class name used to transform results from ldap
   */
  private String editLoaderLdapResultsTransformationClass;

  /**
   * @return the editLoaderLdapResultsTransformationClass
   */
  public String getEditLoaderLdapResultsTransformationClass() {
    return editLoaderLdapResultsTransformationClass;
  }

  
  /**
   * @param editLoaderLdapResultsTransformationClass the editLoaderLdapResultsTransformationClass to set
   */
  public void setEditLoaderLdapResultsTransformationClass(
      String editLoaderLdapResultsTransformationClass) {
    this.editLoaderLdapResultsTransformationClass = editLoaderLdapResultsTransformationClass;
  }  

  /**
   * 
   * @return attribute filter expression
   */
  public String getLdapAttributeFilterExpression() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapAttributeFilterExpressionName());

  }

  /**
   * 
   * @return ldap group description expression
   */
  public String getLdapGroupDescriptionExpression() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupDescriptionExpressionName());

  }

  /**
   * 
   * @return ldap group display name expression
   */
  public String getLdapGroupDisplayNameExpression() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupDisplayNameExpressionName());

  }
  
  /**
   * 
   * @return ldap group name expression
   */
  public String getLdapGroupNameExpression() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupNameExpressionName());

  }
  
  /**
   * 
   * @return ldap results transformation class
   */
  public String getLdapResultsTransformationClass() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapResultsTransformationClassName());

  }
  
  /**
   * 
   * @return ldap subject expression
   */
  public String getLdapSubjectExpression() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName());

  }
  
  /**
   * 
   * @return ldap group types
   */
  public String getLdapGroupTypes() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupTypesName());

  }
  
  /**
   * 
   * @return ldap readers
   */
  public String getLdapReaders() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapReadersName());

  }
  
  /**
   * 
   * @return ldap readers
   */
  public String getLdapAttrReaders() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupAttrReadersName());

  }
  
  /**
   * 
   * @return ldap viewers
   */
  public String getLdapViewers() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapViewersName());

  }
  
  
  
  /**
   * 
   * @return ldap viewers
   */
  public String getLdapAdmins() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapAdminsName());

  }
  
  /**
   * 
   * @return ldap updaters
   */
  public String getLdapUpdaters() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapUpdatersName());

  }
  
  /**
   * 
   * @return ldap attr updaters
   */
  public String getLdapAttrUpdaters() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupAttrUpdatersName());

  }
  
  /**
   * 
   * @return ldap optins
   */
  public String getLdapOptins() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapOptinsName());

  }
  
  /**
   * 
   * @return ldap optouts
   */
  public String getLdapOptouts() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapOptoutsName());

  }
  
  /**
   * 
   * @return database name
   */
  public int getLdapPriorityInt() {
    
    String priority = this.getLdapPriority();
    
    if (!StringUtils.isBlank(priority)) {
      
      try {
        
        return GrouperUtil.intValue(priority);
        
      } catch (Exception e) {
        LOG.error("Cant parse priority: '" + priority + "'", e);
        return -200;
      }
      
    }
    
    return 5;
    
  }

  /**
   * recent memeberships from group as a gui group object
   * @return gui group
   */
  public GuiGroup getRecentFromGuiGroup() {
    final String uuidFrom = this.getRecentGroupUuidFrom();
    if (StringUtils.isBlank(uuidFrom)) {
      return null;
    }
    Group group = (Group)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Group theGroup = GroupFinder.findByUuid(grouperSession, uuidFrom, true);
        
        return theGroup;
      }
    });
    return new GuiGroup(group);
  }
  
  /**
   * 
   * @return sql query
   */
  public String getRecentGroupUuidFrom() {
    
    final Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    
    String groupUuid = (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                
        AttributeDefName recentMarker = AttributeDefNameFinder.findByName(
            GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
        Set<AttributeAssign> attributeAssigns = jobGroup.getAttributeDelegate().retrieveAssignments(recentMarker);
        if (GrouperUtil.length(attributeAssigns) == 0) {
          return null;
        }
        if (GrouperUtil.length(attributeAssigns) > 1) {
          throw new RuntimeException("Not expecting multiple recent membership attribute assignments! " + jobGroup.getName());
        }
        AttributeAssign attributeAssign = attributeAssigns.iterator().next();
        String value = attributeAssign.getAttributeValueDelegate().retrieveValueString(
            GrouperRecentMemberships.recentMembershipsStemName() + ":" 
                + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM);
        
        return value;
      }
    });
    
    return groupUuid;

  }

  /**
   * 
   * @return sql query
   */
  public String getRecentDays() {
    
    final Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    
    String days = (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                
        AttributeDefName recentMarker = AttributeDefNameFinder.findByName(
            GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
        Set<AttributeAssign> attributeAssigns = jobGroup.getAttributeDelegate().retrieveAssignments(recentMarker);
        if (GrouperUtil.length(attributeAssigns) == 0) {
          return null;
        }
        if (GrouperUtil.length(attributeAssigns) > 1) {
          throw new RuntimeException("Not expecting multiple recent membership attribute assignments! " + jobGroup.getName());
        }
        AttributeAssign attributeAssign = attributeAssigns.iterator().next();
        Long micros = attributeAssign.getAttributeValueDelegate().retrieveValueInteger(
            GrouperRecentMemberships.recentMembershipsStemName() + ":" 
                + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS);
        
        if (micros == null) {
          return null;
        }
        
        double daysDouble = micros / (1000.0D * 1000 * 60 * 60 * 24D);
        
        NumberFormat numberFormatter = NumberFormat.getNumberInstance();
        numberFormatter.setMaximumFractionDigits(4);

        return numberFormatter.format(daysDouble);
      }
    });
    
    return days;

  }

  /**
   * 
   * @return jexl script
   */
  public String getJexlScriptJexlScript() {
    
    final Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    
    String jexlScript = (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                
        AttributeDefName jexlScriptMarker = AttributeDefNameFinder.findByName(
            GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, true);
        Set<AttributeAssign> attributeAssigns = jobGroup.getAttributeDelegate().retrieveAssignments(jexlScriptMarker);
        if (GrouperUtil.length(attributeAssigns) == 0) {
          return null;
        }
        if (GrouperUtil.length(attributeAssigns) > 1) {
          throw new RuntimeException("Not expecting multiple jexl script attribute assignments! " + jobGroup.getName());
        }
        AttributeAssign theAttributeAssign = attributeAssigns.iterator().next();
        String theJexlScript = theAttributeAssign.getAttributeValueDelegate().retrieveValueString(
            GrouperAbac.jexlScriptStemName() + ":" 
                + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_SCRIPT);
        return theJexlScript;
      }
    });
    
    return jexlScript;

  }

  /**
   * 
   * @return T if internal sources
   */
  public Boolean getJexlScriptIncludeInternalSources() {
    
    final Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    
    Boolean includeInternalSources = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        AttributeDefName jexlScriptMarker = AttributeDefNameFinder.findByName(
            GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, true);
        Set<AttributeAssign> attributeAssigns = jobGroup.getAttributeDelegate().retrieveAssignments(jexlScriptMarker);
        if (GrouperUtil.length(attributeAssigns) == 0) {
          return null;
        }
        if (GrouperUtil.length(attributeAssigns) > 1) {
          throw new RuntimeException("Not expecting multiple jexl script attribute assignments! " + jobGroup.getName());
        }
        AttributeAssign theAttributeAssign = attributeAssigns.iterator().next();
        String theIncludeSources = theAttributeAssign.getAttributeValueDelegate().retrieveValueString(
            GrouperAbac.jexlScriptStemName() + ":" 
                + GrouperAbac.GROUPER_JEXL_SCRIPT_INCLUDE_INTERNAL_SOURCES);
        return GrouperUtil.booleanObjectValue(theIncludeSources);
      }
    });
    
    return includeInternalSources;

  }

  /**
   * 
   * @return "T", "F" or null
   */
  public String getRecentIncludeCurrent() {
    
    final Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    
    String includeCurrent = (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                
        AttributeDefName recentMarker = AttributeDefNameFinder.findByName(
            GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
        Set<AttributeAssign> attributeAssigns = jobGroup.getAttributeDelegate().retrieveAssignments(recentMarker);
        if (GrouperUtil.length(attributeAssigns) == 0) {
          return null;
        }
        if (GrouperUtil.length(attributeAssigns) > 1) {
          throw new RuntimeException("Not expecting multiple recent membership attribute assignments! " + jobGroup.getName());
        }
        AttributeAssign attributeAssign = attributeAssigns.iterator().next();
        String value = attributeAssign.getAttributeValueDelegate().retrieveValueString(
            GrouperRecentMemberships.recentMembershipsStemName() + ":" 
                + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT);
        
        return value;
      }
    });
    
    return includeCurrent;

  }

  /**
   * 
   * @return sql query
   */
  public String getSqlQuery() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderSqlQuery = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_QUERY);
    
    return grouperLoaderSqlQuery;

  }

  /**
   * 
   * @return sql query
   */
  public String getSqlAndGroups() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderAndGroups = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_AND_GROUPS);
    return grouperLoaderAndGroups;

  }

  /**
   * 
   * @return list of gui groups
   */
  public List<GuiGroup> getSqlAndGuiGroups() {

    final List<String> andGroupsStringList = getSqlAndGroupsStringList();
    
    final List<GuiGroup> guiGroups = new ArrayList<GuiGroup>();
    
    if (GrouperUtil.length(andGroupsStringList) > 0) {

      GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          for (String andGroupString : andGroupsStringList) {
            
            Group group = GroupFinder.findByUuid(grouperSession, andGroupString, false);
            group = group != null ? group : GroupFinder.findByName(grouperSession, andGroupString, false);
            guiGroups.add(new GuiGroup(group));
            
          }
          
          return null;
        }
      });
    }    
    return guiGroups;
  }

  /**
   * convert and groups to string
   * @return the list of strings
   */
  private List<String> getSqlAndGroupsStringList() {
    String andGroupsString = this.getSqlAndGroups();
    
    if (StringUtils.isBlank(andGroupsString)) {
      return null;
    }
    
    final List<String> andGroupsStringList = GrouperUtil.splitTrimToList(andGroupsString, ",");
    return andGroupsStringList;
  }

  /**
   * 
   * @return sql query
   */
  public String getLdapAndGroups() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapAndGroupsName());

  }

  /**
   * 
   * @return list of gui groups
   */
  public List<GuiGroup> getLdapAndGuiGroups() {

    final List<String> andGroupsStringList = getLdapAndGroupsStringList();

    final List<GuiGroup> guiGroups = new ArrayList<GuiGroup>();

    if (GrouperUtil.length(andGroupsStringList) > 0) {

      GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          for (String andGroupString : andGroupsStringList) {
            
            Group group = GroupFinder.findByUuid(grouperSession, andGroupString, false);
            group = group != null ? group : GroupFinder.findByName(grouperSession, andGroupString, false);
            guiGroups.add(new GuiGroup(group));
            
          }
          
          return null;
        }
      });
    }
    
    return guiGroups;
  }

  /**
   * convert and groups to string
   * @return the list of strings
   */
  private List<String> getLdapAndGroupsStringList() {
    String andGroupsString = this.getLdapAndGroups();
    
    if (StringUtils.isBlank(andGroupsString)) {
      return null;
    }
    
    final List<String> andGroupsStringList = GrouperUtil.splitTrimToList(andGroupsString, ",");
    return andGroupsStringList;
  }


  /**
   * 
   * @return sql cron
   */
  public String getSqlCron() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderQuartzCron = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_QUARTZ_CRON);
    
    return grouperLoaderQuartzCron;

  }

  /**
   * 
   * @return ldap cron
   */
  public String getLdapCron() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapQuartzCronName());

  }

  /**
   * 
   * @return sql schedule type
   */
  public String getSqlScheduleType() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderScheduleType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE);
    
    return grouperLoaderScheduleType;

  }

  
  /**
   * 
   * @return sql loader type
   */
  public String getSqlLoaderType() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_TYPE);
    
    return grouperLoaderType;

  }

  /**
   * 
   */
  private AttributeAssign attributeAssign = null;
  
  /**
   * 
   * @return the grouper loader type
   */
  public GrouperLoaderType getGrouperLoaderType() {
    String grouperLoaderTypeString = null;
    
    if (this.isGrouperSqlLoader()) {
      grouperLoaderTypeString = this.getSqlLoaderType();
    } else if (this.isGrouperLdapLoader()) {
      grouperLoaderTypeString = this.getLdapLoaderType();
    }
    
    if (StringUtils.isBlank(grouperLoaderTypeString)) {
      return null;
    }
    
    GrouperLoaderType grouperLoaderType = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderTypeString, true);
    
    return grouperLoaderType;

  }

  /**
   * 
   * @return job name
   */
  public String getJobName() {
    GrouperLoaderType grouperLoaderType = this.getGrouperLoaderType();
    
    if (grouperLoaderType == null) {
      return null;
    }

    Group group = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();

    return grouperLoaderType.name() + "__" + group.getName() + "__" + group.getUuid();

  }
  
  /**
   * @return state of job
   */
  public String getSchedulerState() {
    GuiDaemonJob guiDaemonJob = this.getGuiDaemonJob();
    if (guiDaemonJob == null) {
      return TextContainer.retrieveFromRequest().getText().get("grouperLoaderSchedulerStateNotScheduled");
    }
    
    return guiDaemonJob.getState();
  }
  
  /**
   * 
   * @return is SQL loader
   */
  public boolean isGrouperSqlLoader() {
    return GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().isHasAttrDefNameGrouperLoader();
  }
  
  /**
   * 
   * @return is recent memberships
   */
  public boolean isGrouperRecentMembershipsLoader() {
    return GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().isHasRecentMembershipsGrouperLoader();
  }
  
  /**
   * 
   * @return is JEXL script
   */
  public boolean isGrouperJexlScriptLoader() {
    return GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().isHasJexlScriptGrouperLoader();
  }
  
  /**
   * 
   * @return is LDAP loader
   */
  public boolean isGrouperLdapLoader() {
    return GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().isHasAttrDefNameGrouperLoaderLdap();
  }
  
  /**
   * get the ldap attribute assign for this group
   * @return attribute assign
   */
  private AttributeAssign getLdapAttributeAssign() {
    
    if (this.attributeAssign == null) {

      Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
      
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(LoaderLdapUtils.grouperLoaderLdapName(), false);
      
      this.attributeAssign = jobGroup.getAttributeDelegate().retrieveAssignment(AttributeDef.ACTION_DEFAULT, attributeDefName, false, true);
      
    }
    
    return this.attributeAssign;

  }

  /**
   * 
   * @param nameOfAttributeDefName
   * @return the value of the attribute
   */
  private String retrieveLdapAttributeValue(String nameOfAttributeDefName) {
    AttributeAssign theAttributeAssign = this.getLdapAttributeAssign();
    if (theAttributeAssign == null) { 
      return null;
    }
    return theAttributeAssign.getAttributeValueDelegate().retrieveValueString(nameOfAttributeDefName);
  }
  
  /**
   * LDAP_SIMPLE, LDAP_GROUP_LIST, LDAP_GROUPS_FROM_ATTRIBUTES
   * @return ldap loader type
   */
  public String getLdapLoaderType() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapTypeName());

  }
  
  /**
   * LDAP_SIMPLE, LDAP_GROUP_LIST, LDAP_GROUPS_FROM_ATTRIBUTES
   */
  private String editLoaderLdapType;

  
  /**
   * LDAP_SIMPLE, LDAP_GROUP_LIST, LDAP_GROUPS_FROM_ATTRIBUTES
   * @return the editLoaderLdapType
   */
  public String getEditLoaderLdapType() {
    return this.editLoaderLdapType;
  }

  
  /**
   * LDAP_SIMPLE, LDAP_GROUP_LIST, LDAP_GROUPS_FROM_ATTRIBUTES
   * @param editLoaderLdapType1 the editLoaderLdapType to set
   */
  public void setEditLoaderLdapType(String editLoaderLdapType1) {
    this.editLoaderLdapType = editLoaderLdapType1;
  }

  /**
   * BASE_FOLDER_NAME or LEVELS
   */
  private String editLoaderDisplayNameSyncType;
  
  
  public String getEditLoaderDisplayNameSyncType() {
    return editLoaderDisplayNameSyncType;
  }

  
  public void setEditLoaderDisplayNameSyncType(String editLoaderDisplayNameSyncType) {
    this.editLoaderDisplayNameSyncType = editLoaderDisplayNameSyncType;
  }

  /**
   * base folder name after which display names should be synced between source and grouper
   */
  private String editLoaderDisplayNameSyncBaseFolderName;
  
  /**
   * levels starting from the group after which display names should be synced between source and grouper
   */
  private String editLoaderDisplayNameSyncLevels;
  
  
  public String getEditLoaderDisplayNameSyncBaseFolderName() {
    return editLoaderDisplayNameSyncBaseFolderName;
  }

  
  public void setEditLoaderDisplayNameSyncBaseFolderName(
      String editLoaderDisplayNameSyncBaseFolderName) {
    this.editLoaderDisplayNameSyncBaseFolderName = editLoaderDisplayNameSyncBaseFolderName;
  }

  
  public String getEditLoaderDisplayNameSyncLevels() {
    return editLoaderDisplayNameSyncLevels;
  }

  
  public void setEditLoaderDisplayNameSyncLevels(String editLoaderDisplayNameSyncLevels) {
    this.editLoaderDisplayNameSyncLevels = editLoaderDisplayNameSyncLevels;
  }

  /**
   * 
   * @return ldap server id
   */
  public String getLdapServerId() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapServerIdName());

  }

  /**
   * 
   * @return ldap filter
   */
  public String getLdapLoaderFilter() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapFilterName());

  }

  /**
   * 
   * @return ldap subject attribute name
   */
  public String getLdapSubjectAttributeName() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName());

  }

  /**
   * 
   * @return ldap group attribute name
   */
  public String getLdapGroupAttributeName() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupAttributeName());

  }

  /**
   * 
   * @return ldap group attribute name
   */
  public String getLdapSearchDn() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapSearchDnName());

  }
    
  /**
   * 
   * @return ldap server id
   */
  public String getLdapServerIdUrl() {
    
    String ldapServerId = this.getLdapServerId();

    return convertLdapServerIdToUrl(ldapServerId);
  }
  
  /**
   * convert ldap server id to url
   * @param ldapServerId
   * @return the url
   */
  public static String convertLdapServerIdToUrl(String ldapServerId) {
    if (StringUtils.isBlank(ldapServerId)) {
      return null;
    }
    String ldapUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".url");
    return ldapUrl;
  }

  /**
   * @return ldap server id url or a message that says not found
   */
  public String getLdapServerIdUrlText() {
    
    String ldapUrl = this.getLdapServerIdUrl();
    return convertLdapUrlToDescription(ldapUrl);

  }

  /**
   * convert ldap url to description
   * @param ldapUrl
   * @return description
   */
  public static String convertLdapUrlToDescription(String ldapUrl) {
    if (!StringUtils.isBlank(ldapUrl)) {
      return ldapUrl;
    }
    
    return TextContainer.retrieveFromRequest().getText().get("grouperLoaderLdapServerIdNotFound");
  }

  

  
  /**
   * 
   * @return sql schedule interval
   */
  public String getSqlScheduleInterval() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS);
    
    return grouperLoaderType;

  }

  /**
   * 
   * @return sql schedule interval
   */
  public String getSqlScheduleIntervalHumanReadable() {

    return GrouperUiUtils.convertSecondsToString(this.getSqlScheduleIntervalSecondsTotal());

  }
  
  /**
   * 
   * @return sql schedule interval seconds
   */
  public int getSqlScheduleIntervalSecondsTotal() {
    
    String interval = this.getSqlScheduleInterval();
    
    if (StringUtils.isBlank(interval)) {
      return -1;
    }

    try {
      int intervalInt = GrouperUtil.intValue(interval);
      return intervalInt;
    } catch (Exception e) {
      LOG.error("Cant parse interval: '" + interval + "'", e);
      return -2;
    }
  }

  /**
   * 
   * @return the sql cron description
   */
  public String getSqlCronDescription() {
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderQuartzCron = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_QUARTZ_CRON);
    
    if (!StringUtils.isBlank(grouperLoaderQuartzCron)) {
      try {
        return CronExpressionDescriptor.getDescription(grouperLoaderQuartzCron);
      } catch (Exception e) {
        
        LOG.error("Cant parse cron string:" + grouperLoaderQuartzCron, e);
        
        return TextContainer.retrieveFromRequest().getText().get("grouperLoaderSqlCronDescriptionError");
      }
    }
    return "";
  }
  
  /**
   * ldap filter for the ldap loader
   */
  private String editLoaderLdapFilter;
  
  /**
   * ldap filter for the ldap loader
   * @return the editLoaderLdapFilter
   */
  public String getEditLoaderLdapFilter() {
    return this.editLoaderLdapFilter;
  }

  /**
   * ldap filter for the ldap loader
   * @param editLoaderLdapFilter1 the editLoaderLdapFilter to set
   */
  public void setEditLoaderLdapFilter(String editLoaderLdapFilter1) {
    this.editLoaderLdapFilter = editLoaderLdapFilter1;
  }

  /**
   * edit loader group attribute name
   */
  private String editLoaderLdapGroupAttributeName;
  
  /**
   * edit loader group attribute name
   * @return the editLoaderLdapGroupAttributeName
   */
  public String getEditLoaderLdapGroupAttributeName() {
    return this.editLoaderLdapGroupAttributeName;
  }
  
  /**
   * edit loader group attribute name
   * @param editLoaderLdapGroupAttributeName1 the editLoaderLdapGroupAttributeName to set
   */
  public void setEditLoaderLdapGroupAttributeName(String editLoaderLdapGroupAttributeName1) {
    this.editLoaderLdapGroupAttributeName = editLoaderLdapGroupAttributeName1;
  }
  
  /**
   * edit loader group description
   */
  private String editLoaderLdapGroupDescriptionExpression;

  /**
   * edit loader group description
   * @return the editLoaderLdapGroupDescriptionExpression
   */
  public String getEditLoaderLdapGroupDescriptionExpression() {
    return this.editLoaderLdapGroupDescriptionExpression;
  }

  
  /**
   * edit loader group description
   * @param editLoaderLdapGroupDescriptionExpression1 the editLoaderLdapGroupDescriptionExpression to set
   */
  public void setEditLoaderLdapGroupDescriptionExpression(
      String editLoaderLdapGroupDescriptionExpression1) {
    this.editLoaderLdapGroupDescriptionExpression = editLoaderLdapGroupDescriptionExpression1;
  }

  /**
   * edit loader group display name
   */
  private String editLoaderLdapGroupDisplayNameExpression;
  
  /**
   * edit loader group display name
   * @return the editLoaderLdapGroupDisplayNameExpression
   */
  public String getEditLoaderLdapGroupDisplayNameExpression() {
    return this.editLoaderLdapGroupDisplayNameExpression;
  }

  /**
   * @param editLoaderLdapGroupDisplayNameExpression1 the editLoaderLdapGroupDisplayNameExpression to set
   */
  public void setEditLoaderLdapGroupDisplayNameExpression(
      String editLoaderLdapGroupDisplayNameExpression1) {
    this.editLoaderLdapGroupDisplayNameExpression = editLoaderLdapGroupDisplayNameExpression1;
  }

  /**
   * edit loader group name expression
   */
  private String editLoaderLdapGroupNameExpression;
  
  /**
   * edit loader group name expression
   * @return the editLoaderLdapGroupNameExpression
   */
  public String getEditLoaderLdapGroupNameExpression() {
    return this.editLoaderLdapGroupNameExpression;
  }
  
  /**
   * edit loader group name expression
   * @param editLoaderLdapGroupNameExpression1 the editLoaderLdapGroupNameExpression to set
   */
  public void setEditLoaderLdapGroupNameExpression(String editLoaderLdapGroupNameExpression1) {
    this.editLoaderLdapGroupNameExpression = editLoaderLdapGroupNameExpression1;
  }

  /**
   * edit loader search dn
   */
  private String editLoaderLdapSearchDn;
  
  /**
   * edit loader search dn
   * @return the editLoaderLdapSearchDn
   */
  public String getEditLoaderLdapSearchDn() {
    return this.editLoaderLdapSearchDn;
  }
  
  /**
   * edit loader search dn
   * @param editLoaderLdapSearchDn1 the editLoaderLdapSearchDn to set
   */
  public void setEditLoaderLdapSearchDn(String editLoaderLdapSearchDn1) {
    this.editLoaderLdapSearchDn = editLoaderLdapSearchDn1;
  }

  /**
   * edit loader ldap search scope
   */
  private String editLoaderLdapSearchScope;
  
  /**
   * edit loader ldap search scope
   * @return the editLoaderLdapSearchScope
   */
  public String getEditLoaderLdapSearchScope() {
    return this.editLoaderLdapSearchScope;
  }

  
  /**
   * edit loader ldap search scope
   * @param editLoaderLdapSearchScope1 the editLoaderLdapSearchScope to set
   */
  public void setEditLoaderLdapSearchScope(String editLoaderLdapSearchScope1) {
    this.editLoaderLdapSearchScope = editLoaderLdapSearchScope1;
  }

  /**
   * edit loader ldap source id
   */
  private String editLoaderLdapSourceId;
  
  /**
   * edit loader ldap source id
   * @return the editLoaderLdapSourceId
   */
  public String getEditLoaderLdapSourceId() {
    return this.editLoaderLdapSourceId;
  }
  
  /**
   * edit loader ldap source id
   * @param editLoaderLdapSourceId1 the editLoaderLdapSourceId to set
   */
  public void setEditLoaderLdapSourceId(String editLoaderLdapSourceId1) {
    this.editLoaderLdapSourceId = editLoaderLdapSourceId1;
  }

  /**
   * edit loader subject attribute name
   */
  private String editLoaderLdapSubjectAttributeName;
  
  /**
   * edit loader subject attribute name
   * @return the editLoaderLdapSubjectAttributeName
   */
  public String getEditLoaderLdapSubjectAttributeName() {
    return this.editLoaderLdapSubjectAttributeName;
  }

  /**
   * edit loader subject attribute name
   * @param editLoaderLdapSubjectAttributeName1 the editLoaderLdapSubjectAttributeName to set
   */
  public void setEditLoaderLdapSubjectAttributeName(String editLoaderLdapSubjectAttributeName1) {
    this.editLoaderLdapSubjectAttributeName = editLoaderLdapSubjectAttributeName1;
  }

  /**
   * edit loader subject expression
   */
  private String editLoaderLdapSubjectExpression;
    
  /**
   * edit loader subject expression
   * @return the editLoaderLdapSubjectExpression
   */
  public String getEditLoaderLdapSubjectExpression() {
    return this.editLoaderLdapSubjectExpression;
  }
  
  /**
   * edit loader subject expression
   * @param editLoaderLdapSubjectExpression1 the editLoaderLdapSubjectExpression to set
   */
  public void setEditLoaderLdapSubjectExpression(String editLoaderLdapSubjectExpression1) {
    this.editLoaderLdapSubjectExpression = editLoaderLdapSubjectExpression1;
  }

  /**
   * edit loader subject lookup type
   */
  private String editLoaderLdapSubjectLookupType;
  
  /**
   * edit loader subject lookup type
   * @return the editLoaderLdapSubjectLookupType
   */
  public String getEditLoaderLdapSubjectLookupType() {
    return this.editLoaderLdapSubjectLookupType;
  }
  
  /**
   * edit loader subject lookup type
   * @param editLoaderLdapSubjectLookupType1 the editLoaderLdapSubjectLookupType to set
   */
  public void setEditLoaderLdapSubjectLookupType(String editLoaderLdapSubjectLookupType1) {
    this.editLoaderLdapSubjectLookupType = editLoaderLdapSubjectLookupType1;
  }

  /**
   * edit loader ldap admins
   */
  private String editLoaderLdapAdmins;
  
  /**
   * edit loader ldap admins
   * @return the editLoaderLdapAdmins
   */
  public String getEditLoaderLdapAdmins() {
    return this.editLoaderLdapAdmins;
  }
  
  /**
   * edit loader ldap admins
   * @param editLoaderLdapAdmins1 the editLoaderLdapAdmins to set
   */
  public void setEditLoaderLdapAdmins(String editLoaderLdapAdmins1) {
    this.editLoaderLdapAdmins = editLoaderLdapAdmins1;
  }

  /**
   * edit loader ldap attr readers
   */
  private String editLoaderLdapAttrReaders;
  
  /**
   * edit loader ldap attr readers
   * @return the editLoaderLdapAttrReaders
   */
  public String getEditLoaderLdapAttrReaders() {
    return this.editLoaderLdapAttrReaders;
  }
  
  /**
   * edit loader ldap attr readers
   * @param editLoaderLdapAttrReaders1 the editLoaderLdapAttrReaders to set
   */
  public void setEditLoaderLdapAttrReaders(String editLoaderLdapAttrReaders1) {
    this.editLoaderLdapAttrReaders = editLoaderLdapAttrReaders1;
  }

  /**
   * edit loader ldap attr updaters
   */
  private String editLoaderLdapAttrUpdaters;
  
  /**
   * edit loader ldap attr updaters
   * @return the editLoaderLdapUpdaters
   */
  public String getEditLoaderLdapUpdaters() {
    return this.editLoaderLdapUpdaters;
  }

  
  /**
   * edit loader ldap attr updaters
   * @param editLoaderLdapUpdaters1 the editLoaderLdapUpdaters to set
   */
  public void setEditLoaderLdapUpdaters(String editLoaderLdapUpdaters1) {
    this.editLoaderLdapUpdaters = editLoaderLdapUpdaters1;
  }

  /**
   * edit loader ldap optins
   */
  private String editLoaderLdapOptins;
  
  /**
   * edit loader ldap optins
   * @return the editLoaderLdapOptins
   */
  public String getEditLoaderLdapOptins() {
    return this.editLoaderLdapOptins;
  }
  
  /**
   * @param editLoaderLdapOptins1 the editLoaderLdapOptins to set
   */
  public void setEditLoaderLdapOptins(String editLoaderLdapOptins1) {
    this.editLoaderLdapOptins = editLoaderLdapOptins1;
  }

  /**
   * edit loader ldap optouts
   */
  private String editLoaderLdapOptouts;
  
  /**
   * edit loader ldap optouts
   * @return the editLoaderLdapOptouts
   */
  public String getEditLoaderLdapOptouts() {
    return this.editLoaderLdapOptouts;
  }
  
  /**
   * edit loader ldap optouts
   * @param editLoaderLdapOptouts1 the editLoaderLdapOptouts to set
   */
  public void setEditLoaderLdapOptouts(String editLoaderLdapOptouts1) {
    this.editLoaderLdapOptouts = editLoaderLdapOptouts1;
  }

  /**
   * edit loader ldap readers
   */
  private String editLoaderLdapReaders;
  
  /**
   * edit loader ldap readers
   * @return the editLoaderLdapReaders
   */
  public String getEditLoaderLdapReaders() {
    return this.editLoaderLdapReaders;
  }
  
  /**
   * edit loader ldap readers
   * @param editLoaderLdapReaders1 the editLoaderLdapReaders to set
   */
  public void setEditLoaderLdapReaders(String editLoaderLdapReaders1) {
    this.editLoaderLdapReaders = editLoaderLdapReaders1;
  }

  /**
   * edit loader ldap updaters
   */
  private String editLoaderLdapUpdaters;
  
  /**
   * edit loader ldap updaters
   * @return the editLoaderLdapAttrUpdaters
   */
  public String getEditLoaderLdapAttrUpdaters() {
    return this.editLoaderLdapAttrUpdaters;
  }
  
  /**
   * edit loader ldap updaters
   * @param editLoaderLdapAttrUpdaters1 the editLoaderLdapAttrUpdaters to set
   */
  public void setEditLoaderLdapAttrUpdaters(String editLoaderLdapAttrUpdaters1) {
    this.editLoaderLdapAttrUpdaters = editLoaderLdapAttrUpdaters1;
  }

  /**
   * edit loader ldap viewers
   */
  private String editLoaderLdapViewers;
  
  /**
   * edit loader ldap viewers
   * @return the editLoaderLdapViewers
   */
  public String getEditLoaderLdapViewers() {
    return this.editLoaderLdapViewers;
  }
  
  /**
   * edit loader ldap viewers
   * @param editLoaderLdapViewers1 the editLoaderLdapViewers to set
   */
  public void setEditLoaderLdapViewers(String editLoaderLdapViewers1) {
    this.editLoaderLdapViewers = editLoaderLdapViewers1;
  }

  /**
   * 
   * @return the sql cron description
   */
  public String getLdapCronDescription() {
    String grouperLoaderQuartzCron = this.getLdapCron();
    
    if (!StringUtils.isBlank(grouperLoaderQuartzCron)) {
      try {
        return CronExpressionDescriptor.getDescription(grouperLoaderQuartzCron);
      } catch (Exception e) {
        
        LOG.error("Cant parse cron string:" + grouperLoaderQuartzCron, e);
        
        return TextContainer.retrieveFromRequest().getText().get("grouperLoaderSqlCronDescriptionError");
      }
    }
    return "";
  }
  
  /**
   * 
   * @return source ID
   */
  public String getLdapSourceId() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapSourceIdName());

  }
  
  /**
   * 
   * @return subject lookup type
   */
  public String getLdapSubjectLookupType() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName());

  }
  
  /**
   * 
   * @return ldap search scope
   */
  public String getLdapSearchScope() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapSearchScopeName());

  }

  /**
   * for list of groups this is the failsafe setting for max overall percent of memberships being removed
   */
  private String editLoaderMaxOverallPercentMembershipsRemove;

  /**
   * for list of groups this is the failsafe setting for max overall percent of memberships being removed
   * @return
   */
  public String getEditLoaderMaxOverallPercentMembershipsRemove() {
    return editLoaderMaxOverallPercentMembershipsRemove;
  }

  /**
   * for list of groups this is the failsafe setting for max overall percent of memberships being removed
   * @param editLoaderMaxOverallPercentMembershipsRemove
   */
  public void setEditLoaderMaxOverallPercentMembershipsRemove(
      String editLoaderMaxOverallPercentMembershipsRemove) {
    this.editLoaderMaxOverallPercentMembershipsRemove = editLoaderMaxOverallPercentMembershipsRemove;
  }

  /**
   * for list of groups this is the failsafe setting for max overall percent of groups being removed
   */
  private String editLoaderMaxOverallPercentGroupsRemove;

  /**
   * for list of groups this is the failsafe setting for max overall percent of groups being removed
   * @return
   */
  public String getEditLoaderMaxOverallPercentGroupsRemove() {
    return editLoaderMaxOverallPercentGroupsRemove;
  }

  /**
   * for list of groups this is the failsafe setting for max overall percent of groups being removed
   * @param editLoaderMaxOverallPercentGroupsRemove
   */
  public void setEditLoaderMaxOverallPercentGroupsRemove(
      String editLoaderMaxOverallPercentGroupsRemove) {
    this.editLoaderMaxOverallPercentGroupsRemove = editLoaderMaxOverallPercentGroupsRemove;
  }

  /**
   * T or F if using failsafe.  If blank use the global defaults
   */
  private Boolean editLoaderFailsafeUse;
  
  /**
   * T or F if using failsafe.  If blank use the global defaults
   * @return true or false
   */
  public Boolean getEditLoaderFailsafeUse() {
    return editLoaderFailsafeUse;
  }

  /**
   * T or F if using failsafe.  If blank use the global defaults
   * @param editLoaderFailsafeUse
   */
  public void setEditLoaderFailsafeUse(Boolean editLoaderFailsafeUse) {
    this.editLoaderFailsafeUse = editLoaderFailsafeUse;
  }

  /**
   * true, false, default
   * @return
   */
  public String getEditLoaderFailsafeUseOrDefault() {
    if (this.editLoaderFailsafeUse == null) {
      return "default";
    }
    return this.editLoaderFailsafeUse ? "true" : "false";
  }
  

  /**
   * integer from 0 to 100 which specifies the maximum percent of a group which can be removed in a loader run.
   * If not specified will use the global default grouper-loader.properties config setting:
   * loader.failsafe.maxPercentRemove = 30
   */
  private String editLoaderMaxGroupPercentRemove;
  
  /**
   * integer from 0 to 100 which specifies the maximum percent of a group which can be removed in a loader run.
   * If not specified will use the global default grouper-loader.properties config setting:
   * loader.failsafe.maxPercentRemove = 30
   * @return
   */
  public String getEditLoaderMaxGroupPercentRemove() {
    return editLoaderMaxGroupPercentRemove;
  }

  /**
   * integer from 0 to 100 which specifies the maximum percent of a group which can be removed in a loader run.
   * If not specified will use the global default grouper-loader.properties config setting:
   * loader.failsafe.maxOverallPercentRemove = 30
   * @param editLoaderMaxGroupPercentRemove
   */
  public void setEditLoaderMaxGroupPercentRemove(String editLoaderMaxGroupPercentRemove) {
    this.editLoaderMaxGroupPercentRemove = editLoaderMaxGroupPercentRemove;
  }

  /**
   * minimum number of members for the group to be tracked by failsafe
   * defaults to grouper-loader.base.properties: loader.failsafe.defaultGroupLevel.minGroupSize
   */
  private String editLoaderMinGroupSize;

  
  
  /**
   * minimum number of members for the group to be tracked by failsafe
   * defaults to grouper-loader.base.properties: loader.failsafe.defaultGroupLevel.minGroupSize
   * @return
   */
  public String getEditLoaderMinGroupSize() {
    return editLoaderMinGroupSize;
  }

  /**
   * minimum number of members for the group to be tracked by failsafe
   * defaults to grouper-loader.base.properties: loader.failsafe.defaultGroupLevel.minGroupSize
   * @param editLoaderMinGroupSize
   */
  public void setEditLoaderMinGroupSize(String editLoaderMinGroupSize) {
    this.editLoaderMinGroupSize = editLoaderMinGroupSize;
  }

  /**
   * The minimum number of managed groups for this loader job, a failsafe alert will trigger if the number
   * of managed groups is smaller than this amount
   */
  private String editLoaderMinManagedGroups;

  
  
  /**
   * The minimum number of managed groups for this loader job, a failsafe alert will trigger if the number
   * of managed groups is smaller than this amount
   * @return
   */
  public String getEditLoaderMinManagedGroups() {
    return editLoaderMinManagedGroups;
  }

  /**
   * The minimum number of managed groups for this loader job, a failsafe alert will trigger if the number
   * of managed groups is smaller than this amount
   * @param editLoaderMinManagedGroups
   */
  public void setEditLoaderMinManagedGroups(String editLoaderMinManagedGroups) {
    this.editLoaderMinManagedGroups = editLoaderMinManagedGroups;
  }

  /**
   * The minimum group number of members for this group, a failsafe alert will trigger if the group is smaller than this amount
   */
  private String editLoaderMinGroupNumberOfMembers;

  
  
  /**
   * The minimum group number of members for this group, a failsafe alert will trigger if the group is smaller than this amount
   * @return min
   */
  public String getEditLoaderMinGroupNumberOfMembers() {
    return editLoaderMinGroupNumberOfMembers;
  }

  /**
   * The minimum group number of members for this group, a failsafe alert will trigger if the group is smaller than this amount
   * @param editLoaderMinGroupNumberOfMembers
   */
  public void setEditLoaderMinGroupNumberOfMembers(String editLoaderMinGroupNumberOfMembers) {
    this.editLoaderMinGroupNumberOfMembers = editLoaderMinGroupNumberOfMembers;
  }

  /**
   * The minimum overall number of members for this job across all managed groups, 
   * a failsafe alert will trigger if the job's overall membership count is smaller than this amount
   */
  private String editLoaderMinOverallNumberOfMembers;

  
  
  /**
   * The minimum overall number of members for this job across all managed groups, 
   * a failsafe alert will trigger if the job's overall membership count is smaller than this amount
   * @return
   */
  public String getEditLoaderMinOverallNumberOfMembers() {
    return editLoaderMinOverallNumberOfMembers;
  }

  /**
   * The minimum overall number of members for this job across all managed groups, 
   * a failsafe alert will trigger if the job's overall membership count is smaller than this amount
   * @param editLoaderMinOverallNumberOfMembers
   */
  public void setEditLoaderMinOverallNumberOfMembers(
      String editLoaderMinOverallNumberOfMembers) {
    this.editLoaderMinOverallNumberOfMembers = editLoaderMinOverallNumberOfMembers;
  }

  /**
   * If an email should be sent out when a failsafe alert happens.
   * The email will be sent to the list or group configured in grouper-loader.properties:
   * loader.failsafe.sendEmailToAddresses, or loader.failsafe.sendEmailToGroup 
   */
  private Boolean editLoaderFailsafeSendEmail;

  
  /**
   * If an email should be sent out when a failsafe alert happens.
   * The email will be sent to the list or group configured in grouper-loader.properties:
   * loader.failsafe.sendEmailToAddresses, or loader.failsafe.sendEmailToGroup 
   * @return
   */
  public Boolean getEditLoaderFailsafeSendEmail() {
    return editLoaderFailsafeSendEmail;
  }

  /**
   * true false or default
   * @return
   */
  public String getEditLoaderFailsafeSendEmailOrDefault() {

    if (this.editLoaderFailsafeSendEmail == null) {
      return "default";
    }
    return this.editLoaderFailsafeSendEmail ? "true" : "false";

  }

  /**
   * If an email should be sent out when a failsafe alert happens.
   * The email will be sent to the list or group configured in grouper-loader.properties:
   * loader.failsafe.sendEmailToAddresses, or loader.failsafe.sendEmailToGroup 
   * @param editLoaderFailsafeSendEmail
   */
  public void setEditLoaderFailsafeSendEmail(Boolean editLoaderFailsafeSendEmail) {
    this.editLoaderFailsafeSendEmail = editLoaderFailsafeSendEmail;
  }

  /**
   * if the user explicitly selected the failsafe to customize or not
   */
  private Boolean customizeFailsafeSelected;
  
  /**
   * if the user explicitly selected the failsafe to customize or not
   * @return true or false
   */
  public Boolean getCustomizeFailsafeSelected() {
    return this.customizeFailsafeSelected;
  }

  /**
   * if the user explicitly selected the failsafe to customize or not
   * @param customizeFailsafeSelected1
   */
  public void setCustomizeFailsafeSelected(Boolean customizeFailsafeSelected1) {
    this.customizeFailsafeSelected = customizeFailsafeSelected1;
  }

  /**
   * if any 
   * @return
   */
  public boolean isCustomizeFailsafeTrue() {
    if (this.customizeFailsafeSelected != null) {
      return this.customizeFailsafeSelected;
    }
    
    return false;
    
  }
  
  /**
   * not a normal loader group
   * @return if loader group
   */
  public boolean isLoaderGroup() {

    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();

    if (guiGroup.isHasAttrDefNameGrouperLoader() || guiGroup.isHasAttrDefNameGrouperLoaderLdap() || guiGroup.isHasRecentMembershipsGrouperLoader()
        || guiGroup.isHasJexlScriptGrouperLoader()) {
      return true;
    }
    
    return false;
    
  }

  /**
   * if edit screen should show the ldap filter
   */
  private boolean editLoaderShowLdapFilter;
  
  /**
   * if edit screen should show the ldap filter
   * @return the editLoaderShowLdapFilter
   */
  public boolean isEditLoaderShowLdapFilter() {
    return this.editLoaderShowLdapFilter;
  }
  
  /**
   * if edit screen should show the ldap filter
   * @param editLoaderShowLdapFilter1 the editLoaderShowLdapFilter to set
   */
  public void setEditLoaderShowLdapFilter(boolean editLoaderShowLdapFilter1) {
    this.editLoaderShowLdapFilter = editLoaderShowLdapFilter1;
  }

  /**
   * if should show the sql query
   */
  private boolean editLoaderShowSqlQuery;
  
  /**
   * if should show the sql query
   * @return the editLoaderShowSqlQuery
   */
  public boolean isEditLoaderShowSqlQuery() {
    return this.editLoaderShowSqlQuery;
  }

  
  /**
   * if should show the sql query
   * @param editLoaderShowSqlQuery1 the editLoaderShowSqlQuery to set
   */
  public void setEditLoaderShowSqlQuery(boolean editLoaderShowSqlQuery1) {
    this.editLoaderShowSqlQuery = editLoaderShowSqlQuery1;
  }

  /**
   * group query for list of groups
   */
  private String editLoaderSqlGroupQuery;

  /**
   * group query for list of groups
   * @return the editLoaderSqlGroupQuery
   */
  public String getEditLoaderSqlGroupQuery() {
    return this.editLoaderSqlGroupQuery;
  }

  /**
   * @param editLoaderSqlGroupQuery1 the editLoaderSqlGroupQuery to set
   */
  public void setEditLoaderSqlGroupQuery(String editLoaderSqlGroupQuery1) {
    this.editLoaderSqlGroupQuery = editLoaderSqlGroupQuery1;
  }

  /**
   * group types to add to list of groups
   */
  private String editLoaderGroupTypes;
  
  /**
   * group types to add to list of groups
   * @return the editLoaderGroupTypes
   */
  public String getEditLoaderGroupTypes() {
    return this.editLoaderGroupTypes;
  }
  
  /**
   * group types to add to list of groups
   * @param editLoaderGroupTypes1 the editLoaderGroupTypes to set
   */
  public void setEditLoaderGroupTypes(String editLoaderGroupTypes1) {
    this.editLoaderGroupTypes = editLoaderGroupTypes1;
  }

  /**
   * groups like string in database to remove groups not managed by loader anymore
   */
  private String editLoaderGroupsLike;
  
  /**
   * groups like string in database to remove groups not managed by loader anymore
   * @return the editLoaderSqlGroupsLike
   */
  public String getEditLoaderGroupsLike() {
    return this.editLoaderGroupsLike;
  }
  
  /**
   * groups like string in database to remove groups not managed by loader anymore
   * @param editLoaderSqlGroupsLike1 the editLoaderSqlGroupsLike to set
   */
  public void setEditLoaderGroupsLike(String editLoaderSqlGroupsLike1) {
    this.editLoaderGroupsLike = editLoaderSqlGroupsLike1;
  }

  /**
   * show if grouper admin or edit loader group
   * @return true if should show the edit loader menu item
   */
  public boolean isCanEditLoader() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanView()) {
      return false;
    }
    if (!StringUtils.isBlank(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.loader.edit.if.in.group"))) {
      String error = GrouperUiFilter.requireUiGroup("uiV2.loader.edit.if.in.group", loggedInSubject, false);
      //null error means allow
      return error == null;
    }
    
    return false;
  }
  
  /**
   * if on edit screen this is a loader group
   */
  private boolean editLoaderIsLoader;
  
  /**
   * 
   * @return if on the loader screen this is a loader job
   */
  public boolean isEditLoaderIsLoader() {
    return this.editLoaderIsLoader;
  }
  
  /**
   * @param theEditLoaderIsLoader
   */
  public void setEditLoaderIsLoader(boolean theEditLoaderIsLoader) {
    this.editLoaderIsLoader = theEditLoaderIsLoader;
  }
  
  /**
   * 
   * @return the text of the selected option
   */
  public String getEditLoaderSqlDatabaseNameText() {
    if (StringUtils.isBlank(this.editLoaderSqlDatabaseName)) {
      return null;
    }
    if (StringUtils.equals("grouper", this.editLoaderSqlDatabaseName)) {
      
      return GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.connection.url");
      
    }
    
    String databaseUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("db." + this.editLoaderSqlDatabaseName + ".url");
    return databaseUrl;
  }

  /**
   * 
   * @return the text of the selected option
   */
  public String getEditLoaderLdapServerIdUrlText() {
    if (StringUtils.isBlank(this.editLoaderLdapServerId)) {
      return null;
    }
    String ldapServerIdUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + this.editLoaderLdapServerId + ".url");
    return ldapServerIdUrl;
  }

  /**
   * ldap server id that the user selected
   */
  private String editLoaderLdapServerId;

  /**
   * ldap server id that the user selected
   * @return the editLoaderLdapServerId
   */
  public String getEditLoaderLdapServerId() {
    return this.editLoaderLdapServerId;
  }

  /**
   * ldap server id that the user selected
   * @param editLoaderLdapServerId1 the editLoaderLdapServerId to set
   */
  public void setEditLoaderLdapServerId(String editLoaderLdapServerId1) {
    this.editLoaderLdapServerId = editLoaderLdapServerId1;
  }

  /**
   * sql database name that the user selected
   */
  private String editLoaderSqlDatabaseName;

  /**
   * CRON (recommended) or START_TO_START_INTERVAL
   */
  private String editLoaderScheduleType;
  
  /**
   * CRON (recommended) or START_TO_START_INTERVAL
   * @return the editLoaderScheduleType
   */
  public String getEditLoaderScheduleType() {
    return this.editLoaderScheduleType;
  }
  
  /**
   * "and" groups that members need to be in to be in the loaded group
   */
  private String editLoaderAndGroups;

  /**
   * "and" groups that members need to be in to be in the loaded group
   * @return the editLoaderAndGroups
   */
  public String getEditLoaderAndGroups() {
    return this.editLoaderAndGroups;
  }
  
  /**
   * "and" groups that members need to be in to be in the loaded group
   * @param editLoaderAndGroups1 the editLoaderAndGroups to set
   */
  public void setEditLoaderAndGroups(String editLoaderAndGroups1) {
    this.editLoaderAndGroups = editLoaderAndGroups1;
  }

  /**
   * 
   * @return list of gui groups
   */
  public List<GuiGroup> getEditLoaderAndGuiGroups() {

    final List<String> andGroupsStringList = getEditLoaderAndGroupsStringList();
    
    final List<GuiGroup> guiGroups = new ArrayList<GuiGroup>();
    
    if (GrouperUtil.length(andGroupsStringList) > 0) {

      GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          for (String andGroupString : andGroupsStringList) {
            
            Group group = GroupFinder.findByUuid(grouperSession, andGroupString, false);
            group = group != null ? group : GroupFinder.findByName(grouperSession, andGroupString, false);
            guiGroups.add(new GuiGroup(group));
            
          }
          
          return null;
        }
      });
    }    
    return guiGroups;
  }

  /**
   * convert and groups to string, edit screen
   * @return the list of strings
   */
  private List<String> getEditLoaderAndGroupsStringList() {
    String andGroupsString = this.getEditLoaderAndGroups();
    
    if (StringUtils.isBlank(andGroupsString)) {
      return null;
    }
    
    final List<String> andGroupsStringList = GrouperUtil.splitTrimToList(andGroupsString, ",");
    return andGroupsStringList;
  }


  
  /**
   * CRON (recommended) or START_TO_START_INTERVAL
   * @param editLoaderScheduleType1 the editLoaderScheduleType to set
   */
  public void setEditLoaderScheduleType(String editLoaderScheduleType1) {
    this.editLoaderScheduleType = editLoaderScheduleType1;
  }

  /**
   * sql schedule interval on edit screen
   * @return the editLoaderSqlScheduleInterval
   */
  public int getEditLoaderScheduleIntervalSecondsTotal() {
    
    String interval = this.getEditLoaderScheduleInterval();
    
    if (StringUtils.isBlank(interval)) {
      return -1;
    }

    try {
      int intervalInt = GrouperUtil.intValue(interval);
      return intervalInt;
    } catch (Exception e) {
      LOG.error("Cant parse interval: '" + interval + "'", e);
      return -2;
    }
  }
  
  /**
   * 
   * @return sql schedule interval on edit screen
   */
  public String getEditLoaderScheduleIntervalHumanReadable() {

    return GrouperUiUtils.convertSecondsToString(this.getEditLoaderScheduleIntervalSecondsTotal());

  }
  
  /**
   * priority of this job defaults to 5
   */
  private String editLoaderPriority;
  
  /**
   * priority of this job defaults to 5
   * @return the editLoaderPriority
   */
  public String getEditLoaderPriority() {
    return this.editLoaderPriority;
  }
  
  /**
   * priority of this job defaults to 5
   * @param editLoaderPriority1 the editLoaderPriority to set
   */
  public void setEditLoaderPriority(String editLoaderPriority1) {
    this.editLoaderPriority = editLoaderPriority1;
  }

  /**
   * priority of this job defaults to 5
   * @return the editLoaderPriority
   */
  public int getEditLoaderPriorityInt() {
    String priority = this.getEditLoaderPriority();
    
    if (!StringUtils.isBlank(priority)) {
      
      try {
        
        return GrouperUtil.intValue(priority);
        
      } catch (Exception e) {
        LOG.error("Cant parse priority: '" + priority + "'", e);
        return -200;
      }
      
    }
    
    return 5;

  }
  
  
  
  /**
   * sql schedule interval on edit screen
   */
  private String editLoaderScheduleInterval;
    
  /**
   * sql schedule interval on edit screen
   * @return the editLoaderSqlScheduleInterval
   */
  public String getEditLoaderScheduleInterval() {
    return this.editLoaderScheduleInterval;
  }
  
  /**
   * sql schedule interval on edit screen
   * @param editLoaderSqlScheduleInterval1 the editLoaderSqlScheduleInterval to set
   */
  public void setEditLoaderScheduleInterval(String editLoaderSqlScheduleInterval1) {
    this.editLoaderScheduleInterval = editLoaderSqlScheduleInterval1;
  }

  /**
   * sql cron on edit screen
   */
  private String editLoaderCron;
  
  /**
   * sql cron on edit screen
   * @return the editLoaderSqlCron
   */
  public String getEditLoaderCron() {
    return this.editLoaderCron;
  }
  
  /**
   * 
   * @return the sql cron description on edit screen
   */
  public String getEditLoaderCronDescription() {
    
    if (!StringUtils.isBlank(this.editLoaderCron)) {
      try {
        return CronExpressionDescriptor.getDescription(this.editLoaderCron);
      } catch (Exception e) {
        
        LOG.error("Cant parse cron string:" + this.editLoaderCron, e);
        
        return TextContainer.retrieveFromRequest().getText().get("grouperLoaderSqlCronDescriptionError");
      }
    }
    return "";
  }
  

  /**
   * sql cron on edit screen
   * @param editLoaderSqlCron1 the editLoaderSqlCron to set
   */
  public void setEditLoaderCron(String editLoaderSqlCron1) {
    this.editLoaderCron = editLoaderSqlCron1;
  }

  /**
   * if should show CRON (recommended) or START_TO_START_INTERVAL
   */
  private boolean editLoaderShowFields;
  
  /**
   * if should show CRON (recommended) or START_TO_START_INTERVAL
   * @return the editLoaderShowScheduleType
   */
  public boolean isEditLoaderShowFields() {
    return this.editLoaderShowFields;
  }
  
  /**
   * if should show CRON (recommended) or START_TO_START_INTERVAL
   * @param editLoaderShowScheduleType1 the editLoaderShowScheduleType to set
   */
  public void setEditLoaderShowFields(boolean editLoaderShowScheduleType1) {
    this.editLoaderShowFields = editLoaderShowScheduleType1;
  }

  /**
   * sql database query
   */
  private String editLoaderSqlQuery;
  
  /**
   * sql database query
   * @return the editLoaderSqlQuery
   */
  public String getEditLoaderSqlQuery() {
    return this.editLoaderSqlQuery;
  }
  
  /**
   * sql database query
   * @param editLoaderSqlQuery1 the editLoaderSqlQuery to set
   */
  public void setEditLoaderSqlQuery(String editLoaderSqlQuery1) {
    this.editLoaderSqlQuery = editLoaderSqlQuery1;
  }

  /**
   * sql database name that the user selected
   * @return the editLoaderSqlDatabaseName
   */
  public String getEditLoaderSqlDatabaseName() {
    return this.editLoaderSqlDatabaseName;
  }

  /**
   * sql database name that the user selected
   * @param editLoaderSqlDatabaseName1 the editLoaderSqlDatabaseName to set
   */
  public void setEditLoaderSqlDatabaseName(String editLoaderSqlDatabaseName1) {
    this.editLoaderSqlDatabaseName = editLoaderSqlDatabaseName1;
  }

  /**
   * if loder type should be shown
   */
  private boolean editLoaderShowLoaderType;
  
  /**
   * if loder type should be shown
   * @return the editLoaderShowLoaderType
   */
  public boolean isEditLoaderShowLoaderType() {
    return this.editLoaderShowLoaderType;
  }

  /**
   * if loder type should be shown
   * @param editLoaderShowLoaderType1 the editLoaderShowLoaderType to set
   */
  public void setEditLoaderShowLoaderType(boolean editLoaderShowLoaderType1) {
    this.editLoaderShowLoaderType = editLoaderShowLoaderType1;
  }

  /**
   * if the loader edit screen should show the ldap server id
   */
  private boolean editLoaderShowLdapServerId;

  /**
   * if the loader edit screen should show the ldap server id
   * @return the editLoaderShowLdapServerId
   */
  public boolean isEditLoaderShowLdapServerId() {
    return this.editLoaderShowLdapServerId;
  }

  /**
   * if the loader edit screen should show the ldap server id
   * @param editLoaderShowLdapServerId1 the editLoaderShowLdapServerId to set
   */
  public void setEditLoaderShowLdapServerId(boolean editLoaderShowLdapServerId1) {
    this.editLoaderShowLdapServerId = editLoaderShowLdapServerId1;
  }

  /**
   * if the loader should show the sql database name
   */
  private boolean editLoaderShowSqlDatabaseName;

  /**
   * if the loader should show the sql database name
   * @return the editLoaderShowSqlDatabaseName
   */
  public boolean isEditLoaderShowSqlDatabaseName() {
    return this.editLoaderShowSqlDatabaseName;
  }
  
  /**
   * if the loader should show the sql database name
   * @param editLoaderShowSqlDatabaseName1 the editLoaderShowSqlDatabaseName to set
   */
  public void setEditLoaderShowSqlDatabaseName(boolean editLoaderShowSqlDatabaseName1) {
    this.editLoaderShowSqlDatabaseName = editLoaderShowSqlDatabaseName1;
  }

  /**
   * if the loader show jexl script should be seen
   */
  private boolean editLoaderShowJexlScript;

  
  /**
   * if the loader show jexl script should be seen
   * @return scho jexl script
   */
  public boolean isEditLoaderShowJexlScript() {
    return this.editLoaderShowJexlScript;
  }

  /**
   * if the loader show jexl script should be seen
   * @param editLoaderShowJexlScript1
   */
  public void setEditLoaderShowJexlScript(boolean editLoaderShowJexlScript1) {
    this.editLoaderShowJexlScript = editLoaderShowJexlScript1;
  }

  /**
   * if the loader show recent memberships should be seen
   */
  private boolean editLoaderShowRecentMemberships;

  /**
   * if the loader show recent memberships should be seen
   * @return the editLoaderShowSqlLoaderType
   */
  public boolean isEditLoaderShowRecentMemberships() {
    return this.editLoaderShowRecentMemberships;
  }

  /**
   * if the loader show recent memberships should be seen
   * @param editLoaderShowSqlLoaderType1 the editLoaderShowSqlLoaderType to set
   */
  public void setEditLoaderShowRecentMemberships(boolean editLoaderShowRecentMemberships1) {
    this.editLoaderShowRecentMemberships = editLoaderShowRecentMemberships1;
  }

  /**
   * if the loader show sql loader type should be seen
   */
  private boolean editLoaderShowSqlLoaderType;

  /**
   * if the loader show sql loader type should be seen
   * @return the editLoaderShowSqlLoaderType
   */
  public boolean isEditLoaderShowSqlLoaderType() {
    return this.editLoaderShowSqlLoaderType;
  }

  /**
   * if the loader show sql loader type should be seen
   * @param editLoaderShowSqlLoaderType1 the editLoaderShowSqlLoaderType to set
   */
  public void setEditLoaderShowSqlLoaderType(boolean editLoaderShowSqlLoaderType1) {
    this.editLoaderShowSqlLoaderType = editLoaderShowSqlLoaderType1;
  }

  /**
   * if show ldap loader type should be seen
   */
  private boolean editLoaderShowLdapLoaderType;
  
  /**
   * if show ldap loader type should be seen
   * @return the editLoaderShowLdapLoaderType
   */
  public boolean isEditLoaderShowLdapLoaderType() {
    return this.editLoaderShowLdapLoaderType;
  }
  
  /**
   * @param editLoaderShowLdapLoaderType1 the editLoaderShowLdapLoaderType to set
   */
  public void setEditLoaderShowLdapLoaderType(boolean editLoaderShowLdapLoaderType1) {
    this.editLoaderShowLdapLoaderType = editLoaderShowLdapLoaderType1;
  }

  /**
   * SQL_SIMPLE or SQL_GROUP_LIST
   */
  private String editLoaderSqlType;
  
  /**
   * SQL_SIMPLE or SQL_GROUP_LIST
   * @return the editLoaderSqlType
   */
  public String getEditLoaderSqlType() {
    return this.editLoaderSqlType;
  }

  /**
   * SQL_SIMPLE or SQL_GROUP_LIST
   * @param editLoaderSqlType1 the editLoaderSqlType to set
   */
  public void setEditLoaderSqlType(String editLoaderSqlType1) {
    this.editLoaderSqlType = editLoaderSqlType1;
  }

  /**
   * set of jobs to show on screen
   */
  private List<GuiGrouperLoaderJob> guiGrouperLoaderJobs;
  
  /**
   * set of jobs to show on screen
   * @return set of jobs
   */
  public List<GuiGrouperLoaderJob> getGuiGrouperLoaderJobs() {
    return this.guiGrouperLoaderJobs;
  }

  /**
   * set of jobs to show on screen
   * @param guiGrouperLoaderJobs1
   */
  public void setGuiGrouperLoaderJobs(List<GuiGrouperLoaderJob> guiGrouperLoaderJobs1) {
    this.guiGrouperLoaderJobs = guiGrouperLoaderJobs1;
  }

  /**
   * SQL or LDAP, GrouperLoaderType
   */
  private String editLoaderType;
  
  /**
   * SQL or LDAP, GrouperLoaderType
   * @return the editLoaderType
   */
  public String getEditLoaderType() {
    return this.editLoaderType;
  }
  
  /**
   * SQL or LDAP, GrouperLoaderType
   * @param editLoaderType1 the editLoaderType to set
   */
  public void setEditLoaderType(String editLoaderType1) {
    this.editLoaderType = editLoaderType1;
  }
  
  
  /**
   * loader managed group attributes
   */
  private GuiLoaderManagedGroup loaderManagedGroup;
  
  /**
   * @return
   */
  public GuiLoaderManagedGroup getLoaderManagedGroup() {
    return loaderManagedGroup;
  }

  /**
   * @param loaderManagedGroup
   */
  public void setLoaderManagedGroup(GuiLoaderManagedGroup loaderManagedGroup) {
    this.loaderManagedGroup = loaderManagedGroup;
  }

  /**
   * show if grouper admin or loader group or group admin
   * @return true if shouldl show the loader menu item
   */
  public boolean isCanSeeLoader() {
    if (isCanSeeLoaderOverall()) {
      return true;
    }
    if (GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.loader.view.by.group.admins", true)) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup() != null) {
        if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanAdmin()) {
          return true;
        }
      }
    }
    
    return false;
  }
  
  /**
   * show if grouper admin or loader group
   * @return true if should show the loader menu item
   */
  public boolean isCanSeeLoaderOverall() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    if (isCanEditLoader()) {
      return true;
    }
    if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanView()) {
      return false;
    }
    if (!StringUtils.isBlank(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.loader.must.be.in.group"))) {
      String error = GrouperUiFilter.requireUiGroup("uiV2.loader.must.be.in.group", loggedInSubject, false);
      //null error means allow
      return error == null;
    }
    
    return false;
  }

  private Boolean failsafeIssue = null;
  
  /**
   * see if there is a failsafe issue that is not approved
   * @return true if failsafe issue
   */
  public boolean isFailsafeIssue() {
    if (this.failsafeIssue == null) {
      String jobName = this.getJobName();
      this.failsafeIssue = !StringUtils.isBlank(jobName) && !GrouperFailsafe.isApproved(jobName) && GrouperFailsafe.isFailsafeIssue(jobName);
    }
    return this.failsafeIssue;
  }
  
  /**
   * @return the guiDaemonJob
   */
  public GuiDaemonJob getGuiDaemonJob() {
    if (StringUtils.isEmpty(this.getJobName())) {
      return null;
    }
    
    if (!hasRetrievedDaemonJob) {
      try {
        Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
        
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(new JobKey(this.getJobName()));
        if (triggers.size() > 0) {
          guiDaemonJob = new GuiDaemonJob(this.getJobName());
        }
      } catch (SchedulerException e) {
        throw new RuntimeException(e);
      }
      hasRetrievedDaemonJob = true;
    }
    
    return guiDaemonJob;
  }

  
  /**
   * @param guiDaemonJob the guiDaemonJob to set
   */
  public void setGuiDaemonJob(GuiDaemonJob guiDaemonJob) {
    this.guiDaemonJob = guiDaemonJob;
  }

  public Integer getSqlMaxOverallPercentGroupsRemove() {
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_MAX_OVERALL_PERCENT_GROUPS_REMOVE);
    
    return GrouperUtil.intObjectValue(grouperLoaderType, true);

  }

  public Integer getSqlMaxOverallPercentMembershipsRemove() {
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_MAX_OVERALL_PERCENT_MEMBERSHIPS_REMOVE);
    
    return GrouperUtil.intObjectValue(grouperLoaderType, true);
  }

  public Integer getSqlMinManagedGroups() {
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_MIN_MANAGED_GROUPS);
    
    return GrouperUtil.intObjectValue(grouperLoaderType, true);
  }

  public Integer getSqlMinOverallNumberOfMembers() {
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_MIN_OVERALL_NUMBER_OF_MEMBERS);
    
    return GrouperUtil.intObjectValue(grouperLoaderType, true);
  }

  public Integer getSqlMaxGroupPercentRemove() {
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_MAX_GROUP_PERCENT_REMOVE);
    
    return GrouperUtil.intObjectValue(grouperLoaderType, true);
  }

  public Integer getSqlMinGroupSize() {
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_MIN_GROUP_SIZE);
    
    return GrouperUtil.intObjectValue(grouperLoaderType, true);
  }

  public Integer getSqlMinGroupNumberOfMembers() {
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_MIN_GROUP_NUMBER_OF_MEMBERS);
    
    return GrouperUtil.intObjectValue(grouperLoaderType, true);
  }

  public Boolean getSqlFailsafeUse() {
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_FAILSAFE_USE);
    
    return GrouperUtil.booleanObjectValue(grouperLoaderType);
  }

  public String getSqlFailsafeUseOrDefault() {
    Boolean sqlFailsafeUse = this.getSqlFailsafeUse();
    if (sqlFailsafeUse == null) {
      return "default";
    }
    return sqlFailsafeUse ? "true" : "false";
  }
  
  
  public Boolean getSqlFailsafeSendEmail() {
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_FAILSAFE_SEND_EMAIL);
    
    return GrouperUtil.booleanObjectValue(grouperLoaderType);
  }

  public String getSqlFailsafeSendEmailOrDefault() {
    Boolean sqlFailsafeSendEmail = this.getSqlFailsafeSendEmail();
    if (sqlFailsafeSendEmail == null) {
      return "default";
    }
    return sqlFailsafeSendEmail ? "true" : "false";
  }

  public void grouperLoaderFailsafeAssignUse() {
    if (this.isLoaderGroup() && this.isGrouperSqlLoader()
        && (this.getSqlFailsafeUse() != null || this.getSqlFailsafeSendEmail() != null
        || this.getSqlMaxGroupPercentRemove() != null
        || this.getSqlMaxOverallPercentGroupsRemove() != null
        || this.getSqlMaxOverallPercentMembershipsRemove() != null
        || this.getSqlMinGroupNumberOfMembers() != null
        || this.getSqlMinGroupSize() != null
        || this.getSqlMinManagedGroups() != null
        || this.getSqlMinOverallNumberOfMembers() != null
        )) {
      this.setCustomizeFailsafeSelected(true);
    }
    
    if (this.isLoaderGroup() && this.isGrouperLdapLoader()
        && (this.getLdapFailsafeUse() != null || this.getLdapFailsafeSendEmail() != null
        || this.getLdapMaxGroupPercentRemove() != null
        || this.getLdapMaxOverallPercentGroupsRemove() != null
        || this.getLdapMaxOverallPercentMembershipsRemove() != null
        || this.getLdapMinGroupNumberOfMembers() != null
        || this.getLdapMinGroupSize() != null
        || this.getLdapMinManagedGroups() != null
        || this.getLdapMinOverallNumberOfMembers() != null
        )) {
      this.setCustomizeFailsafeSelected(true);
    }
    
  }
  
  /**
   * Input script or Pattern
   */
  private String editLoaderConstructScript;

  
  public String getEditLoaderConstructScript() {
    return editLoaderConstructScript;
  }

  
  public void setEditLoaderConstructScript(String editLoaderConstructScript) {
    this.editLoaderConstructScript = editLoaderConstructScript;
  }

  
  private String editLoaderAbacPattern;

  
  public String getEditLoaderAbacPattern() {
    return editLoaderAbacPattern;
  }

  
  public void setEditLoaderAbacPattern(String editLoaderAbacPattern) {
    this.editLoaderAbacPattern = editLoaderAbacPattern;
  }
  
  private GrouperJexlScriptAnalysis grouperJexlScriptAnalysis;
  
  public void setGrouperJexlScriptAnalysis(GrouperJexlScriptAnalysis grouperJexlScriptAnalysis) {
    this.grouperJexlScriptAnalysis = grouperJexlScriptAnalysis;
  }

  public GrouperJexlScriptAnalysis getGrouperJexlScriptAnalysis() {
    return grouperJexlScriptAnalysis;
  }
  
  private GuiSubject guiSubject;
  
  public void setGuiSubject(GuiSubject guiSubject) {
    this.guiSubject = guiSubject;
  }

  public GuiSubject getGuiSubject() {
    return guiSubject;
  }
  
}
