/**
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
 */
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
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.privs.AccessAdapter;
import edu.internet2.middleware.grouper.privs.AccessResolver;
import edu.internet2.middleware.grouper.privs.AttributeDefAdapter;
import edu.internet2.middleware.grouper.privs.AttributeDefResolver;
import edu.internet2.middleware.grouper.privs.NamingAdapter;
import edu.internet2.middleware.grouper.privs.NamingResolver;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


/** 
 * Utility class for validating parameters.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ParameterHelper.java,v 1.7 2009-09-21 06:14:27 mchyzer Exp $
 * @since   1.2.1
 */
public class ParameterHelper implements Serializable {


  /**
   * @param object 
   * @param msg 
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
   * @param accessAdapter 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullAccessAdapter(AccessAdapter accessAdapter) {
    return this.notNull(accessAdapter, "null AccessAdapter");
  }

  /** 
   * @param accessResolver 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullAccessResolver(AccessResolver accessResolver) {
    return this.notNull(accessResolver, "null AccessResolver");
  }

  /** 
   * @param attributeDefAdapter 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   */
  public ParameterHelper notNullAttrDefAdapter(AttributeDefAdapter attributeDefAdapter) {
    return this.notNull(attributeDefAdapter, "null AttributeDefAdapter");
  }

  /** 
   * @param attributeDefResolver 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   */
  public ParameterHelper notNullAttrDefResolver(AttributeDefResolver attributeDefResolver) {
    return this.notNull(attributeDefResolver, "null AttributeDefResolver");
  }

  /**
   * @param group 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullGroup(Group group) {
    return this.notNull(group, "null Group");
  }

  /**
   * @param session 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullGrouperSession(GrouperSession session) {
    return this.notNull(session, "null GrouperSession");
  }

  /** 
   * @param namingAdapter 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullNamingAdapter(NamingAdapter namingAdapter) {
    return this.notNull(namingAdapter, "null NamingAdapter");
  }

  /** 
   * @param namingResolver 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullNamingResolver(NamingResolver namingResolver) {
    return this.notNull(namingResolver, "null NamingResolver");
  }

  /**
   * @param privilege 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullPrivilege(Privilege privilege) {
    return this.notNull(privilege, "null Privilege");
  }

  /**
   * @param hqlQuery is the query
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   */
  public ParameterHelper notNullHqlQuery(HqlQuery hqlQuery) {
    return this.notNull(hqlQuery, "null hqlQuery");
  }

  /**
   * @param privilegeArray 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullPrivilegeArray(Privilege[] privilegeArray) {
    return this.notNull(privilegeArray, "null Privilege[]");
  }

  /**
   * @param privilegeSet 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullPrivilegeSet(Set<Privilege> privilegeSet) {
    return this.notNull(privilegeSet, "null Privilege set");
  }

  /**
   * TODO 20070827 test
   * @param sourceManager 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullSourceManager(SourceManager sourceManager) {
    return this.notNull(sourceManager, "null SourceManager");
  }
  /**
   * @param stem 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullStem(Stem stem) {
    return this.notNull(stem, "null Stem");
  }

  /**
   * @param string 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullString(String string) {
    return this.notNullString(string, "null String");
  }

  /**
   * TODO 20070827 test
   * @param string 
   * @param msg 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullString(String string, String msg) {
    return this.notNull(string, msg);
  }

  /**
   * @param string s
   * @param msg 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   2.0.2
   */
  public ParameterHelper notNullCollectionString(Collection<String> strings, String msg) {
    return this.notNull(strings, msg);
  }

  /**
   * @param subject 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullSubject(Subject subject) {
    return this.notNull(subject, "null Subject");
  }

  /**
   * @param attributeDef 
   * @return  Self for chained calling.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public ParameterHelper notNullAttributeDef(AttributeDef attributeDef) {
    return this.notNull(attributeDef, "null Group");
  }

} 

