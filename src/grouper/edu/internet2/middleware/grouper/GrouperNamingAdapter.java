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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.GrantPrivilegeAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteAlreadyDeletedException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.GrouperNonDbNamingAdapter;
import edu.internet2.middleware.grouper.privs.GrouperPrivilegeAdapter;
import edu.internet2.middleware.grouper.privs.NamingAdapter;
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
 * @version $Id: GrouperNamingAdapter.java,v 1.80 2009-04-13 16:53:08 mchyzer Exp $
 */
public class GrouperNamingAdapter extends GrouperNonDbNamingAdapter {

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.BaseNamingAdapter#hqlFilterStemsWhereClause(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public Set getSubjectsWithPriv(GrouperSession s, Stem ns, Privilege priv) 
    throws  SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    return MembershipFinder.internal_findSubjectsStemPriv(
      s, ns, priv.getField()
    );
  } // public Set getSubjectsWithPriv(s, ns, priv)

  /**
   * Get all stems where this subject has this privilege.
   * <pre class="eg">
   * try {
   *   Set isStemmer = np.getStemsWhereSubjectHasPriv(
   *     s, subj, NamingPrivilege.STEM
   *   );
   * }
   * catch (SchemaException e0) {
   *   // Invalid priv
   * }
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   subj  Get privileges for this subject.
   * @param   priv  Get this privilege.
   * @return  Set of {@link Stem} objects.
   * @throws  SchemaException
   */
  public Set getStemsWhereSubjectHasPriv(
    GrouperSession s, Subject subj, Privilege priv
  ) 
    throws  SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set<Stem> stems = new LinkedHashSet<Stem>();
    try {
      Field f = priv.getField();
      // This subject
      stems.addAll( 
        GrouperPrivilegeAdapter.internal_getStemsWhereSubjectHasPriv( s, MemberFinder.findBySubject(s, subj, true), f ) 
      );
      // The ALL subject
      if ( !( SubjectHelper.eq(subj, SubjectFinder.findAllSubject() ) ) ) {
        stems.addAll( 
          GrouperPrivilegeAdapter.internal_getStemsWhereSubjectHasPriv( s, MemberFinder.internal_findAllMember(), f ) 
        );
      }
    }
    catch (StemNotFoundException eSNF) {
      String msg = E.GNA_SNF + eSNF.getMessage();
      LOG.error( msg);
    }
    return stems;
  } // public Set getStemsWhereSubjectHasPriv(s, subj, priv)

  /**
   * Get all privileges held by this subject on this stem.
   * <p/>
   * <pre class="eg">
   * Set privs = np.getPrivs(s, ns, subj);
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   ns    Get privileges on this stem.
   * @param   subj  Get privileges for this subject.
   * @return  Set of {@link NamingPrivilege} objects.
   */
  public Set<NamingPrivilege> getPrivs(GrouperSession s, Stem ns, Subject subj) {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set privs = new LinkedHashSet();
    try {
      Member        m     = MemberFinder.findBySubject(s, subj, true);
      Member        all   = MemberFinder.internal_findAllMember();     
      MembershipDAO dao   = GrouperDAOFactory.getFactory().getMembership();
      Privilege     p;
      Field         f;
      Iterator      it;
      Iterator      iterP = Privilege.getNamingPrivs().iterator();
      while (iterP.hasNext()) {
        p   = (Privilege) iterP.next();
        f   = p.getField();
        it  = dao.findAllByStemOwnerAndMemberAndField( ns.getUuid(), m.getUuid(), f ).iterator();
        privs.addAll( GrouperPrivilegeAdapter.internal_getPrivs(s, ns,subj, m, p, it) );
        if (!m.equals(all)) {
          it = dao.findAllByStemOwnerAndMemberAndField( ns.getUuid(), all.getUuid(), f ).iterator();
          privs.addAll( GrouperPrivilegeAdapter.internal_getPrivs(s, ns,subj, all, p, it) );
        }
    String memberInClause = HibUtils.convertToInClause(memberIds, hqlQuery);
    query.append(memberInClause).append(")");
    return true;
    }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.BaseNamingAdapter#postHqlFilterStems(edu.internet2.middleware.grouper.GrouperSession, java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public void grantPriv(
    GrouperSession s, final Stem ns, final Subject subj, final Privilege priv
  )
    throws  GrantPrivilegeException, 
            InsufficientPrivilegeException,
            SchemaException
  {
    GrouperSession.validate(s);
    try {
      GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            Field f = priv.getField();
            PrivilegeHelper.dispatch( GrouperSession.staticGrouperSession(), ns, grouperSession.getSubject(), f.getWritePriv() );
            if (!f.getType().equals(FieldType.NAMING)) {
              throw new SchemaException(E.FIELD_INVALID_TYPE + f.getType());
            }  
            Membership.internal_addImmediateMembership(grouperSession, ns, subj, f);
          } catch (SchemaException se) {
            throw new GrouperSessionException(se);
          } catch (InsufficientPrivilegeException ipe) {
            throw new GrouperSessionException(ipe);
          } catch (MemberAddAlreadyExistsException eMA) {
            throw new GrouperSessionException(new GrantPrivilegeAlreadyExistsException(
                eMA.getMessage(), eMA));
          } catch (MemberAddException eMA) {
            throw new GrouperSessionException(new GrantPrivilegeException(eMA.getMessage(), eMA));
          }
          return null;
        }
        
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(GrouperNamingAdapter.class);

  /**
   * Check whether the subject has this privilege on this stem.
   * <pre class="eg">
   * try {
   *   np.hasPriv(s, ns, subj, NamingPrivilege.STEM);
   * }
   * catch (SchemaException e) {
   *   // Invalid privilege
   * }
   * </pre>
   * @param   s     Check privilege in this session context.
   * @param   ns    Check privilege on this stem.
   * @param   subj  Check privilege for this subject.
   * @param   priv  Check this privilege.   
   * @return if has priv
   * @throws  SchemaException
   */
  public boolean hasPriv(GrouperSession s, Stem ns, Subject subj, Privilege priv)
    throws  SchemaException 
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    boolean rv = false;
    Member m = MemberFinder.findBySubject(s, subj, true);
    rv = m.isMember( ns.getUuid(), priv.getField() );
    return rv;
  } // public boolean hasPriv(s, ns, subj, priv) 

  /**
   * Revoke this privilege from everyone on this stem.
   * <pre class="eg">
   * try {
   *   np.revokePriv(s, ns, NamingPrivilege.STEM);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to revoke the privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Unable to revoke the privilege
   * }
   * </pre>
   * @param   s     Revoke privilege in this session context.
   * @param   ns    Revoke privilege on this stem.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(GrouperSession s, Stem ns, Privilege priv)
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Field f = priv.getField();
    PrivilegeHelper.dispatch( GrouperSession.staticGrouperSession(), ns, s.getSubject(), f.getWritePriv() );
    if (!f.getType().equals(FieldType.NAMING)) {
      throw new SchemaException(E.FIELD_INVALID_TYPE + f.getType());
    }  
    try {
      Membership.internal_deleteAllField(s, ns, f);
    }
    catch (MemberDeleteException eMD) {
      throw new RevokePrivilegeException( eMD.getMessage(), eMD );
    }
  } // public void revokePriv(s, ns, priv)

  /**
   * Revoke the privilege from the subject on this stem.
   * <pre class="eg">
   * try {
   *   np.revokePriv(s, ns, subj, NamingPrivilege.STEM);
   * }
   * catch (InsufficientPrivilegeException e0) {
   *   // Not privileged to grant the privilege
   * }
   * catch (RevokePrivilegeException e2) {
   *   // Unable to revoke the privilege
   * }
   * </pre>
   * @param   s     Revoke privilege in this session context.
   * @param   ns    Revoke privilege on this stem.
   * @param   subj  Revoke privilege from this subject.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Field f = priv.getField();
    PrivilegeHelper.dispatch( GrouperSession.staticGrouperSession(), ns, s.getSubject(), f.getWritePriv() );
    if (!f.getType().equals(FieldType.NAMING)) {
      throw new SchemaException(E.FIELD_INVALID_TYPE + f.getType());
    }  
    try {
      DefaultMemberOf mof = Membership.internal_delImmediateMembership(s, ns, subj, f);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
    } catch (MemberDeleteAlreadyDeletedException eMD) {
      throw new RevokePrivilegeAlreadyRevokedException( eMD.getMessage(), eMD );
    } catch (MemberDeleteException eMD) {
      throw new RevokePrivilegeException( eMD.getMessage(), eMD );
    }
  } // public void revokePriv(s, ns, subj, priv)
  
  /**
   * Copies privileges for subjects that have the specified privilege on stem1 to stem2.
   * @param s 
   * @param stem1
   * @param stem2
   * @param priv 
   * @throws InsufficientPrivilegeException 
   * @throws GrantPrivilegeException 
   * @throws SchemaException 
   */
  public void privilegeCopy(GrouperSession s, Stem stem1, Stem stem2, Privilege priv)
      throws InsufficientPrivilegeException, GrantPrivilegeException, SchemaException {
    
    Field f = priv.getField();
    Set<Subject> subjs = MembershipFinder.internal_findStemSubjectsImmediateOnly(s, stem1, f);
    
    Iterator<Subject> subjectIter = subjs.iterator();
    while (subjectIter.hasNext()) {
      Subject subj = subjectIter.next();
      this.grantPriv(s, stem2, subj, priv);
    }
  }
  
  /**
   * Copies privileges of type priv on any subject for the given Subject subj1 to the given Subject subj2.
   * For instance, if subj1 has STEM privilege to Stem x, this method will result with subj2
   * having STEM privilege to Stem x.
   * @param s 
   * @param subj1
   * @param subj2
   * @param priv 
   * @throws InsufficientPrivilegeException 
   * @throws GrantPrivilegeException 
   * @throws SchemaException 
   */
  public void privilegeCopy(GrouperSession s, Subject subj1, Subject subj2, Privilege priv)
      throws InsufficientPrivilegeException, GrantPrivilegeException, SchemaException {
    GrouperSession.validate(s);
    
    Field f = priv.getField();
    Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
        .findAllImmediateByMemberAndField(MemberFinder.findBySubject(s, subj1, true).getUuid(), f);

    Iterator<Membership> membershipsIter = memberships.iterator();
    while (membershipsIter.hasNext()) {
      Stem stem;
      try {
        stem = membershipsIter.next().getStem();
      } catch (StemNotFoundException e1) {
        throw new GrouperException(e1.getMessage(), e1);
      }
      PrivilegeHelper.dispatch(s, stem, s.getSubject(), f.getWritePriv());
      try {
        Membership.internal_addImmediateMembership(s, stem, subj2, f);
      } catch (MemberAddException e) {
        throw new GrantPrivilegeException(e.getMessage(), e);
      }
    }    
  }

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
    
    Member member = MemberFinder.internal_findBySubject(subject, false);
    Member allMember = MemberFinder.internal_findAllMember();

    //FieldFinder.findAllIdsByType(FieldType.CREATE);
    Collection<String> namingPrivs = GrouperPrivilegeAdapter.fieldIdSet(priv2list, privInSet); 
    String accessInClause = HibUtils.convertToInClause(namingPrivs, hqlQuery);

    //TODO update this for 1.5 (stem owner)
    //if not, we need an in clause
    StringBuilder query = hql.append( ", Membership __namingMembership where " +
        "__namingMembership.ownerUuid = " + stemColumn
        + " and __namingMembership.fieldId in (");
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
   * @see edu.internet2.middleware.grouper.privs.BaseNamingAdapter#postHqlFilterStems(edu.internet2.middleware.grouper.GrouperSession, java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  @Override
  public Set<Stem> postHqlFilterStems(GrouperSession grouperSession,
      Set<Stem> inputStems, Subject subject, Set<Privilege> privInSet) {
    //this is already filtered
    return inputStems;
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(GrouperNamingAdapter.class);
  
} // public class GrouperNamingAdapter

