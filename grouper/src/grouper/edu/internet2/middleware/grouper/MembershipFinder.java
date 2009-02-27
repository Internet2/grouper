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
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Find memberships within the Groups Registry.
 * 
 * A membership is the object which represents a join of member
 * and group.  Has metadata like type and creator,
 * and, if an effective membership, the parent membership
 * <p/>
 * @author  blair christensen.
 * @version $Id: MembershipFinder.java,v 1.100 2009-02-27 20:51:46 shilen Exp $
 */
public class MembershipFinder {
  
  // PUBLIC CLASS METHODS //

  /**
   * Return the composite membership if it exists. 
   *
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Composite membership has this group.
   * @param   subj  Composite membership has this subject.
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   * @since   1.0
   */
  public static Membership findCompositeMembership(
    GrouperSession s, Group g, Subject subj
  )
    throws  MembershipNotFoundException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
     // @filtered  true
     // @session   true
    GrouperSession.validate(s);
    try {
      Field       f   = Group.getDefaultList();
      Member      m   = MemberFinder.findBySubject(s, subj);
      Membership  ms  = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType( 
          g.getUuid(), m.getUuid(), f, Membership.COMPOSITE);
      PrivilegeHelper.dispatch( s, ms.getGroup(), s.getSubject(), f.getReadPriv() );
      return ms;
    }
    catch (GroupNotFoundException eGNF)         {
      throw new MembershipNotFoundException(eGNF.getMessage(), eGNF);
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new MembershipNotFoundException(eIP.getMessage(), eIP);
    }
  } // public static Membership findCompositeMembership(s, g, m)

  /**
   * Return effective memberships.  
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a 
   * composite group are effective members (since the composite 
   * group has two groups and a set operator and no other immediate 
   * members).  Note that a member can have an immediate membership 
   * and an effective membership.
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Effective membership has this group.
   * @param   subj  Effective membership has this subject.
   * @param   f     Effective membership has this list.
   * @param   via   Effective membership has this via group.
   * @param   depth Effective membership has this depth.
   * @return  A set of {@link Membership} objects.
   * @throws  MembershipNotFoundException
   * @throws  SchemaException
   */
  public static Set<Membership> findEffectiveMemberships(
    GrouperSession s, Group g, Subject subj, Field f, Group via, int depth
  )
    throws  MembershipNotFoundException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
     // @filtered  true
     // @session   true
    GrouperSession.validate(s);
    Set mships = new LinkedHashSet();
    Member m = MemberFinder.findBySubject(s, subj);
    try {
      PrivilegeHelper.dispatch( s, g, s.getSubject(), f.getReadPriv() );
      Iterator  it    = GrouperDAOFactory.getFactory().getMembership().findAllEffectiveByGroupOwner(
        g.getUuid(), m.getUuid(), f, via.getUuid(), depth
      ).iterator();
      Membership eff;
      while (it.hasNext()) {
        eff = (Membership) it.next();
        mships.add(eff);
      }
    }
    catch (InsufficientPrivilegeException eIP) {
      // ??? ignore
    }
    return mships;
  } // public static Membership findEffectiveMembership(s, g, subj, f, via, depth)

  /**
   * Return the immediate membership if it exists.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Immediate membership has this group.
   * @param   subj  Immediate membership has this subject.
   * @param   f     Immediate membership has this list.
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   */
  public static Membership findImmediateMembership(
    GrouperSession s, Group g, Subject subj, Field f
  ) throws  MembershipNotFoundException, SchemaException {
    //note, no need for GrouperSession inverse of control
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    try {
      Member      m   = MemberFinder.findBySubject(s, subj);
      Membership  ms  = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType( 
          g.getUuid(), m.getUuid(), f, Membership.IMMEDIATE );
      PrivilegeHelper.dispatch( s, ms.getGroup(), s.getSubject(), f.getReadPriv() );
      return ms;
    }
    catch (GroupNotFoundException eGNF)         {
      throw new MembershipNotFoundException(eGNF.getMessage(), eGNF);
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new MembershipNotFoundException(eIP.getMessage(), eIP);
    }
  } // public static Membership findImmediateMembership(s, g, m, f)

  /**
   * @param dto
   * @return set of memberships
   */
  public static Set<Membership> internal_findAllChildrenNoPriv(Membership dto) {
    Set           children  = new LinkedHashSet();
    Membership child;
    Iterator      it        = GrouperDAOFactory.getFactory().getMembership().findAllChildMemberships(dto).iterator();
    while (it.hasNext()) {
      child = (Membership) it.next();
      children.addAll( internal_findAllChildrenNoPriv(child) );
      children.add(child);
    }
    return children;
  } // protected static Set internal_findAllChildrenNoPriv(dto)

  /**
   * TODO 20070813 i really need to figure out what this method does and replace it with something cleaner.              
   * @param dto 
   * @param children 
   * @return set of memberships
   * @throws SchemaException 
   * @since  1.2.1
   */
  public static Set<Membership> internal_findAllForwardMembershipsNoPriv(Membership dto, Set children)  
    throws  SchemaException 
  {
    MembershipDAO dao     = GrouperDAOFactory.getFactory().getMembership();
    Set           mships  = new LinkedHashSet();
    Iterator      it      = dao.findAllByMemberAndViaGroup( dto.getMemberUuid(), dto.getOwnerGroupId() ).iterator();
    Iterator      childIt;
    Membership _eff;
    Membership _child;
    while (it.hasNext()) {
      _eff    = (Membership) it.next();
      childIt = children.iterator();
      while (childIt.hasNext()) {
        _child = (Membership) childIt.next();
        Set<Membership> memberships = null;
        Field field = FieldFinder.find( _eff.getListName() );
        if (field.isGroupListField()) {
          memberships = dao.findAllEffectiveByGroupOwner(
              _eff.getOwnerGroupId(), _child.getMemberUuid(), field, 
              _child.getViaGroupId(), _eff.getDepth() + _child.getDepth()    
            );
        } else if (field.isStemListField()) {
          memberships = dao.findAllEffectiveByStemOwner(
              _eff.getOwnerStemId(), _child.getMemberUuid(), field, 
              _child.getViaGroupId(), _eff.getDepth() + _child.getDepth()    
            );
          
        }
        mships.addAll(memberships);
      }
      mships.add(_eff);
    }
    return mships;
  }

  /** 
   * @param group 
   * @param field 
   * @return  A set of all <code>Member</code>'s in <i>group</i>'s list <i>field</i>.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public static Set<Member> findMembers(Group group, Field field)
    throws  IllegalArgumentException
  {
    //note, no need for GrouperSession inverse of control
    if (group == null) { // TODO 20070814 ParameterHelper
      throw new IllegalArgumentException("null Group");
    }
    if (field == null) { // TODO 20070814 ParameterHelper
      throw new IllegalArgumentException("null Field");
    }
    Set<Member> members = new LinkedHashSet();
    try {
      GrouperSession  s   = GrouperSession.staticGrouperSession();
      PrivilegeHelper.dispatch( s, group, s.getSubject(), field.getReadPriv() );
      for ( Member m : GrouperDAOFactory.getFactory().getMembership().findAllMembersByGroupOwnerAndField( group.getUuid(), field ) ) {
       
        members.add(m);
      }
    }
    catch (InsufficientPrivilegeException eIP) {
      // ignore  
    }
    catch (SchemaException eSchema) {
      // ignore  
    }
    return members;
  } 

  /**
   * 
   * @param s
   * @param group
   * @param f
   * @return set of subjects
   * @throws GrouperRuntimeException
   */
  public static Set<Subject> internal_findGroupSubjects(GrouperSession s, Group group, Field f) 
    throws  GrouperRuntimeException
  {
    GrouperSession.validate(s);
    Set       subjs = new LinkedHashSet();
    Iterator  it    = PrivilegeHelper.canViewMemberships(
      s, GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndField( group.getUuid(), f )
    ).iterator();
    try {
      while (it.hasNext()) {
    	//2007-12-18 Gary Brown
        //Instantiating all the Subjects can be very slow. LazySubjects
    	//only make expensive calls when necessary - so a client can page 
        //results.
    	//A partial alternative may have been to always instantiate the Member of
    	//a Membership when the latter is created - assuming one query.
    	try {
    		subjs.add ( new LazySubject((Membership) it.next()) );
    	}catch(GrouperRuntimeException gre) {
    		if(gre.getCause() instanceof MemberNotFoundException) {
    			throw (MemberNotFoundException) gre.getCause();
    		}
    		if(gre.getCause() instanceof SubjectNotFoundException) {
    			throw (SubjectNotFoundException) gre.getCause();
    		}
    	}
      }
    }
    catch (MemberNotFoundException eMNF) {
      String msg = "internal_findSubjects: " + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eMNF);
    }
    catch (SubjectNotFoundException eSNF) {
      String msg = "internal_findSubjects: " + eSNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eSNF);
    }
    return subjs;
  } // public static Set internal_findSubjects(s, o, f)

  /**
   * 
   * @param s
   * @param group
   * @param f
   * @return set of subjects
   * @throws GrouperRuntimeException
   */
  public static Set<Subject> internal_findGroupSubjectsImmediateOnly(GrouperSession s,
      Group group, Field f) throws GrouperRuntimeException {
    GrouperSession.validate(s);
    Set<Subject> subjs = new LinkedHashSet();
    try {
      PrivilegeHelper.dispatch(s, group, s.getSubject(), f.getReadPriv());
      Iterator<Member> it = GrouperDAOFactory.getFactory().getMembership()
          .findAllMembersByGroupOwnerAndFieldAndType(group.getUuid(), f,
              Membership.IMMEDIATE).iterator();

      while (it.hasNext()) {
        try {
          subjs.add(new LazySubject(it.next()));
        } catch (GrouperRuntimeException gre) {
          if (gre.getCause() instanceof MemberNotFoundException) {
            throw (MemberNotFoundException) gre.getCause();
          }
          if (gre.getCause() instanceof SubjectNotFoundException) {
            throw (SubjectNotFoundException) gre.getCause();
          }
        }
      }
    } catch (MemberNotFoundException eMNF) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eMNF);
    } catch (SubjectNotFoundException eSNF) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + eSNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eSNF);
    } catch (InsufficientPrivilegeException e) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, e);
    } catch (SchemaException e) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, e);
    }
    return subjs;
  } 
  
  /**
   * 
   * @param s
   * @param stem
   * @param f
   * @return set of subjects
   * @throws GrouperRuntimeException
   */
  public static Set<Subject> internal_findStemSubjectsImmediateOnly(GrouperSession s,
      Stem stem, Field f) throws GrouperRuntimeException {
    GrouperSession.validate(s);
    Set<Subject> subjs = new LinkedHashSet();
    try {
      PrivilegeHelper.dispatch(s, stem, s.getSubject(), f.getReadPriv());
      Iterator<Member> it = GrouperDAOFactory.getFactory().getMembership()
          .findAllMembersByStemOwnerAndFieldAndType(stem.getUuid(), f,
              Membership.IMMEDIATE).iterator();

      while (it.hasNext()) {
        try {
          subjs.add(new LazySubject(it.next()));
        } catch (GrouperRuntimeException gre) {
          if (gre.getCause() instanceof MemberNotFoundException) {
            throw (MemberNotFoundException) gre.getCause();
          }
          if (gre.getCause() instanceof SubjectNotFoundException) {
            throw (SubjectNotFoundException) gre.getCause();
          }
        }
      }
    } catch (MemberNotFoundException eMNF) {
      String msg = "internal_findStemSubjectsImmediateOnly: " + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eMNF);
    } catch (SubjectNotFoundException eSNF) {
      String msg = "internal_findStemSubjectsImmediateOnly: " + eSNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eSNF);
    } catch (InsufficientPrivilegeException e) {
      String msg = "internal_findStemSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, e);
    } catch (SchemaException e) {
      String msg = "internal_findStemSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, e);
    }
    return subjs;
  } 
  

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(MemberFinder.class);

  /**
   * 
   * @param s
   * @param stem
   * @param f
   * @return set of subjects
   */
  public static Set<Subject> internal_findSubjectsStemPriv(GrouperSession s, Stem stem, Field f) {
     // @filtered  false
     // @session   true 
    GrouperSession.validate(s);
    Membership mbs;
    Set           subjs = new LinkedHashSet();
    Iterator      it    = GrouperDAOFactory.getFactory().getMembership().findAllByStemOwnerAndField( stem.getUuid(), f ).iterator();
    while (it.hasNext()) {
      mbs = (Membership) it.next();
      try {
    	  subjs.add ( new LazySubject(mbs) );
        //_m = dao.findByUuid( ms.getMemberUuid() );
        //subjs.add( SubjectFinder.findById( _m.getSubjectId(), _m.getSubjectTypeId(), _m.getSubjectSourceId() ) );
      }
      catch (Exception e) {
        // @exception MemberNotFoundException
        // @exception SubjectNotFoundException
        LOG.error(E.MSF_FINDSUBJECTS + e.getMessage());
      }
    }
    return subjs;
  } // public static Set internal_findSubjectsNoPriv(s, o, f)

  /**
   * 
   * @param s
   * @param g
   * @param f
   * @param type
   * @return set of members
   */
  public static Set<Member> internal_findMembersByType(GrouperSession s, Group g, Field f, String type) {
    GrouperSession.validate(s);
    Set         members = new LinkedHashSet();
    Membership  ms;
    Iterator    it      = internal_findAllByGroupOwnerAndFieldAndType(s, g, f, type).iterator();
    while (it.hasNext()) {
      ms = (Membership) it.next();
      try {
        members.add(ms.getMember());
      }
      catch (MemberNotFoundException eMNF) {
        // Ignore
      }
    }
    return members;
  } // public static Set internal_findMembersByType(s, g, f, type)

  /**
   * 
   * @param s
   * @param d
   * @param f
   * @return set of memberships
   * @throws QueryException
   */
  public static Set<Membership> internal_findAllByCreatedAfter(GrouperSession s, Date d, Field f) 
    throws QueryException 
  {
    //note, no need for GrouperSession inverse of control
    // @filtered  false
    // @session   true
    Set         mships  = new LinkedHashSet();
    Membership  ms;
    Iterator    it      = GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(d, f).iterator();
    while (it.hasNext()) {
      ms = (Membership) it.next();
      mships.add(ms);
    }
    return mships;
  } 

  /**
   * 
   * @param s
   * @param d
   * @param f
   * @return set of memberships
   * @throws QueryException
   */
  public static Set<Membership> internal_findAllByCreatedBefore(GrouperSession s, Date d, Field f) 
    throws QueryException {
    //note, no need for GrouperSession inverse of control
    // @filtered  false
    // @session   true
    Set         mships  = new LinkedHashSet();
    Membership  ms;
    Iterator    it      = GrouperDAOFactory.getFactory().getMembership().findAllByCreatedBefore(d, f).iterator();
    while (it.hasNext()) {
      ms = (Membership) it.next();
      mships.add(ms);
    }
    return mships;
  } // public static Set internal_findAllByCreatedBefore(s, d, f)

  /**
   * 
   * @param s
   * @param groupOwner
   * @param f
   * @param type
   * @return set of memberships
   */
  public static Set<Membership> internal_findAllByGroupOwnerAndFieldAndType(GrouperSession s, Group groupOwner, Field f, String type) {
     // @filtered  true
     // @session   true
    GrouperSession.validate(s);
    return PrivilegeHelper.canViewMemberships(
      s, GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndFieldAndType(groupOwner.getUuid(), f, type)
    );
  } // public static Set internal_findAllByOwnerAndFieldAndType(s, o, f, type)

  /**
   * 
   * @param s
   * @param m
   * @param f
   * @return set of memberships
   */
  public static Set<Membership> internal_findAllEffectiveByMemberAndField(
    GrouperSession s, Member m, Field f
  ) 
  {
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    return PrivilegeHelper.canViewMemberships( 
      s, GrouperDAOFactory.getFactory().getMembership().findAllEffectiveByMemberAndField( m.getUuid(), f ) 
    );
  } // public static Set internal_findAllEffectiveByMemberAndField(s, m, f)

  /**
   * 
   * @param s
   * @param m
   * @param f
   * @return set of memberships
   */
  public static Set<Membership> internal_findAllImmediateByMemberAndField(GrouperSession s, Member m, Field f) {
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    return PrivilegeHelper.canViewMemberships( 
      s, GrouperDAOFactory.getFactory().getMembership().findAllImmediateByMemberAndField( m.getUuid(), f ) 
    );
  } 

  /**
   * 
   * @param s
   * @param m
   * @param f
   * @return set of memberships
   */
  public static Set<Membership> internal_findMemberships(GrouperSession s, Member m, Field f) {
     // @filtered  true
     // @session   true
    GrouperSession.validate(s);
    MembershipDAO dao     = GrouperDAOFactory.getFactory().getMembership();
    Set           mships  = new LinkedHashSet();
    mships.addAll( dao.findMembershipsByMemberAndField( m.getUuid(), f ) );
    if ( !m.equals( MemberFinder.internal_findAllMember() ) ) {
      mships.addAll( dao.findMembershipsByMemberAndField( MemberFinder.internal_findAllMember().getUuid(), f ) );
    }
    return PrivilegeHelper.canViewMemberships(s, mships);
  } // public static Set internal_findMemberships(s, m, f)

} // public class MembershipFinder

