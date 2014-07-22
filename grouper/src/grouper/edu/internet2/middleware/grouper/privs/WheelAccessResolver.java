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
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2007 The University Of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.privs;

import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperAccessAdapter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Decorator that provides <i>Wheel</i> privilege resolution for {@link AccessResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: WheelAccessResolver.java,v 1.26 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public class WheelAccessResolver extends AccessResolverDecorator {

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#stop()
   */
  public void stop() {

    super.getDecoratedResolver().stop();
  }

  /** if use wheel group */
  private boolean useWheel = false;

  /** wheel group */
  private Group wheelGroup;

  /** wheel session */
  private GrouperSession wheelSession = null;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(WheelAccessResolver.class);

  /** only log this once... */
  private static boolean loggedWheelGroupMissing = false;

  /**
   * @param resolver resolver
   * @since   1.2.1
   */
  public WheelAccessResolver(AccessResolver resolver) {
    super(resolver);

    // TODO 20070816 this is ugly
    String useWheelString = GrouperConfig.retrieveConfig().propertyValueString(GrouperConfig.PROP_USE_WHEEL_GROUP);
    this.useWheel = Boolean.valueOf(useWheelString).booleanValue();
    // TODO 20070816 and this is even worse
    if (this.useWheel) {
      String wheelName = null;
      try {
        wheelName = GrouperConfig.retrieveConfig().propertyValueString(GrouperConfig.PROP_WHEEL_GROUP);
        this.wheelSession = GrouperSession.start(SubjectFinder.findRootSubject(), false);
        this.wheelGroup = GroupFinder.findByName(
            this.wheelSession,
            wheelName, true
            );
      } catch (Exception e) {

        String error = "Initialisation error with wheel group name '" + wheelName
            + "': " + e.getClass().getSimpleName()
            + "\n" + ExceptionUtils.getFullStackTrace(e);

        //only log this once as error, dont log if checking config
        if (!loggedWheelGroupMissing && !GrouperCheckConfig.inCheckConfig) {
          //OK, so wheel group does not exist. Not fatal...
          LOG.error(error);
          loggedWheelGroupMissing = true;
        } else {
          LOG.debug(error);
        }
        this.useWheel = false;
      }
    }
  }

  /**
   * @see     AccessResolver#getPrivileges(Group, Subject)
   * @since   1.2.1
   */
  public Set<AccessPrivilege> getPrivileges(Group group, Subject subject)
      throws IllegalArgumentException {
    //Get any user privs
    Set<AccessPrivilege> accessPrivs = super.getDecoratedResolver().getPrivileges(group,
        subject);

    //Add any due to Wheel.
    if (this.isAndUseWheel(subject)) {
      Set<Privilege> privs = Privilege.getAccessPrivs();
      AccessPrivilege ap = null;
      for (Privilege p : privs) {
        //Not happy about the klass but will do for now in the absence of a GrouperSession
        if (!p.equals(AccessPrivilege.OPTIN) && !p.equals(AccessPrivilege.OPTOUT)) {
          ap = new AccessPrivilege(group, subject, SubjectFinder.findRootSubject(),
              p, GrouperAccessAdapter.class.getName(), false, null);
          accessPrivs.add(ap);
        }
      }
    }
    return accessPrivs;
  }

  /**
   * 
   * @param subject
   * @return true if this is wheel and if using wheel
   */
  private boolean isAndUseWheel(Subject subject) {
    if (this.getGrouperSession().isConsiderIfWheelMember()) {
      if (this.useWheel) {
        if (isWheelMember(subject)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @see     AccessResolver#hasPrivilege(Group, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    //Admin incorporates other privileges - except optin /optout
    //Which we don't want to assume
    if (this.isAndUseWheel(subject)) {
      if (!AccessPrivilege.OPTOUT.equals(privilege)
          && !AccessPrivilege.OPTIN.equals(privilege)) {
        return true;

      }
    }
    AccessResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.hasPrivilege(group, subject, privilege);
  }

  /**
   * Retrieve boolean from cache for <code>isWheelMember(...)</code>.
   * @param subj 
   * @return  Cached return value or null.
   * @since   1.2.1
   */
  private Boolean getFromIsWheelMemberCache(Subject subj) {
    return WheelCache.getFromIsWheelMemberCache(subj);
  }

  /**
   * Put boolean into cache for <code>isWheelMember(...)</code>.
   * @param subj 
   * @param rv 
   * @since   1.2.1
   */
  private void putInHasPrivilegeCache(Subject subj, Boolean rv) {
    WheelCache.putInHasPrivilegeCache(subj, rv);
  }

  /**
   * Put boolean into cache for <code>isWheelMember(...)</code>.
   * @param subj 
   * @return  if wheel member
   * @since   1.2.1
   */
  private boolean isWheelMember(final Subject subj) {
    Boolean rv = getFromIsWheelMemberCache(subj);
    if (rv == null) {

      rv = (Boolean) GrouperSession.callbackGrouperSession(this.wheelSession,
          new GrouperSessionHandler() {

            public Object callback(GrouperSession grouperSession)
                throws GrouperSessionException {
              return WheelAccessResolver.this.wheelGroup.hasMember(subj);
            }
                    });

      putInHasPrivilegeCache(subj, rv);
    }
    return rv;
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#flushCache()
   */
  public void flushCache() {
    WheelCache.flush();
    super.getDecoratedResolver().flushCache();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterGroups(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Group> postHqlFilterGroups(Set<Group> groups, Subject subject,
      Set<Privilege> privInSet) {
    //Wheel can see all groups
    if (this.isAndUseWheel(subject)) {
      return groups;
    }
    AccessResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.postHqlFilterGroups(groups, subject, privInSet);

  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterStemsWithGroups(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Stem> postHqlFilterStemsWithGroups(Set<Stem> stems, Subject subject,
      Set<Privilege> inPrivSet) {
    //Wheel can see all groups
    if (this.isAndUseWheel(subject)) {
      return stems;
    }
    AccessResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.postHqlFilterStemsWithGroups(stems, subject, inPrivSet);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#hqlFilterGroupsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterGroupsWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String groupColumn, Set<Privilege> privInSet) {
    //Wheel can see all groups
    if (this.isAndUseWheel(subject)) {
      return false;
    }
    AccessResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.hqlFilterGroupsWhereClause(subject, hqlQuery, hql,
        groupColumn, privInSet);
  }
  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#hqlFilterGroupsNotWithPrivWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege, boolean)
   */
  public boolean hqlFilterGroupsNotWithPrivWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String groupColumn, Privilege privilege, boolean considerAllSubject) {

    //Wheel can see all groups
    if (this.isAndUseWheel(subject)) {
      return false;
    }
    AccessResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.hqlFilterGroupsNotWithPrivWhereClause(subject, hqlQuery, hql,
        groupColumn, privilege, considerAllSubject);
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterMemberships(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Membership> postHqlFilterMemberships(Subject subject,
      Set<Membership> memberships) {
    //Wheel can see all memberships
    if (this.isAndUseWheel(subject)) {
      return memberships;
    }
    AccessResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.postHqlFilterMemberships(subject, memberships);
  }

}
