/*
 * @author mchyzer
 * $Id: GrouperNonDbNamingAdapter.java,v 1.8 2009-12-07 07:31:09 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.privs;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
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
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 *
 */
public class GrouperNonDbNamingAdapter extends BaseNamingAdapter {
  
  /**
   * Get all stems where this subject doesnt have this privilege.
   * @param grouperSession 
   * @param stemId 
   * @param scope 
   * @param subject 
   * @param privilege 
   * @param considerAllSubject
   * @return stems
   */
  public Set<Stem> getStemsWhereSubjectDoesntHavePrivilege(
      GrouperSession grouperSession, String stemId, Scope scope, Subject subject,
      Privilege privilege, boolean considerAllSubject) {

    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(grouperSession);
    Set<Stem> stems = new LinkedHashSet();

    // This subject
    stems.addAll( 
      GrouperPrivilegeAdapter.internal_getStemsWhereSubjectDoesntHavePriv( grouperSession, 
          stemId, scope, subject, privilege, considerAllSubject) 
    );
    return stems;
  
  }  


  /**
   * 
   */
  protected static Map<Privilege, String> priv2list = new HashMap<Privilege, String>();


  // STATIC //
  static {
    priv2list.put(  NamingPrivilege.CREATE, "creators"  );
    priv2list.put(  NamingPrivilege.STEM  , "stemmers"  );
  } // static

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.NamingAdapter#getSubjectsWithPriv(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.privs.Privilege)
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
      //this is done in dao
      // The ALL subject
//      if ( !( SubjectHelper.eq(subj, SubjectFinder.findAllSubject() ) ) ) {
//        stems.addAll( 
//          GrouperPrivilegeAdapter.internal_getStemsWhereSubjectHasPriv( s, MemberFinder.internal_findAllMember(), f ) 
//        );
//      }
    }
    catch (StemNotFoundException eSNF) {
      String msg = E.GNA_SNF + eSNF.getMessage();
      LOG.error( msg);
    }
    return stems;
  } // public Set getStemsWhereSubjectHasPriv(s, subj, priv)

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperNonDbNamingAdapter.class);

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
        it  = dao.findAllByStemOwnerAndMemberAndField(ns.getUuid(), m.getUuid(), f, true).iterator();
        privs.addAll( GrouperPrivilegeAdapter.internal_getPrivs(s, ns,subj, m, p, it) );
        if (!m.equals(all)) {
          it = dao.findAllByStemOwnerAndMemberAndField(ns.getUuid(), all.getUuid(), f, true).iterator();
          privs.addAll( GrouperPrivilegeAdapter.internal_getPrivs(s, ns,subj, all, p, it) );
        }
      }
    }
    catch (SchemaException eS) {
      LOG.error( eS.getMessage());
    }
    return privs;
  } // public Set getPrivs(s, ns, subj)

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.NamingAdapter#grantPriv(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege, String)
   */
  public void grantPriv(
    GrouperSession s, final Stem ns, final Subject subj, final Privilege priv, final String uuid
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
            Membership.internal_addImmediateMembership(grouperSession, ns, subj, f, uuid);
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
      });
    } catch (GrouperSessionException gse) {
      if (gse.getCause() instanceof GrantPrivilegeException) {
        throw (GrantPrivilegeException)gse.getCause();
      }
      if (gse.getCause() instanceof SchemaException) {
        throw (SchemaException)gse.getCause();
      }
      if (gse.getCause() instanceof InsufficientPrivilegeException) {
        throw (InsufficientPrivilegeException)gse.getCause();
      }
      throw gse;
    }
  } // public void grantPriv(s, ns, subj, priv)


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
    
    GrouperSession.validate(s);
    
    Field f = priv.getField();
    PrivilegeHelper.dispatch(s, stem1, s.getSubject(), f.getReadPriv());
    
    Iterator<Membership> membershipsIter = GrouperDAOFactory.getFactory().getMembership()
      .findAllByStemOwnerAndFieldAndType(stem1.getUuid(), f,
          MembershipType.IMMEDIATE.getTypeString(), false).iterator();

    while (membershipsIter.hasNext()) {
      Membership existingMembership = membershipsIter.next();
      Membership copiedMembership = existingMembership.clone();
      copiedMembership.setOwnerStemId(stem2.getUuid());
      copiedMembership.setCreatorUuid(s.getMemberUuid());
      copiedMembership.setCreateTimeLong(new Date().getTime());
      copiedMembership.setImmediateMembershipId(GrouperUuid.getUuid());
      copiedMembership.setHibernateVersionNumber(-1L);
      GrouperDAOFactory.getFactory().getMembership().save(copiedMembership);
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
        .findAllImmediateByMemberAndField(MemberFinder.findBySubject(s, subj1, true).getUuid(), f, false);
  
    if (memberships.size() == 0) {
      return;
    }
    
    Member member = MemberFinder.findBySubject(s, subj2, true);
    
    Iterator<Membership> membershipsIter = memberships.iterator();
    while (membershipsIter.hasNext()) {
      Membership existingMembership = membershipsIter.next();
      Stem stem;
      try {
        stem = existingMembership.getStem();
      } catch (StemNotFoundException e1) {
        throw new GrouperException(e1.getMessage(), e1);
      }
      PrivilegeHelper.dispatch(s, stem, s.getSubject(), f.getWritePriv());
      
      Membership copiedMembership = existingMembership.clone();
      copiedMembership.setMemberUuid(member.getUuid());
      copiedMembership.setMember(member);
      copiedMembership.setCreatorUuid(s.getMemberUuid());
      copiedMembership.setCreateTimeLong(new Date().getTime());
      copiedMembership.setImmediateMembershipId(GrouperUuid.getUuid());
      copiedMembership.setHibernateVersionNumber(-1L);
      GrouperDAOFactory.getFactory().getMembership().save(copiedMembership);
    }    
  }


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
      Membership.internal_delImmediateMembership(s, ns, subj, f);
    } catch (MemberDeleteAlreadyDeletedException eMD) {
      throw new RevokePrivilegeAlreadyRevokedException( eMD.getMessage(), eMD );
    } catch (MemberDeleteException eMD) {
      throw new RevokePrivilegeException( eMD.getMessage(), eMD );
    }
  } // public void revokePriv(s, ns, subj, priv)

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingAdapter#revokeAllPrivilegesForSubject(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(GrouperSession grouperSession, Subject subject) {
    GrouperSession.validate(grouperSession);
    
    // right now this method only gets executed as the root subject.
    // so we're not doing any privilege checking just to save on performance.
    if (!SubjectHelper.eq(SubjectFinder.findRootSubject(), grouperSession.getSubject())) {
      throw new InsufficientPrivilegeException();
    }
    
    Member member = MemberFinder.findBySubject(grouperSession, subject, true);
    Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
        .findAllImmediateByMemberAndFieldType(member.getUuid(), FieldType.NAMING.getType(), false);
    Iterator<Membership> iter = memberships.iterator();
    while (iter.hasNext()) {
      Membership mship = iter.next();
      mship.delete();
    }
  }


}
