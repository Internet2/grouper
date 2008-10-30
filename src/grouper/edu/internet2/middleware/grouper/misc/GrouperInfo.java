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

package edu.internet2.middleware.grouper.misc;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.internal.dao.hibernate.HibernateDaoConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;

/**
 * Report on system and configuration information.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperInfo.java,v 1.3 2008-10-30 20:57:17 mchyzer Exp $
 * @since   1.1.0
 */
public class GrouperInfo {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(GrouperInfo.class);

  /**
   * 
   */
  private ApiConfig           api;
  /**
   * 
   */
  private HibernateDaoConfig  hib;


  // CONSTRUCTORS //

  /**
   * @since     1.1.0
   */
  private GrouperInfo() {
    this.api  = new ApiConfig();
    this.hib  = new HibernateDaoConfig();
  } 


  // PUBLIC CLASS METHODS //
 
  /**
   * Print system and configuration information to STDOUT.
   * <p/>
   * <pre class="eg">
   * % java edu.internet2.middleware.grouper.GrouperInfo
   * </pre>
   * @param args 
   * @since   1.1.0
   */
  public static void main(String[] args) {
    grouperInfo();
  } // public static void main(args)


  /**
   * 
   */
  public static void grouperInfo() {
    GrouperInfo info = new GrouperInfo();
    info._printSystemInfo();
    System.out.println();
    info._printGrouperInfo();
    System.out.println();
    info._printSubjectInfo();
    System.out.println();
    info._printHibernateInfo();
  }


  // PRIVATE INSTANCE METHODS //

  /**
   * @param key 
   * @param val 
   * @since   1.2.0
   */
  private void _print(String key, String val) {
    if (val == null) {
      val = GrouperConfig.EMPTY_STRING;
    }
    System.out.println(key + ": " + val);
  }

  /**
   * @since   1.2.0
   */
  private void _printGrouperInfo() {
    String key = "privileges.access.interface";
    this._print( key, this.api.getProperty(key) );
    key = "privileges.access.interface";
    this._print( key, this.api.getProperty(key) );
    key = "privileges.naming.interface";
    this._print( key, this.api.getProperty(key) );
    key = "privileges.access.cache.interface";
    this._print( key, this.api.getProperty(key) );
    key = "privileges.naming.cache.interface";
    this._print( key, this.api.getProperty(key) );
    key = "groups.wheel.use";
    this._print( key, this.api.getProperty(key) );
    key = "groups.wheel.group";
    this._print( key, this.api.getProperty(key) );
  } 

  /**
   * @since   1.2.0
   */
  private void _printHibernateInfo() {
    String key = "hibernate.connection.username";
    this._print( key, this.hib.getProperty(key) );
    key = "hibernate.dialect";
    this._print( key, this.hib.getProperty(key) );
    key = "hibernate.dialect";
    this._print( key, this.hib.getProperty(key) );
    key = "hibernate.connection.driver_class";
    this._print( key, this.hib.getProperty(key) );
    key = "hibernate.connection.url";
    this._print( key, this.hib.getProperty(key) );
    key = "hibernate.dbcp.ps.maxIdle";
    this._print( key, this.hib.getProperty(key) );
    key = "hibernate.cache.provider_class";
    this._print( key, this.hib.getProperty(key) );
  } 

  /**
   * @since   1.2.0
   */
  private void _printSubjectInfo() {
    Source    sa;
    Iterator<Source>  it  = SubjectFinder.getSources().iterator();
    while (it.hasNext()) {
      sa = it.next();
      System.out.println(
          sa.printConfig() + " name="  + sa.getName()
      );
    }
  } 

  /**
   * @since   1.2.0
   */
  private void _printSystemInfo() {
    Properties  props = System.getProperties();  
    String      key   = "os.name";
    this._print( key, props.getProperty(key, GrouperConfig.EMPTY_STRING) );
    key = "os.arch";
    this._print( key, props.getProperty(key, GrouperConfig.EMPTY_STRING) );
    key = "os.version";
    this._print( key, props.getProperty(key, GrouperConfig.EMPTY_STRING) );
    key = "java.version";
    this._print( key, props.getProperty(key, GrouperConfig.EMPTY_STRING) );
    key = "java.vendor";
    this._print( key, props.getProperty(key, GrouperConfig.EMPTY_STRING) );
    key = "java.class.path";
    this._print( key, props.getProperty(key, GrouperConfig.EMPTY_STRING) );
    long heapSize = Runtime.getRuntime().totalMemory();
    this._print("heapSize", GrouperUtil.byteCountToDisplaySize(heapSize));
    
    long heapMaxSize = Runtime.getRuntime().maxMemory();
    this._print("heapMaxSize", GrouperUtil.byteCountToDisplaySize(heapMaxSize));

    long heapFreeSize = Runtime.getRuntime().freeMemory();
    this._print("heapSizeFree", GrouperUtil.byteCountToDisplaySize(heapFreeSize));
    
  } 

} 

