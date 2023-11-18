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

package edu.internet2.middleware.grouper.cfg;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.app.loader.NotificationDaemon;
import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigAttributeNames;
import edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames;
import edu.internet2.middleware.grouper.app.usdu.UsduAttributeNames;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceAttributeNames;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.text.TextBundleBean;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttrFramework;
import edu.internet2.middleware.grouper.instrumentation.InstrumentationDataUtils;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.userData.GrouperUserDataUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.config.db.ConfigDatabaseLogic;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/** 
 * Grouper configuration information.
 * <p><b>This class is being deprecated by the {@link edu.internet2.middleware.grouper.cfg.Configuration} interface.</b></p>
 * @author  blair christensen.
 * @version $Id: GrouperConfig.java,v 1.9 2009-12-16 06:02:30 mchyzer Exp $
 * @since   ?
 */
public class GrouperConfig extends ConfigPropertiesCascadeBase {

  /** logger */
  private static Log LOG = null;

  /**
   * logger
   * @return the logger
   */
  private static Log LOG() {
    if (LOG == null) {
      LOG = GrouperUtil.getLog(GrouperConfig.class);
    }
    return LOG;
  }
  
  /**
   * cache this so we dont have to lookup ids all the time
   */
  private static ExpirableCache<String, Set<String>> attributeDefIdsToIgnoreChangeLogAndAuditSetCache = new ExpirableCache<String, Set<String>>(10);
  
  /**
   * attribute def ids that shouldnt be stored in change log or audited
   */
  private Set<String> attributeDefIdsToIgnoreChangeLogAndAuditSet = null;
  
  /**
   * cache this so we dont have to lookup ids all the time
   */
  private static ExpirableCache<String, Set<String>> attributeDefNameIdsToIgnoreChangeLogAndAuditSetCache = new ExpirableCache<String, Set<String>>(10);
  
  /**
   * attribute def name ids that shouldnt be stored in change log or audited
   */
  private Set<String> attributeDefNameIdsToIgnoreChangeLogAndAuditSet = null;
  
  /**
   * get the attribute def ids to ignore when sending to change log, and audit 
   * @return the set of attribute definition ids
   */
  @SuppressWarnings("unchecked")
  public Set<String> attributeDefIdsToIgnoreChangeLogAndAudit() {
    
    if (this.attributeDefIdsToIgnoreChangeLogAndAuditSet == null) {
      
      // dont synchronize, just sleep a bit to reduce deadlock
      GrouperClientUtils.sleep(Math.round(Math.random() * 100d));

      if (this.attributeDefIdsToIgnoreChangeLogAndAuditSet == null) {
      
        Set<String> result = (Set<String>)GrouperSession.callbackGrouperSession(
            GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            Set<String> result = new HashSet<String>();
            
            if (GrouperStartup.isFinishedStartupSuccessfully()) {
              String namesOfAttributeDefsCommaSeparated = GrouperConfig.this.propertyValueString("grouper.attribute.namesOfAttributeDefsToIgnoreAuditsChangeLogPit");
                
              Set<String> tempResult = attributeDefIdsToIgnoreChangeLogAndAuditSetCache.get(namesOfAttributeDefsCommaSeparated);
              
              if (tempResult == null) {
                
                Set<String> namesOfAttributeDefs = new HashSet<String>();
                
                // GRP-1695: hard code built in ignore attribute defs and names
                // $$grouper.attribute.rootStem$$:userData:grouperUserDataValueDef
                namesOfAttributeDefs.add(GrouperUserDataUtils.grouperUserDataStemName() + ":" + GrouperUserDataUtils.USER_DATA_VALUE_DEF);
                namesOfAttributeDefs.add(GrouperUserDataUtils.grouperUserDataStemName() + ":" + GrouperUserDataUtils.USER_DATA_DEF);

                // $$grouper.attribute.rootStem$$:instrumentationData:instrumentationDataInstanceCountsDef
                namesOfAttributeDefs.add(InstrumentationDataUtils.grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_COUNTS_DEF);
    
                // $$grouper.attribute.rootStem$$:instrumentationData:instrumentationDataInstanceDetailsDef
                namesOfAttributeDefs.add(InstrumentationDataUtils.grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_DETAILS_DEF);
    
                // $$grouper.attribute.rootStem$$:instrumentationData:instrumentationDataCollectorDetailsDef
                namesOfAttributeDefs.add(InstrumentationDataUtils.grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_COLLECTOR_DETAILS_DEF);
                
                // $$grouper.attribute.rootStem$$:attribute:loaderMetadata:loaderMetadataValueDef
                namesOfAttributeDefs.add(GrouperCheckConfig.loaderMetadataStemName() + ":loaderMetadataValueDef");
                
                // attestation
                namesOfAttributeDefs.add(GrouperAttestationJob.retrieveAttributeDef().getName());
                namesOfAttributeDefs.add(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName());
                
                // reports
                namesOfAttributeDefs.add(GrouperReportConfigAttributeNames.retrieveAttributeDefBaseDef().getName());
                namesOfAttributeDefs.add(GrouperReportConfigAttributeNames.retrieveAttributeDefValueDef().getName());
                namesOfAttributeDefs.add(GrouperReportInstanceAttributeNames.retrieveAttributeDefBaseDef().getName());
                namesOfAttributeDefs.add(GrouperReportInstanceAttributeNames.retrieveAttributeDefValueDef().getName());
                
                // subject resolution
                namesOfAttributeDefs.add(UsduAttributeNames.retrieveAttributeDefBaseDef().getName());
                namesOfAttributeDefs.add(UsduAttributeNames.retrieveAttributeDefValueDef().getName());
                
                // workflow config 
                namesOfAttributeDefs.add(GrouperWorkflowConfigAttributeNames.retrieveAttributeDefBaseDef().getName());
                namesOfAttributeDefs.add(GrouperWorkflowConfigAttributeNames.retrieveAttributeDefValueDef().getName());
                
                // workflow instance
                namesOfAttributeDefs.add(GrouperWorkflowInstanceAttributeNames.retrieveAttributeDefBaseDef().getName());
                namesOfAttributeDefs.add(GrouperWorkflowInstanceAttributeNames.retrieveAttributeDefValueDef().getName());
                
                // external subject invite
                namesOfAttributeDefs.add(ExternalSubjectAttrFramework.retrieveAttributeDefBaseDef().getName());
                namesOfAttributeDefs.add(ExternalSubjectAttrFramework.retrieveAttributeDefValueDef().getName());
                
                // notifications
                namesOfAttributeDefs.add(NotificationDaemon.attributeAutoCreateStemName() + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT_DEF);
                
                if (!StringUtils.isBlank(namesOfAttributeDefsCommaSeparated)) {
                  namesOfAttributeDefs.addAll(GrouperUtil.splitTrimToSet(namesOfAttributeDefsCommaSeparated, ","));
                }
                
                if (GrouperUtil.length(namesOfAttributeDefs) > 0) {
                  for (String nameOfAttributeDef : namesOfAttributeDefs) {
                    try {
                      //if not there log it.  e.g. for UI you might ignore attributes, but wont be there if testing the API
                      AttributeDef attributeDef = AttributeDefFinder.findByName(nameOfAttributeDef, false);
                      
                      if (attributeDef == null) {
                        LOG().info("Attribute def not found: " + nameOfAttributeDef + " in attribute churn reduction.  Thats ok.");
                        continue;
                      }
                      
                      result.add(attributeDef.getId());
                      
                      if (result.size() > 150) {
                        throw new RuntimeException("Cant have a size of more than 150 for attributeDefs excluded from audits and PIT");
                      }
                      
                    } catch (RuntimeException re) {
                      GrouperUtil.injectInException(re, "name of attributeDef configured "
                          + "in grouper properties file: grouper.attribute.namesOfAttributeDefsToIgnoreAuditsChangeLogPit, "
                          + "that attribute cannot be found.  ");
                      throw re;
                    }
                  }
                }
                
                //you dont want callers modifying this
                result = Collections.unmodifiableSet(result);
    
                //put back in cache
                attributeDefIdsToIgnoreChangeLogAndAuditSetCache.put(namesOfAttributeDefsCommaSeparated, result);
              } else {
                result = GrouperUtil.nonNull(tempResult);
              }
              
              return result;  
            }
            
            return null;
          }
        });
        
        if (result != null) {
          this.attributeDefIdsToIgnoreChangeLogAndAuditSet = result;
        }

      }
      
    }
    return GrouperUtil.nonNull(this.attributeDefIdsToIgnoreChangeLogAndAuditSet);
    
  }
  
  /**
   * For testing purposes
   */
  public void resetAttributeDefNameIdsToIgnoreChangeLogAndAudit() {
    attributeDefNameIdsToIgnoreChangeLogAndAuditSet = null;
    attributeDefNameIdsToIgnoreChangeLogAndAuditSetCache.clear();
  }

  
  /**
   * get the attribute def name ids to ignore when sending to change log, and audit 
   * @return the set of attribute def name ids
   */
  @SuppressWarnings("unchecked")
  public Set<String> attributeDefNameIdsToIgnoreChangeLogAndAudit() {
    
    if (this.attributeDefNameIdsToIgnoreChangeLogAndAuditSet == null) {
      
      synchronized (this) {

        if (this.attributeDefNameIdsToIgnoreChangeLogAndAuditSet == null) {
          
          this.attributeDefNameIdsToIgnoreChangeLogAndAuditSet = (Set<String>)GrouperSession.callbackGrouperSession(
              GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              
              Set<String> result = new HashSet<String>();
              
              String namesOfAttributeDefNamesCommaSeparated = GrouperConfig.this.propertyValueString("grouper.attribute.namesOfAttributeDefNamesToIgnoreAuditsChangeLogPit");
                
              Set<String> tempResult = attributeDefNameIdsToIgnoreChangeLogAndAuditSetCache.get(namesOfAttributeDefNamesCommaSeparated);
              
              if (tempResult == null) {
                
                Set<String> namesOfAttributeDefNames = new HashSet<String>();
                
                if (!StringUtils.isBlank(namesOfAttributeDefNamesCommaSeparated)) {
                  namesOfAttributeDefNames.addAll(GrouperUtil.splitTrimToSet(namesOfAttributeDefNamesCommaSeparated, ","));
                }

                if (GrouperUtil.length(namesOfAttributeDefNames) > 0) {
                  for (String nameOfAttributeDefName : namesOfAttributeDefNames) {
                    try {
                      //if not there log it.  e.g. for UI you might ignore attributes, but wont be there if testing the API
                      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(nameOfAttributeDefName, false);
                      
                      if (attributeDefName == null) {
                        LOG().error("Attribute def name not found: " + nameOfAttributeDefName);
                        continue;
                      }
                      
                      result.add(attributeDefName.getId());
                      
                      if (result.size() > 150) {
                        throw new RuntimeException("Cant have a size of more than 150 for attributeDefNames excluded from audits and PIT");
                      }
                      
                    } catch (RuntimeException re) {
                      GrouperUtil.injectInException(re, "name of attributeDefName configured "
                          + "in grouper properties file: grouper.attribute.namesOfAttributeDefNamesToIgnoreAuditsChangeLogPit, "
                          + "that attribute cannot be found.  ");
                      throw re;
                    }
                  }
                }
                
                //you dont want callers modifying this
                result = Collections.unmodifiableSet(result);

                //put back in cache
                attributeDefNameIdsToIgnoreChangeLogAndAuditSetCache.put(namesOfAttributeDefNamesCommaSeparated, result);
              } else {
                result = GrouperUtil.nonNull(tempResult);
              }
              
              return result;               
            }
          });
          
        }

      }
      
    }

    return GrouperUtil.nonNull(this.attributeDefNameIdsToIgnoreChangeLogAndAuditSet);
    
  }
  
  
  /**
   * use the factory
   */
  private GrouperConfig() {
    
  }

  /** when was last config set to db config settings */
  private static long lastDatabaseLogicConfigSet = -1L;
  
  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperConfig retrieveConfig() {
    GrouperConfig grouperConfig = retrieveConfig(GrouperConfig.class);
    
    // every 10 seconds set properties to db config
    long currentTime = System.currentTimeMillis();
    if ((currentTime - lastDatabaseLogicConfigSet) / 1000 > 10) {
      lastDatabaseLogicConfigSet = currentTime;
      // set some things for db configuration
      boolean readonly = grouperConfig.propertyValueBoolean("grouper.api.readonly", true);
      ConfigDatabaseLogic.assignReadonly(readonly);
      int secondsBetweenUpdateChecksToDb = grouperConfig.propertyValueInt("grouper.config.secondsBetweenUpdateChecksToDb", 600);
      ConfigDatabaseLogic.assignSecondsBetweenUpdateChecksToDb(secondsBetweenUpdateChecksToDb);
      int secondsBetweenFullRefresh = grouperConfig.propertyValueInt("grouper.config.secondsBetweenFullRefresh", 600);
      ConfigDatabaseLogic.assignSecondsBetweenFullRefresh(secondsBetweenFullRefresh);
    }
    return grouperConfig;
  }


  /**
   * Default DAO implementation to be used if an alternative is not configured.
   * <p/>
   * @since   1.2.0
   */
  public static final String DEFAULT_DAO_FACTORY  = Hib3DAOFactory.class.getName();
  /**
   * String with value of <code>""</code>.
   */
  public static final String EMPTY_STRING         = "";
  /**
   * Epoch origin.
   */
  public static final long   EPOCH                = 0;
  /**
   * Property containing name of DAO implementation to be used.
   * <p>Grouper will default to <code>DEFAULT_DAO_FACTORY</code> if this property is not set.</p>
   * @since   1.2.0
   */
  public static final String PROP_DAO_FACTORY     = "dao.factory";
  /**
   * Property containing maximum age of cached wheel group.
   */
  public static final String PROP_MAX_WHEEL_AGE   = "edu.internet2.middleware.internal.cache.SimpleWheelPrivilegeCache.maxWheelAge";
  /**
   * Property containing name of wheel group.
   */
  public static final String PROP_WHEEL_GROUP     = "groups.wheel.group";
  /**
   * Property determining whether wheel group is to be used.
   */
  public static final String PROP_USE_WHEEL_GROUP = "groups.wheel.use";
 
  // PROTECTED CLASS CONSTANTS //
  public static final String ALL           = "GrouperAll";
  public static final String ALL_NAME           = "EveryEntity";
  public static final String ATTR_C        = "createSubject";
  public static final String ATTR_CT       = "createTime";
  public static final String ATTRIBUTE_DESCRIPTION        = "description";
  public static final String ATTRIBUTE_DISPLAY_EXTENSION       = "displayExtension";
  public static final String ATTRIBUTE_DISPLAY_NAME       = "displayName";
  public static final String ATTRIBUTE_EXTENSION        = "extension";
  public static final String ATTRIBUTE_NAME        = "name";
  public static final String BT            = "true";
  public static final String GCGAOI        = "groups.create.grant.all.optin";
  public static final String GCGAOO        = "groups.create.grant.all.optout";
  public static final String GCGAR         = "groups.create.grant.all.read";
  public static final String GCGAV         = "groups.create.grant.all.view";
  public static final String GCGAGAR       = "groups.create.grant.all.groupAttrRead";

  /** */
  public static final String ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_ADMIN 
    = "attributeDefs.create.grant.all.attrAdmin";

  /** */
  public static final String ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_OPTIN 
    = "attributeDefs.create.grant.all.attrOptin";

  /** */
  public static final String ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_OPTOUT 
    = "attributeDefs.create.grant.all.attrOptou";

  /** */
  public static final String ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_READ 
    = "attributeDefs.create.grant.all.attrRead";

  /** */
  public static final String ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_UPDATE 
    = "attributeDefs.create.grant.all.attrUpdate";

  /** */
  public static final String ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_VIEW 
    = "attributeDefs.create.grant.all.attrView";

  /** */
  public static final String ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_DEF_ATTR_READ 
    = "attributeDefs.create.grant.all.attrDefAttrRead";
  
  /** */
  public static final String ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_DEF_ATTR_UPDATE 
    = "attributeDefs.create.grant.all.attrDefAttrUpdate";
  
  public static final String IST           = "application";
  public static final String LIST          = "members";
  public static final String NL            = System.getProperty("line.separator");
  public static final String ROOT          = "GrouperSystem";
  
  public static final String ROOT_NAME          = "GrouperSysAdmin";
  public static final String SCGAC         = "stems.create.grant.all.create";
  public static final String SCGASA        = "stems.create.grant.all.stemAdmin";
  public static final String SCGASAR       = "stems.create.grant.all.stemAttrRead";
  public static final String SCGASAU       = "stems.create.grant.all.stemAttrUpdate";
  public static final String SCII          = "subjects.cache.id.interface";
  public static final String SCIDFRI       = "subjects.cache.identifier.interface";
  public static final String WHEEL_NAME          = "SysAdmin";

  /** if tooltips should be substituted in messages */
  public static final String MESSAGES_USE_TOOLTIPS = "messages.use.tooltips";


  /** 
   * @since   1.2.1
   */
  private static String getDefaultTrimmedValueIfNull(String val) {
    return ( val == null ? GrouperConfig.EMPTY_STRING : val.trim() );
  }

  /**
   * Get a Hibernate configuration parameter.
   * <pre class="eg">
   * String dialect = GrouperConfig.getHibernateProperty("hibernate.dialect");
   * </pre>
   * @return  Value of configuration parameter or an empty string if
   *   parameter is invalid.
   * @since   1.1.0
   * @deprecated use GrouperHibernateConfig.retrieveConfig().propertyValueString() instead
   */
  @Deprecated
  public static String getHibernateProperty(String property) {
    
    return getDefaultTrimmedValueIfNull( GrouperHibernateConfig.retrieveConfig().propertyValueString(property) );
  }

  /**
   * Get a Grouper configuration parameter.
   * <pre class="eg">
   * String wheel = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group");
   * </pre>
   * @param property is the property key
   * @return  Value of configuration parameter or an empty string if
   *   parameter is invalid.
   * @since   1.1.0
   * @deprecated use GrouperConfig.retrieveConfig().propertyValueString instead
   */
  @Deprecated
  public static String getProperty(String property) {
    return retrieveConfig().propertyValueString(property, "");
  }

  /**
   * Get a Grouper config names
   * @return set of names
   * @deprecated use GrouperConfig.retrieveConfig().propertyNames() instead
   */
  @Deprecated
  public static Set<String> getPropertyNames() {
    return retrieveConfig().propertyNames();
  }

  /**
   * get the property value as a boolean, throw an exception if invalid value.
   * Acceptable values are: t, f, true, false (case-insensitive)
   * @param propertyName
   * @param defaultValue if the property is blank or missing, return this value
   * @return true or false
   * @deprecated use GrouperConfig.retrieveConfig().propertyValueBoolean(propertyName, defaultValue) instead
   */
  @Deprecated
  public static boolean getPropertyBoolean(String propertyName, boolean defaultValue) {
    return GrouperConfig.retrieveConfig().propertyValueBoolean(propertyName, defaultValue);
  }
  
  /**
   * get the property value as an int, throw an exception if invalid value.
   * @param propertyName
   * @param defaultValue if the property is blank or missing, return this value
   * @return the int
   * @deprecated use GrouperConfig.retrieveConfig().propertyValueInt
   */
  @Deprecated
  public static int getPropertyInt(String propertyName, int defaultValue) {
    return retrieveConfig().propertyValueInt(propertyName, defaultValue);
  }
  
  /**
   * @param property 
   * @param defaultValue 
   * @return the property value, or the default value if the property value is blank
   * @throws IllegalArgumentException 
   */
  public String getProperty(String property, String defaultValue) 
      throws  IllegalArgumentException {

    String val = getProperty(property);

    //if no val, then return the default value
    if (StringUtils.isBlank(val)) {
      return defaultValue;
    }
    return val;
  }

  /**
   * get the hibernate property value as an int, throw an exception if invalid value.
   * @param property 
   * @param defaultValue 
   * @return int
   */
  public static int getHibernatePropertyInt(String property, int defaultValue) {
    
    String value = getHibernateProperty(property);
    if (StringUtils.isBlank(value)) {
      return defaultValue;
    }
    return GrouperUtil.intValue(value, defaultValue);
  }
  
  /**
   * get the UI url with a slash on the end
   * @param exceptionIfNull
   * @return the UI URL
   */
  public static String getGrouperUiUrl(boolean exceptionIfNull) {
    String url = getProperty("grouper.ui.url");
    if (StringUtils.isBlank(url)) {
      if (exceptionIfNull) {
        throw new RuntimeException("grouper.ui.url is null in grouper.properties");
      }
      return null;
    }
    if (url.endsWith("/")) {
      return url;
    }
    return url + "/";
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues
   */
  @Override
  public void clearCachedCalculatedValues() {
    this.attributeDefIdsToIgnoreChangeLogAndAuditSet = null;
    this.attributeDefNameIdsToIgnoreChangeLogAndAuditSet = null;
    attributeDefNameIdsToIgnoreChangeLogAndAuditSetCache.clear();
    attributeDefIdsToIgnoreChangeLogAndAuditSetCache.clear();
    
    this.deprovisioningAffiliations = null;
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "grouper.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "grouper.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "grouper.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "grouper.config.secondsBetweenUpdateChecks";
  }
  
  /**
   * cache deprovisioning affiliations
   */
  private Set<String> deprovisioningAffiliations = null;

  /**
   * default bundle
   */
  private TextBundleBean textBundleDefault = null;

  /**
   * country to text bundle
   */
  private Map<String, TextBundleBean> textBundleFromCountry  = null;

  /**
   * language to text bundle
   */
  private Map<String, TextBundleBean> textBundleFromLanguage  = null;

  /**
   * language_country to text bundle
   */
  private Map<String, TextBundleBean> textBundleFromLanguageAndCountry  = null;
  
  /**
   * pattern compile alphanumeric and underscore
   */
  private static Pattern affiliationNamePattern = Pattern.compile("^[a-zA-Z0-9_]+$");
  
  /**
   * cache deprovisioning affiliations
   * @return the affiliations
   */
  public Set<String> deprovisioningAffiliations() {

    if (this.deprovisioningAffiliations != null) {
      return this.deprovisioningAffiliations;
    }

    String affiliationsString = GrouperConfig.retrieveConfig().propertyValueString("deprovisioning.affiliations");
    if (StringUtils.isBlank(affiliationsString)) {
      return new HashSet<String>();
    }
    Set<String> result = GrouperUtil.splitTrimToSet(affiliationsString, ",");
    
    Iterator<String> resultIterator = result.iterator();
    
    while (resultIterator.hasNext()) {
      
      String affiliation = resultIterator.next();
      
      Matcher matcher = affiliationNamePattern.matcher(affiliation);
      
      if (!matcher.matches()) {
        LOG.error("Affiliation name configured in grouper.properties deprovisioning.affiliations is not valid: '" + affiliation + "'!!!!!!!");
        resultIterator.remove();
      }
      
    }
    return result;

  }

  /**
   * default bundle
   * @return the textBundleDefault
   */
  public TextBundleBean textBundleDefault() {
    if (this.textBundleDefault == null) {
      this.textBundleFromCountry();
    }
    return this.textBundleDefault;
  }

  /**
   * country to text bundle
   * @return the map
   */
  public Map<String, TextBundleBean> textBundleFromCountry() {
    if (this.textBundleFromCountry == null) {
      
      synchronized (this) {
        
        if (this.textBundleFromCountry == null) {
          
          Map<String, TextBundleBean> tempBundleFromCountry = new HashMap<String, TextBundleBean>();
          Map<String, TextBundleBean> tempBundleFromLanguage = new HashMap<String, TextBundleBean>();
          Map<String, TextBundleBean> tempBundleFromLanguageAndCountry = new HashMap<String, TextBundleBean>();
          
          Pattern pattern = Pattern.compile("^grouper\\.text\\.bundle\\.(.*)\\.fileNamePrefix$");
          
          boolean foundDefault = false;
          
          for (Object keyObject : this.properties().keySet()) {
            String key = (String)keyObject;
            Matcher matcher = pattern.matcher(key);
            if (matcher.matches()) {
              
              String bundleKey = matcher.group(1);
  
              String fileNamePrefix = this.propertyValueString(key);
              String language = StringUtils.defaultString(this.propertyValueString("grouper.text.bundle." + bundleKey + ".language")).toLowerCase();
              String country = StringUtils.defaultString(this.propertyValueString("grouper.text.bundle." + bundleKey + ".country")).toLowerCase();
              
              TextBundleBean textBundleBean = new TextBundleBean();
              
              textBundleBean.setCountry(country);
              textBundleBean.setLanguage(language);
              textBundleBean.setFileNamePrefix(fileNamePrefix);
  
              if (StringUtils.equals(bundleKey, propertyValueStringRequired("grouper.text.defaultBundleIndex"))) {
                foundDefault = true;
                this.textBundleDefault = textBundleBean;
              }
              
              //first in wins
              if (!tempBundleFromCountry.containsKey(country)) {
                tempBundleFromCountry.put(country, textBundleBean);
              }
              if (!tempBundleFromLanguage.containsKey(language)) {
                tempBundleFromLanguage.put(language, textBundleBean);
              }
              String languageAndCountry = language + "_" + country;
              if (tempBundleFromLanguageAndCountry.containsKey(languageAndCountry)) {
                LOG.error("Language and country already defined! " + languageAndCountry);
              }
              tempBundleFromLanguageAndCountry.put(languageAndCountry, textBundleBean);
            }
          }
          
          if (!foundDefault) {
            throw new RuntimeException("Cant find default bundle index: '" 
                + propertyValueStringRequired("grouper.text.defaultBundleIndex") + "', should have a key: grouper.text.bundle."
                + propertyValueStringRequired("grouper.text.defaultBundleIndex") + ".fileNamePrefix");
          }
          
          this.textBundleFromCountry = Collections.unmodifiableMap(tempBundleFromCountry);
          this.textBundleFromLanguage = Collections.unmodifiableMap(tempBundleFromLanguage);
          this.textBundleFromLanguageAndCountry = Collections.unmodifiableMap(tempBundleFromLanguageAndCountry);
          
        }
      }
    }
    return this.textBundleFromCountry;
  }

  /**
   * country to text bundle
   * @return the map
   */
  public Map<String, TextBundleBean> textBundleFromLanguage() {
    //init
    Map<String, TextBundleBean> theTextBundleFromLanguage = this.textBundleFromLanguage;
    if (theTextBundleFromLanguage == null) {
      //init here
      this.textBundleFromCountry();
      theTextBundleFromLanguage = this.textBundleFromLanguage;
    }
    if (theTextBundleFromLanguage == null) {
      throw new RuntimeException("Why is textBundleFromLanguage map null????");
    }
    return theTextBundleFromLanguage;
  }

  /**
   * country to text bundle
   * @return the map
   */
  public Map<String, TextBundleBean> textBundleFromLanguageAndCountry() {
    //init
    Map<String, TextBundleBean> theTextBundleFromLanguageAndCountry = this.textBundleFromLanguageAndCountry;
    if (theTextBundleFromLanguageAndCountry == null) {
      //init here
      this.textBundleFromCountry();
      theTextBundleFromLanguageAndCountry = this.textBundleFromLanguageAndCountry;
    }
    if (theTextBundleFromLanguageAndCountry == null) {
      throw new RuntimeException("Why is textBundleFromLanguage map null????");
    }
    return theTextBundleFromLanguageAndCountry;
  }
  

  
} 
