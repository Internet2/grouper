/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  java.io.*;
import  java.util.*;
import  org.apache.commons.lang.*;

/** 
 * Grouper configuration information.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperConfig.java,v 1.33 2006-09-14 20:04:04 blair Exp $
 */
public class GrouperConfig {

  // PUBLIC CLASS CONSTANTS //
  
  /**
   * Grouper configuration file.
   */
  public static final String GROUPER_CF   = "/grouper.properties";
  /**
   * Hibernate configuration file.
   */
  public static final String HIBERNATE_CF = "/grouper.hibernate.properties";


  // PROTECTED CLASS CONSTANTS //
  protected static final String ALL           = "GrouperAll";
  protected static final String BT            = "true";
  protected static final String EMPTY_STRING  = "";
  protected static final String GCGAA         = "groups.create.grant.all.admin";
  protected static final String GCGAOI        = "groups.create.grant.all.optin";
  protected static final String GCGAOO        = "groups.create.grant.all.optout";
  protected static final String GCGAR         = "groups.create.grant.all.read";
  protected static final String GCGAU         = "groups.create.grant.all.update";
  protected static final String GCGAV         = "groups.create.grant.all.view";
  protected static final String GWG           = "groups.wheel.group";
  protected static final String GWU           = "groups.wheel.use";
  protected static final String IST           = "application";
  protected static final String LIST          = "members";
  protected static final String MSLGEA        = "memberships.log.group.effective.add";
  protected static final String MSLGED        = "memberships.log.group.effective.del";
  protected static final String MSLSEA        = "memberships.log.stem.effective.add";
  protected static final String MSLSED        = "memberships.log.stem.effective.del";
  protected static final String PACI          = "privileges.access.cache.interface";
  protected static final String PAI           = "privileges.access.interface";
  protected static final String PNCI          = "privileges.naming.cache.interface";
  protected static final String PNI           = "privileges.naming.interface";
  protected static final String ROOT          = "GrouperSystem";
  protected static final String SCGAC         = "stems.create.grant.all.create";
  protected static final String SCGAS         = "stems.create.grant.all.stem";
  protected static final String SCII          = "subjects.cache.id.interface";
  protected static final String SCIDFRI       = "subjects.cache.identifier.interface";


  // PRIVATE CLASS VARIABLES //
  private static  Properties  grouper_props = new Properties();
  private static  Properties  hib_props     = new Properties();


  // STATIC //
  static {
    // Load Grouper properties
    try {
      InputStream in = GrouperConfig.class.getResourceAsStream(GROUPER_CF);
      grouper_props.load(in);
    }
    catch (IOException eIO) {
      String msg = E.CONFIG_READ + eIO.getMessage();
      ErrorLog.fatal(GrouperConfig.class, msg);
      throw new GrouperRuntimeException(msg, eIO);
    }
    // Load Hibernate properties
    try {
      InputStream in = GrouperConfig.class.getResourceAsStream(HIBERNATE_CF);
      hib_props.load(in);
    }
    catch (IOException eIO) {
      String msg = E.CONFIG_READ_HIBERNATE + eIO.getMessage();
      ErrorLog.fatal(GrouperConfig.class, msg);
      throw new GrouperRuntimeException(msg, eIO);
    }
  } // static



  // CONSTRUCTORS //
  private GrouperConfig() {
    super();
  } // private GrouperConfig()


  // PUBLIC CLASS METHODS //

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
    return _getProperty(hib_props, property);
  } // public static String getHibernateProperty(property)

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
    return _getProperty(grouper_props, property);
  } // public static String getProperty(property)


  // PROTECTED CLASS METHODS //

  // @since   1.1.0
  protected static Properties getHibernateProperties() {
    return hib_props;
  } // protected static Properties getHibernateProperties()

  // @since   1.1.0
  protected static Properties getProperties() {
    return grouper_props;
  } // protected static Properties getProperties()

  // @since   1.1.0
  protected static void setProperty(String property, String value) {
    grouper_props.setProperty(property, value);
  } // protected static void setProperty(property, value):w


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static String _getProperty(Properties props, String property) {
    String value = GrouperConfig.EMPTY_STRING;
    if ( (property != null) && (props.containsKey(property)) ) {
      value = StringUtils.strip( props.getProperty(property) );
    }
    return value;
  } // private static String _getProperty(props, property)

} // public class GrouperConfig

