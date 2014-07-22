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

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
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
 * Decorator that provides <i>Wheel</i> privilege resolution for {@link NamingResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: WheelNamingResolver.java,v 1.20 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public class WheelNamingResolver extends NamingResolverDecorator {

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#flushCache()
   */
  public void flushCache() {
    WheelCache.flush();
    super.getDecoratedResolver().flushCache();
  }

  // TODO 20070820 DRY w/ access resolution

  /** if use wheel group */
  private boolean useWheel = false;

  /** wheel group */
  private Group wheelGroup;

  /** wheel session */
  private GrouperSession wheelSession = null;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(WheelNamingResolver.class);

  /**
   * @param resolver 
   * @since   1.2.1
   */
  public WheelNamingResolver(NamingResolver resolver) {
    super(resolver);


    // TODO 20070816 this is ugly
    this.useWheel = Boolean.valueOf(
        GrouperConfig.retrieveConfig().propertyValueString(GrouperConfig.PROP_USE_WHEEL_GROUP)).booleanValue();
    // TODO 20070816 and this is even worse
    if (this.useWheel) {
      String wheelGroupName = "";
      try {
        wheelGroupName = GrouperConfig.retrieveConfig().propertyValueString(GrouperConfig.PROP_WHEEL_GROUP);
        this.wheelSession = GrouperSession.start(SubjectFinder.findRootSubject(), false);
        this.wheelGroup = GroupFinder.findByName(
                    //dont replace the current grouper session
            this.wheelSession,
            wheelGroupName, true
            );
      } catch (Exception e) {

        //OK, so wheel group does not exist. Not fatal...
        String error = "Cant find wheel group '" + wheelGroupName + "': "
            + e.getClass().getSimpleName();

        if (!loggedWheelNotThere && !GrouperCheckConfig.inCheckConfig) {
          LOG.error(error);
          loggedWheelNotThere = true;
        }
        //exception stack is not that interesting
        LOG.debug(error, e);
        this.useWheel = false;
      }
    }
  }

  /** */
  private static boolean loggedWheelNotThere = false;

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
              return WheelNamingResolver.this.wheelGroup.hasMember(subj);
            }
                    });

      putInHasPrivilegeCache(subj, rv);
    }
    return rv;
  }

  /**
   * @see     NamingResolver#hasPrivilege(Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Stem stem, final Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    if (this.isAndUseWheel(subject)) {
      return true;
    }
    return super.getDecoratedResolver().hasPrivilege(stem, subject, privilege);
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
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#hqlFilterStemsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterStemsWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String stemColumn, Set<Privilege> privInSet) {
    //Wheel can see all stems
    if (this.isAndUseWheel(subject)) {
      return false;
    }
    NamingResolver decoratedResolver = super.getDecoratedResolver();
    //CachingNamingResolver
    return decoratedResolver.hqlFilterStemsWhereClause(subject, hqlQuery, hql,
        stemColumn, privInSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#postHqlFilterStems(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Stem> postHqlFilterStems(Set<Stem> stems, Subject subject,
      Set<Privilege> privInSet) {

    //Wheel can see all groups
    if (this.isAndUseWheel(subject)) {
      return stems;
    }
    Set<Stem> filteredStems = super.getDecoratedResolver().postHqlFilterStems(stems,
        subject, privInSet);

    //return filtered groups
    return filteredStems;
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
   * 
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#hqlFilterStemsNotWithPrivWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege, boolean)
   */
  public boolean hqlFilterStemsNotWithPrivWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String stemColumn, Privilege privilege, boolean considerAllSubject) {

    //Wheel can see all groups
    if (this.isAndUseWheel(subject)) {
      return false;
    }
    NamingResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.hqlFilterStemsNotWithPrivWhereClause(subject, hqlQuery, hql,
        stemColumn, privilege, considerAllSubject);
  }

}
