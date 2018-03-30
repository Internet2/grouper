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
 * @author mchyzer
 * $Id: BaseNamingAdapter.java,v 1.2 2009-04-13 16:53:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.privs;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public abstract class BaseNamingAdapter implements NamingAdapter {

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.NamingAdapter#hqlFilterStemsWhereClause(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterStemsWhereClause(GrouperSession grouperSession,
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String stemColumn,
      Set<Privilege> privInSet) {
    //by default dont change the hql
    return false;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.NamingAdapter#postHqlFilterStems(edu.internet2.middleware.grouper.GrouperSession, java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Stem> postHqlFilterStems(GrouperSession grouperSession,
      Set<Stem> inputStems, Subject subject, Set<Privilege> privInSet) {
    //no privs no filter
    if (GrouperUtil.length(privInSet) == 0 || GrouperUtil.length(inputStems) == 0) {
      return inputStems;
    }

    Set<Stem>  stems  = new LinkedHashSet();
    for ( Stem stem : inputStems ) {
      
      if ( PrivilegeHelper.hasPrivilege(
          GrouperSession.staticGrouperSession().internal_getRootSession(), stem, subject, privInSet ) ) {
        stems.add(stem);
      }
    }
    return stems;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.NamingAdapter#hqlFilterStemsNotWithPrivWhereClause(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege, boolean)
   */
  public boolean hqlFilterStemsNotWithPrivWhereClause(GrouperSession grouperSession,
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String stemColumn, Privilege privilege, boolean considerAllSubject) {
    //by default dont change the HQL
    return false;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.NamingAdapter#hqlFilterStemsWithPrivWhereClause(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege, boolean)
   */
  public boolean hqlFilterStemsWithPrivWhereClause(GrouperSession grouperSession,
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String stemColumn, Privilege privilege, boolean considerAllSubject) {
    //by default dont change the HQL
    return false;
  }

}
