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
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import  edu.internet2.middleware.grouper.Stem;
import  edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import  edu.internet2.middleware.subject.Subject;


/**
 * Decorator that provides <i>GrouperSystem</i> privilege resolution for {@link NamingResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSystemNamingResolver.java,v 1.10 2009-08-29 15:57:59 shilen Exp $
 * @since   1.2.1
 */
public class GrouperSystemNamingResolver extends NamingResolverDecorator {
  // TODO 20070820 DRY w/ access resolution

  /** */
  private Subject root;



  /**
   * @param resolver 
   * @since   1.2.1
   */
  public GrouperSystemNamingResolver(NamingResolver resolver) {
    super(resolver);
    this.root = SubjectFinder.findRootSubject();
  }



  /**
   * @see     NamingResolver#getStemsWhereSubjectHasPrivilege(Subject, Privilege)
   * @since   1.2.1
   */
  public Set<Stem> getStemsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    return super.getDecoratedResolver().getStemsWhereSubjectHasPrivilege(subject, privilege);
  }

  /**
   * @see     NamingResolver#getPrivileges(Stem, Subject)
   * @since   1.2.1
   */
  public Set<NamingPrivilege> getPrivileges(Stem stem, Subject subject)
    throws  IllegalArgumentException
  {
    return super.getDecoratedResolver().getPrivileges(stem, subject);
  }

  /**
   * @see     NamingResolver#getSubjectsWithPrivilege(Stem, Privilege)
   * @since   1.2.1
   */
  public Set<Subject> getSubjectsWithPrivilege(Stem stem, Privilege privilege)
    throws  IllegalArgumentException
  {
    return super.getDecoratedResolver().getSubjectsWithPrivilege(stem, privilege);
  }

  /**
   * @see     NamingResolver#grantPrivilege(Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public void grantPrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    super.getDecoratedResolver().grantPrivilege(stem, subject, privilege);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#stop()
   */
  public void stop() {
    this.getDecoratedResolver().stop();
  }

  /**
   * @see     NamingResolver#hasPrivilege(Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    if ( SubjectHelper.eq( this.root, subject ) ) {
      return true;
    }
    return super.getDecoratedResolver().hasPrivilege(stem, subject, privilege);
  }

  /**
   * @see     NamingResolver#revokePrivilege(Stem, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Stem stem, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    super.getDecoratedResolver().revokePrivilege(stem, privilege);
  }
            

  /**
   * @see     NamingResolver#revokePrivilege(Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    super.getDecoratedResolver().revokePrivilege(stem, subject, privilege);
  }


  /*
   * (non-Javadoc)
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#privilegeCopy(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Stem stem1, Stem stem2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    super.getDecoratedResolver().privilegeCopy(stem1, stem2, priv);
  }


  /*
   * (non-Javadoc)
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#privilegeCopy(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    super.getDecoratedResolver().privilegeCopy(subj1, subj2, priv);
  }            

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#getGrouperSession()
   */
  public GrouperSession getGrouperSession() {
    NamingResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.getGrouperSession();
  }



  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#hqlFilterStemsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterStemsWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String stemColumn, Set<Privilege> privInSet) {
    //GrouperSystem can see all stems
    if ( SubjectHelper.eq( this.root, subject ) ) {
      return false;
    }
    NamingResolver decoratedResolver = super.getDecoratedResolver();
    //CachingNamingResolver
    return decoratedResolver.hqlFilterStemsWhereClause(subject, hqlQuery, hql, stemColumn, privInSet);
  }



  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#postHqlFilterStems(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Stem> postHqlFilterStems(Set<Stem> stems, Subject subject,
      Set<Privilege> privInSet) {
    
    //GrouperSystem can see all stems
    if ( SubjectHelper.eq( this.root, subject ) ) {
      return stems;
    }
    Set<Stem> filteredStems = super.getDecoratedResolver().postHqlFilterStems(stems, subject, privInSet);
    
    //return filtered groups
    return filteredStems;
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#revokeAllPrivilegesForSubject(edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(Subject subject) {
    super.getDecoratedResolver().revokeAllPrivilegesForSubject(subject);
  }

}

