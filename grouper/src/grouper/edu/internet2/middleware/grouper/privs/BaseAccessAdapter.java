/*
 * @author mchyzer
 * $Id: BaseAccessAdapter.java,v 1.1.2.1 2009-04-10 18:44:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.privs;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * Base class for access adapter
 */
public abstract class BaseAccessAdapter implements AccessAdapter {

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessAdapter#postHqlFilterGroups(edu.internet2.middleware.grouper.GrouperSession, java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Group> postHqlFilterGroups(GrouperSession grouperSession, Set<Group> inputGroups, 
      Subject subject, Set<Privilege> privInSet) {
    

    //no privs no filter
    if (GrouperUtil.length(privInSet) == 0 || GrouperUtil.length(inputGroups) == 0) {
      return inputGroups;
    }

    Set<Group>  groups  = new LinkedHashSet();
    for ( Group child : inputGroups ) {
      
      if ( PrivilegeHelper.hasPrivilege(
          GrouperSession.staticGrouperSession().internal_getRootSession(), child, subject, privInSet ) ) {
        groups.add(child);
      }
    }
    return groups;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessAdapter#hqlFilterGroupsWhereClause(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterGroupsWhereClause(GrouperSession grouperSession,
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String groupColumn, Set<Privilege> privInSet) {
    //by default dont change the HQL
    return false;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessAdapter#postHqlFilterMemberships(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Membership> postHqlFilterMemberships(
      GrouperSession grouperSession, Subject subject,
      Set<Membership> memberships) {

    return PrivilegeHelper.canViewMemberships(grouperSession, memberships);
  }


}
