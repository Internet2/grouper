/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGrouperLoaderJob;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiHib3GrouperLoaderLog;
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
    if (!StringUtils.isBlank(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.loader.must.be.in.group"))) {
      String error = GrouperUiFilter.requireUiGroup("uiV2.loader.must.be.in.group", loggedInSubject, false);
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
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanAdmin()) {
        return true;
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
    if (!StringUtils.isBlank(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.loader.must.be.in.group"))) {
      String error = GrouperUiFilter.requireUiGroup("uiV2.loader.must.be.in.group", loggedInSubject, false);
      //null error means allow
      return error == null;
    }
    
    return false;
  }
  
}
