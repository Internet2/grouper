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

import net.sf.ehcache.Element;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.cache.EhcacheController;
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
 * @version $Id: WheelAttrDefResolver.java,v 1.1 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public class WheelAttrDefResolver extends AttributeDefResolverDecorator {

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#stop()
   */
  public void stop() {
    this.cc.stop();
    super.getDecoratedResolver().stop();
  }

  /** if use wheel group */
  private boolean useWheel = false;

  /** wheel group */
  private Group wheelGroup;

  /** 2007-11-02 Gary Brown
   * Provide cache for wheel group members
   * Profiling showed lots of time rechecking memberships */
  public static final String CACHE_IS_WHEEL_MEMBER = WheelAttrDefResolver.class.getName()
      + ".isWheelMember";

  /** cache controller */
  private EhcacheController cc;

  /** wheel session */
  private GrouperSession wheelSession = null;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(WheelAttrDefResolver.class);

  /** only log this once... */
  private static boolean loggedWheelGroupMissing = false;

  /**
   * @param resolver resolver
   * @since   1.2.1
   */
  public WheelAttrDefResolver(AttributeDefResolver resolver) {
    super(resolver);
    this.cc = new EhcacheController();
    // TODO 20070816 this is ugly
    String useWheelString = GrouperConfig.getProperty(GrouperConfig.PROP_USE_WHEEL_GROUP);
    this.useWheel = Boolean.valueOf(useWheelString).booleanValue();
    // TODO 20070816 and this is even worse
    if (this.useWheel) {
      String wheelName = null;
      try {
        wheelName = GrouperConfig.getProperty(GrouperConfig.PROP_WHEEL_GROUP);
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
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#getPrivileges(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject)
   */
  public Set<AttributeDefPrivilege> getPrivileges(AttributeDef attributeDef, Subject subject)
      throws IllegalArgumentException {
    //Get any user privs
    Set<AttributeDefPrivilege> accessPrivs = super.getDecoratedResolver().getPrivileges(attributeDef,
        subject);

    //Add any due to Wheel.
    if (this.isAndUseWheel(subject)) {
      Set<Privilege> privs = Privilege.getAccessPrivs();
      AttributeDefPrivilege ap = null;
      for (Privilege p : privs) {
        //Not happy about the klass but will do for now in the absence of a GrouperSession
        if (!p.equals(AttributeDefPrivilege.ATTR_OPTIN) && !p.equals(AttributeDefPrivilege.ATTR_OPTOUT)) {
          ap = new AttributeDefPrivilege(attributeDef, subject, SubjectFinder.findRootSubject(),
              p, GrouperConfig.getProperty("privileges.attributeDef.interface"), false, null);
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
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#hasPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public boolean hasPrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    //Admin incorporates other privileges - except optin /optout
    //Which we don't want to assume
    if (this.isAndUseWheel(subject)) {
      if (!AttributeDefPrivilege.ATTR_OPTOUT.equals(privilege)
          && !AttributeDefPrivilege.ATTR_OPTIN.equals(privilege)) {
        return true;

      }
    }
    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.hasPrivilege(attributeDef, subject, privilege);
  }

  /**
   * Retrieve boolean from cache for <code>isWheelMember(...)</code>.
   * @param subj 
   * @return  Cached return value or null.
   * @since   1.2.1
   */
  private Boolean getFromIsWheelMemberCache(Subject subj) {
    Element el = this.cc.getCache(CACHE_IS_WHEEL_MEMBER).get(subj);
    if (el != null) {
      return (Boolean) el.getObjectValue();
    }
    return null;
  }

  /**
   * Put boolean into cache for <code>isWheelMember(...)</code>.
   * @param subj 
   * @param rv 
   * @since   1.2.1
   */
  private void putInHasPrivilegeCache(Subject subj, Boolean rv) {
    this.cc.getCache(CACHE_IS_WHEEL_MEMBER).put(new Element(subj, rv));
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
              return WheelAttrDefResolver.this.wheelGroup.hasMember(subj);
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
    this.cc.flushCache();
    super.getDecoratedResolver().flushCache();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#postHqlFilterAttrDefs(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<AttributeDef> postHqlFilterAttrDefs(Set<AttributeDef> attributeDefs, Subject subject,
      Set<Privilege> privInSet) {
    //Wheel can see all groups
    if (this.isAndUseWheel(subject)) {
      return attributeDefs;
    }
    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.postHqlFilterAttrDefs(attributeDefs, subject, privInSet);

  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#hqlFilterAttrDefsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterAttrDefsWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String attributeDefColumn, Set<Privilege> privInSet) {
    //Wheel can see all groups
    if (this.isAndUseWheel(subject)) {
      return false;
    }
    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.hqlFilterAttrDefsWhereClause(subject, hqlQuery, hql,
        attributeDefColumn, privInSet);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#postHqlFilterAttrDefs(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<AttributeDef> postHqlFilterAttrDefs(Subject subject,
      Set<AttributeDef> attributeDefs) {

    //Wheel can see all memberships
    if (this.isAndUseWheel(subject)) {
      return attributeDefs;
    }
    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.postHqlFilterAttrDefs(subject, attributeDefs);
  }

}
