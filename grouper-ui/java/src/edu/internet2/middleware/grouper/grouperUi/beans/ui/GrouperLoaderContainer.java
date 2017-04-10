/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.redhogs.cronparser.CronExpressionDescriptor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiHib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class GrouperLoaderContainer {

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
  public static class ConfigName {
    
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
   * 
   * @return database name
   */
  public String getSqlDatabaseNameUrl() {
    
    String databaseName = this.getSqlDatabaseName();

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

  
  /**
   * 
   * @return extra attributes
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
   * 
   * @return is SQL loader
   */
  public boolean isGrouperSqlLoader() {
    return GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().isHasAttrDefNameGrouperLoader();
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
   * 
   * @return ldap loader type
   */
  public String getLdapLoaderType() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapTypeName());

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
   * 
   * @return if loader group
   */
  public boolean isLoaderGroup() {

    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();

    if (guiGroup.isHasAttrDefNameGrouperLoader() || guiGroup.isHasAttrDefNameGrouperLoaderLdap()) {
      return true;
    }
    
    return false;
    
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
   * show if grouper admin or edit loader group
   * @return true if should show the edit loader menu item
   */
  public boolean isCanEditLoader() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    String error = GrouperUiFilter.requireUiGroup("uiV2.loader.edit.if.in.group", loggedInSubject);
    //null error means allow
    if (error == null) {
      return true;
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
   * show if grouper admin or loader group
   * @return true if shouldl show the loader menu item
   */
  public boolean isCanSeeLoader() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    String error = GrouperUiFilter.requireUiGroup("uiV2.loader.must.be.in.group", loggedInSubject);
    //null error means allow
    if (error == null) {
      return true;
    }
    
    if (GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.loaderTab.view.by.group.admins", true)) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanAdmin()) {
        return true;
      }
    }
    
    return false;
  }
  
}
