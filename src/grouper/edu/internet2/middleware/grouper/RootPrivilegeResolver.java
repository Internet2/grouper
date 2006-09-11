/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  edu.internet2.middleware.subject.*;

/** 
 * Privilege resolution (as root) class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: RootPrivilegeResolver.java,v 1.4 2006-09-11 14:00:33 blair Exp $
 * @since   1.1.0
 */
 class RootPrivilegeResolver extends PrivilegeResolver {

  // CONSTRUCTORS //
  protected RootPrivilegeResolver() {
    super();
  } // protected RootPrivilegeResolver()



  // PROTECTED CLASS METHODS //

  // @since   1.1.0
  protected static boolean canSTEM(Stem ns, Subject subj) {
    GrouperSession  s   = ns.getSession();  
    ns.setSession( s.getRootSession() );
    boolean         rv  = PrivilegeResolver.canSTEM(ns, subj);
    ns.setSession(s);
    return rv; 
  } // protected static boolean canSTEM(ns, subj)

  // @since   1.1.0
  protected static boolean canVIEW(Group g, Subject subj) {
    GrouperSession  s   = g.getSession();  
    g.setSession( s.getRootSession() );
    boolean         rv  = PrivilegeResolver.canVIEW(g, subj);
    g.setSession(s);
    return rv; 
  } // protected static boolean canVIEW(g, subj)

  // @since   1.1.0
  protected static boolean isRoot(GrouperSession s) {
    return isRoot(s, s.getSubject());
  } // protected static boolean isRoot(s)

  // @since   1.1.0
  // TODO `PrivilegeResolver.hasPriv()` still requires this variant.
  //      `TestMember`` is an example test that fails otherwise.
  protected static boolean isRoot(GrouperSession s, Subject subj) {
    boolean rv    = false;
    // First check to see if this is GrouperSystem
    if ( SubjectHelper.eq(subj, SubjectFinder.findRootSubject()) ) {
      rv = true;
    }  
    else {
      rv = _isWheel(s, subj);
    }
    return rv;
  } // protected static boolean isRoot(s)


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static boolean _isWheel(GrouperSession s, Subject subj) {
    boolean       rv  = false;
    // TODO Should this be a one-time check or not?
    if (Boolean.valueOf(GrouperConfig.getProperty(GrouperConfig.GWU))) {
      // TODO This has to be a performance killer
      String name = GrouperConfig.getProperty(GrouperConfig.GWG);
      try {
        Group wheel = GroupFinder.findByName(s.getRootSession(), name);
        rv          = wheel.hasMember(subj);
      }
      catch (GroupNotFoundException eGNF) {
        // Group not found.  Oh well.
        ErrorLog.error(RootPrivilegeResolver.class, E.NO_WHEEL_GROUP + name);
      }
    } 
    return rv;
  } // private static boolean _isWheel(subj)

} // class RootPrivilegeResolver

