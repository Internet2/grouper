/*******************************************************************************
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
 ******************************************************************************/
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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.subject.Subject;

/** 
 * Facade for the {@link NamingAdapter} interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: NamingResolver.java,v 1.12 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public interface NamingResolver {

  /**
   * find the stems which do not have a certain privilege
   * @param stemId
   * @param scope
   * @param subject
   * @param privilege
   * @param considerAllSubject 
   * @param sqlLikeString
   * @return the stems
   */
  Set<Stem> getStemsWhereSubjectDoesntHavePrivilege(
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString);
  

  /**
   * flush cache if caching resolver
   */
  public void flushCache();

  // TODO 20070820 DRY w/ access resolution

  /** clean up resources, session is stopped */
  public void stop();

  /**
   * Get all groups where <i>subject</i> has <i>privilege</i>.
   * <p/>
   * @param subject 
   * @param privilege 
   * @return  set
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     edu.internet2.middleware.grouper.privs.NamingAdapter#getStemsWhereSubjectHasPriv(GrouperSession, Subject, Privilege)
   * @since   1.2.1
   */
  Set<Stem> getStemsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
      throws IllegalArgumentException;

  /**
   * Get all privileges <i>subject</i> has on <i>group</i>.
   * <p/>
   * @param stem 
   * @param subject 
   * @return  set of naming privileges
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     edu.internet2.middleware.grouper.privs.NamingAdapter#getPrivs(GrouperSession, Stem, Subject)
   * @since   1.2.1
   */
  Set<NamingPrivilege> getPrivileges(Stem stem, Subject subject)
      throws IllegalArgumentException;

  /**
   * Get all subjects with <i>privilege</i> on <i>group</i>.
   * <p/>
   * @param stem 
   * @param privilege 
   * @return  set of subjects
   * @throws  IllegalArgumentException if any parameter is null.
   * @see
   * edu.internet2.middleware.grouper.privs.NamingAdapter#getSubjectsWithPriv(GrouperSession, Stem, Privilege)
   * @since   1.2.1
   */
  Set<Subject> getSubjectsWithPrivilege(Stem stem, Privilege privilege)
      throws IllegalArgumentException;

  /**
   * Grant <i>privilege</i> to <i>subject</i> on <i>group</i>.
   * <p/>
   * @param stem 
   * @param subject 
   * @param privilege 
   * @param uuid if known or null
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  UnableToPerformException if the privilege could not be granted.
   * @see     edu.internet2.middleware.grouper.privs.NamingAdapter#grantPriv(GrouperSession, Stem, Subject, Privilege)
   * @since   1.2.1
   */
  void grantPrivilege(Stem stem, Subject subject, Privilege privilege, String uuid)
      throws IllegalArgumentException,
      UnableToPerformException;

  /**
   * Check whether <i>subject</i> has <i>privilege</i> on <i>group</i>.
   * <p/>
   * @param stem 
   * @param subject 
   * @param privilege 
   * @return  if has privilege
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     edu.internet2.middleware.grouper.privs.NamingAdapter#hasPriv(GrouperSession, Stem, Subject, Privilege)
   * @since   1.2.1
   */
  boolean hasPrivilege(Stem stem, Subject subject, Privilege privilege)
      throws IllegalArgumentException;

  /**
   * Revoke <i>privilege</i> from all subjects on <i>group</i>.
   * <p/>
   * @param stem 
   * @param privilege 
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  UnableToPerformException if the privilege could not be revoked.
   * @see     edu.internet2.middleware.grouper.privs.NamingAdapter#revokePriv(GrouperSession, Stem, Privilege)
   * @since   1.2.1
   */
  void revokePrivilege(Stem stem, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException;

  /**
   * Revoke <i>privilege</i> from <i>subject</i> on <i>group</i>.
   * <p/>
   * @param stem 
   * @param subject 
   * @param privilege 
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  UnableToPerformException if the privilege could not be revoked.
   * @see     edu.internet2.middleware.grouper.privs.NamingAdapter#revokePriv(GrouperSession, Stem, Subject, Privilege)
   * @since   1.2.1
   */
  void revokePrivilege(Stem stem, Subject subject, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException;

  /**
   * Copies privileges for subjects that have the specified privilege on stem1 to stem2.
   * @param stem1
   * @param stem2
   * @param priv 
   * @throws IllegalArgumentException 
   * @throws UnableToPerformException 
   */
  void privilegeCopy(Stem stem1, Stem stem2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException;

  /**
   * Copies privileges of type priv on any subject for the given Subject subj1 to the given Subject subj2.
   * For instance, if subj1 has STEM privilege to Stem x, this method will result with subj2
   * having STEM privilege to Stem x.
   * @param subj1
   * @param subj2
   * @param priv  
   * @throws IllegalArgumentException 
   * @throws UnableToPerformException 
   */
  void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException;

  /**
   * get a reference to the session
   * @return the session
   */
  public GrouperSession getGrouperSession();

  /**
   * for a stem query, check to make sure the subject can see the records (if filtering HQL, you can do 
   * the postHqlFilterGroups instead if you like).  Note, this joins to tables, so the queries should
   * probably be "distinct"
   * @param subject which needs view access to the groups
   * @param hql is the select and part part (hql prefix)
   * @param hqlQuery 
   * @param stemColumn is the name of the stem column to join to
   * @param privInSet find a privilege which is in this set 
   * (e.g. for view, send all access privs).  There are pre-canned sets in AccessAdapter
   * @return if the query was changed
   */
  public boolean hqlFilterStemsWhereClause(
      Subject subject, HqlQuery hqlQuery, StringBuilder hql,
      String stemColumn, Set<Privilege> privInSet);

  /**
   * after HQL is run, filter stems.  If you are filtering in HQL, then dont filter here
   * @param stems
   * @param subject which needs view access to the groups
   * @param privInSet find a privilege which is in this set 
   * (e.g. for view, send all access privs).  There are pre-canned sets in NamingPrivilege
   * @return the set of filtered groups
   */
  public Set<Stem> postHqlFilterStems(
      Set<Stem> stems, Subject subject, Set<Privilege> privInSet);

  /**
   * Revoke all naming privileges that this subject has.
   * @param subject
   */
  public void revokeAllPrivilegesForSubject(Subject subject);
  
  /**
   * for a stem query, check to make sure the subject cant see the records
   * @param subject which needs view access to the groups
   * @param hqlQuery 
   * @param hql the select and current from part
   * @param stemColumn is the name of the group column to join to
   * @param privilege find a privilege which is in this set (e.g. stem or create)
   * @param considerAllSubject if true, then consider GrouperAll when seeign if subject has priv, else do not
   * @return if the statement was changed
   */
  public boolean hqlFilterStemsNotWithPrivWhereClause( 
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String stemColumn, Privilege privilege, boolean considerAllSubject);

  
}
