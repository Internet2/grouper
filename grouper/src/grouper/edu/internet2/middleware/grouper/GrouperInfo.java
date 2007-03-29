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
import  edu.internet2.middleware.subject.Source;
import  java.util.Iterator;
import  java.util.Properties;

/**
 * Report on system and configuration information.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperInfo.java,v 1.5 2007-03-29 15:23:26 blair Exp $
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
    GrouperInfo info = new GrouperInfo();
    info._printSystemInfo();
    System.out.println();
    info._printGrouperInfo();
    System.out.println();
    info._printSubjectInfo();
    System.out.println();
    info._printHibernateInfo();
    System.exit(0);
  } // public static void main(args)


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  private void _print(String key, Properties props) {
    System.out.println(key + ": " + props.getProperty(key, GrouperConfig.EMPTY_STRING));
  } // private void _print(key, props)

  // @since   1.2.0
  private void _printGrouperInfo() {
    Properties props = GrouperConfig.internal_getProperties();
    this._print( "privileges.access.interface"       , props );
    this._print( "privileges.naming.interface"       , props );
    this._print( "privileges.access.cache.interface" , props );
    this._print( "privileges.naming.cache.interface" , props );
    this._print( "groups.wheel.use"                  , props );
    this._print( "groups.wheel.group"                , props );
  } // private void _printGrouperInfo()

  // @since   1.2.0
  private void _printHibernateInfo() {
    Properties props = GrouperConfig.internal_getHibernateProperties();
    this._print( "hibernate.dialect"                 , props );
    this._print( "hibernate.connection.driver_class" , props );
    this._print( "hibernate.connection.url"          , props );
    this._print( "hibernate.dbcp.ps.maxIdle"         , props );
    this._print( "hibernate.cache.provider_class"    , props );
  } // private void _printHibernateInfo()


  // @since   1.2.0
  private void _printSubjectInfo() {
    Source    sa;
    Iterator  it  = SubjectFinder.getSources().iterator();
    while (it.hasNext()) {
      sa = (Source) it.next();
      System.out.println(
        "source:"
        + " id="    + sa.getId()
        + " name="  + sa.getName()
        + " class=" + sa.getClass().getName()
      );
    }
  } // private void _printSubjectInfo()

  // @since   1.2.0
  private void _printSystemInfo() {
    Properties props = System.getProperties();  
    this._print( "os.name"        , props );
    this._print( "os.arch"        , props );
    this._print( "os.version"     , props );
    this._print( "java.version"   , props );
    this._print( "java.vendor"    , props );
    this._print( "java.class.path", props );
  } // private void _printSystemInfo()

} // public class GrouperInfo

