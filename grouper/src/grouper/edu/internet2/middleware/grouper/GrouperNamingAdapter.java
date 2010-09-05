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
import java.util.Collection;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.privs.GrouperNonDbNamingAdapter;
import edu.internet2.middleware.grouper.privs.GrouperPrivilegeAdapter;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/** 
 * Default implementation of the Grouper {@link NamingPrivilege}
 * interface.
 * <p>
 * This implementation uses the Groups Registry and custom list types
 * to manage naming privileges.
 * </p>
 * @author  blair christensen.
 * @version $Id: GrouperNamingAdapter.java,v 1.85 2009-10-03 13:47:12 shilen Exp $
 */
public class GrouperNamingAdapter extends GrouperNonDbNamingAdapter {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(GrouperNamingAdapter.class);

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.BaseNamingAdapter#hqlFilterStemsWhereClause(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  @Override
  public boolean hqlFilterStemsWhereClause(GrouperSession grouperSession,
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String stemColumn,
      Set<Privilege> privInSet) {
    //no privs no filter
    if (GrouperUtil.length(privInSet) == 0) {
      return false;
    }
    
    Member member = MemberFinder.internal_findBySubject(subject, null, false);
    Member allMember = MemberFinder.internal_findAllMember();

    //FieldFinder.findAllIdsByType(FieldType.CREATE);
    Collection<String> namingPrivs = GrouperPrivilegeAdapter.fieldIdSet(priv2list, privInSet); 
    String accessInClause = HibUtils.convertToInClause(namingPrivs, hqlQuery);

    //TODO update this for 1.5 (stem owner)
    //if not, we need an in clause
    StringBuilder query = hql.append( ", MembershipEntry __namingMembership where " +
        "__namingMembership.ownerStemId = " + stemColumn
        + " and __namingMembership.fieldId in (");
    query.append(accessInClause).append(") and __namingMembership.memberUuid in (");
    Set<String> memberIds = GrouperUtil.toSet(allMember.getUuid());
    if (member != null) {
      memberIds.add(member.getUuid());
    }
    String memberInClause = HibUtils.convertToInClause(memberIds, hqlQuery);
    query.append(memberInClause).append(")");
    
    // don't return disabled memberships
    query.append(" and __namingMembership.enabledDb = 'T'");
    return true;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.BaseNamingAdapter#postHqlFilterStems(edu.internet2.middleware.grouper.GrouperSession, java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  @Override
  public Set<Stem> postHqlFilterStems(GrouperSession grouperSession,
      Set<Stem> inputStems, Subject subject, Set<Privilege> privInSet) {
    //this is already filtered
    return inputStems;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.privs.NamingAdapter#hqlFilterStemsNotWithPrivWhereClause(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege, boolean)
   */
  public boolean hqlFilterStemsNotWithPrivWhereClause(GrouperSession grouperSession,
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String stemColumn, Privilege privilege, boolean considerAllSubject) {
    
    Member member = MemberFinder.internal_findBySubject(subject, null, true);
    Member allMember = MemberFinder.internal_findAllMember();

    String fieldId = privilege.getField().getUuid();
    
    if (hql.indexOf(" where ") == -1) {
      hql.append(" where ");
    } else {
      hql.append(" and ");
    }
    
    hql.append(" not exists (select __notInMembership.uuid from MembershipEntry __notInMembership where " +
        " __notInMembership.enabledDb = 'T' and __notInMembership.ownerStemId = " + stemColumn + " " +
            " and __notInMembership.fieldId = :notInMembershipFieldId and __notInMembership.memberUuid in ( " +
            " :notInMembershipMemberId" + (considerAllSubject ? ", :notInMembershipAllMemberId" : "") + ")) ");
    
    hqlQuery.setString("notInMembershipFieldId", fieldId);
    hqlQuery.setString("notInMembershipMemberId", member.getUuid());
    if (considerAllSubject) {
      hqlQuery.setString("notInMembershipAllMemberId", allMember.getUuid());
    }

    return true;
  }

  
} // public class GrouperNamingAdapter

