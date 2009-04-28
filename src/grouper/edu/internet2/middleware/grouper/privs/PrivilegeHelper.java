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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/** 
 * Privilege helper class.
 * <p>TODO 20070823 Relocate these methods once I figure out the best home for them.</p>
 * @author  blair christensen.
 * @version $Id: PrivilegeHelper.java,v 1.9 2009-04-28 18:45:00 shilen Exp $
 * @since   1.2.1
 */
public class PrivilegeHelper {


  /**
   * TODO 20070823 find a real home for this and/or add tests
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
   * TODO 20070823 find a real home for this and/or add tests
   * @since   1.2.1
   */
  public static boolean canCreate(GrouperSession s, Stem ns, Subject subj) {
    // TODO 20070820 deprecate
    // TODO 20070820 perform query for all privs and compare internally
    return s.getNamingResolver().hasPrivilege(ns, subj, NamingPrivilege.CREATE);
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
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
   * @since   1.2.1
   */
  public static boolean canStem(Stem ns, Subject subj) {
    // TODO 20070820 deprecate
    // TODO 20070820 perform query for all privs and compare internally
    return GrouperSession.staticGrouperSession().getNamingResolver().hasPrivilege(ns, subj, NamingPrivilege.STEM);
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
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
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param inputMemberships 
   * @return filtered memberships
   * @SINCE   1.2.1
   */
  public static Set<Membership> canViewMemberships(GrouperSession s, Collection<Membership> inputMemberships) {
    
    if (inputMemberships == null) {
      return null;
    }
    
    //make sure all groups are prepopulated
    Membership.retrieveGroups(inputMemberships);
    
    //note, no need for GrouperSession inverse of control
    Set<Membership>         mships  = new LinkedHashSet<Membership>();
    Membership  ms;
    Iterator    it      = inputMemberships.iterator();
    while ( it.hasNext() ) {
      ms = (Membership)it.next() ;
      try {
    	//2007-10-17: Gary Brown
    	//https://bugs.internet2.edu/jira/browse/GRP-38
        //Ah! Memberships for stem privileges are passed through here also
    	//The conditional makes sense - except it was wrong  -and didn't cope with stem privileges
        if ( FieldType.NAMING.equals( ms.getList().getType() ) ) {
          dispatch( s, ms.getStem(), s.getSubject(), ms.getList().getReadPriv() );
          mships.add(ms);
        }else{
        	dispatch( s, ms.getGroup(), s.getSubject(), ms.getList().getReadPriv() );
            mships.add(ms);
        }
        
      }
      catch (Exception e) {
        LOG.error("canViewMemberships: " + e.getMessage(), e );
      }
    }
    return mships;
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
   * @SINCE   1.2.1
   */
  public static void dispatch(GrouperSession s, Group g, Subject subj, Privilege priv)
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // TODO 20070823 this is ugly
    boolean rv  = false;
    String  msg = GrouperConfig.EMPTY_STRING; 

    if ( Privilege.isNaming(priv) ) {
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
   * @SINCE   1.2.1
   */
  public static void dispatch(GrouperSession s, Stem ns, Subject subj, Privilege priv)
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // TODO 20070823 this is ugly
    boolean rv  = false;
    String  msg = GrouperConfig.EMPTY_STRING; 

    if      ( Privilege.isAccess(priv) ) {
      throw new SchemaException("naming privileges only apply to stems");
    }

    if      (priv.equals(NamingPrivilege.CREATE)) { 
      rv = PrivilegeHelper.canCreate(s, ns,  subj);
      if (!rv) {
        msg = E.CANNOT_CREATE;
      }
    }
    else if (priv.equals(NamingPrivilege.STEM))   {
      rv = PrivilegeHelper.canStem(ns, subj);
      if (!rv) {
        msg = E.CANNOT_STEM;
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
   * TODO 20070824 add tests
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
   * @SINCE   1.2.1
   */
  public static boolean isWheel(GrouperSession s) {
    // TODO 20070823 this is ugly
    boolean rv = false;
    
    if (s.isConsiderIfWheelMember() == false) {
      return false;
    }
    
    if ( Boolean.valueOf( s.getConfig( GrouperConfig.PROP_USE_WHEEL_GROUP ) ).booleanValue() ) {
      String name = s.getConfig( GrouperConfig.PROP_WHEEL_GROUP );
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
    
    if (GrouperConfig.getPropertyBoolean(GrouperConfig.PROP_USE_WHEEL_GROUP, false)) {
      String name = GrouperConfig.getProperty( GrouperConfig.PROP_WHEEL_GROUP );
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
    String allowedGroupName = GrouperConfig
        .getProperty("security.stem.groupAllowedToMoveStem");
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
    String allowedGroupName = GrouperConfig
        .getProperty("security.stem.groupAllowedToCopyStem");
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
    String allowedGroupName = GrouperConfig
        .getProperty("security.stem.groupAllowedToRenameStem");
    if (StringUtils.isNotBlank(allowedGroupName) && !isWheelOrRoot(subject)) {

      Group allowedGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession()
          .internal_getRootSession(), allowedGroupName, false);
      if (allowedGroup == null || !allowedGroup.hasMember(subject)) {
        return false;
      }
    }
    return true;
  }

}

