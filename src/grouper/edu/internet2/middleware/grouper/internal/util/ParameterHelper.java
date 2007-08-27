/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.internal.util;
import  edu.internet2.middleware.grouper.AccessAdapter;
import  edu.internet2.middleware.grouper.Group;
import  edu.internet2.middleware.grouper.GrouperSession;
import  edu.internet2.middleware.grouper.NamingAdapter;
import  edu.internet2.middleware.grouper.Privilege;
import  edu.internet2.middleware.grouper.Stem;
import  edu.internet2.middleware.grouper.privs.AccessResolver;
import  edu.internet2.middleware.grouper.privs.NamingResolver;
import  edu.internet2.middleware.subject.Subject;
import  edu.internet2.middleware.subject.provider.SourceManager;


/** 
 * Utility class for validating parameters.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ParameterHelper.java,v 1.4 2007-08-27 16:40:16 blair Exp $
 * @since   1.2.1
 */
public class ParameterHelper {


  /**
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  private ParameterHelper notNull(Object object, String msg) 
    throws  IllegalArgumentException
  {
    if (msg == null) {
      throw new IllegalArgumentException("null message");
    }
    if (object == null) {
      throw new IllegalArgumentException(msg);
    }
    return this; 
  }

  /** 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullAccessAdapter(AccessAdapter accessAdapter) {
    return this.notNull(accessAdapter, "null AccessAdapter");
  }

  /** 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullAccessResolver(AccessResolver accessResolver) {
    return this.notNull(accessResolver, "null AccessAdapter");
  }

  /**
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullGroup(Group group) {
    return this.notNull(group, "null Group");
  }

  /**
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullGrouperSession(GrouperSession session) {
    return this.notNull(session, "null GrouperSession");
  }

  /** 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullNamingAdapter(NamingAdapter namingAdapter) {
    return this.notNull(namingAdapter, "null NamingAdapter");
  }

  /** 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullNamingResolver(NamingResolver namingResolver) {
    return this.notNull(namingResolver, "null NamingResolver");
  }

  /**
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullPrivilege(Privilege privilege) {
    return this.notNull(privilege, "null Privilege");
  }

  /**
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullPrivilegeArray(Privilege[] privilegeArray) {
    return this.notNull(privilegeArray, "null Privilege[]");
  }

  /**
   * TODO 20070827 test
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullSourceManager(SourceManager sourceManager) {
    return this.notNull(sourceManager, "null SourceManager");
  }
  /**
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullStem(Stem stem) {
    return this.notNull(stem, "null Stem");
  }

  /**
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullString(String string) {
    return this.notNullString(string, "null String");
  }

  /**
   * TODO 20070827 test
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullString(String string, String msg) {
    return this.notNull(string, msg);
  }

  /**
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullSubject(Subject subject) {
    return this.notNull(subject, "null Subject");
  }

} 

