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

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.grouper.cfg.ApiConfig;
import  edu.internet2.middleware.grouper.cfg.BuildConfig;
import  edu.internet2.middleware.grouper.internal.dao.hibernate.HibernateDaoConfig;


/** 
 * Grouper configuration information.
 * <p><b>This class is being deprecated by the {@link edu.internet2.middleware.grouper.cfg.Configuration} interface.</b></p>
 * @author  blair christensen.
 * @version $Id: GrouperConfig.java,v 1.52 2007-08-27 15:53:52 blair Exp $
 * @since   ?
 */
public class GrouperConfig {
  // TODO 20070724 deprecate


  /**
   * Default DAO implementation to be used if an alternative is not configured.
   * <p/>
   * @since   1.2.0
   */
  public static final String DEFAULT_DAO_FACTORY  = "edu.internet2.middleware.grouper.internal.dao.hibernate.HibernateDAOFactory";
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
  protected static final String ALL           = "GrouperAll";
  protected static final String ATTR_C        = "createSubject";
  protected static final String ATTR_CT       = "createTime";
  protected static final String ATTR_D        = "description";
  protected static final String ATTR_DE       = "displayExtension";
  protected static final String ATTR_DN       = "displayName";
  protected static final String ATTR_E        = "extension";
  protected static final String ATTR_N        = "name";
  protected static final String BT            = "true";
  protected static final String GCGAA         = "groups.create.grant.all.admin";
  protected static final String GCGAOI        = "groups.create.grant.all.optin";
  protected static final String GCGAOO        = "groups.create.grant.all.optout";
  protected static final String GCGAR         = "groups.create.grant.all.read";
  protected static final String GCGAU         = "groups.create.grant.all.update";
  protected static final String GCGAV         = "groups.create.grant.all.view";
  protected static final String IST           = "application";
  protected static final String LIST          = "members";
  protected static final String MSLGEA        = "memberships.log.group.effective.add";
  protected static final String MSLGED        = "memberships.log.group.effective.del";
  protected static final String MSLSEA        = "memberships.log.stem.effective.add";
  protected static final String MSLSED        = "memberships.log.stem.effective.del";
  protected static final String NL            = System.getProperty("line.separator");
  protected static final String ROOT          = "GrouperSystem";
  protected static final String SCGAC         = "stems.create.grant.all.create";
  protected static final String SCGAS         = "stems.create.grant.all.stem";
  protected static final String SCII          = "subjects.cache.id.interface";
  protected static final String SCIDFRI       = "subjects.cache.identifier.interface";


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
    return getDefaultValueIfNull( getInstance().build.getProperty(property) );
  } 

  /** 
   * @since   1.2.1
   */
  private static String getDefaultValueIfNull(String val) {
    return ( val == null ? GrouperConfig.EMPTY_STRING : val );
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
    return getDefaultValueIfNull( getInstance().hib.getProperty(property) );
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
    return getDefaultValueIfNull( getInstance().api.getProperty(property) );
  }

  // TODO 20070824 i think GrouperSession now has everything it needs to eliminate this method
  protected static void internal_setProperty(String property, String value) {
    //throw new RuntimeException("!!! DEPRECATED !!!");
    getInstance().api.setProperty(property, value);
  } 

} 

