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
import  edu.internet2.middleware.subject.Source;
import  java.lang.System;
import  java.util.Iterator;
import  java.util.Properties;

/**
 * Report on system and configuration information.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperInfo.java,v 1.1 2006-09-12 13:49:55 blair Exp $
 * @since   1.1.0
 */
public class GrouperInfo {

  // CONSTRUCTORS //

  // @since   1.1.0
  private GrouperInfo() {
    super();
  } // private GrouperInfo()



  // PUBLIC CLASS METHODS //
 
  /**
   * Print system and configuration information to STDOUT.
   * <p/>
   * <pre class="eg">
   * % java edu.internet2.middleware.grouper.GrouperInfo
   * </pre>
   * @since   1.1.0
   */
  public static void main(String[] args) {
    _getSystemInfo();
    _getGrouperInfo();
    _getSubjectInfo();
    _getHibernateInfo();
    System.exit(0);
  } // public static void main(args)


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static void _get(String key, Properties props) {
    System.out.println(key + ": " + props.getProperty(key, GrouperConfig.EMPTY_STRING));
  } // private static void _get(key, props)

  // @since   1.1.0
  private static void _getGrouperInfo() {
    //_getProps( GrouperConfig.getProperties() );
    Properties props = GrouperConfig.getProperties();
    _get( "privileges.access.interface"       , props );
    _get( "privileges.naming.interface"       , props );
    _get( "privileges.access.cache.interface" , props );
    _get( "privileges.naming.cache.interface" , props );
    _get( "groups.wheel.use"                  , props );
    _get( "groups.wheel.group"                , props );
  } // private static void _getGrouperInfo()

  // @since   1.1.0
  private static void _getHibernateInfo() {
    Properties props = GrouperConfig.getHibernateProperties();
    _get( "hibernate.dialect"                 , props );
    _get( "hibernate.connection.driver_class" , props );
    _get( "hibernate.connection.url"          , props );
    _get( "hibernate.dbcp.ps.maxIdle"         , props );
    _get( "hibernate.cache.provider_class"    , props );
  } // private static void _getHibernateInfo()

  // @since   1.1.0
  private static void _getSubjectInfo() {
    Iterator iter = SubjectFinder.getSources().iterator();
    while (iter.hasNext()) {
      Source sa = (Source) iter.next();
      System.out.println(
        "source:"
        + " id="    + sa.getId()
        + " name="  + sa.getName()
        + " class=" + sa.getClass().getName()
      );
    }
  } // private static void _getSubjectInfo()

  // @since   1.1.0
  private static void _getSystemInfo() {
    Properties props = System.getProperties();  
    _get( "java.version"    , props );
    _get( "java.vendor"     , props );
    _get( "java.class.path" , props );
    _get( "os.name"         , props );
    _get( "os.arch"         , props );
    _get( "os.version"      , props );
  } // private static void _getSystemInfo()

} // public class GrouperInfo

