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
import  edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import  edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import  edu.internet2.middleware.subject.Subject;
import  java.util.Set;


/**
 * Decorator that provides parameter validation for {@link NamingResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ValidatingNamingResolver.java,v 1.4 2008-07-21 04:43:58 mchyzer Exp $
 * @since   1.2.1
 */
public class ValidatingNamingResolver extends NamingResolverDecorator {
  // TODO 20070820 DRY w/ access resolution

  private ParameterHelper param;



  /**
   * @since   1.2.1
   */
  public ValidatingNamingResolver(NamingResolver resolver) {
    super(resolver);
    this.param = new ParameterHelper();
  }



  /**
   * @see     NamingResolver#getConfig(String)
   * @throws  IllegalStateException if any parameter is null.
   */
  public String getConfig(String key) 
    throws IllegalStateException
  {
    this.param.notNullString(key);
    return super.getDecoratedResolver().getConfig(key);
  }

  /**
   * @see     NamingResolver#getStemsWhereSubjectHasPrivilege(Subject, Privilege)
   * @since   1.2.1
   */
  public Set<Stem> getStemsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    this.param.notNullSubject(subject).notNullPrivilege(privilege);
    return super.getDecoratedResolver().getStemsWhereSubjectHasPrivilege(subject, privilege);
  }

  /**
   * @see     NamingResolver#getPrivileges(Stem, Subject)
   * @since   1.2.1
   */
  public Set<Privilege> getPrivileges(Stem stem, Subject subject)
    throws  IllegalArgumentException
  {
    this.param.notNullStem(stem).notNullSubject(subject);
    return super.getDecoratedResolver().getPrivileges(stem, subject);
  }

  /**
   * @see     NamingResolver#getSubjectsWithPrivilege(Stem, Privilege)
   * @since   1.2.1
   */
  public Set<Subject> getSubjectsWithPrivilege(Stem stem, Privilege privilege)
    throws  IllegalArgumentException
  {
    this.param.notNullStem(stem).notNullPrivilege(privilege);
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
    this.param.notNullStem(stem).notNullSubject(subject).notNullPrivilege(privilege);
    super.getDecoratedResolver().grantPrivilege(stem, subject, privilege);
  }

  /**
   * @see     NamingResolver#hasPrivilege(Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    this.param.notNullStem(stem).notNullSubject(subject).notNullPrivilege(privilege);
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
    this.param.notNullStem(stem).notNullPrivilege(privilege);
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
    this.param.notNullStem(stem).notNullSubject(subject).notNullPrivilege(privilege);
    super.getDecoratedResolver().revokePrivilege(stem, subject, privilege);
  }            

}

