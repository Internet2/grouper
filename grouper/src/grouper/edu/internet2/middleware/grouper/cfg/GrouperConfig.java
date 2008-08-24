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
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAOFactory;
import  edu.internet2.middleware.grouper.internal.dao.hibernate.HibernateDaoConfig;


/** 
 * Grouper configuration information.
 * <p><b>This class is being deprecated by the {@link edu.internet2.middleware.grouper.cfg.Configuration} interface.</b></p>
 * @author  blair christensen.
 * @version $Id: GrouperConfig.java,v 1.2 2008-08-24 06:16:29 mchyzer Exp $
 * @since   ?
 */
public class GrouperConfig {
  // TODO 20070724 deprecate


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
   * Grouper configuration file.
   */
  public static final String GROUPER_CF           = "/grouper.properties";
  /**
   * Grouper build configuration file.
   */
  public static final String GROUPER_BUILD_CF     = "/buildGrouper.properties";
  /**
   * Hibernate configuration file.
   */
  public static final String HIBERNATE_CF         = "/grouper.hibernate.properties";
  /**
   * Optional local configuration file to override contents of <i>GROUPER_CF</i>.
   * @since   1.2.1
   */
  public static final String LOCAL_GROUPER_CF     = "/local.grouper.properties";
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
  public static final String ATTR_C        = "createSubject";
  public static final String ATTR_CT       = "createTime";
  public static final String ATTR_DESCRIPTION        = "description";
  public static final String ATTR_DISPLAY_EXTENSION       = "displayExtension";
  public static final String ATTR_DISPLAY_NAME       = "displayName";
  public static final String ATTR_EXTENSION        = "extension";
  public static final String ATTR_NAME        = "name";
  public static final String BT            = "true";
  public static final String GCGAA         = "groups.create.grant.all.admin";
  public static final String GCGAOI        = "groups.create.grant.all.optin";
  public static final String GCGAOO        = "groups.create.grant.all.optout";
  public static final String GCGAR         = "groups.create.grant.all.read";
  public static final String GCGAU         = "groups.create.grant.all.update";
  public static final String GCGAV         = "groups.create.grant.all.view";
  public static final String IST           = "application";
  public static final String LIST          = "members";
  public static final String MSLGEA        = "memberships.log.group.effective.add";
  public static final String MSLGED        = "memberships.log.group.effective.del";
  public static final String MSLSEA        = "memberships.log.stem.effective.add";
  public static final String MSLSED        = "memberships.log.stem.effective.del";
  public static final String NL            = System.getProperty("line.separator");
  public static final String ROOT          = "GrouperSystem";
  public static final String SCGAC         = "stems.create.grant.all.create";
  public static final String SCGAS         = "stems.create.grant.all.stem";
  public static final String SCII          = "subjects.cache.id.interface";
  public static final String SCIDFRI       = "subjects.cache.identifier.interface";

  /** if tooltips should be substituted in messages */
  public static final String MESSAGES_USE_TOOLTIPS = "messages.use.tooltips";
  private static  GrouperConfig       cfg;
  private         ApiConfig           api;
  private         BuildConfig         build;
  private         HibernateDaoConfig  hib;


  /**
   * Default constructor.
   * @since   ?
   */
  private GrouperConfig() {
    super();
    this.api   = new ApiConfig();
    this.build = new BuildConfig();
    this.hib   = new HibernateDaoConfig();
  } 

  /**
   * Get a Grouper build configuration parameter.
   * <pre class="eg">
   * String schemaexportOut = GrouperConfig.getBuildProperty("schemaexport.out");
   * </pre>
   * @return  Value of configuration parameter or an empty string if parameter is invalid.
   * @since   1.2.0
   */
  public static String getBuildProperty(String property) {
    return getDefaultTrimmedValueIfNull( getInstance().build.getProperty(property) );
  } 

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
   */
  public static String getHibernateProperty(String property) {
    return getDefaultTrimmedValueIfNull( getInstance().hib.getProperty(property) );
  }

  /**
   * @since   1.2.1
   */
  private static GrouperConfig getInstance() {
    if (cfg == null) {
      cfg = new GrouperConfig();
    }
    return cfg;
  }

  /**
   * Get a Grouper configuration parameter.
   * <pre class="eg">
   * String wheel = GrouperConfig.getProperty("groups.wheel.group");
   * </pre>
   * @return  Value of configuration parameter or an empty string if
   *   parameter is invalid.
   * @since   1.1.0
   */
  public static String getProperty(String property) {
    return getDefaultTrimmedValueIfNull( getInstance().api.getProperty(property) );
  }

  /**
   * get the property value as a boolean, throw an exception if invalid value.
   * Acceptable values are: t, f, true, false (case-insensitive)
   * @param propertyName
   * @param defaultValue if the property is blank or missing, return this value
   * @return true or false
   */
  public static boolean getPropertyBoolean(String propertyName, boolean defaultValue) {
    String value = getProperty(propertyName);
    if (StringUtils.isBlank(value)) {
      return defaultValue;
    }
    
    if ("true".equalsIgnoreCase(value)) {
      return true;
    }
    if ("false".equalsIgnoreCase(value)) {
      return false;
    }
    if ("t".equalsIgnoreCase(value)) {
      return true;
    }
    if ("f".equalsIgnoreCase(value)) {
      return false;
    }
    throw new RuntimeException("Invalid value: '" + value + "' for property: " + propertyName + " in grouper.properties");
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

  /** property setters dont cause queries */
  public static final String PROP_SETTERS_DONT_CAUSE_QUERIES 
    = "grouper.setters.dont.cause.queries";
  
} 
