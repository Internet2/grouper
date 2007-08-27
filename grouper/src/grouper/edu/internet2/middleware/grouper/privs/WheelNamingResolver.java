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
import  edu.internet2.middleware.grouper.Group;
import  edu.internet2.middleware.grouper.GrouperConfig;
import  edu.internet2.middleware.grouper.GrouperRuntimeException;
import  edu.internet2.middleware.grouper.GrouperSession;
import  edu.internet2.middleware.grouper.GroupFinder;
import  edu.internet2.middleware.grouper.Privilege;
import  edu.internet2.middleware.grouper.Stem;
import  edu.internet2.middleware.grouper.SubjectFinder;
import  edu.internet2.middleware.grouper.UnableToPerformException;
import  edu.internet2.middleware.subject.Subject;
import  java.util.Set;


/**
 * Decorator that provides <i>Wheel</i> privilege resolution for {@link NamingResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: WheelNamingResolver.java,v 1.3 2007-08-27 15:46:24 blair Exp $
 * @since   @HEAD@
 */
public class WheelNamingResolver extends NamingResolverDecorator {
  // TODO 20070820 DRY w/ access resolution


  private boolean useWheel    = false;
  private Group   wheelGroup;


  
  /**
   * @since   @HEAD@
   */
  public WheelNamingResolver(NamingResolver resolver) {
    super(resolver);
    // TODO 20070816 this is ugly
    this.useWheel = Boolean.valueOf( this.getConfig( GrouperConfig.PROP_USE_WHEEL_GROUP ) ).booleanValue();
    // TODO 20070816 and this is even worse
    if (this.useWheel) {
      try {
        this.wheelGroup = GroupFinder.findByName(
                            GrouperSession.start( SubjectFinder.findRootSubject() ),
                            this.getConfig( GrouperConfig.PROP_WHEEL_GROUP )
                          );
      }
      catch (Exception e) {
        throw new GrouperRuntimeException( "error initializing WheelNamingResolver: " + e.getMessage(), e );
      }
    }
  }



  /**
   * @see     NamingResolver#getConfig(String)
   * @throws  IllegalStateException if any parameter is null.
   */
  public String getConfig(String key) 
    throws IllegalStateException
  {
    return super.getDecoratedResolver().getConfig(key);
  }

  /**
   * @see     NamingResolver#getStemsWhereSubjectHasPrivilege(Subject, Privilege)
   * @since   @HEAD@
   */
  public Set<Stem> getStemsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    return super.getDecoratedResolver().getStemsWhereSubjectHasPrivilege(subject, privilege);
  }

  /**
   * @see     NamingResolver#getPrivileges(Stem, Subject)
   * @since   @HEAD@
   */
  public Set<Privilege> getPrivileges(Stem stem, Subject subject)
    throws  IllegalArgumentException
  {
    return super.getDecoratedResolver().getPrivileges(stem, subject);
  }

  /**
   * @see     NamingResolver#getSubjectsWithPrivilege(Stem, Privilege)
   * @since   @HEAD@
   */
  public Set<Subject> getSubjectsWithPrivilege(Stem stem, Privilege privilege)
    throws  IllegalArgumentException
  {
    return super.getDecoratedResolver().getSubjectsWithPrivilege(stem, privilege);
  }

  /**
   * @see     NamingResolver#grantPrivilege(Stem, Subject, Privilege)
   * @since   @HEAD@
   */
  public void grantPrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    super.getDecoratedResolver().grantPrivilege(stem, subject, privilege);
  }

  /**
   * @see     NamingResolver#hasPrivilege(Stem, Subject, Privilege)
   * @since   @HEAD@
   */
  public boolean hasPrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    if (this.useWheel) {
      if ( this.wheelGroup.hasMember(subject) ) {
        return true;
      }
    }
    return super.getDecoratedResolver().hasPrivilege(stem, subject, privilege);
  }

  /**
   * @see     NamingResolver#revokePrivilege(Stem, Privilege)
   * @since   @HEAD@
   */
  public void revokePrivilege(Stem stem, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    super.getDecoratedResolver().revokePrivilege(stem, privilege);
  }
            

  /**
   * @see     NamingResolver#revokePrivilege(Stem, Subject, Privilege)
   * @since   @HEAD@
   */
  public void revokePrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    super.getDecoratedResolver().revokePrivilege(stem, subject, privilege);
  }            

}

