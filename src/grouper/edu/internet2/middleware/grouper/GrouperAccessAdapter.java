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

import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.privs.GrouperNonDbAccessAdapter;
import edu.internet2.middleware.grouper.privs.GrouperPrivilegeAdapter;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/** 
 * <pre> 
 * Grouper Access Privilege interface.
 * <p>
 * Unless you are implementing a new implementation of this interface,
 * you should not need to directly use these methods as they are all
 * wrapped by methods in the {@link Group} class.
 * </p>
 * This access adapter affects the HQL queries to give better performance
 * 
 * Note: there is no membership override since there is no way to filter all out
 * by query since it checks which priv is needed to read
 * 
 * TODO filter memberships in an effecient way with one (or only a few) queries
 * </pre>
 * @author  blair christensen.
 * @version $Id: GrouperAccessAdapter.java,v 1.80 2009-04-13 16:53:08 mchyzer Exp $
 */
public class GrouperAccessAdapter extends GrouperNonDbAccessAdapter {

  /**
   * note, can use 
   * @see edu.internet2.middleware.grouper.privs.AccessAdapter#hqlFilterGroupsWhereClause(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterGroupsWhereClause(GrouperSession grouperSession,
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String groupColumn, Set<Privilege> privInSet) {

    //no privs no filter
    if (GrouperUtil.length(privInSet) == 0) {
      return false;
    }
    
    Member member = MemberFinder.internal_findBySubject(subject, null, false);
    Member allMember = MemberFinder.internal_findAllMember();

    //FieldFinder.findAllIdsByType(FieldType.ACCESS);
    Collection<String> accessPrivs = GrouperPrivilegeAdapter.fieldIdSet(priv2list, privInSet); 
    String accessInClause = HibUtils.convertToInClause(accessPrivs, hqlQuery);

    //TODO update this for 1.5 (group owner)
    //if not, we need an in clause
    StringBuilder query = hql.append( ", Membership __accessMembership where " +
    		"__accessMembership.ownerUuid = " + groupColumn
    		+ " and __accessMembership.fieldId in (");
    query.append(accessInClause).append(") and __accessMembership.memberUuid in (");
    Set<String> memberIds = GrouperUtil.toSet(allMember.getUuid());
    if (member != null) {
      memberIds.add(member.getUuid());
    }
    String memberInClause = HibUtils.convertToInClause(memberIds, hqlQuery);
    query.append(memberInClause).append(")");
    return true;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.BaseAccessAdapter#postHqlFilterGroups(edu.internet2.middleware.grouper.GrouperSession, java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  @Override
  public Set<Group> postHqlFilterGroups(GrouperSession grouperSession,
      Set<Group> inputGroups, Subject subject, Set<Privilege> privInSet) {
    //assume the HQL filtered everything
    return inputGroups;
  }
} // public class GrouperAccessAdapter 

