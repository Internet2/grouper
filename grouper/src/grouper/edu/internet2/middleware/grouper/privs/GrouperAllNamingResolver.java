/**
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

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.subject.Subject;

/**
 * Decorator that provides <i>GrouperAll</i> privilege resolution for {@link NamingResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperAllNamingResolver.java,v 1.12 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public class GrouperAllNamingResolver extends NamingResolverDecorator {

  /**
   * @see NamingResolver#getStemsWhereSubjectDoesntHavePrivilege(String, Scope, Subject, Privilege, boolean, String)
   */
  public Set<Stem> getStemsWhereSubjectDoesntHavePrivilege(
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString) {
    Set<Stem> stems = super.getDecoratedResolver().getStemsWhereSubjectDoesntHavePrivilege(
        stemId, scope, subject, privilege, considerAllSubject, sqlLikeString);
    return stems;
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#flushCache()
   */
  public void flushCache() {
    super.getDecoratedResolver().flushCache();
  }

  /** */
  private Subject all;

  /**
   * @param resolver 
   * @since   1.2.1
   */
  public GrouperAllNamingResolver(NamingResolver resolver) {
    super(resolver);
    this.all = SubjectFinder.findAllSubject();
  }

  /**
   * @see     NamingResolver#getStemsWhereSubjectHasPrivilege(Subject, Privilege)
   * @since   1.2.1
   */
  public Set<Stem> getStemsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    Set<Stem> stems = super.getDecoratedResolver().getStemsWhereSubjectHasPrivilege(
        subject, privilege);
    //this happens further down in GrouperNonDbNamingAdapter
    //    stems.addAll(super.getDecoratedResolver().getStemsWhereSubjectHasPrivilege(this.all,
    //        privilege));
    return stems;
  }

  /**
   * @see     NamingResolver#hasPrivilege(Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Stem stem, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    if (super.getDecoratedResolver().hasPrivilege(stem, this.all, privilege)) {
      return true;
    }
    return super.getDecoratedResolver().hasPrivilege(stem, subject, privilege);
  }

}
