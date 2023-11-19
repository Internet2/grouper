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
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
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

  /** if use readonly wheel group */
  private boolean useReadonlyWheel = false;

  /** wheel readonly group */
  private Group wheelReadonlyGroup;

  /** only log this once... */
  private static boolean loggedWheelReadonlyGroupMissing = false;

  /** if use viewonly wheel group */
  private boolean useViewonlyWheel = false;

  /** wheel viewonly group */
  private Group wheelViewonlyGroup;

  /** only log this once... */
  private static boolean loggedWheelViewonlyGroupMissing = false;

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

    {

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
              wheelGroupName, GrouperStartup.isFinishedStartupSuccessfully()
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
      
      {
        // TODO 20070816 this is ugly
        String useWheelViewonlyString = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.viewonly.use");
        this.useViewonlyWheel = Boolean.valueOf(useWheelViewonlyString).booleanValue();
        // TODO 20070816 and this is even worse
        if (this.useViewonlyWheel) {
          String wheelViewonlyName = null;
          try {
            wheelViewonlyName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.viewonly.group");
            if (this.wheelSession == null) {
              this.wheelSession = GrouperSession.start(SubjectFinder.findRootSubject(), false);
            }
            this.wheelViewonlyGroup = GroupFinder.findByName(
                this.wheelSession,
                wheelViewonlyName, GrouperStartup.isFinishedStartupSuccessfully()
                );
          } catch (Exception e) {
    
            String error = "Initialisation error with wheel viewonly group name '" + wheelViewonlyName
                + "': " + e.getClass().getSimpleName()
                + "\n" + ExceptionUtils.getFullStackTrace(e);
    
            //only log this once as error, dont log if checking config
            if (!loggedWheelViewonlyGroupMissing && !GrouperCheckConfig.inCheckConfig) {
              //OK, so wheel group does not exist. Not fatal...
              LOG.error(error);
              loggedWheelViewonlyGroupMissing = true;
            } else {
              LOG.debug(error);
            }
            this.useViewonlyWheel = false;
          }
        }
      }

      {
        // TODO 20070816 this is ugly
        String useWheelReadonlyString = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.readonly.use");
        this.useReadonlyWheel = Boolean.valueOf(useWheelReadonlyString).booleanValue();
        // TODO 20070816 and this is even worse
        if (this.useReadonlyWheel) {
          String wheelReadonlyName = null;
          try {
            wheelReadonlyName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.readonly.group");
            if (this.wheelSession == null) {
              this.wheelSession = GrouperSession.start(SubjectFinder.findRootSubject(), false);
            }
            this.wheelReadonlyGroup = GroupFinder.findByName(
                this.wheelSession,
                wheelReadonlyName, GrouperStartup.isFinishedStartupSuccessfully()
                );
          } catch (Exception e) {
    
            String error = "Initialisation error with wheel readonly group name '" + wheelReadonlyName
                + "': " + e.getClass().getSimpleName()
                + "\n" + ExceptionUtils.getFullStackTrace(e);
    
            //only log this once as error, dont log if checking config
            if (!loggedWheelReadonlyGroupMissing && !GrouperCheckConfig.inCheckConfig) {
              //OK, so wheel group does not exist. Not fatal...
              LOG.error(error);
              loggedWheelReadonlyGroupMissing = true;
            } else {
              LOG.debug(error);
            }
            this.useReadonlyWheel = false;
          }
        }
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
   * Put boolean into cache for viewonly <code>isWheelMember(...)</code>.
   * @param subj 
   * @param rv 
   */
  private void putInHasViewonlyPrivilegeCache(Subject subj, Boolean rv) {
    WheelCache.putInViewonlyHasPrivilegeCache(subj, rv);
  }

  /**
   * Put boolean into cache for readonly <code>isWheelMember(...)</code>.
   * @param subj 
   * @param rv 
   */
  private void putInHasReadonlyPrivilegeCache(Subject subj, Boolean rv) {
    WheelCache.putInReadonlyHasPrivilegeCache(subj, rv);
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
              return WheelNamingResolver.this.wheelGroup != null 
                  && WheelNamingResolver.this.wheelGroup.hasMember(subj);
            }
                    });

      putInHasPrivilegeCache(subj, rv);
    }
    return rv;
  }

  
  /**
   * Put boolean into cache for viewonly <code>isWheelMember(...)</code>.
   * @param subj 
   * @return  if wheel member
   */
  private boolean isWheelViewonlyMember(final Subject subj) {
    Boolean rv = getFromIsWheelViewonlyMemberCache(subj);
    if (rv == null) {

      rv = (Boolean) GrouperSession.callbackGrouperSession(this.wheelSession,
          new GrouperSessionHandler() {

            public Object callback(GrouperSession grouperSession)
                throws GrouperSessionException {
              return WheelNamingResolver.this.wheelViewonlyGroup != null 
                  && WheelNamingResolver.this.wheelViewonlyGroup.hasMember(subj);
            }
                    });

      putInHasViewonlyPrivilegeCache(subj, rv);
    }
    return rv;
  }

  /**
   * Put boolean into cache for readonly <code>isWheelMember(...)</code>.
   * @param subj 
   * @return  if wheel member
   */
  private boolean isWheelReadonlyMember(final Subject subj) {
    Boolean rv = getFromIsWheelReadonlyMemberCache(subj);
    if (rv == null) {

      rv = (Boolean) GrouperSession.callbackGrouperSession(this.wheelSession,
          new GrouperSessionHandler() {

            public Object callback(GrouperSession grouperSession)
                throws GrouperSessionException {
              return WheelNamingResolver.this.wheelReadonlyGroup != null 
                  && WheelNamingResolver.this.wheelReadonlyGroup.hasMember(subj);
            }
                    });

      putInHasReadonlyPrivilegeCache(subj, rv);
    }
    return rv;
  }

  /**
   * 
   * @param subject
   * @return true if this is wheel and if using wheel viewonly
   */
  private boolean isAndUseWheelViewonly(Subject subject) {
    if (this.getGrouperSession().isConsiderIfWheelMember()) {
      if (this.useViewonlyWheel) {
        if (isWheelViewonlyMember(subject)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 
   * @param subject
   * @return true if this is wheel and if using wheel readonly
   */
  private boolean isAndUseWheelReadonly(Subject subject) {
    if (this.getGrouperSession().isConsiderIfWheelMember()) {
      if (this.useReadonlyWheel) {
        if (isWheelReadonlyMember(subject)) {
          return true;
        }
      }
    }
    return false;
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
    if (this.isAndUseWheelReadonly(subject)) {
      if (NamingPrivilege.STEM_ATTR_READ.equals(privilege)) {
        return true;
      }
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
    if (this.isAndUseWheelReadonly(subject) && privInSet != null 
        && (privInSet.contains(NamingPrivilege.STEM_ATTR_READ))) {
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
    if (this.isAndUseWheelReadonly(subject) && privInSet != null 
        && (privInSet.contains(NamingPrivilege.STEM_ATTR_READ))) {
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
   * Retrieve boolean from cache for viewonly <code>isWheelMember(...)</code>.
   * @param subj 
   * @return  Cached return value or null.
   */
  private Boolean getFromIsWheelViewonlyMemberCache(Subject subj) {
    return WheelCache.getFromIsWheelViewonlyMemberCache(subj);
  }

  /**
   * Retrieve boolean from cache for readonly <code>isWheelMember(...)</code>.
   * @param subj 
   * @return  Cached return value or null.
   */
  private Boolean getFromIsWheelReadonlyMemberCache(Subject subj) {
    return WheelCache.getFromIsWheelReadonlyMemberCache(subj);
  }

}
