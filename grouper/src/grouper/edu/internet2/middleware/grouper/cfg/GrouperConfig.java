/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;


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
   * get the attribute def ids to ignore when sending to change log, and audit 
   * @return the set of attribute definition ids
   */
  public Set<String> attributeDefIdsToIgnoreChangeLogAndAudit() {
    
    if (this.attributeDefIdsToIgnoreChangeLogAndAuditSet == null) {
      
      synchronized (this) {

        if (this.attributeDefIdsToIgnoreChangeLogAndAuditSet == null) {
          Set<String> result = new HashSet<String>();
          String namesOfAttributeDefsCommaSeparated = this.propertyValueString("grouper.attribute.namesOfAttributeDefsToIgnoreAuditsChangeLogPit");
            
          Set<String> tempResult = attributeDefIdsToIgnoreChangeLogAndAuditSetCache.get(namesOfAttributeDefsCommaSeparated);
          
          if (tempResult == null) {
            
            if (!StringUtils.isBlank(namesOfAttributeDefsCommaSeparated)) {
              String[] namesOfAttributeDefs = GrouperUtil.splitTrim(namesOfAttributeDefsCommaSeparated, ",");
              
              //get a root session, or use existing
              GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
              boolean startedSession = grouperSession == null;
              try {
                if (startedSession) {
                  grouperSession = GrouperSession.startRootSession();
                } else {
                  grouperSession = grouperSession.internal_getRootSession();
                }
                
                for (String nameOfAttributeDef : namesOfAttributeDefs) {
                  try {
                    //if not there log it.  e.g. for UI you might ignore attributes, but wont be there if testing the API
                    AttributeDef attributeDef = AttributeDefFinder.findByName(nameOfAttributeDef, false);
                    
                    if (attributeDef == null) {
                      LOG().error("Attribute def not found: " + nameOfAttributeDef);
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
                
              } finally {
                if (startedSession) {
                  GrouperSession.stopQuietly(grouperSession);
                }
              }
            }
            
            //you dont want callers modifying this
            result = Collections.unmodifiableSet(result);

            //put back in cache
            attributeDefIdsToIgnoreChangeLogAndAuditSetCache.put(namesOfAttributeDefsCommaSeparated, result);
          } else {
            result = tempResult;
          }
          this.attributeDefIdsToIgnoreChangeLogAndAuditSet = result;
            
        }

      }
      
    }
    return this.attributeDefIdsToIgnoreChangeLogAndAuditSet;
    
  }

  
  
  /**
   * use the factory
   */
  private GrouperConfig() {
    
  }

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperConfig retrieveConfig() {
    return retrieveConfig(GrouperConfig.class);
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
  public static final String SCGAS         = "stems.create.grant.all.stem";
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
    //nothing to do
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
  
} 
