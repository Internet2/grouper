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

package edu.internet2.middleware.grouper.privs;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.subj.SubjectBean;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/** 
 * Privilege helper class.
 * <p>TODO 20070823 Relocate these methods once I figure out the best home for them.</p>
 * @author  blair christensen.
 * @version $Id: PrivilegeHelper.java,v 1.12 2009-09-28 05:06:46 mchyzer Exp $
 * @since   1.2.1
 */
public class PrivilegeHelper {

  /**
   * see if a group has an immediate privilege
   * @param group
   * @param subject
   * @param privilege
   * @return true if has immediate privilege, false if not
   */
  public static boolean hasImmediatePrivilege(Group group, Subject subject, Privilege privilege) {
    
    try {
      MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), group, subject, privilege.getField(), true);
      return true;
    } catch (MembershipNotFoundException eMNF) {    
      
    }
    return false;
  }

  /**
   * flush all privilege caches
   */
  public static void flushCache() {
    WheelCache.flush();
    EhcacheController.ehcacheController().getCache(CachingAccessResolver.CACHE_HASPRIV).flush();
    EhcacheController.ehcacheController().getCache(CachingNamingResolver.CACHE_HASPRIV).flush();
    EhcacheController.ehcacheController().getCache(CachingAttrDefResolver.CACHE_HASPRIV).flush();
  }
  
  /**
   * resolve subjects in one batch
   * @param grouperPrivileges
   * @param resolveAllAlways true to always resolve all no matter how many, false
   * if there are more than 2000 or however many (e.g. for UI)
   */
  public static void resolveSubjects(Collection<GrouperPrivilege> grouperPrivileges, boolean resolveAllAlways) {
    
    if (GrouperUtil.length(grouperPrivileges) == 0) {
      return;
    }
    
    //find the ones which are Lazy
    List<GrouperPrivilege> privilegesNeedResolved = new ArrayList<GrouperPrivilege>();
    for (GrouperPrivilege grouperPrivilege : grouperPrivileges) {
      Subject subject = grouperPrivilege.getSubject();
      if (subject instanceof LazySubject) {
        privilegesNeedResolved.add(grouperPrivilege);
      }
    }
    
    if (GrouperUtil.length(privilegesNeedResolved) == 0) {
      return;
    }

    //if there are more than 2000, forget it, leave them lazy
    if (!resolveAllAlways 
        && GrouperUtil.length(privilegesNeedResolved) > GrouperConfig.retrieveConfig().propertyValueInt("memberLengthAboveWhichDontResolveBatch", 2000)) {
      return;
    }
    
    //lets resolve these
    Map<SubjectBean, GrouperPrivilege> subjectBeanToGrouperPrivilege = new HashMap<SubjectBean, GrouperPrivilege>();
    Set<SubjectBean> subjectBeans = new HashSet<SubjectBean>();
    
    for (GrouperPrivilege grouperPrivilege : privilegesNeedResolved) {

      Subject subject = grouperPrivilege.getSubject();
      
      SubjectBean subjectBean = new SubjectBean(subject.getId(), subject.getSourceId());
      subjectBeans.add(subjectBean);
      subjectBeanToGrouperPrivilege.put(subjectBean, grouperPrivilege);
      
    }
    
    //resolve all members
    Map<SubjectBean, Subject> subjectBeanToSubject = SubjectFinder.findBySubjectBeans(subjectBeans);
    
    for (SubjectBean subjectBean : subjectBeanToSubject.keySet()) {
      Subject subject = subjectBeanToSubject.get(subjectBean);
      GrouperPrivilege grouperPrivilege = subjectBeanToGrouperPrivilege.get(subjectBean);
      grouperPrivilege.internalSetSubject(subject);
    }
  }

  /**
   * @param s 
   * @param g 
   * @param subj 
   * @return admin
   * @since   1.2.1
   */
  public static boolean canAdmin(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    AccessResolver accessResolver = s.getAccessResolver();
    //System.out.println(accessResolver.getClass().getName());
    //validatingAccessResolver
    return accessResolver.hasPrivilege(g, subj, AccessPrivilege.ADMIN);
  } 

  /**
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @return admin
   */
  public static boolean canAttrAdmin(GrouperSession s, AttributeDef attributeDef, Subject subj) {
    AttributeDefResolver attributeDefResolver = s.getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_ADMIN);
  } 

  /**
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @return admin
   */
  public static boolean canAttrRead(GrouperSession s, AttributeDef attributeDef, Subject subj) {
    AttributeDefResolver attributeDefResolver = s.getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_READ)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_ADMIN);
  } 

  /**
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @return admin
   */
  public static boolean canAttrView(GrouperSession s, AttributeDef attributeDef, Subject subj) {
    AttributeDefResolver attributeDefResolver = s.getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_VIEW)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_READ)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_UPDATE)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_ADMIN)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_DEF_ATTR_READ)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_OPTIN)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_OPTOUT);
  } 
  
  /**
   * @param s 
   * @param group 
   * @param subj 
   * @return true if allowed
   */
  public static boolean canGroupAttrRead(GrouperSession s, Group group, Subject subj) {
    AccessResolver resolver = s.getAccessResolver();
    return resolver.hasPrivilege(group, subj, AccessPrivilege.GROUP_ATTR_READ)
      || resolver.hasPrivilege(group, subj, AccessPrivilege.ADMIN);
  } 
  
  /**
   * @param s 
   * @param group 
   * @param subj 
   * @return true if allowed
   */
  public static boolean canGroupAttrUpdate(GrouperSession s, Group group, Subject subj) {
    AccessResolver resolver = s.getAccessResolver();
    return resolver.hasPrivilege(group, subj, AccessPrivilege.GROUP_ATTR_UPDATE)
      || resolver.hasPrivilege(group, subj, AccessPrivilege.ADMIN);
  } 
  
  /**
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @return true if allowed
   */
  public static boolean canAttrDefAttrRead(GrouperSession s, AttributeDef attributeDef, Subject subj) {
    AttributeDefResolver resolver = s.getAttributeDefResolver();
    return resolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_DEF_ATTR_READ)
      || resolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_ADMIN);
  } 
  
  /**
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @return true if allowed
   */
  public static boolean canAttrDefAttrUpdate(GrouperSession s, AttributeDef attributeDef, Subject subj) {
    AttributeDefResolver resolver = s.getAttributeDefResolver();
    return resolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE)
      || resolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_ADMIN);
  } 
  
  /**
   * @param s 
   * @param stem 
   * @param subj 
   * @return true if allowed
   */
  public static boolean canStemAttrRead(GrouperSession s, Stem stem, Subject subj) {
    NamingResolver resolver = s.getNamingResolver();
    return resolver.hasPrivilege(stem, subj, NamingPrivilege.STEM_ATTR_READ)
        || resolver.hasPrivilege(stem, subj, NamingPrivilege.STEM_ADMIN);
  } 
  
  /**
   * @param s 
   * @param stem 
   * @param subj 
   * @return true if allowed
   */
  public static boolean canStemAttrUpdate(GrouperSession s, Stem stem, Subject subj) {
    NamingResolver resolver = s.getNamingResolver();
    return resolver.hasPrivilege(stem, subj, NamingPrivilege.STEM_ATTR_UPDATE)
        || resolver.hasPrivilege(stem, subj, NamingPrivilege.STEM_ADMIN);
  } 

  /**
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @return admin
   */
  public static boolean canAttrUpdate(GrouperSession s, AttributeDef attributeDef, Subject subj) {
    AttributeDefResolver attributeDefResolver = s.getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_UPDATE)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_ADMIN);
  } 

  /**
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @return admin
   */
  public static boolean canAttrOptin(GrouperSession s, AttributeDef attributeDef, Subject subj) {
    AttributeDefResolver attributeDefResolver = s.getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_OPTIN)
        || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_ADMIN)
        || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_UPDATE);
  } 

  /**
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @return admin
   */
  public static boolean canAttrOptout(GrouperSession s, AttributeDef attributeDef, Subject subj) {
    AttributeDefResolver attributeDefResolver = s.getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_OPTOUT)
        || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_ADMIN)
        || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_UPDATE);
  }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param ns 
   * @param subj 
   * @return can create
   * @since   1.2.1
   */
  public static boolean canCreate(GrouperSession s, Stem ns, Subject subj) {
    // TODO 20070820 deprecate
    // TODO 20070820 perform query for all privs and compare internally
    return s.getNamingResolver().hasPrivilege(ns, subj, NamingPrivilege.CREATE)
        || s.getNamingResolver().hasPrivilege(ns, subj, NamingPrivilege.STEM_ADMIN);
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param g 
   * @param subj 
   * @return can
   * @since   1.2.1
   */
  public static boolean canOptin(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.OPTIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.UPDATE)
    )
    {
      return true;
    }
    return false;
  } 

  /**
   * 
   * @param s
   * @param stem
   * @param subj
   * @param privInSet
   * @return if has privilege
   */
  public static boolean hasPrivilege(GrouperSession s, Stem stem, Subject subj, Set<Privilege> privInSet) {
    
    for (Privilege privilege : privInSet) {
      if (s.getNamingResolver().hasPrivilege(stem, subj, privilege)) {
        return true;
      }
    }
    return false;
  } 

  /**
   * 
   * @param s
   * @param g
   * @param subj
   * @param privInSet
   * @return if has privilege
   */
  public static boolean hasPrivilege(GrouperSession s, Group g, Subject subj, Set<Privilege> privInSet) {
    
    for (Privilege privilege : privInSet) {
      if (s.getAccessResolver().hasPrivilege(g, subj, privilege)) {
      return true;
    }
    }
    return false;
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param g 
   * @param subj 
   * @return  can optout
   * @since   1.2.1
   */
  public static boolean canOptout(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.OPTOUT)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.UPDATE)
    )
    {
      return true;
    }
    return false; 
  }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param g 
   * @param subj 
   * @return  can read
   * @since   1.2.1
   */
  public static boolean canRead(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.READ)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
    )
    {
      return true;
    }
    return false; 
   }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param ns 
   * @param subj 
   * @return can stem
   * @since   1.2.1
   */
  public static boolean canStem(Stem ns, Subject subj) {
    // TODO 20070820 deprecate
    // TODO 20070820 perform query for all privs and compare internally
    return canStemAdmin(ns, subj);
  } 
  
  /**
   * @param ns
   * @param subj
   * @return can stem admin
   */
  public static boolean canStemAdmin(Stem ns, Subject subj) {
    return GrouperSession.staticGrouperSession().getNamingResolver().hasPrivilege(ns, subj, NamingPrivilege.STEM_ADMIN);
  } 
  
  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param ns 
   * @param subj 
   * @return can stem
   * @since   1.2.1
   */
  public static boolean canStem(GrouperSession s, Stem ns, Subject subj) {
    // TODO 20070820 deprecate
    // TODO 20070820 perform query for all privs and compare internally
    return canStemAdmin(s, ns, subj);
  } 
  
  /**
   * @param s
   * @param ns
   * @param subj
   * @return can stem admin
   */
  public static boolean canStemAdmin(GrouperSession s, Stem ns, Subject subj) {
    return s.getNamingResolver().hasPrivilege(ns, subj, NamingPrivilege.STEM_ADMIN);
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param g 
   * @param subj 
   * @return can update
   * @since   1.2.1
   */
  public static boolean canUpdate(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.UPDATE)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
    )
    {
      return true;
    }
    return false; 
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param g 
   * @param subj 
   * @return can view
   * @since   1.2.1
   */
  public static boolean canView(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    //note, no need for GrouperSession inverse of control
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.VIEW)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.READ)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.UPDATE)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.GROUP_ATTR_READ)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.GROUP_ATTR_UPDATE)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.OPTIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.OPTOUT)
    )
    {
      return true;
    }
    return false; 
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param candidates 
   * @return can view
   * @since   1.2.1
   */
  public static Set canViewGroups(GrouperSession s, Set candidates) {
    //note, no need for GrouperSession inverse of control
    Set       groups  = new LinkedHashSet();
    Group     g;
    Iterator  it      = candidates.iterator();
    while (it.hasNext()) {
      Object obj = it.next();
      g = (Group)obj;
      if ( canView( s, g, s.getSubject() ) ) {
        groups.add(g);
      }
    }
    return groups;
  } 

  /**
   * 
   * @param grouperSession 
   * @param membership 
   * @return true if ok, false if not
   */
  public static boolean canViewMembership(GrouperSession grouperSession, Membership membership) {
    try {
      //2007-10-17: Gary Brown
      //https://bugs.internet2.edu/jira/browse/GRP-38
        //Ah! Memberships for stem privileges are passed through here also
      //The conditional makes sense - except it was wrong  -and didn't cope with stem privileges
      if ( FieldType.NAMING.equals( membership.getList().getType() ) ) {
        dispatch( grouperSession, membership.getStem(), grouperSession.getSubject(), membership.getList().getReadPriv() );
        return true;
      } else if ( FieldType.ACCESS.equals( membership.getList().getType() ) ) {
        dispatch( grouperSession, membership.getOwnerGroup(), grouperSession.getSubject(), membership.getList().getReadPriv() );
        return true;
      } else if (FieldType.NAMING.equals( membership.getList().getType() ) ) {
        
        dispatch( grouperSession, membership.getAttributeDef(), grouperSession.getSubject(), membership.getList().getReadPriv() );
        return true;

      } else if (FieldType.LIST.equals( membership.getList().getType() ) ) {
        
        //am I supposed to see what the read privilege is for the field, or just look at read???
        if (canRead(grouperSession, membership.getOwnerGroup(), grouperSession.getSubject())) {
          return true;
        }
        return false;
      } else {
        throw new RuntimeException("Invalid field type: " + membership.getList().getType());
      }
      
    } catch (InsufficientPrivilegeException e) {
      //ignore, not allowed, dont add
      return false;
    }
  }
  
  /**
   * @param grouperSession 
   * @param inputMemberships 
   * @return filtered memberships
   */
  public static Set<Membership> canViewMemberships(GrouperSession grouperSession, Collection<Membership> inputMemberships) {
    
    if (inputMemberships == null) {
      return null;
    }
    
    //make sure all groups are prepopulated
    Membership.retrieveGroups(inputMemberships);
    
    //note, no need for GrouperSession inverse of control
    Set<Membership> memberships = new LinkedHashSet<Membership>();
    Membership membership;
    Iterator<Membership> iterator = inputMemberships.iterator();
    while ( iterator.hasNext() ) {
      membership = iterator.next() ;
      if (canViewMembership(grouperSession, membership)) {
        memberships.add(membership);
      }
    }
    return memberships;
  } 


  /**
   * @param grouperSession 
   * @param group 
   * @param field 
   * @return true or false
   */
  public static boolean canViewMembers(GrouperSession grouperSession, Group group, Field field) {
    try {
      dispatch( grouperSession, group, grouperSession.getSubject(), field.getReadPriv() );
      return true;
    } catch (InsufficientPrivilegeException e) {
      return false;
    } catch (SchemaException e) {
      throw new RuntimeException("Problem viewing members: " 
          + (grouperSession == null ? null : GrouperUtil.subjectToString(grouperSession.getSubject())) 
          + ", " + (group == null ? null : group.getName())
          + ", " + (field == null ? null : field.getName()), e);
  } 
  } 

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(PrivilegeHelper.class);

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param g 
   * @param subj 
   * @param priv 
   * @throws InsufficientPrivilegeException 
   * @throws SchemaException 
   * @SINCE   1.2.1
   */
  public static void dispatch(GrouperSession s, Group g, Subject subj, Privilege priv)
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // TODO 20070823 this is ugly
    boolean rv  = false;
    String  msg = GrouperConfig.EMPTY_STRING; 

    if ( !Privilege.isAccess(priv) ) {
      throw new SchemaException("access privileges only apply to groups");
    }

    if      (priv.equals(AccessPrivilege.ADMIN))  {
      rv = PrivilegeHelper.canAdmin(s, g, subj);
      if (!rv) {
        msg = E.CANNOT_ADMIN;
      }
    }
    else if (priv.equals(AccessPrivilege.OPTIN))  {
      rv = PrivilegeHelper.canOptin(s, g, subj);
      if (!rv) {
        msg = E.CANNOT_OPTIN;
      }
    }
    else if (priv.equals(AccessPrivilege.OPTOUT)) {
      rv = PrivilegeHelper.canOptout(s, g, subj);
      if (!rv) {
        msg = E.CANNOT_OPTOUT;
      }
    }
    else if (priv.equals(AccessPrivilege.READ))   {
      rv = PrivilegeHelper.canRead(s, g, subj);
      if (!rv) {
        msg = "subject " + subj.getId() + " cannot READ group: " + g.getName();
      }
    }
    else if (priv.equals(AccessPrivilege.GROUP_ATTR_READ))   {
      rv = PrivilegeHelper.canGroupAttrRead(s, g, subj);
      if (!rv) {
        msg = "subject " + subj.getId() + " cannot GROUP_ATTR_READ group: " + g.getName();
      }
    }
    else if (priv.equals(AccessPrivilege.GROUP_ATTR_UPDATE))   {
      rv = PrivilegeHelper.canGroupAttrUpdate(s, g, subj);
      if (!rv) {
        msg = "subject " + subj.getId() + " cannot GROUP_ATTR_UPDATE group: " + g.getName();
      }
    }
    else if (priv.equals(AccessPrivilege.VIEW))   {
      rv = PrivilegeHelper.canView( s, g, subj );
      if (!rv) {
        msg = E.CANNOT_VIEW;
      }
    }
    else if (priv.equals(AccessPrivilege.UPDATE)) {
      rv = PrivilegeHelper.canUpdate(s, g, subj);
      if (!rv) {
        msg = E.CANNOT_UPDATE;
      }
    }
    else if (priv.equals(AccessPrivilege.SYSTEM))  {
      msg = E.SYSTEM_MAINTAINED + priv;
    }
    else {
      throw new SchemaException(E.UNKNOWN_PRIVILEGE + priv);
    }
    if (!rv) {
      throw new InsufficientPrivilegeException(msg);
    }
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param ns 
   * @param subj 
   * @param priv 
   * @throws InsufficientPrivilegeException 
   * @throws SchemaException 
   * @SINCE   1.2.1
   */
  public static void dispatch(GrouperSession s, Stem ns, Subject subj, Privilege priv)
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // TODO 20070823 this is ugly
    boolean rv  = false;
    String  msg = GrouperConfig.EMPTY_STRING; 

    if      ( !Privilege.isNaming(priv) ) {
      throw new SchemaException("naming privileges only apply to stems");
    }

    if      (priv.equals(NamingPrivilege.CREATE)) { 
      rv = PrivilegeHelper.canCreate(s, ns,  subj);
      if (!rv) {
        msg = E.CANNOT_CREATE;
      }
    }
    else if (priv.equals(NamingPrivilege.STEM) || priv.equals(NamingPrivilege.STEM_ADMIN))   {
      rv = PrivilegeHelper.canStemAdmin(ns, subj);
      if (!rv) {
        msg = E.CANNOT_STEM_ADMIN;
      }
    }
    else if (priv.equals(NamingPrivilege.STEM_ATTR_READ))   {
      rv = PrivilegeHelper.canStemAttrRead(s, ns, subj);
      if (!rv) {
        msg = "subject " + subj.getId() + " cannot STEM_ATTR_READ stem: " + ns.getName();
      }
    }
    else if (priv.equals(NamingPrivilege.STEM_ATTR_UPDATE))   {
      rv = PrivilegeHelper.canStemAttrUpdate(s, ns, subj);
      if (!rv) {
        msg = "subject " + subj.getId() + " cannot STEM_ATTR_UPDATE stem: " + ns.getName();
      }
    }
    else {
      throw new SchemaException(E.UNKNOWN_PRIVILEGE + priv);
    }
    if (!rv) {
      throw new InsufficientPrivilegeException(msg);
    }
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @param priv 
   * @throws InsufficientPrivilegeException 
   * @throws SchemaException 
   */
  public static void dispatch(GrouperSession s, AttributeDef attributeDef, Subject subj, Privilege priv)
    throws  InsufficientPrivilegeException, SchemaException  {
    // TODO 20070823 this is ugly
    boolean rv  = false;
    String  msg = GrouperConfig.EMPTY_STRING; 

    if ( !Privilege.isAttributeDef(priv) ) {
      throw new SchemaException("attributeDef privileges only apply to attributeDefs");
    }

    if      (priv.equals(AttributeDefPrivilege.ATTR_ADMIN)) { 
      rv = PrivilegeHelper.canAttrAdmin(s, attributeDef,  subj);
      if (!rv) {
        msg = E.CANNOT_ATTR_ADMIN;
      }
    } else if (priv.equals(AttributeDefPrivilege.ATTR_OPTIN))   {
      rv = PrivilegeHelper.canAttrOptin(s, attributeDef, subj);
      if (!rv) {
        msg = E.CANNOT_ATTR_OPTIN;
      }
    } else if (priv.equals(AttributeDefPrivilege.ATTR_OPTOUT))   {
      rv = PrivilegeHelper.canAttrOptout(s, attributeDef, subj);
      if (!rv) {
        msg = E.CANNOT_ATTR_OPTOUT;
      }
    } else if (priv.equals(AttributeDefPrivilege.ATTR_READ))   {
      rv = PrivilegeHelper.canAttrRead(s, attributeDef, subj);
      if (!rv) {
        msg = E.CANNOT_ATTR_READ;
      }
    } else if (priv.equals(AttributeDefPrivilege.ATTR_UPDATE))   {
      rv = PrivilegeHelper.canAttrUpdate(s, attributeDef, subj);
      if (!rv) {
        msg = E.CANNOT_ATTR_UPDATE;
      }
    } else if (priv.equals(AttributeDefPrivilege.ATTR_DEF_ATTR_READ))   {
      rv = PrivilegeHelper.canAttrDefAttrRead(s, attributeDef, subj);
      if (!rv) {
        msg = "subject " + subj.getId() + " cannot ATTR_DEF_ATTR_READ stem: " + attributeDef.getName();
      }
    } else if (priv.equals(AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE))   {
      rv = PrivilegeHelper.canAttrDefAttrUpdate(s, attributeDef, subj);
      if (!rv) {
        msg = "subject " + subj.getId() + " cannot ATTR_DEF_ATTR_UPDATE stem: " + attributeDef.getName();
      }
    } else if (priv.equals(AttributeDefPrivilege.ATTR_VIEW))   {
      rv = PrivilegeHelper.canAttrView(s, attributeDef, subj);
      if (!rv) {
        msg = E.CANNOT_ATTR_VIEW;
      }
    } else {
      throw new SchemaException(E.UNKNOWN_PRIVILEGE + priv);
    }
    if (!rv) {
      throw new InsufficientPrivilegeException(msg + ", attributeDef: " 
          + (attributeDef == null ? null : attributeDef.getName()) 
          + ", " + GrouperUtil.subjectToString(subj));
    }
  } 

  /**
   * TODO 20070824 add tests
   * @param privileges 
   * @return  Given an array of privileges return an array of access privileges.
   * @since   1.2.1
   */
  public static Privilege[] getAccessPrivileges(Privilege[] privileges) {
    Set<Privilege> accessPrivs = new LinkedHashSet();
    for ( Privilege priv : privileges ) {
      if ( Privilege.isAccess(priv) ) {
        accessPrivs.add(priv);
      }
    } 
    Privilege[] template = {};
    return accessPrivs.toArray(template);
  }

  /**
   * TODO 20070824 add tests
   * @param privileges 
   * @return  Given an array of privileges return an array of access privileges.
   * @since   1.2.1
   */
  public static Privilege[] getAttributeDefPrivileges(Privilege[] privileges) {
    Set<Privilege> attributeDefPrivs = new LinkedHashSet();
    for ( Privilege priv : privileges ) {
      if ( Privilege.isAttributeDef(priv) ) {
        attributeDefPrivs.add(priv);
      }
    } 
    Privilege[] template = {};
    return attributeDefPrivs.toArray(template);
  }

  /**
   * TODO 20070824 add tests
   * @param privileges 
   * @return  Given an array of privileges return an array of naming privileges.
   * @since   1.2.1
   */
  public static Privilege[] getNamingPrivileges(Privilege[] privileges) {
    Set<Privilege> namingPrivs = new LinkedHashSet();
    for ( Privilege priv : privileges ) {
      if ( Privilege.isNaming(priv) ) {
        namingPrivs.add(priv);
      }
    } 
    Privilege[] template = {};
    return namingPrivs.toArray(template);
  }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @return is root
   * @SINCE   1.2.1
   */
  public static boolean isRoot(GrouperSession s) {
    // TODO 20070823 this is ugly
    boolean rv = false;
    if ( SubjectHelper.eq( s.getSubject(), SubjectFinder.findRootSubject() ) ) {
      rv = true;
    }
    else {
      rv = isWheel(s);
    }
    return rv;
  }

  /**
   * see if system subject
   * @param subject 
   * @return true if grouper system
   */
  public static boolean isSystemSubject(Subject subject) {
    return SubjectHelper.eq( subject, SubjectFinder.findRootSubject() );
  }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @return  is wheel
   * @SINCE   1.2.1
   */
  public static boolean isWheel(GrouperSession s) {
    // TODO 20070823 this is ugly
    boolean rv = false;
    
    if (s.isConsiderIfWheelMember() == false) {
      return false;
    }
    
    if ( Boolean.valueOf( GrouperConfig.retrieveConfig().propertyValueString( GrouperConfig.PROP_USE_WHEEL_GROUP ) ).booleanValue() ) {
      String name = GrouperConfig.retrieveConfig().propertyValueString( GrouperConfig.PROP_WHEEL_GROUP );
      try {
        // goodbye, performance
        Group wheel = GroupFinder.findByName( s.internal_getRootSession(), name, true );
        rv          = wheel.hasMember( s.getSubject() );
      }
      catch (GroupNotFoundException eGNF) {
        // wheel group not found. oh well!
        LOG.error( E.NO_WHEEL_GROUP + name );
      }
    } 
    return rv;
  }

  /**
   * see if a subject is wheel or root
   * @param subject 
   * @return true or false
   */
  public static boolean isWheelOrRoot(Subject subject) {
    if (SubjectHelper.eq( subject, SubjectFinder.findRootSubject() )) {
      return true;
    }
    
    if (GrouperSession.staticGrouperSession().isConsiderIfWheelMember() == false) {
      return false;
    }
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean(GrouperConfig.PROP_USE_WHEEL_GROUP, false)) {
      String name = GrouperConfig.retrieveConfig().propertyValueString( GrouperConfig.PROP_WHEEL_GROUP );
      try {
        Group wheel = GroupFinder.findByName( GrouperSession.staticGrouperSession().internal_getRootSession(), name, true );
        return wheel.hasMember(subject);
      } catch (GroupNotFoundException gnfe) {
        throw new GrouperException("Cant find wheel group: " + name, gnfe);
      }
    }
    return false;
  } 
  
  /**
   * Is this user allowed to move stems?
   * @param subject 
   * @return boolean
   */
  public static boolean canMoveStems(Subject subject) {
    String allowedGroupName = GrouperConfig.retrieveConfig().propertyValueString("security.stem.groupAllowedToMoveStem");
    if (StringUtils.isNotBlank(allowedGroupName) && !isWheelOrRoot(subject)) {

      Group allowedGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession()
          .internal_getRootSession(), allowedGroupName, false);
      if (allowedGroup == null || !allowedGroup.hasMember(subject)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Is this user allowed to copy stems?
   * @param subject 
   * @return boolean
   */
  public static boolean canCopyStems(Subject subject) {
    String allowedGroupName = GrouperConfig.retrieveConfig().propertyValueString("security.stem.groupAllowedToCopyStem");
    if (StringUtils.isNotBlank(allowedGroupName) && !isWheelOrRoot(subject)) {

      Group allowedGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession()
          .internal_getRootSession(), allowedGroupName, false);
      if (allowedGroup == null || !allowedGroup.hasMember(subject)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Is this user allowed to rename stems?
   * @param subject 
   * @return boolean
   */
  public static boolean canRenameStems(Subject subject) {
    String allowedGroupName = GrouperConfig.retrieveConfig().propertyValueString("security.stem.groupAllowedToRenameStem");
    if (StringUtils.isNotBlank(allowedGroupName) && !isWheelOrRoot(subject)) {

      Group allowedGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession()
          .internal_getRootSession(), allowedGroupName, false);
      if (allowedGroup == null || !allowedGroup.hasMember(subject)) {
        return false;
      }
    }
    return true;
  }

  /**
   * 
   * @param s
   * @param attributeDef
   * @param subj
   * @param privInSet
   * @return if has privilege
   */
  public static boolean hasPrivilege(GrouperSession s, AttributeDef attributeDef, Subject subj, Set<Privilege> privInSet) {
    
    for (Privilege privilege : privInSet) {
      if (s.getAttributeDefResolver().hasPrivilege(attributeDef, subj, privilege)) {
        return true;
      }
    }
    return false;
  }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param inputAttributeDefs 
   * @return filtered attributeDefs
   * @SINCE   1.2.1
   */
  public static Set<AttributeDef> canViewAttributeDefs(GrouperSession s, Collection<AttributeDef> inputAttributeDefs) {
    
    if (inputAttributeDefs == null) {
      return null;
    }
    
    //note, no need for GrouperSession inverse of control
    Set<AttributeDef>         attrDefs  = new LinkedHashSet<AttributeDef>();
    AttributeDef  attributeDef;
    Iterator<AttributeDef>    it      = inputAttributeDefs.iterator();
    while ( it.hasNext() ) {
      attributeDef = it.next() ;
      try {
      	//2007-10-17: Gary Brown
      	//https://bugs.internet2.edu/jira/browse/GRP-38
          //Ah! Memberships for stem privileges are passed through here also
      	//The conditional makes sense - except it was wrong  -and didn't cope with stem privileges
        dispatch( s, attributeDef, s.getSubject(), AttributeDefPrivilege.ATTR_VIEW );
        attrDefs.add(attributeDef);
        
      } catch (InsufficientPrivilegeException e) {
        //ignore, not allowed, dont add
        continue;
      }
    }
    return attrDefs;
  }

  /**
   * see if the attribute assigns are viewable
   * @param grouperSession 
   * @param attributeAssign
   * @param checkUnderlyingIfAssignmentOnAssignment if deep security check should take place on underlying assignments
   * @return filtered memberships
   * 
   */
  public static boolean canViewAttributeAssign(GrouperSession grouperSession, AttributeAssign attributeAssign, boolean checkUnderlyingIfAssignmentOnAssignment) {
    if (attributeAssign == null) {
      throw new NullPointerException("attribute assign is null");
    }
    
    try {
      
      //first try the attributeDefs
      AttributeDef attributeDef = attributeAssign.getAttributeDef();
      
      dispatch(grouperSession, attributeDef, grouperSession.getSubject(), AttributeDefPrivilege.ATTR_READ);
      
      //now, depending on the assignment, check it out
      AttributeAssignType attributeAssignType = attributeAssign.getAttributeAssignType();
      
      switch (attributeAssignType) {
        case group:
          dispatch(grouperSession, attributeAssign.getOwnerGroup(), grouperSession.getSubject(), AccessPrivilege.GROUP_ATTR_READ);
          break;

        case stem:
          if (!PrivilegeHelper.canStemAttrRead(grouperSession, attributeAssign.getOwnerStem(), grouperSession.getSubject()) &&
              !PrivilegeHelper.canStemAdmin(grouperSession, attributeAssign.getOwnerStem(), grouperSession.getSubject())) {
            return false;
          }
          break;
          
        case member:
          //no need to check member, everyone can edit all members
          break;
          
        case attr_def:
          dispatch(grouperSession, attributeAssign.getOwnerAttributeDef(), grouperSession.getSubject(), AttributeDefPrivilege.ATTR_DEF_ATTR_READ);
          break;
          
        case imm_mem:
          dispatch(grouperSession, attributeAssign.getOwnerImmediateMembership().getOwnerGroup(), grouperSession.getSubject(), AccessPrivilege.READ);
          break;

        case any_mem:
          dispatch(grouperSession, attributeAssign.getOwnerGroup(), grouperSession.getSubject(), AccessPrivilege.READ);
          break;

        case any_mem_asgn:
        case attr_def_asgn:
        case group_asgn:
        case imm_mem_asgn:
        case mem_asgn:
        case stem_asgn:
          if (checkUnderlyingIfAssignmentOnAssignment) {
            AttributeAssign underlyingAssignment = attributeAssign.getOwnerAttributeAssign();
            if (!canViewAttributeAssign(grouperSession, underlyingAssignment, checkUnderlyingIfAssignmentOnAssignment)) {
              return false;
            }
          }
          break;

        default: 
          throw new RuntimeException("Not expecting attributeAssignType: " + attributeAssignType);
        
        
      }
      
      //ok
      return true;

    } catch (InsufficientPrivilegeException e) {
      //ignore, not allowed, dont add
      return false;
    } catch (AttributeDefNotFoundException e) {
      //ignore, not allowed, dont add
      return false;
    }
    
  }

  /**
   * see if the attribute assigns are viewable
   * @param grouperSession 
   * @param inputAttributeAssigns 
   * @param checkUnderlyingIfAssignmentOnAssignment if deep security check should take place on underlying assignments
   * @return filtered memberships
   * 
   */
  public static Set<AttributeAssign> canViewAttributeAssigns(GrouperSession grouperSession, Collection<AttributeAssign> inputAttributeAssigns, boolean checkUnderlyingIfAssignmentOnAssignment) {
    
    if (inputAttributeAssigns == null) {
      return null;
    }
    
    Set<AttributeAssign> attributeAssigns  = new LinkedHashSet<AttributeAssign>();
    
    for (AttributeAssign attributeAssign : inputAttributeAssigns) {
      if (canViewAttributeAssign(grouperSession, attributeAssign, checkUnderlyingIfAssignmentOnAssignment)) {
        attributeAssigns.add(attributeAssign);
      }
    }
    return attributeAssigns;
  }

  /**
   * see if the attribute assigns are viewable
   * @param grouperSession 
   * @param inputPermissionEntries 
   * @return filtered memberships
   * 
   */
  public static Set<PermissionEntry> canViewPermissions(GrouperSession grouperSession, Collection<PermissionEntry> inputPermissionEntries) {
    
    if (inputPermissionEntries == null) {
      return null;
    }
    
    if (isWheelOrRoot(grouperSession.getSubject())) {
      return new LinkedHashSet<PermissionEntry>(inputPermissionEntries);
    }
    
    Set<PermissionEntry> permissionEntries  = new LinkedHashSet<PermissionEntry>();
    
    for (PermissionEntry permissionEntry : inputPermissionEntries) {
      if (!permissionEntry.isActive()) {
        continue;
      }
      
      Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(permissionEntry.getRoleId(), true);
      if (!canGroupAttrRead(grouperSession, group, grouperSession.getSubject())) {
        continue;
      }      
      AttributeDef attributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findById(permissionEntry.getAttributeDefId(), true);
      if (!canAttrRead(grouperSession, attributeDef, grouperSession.getSubject())) {
        continue;
      }
      permissionEntries.add(permissionEntry);
    }
    return permissionEntries;
  }
  
  /**
   * see if the pit attribute assigns are viewable
   * @param grouperSession 
   * @param inputPITAttributeAssigns 
   * @param checkUnderlyingIfAssignmentOnAssignment if deep security check should take place on underlying assignments
   * @return filtered pit attribute assignments
   * 
   */
  public static Set<PITAttributeAssign> canViewPITAttributeAssigns(GrouperSession grouperSession, Collection<PITAttributeAssign> inputPITAttributeAssigns, boolean checkUnderlyingIfAssignmentOnAssignment) {
    
    if (inputPITAttributeAssigns == null) {
      return null;
    }
    
    if (isWheelOrRoot(grouperSession.getSubject())) {
      return new LinkedHashSet<PITAttributeAssign>(inputPITAttributeAssigns);
    }
    
    Set<PITAttributeAssign> pitAttributeAssigns  = new LinkedHashSet<PITAttributeAssign>();
    
    for (PITAttributeAssign pitAttributeAssign : inputPITAttributeAssigns) {
      
      if (!pitAttributeAssign.isActive()) {
        continue;
      }
      
      AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(pitAttributeAssign.getSourceId(), true);
      if (canViewAttributeAssign(grouperSession, attributeAssign, checkUnderlyingIfAssignmentOnAssignment)) {
        pitAttributeAssigns.add(pitAttributeAssign);
      }
    }
    
    return pitAttributeAssigns;
  }

  /**
   * see if a stem has an immediate privilege
   * @param stem
   * @param subject
   * @param privilege
   * @return true if has immediate privilege, false if not
   */
  public static boolean hasImmediatePrivilege(Stem stem, Subject subject, Privilege privilege) {
    
    try {
      MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), stem, subject, privilege.getField(), true);
      return true;
    } catch (MembershipNotFoundException eMNF) {    
      
    }
    return false;
  }

  /**
   * see if an attributeDef has an immediate privilege
   * @param attributeDef
   * @param subject
   * @param privilege
   * @return true if has immediate privilege, false if not
   */
  public static boolean hasImmediatePrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege) {
    
    try {
      MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), attributeDef, subject, privilege.getField(), true);
      return true;
    } catch (MembershipNotFoundException eMNF) {    
      
    }
    return false;
  }
}

