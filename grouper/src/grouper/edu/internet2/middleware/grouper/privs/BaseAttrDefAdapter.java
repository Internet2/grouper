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
 * @author mchyzer
 * $Id: BaseAttrDefAdapter.java,v 1.1 2009-09-21 06:14:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.privs;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * Base class for attr def access adapter
 */
public abstract class BaseAttrDefAdapter implements AttributeDefAdapter {

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#postHqlFilterPermissions(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<PermissionEntry> postHqlFilterPermissions(GrouperSession grouperSession,
      Subject subject, Set<PermissionEntry> permissionEntries) {
    return PrivilegeHelper.canViewPermissions(grouperSession, permissionEntries);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#postHqlFilterAttributeDefs(edu.internet2.middleware.grouper.GrouperSession, java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<AttributeDef> postHqlFilterAttributeDefs(GrouperSession grouperSession, Set<AttributeDef> inputAttributeDefs, 
      Subject subject, Set<Privilege> privInSet) {

    //no privs no filter
    if (GrouperUtil.length(privInSet) == 0 || GrouperUtil.length(inputAttributeDefs) == 0) {
      return inputAttributeDefs;
    }

    Set<AttributeDef>  attributeDefs  = new LinkedHashSet<AttributeDef>();
    for ( AttributeDef child : inputAttributeDefs ) {
      
      if ( PrivilegeHelper.hasPrivilege(
          GrouperSession.staticGrouperSession().internal_getRootSession(), child, subject, privInSet ) ) {
        attributeDefs.add(child);
      }
    }
    return attributeDefs;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#hqlFilterAttrDefsWhereClause(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterAttrDefsWhereClause(GrouperSession grouperSession,
      Subject subject, HqlQuery hqlQuery, StringBuilder hqlTables, StringBuilder hqlWhereClause, String attributeDefColumn, Set<Privilege> privInSet) {
    //by default dont change the HQL
    return false;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#postHqlFilterAttributeAssigns(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<AttributeAssign> postHqlFilterAttributeAssigns(
      GrouperSession grouperSession, Subject subject,
      Set<AttributeAssign> attributeAssigns) {
    return PrivilegeHelper.canViewAttributeAssigns(grouperSession, attributeAssigns, false);
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#postHqlFilterPITAttributeAssigns(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<PITAttributeAssign> postHqlFilterPITAttributeAssigns(
      GrouperSession grouperSession, Subject subject,
      Set<PITAttributeAssign> pitAttributeAssigns) {
    return PrivilegeHelper.canViewPITAttributeAssigns(grouperSession, pitAttributeAssigns, false);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#hqlFilterAttributeDefsNotWithPrivWhereClause(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege, boolean)
   */
  public boolean hqlFilterAttributeDefsNotWithPrivWhereClause(GrouperSession grouperSession,
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String attributeDefColumn, Privilege privilege, boolean considerAllSubject) {
    //by default dont change the HQL
    return false;
  }

}
